package com.tongtech.proxy.core.rescue;

import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;
import com.tongtech.proxy.core.db.LuaManager;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Vector;

import static com.tongtech.proxy.core.StaticContent.lengthAsUtf8;
import static com.tongtech.proxy.core.StaticContent.splitString;

public class ProxyDataRescue {

    // 日志类
    private static final Log Logger = ProxyConfig.getServerLog();

    // 当动态改变sync配置时，检查此项，如果未同步则同步1次
    // 只要此值不为真，每次改变sync的配置时都会尝试从其他节点同步数据
    // 当配置了不需要同步时，SuccessfullyImplemented 初始值为 true
//    private volatile static boolean SuccessfullyImplemented = "False".equalsIgnoreCase(ProxyConfig.getProperty("Server.Common.NeedRescue"));

    private final static int TotalSections = 100;

    /**
     * 按照新的协议发送请求
     *
     * @param table_id
     * @param ranges
     * @return
     */
    private static String protocolRequest(int table_id, String ranges) {
        String cmd = "datarescue";
        StringBuilder buf = new StringBuilder(32);
        if (ranges == null) {
            buf.append("*3").append('\n');
        } else {
            buf.append("*4").append('\n');
        }
        buf.append(":0").append('\n');
        buf.append('$').append(cmd.length()).append('\n').append(cmd).append('\n');
        buf.append(':').append(table_id).append('\n');
        if (ranges != null) {
            buf.append('$').append(ranges.length()).append('\n').append(ranges).append('\n');
        }
        return buf.toString();
    }

    /**
     * 从指定服务器同步指定范围的数据
     *
     * @param addr
     * @return
     */
    public static boolean getDataFromOther(InetSocketAddress addr) {
        boolean rescueOK = false;
        int table_id = 0;
        int count = 0;

//        // 只要执行到这，说明已经至少读过一次配置文件了
//        SuccessfullyImplemented = true;

        // 未查到合适的同步服务器
        if (addr == null) {
            Logger.warnLog("DataRescue::getDataFromOther() Can not find suitable-server, rescue end.");
            return true;
        } else if (addr == ProxyConfig.getLocalAddress()) {
            // 同步列表里只配了自己，等同于没配
            Logger.infoLog("DataRescue::getDataFromOther() Only find myself, rescue canceled.");
            return true;
        }

        // 是否用同步IO
//            if (false) {
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
            socket.setSoTimeout(10000);// 10 second
            socket.connect(addr, 1000);
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).startHandshake();
            }
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            RescureReader reader = new RescureReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            if ((ProxyConfig.getSecureLevel() & 0x2) > 0) {
                int utf8Len = lengthAsUtf8(ProxyConfig.getAuthenString());
                writer.write("*2\n$4\nauth\n$" + utf8Len + "\n" + ProxyConfig.getAuthenString() + "\n");
//                    writer.write("auth " + Configuration.getAuthenString() + "\n");
            }
//                writer.write("0 datarescue " + CurrentTableId + "\n");
            writer.write(protocolRequest(table_id, null));
            writer.flush();
            String receive;
            while ((receive = reader.readLine()) != null) {
                if (receive == null || receive.length() == 0) {
                    // 连接被关闭了
                    Logger.debugLog("DataRescue::messageReceived() Close connection.");
                    return rescueOK;
                } else if (receive.startsWith("waiting")) {
                    Logger.debugLog("DataRescue::messageReceived() Waiting for data.");
                } else if (receive.startsWith("rescue")) {
                    rescueOK = true;
                    Logger.infoLog("DataRescue::messageReceived() Get {} datas for table-{}."
                            , count, table_id);
                    break;
                } else if (table_id == 0) {
                    Vector<String> s = splitString(receive, ' ', TotalSections);
                    int pos = 0;
                    long timestamp = pos < s.size() ? Long.parseLong(s.get(pos++)) : 0;
                    long exp_time = pos < s.size() ? Long.parseLong(s.get(pos++)) : 0;
                    String key = pos < s.size() ? s.get(pos++) : null;
                    String value = pos < s.size() ? s.get(pos++) : null;
                        /*if (XGroupManager.XGROUP_SAVEKEY.equals(key)) {
                            byte[] v = BinaryStringUtil.getBytes(value);
                            XGroupManager.unserialize(v);
                        } else*/
                    if (LuaManager.LUA_SAVEKEY.equals(key)) {
                        byte[] v = StaticContent.accept(value, 0, value.length());
                        LuaManager.unserialize(v);
                    }
                } else {
                    break;
                }
            }
        } catch (Throwable t) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (Throwable t2) {
                }
            }
        }

        return rescueOK;
    }

}
