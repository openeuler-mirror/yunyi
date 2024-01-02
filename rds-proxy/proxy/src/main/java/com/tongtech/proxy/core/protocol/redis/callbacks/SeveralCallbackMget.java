package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SeveralCallbackMget extends SeveralCallback {
    private final List data = new ArrayList();

    public SeveralCallbackMget(DataResult result, List request) throws IOException {
        super(result, request, "get", false,0);
    }

    @Override
    protected void process(Object o) {
        data.add(o);
    }

    @Override
    protected void result(DataResult result) throws IOException {
        result.sendObject(data);
    }
}
