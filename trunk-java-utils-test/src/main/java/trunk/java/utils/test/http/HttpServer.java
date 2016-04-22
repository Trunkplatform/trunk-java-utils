package trunk.java.utils.test.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

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

  public interface RequestHandler<T> {
    T f(HttpServerRequest request, HttpServerResponse response);
  }
}
