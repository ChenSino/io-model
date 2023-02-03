package org.example.iomodel;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class NIOTest {
    public static void main(String[] args) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        System.out.println("limit:"+ byteBuffer.limit());
        System.out.println("capacity:"+byteBuffer.capacity());
        System.out.println("position:"+byteBuffer.position());
        System.out.println("mark:"+byteBuffer.mark());


        System.out.println("===================");

        byteBuffer.put("hello".getBytes());
        System.out.println("limit:"+ byteBuffer.limit());
        System.out.println("capacity:"+byteBuffer.capacity());
        System.out.println("position:"+byteBuffer.position());
        System.out.println("mark:"+byteBuffer.mark());

        System.out.println("===================");

        byteBuffer.flip();
        System.out.println("limit:"+ byteBuffer.limit());
        System.out.println("capacity:"+byteBuffer.capacity());
        System.out.println("position:"+byteBuffer.position());
        System.out.println("mark:"+byteBuffer.mark());

        System.out.println("===================");

        byte b = byteBuffer.get();
        System.out.println(b);
        System.out.println("limit:"+ byteBuffer.limit());
        System.out.println("capacity:"+byteBuffer.capacity());
        System.out.println("position:"+byteBuffer.position());
        System.out.println("mark:"+byteBuffer.mark());

        System.out.println(~5);
    }
}
