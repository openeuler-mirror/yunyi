package com.tongtech.proxy.core.slices;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DirectRedisConnectionAdaptor extends ChannelInboundHandlerAdapter {
    private static final Log Logger = ProxyConfig.getServerLog();

    public static final ConcurrentHashMap<Channel, DirectRedisConnectionProperties> DirectRedisChannelManagers = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // TODO Auto-generated method stub
        // System.out.println("Connector::sessionOpened()");
        Channel channel = ctx.channel();
        synchronized (channel) {
            DirectRedisConnectionProperties properties = new DirectRedisConnectionProperties(channel);
            DirectRedisChannelManagers.put(channel, properties);
            channel.notify();
        }
        Logger.debugLog("DirectRedisConnectionAdaptor::channelActive() Open {}", channel.localAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        // TODO Auto-generated method stub
        // 按空格分解命令

        // System.out.println("Connector::messageReceived() Receive " + cmd);

        if (msg == null) {
            Logger.infoLog("DirectRedisConnectionAdaptor::channelRead() Null message received from {}", ctx.channel().remoteAddress());
            return;
        }

        Channel channel = ctx.channel();
//        synchronized (channel) {
        DirectRedisConnectionProperties properties = DirectRedisChannelManagers.get(channel);
        if (properties != null) {
            if (!properties.isAuthOk()) {
                if (msg instanceof Boolean && (Boolean) msg) {
                    properties.setAuthOk();
                } else {
                    ctx.close();
                    Logger.warnLog("DirectRedisConnectionAdaptor::channelRead() Authed failed from {}: {}", ctx.channel().remoteAddress(), msg);
                }
            } else {
                ResultCallback callback = properties.getCallback();
                if (callback != null) {
                    callback.callback(msg);
                    properties.setCallback(null);

                    if (msg instanceof Exception) {
                        Logger.infoLog("DirectRedisConnectionAdaptor::channelRead() request = {}, response = {}"
                                , properties.getData(), msg);
                    } else {
                        Logger.debugLog("DirectRedisConnectionAdaptor::channelRead() request = {}, response = {}"
                                , properties.getData(), msg);
                    }
                } else {
                    Logger.warnLog("DirectRedisConnectionAdaptor::channelRead() request = {}, response = {}, but callback is null."
                            , properties.getData(), msg);
                }
            }
        } else {
            ctx.close();
            Logger.warnLog("DirectRedisConnectionAdaptor::channelRead() ConnectionProperties for {} is null", ctx);
        }
//        }
    }

    //    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//        if (evt instanceof IdleStateEvent) {
//            IdleStateEvent event = (IdleStateEvent) evt;
//            if (event.state() == IdleState.WRITER_IDLE) {
//                ctx.writeAndFlush("idle");
//            }
//        }
//        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
            throws Exception {
        // TODO Auto-generated method stub
        Channel channel = ctx.channel();
        ResultCallback callback;
        synchronized (channel) {
            DirectRedisConnectionProperties properties = DirectRedisChannelManagers.get(channel);
            callback = properties.getCallback();
            properties.setCallback(null);
        }
        if (callback != null) {
            callback.callback(new IOException("ERR " + e.getMessage()));
        }

        ctx.close();

        Logger.warnLog("DirectRedisConnectionAdaptor::exceptionCaught() General exception occur at {}: {}"
                , ctx.channel(), e);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // TODO Auto-generated method stub
        // System.out.println("Connector::sessionClosed()");

        Channel channel = ctx.channel();
        ResultCallback callback;
        synchronized (channel) {
            DirectRedisConnectionProperties properties = DirectRedisChannelManagers.remove(channel);
            callback = properties.getCallback();
            properties.setCallback(null);
            channel.notify();
        }

        if (callback != null) {
            String remote = "unknown";
            try {
                remote = ctx.channel().remoteAddress().toString();
            } catch (Throwable t) {
            }
            try {
//                callback.callback(null);
                callback.callback(new IOException("ERR Connection to " + remote + " is closed"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Logger.debugLog("DirectRedisConnectionAdaptor::channelInactive() Close {}", ctx.channel().localAddress());
    }
}
