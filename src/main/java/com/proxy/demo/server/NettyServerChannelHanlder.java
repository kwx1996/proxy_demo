package com.proxy.demo.server;

import com.proxy.demo.client.NettyClient;
import com.proxy.demo.utils.HttpUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import io.netty.util.ReferenceCountUtil;

public class NettyServerChannelHanlder extends ChannelInboundHandlerAdapter {


    private Channel Clientconnect = null;

    /**
     * 收到数据
     * @param ctx 上下文
     * @param msg 收到的数据
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        if (this.Clientconnect != null){ // 如果我们当前管道客户端连接实例已创建，那么我们就不去重复创建了，我们直接转发。

            // 直接进行数据转发
            this.Clientconnect.writeAndFlush(msg);

            return;
        }
        ByteBuf bug = (ByteBuf) msg;
        byte[] req = new byte[bug.readableBytes()];
        bug.readBytes(req);
        String body = new String(req, StandardCharsets.UTF_8);
        HashMap<String, String> header = HttpUtils.getHeader(body);

        /*
        * 我们需要在这里做访问日志
        *
        *
        * **/

        /*
         * 这里我们需要做鉴权操作
         *
         **/

        String temp = header.get("host_temp");
        String ip = "";
        int port = 80;
        if (temp.indexOf(":") != -1){
            String[] split = temp.split(":");
            ip = split[0];
            port = Integer.parseInt(split[1]);
        }else {
            ip = temp;
        }

        // 连接到客户端目标服务器（目标网站服务器）
        this.Clientconnect = NettyClient.connect(ip, port, ctx);

        StringBuffer body2 = new StringBuffer();
        String method = header.get("method");
        if (method.toUpperCase().equals("CONNECT")){ // HTTPS代理请求
            if (this.Clientconnect.isOpen()){ // 我们是否已经连接到目标服务器
                body2.append("HTTP/1.1 200 Connection established" + "\r\n");
            }else{
                body2.append("HTTP/1.1 500 连接目标服务器失败" + "\r\n");
            }
            body2.append("\r\n");
        }else { // HTTP代理请求
            // 重新组装第一行参数（请求方法，请求路径，协议版本）
            body2.append(header.get("method") + " ");
            body2.append(header.get("path") + " ");
            body2.append(header.get("edition") + "\r\n");
            // 重新组装请求头
            header.forEach((key,val)->{
                switch (key){
                    case "path":
                        break;
                    case "edition":
                        break;
                    case "method":
                        break;
                    case "host_temp":
                        break;
                    default:
                        body2.append(key + ": " + val + "\r\n");
                }
            });
            body2.append("\r\n");
        }
        byte[] bytes = body2.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuf firstMessage;
        firstMessage = Unpooled.buffer(bytes.length);
        firstMessage.writeBytes(bytes);

        if (method.toUpperCase().equals("CONNECT")){
            ctx.channel().writeAndFlush(firstMessage); // 如果来源请求是HTTPS请求，那么我们需要跟客户端进行沟通，让客户端发起后续报文（发起后续tls相关握手包）
        }else { // 如果来源请求是HTTP请求，那么我们直接与网站服务器进行沟通
            this.Clientconnect.writeAndFlush(firstMessage);
        }

        // 手动释放资源，防止内存泄漏
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 客户端IP,可与  ctx.channel() 关联做持久化
        String ip = ctx.channel().remoteAddress().toString();
        System.out.println("客户端上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 客户端IP,可与  ctx.channel() 关联做持久化
        String ip = ctx.channel().remoteAddress().toString();
        System.out.println("客户端断开");
        if (this.Clientconnect != null){
            this.Clientconnect.close(); // 关闭网站服务器的连接
        }
        ctx.close();
    }

    /**
     * 数据读取完成后
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 客户端IP,可与  ctx.channel() 关联做持久化
        String ip = ctx.channel().remoteAddress().toString();
        System.out.println("数据读取完成");
        ctx.flush();
    }

    /**
     * 客户端出异常时直接断开
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 客户端IP,可与  ctx.channel() 关联做持久化

        cause.printStackTrace();
        String ip = ctx.channel().remoteAddress().toString();
        System.out.println("连接出现异常");

        if (this.Clientconnect != null){

            this.Clientconnect.close(); // 关闭网站服务器的连接

        }

        ctx.close();
    }

}