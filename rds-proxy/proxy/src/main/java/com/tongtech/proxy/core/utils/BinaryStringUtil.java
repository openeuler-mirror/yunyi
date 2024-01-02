package com.tongtech.proxy.core.utils;

import java.nio.charset.Charset;

import static com.tongtech.proxy.core.StaticContent.accept;
import static com.tongtech.proxy.core.StaticContent.escape;

public class BinaryStringUtil {
    private final static Charset charset = ProxyConfig.getAppCharset();

    /**
     * 将base32编码的字符串转换为常规字符串
     *
     * @param str
     * @return
     */
//    private static String decoder(String str) {
//        byte[] data = Base64String2Bytes(str);
//        return new String(data, BinaryStringUtil.charset);
//    }

    /**
     * 将常规字符串转换成base32编码的字符串
     *
     * @param str
     * @return
     */
//    private static String encoder(String str) {
//        byte[] data = str.getBytes(BinaryStringUtil.charset);
//        return Base64Bytes2String(data, 0, data.length);
//    }


    /**
     * 根据配置，将EBCEID码的字符串转换为long类型数
     *
     * @param str
     * @return
     */
    public static long parseLong(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        } else {
//            if (isBinaryCompatible) {
//                str = decoder(str);
//            }
            return Long.parseLong(str);
        }
    }

    /**
     * 根据配置，将EBCEID码的字符串转换为double类型数
     *
     * @param str
     * @return
     */
    public static double parseDouble(String str) {
        if (str == null) {
            return Double.NaN;
        } else if (str.length() == 0) {
            return 0d;
        } else {
            if (str.equalsIgnoreCase("INF") || str.equalsIgnoreCase("+INF")) {
                return Double.MAX_VALUE;
            } else if (str.equalsIgnoreCase("-INF")) {
                return -Double.MAX_VALUE;
            } else {
                return Double.parseDouble(str);
            }
        }
    }

    /**
     * 根据配置，将long类型数转换为EBCEID码的字符串
     *
     * @param l
     * @return
     */
    public static String toString(long l) {
        return Long.toString(l);
//        String str = Long.toString(l);
//        if (isBinaryCompatible) {
//            str = encoder(str);
//        }
//        return str;
    }

    /**
     * 根据配置，将double类型数转换为EBCEID码的字符串
     *
     * @param d
     * @return
     */
    public static String toString(double d) {

        String str;
        if (d == Double.MAX_VALUE) {
            return "inf";
        } else if (d == -Double.MAX_VALUE) {
            return "-inf";
        } else if ((long) d == d) {
            str = Long.toString((long) d);
        } else {
            str = Double.toString(d);
        }
//        if (isBinaryCompatible) {
//            str = encoder(str);
//        }
        return str;
    }

    /**
     * 根据配置情况，将UTF-8编码的普通字符转换为EBCEID码
     *
     * @param org
     * @return
     */
//    public static String encode(String org) {
//        String ret = org;
//        if (isBinaryCompatible) {
//            ret = encoder(org);
//        }
//        return ret;
//    }

    /**
     * 根据配置情况，将EBCDIC码转换为UTF-8编码的普通字符串
     *
     * @param org
     * @return
     */
