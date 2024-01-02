package com.tongtech.proxy.core.protocol.redis.callbacks;

import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.protocol.SessionAttribute;
import com.tongtech.proxy.core.protocol.SlowLogs;
import com.tongtech.proxy.core.slices.ResultCallback;
import com.tongtech.proxy.core.slices.ServiceMapping;
import com.tongtech.proxy.core.slices.codec.redis.ServiceException;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.util.List;

import static com.tongtech.proxy.core.StaticContent.CachedSessionAttributes;

/**
 * 会访问每个Slice的命令，如keys、scan、dbsize等
 */
public abstract class MultiCallback implements ResultCallback {
    private final static Log logger = ProxyConfig.getServerLog();

    private final DataResult finalResult;
    private final List request;
    private volatile int idx = 0;
    private final long StartTime;

    public MultiCallback(DataResult result, List request) throws IOException {
        this.finalResult = result;
        this.request = request;
        this.StartTime = System.currentTimeMillis();

        ServiceMapping manager = ProxyController.INSTANCE.getNormalManagerDirect(idx++);
        if (manager != null) {
            manager.exchangeRedisData(this, request, result.getTableId());
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

        ServiceMapping manager = ProxyController.INSTANCE.getNormalManagerDirect(idx++);
        if (manager != null) {
            try {
                manager.exchangeRedisData(this, request, finalResult.getTableId());
            } catch (Throwable t) {
                // 异常结束了
                result();
                logger.warnLog("CallbackKeys::callback() Error in get keys: {}", t);
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
