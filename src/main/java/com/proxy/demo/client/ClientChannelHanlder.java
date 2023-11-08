package com.proxy.demo.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.StandardCharsets;

import io.netty.util.ReferenceCountUtil;

public class ClientChannelHanlder extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext ServerCtx = null;

    public ClientChannelHanlder(ChannelHandlerContext ctx) {
        this.ServerCtx = ctx;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        System.out.println("客户端与服务端连接成功");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端与服务端断开连接");
        if (this.ServerCtx != null){
            this.ServerCtx.close();
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        System.out.println("客户端连接出现异常");
        if (this.ServerCtx != null){
            this.ServerCtx.close();
        }
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 获取到服务端返回的字节流
        ByteBuf bug = (ByteBuf) msg;
        byte[] req = new byte[bug.readableBytes()];
        bug.readBytes(req);
//        bug.release();
        // 同样的转成String,可根据自定义的协议转换数据类型
        String body = new String(req, StandardCharsets.UTF_8);

        if (this.ServerCtx != null){
            ByteBuf firstMessage;
            firstMessage = Unpooled.buffer(req.length);
            firstMessage.writeBytes(req);
            this.ServerCtx.channel().writeAndFlush(firstMessage);
        }
        ReferenceCountUtil.release(msg);
    }
}