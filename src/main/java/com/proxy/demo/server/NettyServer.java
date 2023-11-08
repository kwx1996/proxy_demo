package com.proxy.demo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyServer {

    public static void bind(int port) throws Exception {
        // 首先创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // 负责客户端上线
        EventLoopGroup workerrGroup = new NioEventLoopGroup(); // 业务线程组
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerrGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(4096));
                            socketChannel.pipeline().addLast(new NettyServerChannelHanlder());
                        }
                    });
            // 绑定端口,同步等待成功
            ChannelFuture f = b.bind(port).sync();
            System.out.println("服务器启动成功");
            // 等待服务端监听端口关闭
            f.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    // 优雅退出
                    bossGroup.shutdownGracefully();
                    workerrGroup.shutdownGracefully();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("服务器启动失败");
            // 优雅退出
            bossGroup.shutdownGracefully();
            workerrGroup.shutdownGracefully();
        }
    }
}