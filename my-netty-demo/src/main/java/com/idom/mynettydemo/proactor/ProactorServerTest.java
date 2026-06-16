package com.idom.mynettydemo.proactor;

/**
 * @program: netty
 * @description: proactor测试类
 * @author: Timo
 * @create: 2026-06-16 16:30
 **/
public class ProactorServerTest {
    public static void main(String[] args) throws Exception {
         new ProactorServer(8066);
    }
}
