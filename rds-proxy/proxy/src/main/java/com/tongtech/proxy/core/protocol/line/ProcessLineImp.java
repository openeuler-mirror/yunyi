package com.tongtech.proxy.core.protocol.line;

import com.tongtech.proxy.jmx.StatusColector;
import io.netty.channel.ChannelHandlerContext;
import com.org.luaj.LuaError;
import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.acl.AclFailedException;
import com.tongtech.proxy.core.db.LuaManager;
import com.tongtech.proxy.core.protocol.NioProcess;
import com.tongtech.proxy.core.pubsub.PSManager;
import com.tongtech.proxy.core.utils.*;
import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.tongtech.proxy.core.StaticContent.*;


public class ProcessLineImp implements NioProcess {
    // 日志
    private static final Log logger = ProxyConfig.getServerLog();

    // 记录Server启动时间
    private final long ServerStartTime;

    // 记录Server上次提供数据恢复服务的时间
    private volatile long ServerRescuedTime;


    /**
     * 为了判断监听事件内容增加，只在PSManager.isEventCmd(cmd)有效时用到
     */
    public static final HashMap<String, Integer> STR_CMDS = new HashMap<String, Integer>() {
        {
            this.put("get", StaticContent.CMD_GET);
            this.put("set", StaticContent.CMD_SET);
            this.put("getset", StaticContent.CMD_GETSET);
            this.put("append", StaticContent.CMD_APPEND);
            this.put("incr", StaticContent.CMD_INCR);
            this.put("incrby", StaticContent.CMD_INCR);
            this.put("incrbyfloat", StaticContent.CMD_INCRBYFLOAT);
            this.put("decr", StaticContent.CMD_DECR);
            this.put("decrby", StaticContent.CMD_DECR);
            this.put("strlen", StaticContent.CMD_STRLEN);
            this.put("mget", StaticContent.CMD_MGET);
            this.put("mset", StaticContent.CMD_MSET);
            this.put("msetnx", StaticContent.CMD_MSETNX);
            this.put("touch", StaticContent.CMD_TOUCH);
        }
    };

    /**
     * 为了判断监听事件内容增加，只在PSManager.isEventCmd(cmd)有效时用到
     */
    public static final HashMap<String, Integer> KEY_CMDS = new HashMap<String, Integer>() {
        {
            this.put("del", StaticContent.CMD_DEL);
            this.put("rename", StaticContent.CMD_RENAME);
            this.put("renamenx", StaticContent.CMD_RENAMENX);
            this.put("exists", StaticContent.CMD_EXISTS);
            this.put("expire", StaticContent.CMD_EXPIRE);
            this.put("expireat", StaticContent.CMD_EXPIREAT);
            this.put("pexpire", StaticContent.CMD_PEXPIRE);
            this.put("pexpireat", StaticContent.CMD_PEXPIREAT);
            this.put("persist", StaticContent.CMD_PERSIST);
            this.put("keys", StaticContent.CMD_KEYS);
            this.put("ttl", StaticContent.CMD_TTL);
            this.put("pttl", StaticContent.CMD_PTTL);
            this.put("randomkey", StaticContent.CMD_RANDOMKEY);
            this.put("scan", StaticContent.CMD_SCAN);
            this.put("type", StaticContent.CMD_TYPE);
            this.put("touch", StaticContent.CMD_TOUCH);
            this.put("dump", StaticContent.CMD_DUMP);
            this.put("restore", StaticContent.CMD_RESTORE);
        }
    };

    public static final HashMap<String, Integer> STRING_CMDS = new HashMap<String, Integer>() {
        {
            this.put("getset", StaticContent.CMD_GETSET);
            this.put("append", StaticContent.CMD_APPEND);
            this.put("strlen", StaticContent.CMD_STRLEN);
            this.put("incr", StaticContent.CMD_INCR);
            this.put("incrby", StaticContent.CMD_INCR);
            this.put("incrbyfloat", StaticContent.CMD_INCRBYFLOAT);
            this.put("decr", StaticContent.CMD_DECR);
            this.put("decrby", StaticContent.CMD_DECR);
            this.put("mget", StaticContent.CMD_MGET);
            this.put("mset", StaticContent.CMD_MSET);
            this.put("msetnx", StaticContent.CMD_MSETNX);
            this.put("rename", StaticContent.CMD_RENAME);
            this.put("renamenx", StaticContent.CMD_RENAMENX);
            this.put("touch", StaticContent.CMD_TOUCH);
        }
    };

