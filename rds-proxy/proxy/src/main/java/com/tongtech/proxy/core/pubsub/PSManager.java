package com.tongtech.proxy.core.pubsub;

import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.StringMatcher;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.RdsString;
import com.tongtech.proxy.core.sync.SyncManager;
import com.tongtech.proxy.core.sync.SyncSender;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.tongtech.proxy.core.StaticContent.*;

public class PSManager {
    private final static Log logger = ProxyConfig.getServerLog();

    private final static byte[] EXPIRED_CMD = "expired".getBytes();
    private final static byte[] EVICTED_CMD = "evicted".getBytes();

    // 由CMD_SUBSCRIBE命令明确订阅指定消息的客户端
    private static ConcurrentHashMap<RdsString, ArrayList<DataResult>> SUBSCRIBES = new ConcurrentHashMap<>();

    // 由CMD_PSUBSCRIBE命令指定模糊匹配消息的客户端
    private static ConcurrentHashMap<RdsString, ArrayList<DataResult>> PATTERN_SUBSCRIBES = new ConcurrentHashMap<>();

    // 每个客户端订阅的消息列表
    private static ConcurrentHashMap<DataResult, HashSet<RdsString>> RESULTS = new ConcurrentHashMap<>();

    private static final SyncManager SYNC_MANAGER = SyncManager.getInstance();

    private final static boolean KeySpace;
    private final static boolean KeyEvent;
    private final static boolean EventEnable;

    private final static boolean EventExpired;
    private final static boolean EventGeneric;
    private final static boolean EventString;
    private final static boolean EventList;
    private final static boolean EventSet;
    private final static boolean EventHash;
    private final static boolean EventZset;
    private final static boolean EventEvict;

    static {
        boolean space = false, event = false;
        try {
            String[] notifys = ProxyConfig.getProperty("Server.Notify.Event").trim().split(" *, *");
            for (String notify : notifys) {
                if ("keyspace".equalsIgnoreCase(notify)) {
                    space = true;
                    logger.infoLog("PSManager::init() KeySpace is enabled");
                } else if ("keyevent".equalsIgnoreCase(notify)) {
                    event = true;
                    logger.infoLog("PSManager::init() KeyEvent is enabled");
                }
            }
        } catch (Throwable t) {
        }
        KeySpace = space;
        KeyEvent = event;
        EventEnable = space || event;

        if (EventEnable) {
            logger.warnLog("PSManager::init() Event listening has been enabled");
        }

        boolean expired = false, generic = false, string = false, list = false, set = false, hash = false, zset = false, evict = false;
        boolean cmd = false, all = false;
        if (EventEnable) {
            try {
                String[] notifys = ProxyConfig.getProperty("Server.Notify.Message").trim().split(" *, *");
                for (String notify : notifys) {
                    if ("expired".equalsIgnoreCase(notify)) {
                        expired = true;
                        logger.infoLog("PSManager::init() event expired is enabled");
                    } else if ("generic".equalsIgnoreCase(notify)) {
                        generic = true;
                        logger.infoLog("PSManager::init() event generic is enabled");
                    } else if ("string".equalsIgnoreCase(notify)) {
                        string = true;
                        logger.infoLog("PSManager::init() event string is enabled");
                    } else if ("list".equalsIgnoreCase(notify)) {
                        list = true;
                        logger.infoLog("PSManager::init() event list is enabled");
                    } else if ("set".equalsIgnoreCase(notify)) {
                        set = true;
                        logger.infoLog("PSManager::init() event set is enabled");
                    } else if ("hash".equalsIgnoreCase(notify)) {
                        hash = true;
                        logger.infoLog("PSManager::init() event hash is enabled");
                    } else if ("zset".equalsIgnoreCase(notify)) {
                        zset = true;
                        logger.infoLog("PSManager::init() event zset is enabled");
                    } else if ("cmds".equalsIgnoreCase(notify)) {
                        cmd = true;
                        logger.infoLog("PSManager::init() events all commands are enabled");
                    } else if ("all".equalsIgnoreCase(notify)) {
                        all = true;
                        logger.infoLog("PSManager::init() events are all enabled");
                    }
                }
            } catch (Throwable t) {
            }
            if (cmd) {
                // 所有写命令事件（不包括过期事件）
                generic = string = list = set = hash = zset = evict = true;
            }
            if (all) {
                // 所有事件
                expired = generic = string = list = set = hash = zset = evict = true;
            }
        } else {
            logger.debugLog("PSManager::init() Keyspace notification is closed");
        }

        EventExpired = expired;
        EventGeneric = generic;
        EventString = string;
        EventList = list;
        EventSet = set;
        EventHash = hash;
        EventZset = zset;
        EventEvict = evict;

    }

    public static boolean isEventKeySpace() {
        return KeySpace;
    }

