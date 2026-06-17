package com.idom.mynettydemo.localfile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @program: netty
 * @description: 支持大文件上传到远端服务器
 * @author: Timo
 * @create: 2026-06-16 19:40
 **/
public class ResumableFileSender {

    private static final String REMOTE_HOST = "192.168.1.100";
    private static final int REMOTE_PORT = 9000;
    private static final long CHUNK_SIZE = 8 * 1024 * 1024; // 8MB 分块

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("用法: java ResumableFileSender <文件路径> [起始位置]");
            return;
        }

        String filePath = args[0];
        long startPosition = args.length > 1 ? Long.parseLong(args[1]) : 0;

        sendFileWithResume(filePath, startPosition);
    }

    public static void sendFileWithResume(String filePathStr, long startPosition) {
        Path filePath = Paths.get(filePathStr);

        if (!Files.exists(filePath)) {
            System.err.println("文件不存在: " + filePathStr);
            return;
        }

        try (SocketChannel socketChannel = SocketChannel.open();
             FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {

            socketChannel.connect(new InetSocketAddress(REMOTE_HOST, REMOTE_PORT));
            while (!socketChannel.finishConnect()) {
                Thread.sleep(100);
            }

            long fileSize = fileChannel.size();
            long position = startPosition;
            long totalTransferred = startPosition;

            System.out.println("文件: " + filePath.getFileName());
            System.out.println("大小: " + fileSize + " 字节");
            System.out.println("起始位置: " + startPosition + " 字节");

            // 发送文件名和起始位置
            sendFileInfo(socketChannel, filePath.getFileName().toString(), startPosition);

            // 分块传输
            while (position < fileSize) {
                long chunkSize = Math.min(CHUNK_SIZE, fileSize - position);

                long transferred = fileChannel.transferTo(
                        position,
                        chunkSize,
                        socketChannel
                );

                if (transferred <= 0) {
                    break;
                }

                position += transferred;
                totalTransferred += transferred;

                int progress = (int) ((double) totalTransferred / fileSize * 100);
                System.out.printf("进度: %d%% (%d/%d 字节)\r",
                        progress, totalTransferred, fileSize);
            }

            System.out.println("\n传输完成！");

        } catch (Exception e) {
            System.err.println("传输失败: " + e.getMessage());
        }
    }

    private static void sendFileInfo(SocketChannel channel, String fileName, long startPosition)
            throws IOException {
        String info = fileName + "|" + startPosition;
        ByteBuffer buffer = ByteBuffer.wrap(info.getBytes());
        channel.write(buffer);
    }
}
