package com.tongtech.proxy.core.protocol;


import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.tongtech.proxy.core.server.io.CodecContext.ZERO_STRING_OBJECT;

public class SlowLogs {
    private final static Log logger = ProxyConfig.getServerLog();

    private final static int MAX_LEN = ProxyConfig.getSlowOperationMaxLen();
    private static long sno = 0;

    private static final List[] Logs = new List[MAX_LEN];
    private static int stop = 0;
    private static boolean isRoll = false;

    public synchronized static void add(List cmd, long amount, ChannelHandlerContext ctx, String clientName) {
        if (ctx != null) {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            if (cmd != null && cmd.size() > 0 && cmd.get(0) instanceof String) {
                cmd.set(0, ((String) cmd.get(0)).getBytes(StandardCharsets.UTF_8));
            }

            ArrayList data = new ArrayList();
            data.add(sno++);
            data.add(System.currentTimeMillis());
            data.add(amount);
            data.add(cmd);
            data.add((socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort()).getBytes(StandardCharsets.UTF_8));
            data.add(clientName != null ? clientName.getBytes(StandardCharsets.UTF_8) : ZERO_STRING_OBJECT);

            Logs[stop++] = data;
            if (stop >= MAX_LEN) {
                stop = 0;
                isRoll = true;
            }
        }
    }

    public synchronized static ArrayList get(int count) {
        ArrayList ret = new ArrayList();
        if (!isRoll) {
            int off = stop - 1;
            for (int i = 0; i < count && off >= 0; ++i, --off) {
                ret.add(Logs[off]);
            }
        } else {
            int off = stop - 1;
            int len = count >= 0 ? Math.min(count, MAX_LEN) : MAX_LEN;
            for (int i = 0; i < len; ++i, --off) {
                if (off < 0) {
                    off += MAX_LEN;
                }
                ret.add(Logs[off]);
            }
        }
        return ret;
    }

    public synchronized static int len() {
        if (!isRoll) {
            return stop;
        } else {
            return MAX_LEN;
        }
    }

    public synchronized static void reset() {
        stop = 0;
        isRoll = false;
        for (int i = 0; i < Logs.length; ++i) {
            Logs[i] = null;
        }
    }
}
