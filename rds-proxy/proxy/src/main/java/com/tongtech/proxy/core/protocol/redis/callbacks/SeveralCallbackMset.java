package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

public class SeveralCallbackMset extends SeveralCallback {

    public SeveralCallbackMset(DataResult result, List request) throws IOException {
        super(result, request, "set", false,1);
    }

    @Override
    protected void process(Object o) {
    }

    @Override
    protected void result(DataResult result) throws IOException {
        result.setOk();
    }
}
