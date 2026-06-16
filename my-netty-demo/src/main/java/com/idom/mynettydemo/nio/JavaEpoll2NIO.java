package com.idom.mynettydemo.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @program: netty
 * @description: NIO测试
 * @author: Timo
 * @create: 2026-06-16 13:13
 **/
public class JavaEpoll2NIO {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open(); // ✅ Linux = epoll

        ServerSocketChannel server =
                ServerSocketChannel.open();
        server.bind(new InetSocketAddress(8080));
        server.configureBlocking(false);

        server.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        System.out.println("哈哈哈哈哈哈=====》");
        while (true) {
            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    SocketChannel client =
                            server.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                    System.out.println("接收客户端连接！！！！！");
                }

                if (key.isReadable()) {
                    SocketChannel client =
                            (SocketChannel) key.channel();
                    buffer.clear();
                    int n = client.read(buffer);
                    if (n > 0) {
                        buffer.flip();
                        byte[] dst = new byte[buffer.limit()];
                        buffer.get(dst);
                        String content = new String(dst);
                        System.out.println("有来自客户端的数据：" + content);
                        client.write(buffer);
                    }
                }
            }
            selector.selectedKeys().clear();
        }
    }
}
