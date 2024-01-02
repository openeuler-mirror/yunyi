package com.tongtech.proxy.core.protocol.redis;

import io.netty.channel.ChannelHandlerContext;

import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

public class ClientManager {
    private static final ConcurrentHashMap<ChannelHandlerContext, Long> Clients = new ConcurrentHashMap<>();

    public static void active(ChannelHandlerContext ctx) {
        Clients.put(ctx, System.currentTimeMillis());
    }

    public static void deactive(ChannelHandlerContext ctx) {
        Clients.remove(ctx);
    }

    public static Enumeration<ChannelHandlerContext> getClients() {
        return Clients.keys();
    }

    public static Long getStart(ChannelHandlerContext ctx){
        return Clients.get(ctx);
    }
}
