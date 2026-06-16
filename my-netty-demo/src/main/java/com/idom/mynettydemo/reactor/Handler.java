package com.idom.mynettydemo.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @program: netty
 * @description: Handler = 处理单个连接的 I/O
 * @author: Timo
 * @create: 2026-06-16 15:57
 **/
public class Handler implements Runnable {

    private static final int READ = 0;
    private static final int WRITE = 1;

    private final SocketChannel channel;
    private final SelectionKey key;
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

    private int state = READ;

    public Handler(SocketChannel channel) throws IOException {
        this.channel = channel;
        Selector selector = channel.provider().openSelector();
        key = channel.register(selector, SelectionKey.OP_READ);
        key.attach(this);
    }

    @Override
    public void run() {
        try {
            if (state == READ) {
                read();
            } else if (state == WRITE) {
                write();
            }
        } catch (IOException e) {
            close();
        }
    }

    private void read() throws IOException {
        buffer.clear();
        int n = channel.read(buffer);

        if (n > 0) {
            buffer.flip();
            state = WRITE;
            key.interestOps(SelectionKey.OP_WRITE);
            byte[] dst = new byte[buffer.limit()];
            buffer.get(dst);
            String content = new String(dst);
            System.out.println("开始从客户端读取数据！！！！" + content);
        } else if (n == -1) {
            close();
        }
    }

    private void write() throws IOException {
        channel.write(buffer);
        buffer.clear();
        state = READ;
        byte[] dst = new byte[buffer.limit()];
        buffer.get(dst);
        String content = new String(dst);
        System.out.println("开始写数据！！！！" + content);
        key.interestOps(SelectionKey.OP_READ);
    }

    private void close() {
        try {
            key.cancel();
            channel.close();
            System.out.println("client closed");
        } catch (IOException ignored) {
        }
    }
}
