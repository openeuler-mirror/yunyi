package com.tongtech.proxy.core.server.io.redis;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ByteProcessor;
import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProcessCounter;
import com.tongtech.proxy.core.server.ProtocolDecoderException;
import com.tongtech.proxy.core.server.io.CodecContext;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

public class RedisDecode extends ByteToMessageDecoder {
    private static final Log logger = ProxyConfig.getServerLog();

//    private static final ConcurrentHashMap<ChannelHandlerContext, Context> CONTEXTS = new ConcurrentHashMap<>();

//    private int maxLineLength = Integer.MAX_VALUE;

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
                        List list = context.getCommands();
                        out.add(list);
                        logger.infoLog("RedisCodec::decode() Receive {} from '{}'.", list, ctx.channel().remoteAddress());
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

//    private static final Vector<String> split(String src, char c) {
//        int max = Integer.MAX_VALUE;
//
//        Vector<String> spliter = new Vector<>();
//
//        // 输入字符串为空，返回空数组
//        if (src == null) {
//            return spliter;
//        }
//
//        int begin = 0, end = 0;
//        int len = src != null ? src.length() : 0;
//        StringBuilder buf = new StringBuilder(128);
//
//        for (int i = 0; i < max; i++) {
//            // 跳过连续找到非空格的第一个字符
//            begin = end;// 不能加1，否则src的第一个字符可能会被漏掉
//            char seperater = c;
//            while (begin < len) {
//                if (src.charAt(begin) == '"') {
//                    seperater = '"';
//                    begin++;
//                    break;
//                } else if (src.charAt(begin) != seperater) {
//                    break;
//                }
//                begin++;
//            }
//
//            // 找到下1个分隔字符
//            end = begin;
//            boolean shift = false;
//            buf.setLength(0);
//            if (end < len) {
//                while (end < len) {
//                    char b = src.charAt(end);
//                    if (shift) {
//                        // 被转义的字符
//                        buf.append(b);
//                        shift = false;
//                    } else if (b == '\\') {
//                        // 转义符
//                        shift = true;
//                    } else if (b == seperater) {
//                        if (seperater == '"') {
//                            end++;
//                        }
//                        break;
//                    } else {
//                        buf.append(b);
//                    }
//                    end++;
//                }
//                spliter.add(buf.toString());
//            }
//            if (end >= len) {
//                break;
//            }
//        }
//        return spliter;
//    }

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
            parseInit();
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
        private volatile StableList<Object> Commands = null;

        private volatile Vector<Object> Parsed = null;

