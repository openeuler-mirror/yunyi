package com.tongtech.proxy.core.protocol;

public class ProtocolErrorException extends RuntimeException{
    public ProtocolErrorException(String msg){
        super(msg);
    }
}
