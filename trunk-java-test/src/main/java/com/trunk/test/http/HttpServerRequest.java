package com.trunk.test.http;

import java.net.URI;
import java.net.URISyntaxException;
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
    try {
      return new URI(request.getUri()).getPath();
    } catch (URISyntaxException e) {
      return null;
    }
  }

  public String getUri() {
    return request.getUri();
  }

  public Map<String, List<String>> getQueryParameters() {
    return new QueryStringDecoder(request.getUri()).parameters();
  }

  public String getHeader(String name) {
    return request.headers().get(name);
  }

  public ByteBuf getContent() {
    return this.content;
  }

  public void setContent(ByteBuf content) {
    this.content = content;
  }
}
