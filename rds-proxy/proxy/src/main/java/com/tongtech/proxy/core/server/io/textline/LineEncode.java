package com.tongtech.proxy.core.server.io.textline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class LineEncode extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception {
        String message = msg == null ? "" : msg.toString();
        buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
    }
}
