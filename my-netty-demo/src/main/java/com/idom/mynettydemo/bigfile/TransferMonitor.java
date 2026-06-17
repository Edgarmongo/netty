package com.idom.mynettydemo.bigfile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @program: netty
 * @description: 传输监控
 * @author: Timo
 * @create: 2026-06-16 23:23
 **/
public class TransferMonitor implements Runnable {
    private final Process process;
    private final String localFile;
    private long lastTransferred = 0;
    private long startTime;

    public TransferMonitor(Process process, String localFile) {
        this.process = process;
        this.localFile = localFile;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                parseAndDisplayProgress(line);
            }

        } catch (Exception e) {
            System.err.println("监控线程异常: " + e.getMessage());
        }
    }

    /**
     * 解析并显示进度
     */
    private void parseAndDisplayProgress(String line) {
        try {
            // rsync 进度格式示例：
            // 1,234,567,890 100%  123.45MB/s    0:05:30 (xfr#1, to-chk=0/1)

            if (line.contains("%")) {
                // 提取百分比
                int percentStart = line.indexOf(' ');
                int percentEnd = line.indexOf('%');
                if (percentStart > 0 && percentEnd > percentStart) {
                    String percentStr = line.substring(percentStart + 1, percentEnd).trim();
                    double percent = Double.parseDouble(percentStr);

                    // 提取传输速度
                    String speed = extractSpeed(line);

                    // 提取已传输数据量
                    long transferred = extractTransferredBytes(line);

                    // 计算剩余时间
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remaining = estimateRemainingTime(elapsed, percent);

                    System.out.printf("进度: %.2f%% 速度: %s 已传输: %s 剩余时间: %s%n",
                            percent, speed, formatBytes(transferred), formatTime(remaining));
                }
            } else if (line.contains("sent") && line.contains("received")) {
                // 传输完成统计
                System.out.println("传输统计: " + line);
            } else if (line.contains("total size")) {
                // 总大小统计
                System.out.println("文件统计: " + line);
            }

        } catch (Exception e) {
            // 忽略解析错误，直接输出原始行
            System.out.println(line);
        }
    }

    /**
     * 提取传输速度
     */
    private String extractSpeed(String line) {
        // 查找类似 "123.45MB/s" 的模式
        int speedStart = line.indexOf("MB/s");
        if (speedStart > 0) {
            int spaceIndex = line.lastIndexOf(' ', speedStart);
            if (spaceIndex > 0) {
                return line.substring(spaceIndex + 1, speedStart + 4);
            }
        }
        return "未知";
    }

    /**
     * 提取已传输字节数
     */
    private long extractTransferredBytes(String line) {
        try {
            // 查找第一个数字序列
            String[] parts = line.split("\\s+");
            for (String part : parts) {
                if (part.matches("[\\d,]+")) {
                    return Long.parseLong(part.replace(",", ""));
                }
            }
        } catch (Exception e) {
            // 忽略
        }
        return 0;
    }

    /**
     * 估计剩余时间
     */
    private long estimateRemainingTime(long elapsed, double percent) {
        if (percent <= 0) return 0;
        double totalEstimated = elapsed / (percent / 100.0);
        return (long) (totalEstimated - elapsed);
    }

    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 格式化时间
     */
    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if (hours > 0) {
            return String.format("%d小时%d分%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
