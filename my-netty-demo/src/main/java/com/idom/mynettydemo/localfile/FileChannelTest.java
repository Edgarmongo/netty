package com.idom.mynettydemo.localfile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @program: netty
 * @description: 使用FileChannel读取本地文件并打印到控制台
 * @author: Timo
 * @create: 2026-06-16 18:50
 **/
public class FileChannelTest {

    public static void main(String[] args) {
        // 1. 指定要读取的文件路径
        Path path = Paths.get("D:\\tmp\\biotest.txt");

        // 2. 使用 try-with-resources 确保资源被正确关闭
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {

            // 3. 创建缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            /**
             * 调用后，当前线程会阻塞
             * 直到：
             * 数据从磁盘读入内核缓冲区
             * 再从内核缓冲区拷贝到你的 ByteBuffer
             * 才会返回
             */
            // 4. 读取文件内容
            int bytesRead = channel.read(buffer);

            while (bytesRead != -1) {
                System.out.println("读取的字节数: " + bytesRead);

                // 5. 切换为读模式
                buffer.flip();

                // 6. 读取缓冲区中的数据
                while (buffer.hasRemaining()) {
                    byte[] aaa = new byte[buffer.limit()];
                    buffer.get(aaa);
                    String content = new String(aaa);
                    System.out.print(content);
                }

                // 7. 清空缓冲区，准备下一次读取
                buffer.clear();
                bytesRead = channel.read(buffer);
            }

            System.out.println("\n文件读取完成！");

        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
