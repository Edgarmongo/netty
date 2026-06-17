package com.idom.mynettydemo.localfile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @program: netty
 * @description: FileInputStream测试
 * @author: Timo
 * @create: 2026-06-16 19:02
 **/
public class FileInputStreamTest {
    public static void main(String[] args) {
        try (FileInputStream fis = new FileInputStream("D:\\tmp\\biotest.txt");
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int bytesRead;

            while ((bytesRead = channel.read(buffer)) != -1) {
                buffer.flip();
                System.out.print(new String(buffer.array(), 0, bytesRead));
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
