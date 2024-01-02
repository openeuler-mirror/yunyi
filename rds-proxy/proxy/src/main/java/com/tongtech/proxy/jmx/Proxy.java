package com.tongtech.proxy.jmx;

import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;
import com.tongtech.proxy.core.server.ConnectionCounter;
import com.tongtech.proxy.core.server.ProxyController;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Proxy implements ProxyMBean {
    private final ProxyController MemDB;

    public Proxy(ProxyController memdb) {
        this.MemDB = memdb;
    }

    @Override
    public String getId() {
        return ProxyConfig.getIdentify();
    }

    @Override
    public String getVersion() {
        return StatusColector.getVersion();
    }


    @Override
    public long getMemoryTotal() {
        return StatusColector.getMemoryTotal();
    }

    @Override
    public long getJvmAllocated() {
        return StatusColector.getJvmAllocated();
    }

    @Override
    public long getJvmFree() {
        return StatusColector.getJvmFree();
    }

    @Override
    public long getJvmMax() {
        return StatusColector.JVM_AVAILABLE;
    }

    @Override
    public long getTotalPhysicalMemory() {
//        long availbale = -1;
//        try {
//            availbale = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
//        } catch (Throwable t) {
//        }
        return StatusColector.getTotalPhysicalMemory();
    }

    @Override
    public long getClientCurrentConnections() {
        return StatusColector.getClientCurrentConnections();
    }

    @Override
    public long getMaxConnections() {
        return ProxyConfig.getMaxConnections();
    }

    @Override
    public double getConnectedRatio() {
        long max = ProxyConfig.getMaxConnections();
        return max > 0 ? (ConnectionCounter.getCurrentConnectionsRedis() * 1000 / max) / 1000.0 : 0.0;
    }

    @Override
    public long getClientTotalConnections() {
        return StatusColector.getClientTotalConnections();
    }

    @Override
    public double getConnectionsRatio() {
        return StatusColector.getConnectionsRatio();
    }

    @Override
    public long getProcessSecond() {
        return StatusColector.getProcessSecond();
    }

    @Override
    public long getProcessMinute() {
        return (long)StatusColector.getProcessMinute();
    }

    /**
     * 通过创建socket并发送ping命令的方式探测Redis仿真端口工作是否正常，
     * 如果程序未打开Redis仿真功能则返回false
     *
     * @return
     */
    @Override
    public boolean getPing() {

        int redisPort = ProxyConfig.getRedisPort();

        if (redisPort >= 1024) {
            boolean isSsl = (ProxyConfig.getSecureLevel() & 0x1) > 0;
            boolean needPwd = (ProxyConfig.getSecureLevel() & 0x2) > 0;
            Socket socket = null;
            BufferedReader input = null;
            try {
                // redis仿真接口不支持ssl
                isSsl = false;
                if (isSsl) {
                    SSLContext ssc = BogusSSLContextFactory.getInstance(false);
                    SSLSocket sslSocket = (SSLSocket) ssc.getSocketFactory().createSocket();
                    sslSocket.setTcpNoDelay(true);
                    sslSocket.setSoTimeout(200);
                    sslSocket.connect(new InetSocketAddress(redisPort), 200);
                    sslSocket.startHandshake();
                    socket = sslSocket;
                } else {
                    socket = new Socket();
                    socket.setTcpNoDelay(true);
                    socket.setSoTimeout(200);
                    socket.connect(new InetSocketAddress(redisPort), 200);
                }
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                if (needPwd) {
                    byte[] lineEnd = "\r\n".getBytes(StandardCharsets.UTF_8);
                    byte[] pwd = null;
                    try {
                        pwd = ProxyConfig.getRedisPwd().getBytes(StandardCharsets.UTF_8);
                    } catch (Exception e) {
                    }
                    socket.getOutputStream().write(("*2\r\n$4\r\nauth\r\n$").getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().write((pwd != null ? Integer.toString(pwd.length) : "0").getBytes(StandardCharsets.UTF_8));
                    socket.getOutputStream().write(lineEnd);
                    if (pwd != null && pwd.length > 0) {
                        socket.getOutputStream().write(pwd);
                    }
                    socket.getOutputStream().write(lineEnd);
                    input.readLine();
                }
                socket.getOutputStream().write("$4\r\nPING\r\n".getBytes(StandardCharsets.UTF_8));
                String pong = input.readLine();
                if ("+PONG".equalsIgnoreCase(pong)) {
                    return true;
                }
            } catch (Throwable e) {
//                System.err.println("Error occur when connect to "
//                        + "localhost:" + redisPort + "  : " + e.getMessage());
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
}
