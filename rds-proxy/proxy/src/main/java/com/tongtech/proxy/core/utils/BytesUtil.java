package com.tongtech.proxy.core.utils;

public class BytesUtil {

    public static boolean startsWith(byte[] msg, byte[] pre) {
        // 如果前缀数组为null，则直接返回false，因为null无法作为任何数组的前缀
        if (pre == null) {
            return false;
        }
        // 如果消息数组为null，但前缀数组不为null，则它们不可能相等，返回false
        if (msg == null) {
            return false;
        }
        // 如果消息数组的长度小于前缀数组的长度，则它们不可能以相同的前缀开始
        if (msg.length < pre.length) {
            return false;
        }
        // 逐字节比较前缀
        for (int i = 0; i < pre.length; i++) {
            if (msg[i] != pre[i]) {
                return false;
            }
        }
        // 如果所有字节都匹配，则返回true
        return true;
    }
}
