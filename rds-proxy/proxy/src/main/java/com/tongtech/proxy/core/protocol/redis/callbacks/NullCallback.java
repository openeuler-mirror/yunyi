package com.tongtech.proxy.core.protocol.redis.callbacks;

import com.tongtech.proxy.core.slices.ResultCallback;

import java.io.IOException;

public class NullCallback implements ResultCallback {
    public final static ResultCallback INSTANCE = new NullCallback();

    @Override
    public void callback(Object o) throws IOException {

    }
}
