package com.tongtech.proxy.core.protocol;

import java.util.*;

public class Commands {
    private final static StatusString READONLY = new StatusString("+readonly");
    private final static StatusString WRITE = new StatusString("+write");
    private final static StatusString DENYOOM = new StatusString("+denyoom");
    private final static StatusString FAST = new StatusString("+fast");
    private final static StatusString RANDOM = new StatusString("+random");
    private final static StatusString PUBSUB = new StatusString("+pubsub");
    private final static StatusString NOSCRIPT = new StatusString("+noscript");
    private final static StatusString LOADING = new StatusString("+loading");
    private final static StatusString STALE = new StatusString("+stale");
    private final static StatusString SORT_FOR_SCRIPT = new StatusString("+sort_for_script");
    private final static StatusString MOVABLEKEYS = new StatusString("+movablekeys");
    private final static StatusString ADMIN = new StatusString("+admin");
    private final static StatusString SKIP_MONITOR = new StatusString("+skip_monitor");

    public final static HashMap<String, List> COMMANDS = new HashMap<String, List>(256);

    public static final ArrayList<String> ALL_COMMANDS = new ArrayList<String>();

    public final static HashSet<String> WRITABLE_COMMANDS = new HashSet<String>(100);

    public final static HashSet<String> READONLY_COMMANDS = new HashSet<String>(100);

    public final static HashMap<String, List<String>> GROUPS = new HashMap<String, List<String>>(256) {
        {
            this.put("@all", ALL_COMMANDS);
        }
    };

    public final static HashMap<String, boolean[]> BINARY_ARGS = new HashMap<String, boolean[]>(256);

