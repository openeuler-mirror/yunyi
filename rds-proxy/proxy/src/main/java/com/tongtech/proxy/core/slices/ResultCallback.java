package com.tongtech.proxy.core.slices;

import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

public interface ResultCallback {
    /**
     * 当 ServiceNode 有返回值（或连接中断）时，调用此方法
     * @param o 服务节点返回的内容，连接中断时返回 Exception
     * @throws IOException
     */
    void callback(Object o) throws IOException;

    /**
     * 当客户端连接关闭时调用此方法
     */
   default void channelClosed(ChannelHandlerContext ctx){};
}
