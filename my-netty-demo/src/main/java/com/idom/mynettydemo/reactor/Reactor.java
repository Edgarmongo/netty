package com.idom.mynettydemo.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * @program: netty
 * @description:
 * Reactor = Selector + 事件循环
 *
 * SelectionKey是reactor的核心对象，它就是“channel + 事件 + 上下文”的封装，它里面保存了：
 * channel:对应的ServerSocketChannel
 * selector:所属的选择器
 * interestOps:感兴趣的事件，如accept
 * readyOps:当前就绪的事件
 * attachment：附件：存放开发人员存储的对象
 *
 * @author: Timo
 * @create: 2026-06-16 15:52
 **/
public class Reactor implements Runnable {

    private final Selector selector;

    public Reactor(int port) throws IOException {
        //selector可以关注的事件有：accept/connect/read/write
        this.selector = Selector.open();

        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(port));
        server.configureBlocking(false);
        //注册server accept事件,告诉selector，你只需要关心accept事件就行了
        SelectionKey key = server.register(this.selector, SelectionKey.OP_ACCEPT);
        key.attach(new Acceptor(server));
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                //epoll_ctl
                selector.select();
                Iterator<SelectionKey> it =
                        selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    dispatch(it.next());
                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable handler = (Runnable) key.attachment();
        if (handler != null) {
            handler.run(); // 分发事件
        }
    }
}
