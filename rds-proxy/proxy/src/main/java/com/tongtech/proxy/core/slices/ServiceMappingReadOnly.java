package com.tongtech.proxy.core.slices;

import io.netty.channel.Channel;
import com.tongtech.proxy.core.protocol.Commands;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.OriginalSocketAddress;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.tongtech.proxy.core.slices.RedisConnectionFactory.RedisChannelManagers;

public class ServiceMappingReadOnly implements ServiceMapping {
    private static final Log logger = ProxyConfig.getServerLog();

    private final OriginalSocketAddress address;
    private final String host;
    private volatile int slice_start;
    private volatile int slice_stop;
    private final int secure_level;
    private final int rds_port;
    private final String rds_passwd;
    private final int redis_port;
    private final String redis_passwd;

    private final ArrayList<Channel> ChannelsList = new ArrayList<>();
    private final HashSet<Channel> ChannelsSet = new HashSet<>();

    public ServiceMappingReadOnly(String host, int secure_level
            , int rds_port, String rds_passwd, int redis_port, String redis_passwd) {
//        if (address == null) {
//            throw new NullPointerException("address is null when creating NodeManager");
//        }
        this.address = new OriginalSocketAddress(host, rds_port);
        this.host = host;
        this.secure_level = secure_level;
        this.rds_port = rds_port;
        this.rds_passwd = rds_passwd;
        this.redis_port = redis_port;
        this.redis_passwd = redis_passwd;
    }

//    public OriginalSocketAddress getAddress() {
//        return this.address;
//    }

