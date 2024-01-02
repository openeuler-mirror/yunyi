package com.tongtech.proxy.core.cli;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class BulkString {
    private final byte[] data;
    private volatile boolean needEscape = true;

    public BulkString(byte[] data) {
        this.data = data;
    }

//    public static final byte[] accept(byte[] str, int start, int end) {
//        String
//        if (escaped == null) {
//            return null;
//        } else if (start < 0
//                || start > escaped.length()
//                || end < start
//                || end > escaped.length()) {
//            throw new ArrayIndexOutOfBoundsException("in accept: len of data "
//                    + escaped.length() + ", but start is " + start + " and end is " + end);
//        } else if (start == end) {
//            return NULL_BYTES;
//        }
//
//        int idx = escaped.indexOf('\\', start);
//        if (idx < 0 || idx >= end) {
//            return escaped.substring(start, end).getBytes(StandardCharsets.UTF_8);
//        }
//
//        ByteArrayOutputStream bos = new ByteArrayOutputStream(escaped.length());
////        int len = escaped.length();
//        for (int i = start; i < end; ++i) {
//            int ch = escaped.charAt(i);
//            if (ch == '\\') {
//                // char 1
//                int ch1 = escaped.charAt(++i);
//                if (ch1 == 'r') {
//                    ch = '\r';
//                } else if (ch1 == 'n') {
//                    ch = '\n';
//                } else if (ch1 == 'b') {
//                    ch = '\b';
//                } else if (ch1 == 't') {
//                    ch = '\t';
//                } else if (ch1 == 'f') {
//                    ch = '\f';
//                } else if (ch1 == 'x') { // \\u
//                    ch = 0;
//                    for (int j = 0; j < 2; j++) {
//                        ch1 = escaped.charAt(++i);
//                        if (ch1 > '9') {
//                            ch1 |= 0x20;/* Force lowercase */
//                        }
//                        if (ch1 >= 'a' && ch1 <= 'z') {
//                            ch1 = ch1 - 'a' + 10;
//                        } else if (ch1 >= '0' && ch1 <= '9') {
//                            ch1 = ch1 - '0';
//                        } else {
//                            throw new ProtocolErrorException("error char at " + i + " in '" + escaped + "'");
//                        }
//                        ch = (ch << 4) + ch1;
//                    }
//                } else {
//                    ch = ch1;
//                }
//                bos.write(ch);
//            } else {
//                if (ch <= 0x7F) {
//                    bos.write(ch);
//                } else if (ch <= 0x7FF) {
//                    /* 110xxxxx 10xxxxxx */
//                    bos.write((ch >> 6) | 0xC0);
//                    bos.write((ch & 0x3F) | 0x80);
//                } else/* if (ch <= 0xFFFF)*/ {
//                    /* 1110xxxx 10xxxxxx 10xxxxxx */
//                    bos.write((ch >> 12) | 0xE0);
//                    bos.write(((ch >> 6) & 0x3F) | 0x80);
//                    bos.write((ch & 0x3F) | 0x80);
////                } else if (ch <= 0x1FFFFF) {
////                    /* 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx */
////                    bos.write((ch >> 18) | 0xF0);
////                    bos.write(((ch >> 12) & 0x3F) | 0x80);
////                    bos.write(((ch >> 6) & 0x3F) | 0x80);
////                    bos.write((ch & 0x3F) | 0x80);
//                }
//            }
//        }
//        return bos.toByteArray();
//    }

    private static final byte[][] char2escape = new byte[256][];
    private static final byte[] NULL_BYTES = new byte[0];

    static {
        for (int i = 0; i < char2escape.length; ++i) {
            if (i == '\b') {
                char2escape[i] = "\\b".getBytes();
            } else if (i == '\t') {
                char2escape[i] = "\\t".getBytes();
            } else if (i == '\n') {
                char2escape[i] = "\\n".getBytes();
            } else if (i == '\f') {
                char2escape[i] = "\\f".getBytes();
            } else if (i == '\r') {
                char2escape[i] = "\\r".getBytes();
            } else if (i == '/') {
                char2escape[i] = "\\/".getBytes();
            } else if (i == '\\') {
                char2escape[i] = "\\\\".getBytes();
            } else if (i == ' ') {
                // 此项针对rds自有协议的返回值不能有空格
                char2escape[i] = "\\ ".getBytes();
            } else if (i <= 0x1f || i >= 0x7f) {
                char2escape[i] = String.format("\\x%02x", i).getBytes();
            } else {
                char2escape[i] = null;
            }
        }
    }

    private static final int checkUtf8CodeLength(byte[] data, int offset) {
        if (data == null || data.length == 0 || offset >= data.length) {
            return -1;
        }
        int ch = data[offset] & 0xff;
        if (ch <= 0x7f) {
            return -1;
        } else if (ch < 0xe0) {
            if (offset + 1 < data.length) {
                int ch1 = data[offset + 1] & 0xff;
                if (((ch & 0xe0) == 0xc0)
                        && ((ch1 & 0xc0) == 0x80)
                ) {
                    ch = ((ch & 0x1f) << 6) | (ch1 & 0x3f);
                    if (ch > 0x7f && ch <= 0x7ff /* 其实此处必然小于 7FF */) {
                        return 1;
                    }
                }
            }
        } else if (ch < 0xf0) {
            if (offset + 2 < data.length) {
                int ch1 = data[offset + 1] & 0xff;
                int ch2 = data[offset + 2] & 0xff;
                if ((ch & 0xf0) == 0xe0
                        && ((ch1 & 0xc0) == 0x80)
                        && ((ch2 & 0xc0) == 0x80)
                ) {
                    ch = ((ch & 0x0f) << 12) | ((ch1 & 0x3f) << 6) | (ch2 & 0x3f);
                    if (ch > 0x7ff && ch <= 0xFFFF && !Character.isSurrogate((char) ch)) {
                        return 2;
                    }
                }
            }
        }
        return -1;
    }

    public static final String escape(byte[] data, int offset, int len) {
        if (data == null) {
            return null;
        } else if (offset < 0
                || offset > data.length
                || len < 0
                || offset + len > data.length) {
            throw new ArrayIndexOutOfBoundsException("in escape: len of data "
                    + data.length + ", but offset is " + offset + " and len is " + len);
        } else if (len == 0) {
            return "";
        }

        // 判断是否需要转换
        boolean need = false;
        for (int i = 0; i < data.length; ++i) {
            int ch = data[i] & 0xff;
            if (char2escape[ch] != null) {
                need = true;
                break;
            }
        }
        if (!need) {
            return new String(data, offset, len, StandardCharsets.UTF_8);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 2);
        int end = offset + len;
        for (int i = offset; i < end; i++) {
            int ch = data[i] & 0xff;
            if (char2escape[ch] == null) {
                bos.write(ch);
            } else {
                // 以下代码将全部可疑编码进行转换，好处是没有漏网之鱼，问题是能保持正确的utf8编码
//                bos.write(char2escape[ch], 0, char2escape[ch].length);
                // 以下代码只转换utf8不支持的编码，
                int num = checkUtf8CodeLength(data, i);
                if (num < 0) {
                    bos.write(char2escape[ch], 0, char2escape[ch].length);
                } else {
                    bos.write(data, i, num + 1);
                    i += num;
                }
            }
        }
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    public String getString() {
        if (data == null) {
            return null;
        } else if (data.length == 0) {
            return "";
        } else {
            return new String(this.data, StandardCharsets.UTF_8);
        }
    }

    public void setEscape(boolean escape) {
        this.needEscape = escape;
    }

    @Override
    public String toString() {
        if (this.data == null) {
            return "(nil)";
        } else if (this.data.length == 0) {
            return "\"\"";
        }
        StringBuilder buf = new StringBuilder();
        if (needEscape) {
            buf.append('"');
            buf.append(escape(this.data, 0, this.data.length));
            buf.append('"');
        } else {
            buf.append(new String(this.data, StandardCharsets.UTF_8));
        }
        return buf.toString();
    }
}