    public static boolean isEventKeyEvent() {
        return KeyEvent;
    }

    public static boolean isEventEnabled() {
        return EventEnable;
    }

    public static boolean isEventExpired() {
        return EventExpired;
    }

    public static boolean isEventGeneric() {
        return EventGeneric;
    }

    public static boolean isEventString() {
        return EventString;
    }

    public static boolean isEventList() {
        return EventList;
    }

    public static boolean isEventSet() {
        return EventSet;
    }

    public static boolean isEventHash() {
        return EventHash;
    }

    public static boolean isEventZset() {
        return EventZset;
    }

    public static boolean isEventEvict() {
        return EventEvict;
    }

//    public static void setExpireEvent(int db, byte[] key, long expire) {
//        if (Monitor != null) {
//            Monitor.add(db, key, expire);
//        }
//    }
//
//    public static void delExpireEvent(int db, byte[] key) {
//        if (Monitor != null) {
//            Monitor.delete(db, key);
//        }
//    }

//    /**
//     * 记录总共过期了多少个key
//     */
//    private static void addExpiredKyes() {
//        long next = ExpiredKeys + 1;
//        ExpiredKeys = next;
//    }
//
//    /**
//     * 取过期key的总数（开通Expired时间后才有数据）
//     *
//     * @return
//     */
//    public static long getExpiredKeys() {
//        return ExpiredKeys;
//    }
//
//    /**
//     * 记录总共过期了多少个key
//     */
//    private static void addEvictedKyes() {
//        long next = EvictedKeys + 1;
//        EvictedKeys = next;
//    }

//    /**
//     * 取过期key的总数（开通Expired时间后才有数据）
//     *
//     * @return
//     */
//    public static long getEvictedKeys() {
//        return EvictedKeys;
//    }

    public static void eventPublish(int db, byte[] cmd, byte[] bk) {
        byte[] bspace = ("__keyspace@" + db + "__:").getBytes(StandardCharsets.UTF_8);
        byte[] bevent = ("__keyevent@" + db + "__:").getBytes(StandardCharsets.UTF_8);

        if (bk == null) {
            throw new IllegalArgumentException("eventPublish failed: key is null");
        }
        byte[] nk = new byte[bspace.length + bk.length];
        System.arraycopy(bspace, 0, nk, 0, bspace.length);
        System.arraycopy(bk, 0, nk, bspace.length, bk.length);

        byte[] ne = new byte[bevent.length + cmd.length];
        System.arraycopy(bevent, 0, ne, 0, bevent.length);
        System.arraycopy(cmd, 0, ne, bevent.length, cmd.length);

        if (KeySpace) {
            publish(nk, cmd, null, false);
        }

        if (KeyEvent) {
            publish(ne, bk, null, false);
        }
    }

    /**
     * Session关闭时调用此方法，取消已关闭的连接订阅的消息
     *
     * @param result
     */
    public static void sessionClosed(DataResult result) {
        if (result != null) {
            try {
                HashSet<RdsString> keys = RESULTS.get(result);
                if (keys != null) {
                    RdsString[] ks = keys.toArray(new RdsString[0]);
                    for (RdsString key : ks) {
                        unsubscribe(result, key.message());
                        punsubscribe(result, key.message());
                    }
                    RESULTS.remove(result);
                    logger.infoLog("PSManager::sessionClose() {} closed.", result);
                }
            } catch (Throwable t) {
                logger.warnLog(t, "PSManager::sessionClose() error occur: {}", t);
            }
        }
    }

    public static void process(int cmd, byte[] key, List<byte[]> value, DataResult result, boolean isSync) {
        switch (cmd) {
            case CMD_PUBLISH:
                if (value != null && value.size() > 0) {
                    publish(key, value.get(0), result, isSync);
                } else {
                    try {
                        result.setErr(-9, "ERR wrong number of arguments for 'publish' command");
                    } catch (Exception e) {
                    }
                }
                break;
            case CMD_SUBSCRIBE:
                subscribe(result, key);
                if (value != null && value.size() > 0) {
                    for (byte[] v : value) {
                        subscribe(result, v);
                    }
                }
                break;
            case CMD_UNSUBSCRIBE:
                unsubscribe(result, key);
                if (value != null && value.size() > 0) {
                    for (byte[] v : value) {
                        unsubscribe(result, v);
                    }
                }
                break;
            case CMD_PSUBSCRIBE:
                psubscribe(result, key);
                if (value != null && value.size() > 0) {
                    for (byte[] v : value) {
                        psubscribe(result, v);
                    }
                }
                break;
            case CMD_PUNSUBSCRIBE:
                punsubscribe(result, key);
                if (value != null && value.size() > 0) {
                    for (byte[] v : value) {
                        punsubscribe(result, v);
                    }
                }
                break;
            case CMD_PUBSUB:
                pubsub(result, RdsString.getString(key), value);
                break;
        }
    }