    public static final HashMap<String, Integer> LIST_CMDS = new HashMap<String, Integer>() {
        {
            this.put("lpush", StaticContent.CMD_LPUSH);
            this.put("rpush", StaticContent.CMD_RPUSH);
            this.put("lpushx", StaticContent.CMD_LPUSHX);
            this.put("rpushx", StaticContent.CMD_RPUSHX);
            this.put("lpop", StaticContent.CMD_LPOP);
            this.put("rpop", StaticContent.CMD_RPOP);
            this.put("ltrim", StaticContent.CMD_LTRIM);
            this.put("lset", StaticContent.CMD_LSET);
            this.put("lrem", StaticContent.CMD_LREM);
            this.put("lindex", StaticContent.CMD_LINDEX);
            this.put("lfetch", StaticContent.CMD_LRANGE);
            this.put("lrange", StaticContent.CMD_LRANGE);
            this.put("lcount", StaticContent.CMD_LLEN);
            this.put("llen", StaticContent.CMD_LLEN);
            this.put("blpop", StaticContent.CMD_BLPOP);
            this.put("brpop", StaticContent.CMD_BRPOP);
        }
    };

    public static final HashMap<String, Integer> SET_CMDS = new HashMap<String, Integer>() {
        {
            this.put("sadd", StaticContent.CMD_SADD);
            this.put("srem", StaticContent.CMD_SREM);
            this.put("spop", StaticContent.CMD_SPOP);
            this.put("smembers", StaticContent.CMD_SMEMBERS);
            this.put("sismember", StaticContent.CMD_SISMEMBER);
            this.put("scard", StaticContent.CMD_SCARD);
            this.put("sdiff", StaticContent.CMD_SDIFF);
            this.put("sdiffstore", StaticContent.CMD_SDIFFSTORE);
            this.put("sinter", StaticContent.CMD_SINTER);
            this.put("sinterstore", StaticContent.CMD_SINTERSTORE);
            this.put("sunion", StaticContent.CMD_SUNION);
            this.put("sunionstore", StaticContent.CMD_SUNIONSTORE);
            this.put("sscan", StaticContent.CMD_SSCAN);
            this.put("smove", StaticContent.CMD_SMOVE);
        }
    };

    public static final HashMap<String, Integer> HASH_CMDS = new HashMap<String, Integer>() {
        {
            this.put("hset", StaticContent.CMD_HSET);
            this.put("hmset", StaticContent.CMD_HMSET);
            this.put("hsetnx", StaticContent.CMD_HSETNX);
            this.put("hincrby", StaticContent.CMD_HINCRBY);
            this.put("hincrbyfloat", StaticContent.CMD_HINCRBYFLOAT);
            this.put("hdel", StaticContent.CMD_HDEL);
            this.put("hget", StaticContent.CMD_HGET);
            this.put("hmget", StaticContent.CMD_HMGET);
            this.put("hgetall", StaticContent.CMD_HGETALL);
            this.put("hlen", StaticContent.CMD_HLEN);
            this.put("hkeys", StaticContent.CMD_HKEYS);
            this.put("hvals", StaticContent.CMD_HVALS);
            this.put("hexists", StaticContent.CMD_HEXISTS);
            this.put("hstrlen", StaticContent.CMD_HSTRLEN);
            this.put("hscan", StaticContent.CMD_HSCAN);
        }
    };

