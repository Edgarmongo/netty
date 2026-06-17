package com.idom.mynettydemo.bigfile;

/**
 * @program: netty
 * @description: 测试类
 * @author: Timo
 * @create: 2026-06-16 23:30
 **/
public class ProductionTBTransferTest {
    public static void main(String[] args) {

        boolean process = ProductionTBTransfer.process();
        System.out.println("处理结果：" + process);
    }
}
