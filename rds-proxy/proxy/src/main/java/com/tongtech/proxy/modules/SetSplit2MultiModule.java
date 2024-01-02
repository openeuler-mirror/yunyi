package com.tongtech.proxy.modules;

import com.tongtech.proxy.util.Interaction;
import com.tongtech.proxy.util.LOGLEVEL;
import com.tongtech.proxy.util.PluginModule;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 该插件拦截hash类操作命令，将一个key下面的项分配到多个key下面存储，解决单key对应的数据量过大问题
 */
public class SetSplit2MultiModule implements PluginModule {
    // 缺省分块数
    private final static int DEFAULT_BLOCKS = 16;
    // 缺省的smembers命令返回的最多元素数，超过此值返回失败
    private final static int DEFAULT_MAXITMES = 1024 * 100;
    // 缺省的block值在sscan命令返回结果中的cursor中保存的起始位置（bit位），packed模式时小于32
    private final static int DEFAULT_BLOCKBITPOSITION = 32;

    private int BLOCKS = DEFAULT_BLOCKS;
    private String separator = "^o^";
    private int maxItems = DEFAULT_MAXITMES;

    // 以下两项只用户sscan命令时返回cursor中的block信息和block内的cursor信息的组合
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
            // 但采用此配置时sscan返回cursor值的范围的兼容性更好
            // 该配置只影响sscan命令
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
        return new String[]{"sadd", "scard", "sdiff", "sdiffstore", "sinter", "sinterstore", "sunion", "sunionstore"
                , "sismember", "smembers", "smove", "spop", "srandmember", "srem", "sscan", "del", "type"};
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
                if ("set".equalsIgnoreCase(getType(key, interaction, db))) {
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
        } else if ("sismember".equals(cmd)) {
            // 双目命令
            byte[] key = (byte[]) in.get(1);
            byte[] field = (byte[]) in.get(2);
            ArrayList req = new ArrayList();
            req.add(cmd);
            req.add(getNewKey(key, field));
            req.add(field);
            ret = interaction.call(req, db);
        } else if ("smove".equals(cmd)) {
            // 三目命令
            byte[] old_key = (byte[]) in.get(1);
            byte[] new_key = (byte[]) in.get(2);
            byte[] field = (byte[]) in.get(3);
            int block = getHash(field, BLOCKS);
            ArrayList req = new ArrayList();
            req.add("srem");
            req.add(getNewKey(old_key, block));
            req.add(field);
            ret = interaction.call(req, db);
            if (ret instanceof Long && ((Long) ret).longValue() == 1) {
                req.clear();
                req.add("sadd");
                req.add(getNewKey(new_key, block));
                req.add(field);
                interaction.call(req, db);
                ret = new Long(1);
            }
        } else if ("smembers".equals(cmd)) {
            // 单目命令
            byte[] key = (byte[]) in.get(1);
            if (maxItems > 0) {
                int len = approximateLen(interaction, key, db);
                if (len > maxItems) {
                    return new IllegalAccessException("Err too many fields were found");
                }
            }

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
        } else if ("spop".equals(cmd)) {
            // 单目命令
            byte[] key = (byte[]) in.get(1);
            ArrayList req = new ArrayList();
            req.add(cmd);
            req.add("");
            for (int i = 0; i < BLOCKS; ++i) {
                req.set(1, getNewKey(key, i));
                ret = interaction.call(req, db);
                if (ret != null) {
                    break;
                }
            }
        } else if ("scard".equals(cmd)) {
            // 单目命令
            byte[] key = (byte[]) in.get(1);
            long len = 0;
            ArrayList req = new ArrayList();
            req.add(cmd);
            req.add("");
            for (int i = 0; i < BLOCKS; ++i) {
                req.set(1, getNewKey(key, i));
                Object o = interaction.call(req, db);
                if (o instanceof Long) {
                    len += (Long) o;
                } else if (o instanceof Exception) {
                    return o;
                }
            }
            ret = new Long(len);
        } else if ("srem".equals(cmd)) {
            // 多目命令
            byte[] key = (byte[]) in.get(1);
            ArrayList[] reqs = new ArrayList[BLOCKS];
            for (int i = 0; i < BLOCKS; ++i) {
                reqs[i] = new ArrayList();
                reqs[i].add(cmd);
                reqs[i].add(getNewKey(key, i));
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
        } else if ("sadd".equals(cmd)) {
            // 多目命令
            long len = 0;
            byte[] key = (byte[]) in.get(1);
            ArrayList[] reqs = new ArrayList[BLOCKS];
            for (int i = 0; i < BLOCKS; ++i) {
                // 创建key的数组
                reqs[i] = new ArrayList();
                reqs[i].add(cmd);
                reqs[i].add(getNewKey(key, i));
            }
            for (int i = 2; i < in.size(); ++i) {
                int block = getHash((byte[]) in.get(i), BLOCKS);
                reqs[block].add(in.get(i));
            }
            for (int i = 0; i < BLOCKS; ++i) {
                if (reqs[i].size() > 2) {
                    Object o = interaction.call(reqs[i], db);
                    if (o instanceof Long) {
                        len += ((Long) o).longValue();
                    } else {
                        return o;
                    }
                }
            }
            ret = new Long(len);
        } else if ("sscan".equals(cmd)) {
            // 多目可变参数命令
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

                    if (cursor > 0 || count > 0 && resq.size() >= count) {
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
        } else {
            ret = new IllegalAccessException("Err unsupport command '" + cmd + "' in plugin SetSplit2MultiModule");
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
        req.add("scard");
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
}
