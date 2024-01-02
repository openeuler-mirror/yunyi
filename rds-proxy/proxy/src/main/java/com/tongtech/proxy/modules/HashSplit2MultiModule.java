package com.tongtech.proxy.modules;

import com.tongtech.proxy.util.Interaction;
import com.tongtech.proxy.util.LOGLEVEL;
import com.tongtech.proxy.util.PluginModule;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 该插件拦截hash类操作命令，将一个key下面的项分配到多个key下面存储，解决单key对应的数据量过大问题
 */
public class HashSplit2MultiModule implements PluginModule {
    // 缺省分块数
    private final static int DEFAULT_BLOCKS = 32;
    // 缺省的hkeys、hgetall、hvals能返回的最大值，超过此值返回失败
    private final static int DEFAULT_MAXITMES = 1024 * 100;
    // 缺省的block值在hscan返回结果的cursor中保存的起始位置（bit位），packed模式时小于32
    private final static int DEFAULT_BLOCKBITPOSITION = 32;

    private int BLOCKS = DEFAULT_BLOCKS;
    private String separator = "^_^";
    private int maxItems = DEFAULT_MAXITMES;

    // 以下两项只用户hscan命令时返回cursor中的block信息和block内的cursor信息的组合
    // 返回的cursor中block信息的起始位置（比特数）
    private int blockBitPosition = DEFAULT_BLOCKBITPOSITION;
    // 返回cursor中block内的cursor值的掩码
    private long dataMask = (1l << blockBitPosition) - 1;

    @Override
    public void init(Properties properties) {
        String value;
        value = properties.getProperty("suffix");
        if (value != null && value.length() > 0) {
            this.separator = value;
        }

        value = properties.getProperty("blocks");
        if (value != null) {
            try {
                BLOCKS = Integer.parseInt(value);
                if (BLOCKS <= 1) {
                    BLOCKS = 2;
                }
            } catch (Throwable t) {
                BLOCKS = DEFAULT_BLOCKS;
            }
        }
        if ("true".equalsIgnoreCase(properties.getProperty("packedblocknumber"))) {
            // 此配置下理论上最多能存储的项是2的32次方减一。
            // 但采用此配置时hscan返回cursor值的范围的兼容性更好
            // 该配置只影响hscan命令
            int bit = 0;
            while ((1 << bit) < BLOCKS) {
                bit++;
            }
            blockBitPosition = 32 - bit;
            dataMask = (1l << blockBitPosition) - 1;
        }

        value = properties.getProperty("maxItems");
        if (value != null) {
            try {
                maxItems = Integer.parseInt(value);
            } catch (Throwable t) {
                maxItems = DEFAULT_MAXITMES;
            }
        }
    }

    @Override
    public String[] getCommands() {
        return new String[]{"hset", "hget", "hdel", "hgetall", "hexists", "hincrby", "hincrbyfloat", "hkeys", "hlen"
                , "hmget", "hmset", "hsetnx", "hvals", "hscan", "del", "type"};
    }