    static {
//        Version 5.0
//        add("rpoplpush", 3, 1, 2, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("hdel", -3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("hstrlen", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        //add("geopos",-2,1,1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        add("sadd", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        //add("setbit",4,1,1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        add("hmset", -4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        //add("bitpos",-3,1,1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        //add("bgrewriteaof",1,0,0,0,false,false,false,false,false,false,true,false,false,false,false,true,false);
//        //add("xreadgroup",-7,1,1,1,false,true,false,false,false,false,true,false,false,false,true,false,false);
//        //add("bitcount",-2,1,1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        add("unsubscribe", -1, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
//        add("incrby", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        // add("unwatch",1,0,0,0,false,false,false,true,false,false,true,false,false,false,false,false,false);
//        add("hset", -4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        // add("restore",-4,1,1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        add("zincrby", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("psetex", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        // add("restore-asking",-4,1,1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        add("smembers", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
//        // add("replicaof",3,0,0,0,false,false,false,false,false,false,true,false,true,false,false,true,false);
//        add("incr", 2, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        // add("blpop",-3,1,-2,1,false,true,false,false,false,false,true,false,false,false,false,false,false);
//        add("hgetall", 2, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
//        // add("georadius",-6,1,1,1,false,true,false,false,false,false,false,false,false,false,true,false,false);
//        add("scan", -2, 0, 0, 0, true, false, false, false, true, false, false, false, false, false, false, false, false);
//        add("hlen", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        //add("migrate",-6,0,0,0,false,true,false,false,true,false,false,false,false,false,true,false,false);
//        add("lset", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        // add("xread",-4,1,1,1,true,false,false,false,false,false,true,false,false,false,true,false,false);
//        //add("pfselftest",1,0,0,0,false,false,false,false,false,false,false,false,false,false,false,true,false);
//        add("hvals", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
//        add("zcount", 4, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        //add("swapdb",3,0,0,0,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        add("sinterstore", -3, 1, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("mget", -2, 1, -1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("time", 1, 0, 0, 0, false, false, false, true, true, false, false, false, false, false, false, false, false);
//        // add("sync",1,0,0,0,true,false,false,false,false,false,true,false,false,false,false,true,false);
//        add("zremrangebyscore", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
//        //add("xadd",-5,1,1,1,false,true,true,true,true,false,false,false,false,false,false,false,false);
//        // add("module",-2,0,0,0,false,false,false,false,false,false,true,false,false,false,false,true,false);
//        // add("psync",3,0,0,0,true,false,false,false,false,false,true,false,false,false,false,true,false);
//        add("script", -2, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, false, false);
//        // add("move",3,1,1,1,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        add("hscan", -3, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
//        // add("readwrite",1,0,0,0,false,false,false,true,false,false,false,false,false,false,false,false,false);
//        add("punsubscribe", -1, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
//        // add("xrevrange",-4,1,1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        // add("flushall",-1,0,0,0,false,true,false,false,false,false,false,false,false,false,false,false,false);
//        // add("slowlog",-2,0,0,0,false,false,false,false,true,false,false,false,false,false,false,true,false);
//        // add("object",-2,2,2,1,true,false,false,false,true,false,false,false,false,false,false,false,false);
//        add("sort", -2, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, true, false, false);
//        // add("flushdb",-1,0,0,0,false,true,false,false,false,false,false,false,false,false,false,false,false);
//        // add("bzpopmin",-3,1,-2,1,false,true,false,true,false,false,true,false,false,false,false,false,false);
//        add("zrevrangebyscore", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        // add("unlink",-2,1,-1,1,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        //  add("role",1,0,0,0,false,false,false,false,false,false,true,true,true,false,false,false,false);
//        //  add("post",-1,0,0,0,false,false,false,false,false,false,false,true,true,false,false,false,false);
//        add("scard", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        // add("multi",1,0,0,0,false,false,false,true,false,false,true,false,false,false,false,false,false);
//        // add("xinfo",-2,2,2,1,true,false,false,false,true,false,false,false,false,false,false,false,false);
//        //  add("wait",3,0,0,0,false,false,false,false,false,false,true,false,false,false,false,false,false);
//        // add("latency",-2,0,0,0,false,false,false,false,false,false,true,true,true,false,false,true,false);
//        //  add("xgroup",-2,2,2,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        add("sdiff", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
//        //  add("brpoplpush",4,1,2,1,false,true,true,false,false,false,true,false,false,false,false,false,false);
//        add("config", -2, 0, 0, 0, false, false, false, false, false, false, true, true, true, false, false, true, false);
//        add("sunionstore", -3, 1, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        //   add("xrange",-4,1,1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        //  add("zpopmax",-2,1,1,1,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        add("zrange", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        //  add("discard",1,0,0,0,false,false,false,true,false,false,true,false,false,false,false,false,false);
//        add("incrbyfloat", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("sscan", -3, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
//        //  add("exec",1,0,0,0,false,false,false,false,false,false,true,false,false,false,false,false,true);
//        add("zremrangebyrank", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
//        add("substr", 4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        add("zrank", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("hkeys", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
//        add("hmget", -3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("sismember", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("dump", 2, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
//        add("keys", 2, 0, 0, 0, true, false, false, false, false, false, false, false, false, true, false, false, false);
//        // add("xlen",2,1,1,1,true,false,false,true,false,false,false,false,false,false,false,false,false);
//        //  add("replconf",-1,0,0,0,false,false,false,false,false,false,true,true,true,false,false,true,false);
//        //  add("touch",-2,1,1,1,true,false,false,true,false,false,false,false,false,false,false,false,false);
//        add("type", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        // add("monitor",1,0,0,0,false,false,false,false,false,false,true,false,false,false,false,true,false);
//        add("publish", 3, 0, 0, 0, false, false, false, true, false, true, false, true, true, false, false, false, false);
//        add("zinterstore", -4, 0, 0, 0, false, true, true, false, false, false, false, false, false, false, true, false, false);
//        add("georadiusbymember", -5, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, true, false, false);
//        add("llen", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("get", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("del", -2, 1, -1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
//        add("lpop", 2, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("zscan", -3, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
//        //  add("xack",-4,1,1,1,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        add("sinter", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
//        //  add("xpending",-3,1,1,1,true,false,false,false,true,false,false,false,false,false,false,false,false);
//        //  add("setrange",4,1,1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        add("pexpire", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("lrange", 4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        add("hexists", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("decrby", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("zrem", -3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("zcard", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("hincrbyfloat", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        //  add("bitop",-4,2,-1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        add("zscore", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("ping", -1, 0, 0, 0, false, false, false, true, false, false, false, false, true, false, false, false, false);
//        add("append", 3, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("echo", 2, 0, 0, 0, false, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("srandmember", -2, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
//        // add("persist",2,1,1,1,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        add("client", -2, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, true, false);
//        // add("readonly",1,0,0,0,false,false,false,true,false,false,false,false,false,false,false,false,false);
//        add("zremrangebylex", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
//        //  add("pfadd",-2,1,1,1,false,true,true,true,false,false,false,false,false,false,false,false,false);
//        add("lrem", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
//        add("rpushx", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("ttl", 2, 1, 1, 1, true, false, false, true, true, false, false, false, false, false, false, false, false);
//        add("ltrim", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
//        add("pubsub", -2, 0, 0, 0, false, false, false, false, true, true, false, true, true, false, false, false, false);
//        // add("host:",-1,0,0,0,false,false,false,false,false,false,false,true,true,false,false,false,false);
//        add("zrevrangebylex", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        add("subscribe", -2, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
//        add("mset", -3, 1, -1, 2, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("strlen", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        // add("brpop",-3,1,-2,1,false,true,false,false,false,false,true,false,false,false,false,false,false);
//        add("getrange", 4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        add("zrevrank", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        //  add("xdel",-3,1,1,1,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        add("hget", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("spop", -2, 1, 1, 1, false, true, false, true, true, false, false, false, false, false, false, false, false);
//        add("select", 2, 0, 0, 0, false, false, false, true, false, false, false, true, false, false, false, false, false);
//        add("rename", 3, 1, 2, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
//        add("pttl", 2, 1, 1, 1, true, false, false, true, true, false, false, false, false, false, false, false, false);
//        add("lindex", 3, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        //  add("pfcount",-2,1,-1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        //   add("geohash",-2,1,1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        add("lpush", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("hincrby", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("expireat", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("save", 1, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, true, false);
//        add("sunion", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
//        //  add("geoadd",-5,1,1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        //  add("asking",1,0,0,0,false,false,false,true,false,false,false,false,false,false,false,false,false);
//        //  add("pfdebug",-3,0,0,0,false,true,false,false,false,false,false,false,false,false,false,false,false);
//        //  add("smove",4,1,2,1,false,true,false,true,false,false,false,false,false,false,false,false,false);
//        add("eval", -3, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, true, false, false);
//        add("psubscribe", -2, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
//        //  add("pfmerge",-2,1,-1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        add("zlexcount", 4, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        //  add("lastsave",1,0,0,0,false,false,false,true,true,false,false,false,false,false,false,false,false);
//        add("setnx", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("expire", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("bgsave", -1, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, true, false);
//        //  add("xclaim",-6,1,1,1,false,true,false,true,true,false,false,false,false,false,false,false,false);
//        add("renamenx", 3, 1, 2, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("zpopmin", -2, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        //  add("geodist",-4,1,1,1,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        //  add("memory",-2,0,0,0,true,false,false,false,true,false,false,false,false,false,false,false,false);
//        add("set", -3, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("zrangebyscore", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        //   add("randomkey",1,0,0,0,true,false,false,false,true,false,false,false,false,false,false,false,false);
//        add("cluster", -2, 0, 0, 0, false, false, false, false, false, false, false, false, false, false, false, true, false);
//        add("sdiffstore", -3, 1, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        //   add("shutdown",-1,0,0,0,false,false,false,false,false,false,true,true,true,false,false,true,false);
//        //   add("bitfield",-2,1,1,1,false,true,true,false,false,false,false,false,false,false,false,false,false);
//        //   add("slaveof",3,0,0,0,false,false,false,false,false,false,true,false,true,false,false,true,false);
//        //   add("command",0,0,0,0,false,false,false,false,true,false,false,true,true,false,false,false,false);
//        //    add("watch",-2,1,-1,1,false,false,false,true,false,false,true,false,false,false,false,false,false);
//        add("rpop", 2, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        //    add("lolwut",-1,0,0,0,true,false,false,false,false,false,false,false,false,false,false,false,false);
//        //    add("georadius_ro",-6,1,1,1,true,false,false,false,false,false,false,false,false,false,true,false,false);
//        add("pexpireat", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        //    add("bzpopmax",-3,1,-2,1,false,true,false,true,false,false,true,false,false,false,false,false,false);
//        add("zadd", -4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("info", -1, 0, 0, 0, false, false, false, false, true, false, false, true, true, false, false, false, false);
//        add("exists", -2, 1, -1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("getset", 3, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("dbsize", 1, 0, 0, 0, true, false, false, true, false, false, false, false, false, false, false, false, false);
//        add("lpushx", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        //   add("xsetid",3,1,1,1,false,true,true,true,false,false,false,false,false,false,false,false,false);
//        add("auth", 2, 0, 0, 0, false, false, false, true, false, false, true, true, true, false, false, false, true);
//        add("decr", 2, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("linsert", 5, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("xtrim", -2, 1, 1, 1, false, true, false, true, true, false, false, false, false, false, false, false, false);
//        add("rpush", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("srem", -3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
//        add("evalsha", -3, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, true, false, false);
//        add("msetnx", -3, 1, -1, 2, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        add("setex", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
//        //   add("georadiusbymember_ro",-5,1,1,1,true,false,false,false,false,false,false,false,false,false,true,false,false);
//        add("zrevrange", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
//        //    add("debug",-2,0,0,0,false,false,false,false,false,false,true,false,false,false,false,true,false);
//        add("zunionstore", -4, 0, 0, 0, false, true, true, false, false, false, false, false, false, false, true, false, false);
//        //  add("getbit",3,1,1,1,true,false,false,true,false,false,false,false,false,false,false,false,false);
//        add("hsetnx", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
//        add("zrangebylex", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);

        // Version 3.2.13
        add("zremrangebylex", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("pexpireat", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("setnx", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("rpush", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("zrevrange", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("hmget", -3, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("srem", -3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("replconf", -1, 0, 0, 0, false, false, false, false, false, false, true, true, true, false, false, true, false);
        add("mset", -3, 1, -1, 2, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("lrange", 4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("setrange", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("sdiffstore", -3, 1, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("discard", 1, 0, 0, 0, false, false, false, true, false, false, true, false, false, false, false, false, false);
        add("hsetnx", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("flushall", 1, 0, 0, 0, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("sadd", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("pexpire", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("renamenx", 3, 1, 2, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("zrangebyscore", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("get", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("bgrewriteaof", 1, 0, 0, 0, false, false, false, false, false, false, false, false, false, false, false, true, false);
        add("hmset", -4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("type", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("sinterstore", -3, 1, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("zremrangebyscore", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("touch", -2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("lpushx", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("zrank", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("hget", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("randomkey", 1, 0, 0, 0, true, false, false, false, true, false, false, false, false, false, false, false, false);
        add("zrevrangebyscore", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("set", -3, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("getset", 3, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("zrevrank", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("hset", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("sinter", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
        add("move", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("strlen", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("unwatch", 1, 0, 0, 0, false, false, false, true, false, false, true, false, false, false, false, false, false);
        add("lpop", 2, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("smembers", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
        add("pfadd", -2, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("msetnx", -3, 1, -1, 2, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("georadius", -6, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, true, false, false);
        add("zadd", -4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("lastsave", 1, 0, 0, 0, false, false, false, true, true, false, false, false, false, false, false, false, false);
        add("exec", 1, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, false, true);
        add("slowlog", -2, 0, 0, 0, false, false, false, false, false, false, false, false, false, false, false, true, false);
        add("sismember", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("debug", -1, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, true, false);
        add("eval", -3, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, true, false, false);
        add("hexists", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("smove", 4, 1, 2, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("multi", 1, 0, 0, 0, false, false, false, true, false, false, true, false, false, false, false, false, false);
        add("sdiff", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
        add("geopos", -2, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("hscan", -3, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
        add("brpoplpush", 4, 1, 2, 1, false, true, true, false, false, false, true, false, false, false, false, false, false);
        add("script", -2, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, false, false);
        add("keys", 2, 0, 0, 0, true, false, false, false, false, false, false, false, false, true, false, false, false);
        add("hdel", -3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("hvals", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
        add("pfcount", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("command", 0, 0, 0, 0, false, false, false, false, false, false, false, true, true, false, false, false, false);
        add("zscan", -3, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
        add("echo", 2, 0, 0, 0, false, false, false, true, false, false, false, false, false, false, false, false, false);
        add("select", 2, 0, 0, 0, false, false, false, true, false, false, false, true, false, false, false, false, false);
        add("zcount", 4, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("substr", 4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("pttl", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("hincrbyfloat", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("hlen", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("incrby", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("setex", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("persist", 2, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("setbit", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("info", -1, 0, 0, 0, false, false, false, false, false, false, false, true, true, false, false, false, false);
        add("scard", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("geodist", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("srandmember", -2, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
        add("lrem", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("append", 3, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("zincrby", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("rpop", 2, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("hgetall", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("cluster", -2, 0, 0, 0, false, false, false, false, false, false, false, false, false, false, false, true, false);
        add("ltrim", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("flushdb", 1, 0, 0, 0, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("rpoplpush", 3, 1, 2, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("expire", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("psync", 3, 0, 0, 0, true, false, false, false, false, false, true, false, false, false, false, true, false);
        add("pubsub", -2, 0, 0, 0, false, false, false, false, true, true, false, true, true, false, false, false, false);
        add("psubscribe", -2, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
        add("georadiusbymember_ro", -5, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, true, false, false);
        add("object", 3, 2, 2, 2, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("watch", -2, 1, -1, 1, false, false, false, true, false, false, true, false, false, false, false, false, false);
        add("bitop", -4, 2, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("evalsha", -3, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, true, false, false);
        add("punsubscribe", -1, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
        add("georadiusbymember", -5, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, true, false, false);
        add("publish", 3, 0, 0, 0, false, false, false, true, false, true, false, true, true, false, false, false, false);
        add("lset", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("rename", 3, 1, 2, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("bgsave", -1, 0, 0, 0, false, false, false, false, false, false, false, false, false, false, false, true, false);
        add("decrby", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("sunion", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
        add("blpop", -3, 1, -2, 1, false, true, false, false, false, false, true, false, false, false, false, false, false);
        add("readonly", 1, 0, 0, 0, false, false, false, true, false, false, false, false, false, false, false, false, false);
        add("zrem", -3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("exists", -2, 1, -1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("hstrlen", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("linsert", 5, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("lindex", 3, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("scan", -2, 0, 0, 0, true, false, false, false, true, false, false, false, false, false, false, false, false);
        add("migrate", -6, 0, 0, 0, false, true, false, false, false, false, false, false, false, false, true, false, false);
        add("ping", -1, 0, 0, 0, false, false, false, true, false, false, false, false, true, false, false, false, false);
        add("latency", -2, 0, 0, 0, false, false, false, false, false, false, true, true, true, false, false, true, false);
        add("zunionstore", -4, 0, 0, 0, false, true, true, false, false, false, false, false, false, false, true, false, false);
        add("role", 1, 0, 0, 0, false, false, false, false, false, false, true, true, true, false, false, false, false);
        add("ttl", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("wait", 3, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, false, false);
        add("del", -2, 1, -1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("zscore", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("zrevrangebylex", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("sscan", -3, 1, 1, 1, true, false, false, false, true, false, false, false, false, false, false, false, false);
        add("incrbyfloat", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("decr", 2, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("geohash", -2, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("getbit", 3, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("spop", -2, 1, 1, 1, false, true, false, true, true, false, false, false, false, false, false, false, false);
        add("hkeys", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, true, false, false, false);
        add("pfmerge", -2, 1, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("zrange", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("monitor", 1, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, true, false);
        add("zinterstore", -4, 0, 0, 0, false, true, true, false, false, false, false, false, false, false, true, false, false);
        add("rpushx", 3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("llen", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("hincrby", 4, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("save", 1, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, true, false);
        add("zremrangebyrank", 4, 1, 1, 1, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("auth", 2, 0, 0, 0, false, false, false, true, false, false, true, true, true, false, false, false, false);
        add("zcard", 2, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("psetex", 4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("shutdown", -1, 0, 0, 0, false, false, false, false, false, false, false, true, true, false, false, true, false);
        add("sync", 1, 0, 0, 0, true, false, false, false, false, false, true, false, false, false, false, true, false);
        add("dbsize", 1, 0, 0, 0, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("expireat", 3, 1, 1, 1, false, true, false, true, false, false, false, false, false, false, false, false, false);
        add("subscribe", -2, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
        add("bitfield", -2, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("brpop", -3, 1, -2, 1, false, true, false, false, false, false, true, false, false, false, false, false, false);
        add("georadius_ro", -6, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, true, false, false);
        add("geoadd", -5, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("post", -1, 0, 0, 0, false, false, false, false, false, false, false, true, true, false, false, false, false);
        add("sort", -2, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, true, false, false);
        add("sunionstore", -3, 1, -1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("zrangebylex", -4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("zlexcount", 4, 1, 1, 1, true, false, false, true, false, false, false, false, false, false, false, false, false);
        add("lpush", -3, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("incr", 2, 1, 1, 1, false, true, true, true, false, false, false, false, false, false, false, false, false);
        add("mget", -2, 1, -1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("getrange", 4, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("slaveof", 3, 0, 0, 0, false, false, false, false, false, false, true, false, true, false, false, true, false);
        add("bitpos", -3, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("config", -2, 0, 0, 0, false, false, false, false, false, false, false, true, true, false, false, true, false);
        add("host:", -1, 0, 0, 0, false, false, false, false, false, false, false, true, true, false, false, false, false);
        add("pfdebug", -3, 0, 0, 0, false, true, false, false, false, false, false, false, false, false, false, false, false);
        add("asking", 1, 0, 0, 0, false, false, false, true, false, false, false, false, false, false, false, false, false);
        add("client", -2, 0, 0, 0, false, false, false, false, false, false, true, false, false, false, false, true, false);
        add("pfselftest", 1, 0, 0, 0, false, false, false, false, false, false, false, false, false, false, false, true, false);
        add("restore", -4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("unsubscribe", -1, 0, 0, 0, false, false, false, false, false, true, true, true, true, false, false, false, false);
        add("readwrite", 1, 0, 0, 0, false, false, false, true, false, false, false, false, false, false, false, false, false);
        add("bitcount", -2, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);
        add("restore-asking", -4, 1, 1, 1, false, true, true, false, false, false, false, false, false, false, false, false, false);
        add("time", 1, 0, 0, 0, false, false, false, true, true, false, false, false, false, false, false, false, false);
        add("dump", 2, 1, 1, 1, true, false, false, false, false, false, false, false, false, false, false, false, false);

        setGroup("pfcount", new String[]{"@read", "@hyperloglog", "@slow"});
        setGroup("sscan", new String[]{"@read", "@set", "@slow"});
        setGroup("cluster", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("shutdown", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("sinter", new String[]{"@read", "@set", "@slow"});
        setGroup("incr", new String[]{"@write", "@string", "@fast"});
        setGroup("time", new String[]{"@fast"});
        setGroup("mset", new String[]{"@write", "@string", "@slow"});
        setGroup("memory", new String[]{"@read", "@slow"});
        setGroup("psetex", new String[]{"@write", "@string", "@slow"});
        setGroup("renamenx", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("eval", new String[]{"@slow", "@scripting"});
        setGroup("xack", new String[]{"@write", "@stream", "@fast"});
        setGroup("del", new String[]{"@keyspace", "@write", "@slow"});
        setGroup("zunionstore", new String[]{"@write", "@sortedset", "@slow"});
        setGroup("lrange", new String[]{"@read", "@list", "@slow"});
        setGroup("hsetnx", new String[]{"@write", "@hash", "@fast"});
        setGroup("hincrbyfloat", new String[]{"@write", "@hash", "@fast"});
        setGroup("hset", new String[]{"@write", "@hash", "@fast"});
        setGroup("bgrewriteaof", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("zcount", new String[]{"@read", "@sortedset", "@fast"});
        setGroup("zrevrange", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("spop", new String[]{"@write", "@set", "@fast"});
        setGroup("persist", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("zrank", new String[]{"@read", "@sortedset", "@fast"});
        setGroup("rename", new String[]{"@keyspace", "@write", "@slow"});
        setGroup("lpush", new String[]{"@write", "@list", "@fast"});
        setGroup("dump", new String[]{"@keyspace", "@read", "@slow"});
        setGroup("decr", new String[]{"@write", "@string", "@fast"});
        setGroup("hgetall", new String[]{"@read", "@hash", "@slow"});
        setGroup("hscan", new String[]{"@read", "@hash", "@slow"});
        setGroup("auth", new String[]{"@fast", "@connection"});
        setGroup("zmscore", new String[]{"@read", "@sortedset", "@fast"});
        setGroup("mget", new String[]{"@read", "@string", "@fast"});
        setGroup("sismember", new String[]{"@read", "@set", "@fast"});
        setGroup("wait", new String[]{"@keyspace", "@slow"});
        setGroup("georadiusbymember", new String[]{"@write", "@geo", "@slow"});
        setGroup("lpos", new String[]{"@read", "@list", "@slow"});
        setGroup("getset", new String[]{"@write", "@string", "@fast"});
        setGroup("host:", new String[]{"@read", "@slow"});
        setGroup("blpop", new String[]{"@write", "@list", "@slow", "@blocking"});
        setGroup("getex", new String[]{"@write", "@string", "@fast"});
        setGroup("psubscribe", new String[]{"@pubsub", "@slow"});
        setGroup("client", new String[]{"@admin", "@slow", "@dangerous", "@connection"});
        setGroup("decrby", new String[]{"@write", "@string", "@fast"});
        setGroup("pfmerge", new String[]{"@write", "@hyperloglog", "@slow"});
        setGroup("zrevrangebyscore", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("ping", new String[]{"@fast", "@connection"});
        setGroup("hmset", new String[]{"@write", "@hash", "@fast"});
        setGroup("evalsha", new String[]{"@slow", "@scripting"});
        setGroup("zrangebylex", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("hstrlen", new String[]{"@read", "@hash", "@fast"});
        setGroup("touch", new String[]{"@keyspace", "@read", "@fast"});
        setGroup("punsubscribe", new String[]{"@pubsub", "@slow"});
        setGroup("bitfield_ro", new String[]{"@read", "@bitmap", "@fast"});
        setGroup("xpending", new String[]{"@read", "@stream", "@slow"});
        setGroup("getdel", new String[]{"@write", "@string", "@fast"});
        setGroup("role", new String[]{"@fast", "@dangerous"});
        setGroup("subscribe", new String[]{"@pubsub", "@slow"});
        setGroup("lmove", new String[]{"@write", "@list", "@slow"});
        setGroup("flushdb", new String[]{"@keyspace", "@write", "@slow", "@dangerous"});
        setGroup("linsert", new String[]{"@write", "@list", "@slow"});
        setGroup("expireat", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("lindex", new String[]{"@read", "@list", "@slow"});
        setGroup("brpoplpush", new String[]{"@write", "@list", "@slow", "@blocking"});
        setGroup("acl", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("psync", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("readwrite", new String[]{"@keyspace", "@fast"});
        setGroup("command", new String[]{"@slow", "@connection"});
        setGroup("zremrangebyscore", new String[]{"@write", "@sortedset", "@slow"});
        setGroup("bzpopmin", new String[]{"@write", "@sortedset", "@fast", "@blocking"});
        setGroup("bitop", new String[]{"@write", "@bitmap", "@slow"});
        setGroup("rpushx", new String[]{"@write", "@list", "@fast"});
        setGroup("zrange", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("bitfield", new String[]{"@write", "@bitmap", "@slow"});
        setGroup("lpop", new String[]{"@write", "@list", "@fast"});
        setGroup("zadd", new String[]{"@write", "@sortedset", "@fast"});
        setGroup("zscore", new String[]{"@read", "@sortedset", "@fast"});
        setGroup("sdiff", new String[]{"@read", "@set", "@slow"});
        setGroup("pexpireat", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("replicaof", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("hget", new String[]{"@read", "@hash", "@fast"});
        setGroup("pexpire", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("geopos", new String[]{"@read", "@geo", "@slow"});
        setGroup("rpop", new String[]{"@write", "@list", "@fast"});
        setGroup("randomkey", new String[]{"@keyspace", "@read", "@slow"});
        setGroup("zrandmember", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("hrandfield", new String[]{"@read", "@hash", "@slow"});
        setGroup("keys", new String[]{"@keyspace", "@read", "@slow", "@dangerous"});
        setGroup("georadiusbymember_ro", new String[]{"@read", "@geo", "@slow"});
        setGroup("getrange", new String[]{"@read", "@string", "@slow"});
        setGroup("hkeys", new String[]{"@read", "@hash", "@slow"});
        setGroup("hdel", new String[]{"@write", "@hash", "@fast"});
        setGroup("config", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("select", new String[]{"@keyspace", "@fast"});
        setGroup("slowlog", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("smove", new String[]{"@write", "@set", "@fast"});
        setGroup("bitpos", new String[]{"@read", "@bitmap", "@slow"});
        setGroup("incrby", new String[]{"@write", "@string", "@fast"});
        setGroup("geohash", new String[]{"@read", "@geo", "@slow"});
        setGroup("zrangestore", new String[]{"@write", "@sortedset", "@slow"});
        setGroup("hexists", new String[]{"@read", "@hash", "@fast"});
        setGroup("scard", new String[]{"@read", "@set", "@fast"});
        setGroup("watch", new String[]{"@fast", "@transaction"});
        setGroup("brpop", new String[]{"@write", "@list", "@slow", "@blocking"});
        setGroup("hmget", new String[]{"@read", "@hash", "@fast"});
        setGroup("sunionstore", new String[]{"@write", "@set", "@slow"});
        setGroup("type", new String[]{"@keyspace", "@read", "@fast"});
        setGroup("pfselftest", new String[]{"@hyperloglog", "@admin", "@slow", "@dangerous"});
        setGroup("xrange", new String[]{"@read", "@stream", "@slow"});
        setGroup("zinterstore", new String[]{"@write", "@sortedset", "@slow"});
        setGroup("lrem", new String[]{"@write", "@list", "@slow"});
        setGroup("unlink", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("exists", new String[]{"@keyspace", "@read", "@fast"});
        setGroup("llen", new String[]{"@read", "@list", "@fast"});
        setGroup("latency", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("module", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("script", new String[]{"@slow", "@scripting"});
        setGroup("publish", new String[]{"@pubsub", "@fast"});
        setGroup("set", new String[]{"@write", "@string", "@slow"});
        setGroup("bzpopmax", new String[]{"@write", "@sortedset", "@fast", "@blocking"});
        setGroup("bitcount", new String[]{"@read", "@bitmap", "@slow"});
        setGroup("zrem", new String[]{"@write", "@sortedset", "@fast"});
        setGroup("pfdebug", new String[]{"@write", "@hyperloglog", "@admin", "@slow", "@dangerous"});
        setGroup("swapdb", new String[]{"@keyspace", "@write", "@fast", "@dangerous"});
        setGroup("msetnx", new String[]{"@write", "@string", "@slow"});
        setGroup("ttl", new String[]{"@keyspace", "@read", "@fast"});
        setGroup("zdiffstore", new String[]{"@write", "@sortedset", "@slow"});
        setGroup("lpushx", new String[]{"@write", "@list", "@fast"});
        setGroup("rpush", new String[]{"@write", "@list", "@fast"});
        setGroup("multi", new String[]{"@fast", "@transaction"});
        setGroup("zrevrangebylex", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("xtrim", new String[]{"@write", "@stream", "@slow"});
        setGroup("sdiffstore", new String[]{"@write", "@set", "@slow"});
        setGroup("expire", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("get", new String[]{"@read", "@string", "@fast"});
        setGroup("post", new String[]{"@read", "@slow"});
        setGroup("lset", new String[]{"@write", "@list", "@slow"});
        setGroup("stralgo", new String[]{"@read", "@string", "@slow"});
        setGroup("restore-asking", new String[]{"@keyspace", "@write", "@slow", "@dangerous"});
        setGroup("move", new String[]{"@keyspace", "@write", "@fast"});
        setGroup("lastsave", new String[]{"@admin", "@fast", "@dangerous"});
        setGroup("xdel", new String[]{"@write", "@stream", "@fast"});
        setGroup("pfadd", new String[]{"@write", "@hyperloglog", "@fast"});
        setGroup("zincrby", new String[]{"@write", "@sortedset", "@fast"});
        setGroup("ltrim", new String[]{"@write", "@list", "@slow"});
        setGroup("sunion", new String[]{"@read", "@set", "@slow"});
        setGroup("strlen", new String[]{"@read", "@string", "@fast"});
        setGroup("pubsub", new String[]{"@pubsub", "@slow"});
        setGroup("rpoplpush", new String[]{"@write", "@list", "@slow"});
        setGroup("xsetid", new String[]{"@write", "@stream", "@fast"});
        setGroup("sadd", new String[]{"@write", "@set", "@fast"});
        setGroup("monitor", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("xgroup", new String[]{"@write", "@stream", "@slow"});
        setGroup("readonly", new String[]{"@keyspace", "@fast"});
        setGroup("incrbyfloat", new String[]{"@write", "@string", "@fast"});
        setGroup("geodist", new String[]{"@read", "@geo", "@slow"});
        setGroup("smembers", new String[]{"@read", "@set", "@slow"});
        setGroup("replconf", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("hello", new String[]{"@fast", "@connection"});
        setGroup("zscan", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("hincrby", new String[]{"@write", "@hash", "@fast"});
        setGroup("xinfo", new String[]{"@read", "@stream", "@slow"});
        setGroup("bgsave", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("zremrangebylex", new String[]{"@write", "@sortedset", "@slow"});
        setGroup("sinterstore", new String[]{"@write", "@set", "@slow"});
        setGroup("srem", new String[]{"@write", "@set", "@fast"});
        setGroup("exec", new String[]{"@slow", "@transaction"});
        setGroup("xadd", new String[]{"@write", "@stream", "@fast"});
        setGroup("xreadgroup", new String[]{"@write", "@stream", "@slow", "@blocking"});
        setGroup("srandmember", new String[]{"@read", "@set", "@slow"});
        setGroup("scan", new String[]{"@keyspace", "@read", "@slow"});
        setGroup("georadius_ro", new String[]{"@read", "@geo", "@slow"});
        setGroup("setbit", new String[]{"@write", "@bitmap", "@slow"});
        setGroup("zlexcount", new String[]{"@read", "@sortedset", "@fast"});
        setGroup("hvals", new String[]{"@read", "@hash", "@slow"});
        setGroup("dbsize", new String[]{"@keyspace", "@read", "@fast"});
        setGroup("copy", new String[]{"@keyspace", "@write", "@slow"});
        setGroup("unwatch", new String[]{"@fast", "@transaction"});
        setGroup("zremrangebyrank", new String[]{"@write", "@sortedset", "@slow"});
        setGroup("zpopmin", new String[]{"@write", "@sortedset", "@fast"});
        setGroup("flushall", new String[]{"@keyspace", "@write", "@slow", "@dangerous"});
        setGroup("object", new String[]{"@keyspace", "@read", "@slow"});
        setGroup("xread", new String[]{"@read", "@stream", "@slow", "@blocking"});
        setGroup("getbit", new String[]{"@read", "@bitmap", "@fast"});
        setGroup("migrate", new String[]{"@keyspace", "@write", "@slow", "@dangerous"});
        setGroup("xclaim", new String[]{"@write", "@stream", "@fast"});
        setGroup("smismember", new String[]{"@read", "@set", "@fast"});
        setGroup("zrevrank", new String[]{"@read", "@sortedset", "@fast"});
        setGroup("failover", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("sort", new String[]{"@write", "@set", "@sortedset", "@list", "@slow", "@dangerous"});
        setGroup("hlen", new String[]{"@read", "@hash", "@fast"});
        setGroup("xrevrange", new String[]{"@read", "@stream", "@slow"});
        setGroup("geosearchstore", new String[]{"@write", "@geo", "@slow"});
        setGroup("setnx", new String[]{"@write", "@string", "@fast"});
        setGroup("georadius", new String[]{"@write", "@geo", "@slow"});
        setGroup("xautoclaim", new String[]{"@write", "@stream", "@fast"});
        setGroup("append", new String[]{"@write", "@string", "@fast"});
        setGroup("unsubscribe", new String[]{"@pubsub", "@slow"});
        setGroup("zrangebyscore", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("zpopmax", new String[]{"@write", "@sortedset", "@fast"});
        setGroup("geoadd", new String[]{"@write", "@geo", "@slow"});
        setGroup("blmove", new String[]{"@write", "@list", "@slow", "@blocking"});
        setGroup("save", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("setex", new String[]{"@write", "@string", "@slow"});
        setGroup("discard", new String[]{"@fast", "@transaction"});
        setGroup("setrange", new String[]{"@write", "@string", "@slow"});
        setGroup("asking", new String[]{"@keyspace", "@fast"});
        setGroup("info", new String[]{"@slow", "@dangerous"});
        setGroup("zcard", new String[]{"@read", "@sortedset", "@fast"});
        setGroup("reset", new String[]{"@fast", "@connection"});
        setGroup("pttl", new String[]{"@keyspace", "@read", "@fast"});
        setGroup("debug", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("lolwut", new String[]{"@read", "@fast"});
        setGroup("zinter", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("zunion", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("slaveof", new String[]{"@admin", "@slow", "@dangerous"});
        setGroup("echo", new String[]{"@fast", "@connection"});
        setGroup("zdiff", new String[]{"@read", "@sortedset", "@slow"});
        setGroup("xlen", new String[]{"@read", "@stream", "@fast"});
        setGroup("restore", new String[]{"@keyspace", "@write", "@slow", "@dangerous"});
        setGroup("geosearch", new String[]{"@read", "@geo", "@slow"});
        setGroup("substr", new String[]{"@read", "@string", "@slow"});
        setGroup("sync", new String[]{"@admin", "@slow", "@dangerous"});

//        if (!Configuration.isBinaryCompatibleKey()) {
//            setBinaryArgs("rpoplpush", null);
//            setBinaryArgs("lrange", null);
//            setBinaryArgs("zlexcount", new boolean[]{false, false, true, true, false});
//            setBinaryArgs("spop", null);
//            setBinaryArgs("hkeys", null);
//            setBinaryArgs("hmset", new boolean[]{false, false, true, true});
//            setBinaryArgs("dump", null);
//            setBinaryArgs("smove", new boolean[]{false, false, false, true});
//            setBinaryArgs("xdel", null);
//            setBinaryArgs("mget", null);
//            setBinaryArgs("restore-asking", null);
//            setBinaryArgs("lpushx", new boolean[]{false, false, true});
//            setBinaryArgs("smismember", new boolean[]{false, false, true});
//            setBinaryArgs("zrangebylex", new boolean[]{false, false, true, true, false});
//            setBinaryArgs("srem", new boolean[]{false, false, true});
//            setBinaryArgs("lpop", null);
//            setBinaryArgs("ttl", null);
//            setBinaryArgs("select", null);
//            setBinaryArgs("monitor", null);
//            setBinaryArgs("incrby", null);
//            setBinaryArgs("geohash", new boolean[]{false, false, true});
//            setBinaryArgs("pfadd", null);
//            setBinaryArgs("renamenx", null);
//            setBinaryArgs("expire", null);
//            setBinaryArgs("pfselftest", null);
//            setBinaryArgs("zrem", new boolean[]{false, false, true});
//            setBinaryArgs("xrevrange", null);
//            setBinaryArgs("readwrite", null);
//            setBinaryArgs("pubsub", null);
//            setBinaryArgs("hvals", null);
//            setBinaryArgs("wait", null);
//            setBinaryArgs("discard", null);
//            setBinaryArgs("xinfo", null);
//            setBinaryArgs("zrevrangebylex", null);
//            setBinaryArgs("hstrlen", new boolean[]{false, false, true});
//            setBinaryArgs("zscan", null);
//            setBinaryArgs("getex", null);
//            setBinaryArgs("georadiusbymember_ro", new boolean[]{false, false, true, false});
//            setBinaryArgs("incrbyfloat", null);
//            setBinaryArgs("zrange", null);
//            setBinaryArgs("persist", null);
//            setBinaryArgs("bitfield_ro", null);
//            setBinaryArgs("rpop", null);
//            setBinaryArgs("expireat", null);
//            setBinaryArgs("ping", new boolean[]{false, true});
//            setBinaryArgs("rpushx", new boolean[]{false, false, true});
//            setBinaryArgs("hexists", new boolean[]{false, false, true});
//            setBinaryArgs("hset", new boolean[]{false, false, true, true});
//            setBinaryArgs("zadd", new boolean[]{false, false, true, true});
//            setBinaryArgs("srandmember", null);
//            setBinaryArgs("georadius_ro", null);
//            setBinaryArgs("set", new boolean[]{false, false, true, false});
//            setBinaryArgs("flushdb", null);
//            setBinaryArgs("xlen", null);
//            setBinaryArgs("del", null);
//            setBinaryArgs("save", null);
//            setBinaryArgs("getset", new boolean[]{false, false, true});
//            setBinaryArgs("lastsave", null);
//            setBinaryArgs("lindex", null);
//            setBinaryArgs("cluster", null);
//            setBinaryArgs("get", null);
//            setBinaryArgs("psetex", new boolean[]{false, false, false, true});
//            setBinaryArgs("debug", null);
//            setBinaryArgs("sinterstore", null);
//            setBinaryArgs("zpopmin", null);
//            setBinaryArgs("unsubscribe", new boolean[]{false, true, true});
//            setBinaryArgs("zremrangebyrank", null);
//            setBinaryArgs("mset", new boolean[]{false, false, true, false, true, false, true, false, true, false, true});
//            setBinaryArgs("geoadd", new boolean[]{false, false, true});
//            setBinaryArgs("append", new boolean[]{false, false, true});
//            setBinaryArgs("scan", null);
//            setBinaryArgs("multi", null);
//            setBinaryArgs("zcard", null);
//            setBinaryArgs("module", null);
//            setBinaryArgs("pfdebug", null);
//            setBinaryArgs("setnx", new boolean[]{false, false, true});
//            setBinaryArgs("swapdb", null);
////        setBinaryArgs("eval", null);
////        setBinaryArgs("evalsha", null);
//            setBinaryArgs("eval", new boolean[]{false, false, false, true});
//            setBinaryArgs("evalsha", new boolean[]{false, false, false, true});
//            setBinaryArgs("pfcount", null);
//            setBinaryArgs("hrandfield", null);
//            setBinaryArgs("restore", null);
//            setBinaryArgs("exec", null);
//            setBinaryArgs("bgrewriteaof", null);
//            setBinaryArgs("sdiffstore", null);
//            setBinaryArgs("hincrby", new boolean[]{false, false, true, false});
//            setBinaryArgs("llen", null);
//            setBinaryArgs("bzpopmin", null);
//            setBinaryArgs("hscan", null);
//            setBinaryArgs("zrevrangebyscore", null);
//            setBinaryArgs("psubscribe", new boolean[]{false, true, true});
//            setBinaryArgs("sadd", new boolean[]{false, false, true});
//            setBinaryArgs("zrevrange", null);
//            setBinaryArgs("lolwut", null);
//            setBinaryArgs("unwatch", null);
//            setBinaryArgs("sismember", new boolean[]{false, false, true});
//            setBinaryArgs("zrank", new boolean[]{false, false, true});
//            setBinaryArgs("lpos", null);
//            setBinaryArgs("bgsave", null);
//            setBinaryArgs("incr", null);
////        setBinaryArgs("zremrangebyscore", new boolean[]{false, false, false, false});
//            setBinaryArgs("zcount", new boolean[]{false, false, true, true});
//            setBinaryArgs("unlink", null);
//            setBinaryArgs("latency", null);
//            setBinaryArgs("lrem", new boolean[]{false, false, false, true});
//            setBinaryArgs("flushall", null);
//            setBinaryArgs("xsetid", null);
//            setBinaryArgs("msetnx", new boolean[]{false, false, true, false, true, false, true, false, true, false, true});
//            setBinaryArgs("zremrangebylex", new boolean[]{false, false, true, true, false});
//            setBinaryArgs("zrangebyscore", null);
//            setBinaryArgs("punsubscribe", new boolean[]{false, true, true});
//            setBinaryArgs("time", null);
//            setBinaryArgs("hmget", new boolean[]{false, false, true});
//            setBinaryArgs("zinterstore", new boolean[]{false, false, false, true});
//            setBinaryArgs("blmove", null);
//            setBinaryArgs("publish", new boolean[]{false, true, true});
//            setBinaryArgs("slaveof", null);
//            setBinaryArgs("bzpopmax", null);
//            setBinaryArgs("brpop", null);
//            setBinaryArgs("xrange", null);
//            setBinaryArgs("hlen", null);
//            setBinaryArgs("geopos", new boolean[]{false, false, true});
//            setBinaryArgs("watch", null);
//            setBinaryArgs("zinter", new boolean[]{false, false, true});
//            setBinaryArgs("zrandmember", null);
//            setBinaryArgs("setex", new boolean[]{false, false, false, true});
//            setBinaryArgs("reset", null);
//            setBinaryArgs("xautoclaim", null);
//            setBinaryArgs("type", null);
//            setBinaryArgs("shutdown", null);
//            setBinaryArgs("post", null);
//            setBinaryArgs("getdel", null);
//            setBinaryArgs("xtrim", null);
//            setBinaryArgs("exists", null);
//            setBinaryArgs("sync", null);
//            setBinaryArgs("client", null);
//            setBinaryArgs("zrevrank", new boolean[]{false, false, true});
//            setBinaryArgs("zincrby", new boolean[]{false, false, true});
//            setBinaryArgs("pexpire", null);
//            setBinaryArgs("failover", null);
//            setBinaryArgs("stralgo", null);
//            setBinaryArgs("dbsize", null);
//            setBinaryArgs("decrby", null);
//            setBinaryArgs("zdiffstore", null);
//            setBinaryArgs("echo", null);
//            setBinaryArgs("georadius", null);
//            setBinaryArgs("setrange", null);
//            setBinaryArgs("role", null);
//            setBinaryArgs("substr", null);
//            setBinaryArgs("rpush", new boolean[]{false, false, true});
//            setBinaryArgs("hgetall", null);
//            setBinaryArgs("decr", null);
//            setBinaryArgs("acl", null);
//            setBinaryArgs("info", null);
//            setBinaryArgs("script", null);
//            setBinaryArgs("sunionstore", null);
//            setBinaryArgs("getrange", null);
//            setBinaryArgs("pexpireat", null);
//            setBinaryArgs("xreadgroup", null);
//            setBinaryArgs("scard", null);
//            setBinaryArgs("geodist", new boolean[]{false, false, true, true, false});
//            setBinaryArgs("copy", null);
//            setBinaryArgs("xack", null);
//            setBinaryArgs("geosearchstore", null);
//            setBinaryArgs("ltrim", null);
//            setBinaryArgs("subscribe", new boolean[]{false, true, true});
//            setBinaryArgs("zrangestore", null);
//            setBinaryArgs("brpoplpush", null);
//            setBinaryArgs("randomkey", null);
//            setBinaryArgs("move", null);
//            setBinaryArgs("slowlog", null);
//            setBinaryArgs("bitop", null);
//            setBinaryArgs("asking", null);
//            setBinaryArgs("bitfield", null);
//            setBinaryArgs("hdel", new boolean[]{false, false, true});
//            setBinaryArgs("sort", null);
//            setBinaryArgs("rename", null);
//            setBinaryArgs("zdiff", null);
//            setBinaryArgs("xread", new boolean[]{false, true});
//            setBinaryArgs("xadd", new boolean[]{false, false, true});
//            setBinaryArgs("zpopmax", null);
//            setBinaryArgs("pttl", null);
//            setBinaryArgs("blpop", null);
//            setBinaryArgs("bitcount", null);
//            setBinaryArgs("xgroup", null);
//            setBinaryArgs("pfmerge", null);
//            setBinaryArgs("sinter", null);
//            setBinaryArgs("migrate", null);
//            setBinaryArgs("xpending", null);
//            setBinaryArgs("setbit", null);
//            setBinaryArgs("linsert", null);
//            setBinaryArgs("georadiusbymember", new boolean[]{false, false, true, false});
//            setBinaryArgs("zunionstore", new boolean[]{false, false, false, true});
//            setBinaryArgs("sdiff", null);
//            setBinaryArgs("hget", new boolean[]{false, false, true});
//            setBinaryArgs("hello", null);
//            setBinaryArgs("lset", new boolean[]{false, false, false, true});
//            setBinaryArgs("lpush", new boolean[]{false, false, true});
//            setBinaryArgs("bitpos", null);
//            setBinaryArgs("touch", null);
//            setBinaryArgs("zscore", new boolean[]{false, false, true});
//            setBinaryArgs("replicaof", null);
//            setBinaryArgs("xclaim", null);
//            setBinaryArgs("config", null);
//            setBinaryArgs("hsetnx", new boolean[]{false, false, true, true});
//            setBinaryArgs("strlen", null);
//            setBinaryArgs("hincrbyfloat", new boolean[]{false, false, true, false});
//            setBinaryArgs("replconf", null);
//            setBinaryArgs("zunion", new boolean[]{false, false, true});
//            setBinaryArgs("keys", null);
//            setBinaryArgs("memory", null);
//            setBinaryArgs("readonly", null);
//            setBinaryArgs("zmscore", null);
//            setBinaryArgs("lmove", null);
//            setBinaryArgs("auth", null);
//            setBinaryArgs("command", null);
//            setBinaryArgs("host:", null);
//            setBinaryArgs("sscan", null);
//            setBinaryArgs("sunion", null);
//            setBinaryArgs("getbit", null);
//            setBinaryArgs("psync", null);
//            setBinaryArgs("smembers", null);
//            setBinaryArgs("object", null);
//            setBinaryArgs("geosearch", null);
//        } else {
//            /**
//             * key二进制兼容的配置
//             */
//            boolean[] allcompatible = new boolean[]{false, true, true};
//            boolean[] keyonlycompatible = new boolean[]{false, true, false, false};
//            setBinaryArgs("rpoplpush", allcompatible);
//            setBinaryArgs("lrange", keyonlycompatible);
//            setBinaryArgs("zlexcount", new boolean[]{false, true, true, true, false});
//            setBinaryArgs("spop", keyonlycompatible);
//            setBinaryArgs("hkeys", allcompatible);
//            setBinaryArgs("hmset", allcompatible);
//            setBinaryArgs("dump", keyonlycompatible);
//            setBinaryArgs("smove", allcompatible);
//            setBinaryArgs("xdel", keyonlycompatible);
//            setBinaryArgs("mget", allcompatible);
//            setBinaryArgs("restore-asking", null);
//            setBinaryArgs("lpushx", allcompatible);
//            setBinaryArgs("smismember", allcompatible);
//            setBinaryArgs("zrangebylex", new boolean[]{false, true, true, true, false});
//            setBinaryArgs("srem", allcompatible);
//            setBinaryArgs("lpop", allcompatible);
//            setBinaryArgs("ttl", allcompatible);
//            setBinaryArgs("select", null);
//            setBinaryArgs("monitor", null);
//            setBinaryArgs("incrby", keyonlycompatible);
//            setBinaryArgs("geohash", allcompatible);
//            setBinaryArgs("pfadd", keyonlycompatible);
//            setBinaryArgs("renamenx", allcompatible);
//            setBinaryArgs("expire", keyonlycompatible);
//            setBinaryArgs("pfselftest", null);
//            setBinaryArgs("zrem", allcompatible);
//            setBinaryArgs("xrevrange", keyonlycompatible);
//            setBinaryArgs("readwrite", null);
//            setBinaryArgs("pubsub", null);
//            setBinaryArgs("hvals", allcompatible);
//            setBinaryArgs("wait", null);
//            setBinaryArgs("discard", null);
//            setBinaryArgs("xinfo", null);
//            setBinaryArgs("zrevrangebylex", keyonlycompatible);
//            setBinaryArgs("hstrlen", allcompatible);
//            setBinaryArgs("zscan", keyonlycompatible);
//            setBinaryArgs("getex", null);
//            setBinaryArgs("georadiusbymember_ro", new boolean[]{false, true, true, false});
//            setBinaryArgs("incrbyfloat", keyonlycompatible);
//            setBinaryArgs("zrange", keyonlycompatible);
//            setBinaryArgs("persist", keyonlycompatible);
//            setBinaryArgs("bitfield_ro", keyonlycompatible);
//            setBinaryArgs("rpop", keyonlycompatible);
//            setBinaryArgs("expireat", keyonlycompatible);
//            setBinaryArgs("ping", allcompatible);
//            setBinaryArgs("rpushx", allcompatible);
//            setBinaryArgs("hexists", allcompatible);
//            setBinaryArgs("hset", allcompatible);
//            setBinaryArgs("zadd", allcompatible);
//            setBinaryArgs("srandmember", keyonlycompatible);
//            setBinaryArgs("georadius_ro", keyonlycompatible);
//            setBinaryArgs("set", new boolean[]{false, true, true, false});
//            setBinaryArgs("flushdb", null);
//            setBinaryArgs("xlen", keyonlycompatible);
//            setBinaryArgs("del", allcompatible);
//            setBinaryArgs("save", null);
//            setBinaryArgs("getset", allcompatible);
//            setBinaryArgs("lastsave", null);
//            setBinaryArgs("lindex", keyonlycompatible);
//            setBinaryArgs("cluster", null);
//            setBinaryArgs("get", allcompatible);
//            setBinaryArgs("psetex", new boolean[]{false, true, false, true});
//            setBinaryArgs("debug", null);
//            setBinaryArgs("sinterstore", allcompatible);
//            setBinaryArgs("zpopmin", allcompatible);
//            setBinaryArgs("unsubscribe", allcompatible);
//            setBinaryArgs("zremrangebyrank", keyonlycompatible);
//            setBinaryArgs("mset", allcompatible);
//            setBinaryArgs("geoadd", allcompatible);
//            setBinaryArgs("append", allcompatible);
//            setBinaryArgs("scan", null);
//            setBinaryArgs("multi", null);
//            setBinaryArgs("zcard", keyonlycompatible);
//            setBinaryArgs("module", null);
//            setBinaryArgs("pfdebug", null);
//            setBinaryArgs("setnx", allcompatible);
//            setBinaryArgs("swapdb", null);
////        setBinaryArgs("eval", null);
////        setBinaryArgs("evalsha", null);
//            boolean[] evalbinary = new boolean[]{false, false, false, true};
//            setBinaryArgs("eval", evalbinary);
//            setBinaryArgs("evalsha", evalbinary);
//            setBinaryArgs("pfcount", null);
//            setBinaryArgs("hrandfield", null);
//            setBinaryArgs("restore", keyonlycompatible);
//            setBinaryArgs("exec", null);
//            setBinaryArgs("bgrewriteaof", null);
//            setBinaryArgs("sdiffstore", allcompatible);
//            setBinaryArgs("hincrby", new boolean[]{false, true, true, false});
//            setBinaryArgs("llen", allcompatible);
//            setBinaryArgs("bzpopmin", allcompatible);
//            setBinaryArgs("hscan", keyonlycompatible);
//            setBinaryArgs("zrevrangebyscore", keyonlycompatible);
//            setBinaryArgs("psubscribe", allcompatible);
//            setBinaryArgs("sadd", allcompatible);
//            setBinaryArgs("zrevrange", keyonlycompatible);
//            setBinaryArgs("lolwut", null);
//            setBinaryArgs("unwatch", allcompatible);
//            setBinaryArgs("sismember", allcompatible);
//            setBinaryArgs("zrank", allcompatible);
//            setBinaryArgs("lpos", keyonlycompatible);
//            setBinaryArgs("bgsave", null);
//            setBinaryArgs("incr", keyonlycompatible);
//            setBinaryArgs("zremrangebyscore", keyonlycompatible);
//            setBinaryArgs("zcount", allcompatible);
//            setBinaryArgs("unlink", keyonlycompatible);
//            setBinaryArgs("latency", null);
//            setBinaryArgs("lrem", new boolean[]{false, true, false, true});
//            setBinaryArgs("flushall", null);
//            setBinaryArgs("xsetid", keyonlycompatible);
//            setBinaryArgs("msetnx", allcompatible);
//            setBinaryArgs("zremrangebylex", new boolean[]{false, true, true, true, false});
//            setBinaryArgs("zrangebyscore", keyonlycompatible);
//            setBinaryArgs("punsubscribe", allcompatible);
//            setBinaryArgs("time", null);
//            setBinaryArgs("hmget", allcompatible);
//            setBinaryArgs("zinterstore", new boolean[]{false, true, false, true});
//            setBinaryArgs("blmove", new boolean[]{false, true, true, false});
//            setBinaryArgs("publish", allcompatible);
//            setBinaryArgs("slaveof", null);
//            setBinaryArgs("bzpopmax", allcompatible);
//            setBinaryArgs("brpop", allcompatible);
//            setBinaryArgs("xrange", keyonlycompatible);
//            setBinaryArgs("hlen", keyonlycompatible);
//            setBinaryArgs("geopos", allcompatible);
//            setBinaryArgs("watch", null);
//            setBinaryArgs("zinter", new boolean[]{false, false, true});
//            setBinaryArgs("zrandmember", allcompatible);
//            setBinaryArgs("setex", new boolean[]{false, true, false, true});
//            setBinaryArgs("reset", null);
//            setBinaryArgs("xautoclaim", keyonlycompatible);
//            setBinaryArgs("type", allcompatible);
//            setBinaryArgs("shutdown", null);
//            setBinaryArgs("post", null);
//            setBinaryArgs("getdel", null);
//            setBinaryArgs("xtrim", keyonlycompatible);
//            setBinaryArgs("exists", keyonlycompatible);
//            setBinaryArgs("sync", null);
//            setBinaryArgs("client", null);
//            setBinaryArgs("zrevrank", allcompatible);
//            setBinaryArgs("zincrby", allcompatible);
//            setBinaryArgs("pexpire", keyonlycompatible);
//            setBinaryArgs("failover", null);
//            setBinaryArgs("stralgo", null);
//            setBinaryArgs("dbsize", null);
//            setBinaryArgs("decrby", keyonlycompatible);
//            setBinaryArgs("zdiffstore", allcompatible);
//            setBinaryArgs("echo", allcompatible);
//            setBinaryArgs("georadius", keyonlycompatible);
//            setBinaryArgs("setrange", keyonlycompatible);
//            setBinaryArgs("role", null);
//            setBinaryArgs("substr", keyonlycompatible);
//            setBinaryArgs("rpush", allcompatible);
//            setBinaryArgs("hgetall", allcompatible);
//            setBinaryArgs("decr", keyonlycompatible);
//            setBinaryArgs("acl", null);
//            setBinaryArgs("info", null);
//            setBinaryArgs("script", null);
//            setBinaryArgs("sunionstore", allcompatible);
//            setBinaryArgs("getrange", keyonlycompatible);
//            setBinaryArgs("pexpireat", keyonlycompatible);
//            setBinaryArgs("xreadgroup", new boolean[]{false, false, false, false, true});
//            setBinaryArgs("scard", keyonlycompatible);
//            setBinaryArgs("geodist", new boolean[]{false, true, true, true, false});
//            setBinaryArgs("copy", null);
//            setBinaryArgs("xack", keyonlycompatible);
//            setBinaryArgs("geosearchstore", null);
//            setBinaryArgs("ltrim", keyonlycompatible);
//            setBinaryArgs("subscribe", allcompatible);
//            setBinaryArgs("zrangestore", null);
//            setBinaryArgs("brpoplpush", allcompatible);
//            setBinaryArgs("randomkey", null);
//            setBinaryArgs("move", null);
//            setBinaryArgs("slowlog", null);
//            setBinaryArgs("bitop", new boolean[]{false, false, true});
//            setBinaryArgs("asking", null);
//            setBinaryArgs("bitfield", keyonlycompatible);
//            setBinaryArgs("hdel", allcompatible);
//            setBinaryArgs("sort", null);
//            setBinaryArgs("rename", allcompatible);
//            setBinaryArgs("zdiff", allcompatible);
//            setBinaryArgs("xread", allcompatible);
//            setBinaryArgs("xadd", allcompatible);
//            setBinaryArgs("zpopmax", keyonlycompatible);
//            setBinaryArgs("pttl", keyonlycompatible);
//            setBinaryArgs("blpop", allcompatible);
//            setBinaryArgs("bitcount", keyonlycompatible);
//            setBinaryArgs("xgroup", new boolean[]{false, false, true, false});
//            setBinaryArgs("pfmerge", null);
//            setBinaryArgs("sinter", allcompatible);
//            setBinaryArgs("migrate", null);
//            setBinaryArgs("xpending", keyonlycompatible);
//            setBinaryArgs("setbit", keyonlycompatible);
//            setBinaryArgs("linsert", null);
//            setBinaryArgs("georadiusbymember", new boolean[]{false, true, true, false});
//            setBinaryArgs("zunionstore", new boolean[]{false, true, false, true});
//            setBinaryArgs("sdiff", allcompatible);
//            setBinaryArgs("hget", allcompatible);
//            setBinaryArgs("hello", null);
//            setBinaryArgs("lset", new boolean[]{false, true, false, true});
//            setBinaryArgs("lpush", allcompatible);
//            setBinaryArgs("bitpos", keyonlycompatible);
//            setBinaryArgs("touch", allcompatible);
//            setBinaryArgs("zscore", allcompatible);
//            setBinaryArgs("replicaof", null);
//            setBinaryArgs("xclaim", keyonlycompatible);
//            setBinaryArgs("config", null);
//            setBinaryArgs("hsetnx", allcompatible);
//            setBinaryArgs("strlen", keyonlycompatible);
//            setBinaryArgs("hincrbyfloat", new boolean[]{false, true, true, false});
//            setBinaryArgs("replconf", null);
//            setBinaryArgs("zunion", new boolean[]{false, false, true});
//            setBinaryArgs("keys", null);
//            setBinaryArgs("memory", null);
//            setBinaryArgs("readonly", null);
//            setBinaryArgs("zmscore", null);
//            setBinaryArgs("lmove", null);
//            setBinaryArgs("auth", null);
//            setBinaryArgs("command", null);
//            setBinaryArgs("host:", null);
//            setBinaryArgs("sscan", keyonlycompatible);
//            setBinaryArgs("sunion", allcompatible);
//            setBinaryArgs("getbit", keyonlycompatible);
//            setBinaryArgs("psync", null);
//            setBinaryArgs("smembers", keyonlycompatible);
//            setBinaryArgs("object", null);
//            setBinaryArgs("geosearch", null);
//        }
    }

    public static void add(String name, int args, int positionFirstKey, int positionLastKey, int stepCount
            , boolean readonly, boolean write, boolean denyoom, boolean fast, boolean random, boolean pubsub
            , boolean noscript, boolean loading, boolean stale, boolean sort_for_script, boolean movablekeys
            , boolean admin, boolean skip_monitor) {

        ArrayList command = new ArrayList();
        Vector<StatusString> status = new Vector<>();
        command.add(name);
        command.add(args);
        command.add(status);
        command.add(positionFirstKey);
        command.add(positionLastKey);
        command.add(stepCount);
        if (readonly) {
            status.add(READONLY);
        } else if (write) {
            status.add(WRITE);
        }
        if (denyoom) {
            status.add(DENYOOM);
        }
        if (fast) {
            status.add(FAST);
        }
        if (random) {
            status.add(RANDOM);
        }
        if (pubsub) {
            status.add(PUBSUB);
        }
        if (noscript) {
            status.add(NOSCRIPT);
        }
        if (loading) {
            status.add(LOADING);
        }
        if (stale) {
            status.add(STALE);
        }
        if (sort_for_script) {
            status.add(SORT_FOR_SCRIPT);
        }
        if (movablekeys) {
            status.add(MOVABLEKEYS);
        }
        if (admin) {
            status.add(ADMIN);
        }
        if (skip_monitor) {
            status.add(SKIP_MONITOR);
        }

        COMMANDS.put(name, command);
    }

    public static void setGroup(String cmd, String... groups) {
        if (!ALL_COMMANDS.contains(cmd)) {
            ALL_COMMANDS.add(cmd);
        }

        if (groups != null && groups.length > 0) {
            for (String group : groups) {
                if (COMMANDS.containsKey(cmd)) {
                    List cmds = GROUPS.get(group);
                    if (cmds == null) {
                        cmds = new ArrayList();
                        GROUPS.put(group, cmds);
                    }
                    if (!cmds.contains(cmd)) {
                        cmds.add(cmd);
                    }
                }
                if ("@write".equals(group)) {
                    WRITABLE_COMMANDS.add(cmd);
                } else if ("@read".equals(group)) {
                    READONLY_COMMANDS.add(cmd);
                }
            }
        }
    }

//    public static void setBinaryArgs(String cmd, boolean... args) {
//        if (cmd != null && args != null && args.length > 0) {
//            BINARY_ARGS.put(cmd, args);
//        }
//    }

//    /**
//     * 将命令中的字节数组按照配置转换成字符串
//     *
//     * @param data 命令列表，其中第一项必须是字符串
//     */
//    public static void exchangeList(List data) {
//        // 程序运行到此处时，应该只有data【0】是字符串形式，其他都是字节数组
//        String cmd = (String) data.get(0);
//        boolean[] args = BINARY_ARGS.get(cmd);
//        for (int i = 1; data != null && i < data.size(); ++i) {
//            Object o = data.get(i);
//            String str = null;
//            if (o instanceof byte[]) {
//                byte[] d = (byte[]) o;
//                if (IsBinaryCompatible && args != null && args.length > i && args[i]) {
//                    str = Base64Bytes2String(d, 0, d.length);
//                } else if (IsBinaryCompatible && args != null && args.length > 10 && args.length <= i) {
//                    // args长度大于10，最后2位循环扩展
//                    // 目前mset和msetnx用到此算法
//                    int offset = args.length - 2 + ((i - args.length) % 2);
//                    if (args[offset]) {
//                        str = Base64Bytes2String(d, 0, d.length);
//                    } else {
//                        str = new String(d, StandardCharsets.UTF_8);
//                    }
//                } else if (IsBinaryCompatible && args != null && args.length > 0 && args.length <= i && args[args.length - 1]) {
//                    // args长度不大于10，如果最后一个是true，则后续都是true
//                    str = Base64Bytes2String(d, 0, d.length);
//                } else {
//                    str = new String(d, StandardCharsets.UTF_8);
//                }
//                data.set(i, str);
////            } else if (o instanceof String) {
////                if (i == 0) {
////                    str = ((String) o).toLowerCase();
////                    data.set(i, str);
////                }
//            }
//        }
//    }

//    /**
//     * 从lua脚本执行器中转出来时，调用此方法，
//     *
//     * @param data
//     */
//    public static void exchangeLuaInputEncode(List data) {
//        boolean[] args = null;
//        for (int i = 0; data != null && i < data.size(); ++i) {
//            Object o = data.get(i);
//            String str = null;
//            if (o instanceof String) {
//                String s = (String) o;
//                if (i == 0) {
//                    str = s.toLowerCase();
//                    args = BINARY_ARGS.get(str);
//                } else if (IsBinaryCompatible && args != null && args.length > i && args[i]) {
//                    str = BinaryStringUtil.encode(s);
//                } else if (IsBinaryCompatible && args != null && args.length > 0 && args.length <= i && args[args.length - 1]) {
//                    // 如果最后一个是true，则后续都是true
//                    str = BinaryStringUtil.encode(s);
//                } else {
//                    str = s;
//                }
//                data.set(i, str);
//            }
//        }
//    }

//    public static void exchangeLuaOutputDecode(List data) {
//        for (int i = 0; data != null && i < data.size(); ++i) {
//            Object o = data.get(i);
//            if (o instanceof String) {
//                String str = BinaryStringUtil.decode((String) o);
//                data.set(i, str);
//            }
//        }
//    }

    public static void addCommands(Set<String> cmdSet, String group) {
        List<String> cmds = GROUPS.get(group);
        if (cmds != null) {
            for (String cmd : cmds) {
                cmdSet.add(cmd);
            }
        }
    }

    public static void removeCommands(Set<String> cmdSet, String group) {
        List<String> cmds = GROUPS.get(group);
        if (cmds != null) {
            for (String cmd : cmds) {
                cmdSet.remove(cmd);
            }
        }
    }
}
