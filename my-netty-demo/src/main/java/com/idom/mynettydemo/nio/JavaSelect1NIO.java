package com.idom.mynettydemo.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

/**
 * @program: netty
 * @description: Java中的NIO的select实现方式
 * @author: Timo
 * @create: 2026-06-16 13:01
 **/
public class JavaSelect1NIO {


    public static void main(String[] args) throws Exception {
        //创建selector选择器,在Linux中是调用epoll_create()方法
        Selector selector = Selector.open();

        ServerSocketChannel server = ServerSocketChannel.open();
        //将地址和端口绑定到FD
        server.bind(new InetSocketAddress(8081));
        //设置为同步非阻塞
        server.configureBlocking(false);
        //接收来自客户端连接
        server.register(selector, SelectionKey.OP_ACCEPT);
        //开辟JVM对外内存的缓冲区
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        while (true) {
            selector.select(); // 内核 select()

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    SocketChannel client =
                            server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    buffer.clear();
                    int n = client.read(buffer);
                    if (n > 0) {
                        buffer.flip();
                        client.write(buffer);
                    }
                }
            }
            selector.selectedKeys().clear();
        }
    }
}
