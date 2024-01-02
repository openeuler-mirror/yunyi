package com.tongtech.proxy.core.sync;

import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.tongtech.proxy.core.sync.SyncMonitorUtil.SOCKET_CONNECT_TIMEOUT;
import static com.tongtech.proxy.core.sync.SyncMonitorUtil.SOCKET_DATA_TIMEOUT;


public class SyncNodeConnection {
    private static final Log Logger = ProxyConfig.getServerLog();

    private final Socket Socket;
    private final InetSocketAddress Address;
    private final BufferedOutputStream Writer;
    private final BufferedReader Reader;

    private volatile boolean needAuthen = (ProxyConfig.getSecureLevel() & 0x2) > 0;

    public SyncNodeConnection(Socket socket, InetSocketAddress address) throws IOException {
        Socket = socket;
        Address = address;
        Writer = new BufferedOutputStream(socket.getOutputStream());
        Reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public synchronized String exchange(Object data) throws IOException {
        if (needAuthen) {
//            Logger.warnLog("SyncNodeConnection::exchange() send passwd");
            ArrayList auth = new ArrayList();
            auth.add("auth");
            auth.add(ProxyConfig.getAuthenString());
            writeData2OutputStream(auth, Writer, true);
//            int utf8Len = lengthAsUtf8(Configuration.getAuthenString());
//            Writer.write("*2\n$4\nauth\n$" + utf8Len + "\n" + Configuration.getAuthenString() + "\n");
//            Writer.write("auth " + Configuration.getAuthenString());
            needAuthen = false;
        }

//        Logger.warnLog("SyncNodeConnection::exchange() send {}",data);
//        Writer.write(data);
//        Writer.write('\n');
//        Writer.flush();
        writeData2OutputStream(data, Writer, true);
        String str = Reader.readLine();
//        Logger.warnLog("SyncNodeConnection::exchange() receive {}",str);

        return str;
    }

    public String getIdentify() {
        try {
            String[] response = exchange("check").split(" ");
            if (response != null && response.length >= 3) {
                return response[2];
            }
        } catch (Exception e) {
            //throw new IOException(e.getMessage());
        }
        return null;
    }

    public int getRedisPort() {
        try {
            String[] response = exchange("config").split(" ");
            if (response != null && response.length >= 2) {
                return Integer.parseInt(response[1]);
            }
        } catch (Exception e) {
            //throw new IOException(e.getMessage());
        }
        return 0;
    }

    /**
     * 判断对方的redis仿真端口进入监听状态。RDS进程启动后主业务端口就会进入监听状态，但直到启动完成后才会开始监听仿真端口
     * 在判断集群节点是否恢复时，需要将仿真端口是否可用作为节点恢复的判断依据
     *
     * @return
     */
    public boolean redisAlived() {
        int redisPort = getRedisPort();
        Socket redisSocket = null;
        if (redisPort >= 1024) {
            try {
                InetSocketAddress redisAddress = new InetSocketAddress(Address.getAddress(), redisPort);
                redisSocket = new Socket();
                redisSocket.setTcpNoDelay(true);
                redisSocket.setSoTimeout(SOCKET_DATA_TIMEOUT);// 1 second
                redisSocket.connect(redisAddress, SOCKET_CONNECT_TIMEOUT);
                return true;
            } catch (Throwable t) {
                Logger.debugLog(t, "SyncNodeConnection::redisAlived() Connect to {} failed: {}"
                        , redisPort, t);
            } finally {
                if (redisSocket != null) {
                    try {
                        redisSocket.close();
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            Logger.debugLog("SyncNodeConnection::redisAlived() invalide redis-port: {}", redisPort);
        }
        return false;
    }

    /**
     * 判断对方进程的主业务端口是否正常，可以判断进程是否正常
     *
     * @return
     */
    public boolean alive() {
        try {
            return "ok".equals(exchange("idle"));
        } catch (Exception e) {
        }
        return false;
    }

    public void close() {
        try {
            Writer.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        try {
            Reader.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        try {
            Socket.close();
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private static void writeData2OutputStream(Object data, OutputStream os, boolean isFirst) throws IOException {
        if (data == null) {
            os.write('*');
            os.write('0');
            os.write('\n');
        } else if (data instanceof List) {
            List list = (List) data;
            os.write('*');
            os.write(Integer.toString(list.size()).getBytes());
            os.write('\n');
            for (Object o : list) {
                writeData2OutputStream(o, os, false);
            }
        } else if (data instanceof byte[]) {
            if (isFirst) {
                os.write('*');
                os.write('1');
                os.write('\n');
            }
            byte[] bs = (byte[]) data;
            os.write('$');
            os.write(Integer.toString(bs.length).getBytes());
            os.write('\n');
            if (bs.length > 0) {
                os.write(bs);
            }
            os.write('\n');
        } else if (data instanceof String) {
            if (isFirst) {
                os.write('*');
                os.write('1');
                os.write('\n');
            }
            String str = (String) data;
//            byte[] bs = accept(str, 0, str.length());
            // 对端接收端是RDS解码器，输入的必须是UTF8格式，不能是真正2进制的格式
            byte[] bs = str.getBytes(StandardCharsets.UTF_8);
            os.write('$');
            os.write(Integer.toString(bs.length).getBytes());
            os.write('\n');
            if (bs.length > 0) {
                os.write(bs);
            }
            os.write('\n');
        } else if (data instanceof Long) {
            if (isFirst) {
                os.write('*');
                os.write('1');
                os.write('\n');
            }
            os.write(':');
            os.write(((Long) data).toString().getBytes());
            os.write('\n');
        } else if (data instanceof Integer) {
            if (isFirst) {
                os.write('*');
                os.write('1');
                os.write('\n');
            }
            os.write(':');
            os.write(((Integer) data).toString().getBytes());
            os.write('\n');
        }
        os.flush();
    }

    @Override
    public String toString() {
        return Address.getHostString() + ":" + Address.getPort();
    }

    @Override
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
