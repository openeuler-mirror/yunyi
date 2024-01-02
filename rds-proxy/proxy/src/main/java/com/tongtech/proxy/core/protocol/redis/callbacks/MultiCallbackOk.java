package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

/**
 * for save、flushdb、flushall。。。
 */
public class MultiCallbackOk extends MultiCallback {
    public MultiCallbackOk(DataResult result, List request) throws IOException {
        super(result, request);
    }

    @Override
    protected void process(Object o) {
        // do nothing
    }

    @Override
    protected void result(DataResult result) throws IOException {
        result.setOk();
    }
}
