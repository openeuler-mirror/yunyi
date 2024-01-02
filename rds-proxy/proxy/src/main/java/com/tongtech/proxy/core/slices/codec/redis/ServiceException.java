package com.tongtech.proxy.core.slices.codec.redis;

import java.io.IOException;

public class ServiceException extends IOException {
    public ServiceException(String s) {
        super(s);
    }
}
