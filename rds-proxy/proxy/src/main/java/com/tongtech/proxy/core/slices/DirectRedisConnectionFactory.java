package com.tongtech.proxy.core.slices;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import com.tongtech.proxy.core.slices.codec.redis.NodeRedisDecode;
import com.tongtech.proxy.core.slices.codec.redis.NodeRedisEncode;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import static com.tongtech.proxy.core.slices.DirectRedisConnectionAdaptor.DirectRedisChannelManagers;

public class DirectRedisConnectionFactory {

    private static final Log Logger = ProxyConfig.getServerLog();

    // 1 minute
    private static final long CONNECTION_FREE_TIME = 60 * 1000;

    private static final Bootstrap bootstrap_redis = new Bootstrap();

    static final EventLoopGroup DIRECT_LOOP_GROUP_REDIS = new NioEventLoopGroup(ProxyConfig.getSocketProcessThreads());

    static {
        bootstrap_redis.group(DIRECT_LOOP_GROUP_REDIS)
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
//                        pipeline.addLast(new IdleStateHandler(0, 2, 0));
                        pipeline.addLast(new NodeRedisEncode(), new NodeRedisDecode());
                        pipeline.addLast(new DirectRedisConnectionAdaptor());
                    }
                });
    }

    public static Channel getConnection(ServiceMapping manager, ResultCallback callback) {
        ChannelFuture future = bootstrap_redis.connect(manager.getHost(), manager.getRedisPort());
        Channel channel = null;
        try {
            future.sync();
            channel = future.channel();
            if (channel != null) {
                synchronized (channel) {
                    DirectRedisConnectionProperties properties = DirectRedisChannelManagers.get(channel);
                    if (properties == null) {
                        channel.wait(1000);
                        properties = DirectRedisChannelManagers.get(channel);
                    }
                    if (properties != null) {
                        properties.setMapping(manager);
                        properties.setCallback(callback);
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
}
