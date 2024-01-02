package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.slices.ResultCallback;
import com.tongtech.proxy.core.slices.ServiceMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OthersCallbackRpopLpush implements ResultCallback {
    private final DataResult result;
    private final byte[] desc;

    public OthersCallbackRpopLpush(DataResult result, List request) {
        this.result = result;
        this.desc = (byte[]) request.get(2);

        try {
            byte[] key = (byte[]) request.get(1);
            ServiceMapping node = ProxyController.INSTANCE.getNodeManager(key);
            ArrayList data = new ArrayList();
            data.add("rpop");
            data.add(key);

            node.exchangeRedisData(this, data, result.getTableId());
        } catch (Throwable t) {
            try {
                callback(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void callback(Object o) throws IOException {
        if (o instanceof byte[]) {
            ServiceMapping node = ProxyController.INSTANCE.getNodeManager(this.desc);
            if (node != null) {
                ArrayList data = new ArrayList();
                data.add("lpush");
                data.add(this.desc);
                data.add(o);

                node.exchangeRedisData(NullCallback.INSTANCE, data, result.getTableId());
            }
        }
        result.sendObject(o);
    }
}
