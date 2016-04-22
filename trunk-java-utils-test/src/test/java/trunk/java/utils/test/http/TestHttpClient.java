package trunk.java.utils.test.http;

import java.io.IOException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class TestHttpClient {

  private CloseableHttpClient closeableHttpClient;

  public TestHttpClient() {
    closeableHttpClient = HttpClients.createDefault();
  }

  public CloseableHttpResponse get(String url) {
    try {
      HttpGet httpGet = new HttpGet(url);
      return closeableHttpClient.execute(httpGet);
    } catch (Exception e) {
      return null;
    }
  }

  public CloseableHttpResponse post(String url, String jsonBody) {
    HttpPost httpPost = new HttpPost(url);
    StringEntity requestEntity = new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
    httpPost.setEntity(requestEntity);
    try {
      return closeableHttpClient.execute(httpPost);
    } catch (IOException e) {
      return null;
    }
  }
}
