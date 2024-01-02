package com.tongtech.proxy.core.protocol.redis.callbacks;

import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.SessionAttribute;
import com.tongtech.proxy.core.protocol.SlowLogs;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.slices.ResultCallback;
import com.tongtech.proxy.core.slices.ServiceMapping;
import com.tongtech.proxy.core.slices.codec.redis.ServiceException;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tongtech.proxy.core.StaticContent.CachedSessionAttributes;

/**
 * 多key命令，例如：mget、del、mset、msetnx、exists等
 */
public abstract class SeveralCallback implements ResultCallback {
    private final static Log logger = ProxyConfig.getServerLog();

    private final DataResult finalResult;
    private final List request;
    private final String command;
    private final int lenOfValue;
    private volatile int idx = 1;
    private final long StartTime;

    public SeveralCallback(DataResult result, List request, String cmd, boolean haveDescKey, int len_of_value) throws IOException {
        this.finalResult = result;
        this.request = request;
        this.lenOfValue = len_of_value;
        this.command = cmd;
        this.StartTime = System.currentTimeMillis();

        ArrayList data = new ArrayList();
        data.add(cmd);
        if (haveDescKey) {
            // 跳过第一个key
            idx++;
        }
        byte[] key = (byte[]) request.get(idx++);
        data.add(key);
        if (len_of_value > 0) {
            for (int i = 0; i < len_of_value; ++i) {
                data.add(request.get(idx++));
            }
        }

        ServiceMapping manager = ProxyController.INSTANCE.getNodeManager(key);
        if (manager != null) {
            manager.exchangeRedisData(this, data, result.getTableId());
        } else {
            result();
        }
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx) {
        // do nothing
    }

    @Override
    public void callback(Object o) throws IOException {
//        List list = (List) o;
//        Datas.addAll(list);

        if (o instanceof ServiceException) {
            finalResult.setErr(-9, ((ServiceException) o).getMessage());
            return;
        }

        process(o);

        if (request.size() > idx + lenOfValue) {
            // 还有后续
            ArrayList data = new ArrayList();
            data.add(this.command);
            byte[] key = (byte[]) request.get(idx++);
            data.add(key);
            if (this.lenOfValue > 0) {
                for (int i = 0; i < this.lenOfValue; ++i) {
                    data.add(request.get(idx++));
                }
            }

            ServiceMapping manager = ProxyController.INSTANCE.getNodeManager(key);
            if (manager != null) {
                try {
                    manager.exchangeRedisData(this, data, finalResult.getTableId());
                } catch (Throwable t) {
                    // 异常结束了
                    result();
                    logger.warnLog("CallbackKeys::callback() Error in get keys: {}", t);
                }
            } else {
                result();
            }
        } else {
            result();
        }
    }

    private void result() throws IOException {
        long consuming = System.currentTimeMillis() - StartTime;
        if (consuming >= ProxyConfig.getSlowOperationThreshold()) {
            try {
                ChannelHandlerContext ctx = finalResult.getSession();
                if (ctx != null) {
                    SessionAttribute attribute = CachedSessionAttributes.get(ctx);
                    SlowLogs.add(request, consuming, finalResult.getSession(), attribute != null ? attribute.getClientName() : null);
                }
            } catch (Throwable t) {
            }
        }
        result(finalResult);
    }

    abstract protected void process(Object o);

    abstract protected void result(DataResult result) throws IOException;
}
