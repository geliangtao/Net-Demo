package org.glt.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * 需要进行数据的读取操作，服务器端处理完成的数据信息会进行读取
 */
public class EchoClientHandler extends ChannelInboundHandlerAdapter {
    private static final int REPEAT = 500;// 消息重复发送次数

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("hello netty" + System.getProperty("line.separator")) ; // 直接进行对象实例发送
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 只要服务器端发送完成信息之后，都会执行此方法进行内容的输出操作
        try {
            String readData = msg.toString().trim(); // 接收返回数据内容
            System.out.println(readData); // 输出服务器端的响应内容
        } finally {
            ReferenceCountUtil.release(msg); // 释放缓存
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
