package org.example.iomodel;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * IO多路复用模型，一个线程（main）中处理并发请求。本例中，io处理是单线程，业务处理也是用的main线程，这会导致当有一个线程readSocketChannel函数耗时比较长时
 * 其他业务代码就无法执行，所以可以引入线程池，让readSocketChannel在线程池中执行。与阻塞io模型相比，io多路复用模型多个客户端连接，只需要使用一个线程来负责
 * 处理连接，并且此线程可开启non-block,不会被阻塞 ，具体的业务逻辑再交给线程池处理（相当于一个线程处理多个业务），避免了阻塞io中每连接一线程的问题，
 */
public class NIO {

    static {
        BasicConfigurator.configure();
    }

    /**
     * 日志
     */
    private static final Log LOGGER = LogFactory.getLog(NIO.class);

    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket serverSocket = serverChannel.socket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(8888));

        Selector selector = Selector.open();
        //注意、服务器通道只能注册SelectionKey.OP_ACCEPT事件
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        try {
            while (true) {
                //如果条件成立，说明本次询问selector，并没有获取到任何准备好的、感兴趣的事件
                //java程序对多路复用IO的支持也包括了阻塞模式 和非阻塞模式两种。
                //select()在linux下测试，会调用内核的epoll_wait的函数，可以添加 Thread.sleep(xxx);配合strace命令测试epoll_wait打印速度
                //1s轮询一次，看看是否有可用的事件
                if (selector.select(1000) == 0) {
                    //================================================
                    //      这里视业务情况，可以做一些然并卵的事情
                    //================================================
                    continue;
                }
                //这里就是本次询问操作系统，所获取到的“所关心的事件”的事件类型（每一个通道都是独立的）
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();

                while (selectionKeys.hasNext()) {
                    SelectionKey readyKey = selectionKeys.next();

                    //这个已经处理的readyKey一定要移除。如果不移除，就会一直存在在selector.selectedKeys集合中
                    //待到下一次selector.select() > 0时，这个readyKey又会被处理一次
                    selectionKeys.remove();

                    //分事件处理，和之前写websocket类似的
                    if (readyKey.isValid() && readyKey.isAcceptable()) {
                        NIO.LOGGER.info("======channel通道已经准备好=======");
                        /*
                         * 当server socket channel通道已经准备好，就可以从server socket channel中获取socketchannel了
                         * 拿到socket channel后，要做的事情就是马上到selector注册这个socket channel感兴趣的事情。
                         * 否则无法监听到这个socket channel到达的数据
                         * */
                        //以下serverSocketChannel和最初的serverChannel是同一个对象
//                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) readyKey.channel();
                        SocketChannel socketChannel = serverChannel.accept();
                        registerSocketChannel(socketChannel, selector);

                    } else if (readyKey.isValid() && readyKey.isConnectable()) {
                        NIO.LOGGER.info("======socket channel 建立连接=======");
                    } else if (readyKey.isValid() && readyKey.isReadable()) {
                        NIO.LOGGER.info("======socket channel 数据准备完成，可以去读==读取=======");
                        readSocketChannel(readyKey);
                    }
                }
            }
        } catch (Exception e) {
            NIO.LOGGER.error(e.getMessage(), e);
        } finally {
            serverSocket.close();
        }
    }

    /**
     * 在server socket channel接收到/准备好 一个新的 TCP连接后。
     * 就会向程序返回一个新的socketChannel。<br>
     * 但是这个新的socket channel并没有在selector“选择器/代理器”中注册，
     * 所以程序还没法通过selector通知这个socket channel的事件。
     * 于是我们拿到新的socket channel后，要做的第一个事情就是到selector“选择器/代理器”中注册这个
     * socket channel感兴趣的事件
     *
     * @param socketChannel 新的socket channel
     * @param selector      selector“选择器/代理器”
     * @throws Exception
     */
    private static void registerSocketChannel(SocketChannel socketChannel, Selector selector) throws Exception {
        socketChannel.configureBlocking(false);
        //socket通道可以且只可以注册三种事件SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT
        socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(2048));
    }

    /**
     * 这个方法用于读取从客户端传来的信息。
     * 并且观察从客户端过来的socket channel在经过多次传输后，是否完成传输。
     * 如果传输完成，则返回一个true的标记。
     *
     * @param readyKey
     * @throws Exception
     */
    private static void readSocketChannel(SelectionKey readyKey) throws Exception {
        SocketChannel clientSocketChannel = (SocketChannel) readyKey.channel();
        //获取客户端使用的端口
        InetSocketAddress sourceSocketAddress = (InetSocketAddress) clientSocketChannel.getRemoteAddress();
        Integer resourcePort = sourceSocketAddress.getPort();

        //拿到这个socket channel使用的缓存区，准备读取数据
        //在后文，将详细讲解缓存区的用法概念，实际上重要的就是三个元素capacity,position和limit。
        ByteBuffer contextBytes = (ByteBuffer) readyKey.attachment();
        //将通道的数据写入到缓存区，注意是写入到缓存区。
        //由于之前设置了ByteBuffer的大小为2048 byte，所以可以存在写入不完的情况
        //没关系，我们后面来调整代码。这里我们暂时理解为一次接受可以完成
        int realLen = -1;
        try {
            realLen = clientSocketChannel.read(contextBytes);
        } catch (Exception e) {
            //这里抛出了异常，一般就是客户端因为某种原因终止了。所以关闭channel就行了
            NIO.LOGGER.error(e.getMessage());
            clientSocketChannel.close();
            return;
        }

        //如果缓存区中没有任何数据（但实际上这个不太可能，否则就不会触发OP_READ事件了）
        if (realLen == -1) {
            NIO.LOGGER.warn("====缓存区没有数据？====");
            return;
        }

        //将缓存区从写状态切换为读状态（实际上这个方法是读写模式互切换）。
        //这是java nio框架中的这个socket channel的写请求将全部等待。
        contextBytes.flip();
        //注意中文乱码的问题，我个人喜好是使用URLDecoder/URLEncoder，进行解编码。
        //当然java nio框架本身也提供编解码方式，看个人咯
        byte[] messageBytes = contextBytes.array();
        String messageEncode = new String(messageBytes, "UTF-8");
        String message = URLDecoder.decode(messageEncode, "UTF-8");

        //如果收到了“over”关键字，才会清空buffer，并回发数据；
        //否则不清空缓存，还要还原buffer的“写状态”
        if (message.indexOf("over") != -1) {
            //清空已经读取的缓存，并从新切换为写状态(这里要注意clear()和capacity()两个方法的区别)
            contextBytes.clear();
            NIO.LOGGER.info("线程：" + Thread.currentThread().getName() + "端口:" + resourcePort + "客户端发来的信息======message : " + message);

            //======================================================
            //          当然接受完成后，可以在这里正式处理业务了        
            //======================================================

            //回发数据，并关闭channel
            ByteBuffer sendBuffer = ByteBuffer.wrap(URLEncoder.encode("线程：" + Thread.currentThread().getName() + "回发处理结果", "UTF-8").getBytes());
            clientSocketChannel.write(sendBuffer);
            clientSocketChannel.close();
        } else {
            NIO.LOGGER.info("线程：" + Thread.currentThread().getName() + "端口:" + resourcePort + "客户端信息还未接受完，继续接受======message : " + message);
            //这是，limit和capacity的值一致，position的位置是realLen的位置
            contextBytes.position(realLen);
            contextBytes.limit(contextBytes.capacity());
        }
    }
}
