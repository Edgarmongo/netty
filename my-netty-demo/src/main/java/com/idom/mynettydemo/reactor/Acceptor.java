package com.idom.mynettydemo.reactor;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @program: netty
 * @description: Acceptor = 处理新连接
 * @author: Timo
 * @create: 2026-06-16 15:56
 **/
public class Acceptor implements Runnable {

    private final ServerSocketChannel server;

    public Acceptor(ServerSocketChannel server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            SocketChannel client = server.accept();
            if (client != null) {
                client.configureBlocking(false);
                new Handler(client);
                System.out.println("client accepted");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
