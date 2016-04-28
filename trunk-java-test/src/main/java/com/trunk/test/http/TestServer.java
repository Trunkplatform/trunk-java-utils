package com.trunk.test.http;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.trunk.test.http.impl.HttpServer;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.logging.LogLevel;

public class TestServer {

  private static final Gson GSON = new Gson();

  /**
   * @param given call the given OAuth server and test server
   * @param handleRequest handle the incoming requests
   * @param then test the results of given and cached data from handleRequest
   * @param responses the set of response handlers to use. If empty all request will return forbidden. If not, the
   *                  last handler in the list will be replayed indefinitely
   * @throws Exception
   */
  public static <T, S> void withOAuthAndServer(GivenWithOAuth<T> given,
                                               HandleRequest<S> handleRequest,
                                               Then<T, S> then,
                                               HandleOAuth... responses) throws Exception {
    withOAuthAndServer(given, handleRequest, then, ImmutableMap.of(), responses);
  }

  /**
   * @param given call the given OAuth server and test server
   * @param handleRequest handle the incoming requests
   * @param then test the results of given and cached data from handleRequest
   * @param users a list of Bearer token/user pairs for info calls
   * @param responses the set of response handlers to use. If empty all request will return forbidden. If not, the
   *                  last handler in the list will be replayed indefinitely
   * @throws Exception
   */
  public static <T, S> void withOAuthAndServer(GivenWithOAuth<T> given,
                                               HandleRequest<S> handleRequest,
                                               Then<T, S> then,
                                               Map<String, OAuthUser> users,
                                               HandleOAuth... responses) throws Exception {
    withOAuth(
      oAuthHost ->
        withServer(
          serverHost -> given.f(oAuthHost, serverHost),
          handleRequest,
          then
        ),
      users,
      responses
    );
  }

  /**
   * @param given call the given test server
   * @param handleRequest handle the incoming requests
   * @param then test the results of given and cached data from handleRequest
   * @throws Exception
   */
  public static <T, S> void withServer(Given<T> given,
                                       HandleRequest<S> handleRequest,
                                       Then<T, S> then) throws Exception {
    List<S> requestCache = new ArrayList<>();
    HttpServer server = null;
    try {
      server = HttpServer.newServer()
        .withLogging(LogLevel.INFO)
        .start(
          (request, response) ->
            handleRequest.f(
              request,
              response,
              e -> requestCache.add(e)
            )
        );

      T result = given.f(new URI(String.format("http://localhost:%s", server.getPort())));

      then.f(result, requestCache);
    } finally {
      if (server != null) {
        server.shutdown();
      }
    }
  }

  /**
   * @param given call the given OAuth server
   * @param responses the set of response handlers to use. If empty all request will return forbidden. If not, the
   *                  last handler in the list will be replayed indefinitely
   * @throws Exception
   */
  public static void withOAuth(OAuthInteraction given,
                               HandleOAuth... responses) throws Exception {
    withOAuth(given, ImmutableMap.of(), responses);
  }


  /**
   * @param given call the given OAuth server
   * @param users a list of Bearer token/user pairs for info calls
   * @param responses the set of response handlers to use. If empty all request will return forbidden. If not, the
   *                  last handler in the list will be replayed indefinitely
   * @throws Exception
   */
  public static void withOAuth(OAuthInteraction given,
                               Map<String, OAuthUser> users,
                               HandleOAuth... responses) throws Exception {
    final int[] i = { 0 };
    HttpServer server = null;
    try {
      server = HttpServer.newServer()
        .withLogging(LogLevel.INFO)
        .start(
          (request, response) -> {
            if (!(request.getDecodedPath().equals("/oauth/token") || request.getDecodedPath().equals("/oauth/token/info"))) {
              response.setStatus(HttpResponseStatus.NOT_FOUND);
              return;
            }

            if (getUserTokenInfo(users, request, response)) {
              return;
            }

            if (responses.length == 0) {
              response.setStatus(HttpResponseStatus.FORBIDDEN);
              return;
            }

            parseGrantResponse(i, request, response, responses);
          }
        );
      given.f(new URI(String.format("http://localhost:%s", server.getPort())));
    } finally {
      if (server != null) {
        server.shutdown();
      }
    }
  }

  private static void parseGrantResponse(int[] i,
                                        HttpServerRequest request,
                                        HttpServerResponse response,
                                        HandleOAuth[] responses) {
    GrantRequest grantRequest = GrantRequest.from(request.getContent());
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

  private static boolean getUserTokenInfo(Map<String, OAuthUser> users,
                                          HttpServerRequest request,
                                          HttpServerResponse response) {
    if (!request.getDecodedPath().equals("/oauth/token/info")) {
      return false;
    }
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

  /**
   * Test using an OAuth server.
   */
  public interface OAuthInteraction {
    /**
     * @param oAuthHost the address of the OAuth server
     * @throws Exception
     */
    void f(URI oAuthHost) throws Exception;
  }

  /**
   * Perform server interactions using an OAuth server and a test server
   */
  public interface GivenWithOAuth<T> {
    /**
     * @param oAuthHost the address of the OAuth server
     * @param serverHost the address of the test server
     * @return any data to have assertions made against it, such as responses
     * @throws Exception
     */
    T f(URI oAuthHost, URI serverHost) throws Exception;
  }

  /**
   * Perform server interactions using a test server
   */
  public interface Given<T> {
    /**
     * @param serverHost the address of the test server
     * @return any data to have assertions made against it, such as responses
     * @throws Exception
     */
    T f(URI serverHost) throws Exception;
  }

  /**
   * Make assertions against the result of a {@link Given} or {@link GivenWithOAuth} and
   * and data stored using a {@link CacheRequestContent} during {@link HandleRequest}.
   */
  public interface Then<T, S> {
    /**
     * @param resultOfServerInteractions the result of the {@link Given} or {@link GivenWithOAuth}
     * @param requestCache the data cached during {@link HandleRequest}
     * @throws Exception
     */
    void f(T resultOfServerInteractions, List<S> requestCache) throws Exception;
  }

  /**
   * Manage request handling for the test server.
   */
  public interface HandleRequest<S> {
    /**
     * @param request the inbound request
     * @param response the outbound response. Use this write status, headers and data.
     * @param cacheRequestContent used to cache data about the request
     * @throws Exception
     */
    void f(HttpServerRequest request, HttpServerResponse response, CacheRequestContent<S> cacheRequestContent) throws Exception;
  }

  /**
   * Store data during request handling to pass back to the test harness
   */
  public interface CacheRequestContent<S> {
    void f(S requestContent);
  }

  /**
   * Convert a {@link GrantRequest} into a {@link GrantResponse}
   */
  public interface HandleOAuth {
    GrantResponse f(GrantRequest request);
  }
}
