package com.idom.mynettydemo.localfile;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @program: netty
 * @description: 使用FileChannel读取本地文件并打印到控制台
 * 使用 transferTo 的优势
 * 1. 零拷贝（Zero-Copy）
 * <p>
 * 在传统方式中，数据需要从磁盘 → 内核缓冲区 → 用户缓冲区 → 内核缓冲区 → 目标
 * <p>
 * 使用 transferTo()时，数据直接从磁盘 → 内核缓冲区 → 目标
 * <p>
 * 减少了两次数据拷贝
 * @author: Timo
 * @create: 2026-06-16 18:50
 **/
public class FileChannel3Test {


    public static void main(String[] args) {
        readFromLocal();
    }

    private static void readFromLocal() {
        Path sourcePath = Paths.get("D:\\tmp\\biotest.txt");

        try (FileChannel sourceChannel = FileChannel.open(sourcePath, StandardOpenOption.READ)) {
            // 获取标准输出的通道
            FileChannel targetChannel = FileChannel.open(
                    java.nio.file.Paths.get("D:\\tmp\\destFil\\out11.txt"),
                    StandardOpenOption.WRITE
            );

            long position = 0;
            long totalTransferred = 0;
            long size = sourceChannel.size();

            System.out.println("文件大小: " + size + " 字节");
            System.out.println("文件内容：");

            // 循环传输，直到传输完整个文件
            while (totalTransferred < size) {
                long transferred = sourceChannel.transferTo(
                        position,
                        size - position,
                        targetChannel
                );

                if (transferred <= 0) {
                    break; // 传输结束
                }

                position += transferred;
                totalTransferred += transferred;
            }

            System.out.println("\n文件传输完成！共传输 " + totalTransferred + " 字节");

        } catch (IOException e) {
            System.err.println("传输文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