    private synchronized static int getSubSize(DataResult result) {
        HashSet<RdsString> subs = RESULTS.get(result);
        return subs != null ? subs.size() : 0;
    }

    private static void subscribe(DataResult result, byte[] key) {
        try {
            RdsString skey= RdsString.getString(key);
            HashSet<RdsString> keys = RESULTS.get(result);
            if (keys == null) {
                keys = new HashSet<>();
                RESULTS.put(result, keys);
            }
            if (!keys.contains(skey)) {
                keys.add(skey);
                ArrayList<DataResult> results;
                results = SUBSCRIBES.get(skey);
                if (results == null) {
                    results = new ArrayList<>();
                    synchronized (results) {
                        results.add(result);
                        SUBSCRIBES.putIfAbsent(skey, results);
                    }
                }

                synchronized (results) {
                    if (!results.contains(result)) {
                        results.add(result);
                        logger.infoLog("PSManager::Subscribe() Subscribe {} for {}", skey, result);
                    }
                }
            }
            ArrayList ret = new ArrayList();
            ret.add("subscribe");
            ret.add(key);
            ret.add(getSubSize(result));
            result.sendObject(ret);
        } catch (Throwable e) {
            logger.warnLog(e, "PSManager::Subscribe() error occur: {}", e);
        }
    }

    private synchronized static void unsubscribe(DataResult result, byte[] key) {
        try {
            RdsString skey= RdsString.getString(key);
            HashSet<RdsString> keys = RESULTS.get(result);
             if (keys != null && keys.contains(skey)) {
                ArrayList<DataResult> results = SUBSCRIBES.get(skey);
                if (results != null) {
                    synchronized (results) {
                        if (results.remove(result)) {
                            keys.remove(skey);
                            logger.infoLog("PSManager::Unsubscribe() Unsubscribe {} for {}", skey, results);
                        }
                        if (results.size() == 0) {
                            SUBSCRIBES.remove(skey);
                        }
                    }
                }
            }

            if (result.isActive()) {
                ArrayList ret = new ArrayList();
                ret.add("unsubscribe");
                ret.add(key);
                ret.add(getSubSize(result));
                result.sendObject(ret);
            }
        } catch (Throwable e) {
            logger.warnLog(e, "PSManager::Unsubscribe() error occur: {}", e);
        }
    }

    private synchronized static void psubscribe(DataResult result, byte[] key) {
        try {
            RdsString skey= RdsString.getString(key);
            HashSet<RdsString> keys = RESULTS.get(result);
            if (keys == null) {
                keys = new HashSet<>();
                RESULTS.put(result, keys);
            }
            if (!keys.contains(skey)) {
                keys.add(skey);
                ArrayList<DataResult> results;
                results = PATTERN_SUBSCRIBES.get(skey);
                if (results == null) {
                    results = new ArrayList<>();
                    synchronized (results) {
                        results.add(result);
                        PATTERN_SUBSCRIBES.put(skey, results);
                    }
//                    String match = key.replaceAll("\\*", "[\\\\s\\\\S]*");
//                    match = match.replaceAll("\\?", "\\.");
//                    PATTERN_CONTRAST.put(key, match);
                }
                synchronized (results) {
                    if (!results.contains(result)) {
                        results.add(result);
                        logger.infoLog("PSManager::Psubscribe() Psubscribe {} for {}", skey, results);
                    }
                }
            }
            ArrayList ret = new ArrayList();
            ret.add("psubscribe".getBytes(StandardCharsets.UTF_8));
            ret.add(key);
            ret.add(getSubSize(result));
            result.sendObject(ret);
        } catch (Throwable e) {
            logger.warnLog(e, "PSManager::Psubscribe() error occur: {}", e);
        }
    }

    private synchronized static void punsubscribe(DataResult result, byte[] key) {
        try {
            RdsString skey= RdsString.getString(key);
            HashSet<RdsString> keys = RESULTS.get(result);
            if (keys != null && keys.contains(skey)) {
                keys.remove(skey);
                ArrayList<DataResult> results = PATTERN_SUBSCRIBES.get(skey);
                if (results != null) {
                    synchronized (results) {
                        if (results.remove(result)) {
                            logger.infoLog("PSManager::Punsubscribe() Punsubscribe {} for {}", skey, results);
                        }
                        if (results.size() == 0) {
                            PATTERN_SUBSCRIBES.remove(skey);
                        }
                    }
                }
                if (result.isActive()) {
                    ArrayList ret = new ArrayList();
                    ret.add("punsubscribe");
                    ret.add(key);
                    ret.add(getSubSize(result));
                    result.sendObject(ret);
                }
            }
        } catch (Throwable e) {
            logger.warnLog(e, "PSManager::Punsubscribe() error occur: {}", e);
        }
    }

