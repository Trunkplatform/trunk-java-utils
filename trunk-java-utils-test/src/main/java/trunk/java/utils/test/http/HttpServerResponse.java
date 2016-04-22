package trunk.java.utils.test.http;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class HttpServerResponse {
  private FullHttpResponse response;

  public HttpServerResponse() {
    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
  }

  public void setStatus(HttpResponseStatus status) {
    response.setStatus(status);
  }

  public void writeString(String content) {
    response.content().writeBytes(Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
  }

  public FullHttpResponse getResponse() {
    return response;
  }

  public void addHeader(String key, String value) {
    response.headers().add(key, value);
  }
}