package com.proxy.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyClient {
    public static Channel connect(String host, int port, ChannelHandlerContext ServerCtx) throws InterruptedException {

        // 配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.config().setRecvByteBufAllocator(new FixedRecvByteBufAllocator(4096));
                            channel.pipeline().addLast(new ClientChannelHanlder(ServerCtx));
                        }
                    });

            // 发起异步连接
            ChannelFuture f = b.connect(host, port).sync();
            // 等待客户端链路关闭
            f.channel().closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    group.shutdownGracefully();
                }
            });

            return f.channel();
        } catch (Exception e){
            group.shutdownGracefully();
            throw e;
        }
    }
}