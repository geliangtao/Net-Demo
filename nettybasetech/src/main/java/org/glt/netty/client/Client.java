package org.glt.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.glt.netty.Util.HostInfo;


public class Client {

    public static void main(String[] args) throws Exception {
        new EchoClient1().run();
    }
}

/**
 * 需要进行数据的读取操作，服务器端处理完成的数据信息会进行读取
 */
class EchoClientHandler1 extends ChannelInboundHandlerAdapter {
    private static final int REPEAT = 5;// 消息重复发送次数

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        for (int x = 0; x < REPEAT; x++) {  // 消息重复发送
            ctx.writeAndFlush("【" + x + "】Hello World" + System.getProperty("line.separator"));
        }
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

class EchoClient1 {
    public void run() throws Exception {
        // 1、如果现在客户端不同，那么也可以不使用多线程模式来处理;
        // 在Netty中考虑到代码的统一性，也允许你在客户端设置线程池
        EventLoopGroup group = new NioEventLoopGroup(); // 创建一个线程池
        try {
            Bootstrap client = new Bootstrap(); // 创建客户端处理程序
            client.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true) // 允许接收大块的返回数据
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(100)) ;
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024)) ;
                            socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8)) ;
                            socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8)) ;
                            socketChannel.pipeline().addLast(new EchoClientHandler1()); // 追加了处理器
                        }
                    });
            ChannelFuture channelFuture = client.connect(HostInfo.HOST_NAME, HostInfo.PORT).sync();
            channelFuture.channel().closeFuture().sync() ; // 关闭连接
        } finally {
            group.shutdownGracefully();
        }
    }
}



