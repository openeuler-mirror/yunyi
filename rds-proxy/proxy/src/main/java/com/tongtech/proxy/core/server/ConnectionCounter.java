package com.tongtech.proxy.core.server;

import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConnectionCounter implements Runnable {
    private final static Log logger = ProxyConfig.getServerLog();

    //
    // 以下代码统计Redis仿真端口的连接情况
    //
    private static final ConnectionCounter CONNECTIONS_REDIS = new ConnectionCounter();

    public static long getCurrentConnectionsRedis() {
        return CONNECTIONS_REDIS.currentConnections();
    }

    public static long getTotalConnectionsRedis() {
        return CONNECTIONS_REDIS.totalConnections();
    }

    public static ConcurrentHashMap<String, Remote> getConnectionsRedis() {
        return CONNECTIONS_REDIS.getConnectedClients();
    }

    public static void connectionOpenRedis(InetSocketAddress address) {
        CONNECTIONS_REDIS.connectionOpened(address);
    }

    public static void connectionCloseRedis(InetSocketAddress address) {
        CONNECTIONS_REDIS.connectionClosed(address);
    }

    public static void connectionRejectedRedis() {
        CONNECTIONS_REDIS.connectionRejected();
    }

    public static long getConnectionRejectedRedis() {
        return CONNECTIONS_REDIS.rejectConnections;
    }

    //
    // 以下代码统计RDS仿真端口的连接情况
    //
    private static final ConnectionCounter CONNECTIONS_RDS = new ConnectionCounter();

    public static long getCurrentConnectionsRds() {
        return CONNECTIONS_RDS.currentConnections();
    }

    public static long getTotalConnectionsRds() {
        return CONNECTIONS_RDS.totalConnections();
    }

    public static ConcurrentHashMap<String, Remote> getConnectionsRds() {
        return CONNECTIONS_RDS.getConnectedClients();
    }

    public static void connectionOpenRds(InetSocketAddress address) {
        CONNECTIONS_RDS.connectionOpened(address);
    }

    public static void connectionCloseRds(InetSocketAddress address) {
        CONNECTIONS_RDS.connectionClosed(address);
    }

    public static void connectionRejectedRds() {
        CONNECTIONS_RDS.connectionRejected();
    }

    public static long getConnectionRejectedRds() {
        return CONNECTIONS_RDS.rejectConnections;
    }

    private final ConcurrentHashMap<String, Remote> ConnectedClients = new ConcurrentHashMap<>();

    // 总的连接次数
    private long totalConnections = 0;

    // 总的被拒绝的连接
    private long rejectConnections = 0;

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private ConnectionCounter() {
        service.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
    }

    private void connectionOpened(InetSocketAddress address) {

        if (address != null) {
            String ip = address.getHostString();
//            session.setAttribute(TITLE_REMOTE, ip);
            Remote client;
            synchronized (this) {
                // 计算总的连接数
                ++totalConnections;
                client = ConnectedClients.get(ip);
                if (client == null) {
                    client = new Remote(ip);
                    ConnectedClients.put(ip, client);
                }
                client.connectionCreated();
                //ConnectedSessions.put(session, client);
                //  logger.warnLog("ConnectionCounter::connectionOpened() remote {} is {}", client.hostAddress, client.getCurrentConnected());
            }
        }
    }

    private void connectionClosed(InetSocketAddress address) {
//        Object ip = session.getAttribute(TITLE_REMOTE);
        // logger.warnLog("ConnectionCounter::connectionClosed() close session from remote " + ip);
        //Remote client = ConnectedSessions.remove(session);
        if (address != null) {
            String ip = address.getHostString();
            if (ip != null) {
                synchronized (this) {
                    Remote client = ConnectedClients.get(ip);
                    if (client != null) {
                        client.connecitonClosed();
                        //   logger.warnLog("ConnectionCounter::connectionClosed() remote {} is {}", client.hostAddress, client.getCurrentConnected());
                    } else {
                        logger.debugLog("ConnectionCounter::connectionClosed() remote {}", ip);
                    }
                }
            }
        }
    }

    /**
     * 记录被拒绝的连接
     *
     */
    private synchronized void connectionRejected() {
        ++rejectConnections;
    }

    /**
     * 返回被拒绝的连接总数
     *
     * @return
     */
    private synchronized long getRejectConnections() {
        return rejectConnections;
    }

    private ConcurrentHashMap<String, Remote> getConnectedClients() {
        return this.ConnectedClients;
    }

    private int currentConnections() {
        int curr = 0;
        for (String ip : ConnectedClients.keySet()) {
            Remote client = ConnectedClients.get(ip);
            if (client != null) {
                curr += client.getCurrentConnected();
            }
        }
        return curr;
    }

    private synchronized long totalConnections() {
        return this.totalConnections;
    }

    public void run() {
//        // 清理已经关闭的session
//        for (IoSession session : ConnectedSessions.keySet()) {
//            if (session.isClosing()) {
//                connectionClosed(session);
//            }
//        }
//        logger.debugLog("ConnectionCounter::run() Current connections are {}", ConnectedSessions.size());

        // 时间戳的单位是秒
        long timestamp = System.currentTimeMillis() / 1000;
        for (String ip : ConnectedClients.keySet()) {
            Remote client = ConnectedClients.get(ip);
            if (client != null) {
                synchronized (this) {
                    client.flush(timestamp);

                    // 10分钟过期
                    if (client.isExpired(timestamp - 600)) {
                        ConnectedClients.remove(ip);
                    }
                }
            }
        }
    }

    public static class Remote {
        private final String hostAddress;

        // 当前的连接数
        private int currentConnected;

        // 最近1分钟内创建的连接数
        private int createdConnPerMinute;

        // 当前正在创建的连接数
        private int curCreated;

        private long timestamp;

        public Remote(String host) {
            this.hostAddress = host;
            this.timestamp = System.currentTimeMillis() / 1000;
        }

        /**
         * 当前的连接数
         *
         * @return
         */
        public synchronized int getCurrentConnected() {
            return currentConnected;
        }

        /**
         * 每分钟创建的连接数
         *
         * @return
         */
        public synchronized int getCreatedConnPerMinute() {
            return Math.max(this.createdConnPerMinute, this.curCreated);
        }

        public synchronized void connectionCreated() {
            currentConnected = currentConnected + 1;
            curCreated = curCreated + 1;
        }

        public synchronized void connecitonClosed() {
            currentConnected = currentConnected - 1;
        }

        private synchronized void flush(long timestamp) {
            createdConnPerMinute = curCreated;
            curCreated = 0;
            if (createdConnPerMinute > 0 || currentConnected > 0) {
                this.timestamp = timestamp;
            }
        }

        private synchronized boolean isExpired(long expiredTime) {
            return this.timestamp < expiredTime;
        }
    }
}
