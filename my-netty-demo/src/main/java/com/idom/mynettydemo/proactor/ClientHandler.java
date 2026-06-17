package com.idom.mynettydemo.proactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @program: netty
 * @description: 客户端proactor
 * @author: Timo
 * @create: 2026-06-16 16:19
 **/
public class ClientHandler {

    private final AsynchronousSocketChannel client;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public ClientHandler(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public void start() {
        // 发起异步读操作
        client.read(buffer, buffer, new CompletionHandler<>() {

            /**
             * 客户端处理这里的completed()方法，说明数据已经读进缓冲区
             * @param bytesRead
             *          The result of the I/O operation.
             * @param attachment
             *          The object attached to the I/O operation when it was initiated.
             */
            @Override
            public void completed(Integer bytesRead, ByteBuffer attachment) {
                if (bytesRead > 0) {
                    //flip()方法是可以完成读写状态切换，如从读编写，同时把position指针归位
                    attachment.flip();
                    String msg = new String(buffer.array(), 0, buffer.limit());
                    System.out.println("服务器返回：" + msg);
                    // 发起异步写操作
                    client.write(attachment, attachment, new CompletionHandler<>() {

                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.clear();
                            // 继续读（循环）
                            client.read(attachment, attachment, this);
                        }

                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            close();
                        }
                    });
                } else if (bytesRead == -1) {
                    close();
                }
            }

            /**
             * 数据读进缓冲区失败
             * @param exc
             *          The exception to indicate why the I/O operation failed
             * @param attachment
             *          The object attached to the I/O operation when it was initiated.
             */
            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
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
