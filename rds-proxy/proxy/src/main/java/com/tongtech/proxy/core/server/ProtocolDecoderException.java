package com.tongtech.proxy.core.server;

public class ProtocolDecoderException extends IllegalStateException {
    public ProtocolDecoderException(String error) {
        super(error);
    }

    public ProtocolDecoderException(Throwable t, String msg) {
        super(t);
    }
}
