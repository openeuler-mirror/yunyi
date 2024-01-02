package com.tongtech.proxy.core.rescue;


import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.OriginalSocketAddress;
import com.tongtech.proxy.core.utils.ProxyConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import static com.tongtech.proxy.core.StaticContent.splitString;


public class FindSuitableServer {
    // 日志类
    private final static Log Logger = ProxyConfig.getServerLog();

    public static OriginalSocketAddress getServer(List<OriginalSocketAddress> addresses) {
        long start_time = System.currentTimeMillis();
        OriginalSocketAddress suitable_server = null;
        boolean findSelf = false;
        if (addresses != null && addresses.size() > 0) {
            for (OriginalSocketAddress address : addresses) {
                try {
                    // 连结到服务器
                    Logger.debugLog("FindSuitableServer::getServer() Try to connect {}", address);
                    Socket socket = null;
                    try {
                        int secure = ProxyConfig.getSecureLevel();
                        if ((secure & 0x1) > 0) {
                            SSLContext ssc = BogusSSLContextFactory.getInstance(false);
                            socket = ssc.getSocketFactory().createSocket();
                        } else {
                            socket = new Socket();
                        }
                        socket.setTcpNoDelay(true);
                        socket.setSoTimeout(900);// 1 second
                        socket.connect(address, 900);
                        if (socket instanceof SSLSocket) {
                            ((SSLSocket) socket).startHandshake();
                        }
                        OutputStream os = socket.getOutputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                        if ((secure & 2) > 0) {
                            os.write(("auth " + ProxyConfig.getAuthenString() + "\n").getBytes(StandardCharsets.UTF_8));
                        }

                        // check if myself
                        os.write("check\n".getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        String receive = reader.readLine();
                        Vector<String> cmd = splitString(receive, ' ', 3);
                        if (ProxyConfig.getIdentify().equals(cmd.get(2))) {
                            // 是自己
                            findSelf = true;
                            continue;
                        }

                        // get start time from peer
                        os.write("heartbeat\n".getBytes(StandardCharsets.UTF_8));
                        os.flush();
                        receive = reader.readLine();
                        cmd = splitString(receive, ' ', 3);
                        if ("HB".equalsIgnoreCase(cmd.get(0))) {
                            long remove_start = Long.parseLong(cmd.get(1));
                            if (remove_start < start_time) {
                                start_time = remove_start;
                                suitable_server = address;
                            }
                        } else {
                            Logger.warnLog("FindSuitableServer::getServer() Unknown received: '{}'", receive);
                        }
                    } catch (Throwable e) {
                        //e.printStackTrace();
                    } finally {
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (Throwable t2) {
                            }
                        }
                    }
                } catch (Exception e) {
                    if (Logger.isDebug()) {
                        Logger.debugLog(e, "FindSuitableServer::getServer() Connect to server failed: ", e.getMessage());
                    } else {
                        Logger.infoLog("FindSuitableServer::getServer() Connect to server failed: ", e);
                    }
                }
            }
        }

        if (suitable_server == null && findSelf) {
            // 只发现配置了一个指向自己的同步地址
            suitable_server = ProxyConfig.getLocalAddress();
        }

        return suitable_server;
    }
}
