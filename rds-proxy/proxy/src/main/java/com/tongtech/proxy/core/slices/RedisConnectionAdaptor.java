package com.tongtech.proxy.core.slices;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;

import static com.tongtech.proxy.core.slices.RedisConnectionFactory.RedisChannelManagers;

public class RedisConnectionAdaptor extends ChannelInboundHandlerAdapter {
    private static final Log Logger = ProxyConfig.getServerLog();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // TODO Auto-generated method stub
        // System.out.println("Connector::sessionOpened()");
        Channel channel = ctx.channel();
        synchronized (channel) {
            RedisConnectionProperties properties = new RedisConnectionProperties(channel);
            RedisChannelManagers.put(channel, properties);
            channel.notify();
        }
        Logger.infoLog("RedisConnectionAdaptor::channelActive() Open {}", channel.localAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws IOException {
        // TODO Auto-generated method stub
        // 按空格分解命令

        // System.out.println("Connector::messageReceived() Receive " + cmd);

        if (msg == null) {
            Logger.infoLog("RedisConnectionAdaptor::channelRead() Null message received from {}", ctx.channel().remoteAddress());
            return;
        }

        Channel channel = ctx.channel();
//        synchronized (channel) {
        RedisConnectionProperties properties = RedisChannelManagers.get(channel);
        if (properties != null) {
            if (!properties.isAuthOk()) {
                if (msg instanceof Boolean && (Boolean) msg) {
                    properties.setAuthOk();
                } else {
                    ctx.close();
                    Logger.warnLog("RedisConnectionAdaptor::channelRead() Authed failed from {}: {}", ctx.channel().remoteAddress(), msg);
                }
            } else {
                ResultCallback callback = properties.getCallback();
                if (callback != null) {
                    callback.callback(msg);
                    properties.setCallback(null);
                    properties.getMapping().releaseChannel(ctx.channel());

                    if (msg instanceof Exception) {
                        Logger.infoLog("RedisConnectionAdaptor::channelRead() request = {}, response = {}"
                                , properties.getData(), msg);
                    } else {
                        Logger.debugLog("RedisConnectionAdaptor::channelRead() request = {}, response = {}"
                                , properties.getData(), msg);
                    }
                } else {
                    Logger.warnLog("RedisConnectionAdaptor::channelRead() request = {}, response = {}, but callback is null."
                            , properties.getData(), msg);
                }
            }
        } else {
            ctx.close();
            Logger.warnLog("RedisConnectionAdaptor::channelRead() ConnectionProperties for {} is null", ctx);
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
            RedisConnectionProperties properties = RedisChannelManagers.get(channel);
            callback = properties.getCallback();
            properties.setCallback(null);
        }

        if (callback != null) {
            callback.callback(new IOException("ERR " + e.getMessage()));
        }

        ctx.close();

        Logger.warnLog("SyncConnector::exceptionCaught() General exception occur at {}: {}"
                , ctx.channel(), e);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // TODO Auto-generated method stub
        // System.out.println("Connector::sessionClosed()");
        ResultCallback callback;
        Channel channel = ctx.channel();
        synchronized (channel) {
            RedisConnectionProperties properties = RedisChannelManagers.remove(channel);
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
                callback.callback(new IOException("ERR Connection to " + remote + " is closed"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
