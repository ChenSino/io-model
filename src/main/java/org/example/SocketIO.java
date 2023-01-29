package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 【特别强调】：在java中阻塞IO和非阻塞IO和BIO、NIO并无直接关系，很多人喜欢吧java中NIO翻译成non-blockingIO我觉得很扯淡，
 * 因为非阻塞IO并不是只能用NIO实现，可以用BIO实现也可以也可以用NIO实现，无非就是让线程不空等，当没有就绪资源时，采用轮询（死循环）
 * 让线程去干别的事情，在后面@SocketServer3中，就可以在死循环中添加超时异常捕获，捕获到超时异常后，可以让线程去做别的事情，这就是
 * 所谓的非阻塞，所以NIO不等于非阻塞IO,很多人喜欢这样翻译，真的是很有误导性，我认为NIO更应该翻译为new io,就是一个新io,用以区别开始
 * 的bio,仅此而已
 */

/**
 * ##############【nio实现单线程阻塞io模型】################
 */
public class SocketIO {  //blocking
    public static void main(String[] args) {
        // 服务端开启一个端口进行监听
        int port = 9999;
        ServerSocket serverSocket = null;   //服务端
        Socket socket;  //客户端
        InputStream in = null;
        OutputStream out = null;
        try {

            serverSocket = new ServerSocket(port);  //通过构造函数创建ServerSocket，指定监听端口，如果端口合法且空闲，服务器就会监听成功
            System.out.println("listening " + port);

            // 通过无限循环监听客户端连接，如果没有客户端接入，则会阻塞在accept操作
            while (true) {
                System.out.println("Waiting for a new Socket to establish");
                socket = serverSocket.accept();//阻塞  三次握手

                System.out.println(" a new Socket to established ,port is " + socket.getPort());

                in = socket.getInputStream();
                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = in.read(buffer)) > 0) {//阻塞
                    out = socket.getOutputStream();
                    String inputString = new String(buffer, 0, length);
                    System.out.println("input is:" + inputString);
                    if ("exit\n".equals(inputString)) {
                        out.write("exit\n".getBytes());
                        socket.close();
                    }
                    out.write("success\n".getBytes());

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 必要的清理活动
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}