    public static final HashMap<String, Integer> ZSET_CMDS = new HashMap<String, Integer>() {
        {
            this.put("zadd", StaticContent.CMD_ZADD);
            this.put("zincrby", StaticContent.CMD_ZINCRBY);
            this.put("zrem", StaticContent.CMD_ZREM);
            this.put("zremrangebylex", StaticContent.CMD_ZREMRANGEBYLEX);
            this.put("zremrangebyrank", StaticContent.CMD_ZREMRANGEBYRANK);
            this.put("zremrangebyscore", StaticContent.CMD_ZREMRANGEBYSCORE);
            this.put("zcard", StaticContent.CMD_ZCARD);
            this.put("zcount", StaticContent.CMD_ZCOUNT);
            this.put("zlexcount", StaticContent.CMD_ZLEXCOUNT);
            this.put("zrange", StaticContent.CMD_ZRANGE);
            this.put("zrangebylex", StaticContent.CMD_ZRANGEBYLEX);
            this.put("zrangebyscore", StaticContent.CMD_ZRANGEBYSCORE);
            this.put("zrevrangebyscore", StaticContent.CMD_ZREVRANGEBYSCORE);
            this.put("zrevrange", StaticContent.CMD_ZREVRANGE);
            this.put("zrevrank", StaticContent.CMD_ZREVRANK);
            this.put("zscore", StaticContent.CMD_ZSCORE);
            this.put("zscan", StaticContent.CMD_ZSCAN);
            this.put("zinter", StaticContent.CMD_ZINTER);
            this.put("zinterstore", StaticContent.CMD_ZINTERSTORE);
            this.put("zunion", StaticContent.CMD_ZUNION);
            this.put("zunionstore", StaticContent.CMD_ZUNIONSTORE);
            this.put("zrank", StaticContent.CMD_ZRANK);
            this.put("zpopmin", StaticContent.CMD_ZPOPMIN);
            this.put("zpopmax", StaticContent.CMD_ZPOPMAX);
            this.put("bzpopmin", StaticContent.CMD_BZPOPMIN);
            this.put("bzpopmax", StaticContent.CMD_BZPOPMAX);
        }
    };

    public static final HashMap<String, Integer> GEO_CMDS = new HashMap<String, Integer>() {
        {
            this.put("geoadd", StaticContent.CMD_GEOADD);
            this.put("geodist", StaticContent.CMD_GEODIST);
            this.put("geopos", StaticContent.CMD_GEOPOS);
            this.put("geohash", StaticContent.CMD_GEOHASH);
            this.put("georadius", StaticContent.CMD_GEORADIUS);
            this.put("georadiusbymember", StaticContent.CMD_GEORADIUSBYMEMBER);
        }
    };

    public static final HashMap<String, Integer> STREAM_CMDS = new HashMap<String, Integer>() {
        {
            this.put("xadd", StaticContent.CMD_XADD);
            this.put("xdel", StaticContent.CMD_XDEL);
            this.put("xtrim", StaticContent.CMD_XTRIM);
            this.put("xlen", StaticContent.CMD_XLEN);
            this.put("xrange", StaticContent.CMD_XRANGE);
            this.put("xrevrange", StaticContent.CMD_XREVRANGE);
            this.put("xread", StaticContent.CMD_XREAD);
            this.put("xgroup", StaticContent.CMD_XGROUP);
            this.put("xreadgroup", StaticContent.CMD_XREADGROUP);
            this.put("xack", StaticContent.CMD_XACK);
            this.put("xpending", StaticContent.CMD_XPENDING);
            this.put("xclaim", StaticContent.CMD_XCLAIM);
            this.put("xinfo", StaticContent.CMD_XINFO);
        }
    };

    public static final HashMap<String, Integer> PUBSUB_CMDS = new HashMap<String, Integer>() {
        {
            this.put("psubscribe", StaticContent.CMD_PSUBSCRIBE);
            this.put("pubsub", StaticContent.CMD_PUBSUB);
            this.put("publish", CMD_PUBLISH);
            this.put("punsubscribe", StaticContent.CMD_PUNSUBSCRIBE);
            this.put("subscribe", StaticContent.CMD_SUBSCRIBE);
            this.put("unsubscribe", StaticContent.CMD_UNSUBSCRIBE);
        }
    };

    public static final HashMap<String, Integer> TRANSACTION_CMDS = new HashMap<String, Integer>() {
        {
            this.put("multi", StaticContent.CMD_MULTI);
            this.put("exec", StaticContent.CMD_EXEC);
            this.put("discard", StaticContent.CMD_DISCARD);
            this.put("watch", StaticContent.CMD_WATCH);
            this.put("unwatch", StaticContent.CMD_UNWATCH);
        }
    };

