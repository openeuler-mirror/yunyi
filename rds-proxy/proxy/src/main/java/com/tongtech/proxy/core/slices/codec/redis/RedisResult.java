package com.tongtech.proxy.core.slices.codec.redis;

public interface RedisResult {

    byte[] getData();

    default boolean isParseOK() {
        return true;
    }

    default RedisResult getParent() {
        return null;
    }
}
