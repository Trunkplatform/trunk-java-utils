package trunk.java.utils.test;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;
import trunk.java.utils.test.http.TestServer;
import trunk.java.utils.test.http.TestHttpClient;

import java.net.URISyntaxException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestServerTest {

  @Test
  public void shouldReturnCorrectTokenForOauthServer() throws URISyntaxException, InterruptedException {
    TestServer.withServer(host -> {
      TestHttpClient trunkHttpClient = new TestHttpClient();
      return trunkHttpClient.get(host + "/test");
    }, (request, response, cache) -> {
        response.setStatus(HttpResponseStatus.OK);
        cache.f(request.getDecodedPath());
        return response;
    }, (requestContent, resultOfServerInteractions) -> {
      assertThat(requestContent.get(0), is("/test"));
    });

  }
}