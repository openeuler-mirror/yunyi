package com.tongtech.proxy.core.center.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CenterEncoder extends MessageToByteEncoder {

    final static byte HEAD_LIST = '*';
    final static byte HEAD_STRINGBLOCK = '$';
    final static byte HEAD_INTEGER = ':';
    final static byte HEAD_FLOAT = ',';
    final static byte HEAD_BYTES = '[';
    final static byte HEAD_SIMPLESTRING = '+';
    final static byte HEAD_ERROR = '-';
    final static byte HEAD_NULL = '_';
    final static byte HEAD_BOOLEAN = '#';
    final static byte HEAD_ERRORBLOB = '!';
    final static byte HEAD_VERBATIMSTRING = '=';
    final static byte HEAD_BIGINTEGER = '(';
    final static byte HEAD_MAP = '%';
    final static byte HEAD_SET = '~';
    final static byte HEAD_PROPERTIES = '|';
    final static byte HEAD_PUBSUB = '>';

    private final static byte[] ENDOFLINE = "\r\n".getBytes();

    private final static byte[] NULLARRAY = "*-1\r\n".getBytes();
    private final static byte[] NULLSTRING = "$-1\r\n".getBytes();


    @Override
    protected synchronized void encode(ChannelHandlerContext ctx, Object msg, ByteBuf buf) throws Exception {

        // 空包
        if (msg instanceof String) {
            String message = msg.toString();
            if (message.charAt(0) == HEAD_ERROR
                    || message.charAt(0) == HEAD_SIMPLESTRING) {
                buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
            } else {
                buf.writeByte(HEAD_SIMPLESTRING);
                buf.writeBytes(message.getBytes(StandardCharsets.UTF_8));
            }
            buf.writeBytes(ENDOFLINE);
        } else {
            writeObject(buf, msg);
        }
    }

    private void writeObject(ByteBuf buf, Object o) {
        if (o == null) {
            buf.writeBytes(NULLSTRING);
        } else if (o instanceof List) {
            List v = (List) o;
//            if (v.size() != 1) {
            buf.writeByte(HEAD_LIST);
            buf.writeBytes(Integer.toString(v.size()).getBytes());
            buf.writeBytes(ENDOFLINE);
//            }
            for (Object b : v) {
                writeObject(buf, b);
            }
        } else if (o instanceof Integer) {
            buf.writeByte(HEAD_INTEGER);
            buf.writeBytes(Integer.toString((Integer) o).getBytes());
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof Long) {
            buf.writeByte(HEAD_INTEGER);
            buf.writeBytes(Long.toString((Long) o).getBytes());
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof Float) {
            buf.writeByte(HEAD_FLOAT);
            buf.writeBytes(Integer.toString(Float.floatToIntBits((Float) o)).getBytes());
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof byte[]) {
            byte[] d = (byte[]) o;
            buf.writeByte(HEAD_BYTES);
            buf.writeBytes(Integer.toString(d.length).getBytes());
            buf.writeBytes(ENDOFLINE);
            if (d.length > 0) {
                buf.writeBytes(d);
            }
            buf.writeBytes(ENDOFLINE);
        } else {
            writeString(buf, o.toString());
        }
    }

    private void writeString(ByteBuf buf, String msg) {
        if (msg == null) {
            buf.writeBytes(NULLSTRING);
        } else {
            byte[] data = msg.getBytes(StandardCharsets.UTF_8);
            buf.writeByte(HEAD_STRINGBLOCK);
            buf.writeBytes(Integer.toString(data.length).getBytes());
            buf.writeBytes(ENDOFLINE);
            if (data.length > 0) {
                buf.writeBytes(data);
            }
            buf.writeBytes(ENDOFLINE);
        }
    }
}