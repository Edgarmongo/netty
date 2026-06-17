package com.idom.mynettydemo.localfile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @program: netty
 * @description: 使用FileChannel读取本地文件并打印到控制台
 * 这种方式不需要经过内核，磁盘->内存缓冲区
 * @author: Timo
 * @create: 2026-06-16 18:50
 **/
public class FileChannel2Test {
    private static final int CHUNK_SIZE = 4096; // 4KB 块

    public static void main(String[] args) {
        readAndProcessBlock();
    }

    /**
     * 分块读取数据，防止内存撑爆
     */
    private static void readAndProcessBlock() {

        Path path = Paths.get("D:\\tmp\\biotest.txt");

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long fileSize = channel.size();
            long position = 0;

            System.out.println("文件大小: " + fileSize + " 字节");
            System.out.println("开始读取...");

            while (position < fileSize) {
                // 计算当前块的大小
                long remaining = fileSize - position;
                int chunkSize = (int) Math.min(CHUNK_SIZE, remaining);

                // 映射当前块
                MappedByteBuffer chunk = channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        position,
                        chunkSize
                );

                // 读取当前块的数据
                byte[] bytes = new byte[chunkSize];
                chunk.get(bytes);
                String chunkContent = new String(bytes);

                // 处理当前块的数据（这里只是打印）
                System.out.print(chunkContent);

                // 移动到下一个块
                position += chunkSize;
            }

            System.out.println("\n文件读取完成！");

        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 全量读取数据，文件过大会把内存撑爆
     */
    private static void readAndProcess() {
        // 1. 指定要读取的文件路径
        Path path = Paths.get("D:\\tmp\\biotest.txt");
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            // 1. 将整个文件映射到内存
            long fileSize = channel.size();
            MappedByteBuffer mappedBuffer = channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0,
                    fileSize
            );

            // 2. 直接从内存缓冲区读取数据
            byte[] bytes = new byte[(int) fileSize];
            mappedBuffer.get(bytes);

            // 3. 转换为字符串并输出
            String content = new String(bytes);
            System.out.println("文件内容：");
            System.out.println(content);

//            // 4. 或者逐个字符读取
//            System.out.println("\n逐个字符读取：");
//            mappedBuffer.rewind(); // 重置位置到开头
//            while (mappedBuffer.hasRemaining()) {
//                char c = (char) mappedBuffer.get();
//                System.out.print(c);
//            }

        } catch (IOException e) {
            System.err.println("读取文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
