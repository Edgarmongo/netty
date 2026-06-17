package com.idom.mynettydemo.bigfile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @program: netty
 * @description: 断点续传状态管理
 * @author: Timo
 * @create: 2026-06-16 23:23
 **/
public class TransferStateManager {
    private static final String STATE_DIR = ".transfer_states";

    static {
        try {
            Files.createDirectories(Paths.get(STATE_DIR));
        } catch (IOException e) {
            System.err.println("无法创建状态目录: " + e.getMessage());
        }
    }

    /**
     * 保存传输状态
     */
    public static void saveState(String localFile, String remoteFile,
                                 long transferredBytes, long totalBytes,
                                 String checksum) {
        String stateFile = getStateFileName(localFile);

        try (PrintWriter writer = new PrintWriter(new FileWriter(stateFile))) {
            writer.println("# Transfer State");
            writer.println("local_file=" + localFile);
            writer.println("remote_file=" + remoteFile);
            writer.println("transferred_bytes=" + transferredBytes);
            writer.println("total_bytes=" + totalBytes);
            writer.println("checksum=" + checksum);
            writer.println("timestamp=" + System.currentTimeMillis());
            writer.println("status=in_progress");
        } catch (IOException e) {
            System.err.println("保存传输状态失败: " + e.getMessage());
        }
    }

    /**
     * 加载传输状态
     */
    public static TransferState loadState(String localFile) {
        String stateFile = getStateFileName(localFile);
        File file = new File(stateFile);

        if (!file.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            TransferState state = new TransferState();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("local_file=")) {
                    state.localFile = line.substring("local_file=".length());
                } else if (line.startsWith("remote_file=")) {
                    state.remoteFile = line.substring("remote_file=".length());
                } else if (line.startsWith("transferred_bytes=")) {
                    state.transferredBytes = Long.parseLong(line.substring("transferred_bytes=".length()));
                } else if (line.startsWith("total_bytes=")) {
                    state.totalBytes = Long.parseLong(line.substring("total_bytes=".length()));
                } else if (line.startsWith("checksum=")) {
                    state.checksum = line.substring("checksum=".length());
                } else if (line.startsWith("timestamp=")) {
                    state.timestamp = Long.parseLong(line.substring("timestamp=".length()));
                } else if (line.startsWith("status=")) {
                    state.status = line.substring("status=".length());
                }
            }

            return state;
        } catch (Exception e) {
            System.err.println("加载传输状态失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 更新传输状态
     */
    public static void updateState(String localFile, long transferredBytes) {
        TransferState state = loadState(localFile);
        if (state != null) {
            saveState(state.localFile, state.remoteFile, transferredBytes,
                    state.totalBytes, state.checksum);
        }
    }

    /**
     * 标记传输完成
     */
    public static void markCompleted(String localFile) {
        String stateFile = getStateFileName(localFile);
        File file = new File(stateFile);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("status=")) {
                        content.append("status=completed\n");
                    } else {
                        content.append(line).append("\n");
                    }
                }

                try (PrintWriter writer = new PrintWriter(new FileWriter(stateFile))) {
                    writer.print(content.toString());
                }
            } catch (IOException e) {
                System.err.println("更新传输状态失败: " + e.getMessage());
            }
        }
    }

    /**
     * 删除传输状态
     */
    public static void deleteState(String localFile) {
        String stateFile = getStateFileName(localFile);
        new File(stateFile).delete();
    }

    /**
     * 检查是否有可恢复的传输
     */
    public static boolean hasResumableState(String localFile) {
        TransferState state = loadState(localFile);
        return state != null && "in_progress".equals(state.status);
    }

    private static String getStateFileName(String localFile) {
        String hash = Integer.toHexString(localFile.hashCode());
        return STATE_DIR + "/transfer_" + hash + ".state";
    }

    public static class TransferState {
        public String localFile;
        public String remoteFile;
        public long transferredBytes;
        public long totalBytes;
        public String checksum;
        public long timestamp;
        public String status;
    }
}
