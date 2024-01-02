package com.tongtech.proxy.core.utils;

public class ScalableSlotsUtil {
    /**
     * 以下内容用于节点工作在Scalable时数据迁移操作
     */
    public static volatile int TOTAL_BLOCKS = 0;

    public static int[] createBlockByRanges(String ranges) {
        int[] range = new int[TOTAL_BLOCKS];
        if (ranges != null && ranges.length() > 0) {
            String[] rs = ranges.split(",");
            for (String r : rs) {
                int min = -1;
                int max = -1;
                try {
                    String[] reps = r.split("-");
                    min = Integer.parseInt(reps[0].trim());
                    try {
                        max = Integer.parseInt(reps[1].trim());
                    } catch (Throwable t) {
                        max = min;
                    }
                } catch (Throwable t) {
                }
                if (min >= 0 && max >= 0 && min <= max) {
                    for (int i = min; i <= max; ++i) {
                        if (i < TOTAL_BLOCKS) {
                            range[i] = 1;
                        }
                    }
                }
            }
        }
        return range;
    }

    public static boolean isInBlock(byte[] key, int[] blocks) {
        int hash = 0;
        if (key != null && blocks != null && blocks.length > 0) {
            for (int i = 0; i < key.length; ++i) {
                int c = key[i] & 0xff;
                int bit = (i * 7) % 13;
                if (bit > 9) {
                    hash ^= c >> (13 - bit);
                } else {
                    hash ^= (c << bit) & 0xffff;
                }
            }
            return blocks[hash % blocks.length] != 0;
        } else if (blocks == null || blocks.length == 0) {
            // blocks 为空
            return true;
        } else {
            // key 为空
            return false;
        }
    }

    public static int hashKey(byte[] key) {
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
}
