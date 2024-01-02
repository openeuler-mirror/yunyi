package com.tongtech.proxy.core;

import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.protocol.SessionAttribute;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public class StaticContent {
    // 结束服务进程的命令名，要复杂一些
    public static final String STOPING_STRING = "StopIng_ser%0Ver_bY_M#aNa!~gEr_frOmCl^ienT".replace('_', '-');

    // 返回多条记录时单个数据块返回的最大数量
    public static final int MAX_RESULT_ROWS = 512;

    public static final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(2);

    // 本机地址
    public static final InetSocketAddress LOCAL_ADDRESS = new InetSocketAddress(ProxyConfig.getListeningPort());

    // 同步命令
    public final static int SYNC_SET = 0;// 写操作
    public final static int SYNC_DEL = 1;// 删除操作
    public final static int SYNC_UPD = 2;// 更新操作
    public final static int SYNC_EXPIRE = 3;// 过期时间操作
    public final static int SYNC_CMD = 4;// 增（减）量命令操作
    public final static int SYNC_RESCUE = 5;// 包含类型的value全量复制操作
    public final static int SYNC_CLUSTER_DATA = 6;// 从其他cluster-group来的数据发起的多节点同步
    public final static int SYNC_REMOVE = 7;// key和数据在不同RDS间移动。

    // string
    public final static int CMD_SET = 1 << 8;
    public final static int CMD_APPEND = 2 << 8;
    public final static int CMD_GETSET = 3 << 8;
    public final static int CMD_MSET = 5 << 8;
    public final static int CMD_MSETNX = 6 << 8;
    public final static int CMD_PSETEX = 7 << 8;
    public final static int CMD_SETEX = 8 << 8;
    public final static int CMD_SETNX = 9 << 8;
    public final static int CMD_SETRANGE = 10 << 8;
    public final static int CMD_INCR = 11 << 8;
    public final static int CMD_INCRBYFLOAT = 12 << 8;
    public final static int CMD_DECR = 13 << 8;
    public final static int CMD_STRCHANGED = 14 << 8;
    public final static int CMD_STRLEN = 20 << 8;
    public final static int CMD_GET = 21 << 8;
    public final static int CMD_MGET = 22 << 8;
    public final static int CMD_SUBSTR = 23 << 8;
    public final static int CMD_GETRAGNE = 24 << 8;
    public final static int CMD_PFADD = 35 << 8;
    public final static int CMD_PFMERGE = 36 << 8;
    public final static int CMD_PFCOUNT = 37 << 8;
    public final static int CMD_SETBIT = 39 << 8;
    public final static int CMD_BITFIELD_RO = 40 << 8;
    public final static int CMD_GETBIT = 41 << 8;
    public final static int CMD_BITCOUNT = 42 << 8;
    public final static int CMD_BITFIELD = 43 << 8;
    public final static int CMD_BITOP = 44 << 8;
    public final static int CMD_BITPOS = 45 << 8;
    public final static int CMD_STRINGS = 47 << 8;

    // list
    // 命令编码小于CMD_LNEEDNEW的，如果key不存在，会自动创建1个新记录
    // 因为这些命令都会增加数据
    // CMD_LISTS定义list命令组的最大值，也是list命令的标志
    public final static int CMD_LPUSH = (48 + 1) << 8;
    public final static int CMD_RPUSH = (48 + 2) << 8;
    public final static int CMD_LISTNEEDNEW = (48 + 4) << 8; // 如果key不存在需要尝试新增的命令
    public final static int CMD_LSET = (48 + 5) << 8;
    public final static int CMD_LPUSHX = (48 + 6) << 8;
    public final static int CMD_RPUSHX = (48 + 7) << 8;
    public final static int CMD_LPOP = (48 + 8) << 8;
    public final static int CMD_RPOP = (48 + 9) << 8;
    public final static int CMD_LTRIM = (48 + 10) << 8;
    public final static int CMD_LREM = (48 + 12) << 8;// LREM KEY_NAME COUNT VALUE
    public final static int CMD_LISTCHANGED = (48 + 16) << 8; // 会改变list内容的命令
    public final static int CMD_LRANGE = (48 + 17) << 8;
    public final static int CMD_LINDEX = (48 + 18) << 8;
    public final static int CMD_LLEN = (48 + 19) << 8;
    public final static int CMD_BLPOP = (48 + 20) << 8;
    public final static int CMD_BRPOP = (48 + 21) << 8;
    public final static int CMD_BRPOPLPUSH = (48 + 22) << 8;
    public final static int CMD_LISTS = (48 + 47) << 8;

    // set
    // 命令编码小于CMD_SNEEDNEW的，如果key不存在，会自动创建1个新记录
    // 因为这些命令都会增加数据
    // CMD_SETS定义set命令组的最大值，也是set命令组的标志
    public final static int CMD_SADD = (96 + 1) << 8;
    public final static int CMD_SSET = (96 + 2) << 8;// 为了实现集合转储增加的命令
    public final static int CMD_SETNEEDNEW = (96 + 7) << 8; // 如果key不存在需要尝试新增的命令
    public final static int CMD_SREM = (96 + 8) << 8;
    public final static int CMD_SPOP = (96 + 9) << 8;
    public final static int CMD_SETCHANGED = (96 + 16) << 8;// 会改变set内容的命令
    public final static int CMD_SMEMBERS = (96 + 17) << 8;
    public final static int CMD_SISMEMBER = (96 + 18) << 8;
    public final static int CMD_SCARD = (96 + 19) << 8;
    public final static int CMD_SDIFF = (96 + 20) << 8;
    public final static int CMD_SDIFFSTORE = (96 + 21) << 8;
    public final static int CMD_SINTER = (96 + 22) << 8;
    public final static int CMD_SINTERSTORE = (96 + 23) << 8;
    public final static int CMD_SUNION = (96 + 24) << 8;
    public final static int CMD_SUNIONSTORE = (96 + 25) << 8;
    public final static int CMD_SSCAN = (96 + 26) << 8;
    public final static int CMD_SMOVE = (96 + 27) << 8;
    public final static int CMD_SETS = (96 + 47) << 8;

    // hash
    // 命令编码小于CMD_HNEEDNEW的，如果key不存在，会自动创建1个新记录
    // 因为这些命令都会增加数据
    // CMD_HASHS定义hash命令组的最大值，也是hash命令组的标志
    public final static int CMD_HSET = (144 + 1) << 8;
    public final static int CMD_HMSET = (144 + 2) << 8;
    public final static int CMD_HSETNX = (144 + 3) << 8;
    public final static int CMD_HINCRBY = (144 + 4) << 8;
    public final static int CMD_HINCRBYFLOAT = (144 + 5) << 8;
    public final static int CMD_HASHNEEDNEW = (144 + 7) << 8; // 如果key不存在需要尝试新增的命令
    public final static int CMD_HDEL = (144 + 8) << 8;
    public final static int CMD_HASHCHANGED = (144 + 16) << 8; // 会改变hash内容的命令
    public final static int CMD_HGET = (144 + 17) << 8;
    public final static int CMD_HGETALL = (144 + 18) << 8;
    public final static int CMD_HLEN = (144 + 19) << 8;
    public final static int CMD_HKEYS = (144 + 20) << 8;
    public final static int CMD_HVALS = (144 + 21) << 8;
    public final static int CMD_HEXISTS = (144 + 22) << 8;
    public final static int CMD_HSTRLEN = (144 + 23) << 8;
    public final static int CMD_HSCAN = (144 + 24) << 8;
    public final static int CMD_HMGET = (144 + 25) << 8;
    public final static int CMD_HASHS = (144 + 47) << 8;

    // zset
    // 命令编码小于CMD_ZNEEDNEW的，如果key不存在，会自动创建1个新记录
    // 因为这些命令都会增加数据
    // CMD_ZSETSS定义zset命令组的最大值，也是zset命令组的标志
    public final static int CMD_ZADD = (192 + 1) << 8;
    public final static int CMD_ZADDBYRESCUE = (192 + 2) << 8;
    public final static int CMD_ZINCRBY = (192 + 3) << 8;
    public final static int CMD_ZSETNEEDNEW = (192 + 6) << 8; // 如果key不存在需要尝试新增的命令
    public final static int CMD_ZREM = (192 + 7) << 8;
    public final static int CMD_ZREMRANGEBYLEX = (192 + 8) << 8;
    public final static int CMD_ZREMRANGEBYRANK = (192 + 9) << 8;
    public final static int CMD_ZREMRANGEBYSCORE = (192 + 10) << 8;
    public final static int CMD_ZSETCHANGED = (192 + 13) << 8;
    public final static int CMD_ZCARD = (192 + 14) << 8;
    public final static int CMD_ZCOUNT = (192 + 15) << 8;
    public final static int CMD_ZLEXCOUNT = (192 + 16) << 8;
    public final static int CMD_ZRANGE = (192 + 17) << 8;
    public final static int CMD_ZRANGEBYLEX = (192 + 18) << 8;
    public final static int CMD_ZRANGEBYSCORE = (192 + 19) << 8;
    public final static int CMD_ZREVRANGEBYSCORE = (192 + 20) << 8;
    public final static int CMD_ZRANK = (192 + 21) << 8;
    public final static int CMD_ZREVRANGE = (192 + 22) << 8;
    public final static int CMD_ZREVRANK = (192 + 23) << 8;
    public final static int CMD_ZSCORE = (192 + 24) << 8;
    public final static int CMD_ZSCAN = (192 + 25) << 8;
    public final static int CMD_ZINTERSTORE = (192 + 26) << 8;
    public final static int CMD_ZUNIONSTORE = (192 + 27) << 8;
    public final static int CMD_ZPOPMIN = (192 + 28) << 8;
    public final static int CMD_ZPOPMAX = (192 + 29) << 8;
    public final static int CMD_BZPOPMIN = (192 + 30) << 8;
    public final static int CMD_BZPOPMAX = (192 + 31) << 8;
    public final static int CMD_ZINTER = (192 + 32) << 8;
    public final static int CMD_ZUNION = (192 + 33) << 8;
    public final static int CMD_ZSETS = (192 + 47) << 8;

    // GEO 240
    public final static int CMD_GEOADD = (240 + 1) << 8;
    public final static int CMD_GEONEEDNEW = (240 + 6) << 8;
    public final static int CMD_GEOCHANGED = (240 + 7) << 8;
    public final static int CMD_GEODIST = (240 + 8) << 8;
    public final static int CMD_GEOPOS = (240 + 9) << 8;
    public final static int CMD_GEOHASH = (240 + 10) << 8;
    public final static int CMD_GEORADIUS = (240 + 11) << 8;
    public final static int CMD_GEORADIUSBYMEMBER = (240 + 12) << 8;
    public final static int CMD_GEOS = (240 + 15) << 8;

    // padding 256 - 288

    // stream 288
    public final static int CMD_XADD = (288 + 1) << 8;
    public final static int CMD_XADDBYRESCUE = (288 + 2) << 8;
    public final static int CMD_NEWNULLSTREAM = (288 + 3) << 8;
    public final static int CMD_XNEW = (288 + 6) << 8;
    public final static int CMD_XTRIM = (288 + 7) << 8;
    public final static int CMD_XDEL = (288 + 8) << 8;
    public final static int CMD_XLEN = (288 + 9) << 8;
    public final static int CMD_XRANGE = (288 + 10) << 8;
    public final static int CMD_XREVRANGE = (288 + 11) << 8;
    public final static int CMD_XREAD = (288 + 12) << 8;
    public final static int CMD_XGROUPS = (288 + 16) << 8;
    public final static int CMD_XGROUP = (288 + 19) << 8;
    public final static int CMD_XREADGROUP = (288 + 20) << 8;
    public final static int CMD_XACK = (288 + 21) << 8;
    public final static int CMD_XPENDING = (288 + 22) << 8;
    public final static int CMD_XCLAIM = (288 + 23) << 8;
    public final static int CMD_XINFO = (288 + 24) << 8;
    public final static int CMD_XGETSTREAMOBJECT = (288 + 30) << 8;
    public final static int CMD_XSTREAMS = (288 + 47) << 8;

    // script 336(16)
    public final static int CMD_EVEL = (336 + 1) << 8;
    public final static int CMD_EVELSHA = (336 + 2) << 8;
    public final static int CMD_SCRIPT = (336 + 3) << 8;
    public final static int CMD_SCRIPT_LOAD = (336 + 10) << 8;
    public final static int CMD_SCRIPT_FLUSH = (336 + 11) << 8;
    public final static int CMD_SCRIPTS = (336 + 15) << 8;


    // padding 352 - 368 (16)

    // padding 368 - 400 (32)

    // padding 400 - 432 (32)

    // key 336 384 432
    public final static int CMD_DEL = (432 + 1) << 8;
    public final static int CMD_RENAME = (432 + 2) << 8;
    public final static int CMD_RENAMENX = (432 + 3) << 8;
    public final static int CMD_KEYCHANGED = (432 + 7) << 8;
    public final static int CMD_EXISTS = (432 + 8) << 8;
    public final static int CMD_EXPIRE = (432 + 9) << 8;
    public final static int CMD_EXPIREAT = (432 + 10) << 8;
    public final static int CMD_PEXPIRE = (432 + 11) << 8;
    public final static int CMD_PEXPIREAT = (432 + 12) << 8;
    public final static int CMD_PERSIST = (432 + 13) << 8;
    public final static int CMD_KEYS = (432 + 14) << 8;
    public final static int CMD_TTL = (432 + 15) << 8;
    public final static int CMD_PTTL = (432 + 16) << 8;
    public final static int CMD_RANDOMKEY = (432 + 17) << 8;
    public final static int CMD_SCAN = (432 + 18) << 8;
    public final static int CMD_TYPE = (432 + 19) << 8;
    public final static int CMD_TOUCH = (432 + 20) << 8;
    public final static int CMD_DUMP = (432 + 21) << 8;
    public final static int CMD_RESTORE = (432 + 22) << 8;
    public final static int CMD_KEYSKEYS = (432 + 47) << 8;

    // server 480
    public final static int CMD_BGSAVE = (480 + 1) << 8;
    public final static int CMD_SAVE = (480 + 2) << 8;
    public final static int CMD_CLIENT = (480 + 3) << 8;
    public final static int CMD_CLUSTER = (480 + 4) << 8;
    public final static int CMD_COMMAND = (480 + 5) << 8;
    public final static int CMD_TIME = (480 + 6) << 8;
    public final static int CMD_CONFIG = (480 + 7) << 8;
    public final static int CMD_DBSIZE = (480 + 8) << 8;
    public final static int CMD_DEBUG = (480 + 9) << 8;
    public final static int CMD_INFO = (480 + 10) << 8;
    public final static int CMD_LASTSAVE = (480 + 11) << 8;
    public final static int CMD_MONITOR = (480 + 12) << 8;
    public final static int CMD_ROLE = (480 + 13) << 8;
    public final static int CMD_QUIT = (480 + 14) << 8;
    public final static int CMD_PING = (480 + 15) << 8;
    public final static int CMD_AUTH = (480 + 16) << 8;
    public final static int CMD_SELECT = (480 + 17) << 8;
    public final static int CMD_ECHO = (480 + 18) << 8;
    public final static int CMD_FLUSHDB = (480 + 19) << 8;
    public final static int CMD_FLUSHALL = (480 + 20) << 8;
    public final static int CMD_HELLO = (480 + 21) << 8;
    public final static int CMD_READONLY = (480 + 22) << 8;
    public final static int CMD_REDISSERVER = (480 + 33) << 8;
    public final static int CMD_CHECK = (480 + 34) << 8;
    public final static int CMD_IDLE = (480 + 35) << 8;
    public final static int CMD_HEARTBEAT = (480 + 36) << 8;
    public final static int CMD_WAIT = (480 + 37) << 8;
    public final static int CMD_DATARESCUE = (480 + 38);
    public final static int CMD_SERVERS = (480 + 47) << 8;

    // pubsub 
    public final static int CMD_PSUBSCRIBE = (528 + 1) << 8;
    public final static int CMD_PUBSUB = (528 + 2) << 8;
    public final static int CMD_PUBLISH = (528 + 3) << 8;
    public final static int CMD_PUNSUBSCRIBE = (528 + 4) << 8;
    public final static int CMD_SUBSCRIBE = (528 + 5) << 8;
    public final static int CMD_UNSUBSCRIBE = (528 + 6) << 8;
    public final static int CMD_PUBSUBS = (528 + 15) << 8;

    // transaction
    public final static int CMD_DISCARD = (544 + 1) << 8;
    public final static int CMD_MULTI = (544 + 2) << 8;
    public final static int CMD_EXEC = (544 + 3) << 8;
    public final static int CMD_WATCH = (544 + 4) << 8;
    public final static int CMD_UNWATCH = (544 + 5) << 8;
    public final static int CMD_TRANSACTIONS = (544 + 15) << 8;

    public final static int CMD_SLOWLOG = (560 + 1) << 8;
    public final static int CMD_STATISTICS = (560 + 15) << 8;

    // end of 576

    // LicenseChecker中使用
    public static final String LICENSE_AUTHORED_BY_CENTER = "Receive authorization from CenterServer";
    public static final String LICENSE_FILE_NOT_FOUND = "The License load failed.";
    public static final String LICENSE_IS_EXPIRED = "The license is expired. Please contact the manufacturer.";

    public static final long ServerStartTime = System.currentTimeMillis();

    /**
     * Default placeholder prefix: {@value}
     */
    public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

    /**
     * Default placeholder suffix: {@value}
     */
    public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

    /**
     * Default value separator: {@value}
     */
    public static final String DEFAULT_VALUE_SEPARATOR = ":";

//    public static final Object NULL_OBJECT = new Object() {
//        @Override
//        public String toString() {
//            return "";
//        }
//    };

    private final static String[] EXCAPED_CHARS;

    static {
        EXCAPED_CHARS = new String[128];
        for (int i = 0; i < 128; ++i) {
            EXCAPED_CHARS[i] = null;
        }
        EXCAPED_CHARS[' '/*32*/] = "\\ ";
        EXCAPED_CHARS['\\'/*92*/] = "\\\\";
        EXCAPED_CHARS['\n'/*10*/] = "\\\n";
        EXCAPED_CHARS['"'] = "\\\"";
        EXCAPED_CHARS['\''] = "\\'";
    }


    // 当前程序授权的厂家ID
    public static volatile int VENDER_ID = 99;

    // 创建的客户端 Socket 编号
    public static final AtomicLong ClientId = new AtomicLong(0);

    // 集中保存连接的个性化属性
    public static ConcurrentHashMap<ChannelHandlerContext, SessionAttribute> CachedSessionAttributes
            = new ConcurrentHashMap<>(1024);

    public static final Vector<byte[]> splitBytes(byte[] src, char c, int max) {
        // 字符串分解最大值小于1，返回null
        if (max <= 0) {
            return null;
        }

        Vector<byte[]> spliter = new Vector<>();

        // 输入字符串为空，返回空数组
        if (src == null) {
            return spliter;
        }

        int begin = 0, end = 0;
        int len = src != null ? src.length : 0;
//        StringBuilder buf = new StringBuilder(128);

        for (int i = 0; i < max; i++) {
            // 跳过连续找到非空格的第一个字符
            begin = end;// 不能加1，否则src的第一个字符可能会被漏掉
            char seperater = c;
            while (begin < len) {
//                if (src.charAt(begin) == '"') {
//                    seperater = '"';
//                    begin++;
//                    break;
                if (seperater == ' ' && (src[begin] == '"' || src[begin] == '\'')) {
                    seperater = (char) src[begin];
                    begin++;
                    break;
                } else if (src[begin] != seperater) {
                    break;
                }
                begin++;
            }

            // 找到下1个分隔字符
            end = begin;
            boolean shift = false;

            if (end < len) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream(128);
                while (end < len) {
                    byte b = src[end];
                    if (shift) {
                        // 被转义的字符
                        // \r会出现在sync的包里
//                        if (b != '\r') {
//                            // \r 系统内有特殊用途，不允许在内容里出现
                        buf.write(b);
//                        }
                        shift = false;
                    } else if (b == '\\') {
                        // 转义符
                        shift = true;
                    } else if (b == seperater) {
                        if (seperater != ' ') {
                            seperater = ' ';
                            end++;
                        }
                        break;
                    } else {
                        buf.write(b);
                    }
                    end++;
                }
                spliter.add(buf.toByteArray());
            }
            if (end >= len) {
                break;
            }
        }
        return spliter;
    }

    /**
     * 比较2个字节数组是否相等
     *
     * @param b1   数组1
     * @param off1 数组1的开始位置偏移量
     * @param b2   数组2
     * @param off2 数组2的开始位置偏移量
     * @param len  比较的总长度
     * @return 如果所有字节都相等返回true, 否则返回false.
     */
    public final static boolean intArrayEquals(int[] b1, int off1, int[] b2,
                                               int off2, int len) {

        // 数组越界
        if ((b1.length < off1 + len) || (b2.length < off2 + len)) {
            return false;
        }

        // 判断是否有不等的字节
        for (int i = 0; i < len; i++) {
            // System.out.println("b1="+b1[off1 + i]+", b2="+b2[off2 + i]);
            if (b1[off1 + i] != b2[off2 + i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 取字符串的hash值，自有算法。 语言算法hash值由字符串的最后位决定，当前程序不适用
     *
     * @param value
     * @return
     */
    public static int hashBytes(byte[] value) {
        int hash = 0;
        if (value != null && value.length > 0) {
            for (int i = 0; i < value.length; i++) {
                hash = 31 * hash + value[i];
            }
            hash = (hash >>> 22) ^ (hash >>> 13) ^ hash;
        }
        return hash & 0x7fffffff;
    }

    /**
     * 从Vector中取指定项，如果指定项不存在或是空项则返回null
     *
     * @param argv
     * @param index
     * @return
     */
    public static byte[] getFromBytesList(List<byte[]> argv, int index) {
        return argv != null && index >= 0 && index < argv.size() ? argv.get(index) : null;
        // return ret != null && ret.length() > 0 ? ret : null;
    }

    public static String getFromStringList(List<String> argv, int index) {
        return argv != null && index >= 0 && index < argv.size() ? argv.get(index) : null;
        // return ret != null && ret.length() > 0 ? ret : null;
    }

    /**
     * 用来将字符串中间的空格转义
     *
     * @param str
     * @return
     */
    public static String escapedString(String str) {
        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            return "\"\"";
        }

        boolean escaped = false;
        StringBuilder buf = new StringBuilder(str.length() + 10);
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            String esc;
            if (c >= 0 && c < EXCAPED_CHARS.length && (esc = EXCAPED_CHARS[c]) != null) {
                buf.append(esc);
                escaped = true;
            } else {
                buf.append(c);
            }
        }
        if (escaped) {
            return buf.toString();
        }
        return str;
    }

    /**
     * 恢复上述方法的转义,20220724增加
     *
     * @param str
     * @return
     */
    public static String unescapedString(String str) {
        if (str == null) {
            return null;
        } else if ("\"\"".equals(str)) {
            return "";
        }

        boolean unescaped = false;
        StringBuilder buf = new StringBuilder(str.length());
        boolean lastescaped = false;
        for (int i = 0; i < str.length(); ++i) {
            char c = str.charAt(i);
            if (lastescaped) {
                buf.append(c);
                lastescaped = false;
            } else if (c == '\\') {
                unescaped = true;
                lastescaped = true;
            } else {
                buf.append(c);
            }
        }
        if (unescaped) {
            return buf.toString();
        }
        return str;
    }

    /**
     * 获取当前主机的MAC地址
     *
     * @return
     */
    public static long[] getCurrentMacs() {
        HashSet<Long> macs = new HashSet<Long>();
        //new NetPermission("getNetworkInformation");
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface inter = interfaces.nextElement();
                byte[] hardware = inter.getHardwareAddress();
                if (hardware == null) {
                    // null if network is not accessable
                    continue;
                }
                long h = 0;
                for (int i = 0; i < hardware.length; i++) {
                    h = (h << 8) | (hardware[i] & 0xffl);
                }
                macs.add(h);
            }
        } catch (SocketException se) {
        }

        Long[] mac = macs.toArray(new Long[0]);
        long[] ret = new long[mac.length];
        for (int i = 0; i < mac.length; i++) {
            ret[i] = mac[i];
        }
        return ret;
    }

    /**
     * 将输入的内存大小转换为更易读的内容
     *
     * @param l
     * @return
     */
    public static String getMemorySizeHuman1(long l) {
        if (l < 1024) {
            return Long.toString(l);
        }
        String ret;
        if (l > (1l << 30)) {
            l = l * 100 / 1024 / 1024 / 1024;
            ret = Long.toString(l);
            ret = ret.substring(0, ret.length() - 2) + "." + ret.substring(ret.length() - 2) + "G";
        } else if (l > (1l << 20)) {
            l = l * 100 / 1024 / 1024;
            ret = Long.toString(l);
            ret = ret.substring(0, ret.length() - 2) + "." + ret.substring(ret.length() - 2) + "M";
        } else if (l > (1l << 10)) {
            l = l * 100 / 1024;
            ret = Long.toString(l);
            ret = ret.substring(0, ret.length() - 2) + "." + ret.substring(ret.length() - 2) + "K";
        } else {
            ret = Long.toString(l);
        }
        return ret;
    }

    public static String getMemorySizeHuman(long l) {
        if (l < 1024) {
            return Long.toString(l);
        }
        String ret;
        if (l > (1l << 30)) {
            l = l * 100 / 1024 / 1024 / 1024;
            if (l % 100 == 0) {
                ret = Long.toString(l / 100);
            } else {
                ret = Double.toString(l / 100.0d);
            }
            ret = ret + "G";
        } else if (l > (1l << 20)) {
            l = l * 100 / 1024 / 1024;
            if (l % 100 == 0) {
                ret = Long.toString(l / 100);
            } else {
                ret = Double.toString(l / 100.0d);
            }
            ret = ret + "M";
        } else if (l > (1l << 10)) {
            l = l * 100 / 1024;
            if (l % 100 == 0) {
                ret = Long.toString(l / 100);
            } else {
                ret = Double.toString(l / 100.0d);
            }
            ret = ret + "K";
        } else {
            ret = Long.toString(l);
        }
        return ret;
    }

    /**
     * 上述函数的逆函数
     *
     * @param str
     * @return
     */
    public static long humanStringToLong(String str, long def) {
        long data = def;
        try {
            if (str.endsWith("g") || str.endsWith("G")) {
                data = Long.parseLong(str.substring(0, str.length() - 1)) * 1024 * 1024 * 1024;
            } else if (str.endsWith("m") || str.endsWith("M")) {
                data = Long.parseLong(str.substring(0, str.length() - 1)) * 1024 * 1024;
            } else if (str.endsWith("k") || str.endsWith("K")) {
                data = Long.parseLong(str.substring(0, str.length() - 1)) * 1024;
            } else {
                data = Long.parseLong(str);
            }
        } catch (Throwable t) {
        }
        return data;
    }

    // 最近一次成功执行dumpAll的时间
    private static volatile long lastSuccessfulSave = 0;

    public static void setLastSuccessfulSave(long timestamp) {
        lastSuccessfulSave = timestamp;
    }

    public static long getLastSuccessfulSave() {
        return lastSuccessfulSave;
    }

    /**
     * Count the number of bytes required to encode the string as UTF-8.
     *
     * @param str String of unicode characters to be encoded as UTF-8
     * @return count of bytes needed to encode using UTF-8
     */
    public static int lengthAsUtf8(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }

        int i, b;
        char c;
        for (i = b = str.length(); --i >= 0; )
            if ((c = str.charAt(i)) >= 0x80)
                b += (c >= 0x800) ? 2 : 1;
        return b;
    }

    public static Set getLocalIpAddress() {
        Set ipList = new HashSet<>();
        try {
            Enumeration allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                if (netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress ip = (InetAddress) addresses.nextElement();
                        if (ip != null) {
                            ipList.add(ip);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipList;
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean substringMatch(CharSequence str, int index, CharSequence substring) {
        if (index + substring.length() > str.length()) {
            return false;
        }
        for (int i = 0; i < substring.length(); i++) {
            if (str.charAt(index + i) != substring.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static long getStartedSeconds() {
        return (System.currentTimeMillis() - ServerStartTime) / 1000;
    }

    public static final Vector<String> splitString(String src, char c, int max) {
        // 字符串分解最大值小于1，返回null
        if (max <= 0) {
            return null;
        }

        Vector<String> spliter = new Vector<>();

        // 输入字符串为空，返回空数组
        if (src == null) {
            return spliter;
        }

        int begin = 0, end = 0;
        int len = src != null ? src.length() : 0;
        StringBuilder buf = new StringBuilder(128);

        for (int i = 0; i < max; i++) {
            // 跳过连续找到非空格的第一个字符
            begin = end;// 不能加1，否则src的第一个字符可能会被漏掉
            char seperater = c;
            while (begin < len) {
//                if (src.charAt(begin) == '"') {
//                    seperater = '"';
//                    begin++;
//                    break;
                if (seperater == ' ' && (src.charAt(begin) == '"' /*|| src.charAt(begin) == '\''*/)) {
                    seperater = src.charAt(begin);
                    begin++;
                    break;
                } else if (src.charAt(begin) != seperater) {
                    break;
                }
                begin++;
            }

            // 找到下1个分隔字符
            end = begin;
            boolean shift = false;
            buf.setLength(0);
            if (end < len) {
                while (end < len) {
                    char b = src.charAt(end);
                    if (shift) {
                        // 被转义的字符
                        // \r会出现在sync的包里
//                        if (b != '\r') {
//                            // \r 系统内有特殊用途，不允许在内容里出现
                        if (b != seperater) {
                            buf.append('\\');
                        }
                        buf.append(b);
//                        }
                        shift = false;
                    } else if (b == '\\') {
                        // 转义符
                        shift = true;
                    } else if (b == seperater) {
                        if (seperater != ' ') {
                            seperater = ' ';
                            end++;
                        }
                        break;
                    } else {
                        buf.append(b);
                    }
                    end++;
                }
                spliter.add(buf.toString());
            }
            if (end >= len) {
                break;
            }
        }
        return spliter;
    }

    /**
     * 以下部分定义2进制兼容模式的转换函数
     * char2escape中是针对2进制模式下不能兼容的字符的转换对照表
     * 其中比正常多增加了空格（‘ ’）的转换，针对自有协议的转换
     */
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

    /**
     * 判断当前字节是否是utf8编码的字节串的首字符
     *
     * @param data
     * @param offset
     * @return utf8字节串附属字节长度，例如：大多数中文是utf8为 3个字节的编码，则函数返回 2。如果返回 -1 说明 offset指定的位置不是 utf8 多字符字节串
     */
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
                if (((ch & 0xe0) == 0xc0) && ((ch1 & 0xc0) == 0x80)) {
                    ch = ((ch & 0x1f) << 6) | (ch1 & 0x3f);
                    if (ch > 0x7f && ch <= 0x7ff) {
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

    /**
     * 编码,模拟js的escape函数.<br>
     * escape不编码字符有69个：*+-./@_0-9a-zA-Z
     *
     * @param data 字符串
     * @return 转义后的字符串或者null
     */
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
            return new String(data, StandardCharsets.UTF_8);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 2);
        int end = offset + len;
        for (int i = offset; i < end; i++) {
            int ch = data[i] & 0xff;
            if (char2escape[ch] == null) {
                bos.write(ch);
            } else {
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

    /**
     * escape 方法的逆方法
     *
     * @param escaped 编码后的字符串
     * @return
     */
    public static final byte[] accept(String escaped, int start, int end) {
        if (escaped == null) {
            return null;
        } else if (start < 0
                || start > escaped.length()
                || end < start
                || end > escaped.length()) {
            throw new ArrayIndexOutOfBoundsException("in accept: len of data "
                    + escaped.length() + ", but start is " + start + " and end is " + end);
        } else if (start == end) {
            return NULL_BYTES;
        }

        int idx = escaped.indexOf('\\', start);
        if (idx < 0 || idx >= end) {
            return escaped.substring(start, end).getBytes(StandardCharsets.UTF_8);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream(escaped.length());
        for (int i = start; i < end; ++i) {
            int ch = escaped.charAt(i);
            if (ch == '\\') {
                // char 1
                int ch1 = escaped.charAt(++i);
                if (ch1 == 'r') {
                    ch = '\r';
                } else if (ch1 == 'n') {
                    ch = '\n';
                } else if (ch1 == 'b') {
                    ch = '\b';
                } else if (ch1 == 't') {
                    ch = '\t';
                } else if (ch1 == 'f') {
                    ch = '\f';
                } else if (ch1 == 'x') { // \\u
                    ch = 0;
                    for (int j = 0; j < 2; j++) {
                        ch1 = escaped.charAt(++i);
                        if (ch1 > '9') {
                            ch1 |= 0x20;/* Force lowercase */
                        }
                        if (ch1 >= 'a' && ch1 <= 'z') {
                            ch1 = ch1 - 'a' + 10;
                        } else if (ch1 >= '0' && ch1 <= '9') {
                            ch1 = ch1 - '0';
                        } else {
                            throw new IllegalArgumentException("error char at " + i + " in '" + escaped + "'");
                        }
                        ch = (ch << 4) + ch1;
                    }
                } else {
                    ch = ch1;
                }
                bos.write(ch);
            } else {
                if (ch <= 0x7F) {
                    bos.write(ch);
                } else if (ch <= 0x7FF) {
                    bos.write((ch >> 6) | 0xC0);
                    bos.write((ch & 0x3F) | 0x80);
                } else {
                    bos.write((ch >> 12) | 0xE0);
                    bos.write(((ch >> 6) & 0x3F) | 0x80);
                    bos.write((ch & 0x3F) | 0x80);
                }
            }
        }
        return bos.toByteArray();
    }
}
