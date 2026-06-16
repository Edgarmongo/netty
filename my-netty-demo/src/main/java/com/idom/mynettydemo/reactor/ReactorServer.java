package com.idom.mynettydemo.reactor;

import java.io.IOException;

/**
 * @program: netty
 * @description: 启动类
 * @author: Timo
 * @create: 2026-06-16 15:59
 **/
public class ReactorServer {
    public static void main(String[] args) throws IOException {
        Reactor reactor = new Reactor(8084);
        new Thread(reactor).start();
    }
}
