package com.tongtech.proxy.core.slices;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;
import com.tongtech.proxy.core.slices.codec.redis.NodeRedisDecode;
import com.tongtech.proxy.core.slices.codec.redis.NodeRedisEncode;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import static com.tongtech.proxy.core.slices.RedisConnectionFactory.RedisChannelManagers;
import static com.tongtech.proxy.core.slices.RedisConnectionFactory.LOOP_GROUP_REDIS;

public class RedisSslConnectionFactory {
    private static final Log Logger = ProxyConfig.getServerLog();

    private static final Bootstrap bootstrap_redis = new Bootstrap();

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
                        pipeline.addFirst(new SslHandler(BogusSSLContextFactory.createClientSSLEngine()));
//                        pipeline.addLast(new IdleStateHandler(0, 2, 0));
                        pipeline.addLast(new NodeRedisEncode(), new NodeRedisDecode());
                        pipeline.addLast(new RedisConnectionAdaptor());
                    }
                });
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
                        Logger.debugLog("RedisSslConnectionFactory::getConnection() channel ok at {}", channel.localAddress());
                        return channel;
                    } else {
                        Logger.infoLog("RedisSslConnectionFactory::getConnection() null property at {}", channel.localAddress());
                    }
                }
            }
        } catch (Throwable t) {
            Logger.warnLog("RedisSslConnectionFactory::getConnection() Get connection from {}:{} failed: {}"
                    , manager.getHost(), manager.getRedisPort(), t);
        }

        return null;
    }
}
