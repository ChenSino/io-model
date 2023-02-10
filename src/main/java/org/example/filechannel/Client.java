package org.example.filechannel;

import org.junit.Test;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) throws Exception {
        RandomAccessFile readFile = new RandomAccessFile("/home/chenkun/Desktop/TestSocket.java", "r");
        RandomAccessFile writeFile = new RandomAccessFile("/home/chenkun/Desktop/TestSocket1.java", "rw");
        FileChannel readFileChannel = readFile.getChannel();
        FileChannel writeFileChannel = writeFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        while (( readFileChannel.read(buffer)) != -1) {
            buffer.flip();
            //写入
            writeFileChannel.write(buffer);
            buffer.clear();
        }
        writeFile.close();
        readFile.close();
    }

    @Test
    public void testBuffer(){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putChar('陈');
        buffer.putInt(33);
        byte b = 3;
        buffer.put(b);

        buffer.flip();
        System.out.println(buffer.getChar());
        System.out.println(buffer.getInt());

    }
}