    public static final HashMap<String, Integer> BLOCKINGKEY_CMDS = new HashMap<String, Integer>() {
        {
            this.put("blpop", CMD_BLPOP);
            this.put("brpop", CMD_BRPOP);
            this.put("bzpopmin", CMD_BZPOPMIN);
            this.put("bzpopmax", CMD_BZPOPMAX);
        }
    };

    public static final HashMap<String, Integer> MULTIKEY_CMDS = new HashMap<String, Integer>() {
        {
            this.put("sinter", CMD_SINTER);
            this.put("sintestorer", CMD_SINTERSTORE);
            this.put("sdiff", CMD_SDIFF);
            this.put("sdiffstore", CMD_SDIFFSTORE);

            this.put("zunion", CMD_ZUNION);
            this.put("zunionstore", CMD_ZUNIONSTORE);
            this.put("zinter", CMD_ZINTER);
            this.put("zinterstore", CMD_ZINTERSTORE);

            this.put("del", CMD_DEL);
            this.put("exists", CMD_EXISTS);
            this.put("mset", CMD_MSET);
            this.put("msetnx", CMD_MSETNX);
            this.put("mget", CMD_MGET);

            this.put("pfmerge", CMD_PFMERGE);
        }
    };

    public static final HashMap<String, Integer> NOKEY_CMDS = new HashMap<String, Integer>() {
        {
            this.put("scan", StaticContent.CMD_SCAN);
            this.put("keys", StaticContent.CMD_KEYS);
            this.put("bgsave", StaticContent.CMD_SAVE);
            this.put("save", StaticContent.CMD_SAVE);
            this.put("client", StaticContent.CMD_CLIENT);
            this.put("cluster", StaticContent.CMD_CLUSTER);
            this.put("command", StaticContent.CMD_COMMAND);
            this.put("time", StaticContent.CMD_TIME);
            this.put("config", StaticContent.CMD_CONFIG);
            this.put("dbsize", StaticContent.CMD_DBSIZE);
            this.put("debug", StaticContent.CMD_DEBUG);
            this.put("info", StaticContent.CMD_INFO);
            this.put("lastsave", StaticContent.CMD_LASTSAVE);
            this.put("monitor", StaticContent.CMD_MONITOR);
            this.put("role", StaticContent.CMD_ROLE);
            this.put("quit", StaticContent.CMD_QUIT);
            this.put("ping", StaticContent.CMD_PING);
            this.put("auth", StaticContent.CMD_AUTH);
            this.put("select", StaticContent.CMD_SELECT);
            this.put("echo", StaticContent.CMD_ECHO);
            this.put("check", StaticContent.CMD_CHECK);
            this.put("idle", StaticContent.CMD_IDLE);
            this.put("heartbeat", StaticContent.CMD_HEARTBEAT);
            this.put("datarescue", StaticContent.CMD_DATARESCUE);
            this.put("wait", StaticContent.CMD_WAIT);
//            if (!ProxyConfig.isDangerousCommandFilter()) {
            this.put("flushdb", StaticContent.CMD_FLUSHDB);
            this.put("flushall", StaticContent.CMD_FLUSHALL);
//            }
            this.put("hello", StaticContent.CMD_HELLO);
            this.put("readonly", StaticContent.CMD_READONLY);
            this.put("slowlog", StaticContent.CMD_SLOWLOG);
        }
    };
    public static final HashMap<String, Integer> SCRIPT_CMDS = new HashMap<String, Integer>() {
        {
            this.put("eval", CMD_EVEL);
            this.put("evalsha", CMD_EVELSHA);
            this.put("script", CMD_SCRIPT);
        }
    };

