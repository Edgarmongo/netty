package com.idom.mynettydemo.proactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @program: netty
 * @description: proactor
 * App 发起异步 I/O
 * ↓
 * 内核完成 I/O（数据已拷到用户缓冲区）
 * ↓
 * 通知 App：I/O 完成了
 * ↓
 * App 处理数据
 * @author: Timo
 * @create: 2026-06-16 16:18
 **/
public class ProactorServer {

    public ProactorServer(int port) throws Exception {
        // 1. 创建异步服务器通道
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        System.out.println("Proactor Echo Server started on port :" + port);
        acceptAndProcess(server);
    }

    /**
     * CompletionHandler:这个类是程序处理内核完整IO操作后返回的数据入口
     *
     * @param server
     * @throws Exception
     */
    public void acceptAndProcess(AsynchronousServerSocketChannel server) throws Exception {


        // 2. 开始接受连接（异步）
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel client, Void attachment) {
                // 3. 继续接受下一个连接（不阻塞）
                server.accept(null, this);

                try {
                    System.out.println("Client connected: " + client.getRemoteAddress());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // 4. 为每个连接创建处理器
//                new ClientHandler(client).start();
//                new ClientV2Handler(client).start();
                new ClientV3Handler(client).start();
            }

            /**
             * 接收连接失败
             * @param exc
             *          The exception to indicate why the I/O operation failed
             * @param attachment
             *          The object attached to the I/O operation when it was initiated.
             */
            @Override
            public void failed(Throwable exc, Void attachment) {
                System.err.println("Accept failed: " + exc.getMessage());
            }
        });

        // 5. 主线程不退出
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
