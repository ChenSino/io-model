package org.example.filechannel;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Client {
    public static void main(String[] args) throws Exception {
        RandomAccessFile file = new RandomAccessFile("/home/chenkun/Desktop/TestSocket.java", "r");
        FileChannel channel = file.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int len = 0;
        byte[] temp = new byte[1024];
        while ((len = channel.read(buffer)) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.print((char)buffer.get());
            }
            buffer.clear();
        }

    }
}