    public static final HashSet<String> DANGEROUS_CMDS = new HashSet<String>() {
        {
            try {
                String[] cmds = ProxyConfig.getProperty("Server.Common.DangerousCommands").trim().split("[ \t]*,[ \t]*");
                for (String cmd : cmds) {
                    if (cmd != null && cmd.length() > 0) {
                        cmd = cmd.toLowerCase();
                        this.add(cmd);
                        if ("flushdb".equals(cmd)) {
                            this.add("flushall");
                        } else if ("set".equals(cmd)) {
                            this.add("mset");
                        } else if ("get".equals(cmd)) {
                            this.add("mget");
                        }
                    }
                }
            } catch (Throwable t) {
                this.clear();
            }
        }
    };

    // 慢操作告警线
    private final long SLOW_THRESHOLD = ProxyConfig.getLongProperty("Server.Common.SlowOperationThreshold");

    public ProcessLineImp() {

        ServerStartTime = System.currentTimeMillis();
        ServerRescuedTime = System.currentTimeMillis();

        int max_index = 0;
    }

    public boolean synchronize(List argv, DataResult result) throws IOException {

        // 记录收到的请求
        if (logger.isDebug()) {
            StringBuilder buf = new StringBuilder();
            printList(argv, buf);
            logger.debugLog("MessageLineImp::synchronize() Receive message: '{}'", buf);
        }

        int pos = 0;
        Long session = (Long) argv.get(pos++);
        StatusColector.setReceivedSyncSN(session);

        Long timestamp = (Long) argv.get(pos++);
        String cmd = (String) argv.get(pos++);
        int table_id = argv.size() > pos ? ((Long) argv.get(pos++)).intValue() : 0;
        String key = argv.size() > pos ? (String) argv.get(pos++) : null;
        Vector<String> value = new Vector<>();
        if (argv.size() > pos) {
            List list = (List) argv.get(pos++);
            if (list != null && list.size() > 0) {
                for (Object o : list) {
                    value.add(o != null ? o.toString() : null);
                }
            }
        }
        Vector<String> indxs = null;
        if (argv.size() > pos) {
            List list = (List) argv.get(pos++);
            if (list != null && list.size() > 0) {
                indxs = new Vector<>();
                for (Object o : list) {
                    indxs.add(o != null ? o.toString() : null);
                }
            }
        }

        // 初始化返回结果
        result.init(session, argv);

        try {

            if (cmd.startsWith("sync_cmd_")) {
                int cmd_id = (Integer.parseInt(cmd.substring("sync_cmd_".length()))) << 8;
                return process_sync_cmd(timestamp, table_id, cmd_id, key, value, indxs, result);
            } else {// 未定义的命令
                result.setErr(-4, "unknown message");
                if (logger.isInfo()) {
                    StringBuilder buf = new StringBuilder(64);
//                    buf.append("MessageLineImp::process() Unknow message '");
                    boolean isagain = false;
                    for (Object o : argv) {
                        if (isagain) {
                            buf.append(' ');
                        }
                        if (o instanceof byte[]) {
                            buf.append(new String((byte[]) o, StandardCharsets.UTF_8));
                        } else {
                            buf.append(o.toString());
                        }
                        isagain = true;
                    }
//                    buf.append("' received");
                    logger.infoLog("MessageLineImp::synchronize() Unknown message '{}' received.", buf);
                }
            }
        } catch (NumberFormatException nfe) {
            result.setErr(-4, "WRONGTYPE " + nfe.getMessage());
            logger.warnLog("MessageLineImp::process() Format error: '{}' received", nfe.getMessage());
        } catch (IllegalArgumentException iae) {
            result.setErr(-9, "ERR " + iae.getMessage());
            logger.warnLog("MessageLineImp::process() Format error: '{}' received", iae.getMessage());
        } catch (NullPointerException npe) {
            result.setErr(-9, "ERR Error executing '" + cmd + "'");

            StringBuilder buf = new StringBuilder(64);
            printList(argv, buf);
            logger.errorLog(npe, "MessageLineImp::process() A fatal error occur when process '{}': {}", buf, npe.getMessage());
        } catch (Throwable e) {
            result.setErr(-9, e.getMessage());
            logger.warnLog("MessageLineImp::process() error: {}", e);
        }
        // 保持连接,等待下一个请求
        return true;
    }

