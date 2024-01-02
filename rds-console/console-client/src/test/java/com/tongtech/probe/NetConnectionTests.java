package com.tongtech.probe;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class NetConnectionTests {

    @Test
    void testSocket() throws IOException {
        Socket socket = new Socket();
        try {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(2000);// 1 second
            socket.connect(new InetSocketAddress(InetAddress.getByName("localhost"), 6300), 2000);
        } catch (IOException e) {
            throw e;
        }finally{
            socket.close();
        }
    }
}
