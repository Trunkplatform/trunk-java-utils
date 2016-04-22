package com.trunk.java.utils.test.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.logging.LogLevel;
import io.netty.util.CharsetUtil;

public class TestServer {

  public static final Gson GSON = new Gson();

  public static <T, S> void withOAuthAndServer(GivenWithOAuth<T> given, HandleRequest<S> handleRequest, Then<T, S> then, Map<String, OAuthUser> users, HandleOAuth... responses) throws URISyntaxException, InterruptedException {
    withOAuthAndServer(given, handleRequest, then, getRandomPort(), getRandomPort(), users, responses);
  }

  public static <T, S> void withOAuthAndServer(GivenWithOAuth<T> given, HandleRequest<S> handleRequest, Then<T, S> then, HandleOAuth... responses) throws URISyntaxException, InterruptedException {
    withOAuthAndServer(given, handleRequest, then, getRandomPort(), getRandomPort(), ImmutableMap.of(), responses);
  }

  public static <T, S> void withOAuthAndServer(GivenWithOAuth<T> given, HandleRequest<S> handleRequest, Then<T, S> then, int oAuthPort, int serverPort, Map<String, OAuthUser> users, HandleOAuth... responses) throws URISyntaxException, InterruptedException {
    withOAuthServer(
      oAuthHost ->
        withServer(
          serverHost -> given.f(oAuthHost, serverHost),
          handleRequest,
          then, serverPort
        ),
      users,
      oAuthPort,
      responses
    );
  }

  public static <T, S> void withServer(Given<T> given, HandleRequest<S> handleRequest, Then<T, S> then) throws URISyntaxException, InterruptedException {
    withServer(given, handleRequest, then, getRandomPort());
  }

  private static int getRandomPort() {
    return 20000 + new Random().nextInt(40000);
  }

  public static <T, S> void withServer(Given<T> given, HandleRequest<S> handleRequest, Then<T, S> then, int port) throws URISyntaxException, InterruptedException {
    List<S> requestContent = new ArrayList<>();
    HttpServer server = null;
    try {
      server = HttpServer.newServer()
        .withLogging(LogLevel.INFO)
        .setPort(port)
        .start(
          (request, response) -> {
            try {
              return handleRequest.f(
                request,
                response,
                e -> requestContent.add(e)
              );
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        );

      T result = given.f(new URI(String.format("http://localhost:%s", port)));

      then.f(requestContent, result);
    } finally {
      if (server != null) {
        server.shutdown();
      }
    }
  }

  public static void withOAuthServer(OAuthInteraction given, HandleOAuth... responses) throws URISyntaxException, InterruptedException {
    withOAuthServer(given, getRandomPort(), responses);
  }

  public static void withOAuthServer(OAuthInteraction given, int oAuthPort, HandleOAuth... responses) throws URISyntaxException, InterruptedException {
    withOAuthServer(given, ImmutableMap.of(), oAuthPort, responses);
  }

  public static void withOAuthServer(OAuthInteraction given, Map<String, OAuthUser> users, HandleOAuth... responses) throws URISyntaxException, InterruptedException {
    withOAuthServer(given, users, getRandomPort(), responses);
  }

  public static void withOAuthServer(OAuthInteraction given, Map<String, OAuthUser> users, int oAuthPort, HandleOAuth... responses) throws URISyntaxException, InterruptedException {
    final int[] i = { 0 };
    HttpServer server = null;
    try {
      server = HttpServer.newServer()
        .withLogging(LogLevel.INFO)
        .setPort(oAuthPort)
        .start(
          (request, response) -> {
            if (!(request.getDecodedPath().equals("/oauth/token") || request.getDecodedPath().equals("/oauth/token/info"))) {
              response.setStatus(HttpResponseStatus.NOT_FOUND);
              return response;
            }

            if (getUserTokenInfo(users, request, response)) {
              return response;
            }

            if (responses.length == 0) {
              response.setStatus(HttpResponseStatus.FORBIDDEN);
              return response;
            }

            parseGrantResponse(i, request, response, responses);
            return response;
          }
        );
      given.f(new URI(String.format("http://localhost:%s", oAuthPort)));
    } finally {
      if (server != null) {
        server.shutdown();
      }
    }
  }

  private static void parseGrantResponse(int[] i, HttpServerRequest request, HttpServerResponse response, HandleOAuth[] responses) {GrantRequest grantRequest = parseGrantRequest(request.getContent());
    GrantResponse grantResponse = responses[i[0]].f(grantRequest);
    if (i[0] < responses.length - 1) {
      i[0] += 1;
    }
    String json = grantResponse.asJson();
    if (json != null) {
      response.addHeader("Content-Type", "application/json; charset=utf-8");
      response.setStatus(grantResponse.getStatusCode());
      response.writeString(json);
    } else {
      response.setStatus(grantResponse.getStatusCode());
    }
  }

  private static boolean getUserTokenInfo(Map<String, OAuthUser> users, HttpServerRequest request, HttpServerResponse response) {
    if (request.getDecodedPath().equals("/oauth/token/info")) {
      if (request.getQueryParameters().containsKey("access_token") && !request.getQueryParameters().get("access_token").isEmpty()) {
        String key = request.getQueryParameters().get("access_token").get(0);
        if (users.containsKey(key)) {
          response.writeString(GSON.toJson(users.get(key)));
        } else {
          response.setStatus(HttpResponseStatus.UNAUTHORIZED);
        }
      } else {
        response.setStatus(HttpResponseStatus.BAD_REQUEST);
      }
      return true;
    }
    return false;
  }

  private static GrantRequest parseGrantRequest(ByteBuf content) {
    String json = content.toString(CharsetUtil.UTF_8);
    JsonObject o = new JsonParser().parse(json).getAsJsonObject();
    return new GrantRequest(o.get("grant_type").getAsString(),
                            o.get("username").getAsString(),
                            o.get("password").getAsString());
  }

  public interface OAuthInteraction {
    void f(URI host) throws URISyntaxException, InterruptedException;
  }

  public interface GivenWithOAuth<T> {
    T f(URI oAuthHost, URI serverHost) throws URISyntaxException;
  }

  public interface Given<T> {
    T f(URI host) throws URISyntaxException;
  }

  public interface Then<T, S> {
    void f(List<S> requestContent, T resultOfServerInteractions);
  }

  public interface HandleRequest<S> {
    S f(HttpServerRequest request, HttpServerResponse response, CacheRequestContent<S> cacheRequestContent) throws IOException;
  }

  public interface CacheRequestContent<S> {
    void f(S requestContent);
  }

  public interface HandleOAuth {
    GrantResponse f(GrantRequest request);
  }
}
