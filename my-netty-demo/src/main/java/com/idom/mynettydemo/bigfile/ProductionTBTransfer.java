package com.idom.mynettydemo.bigfile;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @program: netty
 * @description: 主程序  不完整，不能用
 * @author: Timo
 * @create: 2026-06-16 23:21
 **/
public class ProductionTBTransfer {
    //    ClassLoader.class.getResourceAsStream("config/transfer.properties")
//    private static final String CONFIG_FILE = "config/transfer.properties";
    private static final String LOG_FILE = "logs/transfer.log";

    private String host;
    private int port;
    private String username;
    private String keyFile;
    private String localFile;
    private String remoteFile;
    private boolean useCompression;
    private int timeoutHours;
    private boolean verbose;

    /**
     * 主要处理的方法
     */
    public static boolean process() {
        ProductionTBTransfer transfer = new ProductionTBTransfer();

        try {
            // 1. 加载配置
            if (!transfer.loadConfig()) {
                System.err.println("配置加载失败");
                System.exit(1);
            }

            // 2. 检查系统环境
            if (!SystemChecker.checkAll()) {
                System.err.println("系统环境检查失败");
                System.exit(1);
            }

            // 3. 检查文件
            if (!transfer.validateFiles()) {
                System.err.println("文件验证失败");
                System.exit(1);
            }

            // 4. 检查 SSH 密钥
            if (!SSHKeyManager.validateKey(transfer.keyFile)) {
                System.err.println("SSH 密钥验证失败");
                System.exit(1);
            }

            // 5. 执行传输
            boolean success = transfer.executeTransfer();

            // 6. 退出
            System.exit(success ? 0 : 1);

        } catch (Exception e) {
            logError("传输过程中发生未预期的错误", e);
            System.exit(1);
        }
        return true;
    }

    /**
     * 加载配置文件
     */
    private boolean loadConfig() {
        InputStream resource = ClassLoader.getSystemResourceAsStream("config/transfer.properties");
//        InputStream input = resource.getInputStream();
//        File file = new File("config/transfer.properties");
        try (InputStream input = resource) {
            java.util.Properties prop = new java.util.Properties();
            prop.load(input);

            host = prop.getProperty("host");
            port = Integer.parseInt(prop.getProperty("port", "22"));
            username = prop.getProperty("username");
            keyFile = prop.getProperty("keyFile");
            localFile = prop.getProperty("localFile");
            remoteFile = prop.getProperty("remoteFile");
            useCompression = Boolean.parseBoolean(prop.getProperty("useCompression", "true"));
            timeoutHours = Integer.parseInt(prop.getProperty("timeoutHours", "24"));
            verbose = Boolean.parseBoolean(prop.getProperty("verbose", "false"));

            // 验证必需参数
            if (host == null || username == null || keyFile == null ||
                    localFile == null || remoteFile == null) {
                System.err.println("配置文件缺少必需参数");
                return false;
            }

            logInfo("配置加载成功");
            logInfo("主机: " + host + ":" + port);
            logInfo("用户: " + username);
            logInfo("本地文件: " + localFile);
            logInfo("远程文件: " + remoteFile);
            logInfo("使用压缩: " + useCompression);
            logInfo("超时时间: " + timeoutHours + " 小时");

            return true;

        } catch (IOException e) {
            logError("加载配置文件失败", e);
            return false;
        }
    }

    /**
     * 验证文件
     */
    private boolean validateFiles() {
        try {
            // 检查本地文件
            Path localPath = Paths.get(localFile);
            if (!Files.exists(localPath)) {
                System.err.println("本地文件不存在: " + localFile);
                return false;
            }

            if (!Files.isRegularFile(localPath)) {
                System.err.println("不是普通文件: " + localFile);
                return false;
            }

            long fileSize = Files.size(localPath);
            if (fileSize == 0) {
                System.err.println("文件大小为0: " + localFile);
                return false;
            }

            logInfo("本地文件验证通过，大小: " + formatBytes(fileSize));

            // 检查远程目录
            String remoteDir = remoteFile.substring(0, remoteFile.lastIndexOf('/'));
            if (remoteDir.isEmpty()) {
                remoteDir = "/";
            }

            logInfo("远程目录: " + remoteDir);

            return true;

        } catch (IOException e) {
            logError("文件验证失败", e);
            return false;
        }
    }

    /**
     * 执行传输
     */
    private boolean executeTransfer() {
        Process process = null;

        try {
            // 构建 rsync 命令
            String[] command = buildRsyncCommand();

            logInfo("开始执行 rsync 传输...");
            logInfo("命令: " + String.join(" ", command));

            // 创建进程
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true); // 合并错误输出到标准输出
            process = pb.start();

            // 启动监控线程
            TransferMonitor monitor = new TransferMonitor(process, localFile);
            Thread monitorThread = new Thread(monitor);
            monitorThread.start();

            // 等待进程完成
            boolean finished = process.waitFor(timeoutHours, TimeUnit.HOURS);

            if (!finished) {
                logError("传输超时，强制终止进程", null);
                process.destroyForcibly();
                return false;
            }

            int exitCode = process.exitValue();

            if (exitCode == 0) {
                logInfo("✅ 传输成功完成！");
                return true;
            } else {
                logError("❌ 传输失败，退出码: " + exitCode, null);
                return false;
            }

        } catch (Exception e) {
            logError("执行传输时发生错误", e);
            return false;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * 构建 rsync 命令
     */
    private String[] buildRsyncCommand() {
        java.util.ArrayList<String> command = new java.util.ArrayList<>();

        command.add("rsync");

        // 基本参数
        command.add("-avz");  // 归档模式、显示进度、压缩

        // 断点续传支持
        command.add("--partial");      // 保留部分传输的文件
        command.add("--inplace");     // 原地更新文件，节省空间

        // 进度显示
        if (verbose) {
            command.add("--progress"); // 显示详细进度
        }

        // 超时设置
        command.add("--timeout=300");  // 5分钟无数据传输则超时

        // SSH 配置
        String sshOptions = String.format(
                "ssh -p %d -i %s -o StrictHostKeyChecking=no -o BatchMode=yes -o ConnectTimeout=30",
                port, keyFile
        );
        command.add("-e");
        command.add(sshOptions);

        // 排除文件（可选）
        // command.add("--exclude=*.tmp");
        // command.add("--exclude=*.log");

        // 带宽限制（可选，单位 KB/s）
        // command.add("--bwlimit=10240");  // 限制为 10MB/s

        // 源文件和目标
        command.add(localFile);
        command.add(String.format("%s@%s:%s", username, host, remoteFile));

        return command.toArray(new String[0]);
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
     * 日志记录
     */
    private static void logInfo(String message) {
        System.out.println("[INFO] " + message);
        appendToLog("[INFO] " + message);
    }

    private static void logError(String message, Exception e) {
        System.err.println("[ERROR] " + message);
        appendToLog("[ERROR] " + message);
        if (e != null) {
            e.printStackTrace(System.err);
            appendToLog(e.toString());
        }
    }

    private static void appendToLog(String message) {
        try {
            Files.createDirectories(Paths.get(LOG_FILE).getParent());
            try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
                fw.write(java.time.LocalDateTime.now() + " " + message + "\n");
            }
        } catch (IOException e) {
            // 忽略日志写入错误
        }
    }
}
