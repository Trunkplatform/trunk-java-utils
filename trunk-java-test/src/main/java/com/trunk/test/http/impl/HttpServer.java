package com.trunk.test.http.impl;

import java.net.InetSocketAddress;

import com.trunk.test.http.HttpServerRequest;
import com.trunk.test.http.HttpServerResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpServer {

  private final EventLoopGroup bossGroup;
  private final EventLoopGroup workerGroup;
  private final ServerBootstrap server;
  private Channel channel;


  public static HttpServer newServer() {
    return new HttpServer();
  }

  private HttpServer() {
    bossGroup = new NioEventLoopGroup(1);
    workerGroup = new NioEventLoopGroup();
    server = new ServerBootstrap();
    server.group(bossGroup, workerGroup)
      .channel(NioServerSocketChannel.class);
  }

  public HttpServer withLogging(LogLevel logLevel) {
    server.handler(new LoggingHandler(logLevel));
    return this;
  }

  public HttpServer start(RequestHandler handler) {
    try {
      server.childHandler(new HttpServerInitializer(handler));
      channel = server.bind(0).sync().channel();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public HttpServer shutdown() {
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
    return this;
  }

  public int getPort() {
    return ((InetSocketAddress) channel.localAddress()).getPort() ;
  }

  public interface RequestHandler<T> {
    void f(HttpServerRequest request, HttpServerResponse response) throws Exception;
  }
}
