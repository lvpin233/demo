package com.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

/**
 * Created by Lvpin on 2019/3/5.
 */
@Component
public class NettyServer {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    ServerBootstrap serverBootstrap = new ServerBootstrap();
    NioEventLoopGroup boss = new NioEventLoopGroup();
    NioEventLoopGroup work = new NioEventLoopGroup();
    private FrameHandler frameHandler;

    @PreDestroy
    public void close() {
        LOGGER.info("关闭服务器");
        boss.shutdownGracefully();
        work.shutdownGracefully();
    }

    public void start() {
        int port = 8083;
        serverBootstrap.group(boss, work)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 100)
                .handler(new LoggingHandler((LogLevel.INFO)));
        try {
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast(new Received(), frameHandler);
                    pipeline.addLast(new LengthFieldPrepender(2));
                }
            });
            LOGGER.info("netty服务器在[{}]端口监听", port);

            ChannelFuture sync = serverBootstrap.bind(port).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.info("[出现异常] 释放资源");
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }

}
