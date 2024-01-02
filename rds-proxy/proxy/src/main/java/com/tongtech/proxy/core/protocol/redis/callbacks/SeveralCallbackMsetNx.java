package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

public class SeveralCallbackMsetNx extends SeveralCallback {
    private volatile long isSet = 1;

    public SeveralCallbackMsetNx(DataResult result, List request) throws IOException {
        super(result, request, "setnx", false,1);
    }

    @Override
    protected void process(Object o) {
        if (o instanceof Long) {
            long cur = ((Long) o).intValue();
            if (cur == 0) {
                isSet = 0;
            }
        }
    }

    @Override
    protected void result(DataResult result) throws IOException {
        result.sendObject(isSet);
    }
}
