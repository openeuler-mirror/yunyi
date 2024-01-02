package com.tongtech.proxy.core.utils;

public class BytesUtil {
    public static boolean startWith(byte[] msg, byte[] pre) {
        if (msg == null && pre == null) {
            return true;
        }
        if (msg.length < pre.length) {
            return false;
        }
        for (int i = 0; i < pre.length; ++i) {
            if (msg[i] != pre[i]) {
                return false;
            }
        }
        return true;
    }
}
