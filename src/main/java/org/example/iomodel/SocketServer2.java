package org.example.iomodel;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * IO阻塞（我们通常说的io阻塞非阻塞是操作系统层面的，本例演示通过超时控制程序达到“非阻塞”效果，并不是真正的非阻塞，仅仅是代码层面的非阻塞），
 * 本演示依然存在问题，仅仅做了accept的非阻塞，read还是阻塞的，需要升级，参考
 * @see SocketServer3
 */
public class SocketServer2 {

    static {
        BasicConfigurator.configure();
    }

    private static Object xWait = new Object();

    /**
     * 日志
     */
    private static final Log LOGGER = LogFactory.getLog(SocketServer2.class);

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(8888);
            //此设置可以保证下面的accept方法不会被一直阻塞，会等待2s,如果没有连接就直接抛出了java.net.SocketTimeoutException异常，
            serverSocket.setSoTimeout(2000);
            while(true) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch(SocketTimeoutException e1) {
                    //===========================================================
                    //      执行到这里，说明本次accept没有接收到任何数据报文
                    //      主线程在这里就可以做一些事情，记为X
                    //===========================================================
                    synchronized (SocketServer2.xWait) {
                        SocketServer2.LOGGER.info("这次没有从底层接收到任务数据报文，等待1秒，模拟事件X的处理时间");
                        SocketServer2.xWait.wait(1000);
                    }
                    continue;
                }

                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Integer sourcePort = socket.getPort();
                int maxLen = 4;
                byte[] contextBytes = new byte[maxLen];
                int realLen;
                StringBuffer message = new StringBuffer();
                //下面我们收取信息（这里还是阻塞式的,一直等待，直到有数据可以接受）
                while((realLen = in.read(contextBytes, 0, maxLen)) != -1) {
                    message.append(new String(contextBytes , 0 , realLen));
                    /*
                     * 我们假设读取到“over”关键字，
                     * 表示客户端的所有信息在经过若干次传送后，完成
                     * */
                    if(message.indexOf("over") != -1) {
                        break;
                    }
                }
                //下面打印信息
                SocketServer2.LOGGER.info("服务器收到来自于端口：" + sourcePort + "的信息：" + message);

                //下面开始发送信息
                out.write("回发响应信息！".getBytes());

                //关闭
                out.close();
                in.close();
                socket.close();
            }
        } catch(Exception e) {
            SocketServer2.LOGGER.error(e.getMessage(), e);
        } finally {
            if(serverSocket != null) {
                serverSocket.close();
            }
        }
    }
}