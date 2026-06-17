package com.idom.mynettydemo.bio;

import java.io.*;
import java.util.Objects;

/**
 * @program: netty
 * @description: 同步阻塞BIO测试
 * Java:  fis.read(buf)
 * │
 * ▼
 * FileInputStream.readBytes()          ← native 方法
 * │  (jdk/src/share/native/java/io/FileInputStream.c)
 * │  readBytes → io_util.c → handleRead()
 * ▼
 * C 层:  read(fd, buf, len)             ← 这就是 libc 的 read()，本质是 syscall
 * │
 * ╞═══════════════════════════════  进入内核态（syscall 指令 / entry 门）
 * │
 * ▼
 * SYSCALL_DEFINE3(read, ...)            ← 内核入口：fs/read_write.c
 * → ksys_read()
 * → vfs_read()
 * → file->f_op->read_iter()       ← 走 VFS 分发，最终到具体文件系统
 * → copy_to_user(buf, ...)      ← 若数据已在 page cache，直接拷贝返回
 * ← 若不在，触发缺页/读盘（磁盘 BIO 路径），同样等 I/O 完成
 *
 * │
 * ╞═══════════════════════════════  返回用户态
 * ▼
 * C 层拿到返回值 → JNI 把数据填进 Java byte[] → read() 返回
 * @author: Timo
 * @create: 2026-06-16 02:40
 **/
public class JavaLocalFileBIO {
    public static void main(String[] args) throws Exception {



    }

    public static void writeStream() throws Exception {
        File file = new File("D:\\tmp\\outtest.txt");

        byte[] wbuf = new byte[1024];

        try(OutputStream ops = new FileOutputStream(file)){

            ops.write(wbuf);
        }

    }

    public static void inputStream(){
        File file = new File("D:\\tmp\\biotest.txt");
        File file2 = new File("D:\\tmp\\outtest.txt");
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream ops = new FileOutputStream(file2)) {


            byte[] buffer = new byte[1024];
            int length;
            // 循环读取，当返回 -1 时表示文件读取完毕
            while ((length = fis.read(buffer)) != -1) {
                // 将读取到的字节转换为字符串（针对文本文件）
                // 注意：若直接打印文本可能出现中文乱码，需视文件编码而定
                String content = new String(buffer, 0, length);
                System.out.print(content);
                //通过输出流把缓冲区中的数据写出去
                ops.write(buffer,0,length);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
