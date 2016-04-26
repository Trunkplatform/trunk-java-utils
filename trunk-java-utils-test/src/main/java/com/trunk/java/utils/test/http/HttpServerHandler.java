package com.trunk.java.utils.test.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

  public static final String EMPTY_LAST_HTTP_CONTENT = "EmptyLastHttpContent";
  private final HttpServer.RequestHandler handler;
  private HttpServerRequest request;

  public HttpServerHandler(HttpServer.RequestHandler handler) {
    this.handler = handler;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
    if (msg instanceof HttpRequest) {
      request = new HttpServerRequest((HttpRequest) msg);
    } else if (msg instanceof HttpContent) {
      if (msg instanceof LastHttpContent) {
        if (!msg.toString().equals(EMPTY_LAST_HTTP_CONTENT)) {
          setContent((HttpContent) msg);
        }
        HttpServerResponse response = new HttpServerResponse();
        handler.f(request, response);
        if (response.getResponse().getStatus().equals(HttpResponseStatus.OK)) {
          response.getResponse().headers().set("Content-Length", response.getResponse().content().readableBytes());
        } else {
          response.getResponse().headers().set("Content-Length", 0);
        }
        ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
      } else if (msg instanceof DefaultHttpContent) {
        setContent((HttpContent) msg);
      }
    }
  }

  private void setContent(HttpContent msg) {
    HttpContent msgContent = msg;
    String reqBody = msgContent.content().toString(CharsetUtil.UTF_8);
    ByteBuf buffer = Unpooled.copiedBuffer(reqBody, CharsetUtil.UTF_8);
    request.setContent(buffer);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }
}