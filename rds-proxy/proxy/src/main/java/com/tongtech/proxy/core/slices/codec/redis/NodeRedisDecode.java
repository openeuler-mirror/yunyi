package com.tongtech.proxy.core.slices.codec.redis;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ByteProcessor;
import com.tongtech.proxy.core.server.ProtocolDecoderException;
import com.tongtech.proxy.core.server.io.CodecContext;
import com.tongtech.proxy.core.utils.ProcessCounter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.tongtech.proxy.core.server.io.CodecContext.NULL_ARRAY_OBJECT;
import static com.tongtech.proxy.core.server.io.CodecContext.NULL_STRING_OBJECT;

public class NodeRedisDecode extends ByteToMessageDecoder {

    private final Context context = new Context();

    // findEndOfLine方法中使用
    private volatile int offset = 0;

    /**
     * Decode in block-io style, rather than nio.
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws ProtocolDecoderException {

        int start = in.readableBytes();
        int readable;
        do {
            ByteBuf data = null;
            readable = in.readableBytes();

            int curlen = context.getCurLen();
            if (curlen < 0) {
                data = readLine(in);
            } else /*if (curlen > 0)*/ {
                data = readBuf(in, curlen);
            }

            if (data != null) {
                try {
                    boolean isOk = context.parse(data);
                    if (isOk) {
                        Object result = context.getCommands();
                        out.add(result);
                    }
                } finally {
                    data.release();
                }
            }
        } while (readable != in.readableBytes());

        start -= readable;
        if (start > 0) {
            ProcessCounter.addNetworkInflow(start);
        }
    }

    private ByteBuf readBuf(ByteBuf in, int len) {
        int totalLength = in.readableBytes();
        if (totalLength <= len) {
            // 没有回车
            return null;
        }

        int offset = in.readerIndex();
        int delimLength = in.getByte(offset + len) == '\r' ? 2 : 1;

        if (totalLength < len + delimLength) {
            // 少1个字节
            return null;
        }

        ByteBuf frame = null;
        try {
            frame = in.readRetainedSlice(len);
            in.skipBytes(delimLength);
        } catch (Throwable t) {
            if (frame != null) {
                try {
                    frame.release();
                } catch (Throwable t2) {
                }
//                frame = null;
            }
            throw new ProtocolDecoderException(t, t.getMessage());
        }

        return frame;
    }

    private ByteBuf readLine(ByteBuf in) {
        int eol = findEndOfLine(in);
        ByteBuf frame = null;
        if (eol >= 0) {
            final int length = eol - in.readerIndex();
            final int delimLength = in.getByte(eol) == '\r' ? 2 : 1;

            try {
                frame = in.readRetainedSlice(length);
                in.skipBytes(delimLength);
            } catch (Throwable t) {
                if (frame != null) {
                    try {
                        frame.release();
                    } catch (Throwable t2) {
                    }
//                    frame = null;
                }
                throw new ProtocolDecoderException(t, t.getMessage());
            }
        }
        return frame;
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

    private long readLong(ByteBuf buf, int skip) {
        int count = skip;
        buf.skipBytes(skip);
        boolean isNeg = false;
        long value = 0;
        while (buf.isReadable()) {
            byte b = buf.readByte();
            if (b == '-') {
                isNeg = !isNeg;
            } else if (b > '9' || b < '0') {
                break;
            } else {
                value = value * 10 + b - '0';
            }
        }
        return (isNeg ? -value : value);
    }

    /**
     * A Context used during the decoding of a lin. It stores the decoder,
     * the temporary buffer containing the decoded line, and other status flags.
     *
     * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
     * @version $Rev$, $Date$
     */
    private class Context {

//        /**
//         * The number of lines found so far
//         */
//        private int matchCount = 0;

        /**
         * Create a new Context object with a default buffer
         */
        private Context() {
            init();
        }

//        public int getMatchCount() {
//            return matchCount;
//        }
//
//        public void setMatchCount(int matchCount) {
//            this.matchCount = matchCount;
//        }

        public int getCurLen() {
            return this.CurLen;
        }

//        public void reset() {
//            matchCount = 0;
//        }

        // 当前解析的命令一共多少字段
        //private int Itmes = 0;
        // 当前正在解析的字段
        //private int CurItem = 0;
        // 如果正在解析的是字符串段，CurLen>0
        private int CurLen = -1;
        private byte CurType;
        // 开始解析新命令
        private boolean IsBeginning;
        // 解析好的一个命令的各字段
        private volatile Object Commands = null;

        private volatile Object Parsed = null;

        private void init() {
//            Itmes = 1;
//            CurItem = 0;
            CurLen = -1;
            IsBeginning = true;
            Parsed = Commands;
            Commands = null;
        }

        /**
         * @param data
         * @return 如果完整命令已经解析完成，返回真
         * @throws ProtocolDecoderException
         */
        public boolean parse(ByteBuf data) throws ProtocolDecoderException {
            if (IsBeginning && !data.isReadable()) {
                return false;
            }

            IsBeginning = false;

            if (CurLen > 0) {
                int len = data.readableBytes();
                byte[] bytes = new byte[len];
                data.readBytes(bytes);
                if (Commands != null) {
                    if (Commands instanceof StableListResult) {
                        ((StableListResult) Commands).add(bytes);
                    } else {
                        throw new RedisProtocolError("expect StableListResult");
                    }
                } else {
                    Commands = bytes;
                }
                CurLen = -1;
            } else if (CurLen == 0) {
                if (Commands != null) {
                    if (Commands instanceof StableListResult) {
                        ((StableListResult) Commands).add(new byte[0]);
                    } else {
                        throw new RedisProtocolError("expect StableListResult");
                    }
                } else {
                    Commands = new byte[0];
                }
                CurLen = -1;
            } else {
                CurType = data.readByte();
                if (CurType == CodecContext.HEAD_LIST) {
                    // 以“*”开头的行
                    // 并且不是以”$“开头的行的后续行
                    int items = (int) readLong(data, 0);
                    if (items < 0) {
                        Commands = NULL_ARRAY_OBJECT;
                    } else {
                        Commands = new StableListResult(Commands, items);
                    }
                    //CurItem = 0;
                    CurLen = -1;
                } else if (CurType == CodecContext.HEAD_STRINGBLOB
                        || CurType == CodecContext.HEAD_BYTES) {
                    // 字符串段
                    CurLen = (int) readLong(data, 0);
                    if (CurLen < 0) {
                        if (Commands != null) {
                            if (Commands instanceof StableListResult) {
                                ((StableListResult) Commands).add(NULL_STRING_OBJECT);
                            } else {
                                throw new RedisProtocolError("expect StableListResult");
                            }
                        } else {
                            Commands = NULL_STRING_OBJECT;
                        }
                    }
                } else if (CurType == CodecContext.HEAD_SIMPLESTRING) {
                    // 简单字符串，去掉类型标识，将剩下部分当作字符串处理
                    int len = data.readableBytes();
                    byte[] bytes = new byte[len];
                    data.readBytes(bytes);

                    Object v;
                    if (len == 2 && bytes[0] == 'O' && bytes[1] == 'K') {
                        // +OK
                        v = Boolean.TRUE;
                    } else {
                        // +Others
                        v = new String(bytes, StandardCharsets.UTF_8);
                    }

                    if (Commands != null) {
                        if (Commands instanceof StableListResult) {
                            ((StableListResult) Commands).add(v);
                        } else {
                            throw new RedisProtocolError("expect StableListResult");
                        }
                    } else {
                        Commands = v;
                    }
                    //CurItem++;
                } else if (CurType == CodecContext.HEAD_ERROR) {
                    // 返回的错误，去掉类型标识，将剩下部分当作错误内容
                    int len = data.readableBytes();
                    byte[] bytes = new byte[len];
                    data.readBytes(bytes);

                    if (Commands != null) {
//                        if (Commands instanceof StableListResult) {
//                            ((StableListResult) Commands).add(new ServiceException(new String(bytes, StandardCharsets.UTF_8)));
//                        } else {
                        throw new RedisProtocolError("expect StableListResult");
//                        }
                    } else {
                        Commands = new ServiceException(new String(bytes, StandardCharsets.UTF_8));
                    }
                } else if (CurType == CodecContext.HEAD_INTEGER) {
                    // 整数字符串，去掉类型标识，将剩下部分转换成整数
                    if (Commands != null) {
                        if (Commands instanceof StableListResult) {
                            ((StableListResult) Commands).add(readLong(data, 0));
                        } else {
                            throw new RedisProtocolError("expect StableListResult");
                        }
                    } else {
                        Commands = readLong(data, 0);
                    }
                }
            }


            // 已经解析完了一个完整命令
            while (Commands != null && (!(Commands instanceof StableListResult) || ((StableListResult) Commands).isParseOK())) {
                // 当前级的列表已经解析完
                Object parent = Commands instanceof StableListResult ? ((StableListResult) Commands).getParent() : null;
                if (parent != null && parent instanceof StableListResult) {
                    // 如果parnet不为空，说明当前解析的是个子表
                    ((StableListResult) parent).add(Commands);
                    Commands = parent;
                } else {
                    break;
                }
            }

            if (Commands != null && (!(Commands instanceof StableListResult) || ((StableListResult) Commands).isParseOK())) {
                // 已经全部解析完成，恢复初始值
                init();
                return true;
            }

            return false;
        }

        public Object getCommands() {
            return this.Parsed;
        }
    }
}