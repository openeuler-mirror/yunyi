package com.tongtech.proxy.core.server.io.redis;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.tongtech.proxy.core.protocol.ProtoUtils;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProcessCounter;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.tongtech.proxy.core.server.io.CodecContext.*;

public class RedisEncode extends MessageToByteEncoder<Object> {
    private static final Log logger = ProxyConfig.getServerLog();

    public RedisEncode() {
        super(true);
    }

    /**
     * 此方法必须要有 synchronized 同步，因为方法内会多次调用 out.write，线程不安全
     *
     * @param ctx
     * @param msg
     * @param buf
     * @throws Exception
     */
    @Override
    protected synchronized void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception {

        int start = buf.readableBytes();
        // 空包
//        if (msg instanceof Exception) {
//            // 从服务节点返回的是ERR时，此处有可能是Exception
//            buf.writeByte('-');
//            buf.writeBytes(((Exception) msg).getMessage().getBytes(StandardCharsets.UTF_8));
//            buf.writeBytes(ENDOFLINE);
//        } else if (msg instanceof String) {
//            String message = msg.toString();
//            if (message.charAt(0) == HEAD_ERROR
//                    || message.charAt(0) == HEAD_SIMPLESTRING) {
//                buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
//            } else {
//                buf.writeByte(HEAD_SIMPLESTRING);
//                buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
//            }
//            buf.writeBytes(ENDOFLINE);
////        } else if (msg instanceof RawString) {
////            buf.writeBytes(msg.toString().getBytes(StandardCharsets.UTF_8));
////            buf.writeBytes(ENDOFLINE);
//        } else {
//            try {
//                writeObject(buf, msg);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        writeObject(buf, msg);

        start = buf.readableBytes() - start;
        ProcessCounter.addNetworkOutflow(start);

        logger.infoLog("RedisCodec::encode() Send '{}' to '{}'.", msg, ctx.channel().remoteAddress());
    }

    private void writeObject(ByteBuf buf, Object o) {

        if (o == null || o == NULL_STRING_OBJECT) {
            buf.writeBytes(NULLSTRING);
        } else if (o == ZERO_STRING_OBJECT) {
            buf.writeBytes(ZEROSTRING);
        } else if (o == NULL_ARRAY_OBJECT) {
            buf.writeBytes(NULLARRAY);
        } else if (o instanceof byte[]) {
            byte[] data = (byte[]) o;
            if (data == null) {
                buf.writeBytes(NULLSTRING);
            } else if (data.length == 0) {
                buf.writeBytes(ZEROSTRING);
            } else {
                buf.writeByte(HEAD_STRINGBLOB);
                ProtoUtils.writeLongToByteBuf(buf,data.length);
//                buf.writeBytes(Integer.toString(data.length).getBytes());
                buf.writeBytes(ENDOFLINE);
                buf.writeBytes(data);
                buf.writeBytes(ENDOFLINE);
            }
        } else if (o instanceof Integer) {
            buf.writeByte(HEAD_INTEGER);
            ProtoUtils.writeLongToByteBuf(buf, ((Integer) o).longValue());
//            buf.writeBytes(Integer.toString((Integer) o).getBytes());
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof Long) {
            buf.writeByte(HEAD_INTEGER);
            ProtoUtils.writeLongToByteBuf(buf,((Long) o).longValue() );
//            buf.writeBytes(Long.toString((Long) o).getBytes());
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof List) {
            List v = (List) o;
            buf.writeByte(HEAD_LIST);
            ProtoUtils.writeLongToByteBuf(buf, v.size());
//            buf.writeBytes(Integer.toString(v.size()).getBytes());
            buf.writeBytes(ENDOFLINE);
            for (Object b : v) {
                writeObject(buf, b);
            }
        } else if (o instanceof Boolean && (Boolean) o) {
            buf.writeByte('+');
            buf.writeByte('O');
            buf.writeByte('K');
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof Exception) {
            // 从服务节点返回的是ERR时，此处有可能是Exception
            buf.writeByte('-');
            buf.writeBytes(((Exception) o).getMessage().getBytes(StandardCharsets.UTF_8));
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof String) {
            String message = o.toString();
            if (message.length() > 0) {
                if (message.charAt(0) == HEAD_ERROR
                        || message.charAt(0) == HEAD_SIMPLESTRING) {
                    buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
                } else {
                    buf.writeByte(HEAD_SIMPLESTRING);
                    buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
                }
                buf.writeBytes(ENDOFLINE);
            } else {
                buf.writeBytes(ZEROSTRING);
            }

        } else {
            writeString(buf, o.toString());
        }
    }

    private void writeString(ByteBuf buf, String msg) {
        if (msg == null) {
            buf.writeBytes(NULLSTRING);
        } else {
            byte[] data;
            data = msg.getBytes(StandardCharsets.UTF_8);
            buf.writeByte(HEAD_STRINGBLOB);
            buf.writeBytes(Integer.toString(data.length).getBytes());
            buf.writeBytes(ENDOFLINE);
            if (data.length > 0) {
                buf.writeBytes(data);
            }
            buf.writeBytes(ENDOFLINE);
        }
    }
}
