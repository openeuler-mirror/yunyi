package com.tongtech.proxy.core.sync;

import java.net.InetSocketAddress;

public interface SyncConnector {
    void close();

    InetSocketAddress getServerAddress();

    boolean isClose();

    boolean isConnected();

//    void sendMessage();

    int getRedisPort();
}