//    public static String decode(String org) {
//        String ret = org;
//        if (isBinaryCompatible) {
//            ret = decoder(org);
//        }
//        return ret;
//    }
//
//    public static String decodeKey(String key) {
//        String ret = key;
//        if (isBinaryCompatibleKey) {
//            ret = decoder(key);
//        }
//        return ret;
//    }

    /**
     * 判断字节数组src是否是pre内容开头的数组
     *
     * @param src
     * @param pre
     * @return
     */
    public static boolean startWith(byte[] src, byte[] pre) {
        if (pre == null || pre.length == 0) {
            return true;
        }
        if (src == null || src.length < pre.length) {
            return false;
        }

        for (int i = 0; i < pre.length; ++i) {
            if (src[i] != pre[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将字符串转换回原始的字节数组，转换规则根据传入参数的needBinaryCompatible和当前是否为2进制兼容模式决定
     * 如果 needBinaryCompatible=true 并且是2进制兼容模式则按照Base64编码进行转换，
     * 否则按普通字符串的UTF-8格式转换为字节数组
     *
     * @param value //     * @param needBinaryCompatible
     * @return
     */
    public static byte[] getBytes(String value/*, boolean needBinaryCompatible*/) {
        byte[] b = null;
        if (value != null) {
            b = accept(value, 0, value.length());
        }
        return b;
    }

    /**
     * 将字符串形式的key转换为字节数组，如果key是2进制兼容模式则将key当作Base64编码处理
     *
     * @param key
     * @return
     */
    public static byte[] getKeyBytes(String key) {
        byte[] b = null;
        if (key != null) {
            b = accept(key, 0, key.length());
        }
        return b;
    }

    /**
     * 将字节数组转换为字符串，转换规则根据传入参数的needBinaryCompatible和当前是否为2进制兼容模式决定
     * 如果 needBinaryCompatible=true 并且是2进制兼容模式则转换为Base64编码，否则按UTF-8格式转为字符串
     *
     * @param data
     * @param needBinaryCompatible
     * @return
     */
    public static String getString(byte[] data, boolean needBinaryCompatible) {
        return getString(data, 0, data.length, needBinaryCompatible);
    }

    public static String getString(byte[] data, int offset, int len, boolean needBinaryCompatible) {
        String msg = null;
        if (data != null) {
            msg = escape(data, offset, len);
        }
        return msg;
    }

    /**
     * 与Redis一样的字符串匹配判断
     *
     * @param str    被匹配的字符串，格式为符合isBinaryCompatible编码要求的字符串
     * @param match  匹配字符串支持部分正则表达式，正常的java字符串格式
     * @param noCase 是否区分大小写，true为不区分大小写
     * @return 匹配成功返回true
     */
    public static boolean stringMatchBytes(String str, byte[] match, boolean noCase) {
        byte[] sdata = getBytes(str);
        return StringMatcher.stringmatchlen(match, 0, sdata, 0, noCase);
    }

//    public static boolean stringMatchString1(String str, String match, boolean noCase) {
//        byte[] sdata = getBytes(str, true);
////        byte[] mdata = match.getBytes(StandardCharsets.UTF_8);
//        byte[] mdata = getBytes(match, true);
//
//        return StringMatcher.stringmatchlen(mdata, 0, sdata, 0, noCase);
//    }
//
//    public static boolean bytesMatchString1(byte[] sdata, String match, boolean noCase) {
////        byte[] mdata = match.getBytes(StandardCharsets.UTF_8);
//        byte[] mdata = getBytes(match, true);
//
//        return StringMatcher.stringmatchlen(mdata, 0, sdata, 0, noCase);
//    }

    /**
     * @param org   原始字符串
     * @param begin 截取字符串的开始位置（包含）小于零代表距离结尾的长度，如-1表示最后1个字符，-2表示倒数第二个字符，and so on
     * @param end   截取字符串的结束位置（包含），小于零代表距离结尾的长度，如-1表示最后1个字符，-2表示倒数第二个字符
     * @return
     */
    public static String substr(String org, int begin, int end) {
        if (org == null || org.length() == 0) {
            return "";
        }

        byte[] data = getBytes(org);
//        if (isBinaryCompatible) {
//            // 2进制兼容模式
//            data = Base64String2Bytes(org);
//        } else {
//            data = org.getBytes(StandardCharsets.UTF_8);
//        }

        int len = data.length;
        if (begin < 0) {
            begin += len;
        }
        if (begin < 0) {
            begin = 0;
        }
        if (end < 0) {
            end += len;
        }
        if (end >= len) {
            end = len - 1;
        }
        if (begin < len && begin <= end) {
            len = end - begin + 1;
//            byte[] ret = new byte[len];
//            System.arraycopy(data, begin, ret, 0, len);
//            if (isBinaryCompatible) {
//                return Base64Bytes2String(ret, 0, ret.length);
//            } else {
//                return new String(ret, BinaryStringUtil.charset);
//            }
            return getString(data, begin, len, true);
        }
        return "";
    }

    public static String append(String head, String tail) {
//        if (isBinaryCompatible) {
//            try {
//                if (head == null || head.length() < 4) {
//                    return tail;
//                } else if (tail == null || tail.length() < 4) {
//                    return head;
//                }
//                byte[] h = Base64String2Bytes(head);
//                byte[] t = Base64String2Bytes(tail);
//                byte[] r = new byte[h.length + t.length];
//                System.arraycopy(h, 0, r, 0, h.length);
//                System.arraycopy(t, 0, r, h.length, t.length);
//                return Base64Bytes2String(r, 0, r.length);
//            } catch (Exception e) {
//                return head;
//            }
//        } else {
        if (head == null || head.length() == 0) {
            return tail;
        } else if (tail == null || tail.length() == 0) {
            return head;
        } else {
            return head + tail;
        }
//        }
    }
}
