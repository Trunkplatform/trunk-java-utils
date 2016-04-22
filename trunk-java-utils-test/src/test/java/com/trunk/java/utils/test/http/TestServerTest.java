package com.trunk.java.utils.test.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestServerTest {

  @Test
  public void shouldReturnCorrectURIPathForRequest() throws URISyntaxException, InterruptedException {
    TestServer.<CloseableHttpResponse, String>withServer(host -> {
      TestHttpClient testClient = new TestHttpClient();
      return testClient.get(host + "/test");
    }, (request, response, cache) -> {
      response.setStatus(HttpResponseStatus.OK);
      cache.f(request.getDecodedPath());
      return null;
    }, (requestContent, resultOfServerInteractions) -> {
      assertThat(requestContent.get(0), is("/test"));
    });
  }

  @Test
  public void shouldParseCorrectContent() throws URISyntaxException, InterruptedException {
    TestServer.<CloseableHttpResponse, ByteBuf>withServer(host -> {
      TestHttpClient testClient = new TestHttpClient();
      return testClient.post(host + "/test", "{\"name\":\"foo\"}");
    }, (request, response, cache) -> {
      response.setStatus(HttpResponseStatus.OK);
      cache.f(request.getContent());
      return null;
    }, (requestContent, resultOfServerInteractions) -> {
      assertThat(requestContent.get(0).toString(CharsetUtil.UTF_8), is("{\"name\":\"foo\"}"));
    });
  }

  @Test
  public void shouldReturnCorrectResponse() throws URISyntaxException, InterruptedException {
    TestServer.<CloseableHttpResponse, ByteBuf>withServer(host -> {
      TestHttpClient testClient = new TestHttpClient();
      return testClient.get(host + "/test");
    }, (request, response, cache) -> {
      response.setStatus(HttpResponseStatus.OK);
      response.writeString("hello");
      return null;
    }, (requestContent, resultOfServerInteractions) -> {
      try {
        String responseString = EntityUtils.toString(resultOfServerInteractions.getEntity(), CharsetUtil.UTF_8);
        assertThat(responseString, is("hello"));
      } catch (IOException e) {
      }
    });
  }

  @Test
  public void shouldReturnNotFoundIfUriIsInvalidForOAuthRequest() throws URISyntaxException, InterruptedException {
    TestServer.withOAuthServer(host -> {
      TestHttpClient testClient = new TestHttpClient();
      CloseableHttpResponse response = testClient.get(host + "/test");
      assertThat(response.getStatusLine().getStatusCode(), is(HttpResponseStatus.NOT_FOUND.code()));
    });
  }

  @Test
  public void shouldReturnBadRequestIfNoAccessTokenForOAuthRequest() throws URISyntaxException, InterruptedException {
    TestServer.withOAuthServer(host -> {
      TestHttpClient testClient = new TestHttpClient();
      CloseableHttpResponse response = testClient.get(host + "/oauth/token/info");
      assertThat(response.getStatusLine().getStatusCode(), is(HttpResponseStatus.BAD_REQUEST.code()));
    });
  }

  @Test
  public void shouldReturnUnauthorizedIfNoUsersForOAuthRequest() throws URISyntaxException, InterruptedException {
    TestServer.withOAuthServer(host -> {
      TestHttpClient testClient = new TestHttpClient();
      CloseableHttpResponse response = testClient.get(host + "/oauth/token/info?access_token=123");
      assertThat(response.getStatusLine().getStatusCode(), is(HttpResponseStatus.UNAUTHORIZED.code()));
    });
  }

  @Test
  public void shouldReturnForbiddenIfNoResponseForOAuthRequest() throws URISyntaxException, InterruptedException {
    TestServer.withOAuthServer(host -> {
      TestHttpClient testClient = new TestHttpClient();
      CloseableHttpResponse response = testClient.get(host + "/oauth/token");
      assertThat(response.getStatusLine().getStatusCode(), is(HttpResponseStatus.FORBIDDEN.code()));
    });
  }

  @Test
  public void shouldReturnOKForValidOAuthRequest() throws URISyntaxException, InterruptedException {
    TestServer.withOAuthServer(
      host -> {
        TestHttpClient testClient = new TestHttpClient();
        CloseableHttpResponse response = testClient.post(host + "/oauth/token", "{\"grant_type\":\"test\",\"username\":\"foo\",\"password\":\"pass\"}");
        assertThat(response.getStatusLine().getStatusCode(), is(HttpResponseStatus.OK.code()));
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
  public void shouldReturnCorrectStatusForMultipleOAuthRequests() throws URISyntaxException, InterruptedException {
    TestServer.withOAuthServer(
      host -> {
        CloseableHttpResponse response1 = new TestHttpClient().post(host + "/oauth/token", "{\"grant_type\":\"test\",\"username\":\"foo\",\"password\":\"pass\"}");
        assertThat(response1.getStatusLine().getStatusCode(), is(HttpResponseStatus.OK.code()));
        CloseableHttpResponse response2 = new TestHttpClient().post(host + "/oauth/token", "{\"grant_type\":\"test\",\"username\":\"foo\",\"password\":\"wrong\"}");
        assertThat(response2.getStatusLine().getStatusCode(), is(HttpResponseStatus.FORBIDDEN.code()));
      },
      grantRequest -> new GrantResponse("test", "Bearer", 0, 0),
      grantRequest -> new GrantResponse("test", "Bearer", 0, 0, HttpResponseStatus.FORBIDDEN)
    );
  }

  @Test
  public void shouldGetSameTokenFromServer() throws URISyntaxException, InterruptedException {
    TestServer.<CloseableHttpResponse, ByteBuf>withOAuthAndServer(
      (oAuthHost, serverHost) -> {
        CloseableHttpResponse response = new TestHttpClient().post(oAuthHost + "/oauth/token", "{\"grant_type\":\"test\",\"username\":\"foo\",\"password\":\"pass\"}");
        try {
          String jsonBody = EntityUtils.toString(response.getEntity(), CharsetUtil.UTF_8);
          TestHttpClient testClient = new TestHttpClient();
          return testClient.post(serverHost + "/test", jsonBody);
        } catch (IOException e) {
          return null;
        }
      },
      (request, response, cache) -> {
        cache.f(request.getContent());
        return null;
      },
      (requestContent, resultOfServerInteractions) -> {
        assertThat(requestContent.get(0).toString(CharsetUtil.UTF_8), is("{\"access_token\":\"test\",\"token_type\":\"Bearer\",\"expires_in\":0,\"created_at\":0}"));
      },
      grantRequest -> new GrantResponse("test", "Bearer", 0, 0)
    );
  }

  @Test
  public void shouldSendOAuthTokenToServerAndReturnOK() throws URISyntaxException, InterruptedException {
    final URI[] oauthHost = new URI[1];
    TestServer.<CloseableHttpResponse, HttpResponseStatus>withOAuthAndServer(
      (oAuthHost, serverHost) -> {
        oauthHost[0] = oAuthHost;
        new TestHttpClient().post(oAuthHost + "/oauth/token", "{\"grant_type\":\"test\",\"username\":\"foo\",\"password\":\"pass\"}");
        TestHttpClient testClient = new TestHttpClient();
        return testClient.get(serverHost + "/hello");
      },
      (request, response, cache) -> {
        CloseableHttpResponse tokenInfoResponse = new TestHttpClient().get(oauthHost[0] + "/oauth/token/info?access_token=test");
        response.setStatus(HttpResponseStatus.valueOf(tokenInfoResponse.getStatusLine().getStatusCode()));
        cache.f(response.getResponse().getStatus());
        return null;
      },
      (requestContent, resultOfServerInteractions) -> {
        assertThat(resultOfServerInteractions.getStatusLine().getStatusCode(), is(HttpResponseStatus.OK.code()));
      },
      ImmutableMap.of("test", new OAuthUser(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), false)),
      grantRequest -> new GrantResponse("test", "Bearer", 0, 0)
    );
  }

  @Test
  public void shouldReturnUnauthorizedForInvalidToken() throws URISyntaxException, InterruptedException {
    final URI[] oauthHost = new URI[1];
    TestServer.<CloseableHttpResponse, HttpResponseStatus>withOAuthAndServer(
      (oAuthHost, serverHost) -> {
        oauthHost[0] = oAuthHost;
        new TestHttpClient().post(oAuthHost + "/oauth/token", "{\"grant_type\":\"test\",\"username\":\"foo\",\"password\":\"pass\"}");
        TestHttpClient testClient = new TestHttpClient();
        return testClient.get(serverHost + "/hello");
      },
      (request, response, cache) -> {
        CloseableHttpResponse tokenInfoResponse = new TestHttpClient().get(oauthHost[0] + "/oauth/token/info?access_token=invalid");
        response.setStatus(HttpResponseStatus.valueOf(tokenInfoResponse.getStatusLine().getStatusCode()));
        cache.f(response.getResponse().getStatus());
        return null;
      },
      (requestContent, resultOfServerInteractions) -> {
        assertThat(resultOfServerInteractions.getStatusLine().getStatusCode(), is(HttpResponseStatus.UNAUTHORIZED.code()));
      },
      ImmutableMap.of("test", new OAuthUser(ImmutableList.of(), ImmutableList.of(), ImmutableList.of(), false)),
      grantRequest -> new GrantResponse("test", "Bearer", 0, 0)
    );
  }
}