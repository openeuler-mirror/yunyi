package com.tongtech.proxy.core.sync;

import com.tongtech.proxy.core.utils.OriginalSocketAddress;

public class ReplicationNode {
    // RDS 同步地址
    private final OriginalSocketAddress Address;
    // 对应的 Redis 兼容端口地址
    private final OriginalSocketAddress RedisAddress;
    private final boolean isMyself;
    // 是否活跃
    private final boolean isAlive;

    private final String Identify;

    private final long Timestamp;

    public ReplicationNode(boolean myself, boolean alive, OriginalSocketAddress address
            , OriginalSocketAddress redisAddr,String identify,long timestamp) {
        this.isMyself = myself;
        this.isAlive = alive;
        this.Address = address;
        this.RedisAddress = redisAddr;
        this.Identify=identify;
        this.Timestamp=timestamp;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public boolean isMyself() {
        return isMyself;
    }

    public OriginalSocketAddress getAddress() {
        return Address;
    }

    public OriginalSocketAddress getRedisAddress() {
        return RedisAddress;
    }

    public String getIdentify(){
        return Identify;
    }

    public long getTimestamp(){
        return  this.Timestamp;
    }
}
