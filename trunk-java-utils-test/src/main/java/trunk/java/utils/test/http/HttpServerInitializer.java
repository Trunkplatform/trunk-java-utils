package trunk.java.utils.test.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServerInitializer extends ChannelInitializer<Channel> {

  private final HttpServer.RequestHandler handler;

  public HttpServerInitializer(HttpServer.RequestHandler handler) {
    this.handler = handler;
  }

  @Override
  protected void initChannel(Channel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    pipeline.addLast(new HttpServerCodec());
    pipeline.addLast(new HttpServerHandler(handler));
  }
}
