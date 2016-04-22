package trunk.java.utils.test.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

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
      setContent((HttpContent) msg);
      if (msg instanceof LastHttpContent) {
        HttpServerResponse response = new HttpServerResponse();
        handler.f(request, response);
        ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
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