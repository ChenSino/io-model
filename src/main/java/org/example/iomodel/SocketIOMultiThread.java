package org.example.iomodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * io模型——阻塞io,每请求一线程模型
 */
public class SocketIOMultiThread {  //blocking
    public static void main(String[] args) {
        // 服务端开启一个端口进行监听
        int port = 9999;
        ServerSocket serverSocket = null;   //服务端
        try {
            serverSocket = new ServerSocket(port);  //通过构造函数创建ServerSocket，指定监听端口，如果端口合法且空闲，服务器就会监听成功

            // 通过无限循环监听客户端连接，如果没有客户端接入，则会阻塞在accept操作
            while (true) {
                Socket socket;  //客户端
                System.out.println("Waiting for a new Socket to establish");
                socket = serverSocket.accept();//阻塞  三次握手

                System.out.println(" a new Socket to established ,port is " + socket.getPort());

//每连接一线程
                new Thread(() -> {
                    InputStream in = null;
                    OutputStream out = null;
                    try {
                        in = socket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = in.read(buffer)) > 0) {//阻塞
                            out = socket.getOutputStream();
                            String inputString = new String(buffer, 0, length);
                            System.out.println("input is:" + inputString);
                            out.write("success\n".getBytes());

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } finally {
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

                }).start();


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
        }
    }
}