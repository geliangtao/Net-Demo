package org.glt.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import javax.xml.crypto.Data;
import java.util.Date;

/**
 * ChannelInboundHandlerAdapter是针对数据输入对应的状态做处理的类
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("channelActive invoker  "+new Date().toLocaleString());
        ctx.writeAndFlush("client 连接成功");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("channelRead invoker,msg="+msg +"\ttime="+new Date().toLocaleString());
        try {
            String inputData = msg.toString().trim();    // 将字节缓冲区的内容转为字符串
            System.err.println("{服务器}" + inputData);
            String echoData = "【ECHO】" + inputData + System.getProperty("line.separator"); // 数据的回应处理
            ctx.writeAndFlush(echoData); // 回应的输出操作
        } finally {
            ReferenceCountUtil.release(msg) ; // 释放缓存
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        System.out.println("channelReadComplete invoker");

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("exceptionCaught invoker");


    }
}
