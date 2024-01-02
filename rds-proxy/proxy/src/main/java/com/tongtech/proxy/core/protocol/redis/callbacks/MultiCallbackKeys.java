package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class MultiCallbackKeys extends MultiCallback{
    private final Vector Datas = new Vector();

    public MultiCallbackKeys(DataResult result, List request) throws IOException {
        super(result, request);
    }

    @Override
    protected void process(Object o) {
        List list = (List) o;
        Datas.addAll(list);
    }

    @Override
    protected void result(DataResult result) throws IOException {
        result.sendObject(Datas);
    }
}
