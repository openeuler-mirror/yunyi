package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

public class MultiCallbackDbsize extends MultiCallback {
    private volatile long dbsize = 0;

    public MultiCallbackDbsize(DataResult result, List request) throws IOException {
        super(result, request);
    }

    @Override
    protected void process(Object o) {
        if (o instanceof Long) {
             dbsize += (Long)o;
        }
    }

    @Override
    protected void result(DataResult result) throws IOException {
        result.sendObject(new Long(dbsize));
    }
}
