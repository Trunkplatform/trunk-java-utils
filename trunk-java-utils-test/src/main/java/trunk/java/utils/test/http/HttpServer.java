package trunk.java.utils.test.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.Map;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Values.KEEP_ALIVE;

public class HttpServer {

  private EventLoopGroup bossGroup;
  private EventLoopGroup workerGroup;
  private ServerBootstrap server;
  private int port;

  private HttpServer() {
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    server = new ServerBootstrap();
    server.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class);
    port = 8090;
  }

  public static HttpServer newServer() {
    return new HttpServer();
  }

  public HttpServer setPort(int port) {
    this.port = port;
    return this;
  }

  public HttpServer withLogging(LogLevel logLevel) {
    server.handler(new LoggingHandler(logLevel));
    return this;
  }

  public HttpServer start(RequestHandler handler) {
    try {
      server.childHandler(new HttpServerInitializer(handler));
      server.bind(port).sync().channel();
    } catch (Exception e) {
      return null;
    }
    return this;
  }

  public HttpServer shutdown() {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    return this;
  }

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

  private class HttpServerInitializer extends ChannelInitializer<Channel> {

    private final RequestHandler handler;

    public HttpServerInitializer(RequestHandler handler) {
      this.handler = handler;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
      ChannelPipeline pipeline = ch.pipeline();

      pipeline.addLast(new HttpServerCodec());
      pipeline.addLast(new HttpServerHandler(handler));
    }
  }

  private class HttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final RequestHandler handler;
    private HttpServerRequest request;

    public HttpServerHandler(RequestHandler handler) {
      this.handler = handler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
      if (msg instanceof HttpRequest) {
        request = new HttpServerRequest((HttpRequest) msg);
      } else if (msg instanceof LastHttpContent) {
        HttpServerResponse response = new HttpServerResponse();
        handler.f(request, response);
        ctx.writeAndFlush(response.getResponse()).addListener(ChannelFutureListener.CLOSE);
      }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      ctx.flush();
    }
  }

  public class HttpServerRequest {
    private HttpRequest request;

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
      return ((HttpContent) request).content();
    }
  }

  public interface RequestHandler<T> {
    T f(HttpServerRequest request, HttpServerResponse response);
  }
}
