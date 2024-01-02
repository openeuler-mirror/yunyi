package com.tongtech.proxy.core.slices.codec.redis;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import com.tongtech.proxy.core.utils.ProcessCounter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.tongtech.proxy.core.server.io.CodecContext.*;

public class NodeRedisEncode extends MessageToByteEncoder<Object> {

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
            try {
                writeObject(buf, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        start = buf.readableBytes() - start;
        ProcessCounter.addNetworkOutflow(start);
    }

    private void writeObject(ByteBuf buf, Object o) {

        if (o == null) {
            buf.writeBytes(NULLSTRING);
        } else if (o == NULL_ARRAY_OBJECT) {
            buf.writeBytes(NULLARRAY);
        } else if (o instanceof byte[]) {
            byte[] d = (byte[]) o;
            buf.writeByte(HEAD_STRINGBLOB);
            buf.writeBytes(Integer.toString(d.length).getBytes());
            buf.writeBytes(ENDOFLINE);
            if (d.length > 0) {
                buf.writeBytes(d);
            }
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof String) {
            byte[] d = ((String) o).getBytes(StandardCharsets.UTF_8);
            buf.writeByte(HEAD_STRINGBLOB);
            buf.writeBytes(Integer.toString(d.length).getBytes());
            buf.writeBytes(ENDOFLINE);
            if (d.length > 0) {
                buf.writeBytes(d);
            }
            buf.writeBytes(ENDOFLINE);
        } else if (o instanceof List) {
            List v = (List) o;
            // 有此判断jedis才不报错
            // MustNumber时，必须输出个数
            buf.writeByte(HEAD_LIST);
            buf.writeBytes(Integer.toString(v.size()).getBytes());
            buf.writeBytes(ENDOFLINE);
            for (Object b : v) {
                writeObject(buf, b);
            }
        }
    }
}