    @Override
    public boolean process(Object object, DataResult result) throws IOException {
        // 收到命令的时间
        long receive_time = System.currentTimeMillis();
        boolean ret = process(object, receive_time, result);
        if (SLOW_THRESHOLD > 0) {
            long consuming = System.currentTimeMillis() - receive_time;
            if (consuming > SLOW_THRESHOLD) {
                logger.warnLog("MessageLineImp::process() Time consuming {} ms for '{}'", consuming, object);
            }
        }
        return ret;
    }

    private void printList(List list, StringBuilder buf) {
        boolean first = true;
        buf.append('{');
        for (Object o : list) {
            if (!first) {
                buf.append(", ");
            }
            first = false;
            if (o instanceof List) {
                printList((List) o, buf);
            } else {
                buf.append(o);
            }
        }
        buf.append('}');
    }

    private boolean process(Object object, long receive_time, DataResult result) throws IOException {
        if (object == null) {
            logger.debugLog("MessageLineImp::process() Null message received");
            return true;
        }

//        String command = object.toString();

        // 记录收到的请求
//        if (logger.isDebug()) {
//            debuglog("MessageLineImp::process() Receive message: '" + command + "'");
//        }

        // 按空格分解命令
//        Vector<String> argv = splitString(command, ' ', TotalSections);

        Vector argv = (Vector) object;

        if (argv.size() == 0 || argv.get(0) == null) {
            logger.infoLog("MessageLineImp::process() Header is null");
            return true;
        }

        // 记录收到的请求
        if (logger.isDebug()) {
            StringBuilder buf = new StringBuilder();
            printList(argv, buf);
            logger.debugLog("MessageLineImp::process() Receive message: '{}'", buf);
        }

        /**
         * 以下是协议解析接口的处理流程
         */
        if (argv.get(0) instanceof String) {
            String cmd = (String) argv.get(0);
            if ("Exit".equalsIgnoreCase(cmd) || "Quit".equalsIgnoreCase(cmd)) {
                logger.infoLog("MessageLineImp::process() Terminate from client");
                return false;
            } else if ("Check".equalsIgnoreCase(cmd)) {
                result.send("ck " + ServerStartTime
                        + " " + ProxyConfig.getIdentify()
                        + " " + ProxyConfig.getRedisPort());
                logger.infoLog("MessageLineImp::process() Check response");
                return true;
            } else if ("Idle".equalsIgnoreCase(cmd)) {
                result.send("ok");
                logger.debugLog("MessageLineImp::process() Idle response");
                return true;
            } else if ("Heartbeat".equalsIgnoreCase(cmd)) {
                // 心跳
                result.send("hb " + Long.toString(ServerRescuedTime));
                logger.infoLog("MessageLineImp::process() Heartbeat response");
                return true;
            } else if ("Config".equalsIgnoreCase(cmd)) {
                result.send("ok " + ProxyConfig.getRedisPort());
                logger.infoLog("MessageLineImp::process() Config response");
                return true;
            } else if ("Dump".equalsIgnoreCase(cmd)) {
//                return process_dumpAll(result);
            } else if ("Auth".equalsIgnoreCase(cmd)) {
                logger.infoLog("MessageLineImp::process() Auth received.");
                return true;
            } else if ("FlushDB".equalsIgnoreCase(cmd)) {
                String sid = null;
                try {
                    for (int i = 1; i < argv.size(); ++i) {
                        sid = (String) argv.get(i);
                        int id = (int) Long.parseLong(sid);
//                        Servers[id].clear(false);
                    }
                    result.setOk();
                } catch (Throwable t) {
                    result.setErr(-1, "truncate table-" + sid + " failed: " + t.getMessage());
                }
                return true;
            } else if (StaticContent.STOPING_STRING.equalsIgnoreCase(cmd)) {
                ChannelHandlerContext s = result.getSession();
                SocketAddress socketAddress = null;
//                if (s instanceof IoSession) {
//                    socketAddress = ((IoSession) s).getRemoteAddress();
//                } else if (s instanceof ChannelHandlerContext) {
                socketAddress = s.channel().remoteAddress();
//                }
                InetAddress remote = null;
                if (socketAddress != null && socketAddress instanceof InetSocketAddress) {
                    remote = ((InetSocketAddress) socketAddress).getAddress();
                }
                Set localIps = StaticContent.getLocalIpAddress();
                if (remote != null && remote.isLoopbackAddress()) {
                    String pid = "0";
                    try {
                        String name = ManagementFactory.getRuntimeMXBean().getName();
                        pid = name.split("@")[0];
                    } catch (Throwable t) {
                    }
                    if (localIps.contains(remote)) {
                        result.send("0");
                        logger.warnLog("MessageLineImp::process() Server shutdown by '{}'", socketAddress);
                        System.exit(0);
                    } else {
                        result.send(pid);
                        logger.coreLog("MessageLineImp::process() Unacceptable shutdown command from '{}'"
                                , socketAddress);
                    }
                }
                return true;
            }

            if (cmd.length() > 10) {
                cmd = cmd.substring(0, 7) + "...";
            }
            logger.infoLog("MessageLineImp::process() Unknow message '{}' received.", cmd);
            result.setErr(-1, "unknown message '" + cmd + "'");
            return true;
        }

        // 命令格式非法
        if (argv.size() < 2 || argv.get(0) == null || argv.get(1) == null) {
            if (logger.isInfo()) {
                StringBuilder buf = new StringBuilder();
                printList(argv, buf);
                logger.infoLog("MessageLineImp::process() Invalid message '{}' received", buf.toString());
            }
            result.setErr(-1, "unknow format");
            return true;
        }

        int pos = 0;

        Long session = (Long) argv.get(pos++);
        String cmd = (String) argv.get(pos++);
        int table_id = argv.size() > pos ? ((Long) argv.get(pos++)).intValue() : 0;
        String key = argv.size() > pos ? (String) argv.get(pos++) : null;
        Vector<String> value = new Vector<>();
        if (argv.size() > pos) {
            List list = (List) argv.get(pos++);
            if (list != null && list.size() > 0) {
                for (Object o : list) {
                    value.add(o != null ? o.toString() : null);
                }
            }
        }
        Vector<String> indxs = null;
        if (argv.size() > pos) {
            List list = (List) argv.get(pos++);
            if (list != null && list.size() > 0) {
                indxs = new Vector<>();
                for (Object o : list) {
                    indxs.add(o != null ? o.toString() : null);
                }
            }
        }

        // 初始化返回结果
        result.init(session, argv);

        boolean is_datarescue = "datarescue".equals(cmd);

        if (table_id < 0 || (table_id == 0 && !is_datarescue)) {
            if (is_datarescue) {
                // datarescue命令时，如果对端配置不一样，可能会收到table_id越界的命令
                result.send("rescue error: invalid table-" + table_id);
            } else {
                if (logger.isInfo()) {
                    logger.infoLog("MessageLineImp::process() Table-{} is inexistence.", table_id);
                }
                result.setErr(-5, "invalid table_id");
            }
            return true;
        }

        try {
            if(is_datarescue){
                return process_rescue(table_id, key, result);
            } else {// 未定义的命令
                result.setErr(-4, "unknown message");
                if (logger.isInfo()) {
                    StringBuilder buf = new StringBuilder(64);
//                    buf.append("MessageLineImp::process() Unknow message '");
                    boolean isagain = false;
                    for (Object o : argv) {
                        if (isagain) {
                            buf.append(' ');
                        }
                        buf.append(o.toString());
                        isagain = true;
                    }
//                    buf.append("' received");
                    logger.infoLog("MessageLineImp::process() Unknown message '{}' received.", buf);
                }
            }
        } catch (NumberFormatException nfe) {
            result.setErr(-4, "WRONGTYPE " + nfe.getMessage());
            logger.warnLog("MessageLineImp::process() Format error: '{}' received", nfe.getMessage());
        } catch (IllegalArgumentException iae) {
            result.setErr(-9, "ERR " + iae.getMessage());
            logger.warnLog("MessageLineImp::process() Format error: '{}' received", iae.getMessage());
        } catch (NullPointerException npe) {
            result.setErr(-9, "ERR Error executing '" + cmd + "'");

            StringBuilder buf = new StringBuilder(64);
//            buf.append("MessageLineImp::process() A fatal error occur when process '");
            boolean isagain = false;
            for (Object o : argv) {
                if (isagain) {
                    buf.append(' ');
                }
                buf.append(o);
                isagain = true;
            }
//            buf.append('\'');
//            buf.append(": ").append(npe.getMessage());
            logger.errorLog(npe, "MessageLineImp::process() A fatal error occur when process '{}': {}", buf, npe.getMessage());
        } catch (AclFailedException afe) {
            result.setErr(-9, "ACL Failed");
        } catch (LuaError luaError) {
            result.setErr(-9, "ERR lua error: " + luaError.getMessage());
            logger.warnLog("MessageLineImp::process() lua failed: {}", luaError);
        } catch (Error err) {
            result.setErr(-9, "-ERR jvm error: " + err);
            logger.errorLog("MessageLineImp::process() Error occur: {}.", err);
        } catch (Throwable e) {
            result.setErr(-9, e.getMessage());
            logger.warnLog("MessageLineImp::process() error: {}", e);
        }

            // 保持连接,等待下一个请求
        return true;
    }

