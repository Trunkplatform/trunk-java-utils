package com.trunk.test.http;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HttpHeaders;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.logging.LogLevel;
import io.netty.util.CharsetUtil;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import org.testng.annotations.Test;
import rx.Observable;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RxTestServerTest {
  @Test
  public void shouldReturnCorrectURIPathForRequest() throws Exception {
    TestServer.<HttpClientResponse<ByteBuf>, String>withServer(
      host -> createClient(host, false).flatMap(
        client ->
          client.createGet(host.toString() + "/test")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .timeout(5, TimeUnit.SECONDS)
      ).toBlocking().first(),
      (request, response, cache) -> {
        response.setStatus(HttpResponseStatus.OK);
        cache.f(request.getDecodedPath());
      },
      (resultOfServerInteractions, requestContent) -> {
        assertThat(requestContent.get(0), is("/test"));
      }
    );
  }

  @Test
  public void shouldParseCorrectContent() throws Exception {
    TestServer.<HttpClientResponse<ByteBuf>, ByteBuf>withServer(
      host -> createClient(host, false).flatMap(
        client ->
          client.createPost(host.toString() + "/test")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .writeStringContent(Observable.just("{", "\"", "name", "\"", ":", "\"", "foo", "\"", "}"))
            .timeout(5, TimeUnit.SECONDS)
      ).toBlocking().first()
      ,
      (request, response, cache) -> {
        response.setStatus(HttpResponseStatus.OK);
        cache.f(request.getContent());
      },
      (resultOfServerInteractions, requestContent) -> {
        assertThat(requestContent.get(0).toString(CharsetUtil.UTF_8), is("{\"name\":\"foo\"}"));
      }
    );
  }

  @Test
  public void shouldReturnCorrectResponse() throws Exception {
    TestServer.<HttpClientResponse<ByteBuf>, ByteBuf>withServer(
      host -> createClient(host, false).flatMap(
        client ->
          client.createGet(host.toString() + "/test")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .timeout(5, TimeUnit.SECONDS)
      ).toBlocking().first()
      ,
      (request, response, cache) -> {
        response.setStatus(HttpResponseStatus.OK);
        response.writeString("hello");
      },
      (resultOfServerInteractions, requestContent) -> {
        String responseString = resultOfServerInteractions.getContent().toBlocking().single().toString(CharsetUtil.UTF_8);
        assertThat(responseString, is("hello"));
      }
    );
  }

  @Test
  public void shouldReturnOKForValidOAuthInfoRequest() throws Exception {
    TestServer.withOAuth(
      host -> {
        HttpClientResponse<ByteBuf> response = createClient(host, false).flatMap(
          client -> client.createGet(host.toString() + "/oauth/token/info?access_token=test")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .timeout(5, TimeUnit.SECONDS)
        ).toBlocking().first();
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
      },
      ImmutableMap.of("test", new OAuthUser(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), false))
    );
  }

  @Test
  public void shouldReturnNotFoundIfUriIsInvalidForOAuthRequest() throws Exception {
    TestServer.withOAuth(host -> {
      HttpClientResponse<ByteBuf> response = createClient(host, false).flatMap(
        client -> client.createGet(host.toString() + "/test")
          .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .timeout(5, TimeUnit.SECONDS)
      ).toBlocking().first();
      assertThat(response.getStatus(), is(HttpResponseStatus.NOT_FOUND));
    });
  }

  @Test
  public void shouldReturnBadRequestIfNoAccessTokenForOAuthRequest() throws Exception {
    TestServer.withOAuth(host -> {
      HttpClientResponse<ByteBuf> response = createClient(host, false).flatMap(
        client -> client.createGet(host.toString() + "/oauth/token/info")
          .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .timeout(5, TimeUnit.SECONDS)
      ).toBlocking().first();
      assertThat(response.getStatus(), is(HttpResponseStatus.BAD_REQUEST));
    });
  }

  @Test
  public void shouldReturnUnauthorizedIfNoUsersForOAuthRequest() throws Exception {
    TestServer.withOAuth(host -> {
      HttpClientResponse<ByteBuf> response = createClient(host, false).flatMap(
        client -> client.createGet(host.toString() + "/oauth/token/info?access_token=123")
          .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .timeout(5, TimeUnit.SECONDS)
      ).toBlocking().first();
      assertThat(response.getStatus(), is(HttpResponseStatus.UNAUTHORIZED));
    });
  }

  @Test
  public void shouldReturnForbiddenIfNoResponseForOAuthRequest() throws Exception {
    TestServer.withOAuth(host -> {
      HttpClientResponse<ByteBuf> response = createClient(host, false).flatMap(
        client -> client.createGet(host.toString() + "/oauth/token")
          .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .timeout(5, TimeUnit.SECONDS)
      ).toBlocking().first();
      assertThat(response.getStatus(), is(HttpResponseStatus.FORBIDDEN));
    });
  }

  @Test
  public void shouldReturnOKForValidOAuthRequest() throws Exception {
    TestServer.withOAuth(
      host -> {
        HttpClientResponse<ByteBuf> response = createClient(host, false).flatMap(
          client -> client.createGet(host.toString() + "/oauth/token")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .writeStringContent(
              Observable.just("{", "\"", "grant_type", "\"", ":", "\"", "test", "\"")
                .concatWith(Observable.just(",", "\"", "username", "\"", ":", "\"", "foo"))
                .concatWith(Observable.just("\"", ",", "\"", "password", "\"", ":", "\"", "pass", "\"", "}"))
            )
            .timeout(5, TimeUnit.SECONDS)
        ).toBlocking().first();
        assertThat(response.getStatus(), is(HttpResponseStatus.OK));
      },
      grantRequest -> {
        if (grantRequest.getUsername().equals("foo") && grantRequest.getGrantType().equals("test") && grantRequest.getPassword().equals("pass")) {
          return new GrantResponse("the-token", "Bearer", 0, 0);
        }
        return new GrantResponse("test", "Bearer", 0, 0, HttpResponseStatus.FORBIDDEN);
      }
    );
  }

  @Test
  public void shouldReturnCorrectStatusForMultipleOAuthRequests() throws Exception {
    TestServer.withOAuth(
      host -> {
        HttpClientResponse<ByteBuf> response1 = createClient(host, false).flatMap(
          client -> client.createGet(host.toString() + "/oauth/token")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .writeStringContent(
              Observable.just("{", "\"", "grant_type", "\"", ":", "\"", "test", "\"")
                .concatWith(Observable.just(",", "\"", "username", "\"", ":", "\"", "foo"))
                .concatWith(Observable.just("\"", ",", "\"", "password", "\"", ":", "\"", "pass", "\"", "}"))
            )
            .timeout(5, TimeUnit.SECONDS)
        ).toBlocking().first();
        assertThat(response1.getStatus(), is(HttpResponseStatus.OK));

        HttpClientResponse<ByteBuf> response2 = createClient(host, false).flatMap(
          client -> client.createGet(host.toString() + "/oauth/token")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .writeStringContent(
              Observable.just("{", "\"", "grant_type", "\"", ":", "\"", "test", "\"")
                .concatWith(Observable.just(",", "\"", "username", "\"", ":", "\"", "foo"))
                .concatWith(Observable.just("\"", ",", "\"", "password", "\"", ":", "\"", "wrong", "\"", "}"))
            )
            .timeout(5, TimeUnit.SECONDS)
        ).toBlocking().first();
        assertThat(response2.getStatus(), is(HttpResponseStatus.FORBIDDEN));
      },
      grantRequest -> new GrantResponse("test", "Bearer", 0, 0),
      grantRequest -> new GrantResponse("test", "Bearer", 0, 0, HttpResponseStatus.FORBIDDEN)
    );
  }

  public static Observable<HttpClient<ByteBuf, ByteBuf>> createClient(URI url, boolean forceSSL) {
    boolean useSsl = url.getScheme().equals("https");
    int port = url.getPort() > 0 ? url.getPort() : (useSsl ? 443 : 80);

    HttpClient<ByteBuf, ByteBuf> client =
      HttpClient.newClient(url.getHost(), port)
        .enableWireLogging(RxTestServerTest.class.getName(), LogLevel.TRACE)
        .followRedirects(true);

    if (forceSSL || useSsl) {
      return Observable.just(client.unsafeSecure());
    }
    return Observable.just(client);
  }
}
