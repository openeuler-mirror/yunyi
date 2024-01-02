package com.tongtech.proxy.core.slices;

import io.netty.channel.Channel;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.OriginalSocketAddress;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.tongtech.proxy.core.slices.DirectRedisConnectionAdaptor.DirectRedisChannelManagers;
import static com.tongtech.proxy.core.slices.RedisConnectionFactory.RedisChannelManagers;

public class ServiceMappingNormal implements ServiceMapping {
    private static final Log logger = ProxyConfig.getServerLog();

    private final long SLOW_THRESHOLD = ProxyConfig.getLongProperty("Server.Common.SlowOperationThreshold");

    public static final int MAX_SUPPORTED_DBNUMBER = 512;

    static final ArrayList ALL_SIZE_DATA = new ArrayList() {
        {
            this.add("inner.allsize");
        }
    };

    private static final List[] DbNumbers = new List[MAX_SUPPORTED_DBNUMBER];

    private final DbSizePuller puller = new DbSizePuller();

    private volatile List SliceData = null;

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

    public static List getSelectData(int db) {
        List list;
        if (db < 0 || db >= MAX_SUPPORTED_DBNUMBER) {
            throw new IllegalArgumentException("unsupported db");
        } else {
            synchronized (DbNumbers) {
                list = DbNumbers[db];
                if (list == null) {
                    list = new ArrayList();
                    list.add("inner.select");
                    list.add(Integer.toString(db).getBytes());
                    DbNumbers[db] = list;
                }
            }
        }
        return list;
    }

    public ServiceMappingNormal(String host, int secure_level
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
        return "normal";
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
        puller.pull();
    }

    @Override
    public List getSliceData() {
        return SliceData;
    }

    @Override
    public void exchangeRedisData(ResultCallback callback, List data, int db) throws IOException {
        logger.infoLog("ServiceMappingNormal::exchangeRedisData() Receive message {}", data);

        Channel channel = null;
        String cmd = null;
        try {
            cmd = (String) data.get(0);
            while (true) {
                synchronized (this) {
                    if (ChannelsList.size() > 0) {
                        channel = ChannelsList.remove(ChannelsList.size() - 1);
                        ChannelsSet.remove(channel);
                    } else {
                        channel = null;
                    }
                }

                if (channel == null) {
                    // 缓冲池中已经没有富裕的连接了
                    break;
                }

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
            if (channel != null && channel.isActive()) {
                RedisConnectionProperties properties = RedisChannelManagers.get(channel);
                if (properties != null) {
//                    properties.setTimestamp();
                    properties.setData(data);
                    properties.setCallback(callback);
                    if (db != properties.getDb()) {
                        channel.write(getSelectData(db));
                        properties.setDb(db);
                    }
                    channel.writeAndFlush(data);
//                    if (db != 0) {
//                        channel.write(getSelectData(0));
//                    }
                    return;
                }
            }

            callback.callback(new IOException("ERR connect to service failed: "
                    + (channel == null ? "channel is null" : "properties is null")));
            logger.warnLog("ServiceMappingNormal::exchangeRedisData() Error connect to service failed: {}"
                    , channel == null ? "channel is null" : "properties is null");
        } catch (Throwable t) {
            callback.callback(new IOException("ERR " + t));
            if (logger.isInfo()) {
                logger.warnLog(t, "ServiceMappingNormal::exchangeRedisData() Error in send command {} to {}: {}"
                        , cmd, channel, t.getMessage());
            } else {
                logger.warnLog("ServiceMappingNormal::exchangeRedisData() Error in send command {} to {}: {}"
                        , cmd, channel, t);
            }
            return;
        }
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
     * 创建到后端服务节点的直接连接
     *
     * @param callback
     * @return
     */
    @Override
    public Channel getDirectRedisConnection(ResultCallback callback, int db) {
        Channel channel;
        if ((this.secure_level & 1) > 0) {
            channel = DirectRedisSslConnectionFactory.getConnection(this, callback);
        } else {
            channel = DirectRedisConnectionFactory.getConnection(this, callback);
        }
        if (channel != null) {
            if ((this.secure_level & 2) != 0) {
                ArrayList auth = new ArrayList();
                auth.add("auth".getBytes(StandardCharsets.UTF_8));
                auth.add(this.redis_passwd.getBytes(StandardCharsets.UTF_8));
                channel.writeAndFlush(auth);
            } else {
                DirectRedisConnectionProperties properties = DirectRedisChannelManagers.get(channel);
                properties.setAuthOk();
            }
            if (db != 0) {
                channel.write(getSelectData(db));
            }
        }
        return channel;
    }

    @Override
    public int hashCode() {
        return this.address.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof ServiceMappingNormal) {
            try {
                ServiceMappingNormal other = (ServiceMappingNormal) o;
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
                logger.warnLog(t, "ServiceMappingNormal::equals() Error occur: {}", t.getMessage());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.address.toString();
    }

    private class DbSizePuller implements ResultCallback {

        private void pull() {
            try {
                exchangeRedisData(this, ALL_SIZE_DATA, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void callback(Object o) throws IOException {
            if (o instanceof List) {
                SliceData = (List) o;
            }
        }
    }
}
