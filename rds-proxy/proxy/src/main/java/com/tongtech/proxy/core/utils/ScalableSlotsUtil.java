package com.tongtech.proxy.core.utils;

public class ScalableSlotsUtil {
    /**
     * 以下内容用于节点工作在Scalable时数据迁移操作
     */
    public static volatile int TOTAL_BLOCKS = 0;

    /**
     * 旧版本使用的hash算法
     *
     * @param key
     * @return
     */
    public static int old_hashKey(byte[] key) {
        // 未成功连接Center时，该值为“0”
        if (TOTAL_BLOCKS <= 0) {
            throw new IllegalStateException("ERR unauthorized from center");
        }

        int hash = 0;
        for (int i = 0; i < key.length; ++i) {
            int c = key[i] & 0xff;
            int bit = (i * 7) % 13;
            if (bit > 9) {
                hash ^= c >> (13 - bit);
            } else {
                hash ^= (c << bit) & 0xffff;
            }
        }
        return hash % TOTAL_BLOCKS;
    }

    /**
     * TongRDS 2.2.1.5和TongRDS-CN 2.2.C.2以后使用的新算法
     *
     * @param key
     * @return
     */
    public static int hashKey(byte[] key) {
        // 未成功连接Center时，该值为“0”
        if (TOTAL_BLOCKS <= 0) {
            throw new IllegalStateException("ERR unauthorized from center");
        }
        return RedisSlotCRC.getSlot(key) % TOTAL_BLOCKS;
    }

}