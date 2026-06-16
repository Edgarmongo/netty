package com.idom.mynettydemo.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @program: netty
 * @description: Java的nio同步非阻塞模式测试
 * @author: Timo
 * @create: 2026-06-16 12:11
 **/
public class JavaEpoll1NIO {
    public static void main(String[] args) throws Exception {

        ServerSocketChannel server = ServerSocketChannel.open();
        //绑定地址和端口
        server.bind(new InetSocketAddress(8099));
        //设置为非阻塞,告诉JVM 这个FD必须为nonblocking
        server.configureBlocking(false);
        //创建selector选择器,在Linux中是调用epoll_create()方法
        Selector selector = Selector.open();
        //注册Accept事件，在Linux内核中是调用epoll_ctl()并把FD加入到红黑树中
        server.register(selector, SelectionKey.OP_ACCEPT);
        //创建缓冲区,在JVM的对内
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //在JVM的对外内存分配缓冲区
//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);


        while (true) {
            //阻塞在epoll_wait上，在Linux内核中是调用epoll_wait（）并把线程放到epoll的就绪链表中
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keys = selectedKeys.iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (key.isAcceptable()) {
                    //接收客户端连接
                    SocketChannel client = server.accept();
                    //进来的客户端也设置为非阻塞模式
                    client.configureBlocking(false);
//                    server.register(selector, SelectionKey.OP_READ);
                    System.out.println("client has connected!!!!!");
                }
                //客户端有数据了
                if (key.isReadable()) {
                    //非阻塞读
                    SocketChannel client = (SocketChannel) key.channel();
                    buffer.clear();
                    int cn = client.read(buffer);
                    if (cn > 0) {
                        buffer.flip();
                        String content = buffer.limit(1024).toString();
                        System.out.println(client.socket().getPort() + " : " + content);
                        //非阻塞写
                        client.write(buffer);
                        buffer.clear();
                    }else if(cn == 0){
                        System.out.println("暂无来自客户端的数据！！！！");
                    } else if (cn == -1) {
                        System.out.println("客户端连接关闭！！！！");
                        client.close();
                    }

                }
                keys.remove();
            }


        }

    }
}
