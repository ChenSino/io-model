package org.example.reactor.singlethread;

import java.io.IOException;

/**
 * 单reactor单线程模型
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Reactor reactor = new Reactor(6666);
        new Thread(reactor).start();
    }
}
