package trunk.java.utils.test.http;

import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

public class HttpServerRequest {
  private HttpRequest request;
  private ByteBuf content;

  public HttpServerRequest(HttpRequest request) {
    this.request = request;
  }

  public String getDecodedPath() {
    return new QueryStringDecoder(request.getUri()).path();
  }

  public Map<String, List<String>> getQueryParameters() {
    return new QueryStringDecoder(request.getUri()).parameters();
  }

  public ByteBuf getContent() {
    return this.content;
  }

  public void setContent(ByteBuf content) {
    this.content = content;
  }
}
