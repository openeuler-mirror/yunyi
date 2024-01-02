package com.tongtech.proxy.core.utils;

import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static com.tongtech.proxy.core.StaticContent.STOPING_STRING;
import static com.tongtech.proxy.core.StaticContent.lengthAsUtf8;

public class StopServer {
    public static void stopServer() {
        Socket socket = null;
        try {
            int port = ProxyConfig.getListeningPort();
            int secureLevel = ProxyConfig.getSecureLevel();
            String password = ProxyConfig.getAuthenString();

            if ((secureLevel & 0x1) > 0) {
                System.out.println("Try to connect Server via SSL");
                SSLContext ssc = BogusSSLContextFactory.getInstance(false);
                socket = ssc.getSocketFactory().createSocket();
            } else {
                System.out.println("Try to connect Server");
                socket = new Socket();
            }
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(2000);// 1 second
            socket.connect(new InetSocketAddress("localhost", port), 2000);
            System.out.println("Server Connected.");
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).startHandshake();
                System.out.println("SSL handshake ok.");
            }
            if ((secureLevel & 0x2) > 0) {
                int utf8Len = lengthAsUtf8(ProxyConfig.getAuthenString());
                socket.getOutputStream().write(("*2\n$4\nauth\n$" + utf8Len + "\n" + ProxyConfig.getAuthenString() + "\n")
                        .getBytes(StandardCharsets.UTF_8));

//                socket.getOutputStream().write(("auth " + password + "\n").getBytes(StandardCharsets.UTF_8));
                socket.getOutputStream().flush();
                System.out.println("Connect auth.");
            }

            System.out.println("Shutdown Server...");

            socket.getOutputStream().write((STOPING_STRING.toLowerCase() + "\n").getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String pid = reader.readLine();
            try {
                socket.close();
            } catch (Throwable t) {
            }

            try {
                long l = Long.parseLong(pid);
                if (l > 0) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                    if (isWindows()) {
                        Runtime.getRuntime().exec("taskkill /pid " + pid);
                    } else {
                        Runtime.getRuntime().exec("kill " + pid);
                    }
                }
            } catch (Throwable t) {
            }

            while (socket.getInputStream().read() >= 0) ;

            System.out.println("Shutdown ok.");

        } catch (Throwable t) {
            System.err.println("Stop server error: " + t);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        if (os != null) {
            os = os.toLowerCase();
            return os.startsWith("win");
        }
        return false;
    }

}