    private boolean process_sync_cmd(long time, int table_id, int cmd_id, String key, Vector<String> value, Vector<String> indxs, DataResult result)
            throws Exception {
        try {
            if (table_id == 0) {
                switch (cmd_id) {
                    case CMD_PUBLISH:
                        ArrayList<byte[]> values = new ArrayList<>();
                        for (String v : value) {
                            if (v != null) {
                                values.add(accept(v, 0, v.length()));
                            }
                        }
                        PSManager.process(cmd_id, accept(key, 0, key.length()), values, null, true);
                        break;
                    case CMD_SCRIPT_LOAD:
                        String str_script = value.get(0);
                        LuaManager.synchronize_load(key, accept(str_script, 0, str_script.length()));
                        break;
                    case CMD_SCRIPT_FLUSH:
                        LuaManager.synchronize_flush(key);
                        break;
                    default:
                        logger.warnLog("MessageLineImp::process_sync_cmd() Unknown command: {}", cmd_id);
                }
            } else {
                logger.warnLog("MessageLineImp::process_sync_cmd() Unsupported table: {}", table_id);
            }
            return true;
        } finally {
            result.setOk();
        }
    }

    private boolean process_rescue(int table_id, String ranges, DataResult result) {
        try {
            if (table_id <= 0) {
                final StringBuilder buf = new StringBuilder();
                try {
                    LuaManager.serialize(data -> {
                        buf.setLength(0);
                        buf.append('0').append(' ');
                        buf.append('0').append(' ');
                        buf.append(LuaManager.LUA_SAVEKEY).append(' ');
                        buf.append(BinaryStringUtil.getString(data, true));
                        try {
                            result.send(buf.toString()).sync();
                        } catch (Exception e) {
                            logger.warnLog("MessageLineImp::process_rescue() Rescue Lua failed: {}", e);
                        }
                    });
                } catch (Throwable t) {
                    logger.warnLog("MessageLineImp::process_rescue() Rescue Lua failed: {}", t);
                }
//                try {
//                    buf.setLength(0);
//                    buf.append('0').append(' ');
//                    buf.append('0').append(' ');
//                    buf.append(XGroupManager.XGROUP_SAVEKEY).append(' ');
//                    byte[] data = XGroupManager.serialize();
//                    buf.append(BinaryStringUtil.getString(data, true));
//                    result.send(buf.toString()).sync();
//                } catch (Throwable t) {
//                    logger.warnLog("MessageLineImp::process_rescue() Rescue XGroup failed: {}", t);
//                }
            } else {
                result.send("rescue end");
            }
        } catch (Exception e) {
//            if (logger.isDebug()) {
//                e.printStackTrace();
//            }
            if (logger.isInfo()) {
                logger.warnLog(e, "MessageLineImp::process_rescue() Rescue table-{} in range [{}] failed: {}", table_id, ranges, e.getMessage());
            } else {
                logger.warnLog("MessageLineImp::process_rescue() Rescue table-{} failed: {}", table_id, e);
            }
            return false;
        }
        return true;
    }
}