    @Override
    public String getType() {
        return "maintain";
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getStart() {
        return this.slice_start;
    }

    @Override
    public void setStart(int start) {
        this.slice_start = start;
    }

    @Override
    public int getStop() {
        return this.slice_stop;
    }

    @Override
    public void setStop(int stop) {
        this.slice_stop = stop;
    }

    @Override
    public int getRdsPort() {
        return rds_port;
    }

    @Override
    public int getRedisPort() {
        return redis_port;
    }

    @Override
    public int getFreeConns() {
        return this.ChannelsList.size();
    }

    @Override
    public synchronized void closeChannel(Channel channel) {
        if (ChannelsSet.remove(channel)) {
            ChannelsList.remove(channel);
        }
    }

    @Override
    public void pullSliceDataFromMaster() {
    }

    @Override
    public List getSliceData() {
        return null;
    }

    @Override
    public void exchangeRedisData(ResultCallback callback, List data, int db) throws IOException {
        Channel channel = null;
        String cmd = null;
        try {
            cmd = (String) data.get(0);
            if (!Commands.READONLY_COMMANDS.contains(cmd)) {
                callback.callback(new IOException("ERR Data is being maintained"));
                logger.infoLog("ServiceMappingReadOnly::exchangeRedisData() data is in maintained");
                return;
            }

            synchronized (this) {
                while (ChannelsList.size() > 0) {
                    channel = ChannelsList.remove(ChannelsList.size() - 1);
                    ChannelsSet.remove(channel);
//                    if (channel.isActive()) {
//                        break;
//                    } else {
//                        channel = null;
//                    }
                    RedisConnectionProperties properties = RedisChannelManagers.get(channel);
                    if (properties != null) {
                        synchronized (properties) {
                            if (!properties.isClosed() && channel.isActive()) {
                                properties.setTimestamp();
                                break;
                            } else {
                                channel = null;
                            }
                        }
                    }
                }
            }

            if (channel == null) {
                if ((this.secure_level & 1) > 0) {
                    channel = RedisSslConnectionFactory.getConnection(this);
                } else {
                    channel = RedisConnectionFactory.getConnection(this);
                }
                if (channel != null) {
                    if ((this.secure_level & 2) != 0) {
                        ArrayList auth = new ArrayList();
                        auth.add("auth".getBytes(StandardCharsets.UTF_8));
                        auth.add(this.redis_passwd.getBytes(StandardCharsets.UTF_8));
                        channel.writeAndFlush(auth);
                    } else {
                        RedisConnectionProperties properties = RedisChannelManagers.get(channel);
                        properties.setAuthOk();
                    }
                }
            }

            // 发送数据
//            ConnectionProperties properties = null;
            if (channel != null) {
                RedisConnectionProperties properties = RedisChannelManagers.get(channel);
                if (properties != null) {
                    properties.setTimestamp();
                    properties.setData(data);
                    properties.setCallback(callback);
                    if (db != properties.getDb()) {
                        channel.write(ServiceMappingNormal.getSelectData(db));
                        properties.setDb(db);
                    }
                    channel.writeAndFlush(data);
//                    if (db != 0) {
//                        channel.write(ServiceMappingNormal.getSelectData(0));
//                    }
                    return;
                }
            }
        } catch (Throwable t) {
            callback.callback(new IOException("ERR " + t));
            if (logger.isInfo()) {
                logger.warnLog(t, "ServiceMappingReadOnly::exchangeRedisData() Error in send command {} to {}: {}"
                        , cmd, channel, t.getMessage());
            } else {
                logger.warnLog("ServiceMappingReadOnly::exchangeRedisData() Error in send command {} to {}: {}"
                        , cmd, channel, t);
            }
            return;
        }
        callback.callback(new IOException("ERR connect to service failed: "
                + (channel == null ? "channel is null" : "properties is null")));
        logger.warnLog("ServiceMappingReadOnly::exchangeRedisData() Error connect to service failed: {}"
                , channel == null ? "channel is null" : "properties is null");
    }

    @Override
    public synchronized void finalize() {
        for (Channel channel : ChannelsList) {
            channel.close();
        }
        ChannelsList.clear();
        ChannelsSet.clear();
    }

    @Override
    public synchronized void releaseChannel(Channel channel) {
        if (!ChannelsSet.contains(channel)) {
            ChannelsList.add(channel);
            ChannelsSet.add(channel);
        }
    }

    /**
     * 不支持直接连接
     *
     * @param callback
     * @return
     */
    @Override
    public Channel getDirectRedisConnection(ResultCallback callback, int db) {
//        Channel channel;
//        if ((this.secure_level & 1) > 0) {
//            channel = DirectRedisSslConnectionFactory.getConnection(this, callback);
//        } else {
//            channel = DirectRedisConnectionFactory.getConnection(this, callback);
//        }
//        if (channel != null) {
//            if ((this.secure_level & 2) != 0) {
//                ArrayList auth = new ArrayList();
//                auth.add("auth".getBytes(StandardCharsets.UTF_8));
//                auth.add(this.redis_passwd.getBytes(StandardCharsets.UTF_8));
//                channel.writeAndFlush(auth);
//            } else {
//                ConnectionProperties properties = DirectRedisChannelManagers.get(channel);
//                properties.setAuthOk();
//            }
//        }
//        return channel;
        return null;
    }

    @Override
    public int hashCode() {
        return this.address.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ServiceMappingReadOnly) {
            try {
                ServiceMappingReadOnly other = (ServiceMappingReadOnly) o;
                boolean rdsPasswordIsEqual = ((this.secure_level & 0x2) == 0 && (other.secure_level & 0x2) == 0)
                        || (this.rds_passwd == null && other.rds_passwd == null)
                        || (this.rds_passwd != null && this.rds_passwd.equals(other.rds_passwd));

                boolean redisPasswordIsEqual = ((this.secure_level & 0x2) == 0 && (other.secure_level & 0x2) == 0)
                        || (this.redis_passwd == null && other.redis_passwd == null)
                        || (this.redis_passwd != null && this.redis_passwd.equals(other.redis_passwd));

                return this.address.equals(other.address)
                        && this.redis_port == other.redis_port
                        && this.secure_level == other.secure_level
                        && rdsPasswordIsEqual
                        && redisPasswordIsEqual;
            } catch (Throwable t) {
                logger.warnLog(t, "ServiceMappingReadOnly::equals() Error occur: {}", t.getMessage());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.address.toString();
    }
}
