package com.tongtech.proxy.core.slices;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import com.tongtech.proxy.core.slices.codec.redis.NodeRedisDecode;
import com.tongtech.proxy.core.slices.codec.redis.NodeRedisEncode;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.tongtech.proxy.core.StaticContent.SCHEDULED_THREAD_POOL_EXECUTOR;

public class RedisConnectionFactory {

    private static final Log Logger = ProxyConfig.getServerLog();

    // 1 minute
    private static final long CONNECTION_FREE_TIME =  60_000;

    private static final Bootstrap bootstrap_redis = new Bootstrap();
    static final EventLoopGroup LOOP_GROUP_REDIS = new NioEventLoopGroup(ProxyConfig.getSocketProcessThreads());

    public static final ConcurrentHashMap<Channel, RedisConnectionProperties> RedisChannelManagers = new ConcurrentHashMap<>();

    static {
        bootstrap_redis.group(LOOP_GROUP_REDIS)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .option(ChannelOption.SO_TIMEOUT, 1000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 给pipeline管道设置处理器
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 2, 0));
                        pipeline.addLast(new NodeRedisEncode(), new NodeRedisDecode());
                        pipeline.addLast(new RedisConnectionAdaptor());
                    }
                });

        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(RedisConnectionFactory::releaseFreeConnection
                , CONNECTION_FREE_TIME, CONNECTION_FREE_TIME, TimeUnit.MILLISECONDS);
    }

    public static Channel getConnection(ServiceMapping manager) {
        ChannelFuture future = bootstrap_redis.connect(manager.getHost(), manager.getRedisPort());
        try {
            future.sync();
            Channel channel = future.channel();
            if (channel != null) {
//                RedisChannelManagers.put(channel, new RedisConnectionProperties(manager, channel));
//                return channel;
                synchronized (channel) {
                    RedisConnectionProperties properties = RedisChannelManagers.get(channel);
                    if (properties == null) {
                        channel.wait(1000);
                        properties = RedisChannelManagers.get(channel);
                    }
                    if (properties != null) {
                        properties.setMapping(manager);
                        Logger.debugLog("RedisConnectionFactory::getConnection() channel ok at {}", channel.localAddress());
                        return channel;
                    } else {
                        Logger.infoLog("RedisConnectionFactory::getConnection() null property at {}", channel.localAddress());
                    }
                }
            }
        } catch (Throwable t) {
            Logger.warnLog("RedisConnectionFactory::getConnection() Get connection from {}:{} failed: {}"
                    , manager.getHost(), manager.getRedisPort(), t);
        }

        return null;
    }

    /**
     * 定时回收超时不用的到ServiceNode的连接
     */
    private static void releaseFreeConnection() {
        long exptime = System.currentTimeMillis() - CONNECTION_FREE_TIME;
        for (RedisConnectionProperties prop : RedisChannelManagers.values()) {
            boolean close = false;
            synchronized (prop) {
                if (prop.getTimestamp() < exptime) {
                    prop.setClosed(true);
                    close = true;
                }
            }
            if (close) {
                try {
                    Channel channel = prop.getChannel();
                    RedisChannelManagers.remove(channel);
                    prop.channelClose();
                } catch (Throwable t) {
                    Logger.warnLog(t, "ConnectionFactory::releaseFreeConnection() error: {}", prop);
                }
            }
        }
    }
}
