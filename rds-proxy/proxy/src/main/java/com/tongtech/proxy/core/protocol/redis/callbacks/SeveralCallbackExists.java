package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

public class SeveralCallbackExists extends SeveralCallback {
    private volatile long total_deleted = 0;

    public SeveralCallbackExists(DataResult result, List request) throws IOException {
        super(result, request, "exists", false,0);
    }

    @Override
    protected void process(Object o) {
        if (o instanceof Long) {
            total_deleted += ((Long) o).intValue();
        }
    }

    @Override
    protected void result(DataResult result) throws IOException {
        result.sendObject(total_deleted);
    }
}
