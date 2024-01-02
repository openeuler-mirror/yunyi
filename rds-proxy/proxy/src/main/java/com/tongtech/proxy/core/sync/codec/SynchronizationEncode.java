package com.tongtech.proxy.core.sync.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.tongtech.proxy.core.protocol.ProtoUtils;
import com.tongtech.proxy.core.protocol.ProtocolErrorException;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.tongtech.proxy.core.server.io.CodecContext.*;

public class SynchronizationEncode extends MessageToByteEncoder<Object> {
    private static final Log logger = ProxyConfig.getServerLog();

    private final static byte SINGLE_END_LINE = '\n';

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception {
        if (msg instanceof List) {
            // 新的调用方式
            writeObject(buf, msg);
        } else if (msg instanceof byte[]) {
            // 旧的调用会用到此分支
            buf.writeBytes((byte[]) msg);
        } else if (msg instanceof String) {
            // 旧的调用会用String，兼容早期编码方式
            String message = msg == null ? "" : msg.toString();
            boolean lnHaved = message.length() > 0 && (message.charAt(message.length() - 1) == '\n');
            buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
            if (!lnHaved) {
                buf.writeByte('\n');
            }
        } else {
            throw new ProtocolErrorException("unexpected message: " + msg);
        }

        if (logger.isDebug()) {
            int index = buf.readerIndex();
            logger.debugLog("SynchronizationEncode::encode() encode data '{}' to '{}' and send to {}."
                    , msg, buf.toString(StandardCharsets.UTF_8), ctx.channel().remoteAddress());
            buf.readerIndex(index);
        }
    }

    private void writeObject(ByteBuf buf, Object o) {
        if (o == null) {
            buf.writeBytes(NULLSTRING);
        } else if (o == NULL_ARRAY_OBJECT) {
            buf.writeBytes(NULLARRAY);
        } else if (o instanceof List) {
            List v = (List) o;
            buf.writeByte(HEAD_LIST);
            ProtoUtils.writeLongToByteBuf(buf, v.size());
            buf.writeByte(SINGLE_END_LINE);
            for (Object b : v) {
                writeObject(buf, b);
            }
        } else if (o instanceof Long) {
            buf.writeByte(HEAD_INTEGER);
            ProtoUtils.writeLongToByteBuf(buf, ((Long) o).longValue());
            buf.writeByte(SINGLE_END_LINE);
        } else if (o instanceof Integer) {
            buf.writeByte(HEAD_INTEGER);
            ProtoUtils.writeLongToByteBuf(buf, ((Integer) o).longValue());
            buf.writeByte(SINGLE_END_LINE);
        } else {
            byte[] str;
            if (o instanceof byte[]) {
                str = (byte[]) o;
            } else {
                str = o.toString().getBytes(StandardCharsets.UTF_8);
            }
            buf.writeByte(HEAD_STRINGBLOB);
            ProtoUtils.writeLongToByteBuf(buf, str.length);
            buf.writeByte(SINGLE_END_LINE);
            if (str.length > 0) {
                buf.writeBytes(str);
            }
            buf.writeByte(SINGLE_END_LINE);
        }
    }
}
