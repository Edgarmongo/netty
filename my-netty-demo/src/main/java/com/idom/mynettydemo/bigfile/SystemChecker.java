package com.idom.mynettydemo.bigfile;

import java.io.File;

/**
 * @program: netty
 * @description: 系统检查
 * @author: Timo
 * @create: 2026-06-16 23:22
 **/
public class SystemChecker {
    /**
     * 检查所有系统要求
     */
    public static boolean checkAll() {
        System.out.println("=== 系统环境检查 ===");

        boolean rsyncOk = checkRsync();
        boolean sshOk = checkSsh();
        boolean diskSpaceOk = checkDiskSpace();
        boolean memoryOk = checkMemory();

        boolean allOk = rsyncOk && sshOk && diskSpaceOk && memoryOk;

        System.out.println("=== 检查结果 ===");
        System.out.println("rsync: " + (rsyncOk ? "✅" : "❌"));
        System.out.println("ssh: " + (sshOk ? "✅" : "❌"));
        System.out.println("磁盘空间: " + (diskSpaceOk ? "✅" : "❌"));
        System.out.println("内存: " + (memoryOk ? "✅" : "❌"));
        System.out.println("总体: " + (allOk ? "✅ 通过" : "❌ 失败"));

        return allOk;
    }

    /**
     * 检查 rsync 是否安装
     */
    public static boolean checkRsync() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"rsync", "--version"});
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("✅ rsync 已安装");
                return true;
            } else {
                System.err.println("❌ rsync 未安装或不可用");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ 检查 rsync 失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查 ssh 是否安装
     */
    public static boolean checkSsh() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"ssh", "-V"});
            int exitCode = process.waitFor();

            // ssh -V 通常返回非0退出码，但通过错误输出显示版本
            System.out.println("✅ ssh 已安装");
            return true;
        } catch (Exception e) {
            System.err.println("❌ ssh 未安装或不可用");
            return false;
        }
    }

    /**
     * 检查磁盘空间
     */
    public static boolean checkDiskSpace() {
        try {
            File file = new File(".");
            long usableSpace = file.getUsableSpace();
            long requiredSpace = 10L * 1024 * 1024 * 1024; // 10GB

            if (usableSpace >= requiredSpace) {
                System.out.println("✅ 磁盘空间充足: " + formatBytes(usableSpace));
                return true;
            } else {
                System.err.println("❌ 磁盘空间不足: " + formatBytes(usableSpace));
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ 检查磁盘空间失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查内存
     */
    public static boolean checkMemory() {
        try {
            long maxMemory = Runtime.getRuntime().maxMemory();
            long requiredMemory = 512L * 1024 * 1024; // 512MB

            if (maxMemory >= requiredMemory) {
                System.out.println("✅ 内存充足: " + formatBytes(maxMemory));
                return true;
            } else {
                System.err.println("❌ 内存不足: " + formatBytes(maxMemory));
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ 检查内存失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 格式化字节数
     */
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
