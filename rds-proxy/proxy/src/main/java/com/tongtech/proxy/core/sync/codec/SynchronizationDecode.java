package com.tongtech.proxy.core.sync.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ByteProcessor;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class SynchronizationDecode extends ByteToMessageDecoder {

    // findEndOfLine方法中使用
    private volatile int offset = 0;

    /**
     * Decode in block-io style, rather than nio.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
//        Context context = getContext(ctx);

        int readable;
        do {
            readable = in.readableBytes();

            ByteBuf data = readLine(in);

            if (data != null) {
                try {
                    String msg = data.toString(StandardCharsets.UTF_8);
                    out.add(msg);
                } finally {
                    data.release();
                }
            }
        } while (readable != in.readableBytes());
    }

    private ByteBuf readLine(ByteBuf in) {
        int eol = findEndOfLine(in);
        if (eol >= 0) {
            final ByteBuf frame;
            final int length = eol - in.readerIndex();
            final int delimLength = in.getByte(eol) == '\r' ? 2 : 1;

            frame = in.readRetainedSlice(length);
            in.skipBytes(delimLength);

            return frame;
        }
        return null;
    }

    private int findEndOfLine(final ByteBuf buffer) {
        int totalLength = buffer.readableBytes();
        int i = buffer.forEachByte(buffer.readerIndex() + offset, totalLength - offset, ByteProcessor.FIND_LF);
        if (i >= 0) {
            offset = 0;
            if (i > 0 && buffer.getByte(i - 1) == '\r') {
                i--;
            }
        } else {
            offset = totalLength;
        }
        return i;
    }
}