    @Override
    public Object process(List in, Interaction interaction, int db) {
        Object ret = null;
        String cmd = (String) in.get(0);
        interaction.log(LOGLEVEL.DEBUG, "receive '" + cmd + "'");
        if ("type".equals(cmd)) {
            return getType((byte[]) in.get(1), interaction, db);
        } else if ("del".equals(cmd)) {
            int count = 0;
            for (int i = 1; i < in.size(); ++i) {
                byte[] key = (byte[]) in.get(i);
                if ("hash".equalsIgnoreCase(getType(key, interaction, db))) {
                    del(key, interaction, db);
                    in.remove(i);
                    count++;
                }
            }
            if (in.size() > 1) {
                Long l = (Long) interaction.call(in, db);
                count += l.intValue();
            }
            return new Long(count);
        } else if ("hset".equals(cmd) || "hsetnx".equals(cmd)
                || "hincrby".equals(cmd) || "hincrbyfloat".equals(cmd)) {
            // 三目命令
            ArrayList req = new ArrayList();
            req.add(cmd);
            req.add(getNewKey((byte[]) in.get(1), (byte[]) in.get(2)));
            req.add(in.get(2));
            req.add(in.get(3));
            ret = interaction.call(req, db);
        } else if ("hget".equals(cmd) || "hexists".equals(cmd)) {
            // 双目命令
            ArrayList req = new ArrayList();
            req.add(cmd);
            req.add(getNewKey((byte[]) in.get(1), (byte[]) in.get(2)));
            req.add(in.get(2));
            ret = interaction.call(req, db);
        } else if ("hkeys".equals(cmd) || "hvals".equals(cmd) || "hgetall".equals(cmd)) {
            if (maxItems > 0) {
                int len = approximateLen(interaction, (byte[]) in.get(1), db);
                if (len > maxItems) {
                    return new IllegalAccessException("Err too many fields were found");
                }
            }

            // 单目命令
            ArrayList[] reqs = new ArrayList[BLOCKS];
            for (int i = 0; i < BLOCKS; ++i) {
                // 创建key的数组
                reqs[i] = new ArrayList();
                reqs[i].add(cmd);
                reqs[i].add(getNewKey((byte[]) in.get(1), i));
            }
            for (int i = 0; i < BLOCKS; ++i) {
                Object o = interaction.call(reqs[i], db);
                if (o instanceof List) {
                    if (ret == null) {
                        ret = new ArrayList<>();
                    }
                    List list = (List) o;
                    if (list.size() > 0) {
                        ((List) ret).addAll(list);
                    }
                } else if (o instanceof Map) {
                    // for RSP3
                    if (ret == null) {
                        ret = new HashMap<>();
                    }
                    Map map = (Map) o;
                    if (map.size() > 0) {
                        ((Map) ret).putAll(map);
                    }
                }
            }
        } else if ("hlen".equals(cmd)) {
            // 单目命令
            long len = 0;
            ArrayList req = new ArrayList();
            req.add(cmd);
            req.add("");
            for (int i = 0; i < BLOCKS; ++i) {
                req.set(1, getNewKey((byte[]) in.get(1), i));
                Object o = interaction.call(req, db);
                if (o instanceof Long) {
                    len += (Long) o;
                } else if (o instanceof Exception) {
                    return o;
                }
            }
            ret = new Long(len);
        } else if ("hdel".equals(cmd)) {
            ArrayList[] reqs = new ArrayList[BLOCKS];
            for (int i = 0; i < BLOCKS; ++i) {
                reqs[i] = new ArrayList();
                reqs[i].add(cmd);
                reqs[i].add(getNewKey((byte[]) in.get(1), i));
            }
            for (int i = 2; i < in.size(); ++i) {
                reqs[getHash((byte[]) in.get(i), BLOCKS)].add(in.get(i));
            }
            long deled = 0;
            for (int i = 0; i < BLOCKS; ++i) {
                if (reqs[i].size() > 2) {
                    try {
                        deled += (Long) interaction.call(reqs[i], db);
                    } catch (Throwable t) {
                    }
                }
            }
            ret = new Long(deled);
        } else if ("hmset".equals(cmd)) {
            ArrayList[] reqs = new ArrayList[BLOCKS];
            for (int i = 0; i < BLOCKS; ++i) {
                // 创建key的数组
                reqs[i] = new ArrayList();
                reqs[i].add(cmd);
                reqs[i].add(getNewKey((byte[]) in.get(1), i));
            }
            for (int i = 3; i < in.size(); ++i) {
                int block = getHash((byte[]) in.get(i - 1), BLOCKS);
                reqs[block].add(in.get(i - 1));
                reqs[block].add(in.get(i++));
            }
            for (int i = 0; i < BLOCKS; ++i) {
                if (reqs[i].size() > 2) {
                    Object o = interaction.call(reqs[i], db);
                    if (o instanceof Exception) {
                        return o;
                    }
                }
            }
            ret = new Boolean(true);
        } else if ("hmget".equals(cmd)) {
            ArrayList resp = new ArrayList<>();
            ArrayList req = new ArrayList();
            byte[] key = (byte[]) in.get(1);
            for (int i = 2; i < in.size(); ++i) {
                byte[] field = (byte[]) in.get(i);
                int block = getHash(field, BLOCKS);
                req.add("hget");
                req.add(getNewKey(key, block));
                req.add(field);
                resp.add(interaction.call(req, db));
                req.clear();
            }
            ret = resp;
        } else if ("hscan".equals(cmd)) {
            int count = 10;
            byte[] match = null;
            try {
                for (int i = 3; i < in.size(); ++i) {
                    String arg = new String((byte[]) in.get(i));
                    if ("count".equalsIgnoreCase(arg)) {
                        try {
                            count = Integer.parseInt(new String((byte[]) in.get(i + 1)));
                        } catch (Exception e) {
                        }
                    } else if ("match".equalsIgnoreCase(arg)) {
                        try {
                            match = (byte[]) in.get(i + 1);
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Throwable t) {
            }
            long cursor = Long.parseLong(new String((byte[]) in.get(2)));
            int block = (int) (cursor >>> blockBitPosition);
            cursor = cursor & dataMask;
            ArrayList resq = new ArrayList();
            int leaved = count;
            int i;
            for (i = block; i < BLOCKS; ++i) {
                ArrayList req = new ArrayList();
                req.add(cmd);
                req.add(getNewKey((byte[]) in.get(1), i));
                req.add(Long.toString(cursor).getBytes(StandardCharsets.UTF_8));
                if (match != null) {
                    req.add("match".getBytes(StandardCharsets.UTF_8));
                    req.add(match);
                }
                if (count > 0) {
                    req.add("count".getBytes(StandardCharsets.UTF_8));
                    req.add(Integer.toString(leaved).getBytes(StandardCharsets.UTF_8));
                }
                Object o = interaction.call(req, db);
                if (o instanceof List) {
                    List list = (List) o;
                    cursor = Long.parseLong(new String((byte[]) list.get(0)));
                    resq.addAll((List) list.get(1));

                    // 当前block的项数太多了
                    if (cursor > dataMask) {
                        return new IllegalStateException("Err too many items(" + cursor + ") in "
                                + new String(getNewKey((byte[]) in.get(1), i))
                                + ", the max is " + dataMask);
                    }

                    if (cursor > 0 || count > 0 && (resq.size() >> 1) >= count) {
                        // 本次已经结束了
                        break;
                    }
                } else if (o instanceof Exception) {
                    return o;
                }
            }
            if (cursor == 0) {
                // 当前block已经全部取完了，代表当前block的值需要+1
                i++;
            }
            if (i < BLOCKS || cursor > 0) {
                long l = i == BLOCKS ? 0 : i;
                cursor = (l << blockBitPosition) + cursor;
            }
            List response = new ArrayList();
            response.add(Long.toString(cursor));
            response.add(resq);
            ret = response;
        }
        interaction.log(LOGLEVEL.DEBUG, "request: " + cmd + ", response: " + ret);
        return ret;
    }

    /**
     * 大致计算当前map中有多少条记录
     *
     * @param interaction
     * @param key
     * @param db
     * @return
     */
    private int approximateLen(Interaction interaction, byte[] key, int db) {
        long len = 0;
        ArrayList req = new ArrayList();
        req.add("hlen");
        req.add("");
        int loop = 0;
        for (int i = 0; i < BLOCKS; i += 7) {
            req.set(1, getNewKey(key, i));
            len += (Long) interaction.call(req, db);
            loop++;
        }
        if (loop > 1) {
            len = (long) (len * 1.0d / loop * BLOCKS);
        }
        return (int) len;
    }

    private String getType(byte[] key, Interaction interaction, int db) {
        ArrayList list = new ArrayList<>();
        list.add("type");
        list.add("");
        for (int i = 0; i < BLOCKS; ++i) {
            list.set(1, getNewKey(key, i));
            Object o = interaction.call(list, db);
            if (o instanceof String) {
                String resp = (String) o;
                if (!"none".equalsIgnoreCase(resp)) {
                    return resp;
                }
            }
        }
        list.set(1, key);
        return (String) interaction.call(list, db);
    }

    private void del(byte[] key, Interaction interaction, int db) {
        ArrayList list = new ArrayList<>();
        list.add("del");
        for (int i = 0; i < BLOCKS; ++i) {
            list.add(getNewKey(key, i));
        }
        interaction.call(list, db);
    }

    private byte[] getNewKey(byte[] key, byte[] field) {
        return getNewKey(key, getHash(field, BLOCKS));
    }

    private byte[] getNewKey(byte[] key, int block) {
        StringBuilder buf = new StringBuilder();
        buf.append(new String(key, StandardCharsets.UTF_8)).append(separator).append(block);
        return buf.toString().getBytes(StandardCharsets.UTF_8);
    }

//
//    public static void main(String[] arg) {
//        HashSplit2MultiModule multiModule = new HashSplit2MultiModule();
//
//        int BLOCKS = 64;
//
//        int[] data1 = new int[BLOCKS];
//        int[] data2 = new int[BLOCKS];
//        for (int i = 0; i < 700; i++) {
//            String str = "fieldaaa-" + i;
//            int block = multiModule.getHash(str.getBytes(StandardCharsets.UTF_8), BLOCKS);
//            data1[block]++;
//            if (block == 0) {
//                System.out.println("i = " + i + ", field = " + str);
//            }
//            block = str.hashCode() & 0xffff % BLOCKS;
//            data2[block]++;
////            data1[multiModule.getHash((""+i+"field-aefewfewfasfeafsdaerfAFAFEWFAWEDdfhgdtyjtrhrgelsfkpjoeirfjpoieapjfoiejrfpoiqajf;aoiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiweweiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiufuwehiuiwequhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhrgegasdfegefrwsfwafearfqewf").getBytes(StandardCharsets.UTF_8), DEFAULT_BLOCKS)]++;
////            data2[(""+i+"field-aefewfewfasfeafsdaerfAFAFEWFAWEDdfhgdtyjtrhrgelsfkpjoeirfjpoieapjfoiejrfpoiqajf;aoiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiweweiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiuiufuwehiuiwequhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhrgegasdfegefrwsfwafearfqewf").hashCode()&0xff % DEFAULT_BLOCKS]++;
//        }
//        System.out.println("\nData1:");
//        for (int i = 0; i < BLOCKS; i++) {
//            System.out.println(data1[i]);
//        }
//        System.out.println("\nData2:");
//        for (int i = 0; i < BLOCKS; i++) {
//            System.out.println(data2[i]);
//        }
//    }
}
