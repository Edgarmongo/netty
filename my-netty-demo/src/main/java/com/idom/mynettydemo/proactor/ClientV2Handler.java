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
public class ClientV2Handler {

    private final AsynchronousSocketChannel client;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(1024);

    public ClientV2Handler(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public void start() {
        read();
    }

    private void read() {
        client.read(readBuffer, readBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer n, ByteBuffer buffer) {
                if (n > 0) {
                    buffer.flip();
                    String msg = new String(buffer.array(), 0, buffer.limit());
                    System.out.println("服务器返回：" + msg);

                    // 准备写回
                    writeBuffer.clear();
                    writeBuffer.put(msg.getBytes());
                    writeBuffer.flip();

                    write(writeBuffer);
                } else if (n == -1) {
                    close();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
                close();
            }
        });
    }

    private void write(ByteBuffer buffer) {
        client.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer n, ByteBuffer buffer) {
                if (buffer.hasRemaining()) {
                    // 没写完，继续写
                    client.write(buffer, buffer, this);
                } else {
                    // 写完了，清空并继续读
                    buffer.clear();
                    read();
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buffer) {
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
