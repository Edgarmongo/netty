package com.idom.mynettydemo.proactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @program: netty
 * @description: 调整版本
 * @author: Timo
 * @create: 2026-06-16 17:25
 **/
public class ClientV3Handler {

    private final AsynchronousSocketChannel client;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    public ClientV3Handler(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public void start() {
        read();
    }

    private void read() {
        readBuffer.clear();
        client.read(readBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer n, Void a) {
                if (n > 0) {
                    readBuffer.flip();
                    String msg = new String(readBuffer.array(), 0, readBuffer.limit());
                    msg = msg.trim() + "\n";

                    writeBuffer.clear();
                    writeBuffer.put(msg.getBytes());
                    writeBuffer.flip();

                    write();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                close();
            }
        });
    }
    private void write() {
        client.write(writeBuffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer n, Void a) {
                if (writeBuffer.hasRemaining()) {
                    client.write(writeBuffer, null, this);
                } else {
                    read();
                }
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                close();
            }
        });
    }

    private void close() {
        try {
            client.close();
            System.out.println("Client disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
