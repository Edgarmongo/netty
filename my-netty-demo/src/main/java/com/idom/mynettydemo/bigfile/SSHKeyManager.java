package com.idom.mynettydemo.bigfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @program: netty
 * @description: SSH 密钥管理
 * @author: Timo
 * @create: 2026-06-16 23:20
 **/
public class SSHKeyManager {
    /**
     * 验证 SSH 密钥文件
     */
    public static boolean validateKey(String keyFile) {
        try {
            Path keyPath = Paths.get(keyFile);

            // 检查文件是否存在
            if (!Files.exists(keyPath)) {
                System.err.println("SSH 密钥文件不存在: " + keyFile);
                return false;
            }

            // 检查是否是文件
            if (!Files.isRegularFile(keyPath)) {
                System.err.println("不是有效的文件: " + keyFile);
                return false;
            }

            // 检查文件权限（Linux/Unix）
            if (isUnixLike()) {
                if (!checkKeyPermissions(keyFile)) {
                    System.err.println("SSH 密钥文件权限不安全，应为 600");
                    return false;
                }
            }

            // 检查文件内容
            if (!validateKeyContent(keyFile)) {
                System.err.println("SSH 密钥文件内容无效");
                return false;
            }

            System.out.println("SSH 密钥验证通过: " + keyFile);
            return true;

        } catch (Exception e) {
            System.err.println("SSH 密钥验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 检查密钥文件权限
     */
    private static boolean checkKeyPermissions(String keyFile) {
        try {
            File file = new File(keyFile);
            String permissions = getFilePermissions(keyFile);

            // 私钥文件权限应为 600 (rw-------)
            if (!permissions.equals("600") && !permissions.equals("400")) {
                System.err.println("警告: SSH 私钥文件权限应为 600 或 400，当前为: " + permissions);
                return false;
            }

            return true;
        } catch (Exception e) {
            return true; // 忽略权限检查错误
        }
    }

    /**
     * 获取文件权限
     */
    private static String getFilePermissions(String filePath) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"stat", "-c", "%a", filePath});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String permissions = reader.readLine();
            process.waitFor();
            return permissions;
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * 验证密钥内容
     */
    private static boolean validateKeyContent(String keyFile) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(keyFile)));

            // 检查是否是有效的 SSH 私钥
            if (content.contains("BEGIN RSA PRIVATE KEY") ||
                    content.contains("BEGIN OPENSSH PRIVATE KEY") ||
                    content.contains("BEGIN DSA PRIVATE KEY") ||
                    content.contains("BEGIN EC PRIVATE KEY")) {
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否是 Unix-like 系统
     */
    private static boolean isUnixLike() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("mac");
    }

    /**
     * 生成 SSH 密钥对
     */
    public static boolean generateKeyPair(String keyFile, String comment) {
        try {
            String[] command = {
                    "ssh-keygen",
                    "-t", "rsa",
                    "-b", "4096",
                    "-f", keyFile,
                    "-N", "",  // 空密码
                    "-C", comment
            };

            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("SSH 密钥对生成成功: " + keyFile);
                return true;
            } else {
                System.err.println("SSH 密钥对生成失败，退出码: " + exitCode);
                return false;
            }
        } catch (Exception e) {
            System.err.println("生成 SSH 密钥对失败: " + e.getMessage());
            return false;
        }
    }
}
