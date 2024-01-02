package com.tongtech.proxy.core.slices.codec.redis;

public class RedisProtocolError extends RuntimeException {
    public RedisProtocolError(String msg) {
        super(msg);
    }
}
