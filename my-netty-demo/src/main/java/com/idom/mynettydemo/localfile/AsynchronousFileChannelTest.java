package com.idom.mynettydemo.localfile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;

/**
 * @program: netty
 * @description: AsynchronousFileChannel读取本地文件
 * @author: Timo
 * @create: 2026-06-16 19:07
 **/
public class AsynchronousFileChannelTest {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("D:\\tmp\\biotest.txt");

        AsynchronousFileChannel channel =
                AsynchronousFileChannel.open(path);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        channel.read(buffer, 0, buffer, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                attachment.flip();
                System.out.println(new String(attachment.array()));
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
            }
        });
    }
}
