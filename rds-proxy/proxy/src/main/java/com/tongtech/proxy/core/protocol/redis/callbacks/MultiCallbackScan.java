package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.slices.ResultCallback;
import com.tongtech.proxy.core.slices.ServiceMapping;
import com.tongtech.proxy.core.slices.codec.redis.ServiceException;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.RdsString;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MultiCallbackScan implements ResultCallback {
    private static final int shiftBits = 40;
    private static final long shiftMask = (1l << shiftBits) - 1;
    private static final int cursorPos = 1;

    private final Vector Datas = new Vector();
    private volatile int countPos = -1;
    private volatile int count = 10;
    private volatile long cursorHead;
    private volatile long cursorTail;
    private volatile byte[] match;

    private final static Log logger = ProxyConfig.getServerLog();

    private final DataResult finalResult;
    private final List request;

    public MultiCallbackScan(DataResult result, List request) throws IOException {
        this.finalResult = result;
        this.request = request;

        long cursor = RdsString.parseLong((byte[]) request.get(cursorPos));
        cursorHead = cursor >>> shiftBits;
        cursorTail = cursor & shiftMask;
        int pos = 2;
        while (pos < request.size()) {
            String str = new String((byte[]) request.get(pos++), StandardCharsets.UTF_8);
            if ("Match".equalsIgnoreCase(str)) {
                match = (byte[]) request.get(pos++);
            } else if ("Count".equalsIgnoreCase(str)) {
                countPos = pos;
                count = Integer.parseInt(new String((byte[]) request.get(pos++), StandardCharsets.UTF_8));
            }
        }
        start();
    }

    public void start() throws IOException {
        ServiceMapping manager = ProxyController.INSTANCE.getNormalManagerDirect((int) cursorHead);
        if (manager != null) {
            request.set(cursorPos, Long.toString(cursorTail).getBytes(StandardCharsets.UTF_8));
            manager.exchangeRedisData(this, request,finalResult.getTableId());
        } else {
            result();
        }
    }

    @Override
    public void callback(Object o) throws IOException {
        if (o instanceof ServiceException) {
            finalResult.setErr(-9, ((ServiceException) o).getMessage());
            return;
        }

        List list = (List) o;
        cursorTail = RdsString.parseLong((byte[]) list.get(0));
        list = (List) list.get(1);
        Datas.addAll(list);

        this.count -= list.size();
        if (cursorTail == 0) {
            // 开始遍历下一个分片
            cursorHead++;
            if (ProxyController.INSTANCE.getNormalManagerDirect((int) cursorHead) == null) {
                // 没有下一个分片了
                cursorHead = 0;
            }
        }

        if (cursorHead == 0 && cursorTail == 0 /*已经遍历完了*/ || count <= 0/*遍历个数已经够了*/) {
            // 已经遍历完了
            result();
        } else {
            ServiceMapping manager = ProxyController.INSTANCE.getNormalManagerDirect((int) cursorHead);
            request.set(cursorPos, Long.toString(cursorTail).getBytes(StandardCharsets.UTF_8));
            if (countPos > 0) {
                request.set(countPos, Integer.toString(count).getBytes(StandardCharsets.UTF_8));
            }
            manager.exchangeRedisData(this, request,finalResult.getTableId());
        }
    }

    protected void result() throws IOException {
        ArrayList ret = new ArrayList();
        ret.add(Long.toString((cursorHead << shiftBits) | cursorTail).getBytes());
        ret.add(Datas);
        finalResult.sendObject(ret);
    }
}