        private void parseInit() {
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
                if (Commands.size() == 0 && Commands.getParent() == null) {
                    // 第一个字段是命令，用字符串解析
                    Commands.add(data.toString(StandardCharsets.UTF_8).toLowerCase());
                } else {
                    int len = data.readableBytes();
                    byte[] bytes = new byte[len];
                    data.readBytes(bytes);
                    Commands.add(bytes);
                }
                CurLen = -1;
            } else if (CurLen == 0) {
                if (Commands.size() == 0) {
                    Commands.add("");
                } else {
                    Commands.add(new byte[0]);
                }
                CurLen = -1;
            } else {
                CurType = data.readByte();
                if (CurType == CodecContext.HEAD_LIST) {
                    // 以“*”开头的行
                    // 并且不是以”$“开头的行的后续行
                    int items = (int) readLong(data, 0);
                    Commands = new StableList<>(Commands, items);
                    //CurItem = 0;
                    CurLen = -1;
                } else if (CurType == CodecContext.HEAD_STRINGBLOB
                        || CurType == CodecContext.HEAD_BYTES) {
                    if (Commands == null) {
                        Commands = new StableList<>(null, 1);
                    }
                    // 字符串段
                    CurLen = (int) readLong(data, 0);
                    if (CurLen < 0) {
                        Commands.add(null);
//                    } else if (CurLen == 0) {
////                        CurLen = -1;
                    }
                } else if (CurType == CodecContext.HEAD_SIMPLESTRING) {
                    // 简单字符串，去掉类型标识，将剩下部分当作字符串处理
                    if (Commands == null) {
                        Commands = new StableList<>(null, 1);
                    }
                    int len = data.readableBytes();
                    byte[] bytes = new byte[len];
//                    bytes[0] = CurType;
                    data.readBytes(bytes);
                    Commands.add(new String(bytes, StandardCharsets.UTF_8));
                    //CurItem++;
                } else if (CurType == CodecContext.HEAD_ERROR) {
                    // 返回的错误，去掉类型标识，将剩下部分当作错误内容
                    if (Commands == null) {
                        Commands = new StableList<>(null, 1);
                    }
                    int len = data.readableBytes();
                    byte[] bytes = new byte[len];
//                    bytes[0] = CurType;
                    data.readBytes(bytes);
                    Commands.add(new IOException(new String(bytes, StandardCharsets.UTF_8)));
                    // CurItem++;
                } else if (CurType == CodecContext.HEAD_INTEGER) {
                    // 整数字符串，去掉类型标识，将剩下部分转换成整数
                    if (Commands == null) {
                        Commands = new StableList<>(null, 1);
                    }
                    Commands.add(readLong(data, 0));
                    //CurItem++;
                } else if (CurType == CodecContext.HEAD_FLOAT) {
                    // 整数字符串表示的float类型的浮点数
                    if (Commands == null) {
                        Commands = new StableList<>(null, 1);
                    }
                    Commands.add(Float.intBitsToFloat((int) readLong(data, 1)));
                    //CurItem++;
                } else {
                    // 无标识的字符串，当作以空格分隔的命令行处理
                    if (Commands == null) {
                        int len = data.readableBytes();
                        byte[] bytes = new byte[len + 1];
                        bytes[0] = CurType;
                        data.readBytes(bytes, 1, len);
                        Vector<byte[]> cmds = StaticContent.splitBytes(bytes, ' ', 1000);
                        Commands = new StableList<>(null, cmds.size());
                        for (byte[] s : cmds) {
                            if (Commands.size() == 0) {
                                Commands.add(new String(s).toLowerCase());
                            } else {
                                Commands.add(s);
                            }
                        }
                    } else {
                        throw new ProtocolDecoderException("protocol error");
                    }
                    // CurItem++;
                }
            }


            // 已经解析完了一个完整命令
            while (Commands.isParseOK()) {
                // 当前级的列表已经解析完
                StableList parent = Commands.getParent();
                if (parent != null) {
                    // 如果parnet不为空，说明当前解析的是个子表
                    parent.add(Commands);
                    Commands = parent;
                } else {
                    break;
                }
            }

            if (Commands.isParseOK()) {
                // 已经全部解析完成，恢复初始值
                parseInit();
                return true;
            }

            return false;
        }

        public Vector<Object> getCommands() {
            return Parsed;
        }


        private class StableList<T> extends Vector<T> {
            private final StableList Parent;
            private final int Capacity;

            public StableList(StableList parent, int capacity) {
                super(capacity);
                Parent = parent;
                Capacity = capacity;
            }

            /**
             * 判断当前list是否已经解析完成。
             *
             * @return true：解析完成
             */
            public boolean isParseOK() {
                return Capacity == size();
            }

            /**
             * 返回当前List的上一级list对象
             *
             * @return
             */
            public StableList<T> getParent() {
                return Parent;
            }

            @Override
            public boolean add(T o) {
                if (Capacity < size() + 1) {
                    throw new IndexOutOfBoundsException("too more members");
                }
                return super.add(o);
            }

            @Override
            public void add(int index, T o) {
                if (Capacity < size() + 1) {
                    throw new IndexOutOfBoundsException("too more members");
                }
                super.add(index, o);
            }
        }
    }
}