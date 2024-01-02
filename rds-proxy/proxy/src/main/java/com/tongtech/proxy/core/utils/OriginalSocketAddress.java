package com.tongtech.proxy.core.utils;

import java.net.InetSocketAddress;

public class OriginalSocketAddress extends InetSocketAddress {
    private final static Log logger = ProxyConfig.getServerLog();

    private final String RemoteHost;
    private final String RemoteHastAlias;
    private final int RedisPortAlias;

    public OriginalSocketAddress(int port) {
        super(port);
        this.RemoteHost = null;
        this.RemoteHastAlias = null;
        this.RedisPortAlias = 0;
    }

    public OriginalSocketAddress(String host, int port) {
        this(host, null, port, 0);
    }

    public OriginalSocketAddress(String host, String host_alias, int port, int port_alias) {
        super(host, port);
        this.RemoteHost = host;
        this.RemoteHastAlias = host_alias;
        this.RedisPortAlias = port_alias;
    }

    public String getOriginalAddress() {
        return RemoteHost;
    }

    public String getRemoteHastAlias() {
        return RemoteHastAlias;
    }

    public int getRedisPortAlias() {
        return RedisPortAlias;
    }

    public String getRemoteAliasOrHost() {
        return RemoteHastAlias != null ? RemoteHastAlias : RemoteHost;
    }

    public int getRemoteAliasOrPort() {
        return RedisPortAlias > 0 ? RedisPortAlias : this.getPort();
    }

    /**
     * @param host_and_port 字符串形式的地址和端口，格式为"ip:port"，例如：“192.168.0.60:6200”
     * @return
     */
    public static OriginalSocketAddress getSocketAddress(String host_and_port) {
        // 分离host和port，输入格式为：host:port
        try {
//            int idx_port = host_and_port.lastIndexOf(':');
//            String addr = host_and_port.substring(0, idx_port);
//            // 取port的内容
//            String s_port = host_and_port.substring(idx_port + 1);
//            int port = Integer.parseInt(s_port);
//            return new OriginalSocketAddress(addr, port);
            final int idx_port = host_and_port.lastIndexOf(':');

            int idx_alias = host_and_port.indexOf('(');
            String addr_alias = null;
            if (idx_alias <= 0 || idx_alias > idx_port) {
                idx_alias = idx_port;
            }
            String addr = host_and_port.substring(0, idx_alias);
            if (idx_alias < idx_port) {
                // 可能有别名配置
                int alias_end = host_and_port.indexOf(')', idx_alias);
                if (alias_end > idx_alias + 1 && alias_end < idx_port) {
                    // 有别名配置
                    addr_alias = host_and_port.substring(idx_alias + 1, alias_end);
                }
            }

            // 取port的内容
            idx_alias = host_and_port.indexOf('(', idx_port);
            if (idx_alias <= 0) {
                idx_alias = host_and_port.length();
            }
            String s_port = host_and_port.substring(idx_port + 1, idx_alias);
            int port = Integer.parseInt(s_port);
            int port_alias = 0;
            if (idx_alias < host_and_port.length()) {
                // 可能有别名配置
                int alias_end = host_and_port.indexOf(')', idx_alias);
                if (alias_end > idx_alias + 1) {
                    try {
                        port_alias = Integer.parseInt(host_and_port.substring(idx_alias + 1, alias_end));
                    } catch (Throwable t) {
                    }
                }
            }

            return new OriginalSocketAddress(addr, addr_alias, port, port_alias);
        } catch (Exception e) {
            // 任何异常都会返回null
            logger.warnLog("OriginalSocketAddress::getSocketAddress() Can not parse '{}' to an OriginalSocketAddress", host_and_port);
        }
        return null;
    }
}
