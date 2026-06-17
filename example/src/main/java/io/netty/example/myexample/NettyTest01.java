package io.netty.example.myexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.file.FileServerHandler;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

/**
 * @program: netty
 * @description: 测试案例
 * @author: Timo
 * @create: 2026-06-17 16:31
 **/
public class NettyTest01 {
    public static void main(String[] args) {
        //通过多线程的方式创建MainReactor
        EventLoopGroup mainGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        EventLoopGroup childGroup = new MultiThreadIoEventLoopGroup(10, NioIoHandler.newFactory());

        //服务器启动
        ServerBootstrap server = new ServerBootstrap();
        server.group(mainGroup, childGroup)
                .channel(NioServerSocketChannel.class)//设置channel类型为NIO
                .option(ChannelOption.SO_BACKLOG, 100)//设置连接配置参数
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    public void initChannel(NioSocketChannel ch) throws Exception {
                        //拿到通道管道，并配置入站，出站channel事件
                        ChannelPipeline p = ch.pipeline();
                        Channel channel = p.channel();
                        System.out.println("通道："+channel);
                    }
                });
        // Start the server.
        server.bind(8000).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("端口绑定成功");
            } else {
                System.out.println("端口绑定失败");
            }
        });


    }
}
