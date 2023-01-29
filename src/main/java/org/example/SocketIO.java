package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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