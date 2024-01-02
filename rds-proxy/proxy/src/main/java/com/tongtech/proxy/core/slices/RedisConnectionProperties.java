package com.tongtech.proxy.core.slices;

import io.netty.channel.Channel;

import java.util.List;

public class RedisConnectionProperties {
    private ServiceMapping mapping;
    private final Channel channel;
    private long timestamp;
    private ResultCallback callback;
    private boolean authOk;
    private boolean closed;
    private List data;
    private int db = 0;

    public RedisConnectionProperties(Channel channel) {
        this.channel = channel;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "{\"remote\":\"" + mapping.getHost() + ":(" + mapping.getRdsPort() + "," + mapping.getRedisPort() + ")\",\"timestame\":" + this.timestamp + "}";
    }

    public void channelClose() {
        this.channel.close();
        if (mapping != null) {
            mapping.closeChannel(this.channel);
        }
    }

    public synchronized ServiceMapping getMapping() {
        return mapping;
    }

    public synchronized void setMapping(ServiceMapping mapping) {
        this.mapping = mapping;
    }

    public Channel getChannel() {
        return channel;
    }

    public synchronized long getTimestamp() {
        return timestamp;
    }

    public synchronized void setTimestamp() {
        this.timestamp = System.currentTimeMillis();
    }

    public synchronized ResultCallback getCallback() {
        return callback;
    }

    public synchronized void setCallback(ResultCallback callback) {
        this.callback = callback;
    }

    public synchronized List getData() {
        return this.data;
    }

    public synchronized void setData(List data) {
        this.data = data;
    }

    public synchronized boolean isAuthOk() {
        return authOk;
    }

    public synchronized void setAuthOk() {
        this.authOk = true;
    }

    public synchronized int getDb() {
        return db;
    }

    public synchronized void setDb(int db) {
        this.db = db;
    }

    /**
     * 需要外部同步
     *
     * @return
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * 需要外部同步
     *
     * @param closed
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
