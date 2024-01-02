package com.tongtech.proxy.core.cli;

public class ErrorString {
    private final String str;

    public ErrorString(String msg) {
        this.str = msg;
    }

    public String toString() {
        return this.str;
    }
}