    private static void publish(byte[] key, byte[] value, DataResult result, boolean isSync) {
        // 同步数据
        if (!isSync && SYNC_MANAGER != null) {
            try {
                SyncSender s_server = SYNC_MANAGER.getSyncServer(hashBytes(key));
                if (s_server != null) {
                    // 如果同步列表插入失败会抛出异常,终止后续的set操作
                    s_server.putData(SYNC_CMD | CMD_PUBLISH, escape(key, 0, key.length), new ArrayList<String>() {{
                        this.add(escape(value, 0, value.length));
                    }}, 1, null);
                    logger.debugLog("PSManager::publish() synchronized message {} = {} ok.", key, value);
                }
            } catch (IllegalArgumentException ia) {
                logger.warnLog("PSManager::publish() synchronized message error: {}", ia.getMessage());
            } catch (Throwable t) {
                logger.warnLog(t, "PSManager::publish() synchronized message error: {}", t);
            }
        }

//        if (Arrays.equals(EXPIRED_CMD, key)) {
//            addExpiredKyes();
//        } else if (Arrays.equals(EVICTED_CMD, key)) {
//            addEvictedKyes();
//        }

        int send = 0;

        // subscribe
        ArrayList<DataResult> results;
        RdsString skey= RdsString.getString(key);
        results = SUBSCRIBES.get(skey);
        if (results != null) {
            ArrayList ret = new ArrayList();
            ret.add("message".getBytes(StandardCharsets.UTF_8));
            ret.add(key);
            ret.add(value);
            synchronized (results) {
                for (DataResult res : results) {
                    try {
                        res.sendObject(ret);
                        send++;
                        logger.debugLog("PSManager::publish() send message '{}' for {} to {} ok."
                                , value, skey, res);
                    } catch (Throwable t) {
//                        unsubscribe(res, key);
                        logger.warnLog("PSManager::publish() send message '{}' for {} to {} failed: {}."
                                , value, skey, res, t);
                    }
                }
            }
        }

        // psubscribe
        for (RdsString k : PATTERN_SUBSCRIBES.keySet()) {
//            String match = PATTERN_CONTRAST.get(k);
            byte[] match = k.message();
//            if (BinaryStringUtil.stringMatchString(key, match, false) /* key.matches(match) */) {
            if (StringMatcher.stringmatchlen(match, 0, key, 0, false)) {
                results = PATTERN_SUBSCRIBES.get(k);
                if (results != null) {
                    synchronized (results) {
                        for (DataResult res : results) {
                            try {
                                ArrayList ret = new ArrayList();
                                ret.add("pmessage".getBytes(StandardCharsets.UTF_8));
                                ret.add(k.message());
                                ret.add(key);
                                ret.add(value);
                                res.sendObject(ret);
                                send++;
                            } catch (Throwable t) {
//                                unsubscribe(res, key);
                            }
                        }
                    }
                }
            }
        }

        if (result != null) {
            try {
                result.sendObject(new Integer(send));
                logger.debugLog("PSManager::publish() send message to {} clients", send);
            } catch (IOException e) {
                logger.warnLog(e, "PSManager::publish() error occur: {}", e);
            }
        }
        logger.infoLog("PSManager::publish() publish message {} ok.", skey);
    }

    public static synchronized void pubsub(DataResult result, RdsString subcommand, List<byte[]> value) {
        try {
            if ("CHANNELS".equalsIgnoreCase(subcommand.toString())) {
                byte[] match = getFromBytesList(value, 0);
                if (match.length == 1 && match[0] == '*') {
                    match = null;
                }
                Vector<byte[]> data = new Vector<>();
                for (RdsString channel : SUBSCRIBES.keySet()) {
                    if (match == null || channel.matches(match, false)) {
                        data.add(channel.message());
                    }
                }
                result.sendObject(data);
            } else if ("NUMSUB".equalsIgnoreCase(subcommand.toString())) {
                Vector data = new Vector();
                for (byte[] channel : value) {
                    data.add(channel);
                    List ret = SUBSCRIBES.get(channel);
                    if (ret != null) {
                        data.add(ret.size());
                    } else {
                        data.add(0);
                    }
                }
                result.sendObject(data);
            } else if ("NUMPAT".equalsIgnoreCase(subcommand.toString())) {
                int size = 0;
                for (List pchans : PATTERN_SUBSCRIBES.values()) {
                    if (pchans != null) {
                        size += pchans.size();
                    }
                }
                result.setOk(size);
            } else {
                result.setErr(-9, "ERR Unknown PUBSUB subcommand or wrong number of arguments for '" + subcommand + "'");
            }
        } catch (Throwable t) {
            logger.warnLog("PSManager::process() error pubsub: {}", t);
        }
    }
}
