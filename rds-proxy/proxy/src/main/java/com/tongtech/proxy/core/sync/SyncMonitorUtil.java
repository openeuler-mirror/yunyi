package com.tongtech.proxy.core.sync;

import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;
import com.tongtech.proxy.core.utils.ProxyConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.net.InetSocketAddress;
import java.net.Socket;

public class SyncMonitorUtil {
    public static final int SOCKET_CONNECT_TIMEOUT = 1000;
    public static final int SOCKET_DATA_TIMEOUT = 1000;

    /**
     * 创建socket连接
     *
     * @param address
     * @return
     */
    public static SyncNodeConnection getConnection(/*int secure, String password,*/ InetSocketAddress address) throws Exception {
        Socket socket = null;
        int secure = ProxyConfig.getSecureLevel();
        try {
            if ((secure & 0x1) > 0) {
                SSLContext ssc = BogusSSLContextFactory.getInstance(false);
                socket = ssc.getSocketFactory().createSocket();
            } else {
                socket = new Socket();
            }
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(SOCKET_DATA_TIMEOUT);// 1 second
            socket.connect(address, SOCKET_CONNECT_TIMEOUT);
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).startHandshake();
            }
            return new SyncNodeConnection(socket, address);
        } catch (Exception e) {
            //e.printStackTrace();
            try {
                socket.close();
            } catch (Exception e1) {
            }
            throw e;
        }
    }
}
