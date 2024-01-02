package com.tongtech.proxy.core.utils;

public class Base16 {
    /**
     * byte数组和String转换
     */
    private static final char[] CODE = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String encode(byte[] b) {
        if (b == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder(b.length * 2);
        for (byte b1 : b) {
            buf.append(CODE[(b1 & 0xf0) >> 4]);
            buf.append(CODE[b1 & 0x0f]);
        }
        return buf.toString();
    }

    public static byte[] decode(String s) {
        if (s == null) {
            return null;
        }
        byte[] buf = new byte[s.length() >> 1];
        for (int i = 0; i < s.length() - 1; i += 2) {
            char c = s.charAt(i);
            int i1 = 0;
            if (c >= '0' && c <= '9') {
                i1 = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                i1 = c - 'a' + 10;
            } else if (c >= 'A' && c <= 'F') {
                i1 = c - 'A' + 10;
            }

            c = s.charAt(i + 1);
            int i2 = 0;
            if (c >= '0' && c <= '9') {
                i2 = c - '0';
            } else if (c >= 'a' && c <= 'f') {
                i2 = c - 'a' + 10;
            } else if (c >= 'A' && c <= 'F') {
                i2 = c - 'A' + 10;
            }
            buf[i >> 1] = (byte) ((i1 << 4) | i2);
        }
        return buf;
    }
}
