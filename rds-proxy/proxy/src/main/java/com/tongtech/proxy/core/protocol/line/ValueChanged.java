package com.tongtech.proxy.core.protocol.line;

public class ValueChanged {
    private long update_time = 0;
    private String value = null;

    public long getUpdateTime() {
        return update_time;
    }

    public void setUpdateTime(long update_time) {
        this.update_time = update_time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
