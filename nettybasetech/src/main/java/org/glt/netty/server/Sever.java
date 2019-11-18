package org.glt.netty.server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.glt.netty.Util.HostInfo;


public class Sever{
    public static void main(String[] args) throws Exception {
        new EchoServer1().run();
    }
}

/**
 * 实现了基础的线程池与网络连接的配置项
 */
class EchoServer1 {
    public void run() throws Exception {    // 进行服务器端的启动处理
        // 线程池是提升服务器性能的重要技术手段，利用定长的线程池可以保证核心线程的有效数量
        // 在Netty之中线程池的实现分为两类：主线程池（接收客户端连接）、工作线程池（处理客户端连接）
        EventLoopGroup bossGroup = new NioEventLoopGroup(10); // 创建接收线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup(20); // 创建工作线程池
        System.out.println("服务器启动成功，监听端口为：" + HostInfo.PORT);
        try {
            // 创建一个服务器端的程序类进行NIO启动，同时可以设置Channel
            ServerBootstrap serverBootstrap = new ServerBootstrap();   // 服务器端
            // 设置要使用的线程池以及当前的Channel类型
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
            // 接收到信息之后需要进行处理，于是定义子处理器
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new FixedLengthFrameDecoder(100)) ;
                    socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024)) ;
                    socketChannel.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8)) ;
                    socketChannel.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8)) ;
                    socketChannel.pipeline().addLast(new EchoServerHandler1()); // 追加了处理器
                }
            });
            // 可以直接利用常亮进行TCP协议的相关配置
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 128);
            serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            // ChannelFuture描述的时异步回调的处理操作
            ChannelFuture future = serverBootstrap.bind(HostInfo.PORT).sync();
            future.channel().closeFuture().sync();// 等待Socket被关闭
        } finally {
            workerGroup.shutdownGracefully() ;
            bossGroup.shutdownGracefully() ;
        }
    }
}


/**
 * 处理Echo的操作方式，其中ChannelInboundHandlerAdapter是针对于数据输入的处理
 * Netty是基于NIO的一种开发框架的封装，这里面和AIO是没有任何关系的。
 */
class EchoServerHandler1 extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close() ;
    }
}



