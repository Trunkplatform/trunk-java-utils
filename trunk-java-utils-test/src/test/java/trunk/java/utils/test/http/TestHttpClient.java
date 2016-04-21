package trunk.java.utils.test.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestHttpClient {

  private CloseableHttpClient closeableHttpClient;

  public TestHttpClient() {
    closeableHttpClient = HttpClients.createDefault();
  }

  public String get(String url) {
    try {
      HttpGet httpGet = new HttpGet(url);
      CloseableHttpResponse httpResponse = closeableHttpClient.execute(httpGet);

      BufferedReader reader = new BufferedReader(new InputStreamReader(
        httpResponse.getEntity().getContent()));

      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = reader.readLine()) != null) {
        response.append(inputLine);
      }
      reader.close();

      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        closeableHttpClient.close();
      } catch (IOException e) {
      }
    }
    return null;
  }
}
