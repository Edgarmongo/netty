package com.idom.mynettydemo.bio;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: netty
 * @description: 网络BIO的测试
 * @author: Timo
 * @create: 2026-06-16 03:23
 **/
public class JavaSocketBIO {
    public static void main(String[] args) throws Exception {

        //socket+bind+listen+
        ServerSocket server = new ServerSocket(8098);
        //server端在接收客户端连接，这里是阻塞点1
        Socket client = server.accept();
        //从客户端的socket中获取输入流数据
        InputStream fis = client.getInputStream();
        byte[] buffer = new byte[1024];
        int length;
        // 循环读取，当返回 -1 时表示文件读取完毕
        while ((length = fis.read(buffer)) != -1) {//这里读取客户端数据的时候，也会阻塞
            // 将读取到的字节转换为字符串（针对文本文件）
            // 注意：若直接打印文本可能出现中文乱码，需视文件编码而定
            String content = new String(buffer, 0, length);
            System.out.print(content);
        }

    }
}
