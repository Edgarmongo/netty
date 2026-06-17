package com.idom.mynettydemo.bio;

import java.nio.IntBuffer;

/**
 * @program: netty
 * @description: 缓冲区测试
 * @author: Timo
 * @create: 2026-06-16 18:26
 **/
public class BufferTest {
    public static void main(String[] args) {

        IntBuffer buffer = IntBuffer.allocate(10);
        //往缓冲区中存放数据
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.put(i * 2);
        }
        //切换为读模式才可以读取数据
        buffer.flip();
        for (int i = 0; i < buffer.capacity(); i++) {
            System.out.println("buffer.get() = " + buffer.get());
        }



    }
}
