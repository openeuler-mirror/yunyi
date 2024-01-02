package com.tongtech.proxy.core.protocol.redis;

import com.org.luaj.LuaError;
import com.tongtech.proxy.jmx.StatusColector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.Version;
import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.acl.AclFailedException;
import com.tongtech.proxy.core.db.LuaManager;
import com.tongtech.proxy.core.protocol.redis.callbacks.*;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.slices.DirectRedisConnectionAdaptor;
import com.tongtech.proxy.core.slices.RedisConnectionFactory;
import com.tongtech.proxy.core.slices.ServiceMapping;
import com.tongtech.proxy.core.sync.ReplicationNode;
import com.tongtech.proxy.core.sync.SyncMonitor;
import com.tongtech.proxy.core.utils.*;
import com.tongtech.proxy.core.protocol.*;
import com.tongtech.proxy.core.pubsub.PSManager;

import com.tongtech.proxy.core.server.ConnectionCounter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.tongtech.proxy.core.StaticContent.*;
import static com.tongtech.proxy.core.protocol.line.ProcessLineImp.*;
import static com.tongtech.proxy.core.server.io.CodecContext.NULL_STRING_OBJECT;

public class ProcessorRedisImpCore implements Processer {

    // 记录Server启动时间
    private final long ServerStartTime;

    // 日志
    private static final Log logger = ProxyConfig.getServerLog();

    //    private final String HOST_NAME;
//    private final int PID;

//    private static final int SCRIPT_EVEL = 0;
//    private static final int SCRIPT_EVELSHA = 1;
//    private static final int SCRIPT_SCRIPT = 2;
//
//
//    private static final HashMap<String, Integer> SCRIPT_CMDS = new HashMap<String, Integer>() {
//        {
//            this.put("eval", SCRIPT_EVEL);
//            this.put("evalsha", SCRIPT_EVELSHA);
//            this.put("script", SCRIPT_SCRIPT);
//        }
//    };


//    private final LuaRedisModule rdsBridge;

    private final NioProcess ParentProcsser;

    // 当前节点是否为只读节点
//    private final boolean DataReadOnly = "True".equalsIgnoreCase(ProxyConfig.getProperty("Server.Common.DataReadOnly"));

    public ProcessorRedisImpCore(NioProcess processer) {

        ParentProcsser = processer;

        ServerStartTime = System.currentTimeMillis();
    }

    @Override
    public String name() {
        return "ProcessorRedisImpCore";
    }

    @Override
    public boolean process(List argv, long receive_time, DataResult result) throws IOException {

        if (argv == null || argv.size() == 0) {
            return true;
        }

        // 记录收到的请求
        if (logger.isDebug()) {
            logger.debugLog("ProcessorRedisImpCore::process() Receive command: '{}'", argv);
        }

        if (argv == null || argv.size() < 1 || argv.get(0) == null) {
            logger.infoLog("ProcessorRedisImpCore::process() Header is null");
            return true;
        }

        if (DANGEROUS_CMDS.size() > 0 && DANGEROUS_CMDS.contains(argv.get(0))) {
            result.setErr(-9, "ERR unknown command `" + argv.get(0) + "`,");
            logger.warnLog("ProcessorRedisImpCore::process() Dangerous command {} called.", argv.get(0));
            return true;
        }

        int pos = 0;
//        String cmd = argv.get(pos++).toString().toLowerCase();
        String cmd = argv.get(pos++).toString();


        int table_id = result.getTableId();
        byte[] key = argv.size() > pos ? (byte[]) argv.get(pos++) : null;

        Vector<byte[]> value = new Vector<>();
        while (pos < argv.size()) {
            value.add((byte[]) argv.get(pos++));
        }

        // 初始化返回结果
        result.init(null, argv);

        /**
         * 以下是不指定key的命令处理
         */
        if (NOKEY_CMDS.containsKey(cmd)) {
            int iCmd = NOKEY_CMDS.get(cmd);
            if (iCmd != CMD_HELLO) {
                // ACL鉴权
                try {
                    result.aclAuth(cmd, null);
                } catch (AclFailedException afe) {
                    result.setErr(-9, afe.getMessage());
                    return true;
                }
            }
            return process_server(table_id, NOKEY_CMDS.get(cmd), key, value, result, argv);
        } else if (PUBSUB_CMDS.containsKey(cmd)) {
            // pubsub命令和后面的集群分片啥的没关系，所以要提前单独处理
            // sub命令的内容不作为数据存储，因此可以单独处理2进制兼容问题，不受限于其他配置
            try {
                result.aclAuth(cmd, null);

                // pubsub
                if (logger.isInfo()) {
                    StringBuilder buf = new StringBuilder(64);
//                    buf.append("ProcessorRedisImpCore::process() Pubsub:");
                    for (Object v : argv) {
                        buf.append(' ').append(v instanceof byte[] ? StaticContent.escape((byte[]) v, 0, ((byte[]) v).length) : v);
                    }
                    logger.infoLog("ProcessorRedisImpCore::process() Pubsub:{}", buf);
                }
                process_pubsub(PUBSUB_CMDS.get(cmd), receive_time, key, value, result);
            } catch (AclFailedException afe) {
                result.setErr(-9, "ACL failed.");
                logger.infoLog("ProcessorRedisImpCore::process() PubSub ACL failed.");
            } catch (Throwable t) {
                result.setErr(-9, "ACL failed.");
                logger.warnLog("ProcessorRedisImpCore::process() PubSub error: {}", t);
            }
            return true;
        }


        /**
         * 后续处理的命令均需要指定key，且key是第一个参数
         */
        if (key == null) {
            if (Commands.ALL_COMMANDS.contains(cmd) && !DANGEROUS_CMDS.contains(cmd)/*!ProxyConfig.isDangerousCommandFilter()*/) {
                throw new IllegalArgumentException("wrong number of arguments '" + cmd + "'");
            } else {
                throw new IllegalArgumentException("unknown command '" + cmd + "'");
            }
        }

        try {
            // ACL鉴权
            result.aclAuth(cmd, key);

            // 以下代码触发事件监听事件
            if (PSManager.isEventEnabled()) {
                // 有配置事件监听（proxy.xml 中的 Server.Notify.Event）
                if (Commands.WRITABLE_COMMANDS.contains(cmd)) {
                    // 是写命令
                    if (KEY_CMDS.containsKey(cmd) && PSManager.isEventGeneric()
                            || STR_CMDS.containsKey(cmd) && PSManager.isEventString()
                            || LIST_CMDS.containsKey(cmd) && PSManager.isEventList()
                            || SET_CMDS.containsKey(cmd) && PSManager.isEventSet()
                            || HASH_CMDS.containsKey(cmd) && PSManager.isEventHash()
                            || ZSET_CMDS.containsKey(cmd) && PSManager.isEventZset()
                    ) {
                        // 符合触发条件
                        if ("mset".equals(cmd)) {
                            PSManager.eventPublish(result.getTableId(), "set".getBytes(), key);
                            for (int i = 1; i < value.size(); i += 2) {
                                PSManager.eventPublish(result.getTableId(), "set".getBytes(), value.get(i));
                            }
                        } else if ("mdel".equals(cmd)) {
                            PSManager.eventPublish(result.getTableId(), "del".getBytes(), key);
                            for (int i = 0; i < value.size(); i++) {
                                PSManager.eventPublish(result.getTableId(), "del".getBytes(), value.get(i));
                            }
                        } else {
                            PSManager.eventPublish(result.getTableId(), cmd.getBytes(), key);
                        }
                    }
                }
            }

            if (SCRIPT_CMDS.containsKey(cmd)) {
                process_script(table_id, SCRIPT_CMDS.get(cmd), key, value, result, argv);
            } else if (BLOCKINGKEY_CMDS.containsKey(cmd)) {
                new BlockingCallback(result, BLOCKINGKEY_CMDS.get(cmd), argv);
            } else if (MULTIKEY_CMDS.containsKey(cmd)) {
                process_multikeys(table_id, MULTIKEY_CMDS.get(cmd), key, value, result, argv);
            } else if (STREAM_CMDS.containsKey(cmd)) {
                process_stream(table_id, STREAM_CMDS.get(cmd), result, argv);
            } else if (cmd.equals("rpoplpush")) {
                new OthersCallbackRpopLpush(result, argv);
            } else {// 未定义的命令
                ServiceMapping manager = ProxyController.INSTANCE.getNodeManager(key);
                if (manager != null) {
                    manager.exchangeRedisData(result, argv, table_id);
                } else {
                    result.setErr(-4, "ERR null manager for command '" + argv.get(0) + "'");
                }
            }
        } catch (NumberFormatException nfe) {
            result.setErr(-4, "WRONGTYPE " + nfe.getMessage());
            if (logger.isInfo()) {
                logger.infoLog("ProcessorRedisImpCore::process() Format error: {}", nfe.getMessage());
            }
        } catch (NullPointerException npe) {
            result.setErr(-9, "ERR Error executing '" + cmd + "'");

            StringBuilder buf = new StringBuilder(64);
            boolean isagain = false;
            for (Object o : argv) {
                if (isagain) {
                    buf.append(' ');
                }
                buf.append(o);
                isagain = true;
            }
            logger.errorLog(npe, "ProcessorRedisImpCore::process() A fatal error occur when process '{}': {}"
                    , buf, npe.getMessage());
        } catch (AclFailedException afe) {
            result.setErr(-9, "ACL failed: " + cmd);
            logger.warnLog("ProcessorRedisImpCore::process() Command '{}' from {} ({}) is rejected by ACL.", cmd, key);
        } catch (LuaError luaError) {
            result.setErr(-9, "ERR lua error: " + luaError.getMessage());
            logger.warnLog("ProcessorRedisImpCore::process() lua failed: {}", luaError);
        } catch (Throwable e) {
            result.setErr(-9, e.getMessage());
            logger.warnLog(e, "ProcessorRedisImpCore::process() error: {}", e);
        }
        // 保持连接,等待下一个请求
        return true;
    }

    /**
     * 处理所有不需要指定key的命令
     *
     * @param table_id
     * @param cmd
     * @param key
     * @param values
     * @param result
     * @return
     * @throws IOException
     */
    private boolean process_server(int table_id, int cmd, byte[] key, Vector<byte[]> values, DataResult
            result, List argv) throws IOException {
        if (cmd == CMD_QUIT) {
            logger.infoLog("ProcessorRedisImpCore::process_server() Terminated by client");
            result.setOk();
            return false;
        } else if (cmd == CMD_CHECK) {
            result.send("ck " + Long.toString(ServerStartTime) + " "
                    + ProxyConfig.getIdentify());
            logger.infoLog("ProcessorRedisImpCore::process_server() Check response");
        } else if (cmd == CMD_PING) {
            if (key != null) {
                result.sendObject(key);
            } else {
                result.send("PONG");
            }
            logger.infoLog("ProcessorRedisImpCore::process_server() Pong response");
        } else if (cmd == CMD_AUTH) {
            if ((ProxyConfig.getSecureLevel() & 0x2) != 0) {
                result.setOk();
            } else {
                result.setErr(-9, "-ERR AUTH <password> called without password authentication. Are you sure your configuration is correct?");
            }
            logger.infoLog("ProcessorRedisImpCore::process_server() Auth response");
        } else if (cmd == CMD_CLUSTER) {
            if (ProxyConfig.isPretentAsACluster()) {
                process_cluster(key, result);
            } else {
                result.setErr(-1, "ERR  This instance has cluster support disabled");
            }
        } else if (cmd == CMD_COMMAND) {
            return process_command(key, values, result);
        } else if (cmd == CMD_SELECT) {
            // 切换表
            int db = RdsString.parseInt(key);

            int dbmax = 0;
            ServiceMapping mapping = ProxyController.INSTANCE.getNormalManagerDirect(0);
            List list = mapping != null ? mapping.getSliceData() : null;
            // 计算 dbmax
            if (list != null && list.size() > dbmax) {
                dbmax = list.size();
            }

            if (db >= 0 && db < dbmax) {
                result.setTableId(db);
                result.setOk();
                logger.infoLog("ProcessorRedisImpCore::process_server() Select db to db{}", result.getTableId());
            } else {
                result.setErr(-9, "ERR DB index is out of range");
                logger.infoLog("ProcessorRedisImpCore::process_server() Select db error: db{} is out of range.", db);
            }
//        } else if (cmd == CMD_KEYS) {
//            return process_keys(table_id, key, result);
//        } else if (cmd == CMD_SCAN) {
//            return process_scan(table_id, key, values, result);
        } else if (cmd == CMD_DBSIZE) {
            new MultiCallbackDbsize(result, argv);
            logger.infoLog("ProcessorRedisImpCore::process_server() Size of table-{} is {}", table_id);
        } else if (cmd == CMD_ECHO) {
            if (key == null) {
                result.setErr(-9, "ERR wrong number of arguments for 'echo' command");
            } else {
                result.sendObject(key);
            }
        } else if (cmd == CMD_TIME) {
            ArrayList data = new ArrayList();
            data.add(Long.toString(System.currentTimeMillis() / 1000));
            data.add(Long.toString((System.nanoTime() / 1000) % 1000000));
            result.sendObject(data);
            logger.infoLog("ProcessorRedisImpCore::process_server() TIME response");
        } else if (cmd == CMD_CLIENT
                || cmd == CMD_INFO
                || cmd == CMD_COMMAND
                || cmd == CMD_CONFIG
                || cmd == CMD_LASTSAVE) {
            return process_manage(cmd, key, values, result);

        } else if (cmd == CMD_SAVE) {
            new MultiCallbackOk(result, argv);
            StaticContent.setLastSuccessfulSave(System.currentTimeMillis());
            logger.infoLog("ProcessorRedisImpCore::process_server() save ok.");
        } else if (cmd == CMD_FLUSHDB) {
            try {
                new MultiCallbackOk(result, argv);
                logger.infoLog("ProcessorRedisImpCore::process_server() table-{} is cleared.", table_id);
            } catch (Throwable t) {
                result.setErr(-9, "ERR " + t.getMessage());
                logger.warnLog("ProcessorRedisImpCore::process_server() clear table-{} failed: {}"
                        , table_id, t);
            }
        } else if (cmd == CMD_FLUSHALL) {
            try {
                new MultiCallbackOk(result, argv);
                logger.infoLog("ProcessorRedisImpCore::process_server() all tables are cleared.");
            } catch (Throwable t) {
                result.setErr(-9, "ERR " + t.getMessage());
                logger.warnLog("ProcessorRedisImpCore::process_server() clear tables failed: {}", t);
            }
        } else if (cmd == CMD_WAIT) {
            long time = 0;
            try {
                time = RdsString.parseLong(key);
            } catch (Exception e) {
            }
            result.setOk(SyncMonitor.getReplications() - 1);
            logger.infoLog("ProcessorRedisImpCore::process_server() WAIT({}) ok.", time);
        } else if (cmd == CMD_READONLY) {
            result.setErr(-9, "ERR This instance has cluster support disabled");
        } else if (cmd == CMD_ROLE) {
            ArrayList<ReplicationNode> list = SyncMonitor.getReplication();
            Vector obj = new Vector();
            if (list == null || list.get(0).isMyself()) {
                ArrayList slaves = new ArrayList();
                obj.add("master".getBytes(StandardCharsets.UTF_8));
                obj.add(Long.toString(SyncMonitor.get()).getBytes(StandardCharsets.UTF_8));
                obj.add(slaves);
                if (list != null && list.size() > 1) {
                    for (int i = 1; i < list.size(); ++i) {
                        ReplicationNode node = list.get(i);
                        InetSocketAddress addr = node.getRedisAddress();
                        InetSocketAddress slaveRds = node.getAddress();
                        Vector slave = new Vector<>();
                        slave.add(addr.getHostString().getBytes(StandardCharsets.UTF_8));
                        slave.add(Integer.toString(addr.getPort()).getBytes(StandardCharsets.UTF_8));
                        slave.add(Long.toString(SyncMonitor.getExchangedNo(slaveRds)).getBytes(StandardCharsets.UTF_8));
                        slaves.add(slave);
                    }
                }
            } else {
                obj.add("slave".getBytes(StandardCharsets.UTF_8));
                ReplicationNode master = list.get(0);
                InetSocketAddress masterAddr = master.getRedisAddress();
                obj.add(masterAddr.getHostString().getBytes(StandardCharsets.UTF_8));
                obj.add(new Long(masterAddr.getPort()));
                obj.add(master.isAlive() ? "connected".getBytes(StandardCharsets.UTF_8) : "connect".getBytes(StandardCharsets.UTF_8));
                obj.add(StatusColector.getReceivedReplicaOffset());
            }
            result.sendObject(obj);
            logger.infoLog("ProcessorRedisImpCore::process_server() role in {} ok.", ((list == null || list.get(0) == null) ? "master" : "slave"));
        } else if (cmd == CMD_KEYS) {
            new MultiCallbackKeys(result, argv);
        } else if (cmd == CMD_SCAN) {
            new MultiCallbackScan(result, argv);
        } else if (cmd == CMD_SLOWLOG) {
            String k = new String(key);
            if ("Get".equalsIgnoreCase(k)) {
                int count = 10;
                try {
                    count = RdsString.parseInt(values.get(0));
                } catch (Throwable t) {
                }
                List data = SlowLogs.get(count);
                result.sendObject(data);
                logger.infoLog("ProcessorRedisImpCore::process_server() get {} slow logs.", data.size());
            } else if ("Len".equalsIgnoreCase(k)) {
                int len = SlowLogs.len();
                result.setOk(len);
            } else if ("Reset".equalsIgnoreCase(k)) {
                SlowLogs.reset();
                result.setOk();
            } else {
                result.setErr(-9, "Err unknown para for slowlog");
            }
        } else {
            result.setErr(-9, "ERR unknown command '" + argv.get(0) + "'");
            logger.warnLog("ProcessorRedisImpCore::process_server() Unknow command: {}", argv.get(0));
        }
        return true;
    }


    /**
     * @param cmd
     * @param receive_time
     * @param values
     * @param result
     * @return
     * @throws IOException
     * @throws IllegalStateException
     */
    private boolean process_pubsub(int cmd, long receive_time, byte[] key, Vector<byte[]> values, DataResult
            result)
            throws IOException, IllegalStateException {
        try {
            PSManager.process(cmd, key, values, result, false);
        } catch (NumberFormatException nfe) {
            if (logger.isInfo()) {
                logger.infoLog("ProcessorRedisImpCore::process_pubsub() Format error in pubsub(" + cmd
                        + "): " + nfe.toString());
            }
            if (logger.isDebug()) {
                // nfe.printStackTrace();
            }
            result.setErr(2, nfe.toString());
        } catch (IllegalStateException ise) {
            if (logger.isInfo()) {
                logger.infoLog("ProcessorRedisImpCore::process_pubsub() Format error in pubsub({}): {}", cmd, ise.getMessage());
            }
            result.setErr(-9, ise.getMessage());
        } catch (Exception e) {
            if (logger.isDebug()) {
                logger.debugLog(e, "ProcessorRedisImpCore::process_pubsub() Process error in pubsub {}<...> = '': "
                        , cmd, values, e.getMessage());
            } else if (logger.isInfo()) {
                logger.infoLog("ProcessorRedisImpCore::process_pubsub() Process error in pubsub {}<...> = '': "
                        , cmd, values, e);
            }
            if (logger.isDebug()) {
                e.printStackTrace();
            }
            return false;
        }
        return true;
    }


    /**
     * 仿真redis的command命令，返回一个命令列表
     *
     * @param k
     * @param value
     * @param result
     * @return
     * @throws IOException
     */
    private boolean process_command(byte[] k, Vector<byte[]> value, DataResult result) throws IOException {
        String key = null;
        if (k != null) {
            key = new String(k, StandardCharsets.UTF_8);
        }
        if (key == null) {
            ArrayList cmds = new ArrayList();
            cmds.addAll(Commands.COMMANDS.values());
            result.sendObject(cmds);
            logger.infoLog("ProcessorRedisImpCore::process_command() command");
        } else if ("count".equalsIgnoreCase(key)) {
            result.setOk(Commands.COMMANDS.size());
            logger.infoLog("ProcessorRedisImpCore::process_command() command count");
        } else if ("info".equalsIgnoreCase(key)) {
            Vector cmds = new Vector();
            if (value.size() > 0) {
                for (byte[] b : value) {
                    String cmd = new String(b);
                    List context = Commands.COMMANDS.get(cmd);
                    cmds.add(context);
                }
            }
            result.sendObject(cmds);
            logger.infoLog("ProcessorRedisImpCore::process_command() command info {}", value);
        } else {
            result.setErr(-9, "ERR unknown subcommand or wrong number of arguments");
            logger.infoLog("ProcessorRedisImpCore::process_command() Unknown subcommand or wrong number of arguments: {}", key);
        }
        return true;
    }


    /**
     * lua脚本命令的处理函数
     *
     * @param table_id
     * @param cmd
     * @param key
     * @param values
     * @param result
     * @return
     * @throws Exception
     */
    private boolean process_script(int table_id, int cmd, byte[] key, Vector<byte[]> values, DataResult result, List argv) throws IOException {
        return LuaManager.process(cmd, key, values, result, argv, ParentProcsser);
    }
//        private boolean process_script(int table_id, int cmd, byte[] key, Vector<byte[]> values, DataResult result, List argv) throws IOException {
//        try {
//            ConcurrentHashMap<String, CompiledScript> cacher = result.getLuaCatcher();
//            if (cacher == null) {
//                cacher = luaCacher;
//            }
//            if (cmd == SCRIPT_EVEL || cmd == SCRIPT_EVELSHA) {
////            this.rdsBridge.setTableId(table_id);
////            this.rdsBridge.setAcl(result.getAcl());
//                CompiledScript cs = null;
//                if (cmd == SCRIPT_EVEL) {
//                    cs = compiledScript(new String(key, StandardCharsets.UTF_8), cacher);
//                } else {
//                    cs = cacher.get(new String(key, StandardCharsets.UTF_8));
//                }
//                if (cs != null) {
//                    final CompiledScript finalCs = cs;
//                    ScriptProcessService.execute(() -> {
//                        try {
//                            Bindings bindings = engine.createBindings();
//
//                            // 加载lua扩展模块
//                            // Lua插件限制处
//                            for (String name : LuaObjects.keySet()) {
//                                bindings.put(name, LuaObjects.get(name));
//                            }
//
//                            int n = 0;
//                            if (values.size() > 0) {
//                                try {
//                                    n = RdsString.parseInt(values.get(0));
//                                } catch (Throwable t) {
//                                }
//                            }
//                            // KEYS
//                            LuaTable ks = new LuaTable();
//                            if (n > 0 && values.size() >= n + 1) {
//                                for (int i = 1; i < n + 1; ++i) {
//                                    LuaString luaString = LuaString.valueOf(values.get(i));
//                                    ks.set(i, luaString);
//                                }
//                            }
//                            bindings.put("KEYS", ks);
//
//                            // ARGV
//                            LuaTable vs = new LuaTable();
//                            if (values.size() > n + 1) {
//                                for (int i = n + 1; i < values.size(); ++i) {
//                                    LuaString luaString = LuaString.valueOf(values.get(i));
//                                    vs.set(i - n, luaString);
//                                }
//                            }
//                            bindings.put("ARGV", vs);
//
//                            Object o = null;
//                            synchronized (engine) {
//                                CurrentLuaRunning = Thread.currentThread();
//                                try {
//                                    RedisLib.setRuntime(ParentProcsser, result);
//                                    o = finalCs.eval(bindings);
//                                } finally {
//                                    CurrentLuaRunning = null;
//                                }
//                            }
//
//                            // 以下处理返回值
//                            if (o instanceof LuaBoolean) {
//                                if (((LuaBoolean) o).booleanValue()) {
//                                    result.setOk();
//                                } else {
//                                    result.setOk(0);
//                                }
//                            } else if (o instanceof LuaLong) {
//                                result.setOk(((LuaLong) o).v);
//                            } else if (o instanceof LuaTable) {
//                                result.sendObject(luatable2Java((LuaTable) o));
//                            } else if (o instanceof LuaUserdata) {
//                                LuaUserdata userdata = (LuaUserdata) o;
//                                Object data = userdata.userdata();
//                                result.sendObject(data);
//                            } else if (o == LuaValue.NONE) {
//                                // 当lua脚本中没有返回参数时，程序得到的返回值是LuaValue.NONE，redis执行此类脚本返回的是”$-1“
//                                // result.sendObject(new MustListVector()); 返回的是”*0“
//                                result.sendObject(NULL_STRING_OBJECT);
//                            } else if (o == LuaValue.NIL) {
//                                result.sendObject(NULL_STRING_OBJECT);
//                            } else if (o instanceof LuaString) {
//                                LuaString ls = (LuaString) o;
//                                byte[] b = new byte[ls.m_length];
//                                ls.copyInto(0, b, 0, b.length);
//                                if (b.length > 0 && b[0] == '+') {
//                                    result.send(new String(b, StandardCharsets.UTF_8));
//                                } else {
//                                    // bulk string，任意内容
//                                    result.sendObject(b);
//                                }
//                            } else {
//                                result.sendObject(o.toString().getBytes(StandardCharsets.UTF_8));
//                            }
//                        } catch (Throwable t) {
//                            try {
//                                result.sendObject(new IOException("ERR " + t));
//                            } catch (IOException e) {
//                                // do
//                            }
//                            logger.warnLog("ProcessorRedisImpCore::process_script() Script process '{}' failed: {}", key, t);
//                        }
//                    });
//                    return true;
//                } else {
//                    result.setErr(-9, "NOSCRIPT No matching script. Please use EVAL.");
//                    logger.warnLog("ProcessorRedisImpCore::process_script() No matching script for: {}", key);
//                }
//            } else if (cmd == SCRIPT_SCRIPT) {
//                String skey = new String(key, StandardCharsets.UTF_8);
//                if ("exists".equalsIgnoreCase(skey)) {
//                    int n = 0;
//                    for (int i = 0; i < values.size(); i++) {
//                        if (cacher.containsKey(values.get(i))) {
//                            ++n;
//                        }
//                    }
//                    result.setOk(n);
//                } else if ("flush".equalsIgnoreCase(skey)) {
//                    cacher.clear();
//                    result.setOk();
//                } else if ("load".equalsIgnoreCase(skey)) {
//                    String msg = new String(values.get(0), StandardCharsets.UTF_8);
//                    if (compiledScript(msg, cacher) != null) {
//                        result.sendObject(getSHA(msg).getBytes(StandardCharsets.UTF_8));
//                    } else {
//                        result.setErr(-9, "ERR compile error.");
//                    }
//                } else if ("kill".equalsIgnoreCase(skey)) {
//                    Thread cur = CurrentLuaRunning;
//                    if (cur != null) {
//                        cur.interrupt();
//                    }
//                    result.setOk();
//                } else {
//                    result.setErr(-9, "ERR unknow command: script " + key);
//                }
//            } else {
//                result.setErr(-9, "ERR unknown command.");
//                logger.warnLog("ProcessorRedisImpCore::process_script() Unknown script command: {}", cmd);
//            }
//        } catch (NumberFormatException nfe) {
//            result.setErr(-4, "WRONGTYPE " + nfe.getMessage());
//            if (logger.isInfo()) {
//                logger.infoLog("ProcessorRedisImpCore::process_script() Format error: {}", nfe.getMessage());
//            }
//        } catch (NullPointerException npe) {
//            result.setErr(-9, "ERR Error executing '" + cmd + "'");
//
//            StringBuilder buf = new StringBuilder(64);
////            buf.append("ProcessorRedisImpCore::process() A fatal error occur when process '");
//            boolean isagain = false;
//            for (Object o : argv) {
//                if (isagain) {
//                    buf.append(' ');
//                }
//                buf.append(o);
//                isagain = true;
//            }
////            buf.append('\'');
//            logger.errorLog(npe, "ProcessorRedisImpCore::process_script() A fatal error occur when process '{}': {}"
//                    , buf, npe.getMessage());
//        } catch (AclFailedException afe) {
//            result.setErr(-9, "ACL failed: " + cmd);
//            logger.warnLog("ProcessorRedisImpCore::process_script() Command '{}' from {} ({}) is rejected by ACL.", cmd, key);
//        } catch (LuaError luaError) {
//            result.setErr(-9, "ERR lua error: " + luaError.getMessage());
//            logger.warnLog("ProcessorRedisImpCore::process_script() lua failed: {}", luaError);
//        } catch (Throwable e) {
//            result.setErr(-9, e.getMessage());
//            logger.warnLog(e, "ProcessorRedisImpCore::process_script() error: {}", e);
//        }
//
//        return true;
//    }

    private void process_multikeys(int table_id, int cmd, byte[] k, Vector<byte[]> values, DataResult result, List request) throws IOException {
        if (cmd == CMD_MSET) {
            new SeveralCallbackMset(result, request);
        } else if (cmd == CMD_MSETNX) {
            new SeveralCallbackMsetNx(result, request);
        } else if (cmd == CMD_MGET) {
            new SeveralCallbackMget(result, request);
        } else if (cmd == CMD_DEL) {
            new SeveralCallbackDel(result, request);
        } else if (cmd == CMD_EXISTS) {
            new SeveralCallbackExists(result, request);
        } else if (cmd == CMD_PFMERGE) {
            new SeveralCallbackPfmerge(result, request);
        } else {
            result.setErr(-1, "ERR unsupported command");
        }
    }

    private void process_stream(int table_id, int cmd, DataResult result, List request) throws IOException {
        // 以下判断命令是否可能阻塞
        boolean is_blocking = false;
        if (cmd == CMD_XREAD) {
            int pos = 1;
            while (pos < request.size()) {
                String str = new String((byte[]) request.get(pos++), StandardCharsets.ISO_8859_1);
                if ("Count".equalsIgnoreCase(str)) {
//                    count = (int) BinaryStringUtil.parseLong(value.get(pos++));
                    pos++;
                } else if ("Block".equalsIgnoreCase(str)) {
                    is_blocking = true;
                } else if ("Streams".equalsIgnoreCase(str)) {
                    break;
                }
            }
        } else if (cmd == CMD_XREADGROUP) {
            int pos = 4;
            while (pos < request.size()) {
                String str = new String((byte[]) request.get(pos++), StandardCharsets.ISO_8859_1);
                if ("Count".equalsIgnoreCase(str)) {
//                    count = (int) BinaryStringUtil.parseLong(value.get(pos++));
                    pos++;
                } else if ("Block".equalsIgnoreCase(str)) {
                    is_blocking = true;
                } else if ("NoAck".equalsIgnoreCase(str)) {
                } else if ("Streams".equalsIgnoreCase(str)) {
                    break;
                }
            }
        }

        // 所有命令都会发到第一个slice
        ServiceMapping manager = ProxyController.INSTANCE.getFirstManager();
        if (is_blocking) {
            // 有可能阻塞的命令
            Channel channel = manager.getDirectRedisConnection(result, table_id);
            if (channel != null) {
                channel.writeAndFlush(request);
            } else {
                result.setErr(-9, "ERR Data is being maintained");
                logger.infoLog("ProcesserRedisImpCore::process_stream() data is in maintained");
            }
        } else {
            // 正常命令
            manager.exchangeRedisData(result, request, table_id);
        }
    }

    private boolean process_manage(int cmd, byte[] k, Vector<byte[]> values, DataResult result)
            throws IOException, IllegalStateException {
        String key = null;
        try {
            key = k != null ? new String(k, StandardCharsets.UTF_8) : null;
            if (cmd == CMD_CLIENT) {
                byte[] value = getFromBytesList(values, 0);
                if ("setname".equalsIgnoreCase(key)) {
                    Object sess = result.getSession();
                    if (sess != null) {
                        SessionAttribute attribute = CachedSessionAttributes.get(sess);
                        attribute.setClientName(new String(value, StandardCharsets.UTF_8));
                    }
                    result.setOk();
                } else if ("getname".equalsIgnoreCase(key)) {
                    Object sess = result.getSession();
                    SessionAttribute attribute = null;
                    if (sess != null) {
                        attribute = CachedSessionAttributes.get(sess);
                    }
                    if (attribute != null && attribute.getClientName() != null) {
                        result.sendObject(attribute.getClientName().getBytes(StandardCharsets.UTF_8));
//                        result.foundData(BinaryStringUtil.encode(attribute.getClientName()));
                    } else {
//                        result.foundData(null);
//                        result.finish();
                        result.sendObject(NULL_STRING_OBJECT);
                    }
                } else if ("list".equalsIgnoreCase(key)) {
                    // id=25 addr=127.0.0.1:45170 fd=11 name= age=4 idle=0 flags=N db=0 sub=0 psub=0 multi=-1 qbuf=13 qbuf-free=32755 obl=0 oll=0 omem=0 events=r cmd=client
                    StringBuilder buf = new StringBuilder(64);
                    Enumeration<ChannelHandlerContext> ctxes = ClientManager.getClients();
                    while (ctxes.hasMoreElements()) {
                        try {
                            ChannelHandlerContext ctx = ctxes.nextElement();
                            long age = (System.currentTimeMillis() - ClientManager.getStart(ctx)) / 1000;
                            SessionAttribute attribute = CachedSessionAttributes.get(ctx);
                            String clientName = attribute.getClientName();
                            long clinetid = attribute.getClientId();
                            InetSocketAddress address = attribute.getRemoteAddress();
                            String addr = "null";
                            if (address != null) {
                                addr = address.getHostString() + ":" + address.getPort();
                            }
                            buf.append("id=").append(clinetid);
                            buf.append(" addr=").append(addr);
                            buf.append(" fd=").append(clinetid);
                            buf.append(" name=").append(clientName != null ? clientName : "");
                            buf.append(" age=").append(age);
                            buf.append(" idle=0 flags=N");
                            buf.append(" db=").append(result.getTableId() - 1);
                            buf.append(" sub=0 psub=0 multi=-1 qbuf=13 qbuf-free=32755 obl=0 oll=0 omem=0 events=r cmd=client");
                            buf.append('\n');
                        } catch (Throwable t) {
                        }
                    }
//                    result.foundData(BinaryStringUtil.encode(buf.toString()));
//                    result.finish();
                    result.sendObject(buf.toString().getBytes(StandardCharsets.UTF_8));
                } else {
                    result.setErr(-9, "-ERR Unknown subcommand or wrong number of arguments for '" + key + "'");
                    logger.infoLog("ProcessorRedisImpCore::process_manage() Unknow subcommand for client: {}", key);
                }
            } else if (cmd == CMD_LASTSAVE) {
                result.sendObject(StaticContent.getLastSuccessfulSave() / 1000);
            } else if (cmd == CMD_CONFIG) {
                if ("get".equalsIgnoreCase(key)) {
                    ArrayList data = new ArrayList();
                    byte[] subs = getFromBytesList(values, 0);
                    String sub_cmd = null;
                    if (subs != null) {
                        sub_cmd = new String(subs, StandardCharsets.UTF_8);
                    }
//                    if ("databases".equalsIgnoreCase(sub_cmd) || "*".equals(sub_cmd)) {
//                        result.foundData("databases");
////                        result.foundData(BinaryStringUtil.encode(Integer.toString(Servers.length - 1)));
//                    }
                    if ("notify-keyspace-events".equalsIgnoreCase(sub_cmd) || "*".equals(sub_cmd)) {
                        //                        result.foundData("notify-keyspace-events")
                        data.add("notify-keyspace-events".getBytes(StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        if (PSManager.isEventEnabled()) {
                            if (PSManager.isEventKeySpace()) {
                                sb.append('K');
                            }
                            if (PSManager.isEventKeyEvent()) {
                                sb.append('E');
                            }
                            if (PSManager.isEventGeneric()) {
                                sb.append('g');
                            }
                            if (PSManager.isEventString()) {
                                sb.append('$');
                            }
                            if (PSManager.isEventList()) {
                                sb.append('l');
                            }
                            if (PSManager.isEventGeneric()) {
                                sb.append('g');
                            }
                            if (PSManager.isEventSet()) {
                                sb.append('s');
                            }
                            if (PSManager.isEventHash()) {
                                sb.append('h');
                            }
                            if (PSManager.isEventZset()) {
                                sb.append('z');
                            }
                            if (PSManager.isEventExpired()) {
                                sb.append('x');
                            }
                            if (PSManager.isEventEvict()) {
                                sb.append('e');
                            }
                        }
                        data.add(sb.toString().getBytes(StandardCharsets.UTF_8));
                    }
                    if ("slowlog-log-slower-than".equalsIgnoreCase(sub_cmd) || "*".equals(sub_cmd)) {
                        data.add("slowlog-log-slower-than".getBytes(StandardCharsets.UTF_8));
                        data.add(Integer.toString(ProxyConfig.getSlowOperationThreshold()).getBytes());
                    }
                    if ("slowlog-max-len".equalsIgnoreCase(sub_cmd) || "*".equals(sub_cmd)) {
                        data.add("slowlog-max-len".getBytes(StandardCharsets.UTF_8));
                        data.add(Integer.toString(ProxyConfig.getSlowOperationMaxLen()).getBytes());
                    }
                    result.sendObject(data);
                } else {
                    result.setErr(-9, "unsupported config command: " + key);
                }
            } else if (cmd == CMD_INFO) {
//                StringBuilder buf = new StringBuilder(1024);
//
//                buf.append("# Server\n");
//                buf.append("redis_version:5.0.12\n");
//                buf.append("redis_git_sha1:00000000\n");
//                buf.append("redis_git_dirty:0\n");
//                buf.append("redis_build_id:5d89f7520e4755b3\r\n");
//                buf.append("redis_mode:standalone\r\n");
//                buf.append("os:Linux 3.10.0-1160.15.2.el7.x86_64 x86_64\r\n");
//                buf.append("arch_bits:64\r\n");
//                buf.append("multiplexing_api:epoll\r\n");
//                buf.append("atomicvar_api:atomic-builtin\r\n");
//                buf.append("gcc_version:4.8.5\r\n");
//                buf.append("process_id:24463\r\n");
//                buf.append("run_id:d2c4cf3e64a6658733124fe926b1a46ff5a89d42\r\n");
//                buf.append("tcp_port:6379\r\n");
//                buf.append("uptime_in_seconds:96452\r\n");
//                buf.append("uptime_in_days:1\r\n");
//                buf.append("hz:10\r\n");
//                buf.append("configured_hz:10\r\n");
//                buf.append("lru_clock:11912585\r\n");
//                buf.append("executable:/home/hadoop/redis-5.0.12/redis-server\r\n");
//                buf.append("config_file:/home/hadoop/redis-5.0.12/./redis.conf\r\n");
//                buf.append("\n");
//
//                buf.append("# Clients\r\n");
//                buf.append("connected_clients:1\r\n");
//                buf.append("client_recent_max_input_buffer:4\r\n");
//                buf.append("client_recent_max_output_buffer:0\r\n");
//                buf.append("blocked_clients:0\r\n");
//                buf.append("\n");
//
//                buf.append("# Memory\r\n");
//                buf.append("used_memory:855632\r\n");
//                buf.append("used_memory_human:856.02K\r\n");
//                buf.append("used_memory_rss:3985408\r\n");
//                buf.append("used_memory_rss_human:3.80M\r\n");
//                buf.append("used_memory_peak:876560\r\n");
//                buf.append("used_memory_peak_human:856.02K\r\n");
//                buf.append("used_memory_peak_perc:95.36%\r\n");
//                buf.append("used_memory_overhead:841926\r\n");
//                buf.append("used_memory_startup:791424\r\n");
//                buf.append("used_memory_dataset:15162\r\n");
//                buf.append("used_memory_dataset_perc:23.09%\r\n");
//                buf.append("allocator_allocated:974096\r\n");
//                buf.append("allocator_active:1212416\r\n");
//                buf.append("allocator_resident:9179136\r\n");
//                buf.append("total_system_memory:33564971008\r\n");
//                buf.append("total_system_memory_human:31.26G\r\n");
//                buf.append("used_memory_lua:37888\r\n");
//                buf.append("used_memory_lua_human:37.00K\r\n");
//                buf.append("used_memory_scripts:0\r\n");
//                buf.append("used_memory_scripts_human:0B\r\n");
//                buf.append("number_of_cached_scripts:0\r\n");
//                buf.append("maxmemory:0\r\n");
//                buf.append("maxmemory_human:0B\r\n");
//                buf.append("maxmemory_policy:noeviction\r\n");
//                buf.append("allocator_frag_ratio:1.24\r\n");
//                buf.append("allocator_frag_bytes:238320\r\n");
//                buf.append("allocator_rss_ratio:7.57\r\n");
//                buf.append("allocator_rss_bytes:7966720\r\n");
//                buf.append("rss_overhead_ratio:0.43\r\n");
//                buf.append("rss_overhead_bytes:-5193728\r\n");
//                buf.append("mem_fragmentation_ratio:4.89\r\n");
//                buf.append("mem_fragmentation_bytes:3170576\r\n");
//                buf.append("mem_not_counted_for_evict:0\r\n");
//                buf.append("mem_replication_backlog:0\r\n");
//                buf.append("mem_clients_slaves:0\r\n");
//                buf.append("mem_clients_normal:49694\r\n");
//                buf.append("mem_aof_buffer:0\r\n");
//                buf.append("mem_allocator:jemalloc-5.1.0\r\n");
//                buf.append("active_defrag_running:0\r\n");
//                buf.append("lazyfree_pending_objects:0\r\n");
//
//                buf.append("# Persistence\r\n");
//                buf.append("loading:0\r\n");
//                buf.append("rdb_changes_since_last_save:0\r\n");
//                buf.append("rdb_bgsave_in_progress:0\r\n");
//                buf.append("rdb_last_save_time:1622521339\r\n");
//                buf.append("rdb_last_bgsave_status:ok\r\n");
//                buf.append("rdb_last_bgsave_time_sec:0\r\n");
//                buf.append("rdb_current_bgsave_time_sec:-1\r\n");
//                buf.append("rdb_last_cow_size:643072\r\n");
//                buf.append("aof_enabled:0\r\n");
//                buf.append("aof_rewrite_in_progress:0\r\n");
//                buf.append("aof_rewrite_scheduled:0\r\n");
//                buf.append("aof_last_rewrite_time_sec:-1\r\n");
//                buf.append("aof_current_rewrite_time_sec:-1\r\n");
//                buf.append("aof_last_bgrewrite_status:ok\r\n");
//                buf.append("aof_last_write_status:ok\r\n");
//                buf.append("aof_last_cow_size:0\r\n");
//                buf.append("\n");
//
//                buf.append("# Stats\r\n");
//                buf.append("total_connections_received:5\r\n");
//                buf.append("total_commands_processed:131\r\n");
//                buf.append("instantaneous_ops_per_sec:0\r\n");
//                buf.append("total_net_input_bytes:4272\r\n");
//                buf.append("total_net_output_bytes:60362\r\n");
//                buf.append("instantaneous_input_kbps:0.00\r\n");
//                buf.append("instantaneous_output_kbps:0.00\r\n");
//                buf.append("rejected_connections:0\r\n");
//                buf.append("sync_full:0\r\n");
//                buf.append("sync_partial_ok:0\r\n");
//                buf.append("sync_partial_err:0\r\n");
//                buf.append("expired_keys:0\r\n");
//                buf.append("expired_stale_perc:0.00\r\n");
//                buf.append("expired_time_cap_reached_count:0\r\n");
//                buf.append("evicted_keys:0\r\n");
//                buf.append("keyspace_hits:24\r\n");
//                buf.append("keyspace_misses:6\r\n");
//                buf.append("pubsub_channels:0\r\n");
//                buf.append("pubsub_patterns:0\r\n");
//                buf.append("latest_fork_usec:267\r\n");
//                buf.append("migrate_cached_sockets:0\r\n");
//                buf.append("slave_expires_tracked_keys:0\r\n");
//                buf.append("active_defrag_hits:0\r\n");
//                buf.append("active_defrag_misses:0\r\n");
//                buf.append("active_defrag_key_hits:0\r\n");
//                buf.append("active_defrag_key_misses:0\r\n");
//                buf.append("\n");
//
//                buf.append("# Replication\r\n");
//                buf.append("role:master\r\n");
//                buf.append("connected_slaves:0\r\n");
//                buf.append("master_replid:1256e7d9aaea3638516ff03140a5d4836aef0774\r\n");
//                buf.append("master_replid2:0000000000000000000000000000000000000000\r\n");
//                buf.append("master_repl_offset:0\r\n");
//                buf.append("second_repl_offset:-1\r\n");
//                buf.append("repl_backlog_active:0\r\n");
//                buf.append("repl_backlog_size:1048576\r\n");
//                buf.append("repl_backlog_first_byte_offset:0\r\n");
//                buf.append("repl_backlog_histlen:0\r\n");
//                buf.append("\n");
//
//                buf.append("# CPU\r\n");
//                buf.append("used_cpu_sys:223.463628\r\n");
//                buf.append("used_cpu_user:199.784439\r\n");
//                buf.append("used_cpu_sys_children:0.004096\r\n");
//                buf.append("used_cpu_user_children:0.001723\r\n");
//                buf.append("\n");
//
//                buf.append("# Cluster\r\n");
//                buf.append("cluster_enabled:0\r\n");
//                buf.append("\n");
//
//                buf.append("# Keyspace\r\n");
//                for (int i = 1; i < Servers.length; ++i) {
//                    if (Servers[i] != null) {
//                        buf.append("db").append(i - 1).append(":keys=").append(Servers[i].dbsize());
//                        buf.append(",expires=0,avg_ttl=0").append("\n");
//                    }
//                }
//
//                result.foundData(buf.toString());
//                result.finish();

                StringBuilder buf = new StringBuilder(1024);

                boolean isAll = "All".equalsIgnoreCase(key);
                if (isAll) {
                    key = null;
                }
                if (key == null || "Server".equalsIgnoreCase(key)) {
                    buf.append("# Server\r\n");
                    buf.append("version:").append(Version.Version).append("\r\n");
                    buf.append("build_time:").append(Version.BuildTime).append("\r\n");
                    if (!ProxyConfig.isAntiRedis()) {
                        buf.append("redis_version:5.0.12\r\n");
                    }
                    buf.append("lic_type:");
                    buf.append("enterprise\r\n");

                    if (!ProxyConfig.isAntiRedis()) {
                        buf.append("redis_mode:");
                    } else {
                        buf.append("rds_mode:");
                    }
                    buf.append("standalone\r\n");
                    buf.append("run_id:").append(ProxyConfig.getIdentify()).append("\r\n");
                    buf.append("tcp_port:").append(ProxyConfig.getRedisPort()).append("\r\n");
                    buf.append("uptime_in_seconds:").append(getStartedSeconds()).append("\r\n");
                    buf.append("uptime_in_days:").append(getStartedSeconds() / 86400).append("\r\n");
//                    buf.append("os:").append(System.getProperty("os.name")).append("\r\n");
//                    buf.append("process_id:").append(PID).append("\r\n");
//                    buf.append("hostname:").append(HOST_NAME).append("\r\n");
                    buf.append("java_version:").append(System.getProperty("java.version")).append(" ( ").append(System.getProperty("java.vm.version")).append(" )").append("\r\n");
                    buf.append("java_name:").append(System.getProperty("java.vm.name")).append("\r\n");
                    buf.append("\r\n");
                }

                if (key == null || "Clients".equalsIgnoreCase(key)) {
                    buf.append("# Clients\r\n");
                    buf.append("connected_clients:").append(ConnectionCounter.getCurrentConnectionsRedis()).append("\r\n");
                    buf.append("max_clients:").append(ProxyConfig.getMaxConnections()).append("\r\n");
                    buf.append("connected_ratio:").append((ConnectionCounter.getCurrentConnectionsRedis() * 1000 / ProxyConfig.getMaxConnections()) / 1000.0).append("\r\n");
                    ConcurrentHashMap<String, ConnectionCounter.Remote> clients = ConnectionCounter.getConnectionsRedis();
                    for (String ip : clients.keySet()) {
                        ConnectionCounter.Remote client = clients.get(ip);
                        if (client != null) {
                            buf.append(ip).append("-connected:").append(client.getCurrentConnected()).append("\r\n");
                            buf.append(ip).append("-received_in_minute:").append(client.getCreatedConnPerMinute()).append("\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                if (key == null || "Memory".equalsIgnoreCase(key)) {
                    long memory;
                    long memory_free;
                    buf.append("# Memory\r\n");
                    // jvm runtime memory
                    memory = StatusColector.getJvmAllocated();
                    memory_free = StatusColector.getJvmFree();
                    buf.append("jvm_allocated_memory:").append(memory).append("\r\n");
                    buf.append("jvm_allocated_memory_human:").append(getMemorySizeHuman(memory)).append("\r\n");
                    buf.append("jvm_used_memory:").append(memory - memory_free).append("\r\n");
                    buf.append("jvm_used_memory_human:").append(getMemorySizeHuman(memory - memory_free)).append("\r\n");
                    buf.append("jvm_free_memory:").append(memory_free).append("\r\n");
                    buf.append("jvm_free_memory_human:").append(getMemorySizeHuman(memory_free)).append("\r\n");
                    buf.append("jvm_max_memory:").append(StatusColector.JVM_AVAILABLE).append("\r\n");
                    buf.append("jvm_max_memory_human:").append(StatusColector.JVM_AVAILABLE_HUMAN).append("\r\n");
                    // total memory
                    buf.append("total_system_memory:").append(StatusColector.getTotalPhysicalMemory()).append("\r\n");
                    buf.append("total_system_memory_human:").append(StatusColector.getTotalPhysicalMemoryHuman()).append("\r\n");
                    buf.append("\r\n");
                }

                if (key == null || "Stats".equalsIgnoreCase(key)) {
                    buf.append("# Stats\r\n");
                    buf.append("total_connections_received:").append(StatusColector.getClientTotalConnections()).append("\r\n");
                    buf.append("total_connections_ratio:").append(StatusColector.getConnectionsRatio()).append("\r\n");
                    buf.append("rejected_connections:").append(StatusColector.getClientRejectConnections()).append("\r\n");
                    buf.append("total_commands_processed:").append(StatusColector.getTotalProcessed()).append("\r\n");
                    buf.append("process_speed:").append(StatusColector.getProcessSecond()).append("\r\n");
                    buf.append("process_speed_minute:").append(String.format("%.1f", StatusColector.getProcessMinute())).append("\r\n");
                    buf.append("expired_keys:").append(StatusColector.getExpiredKeys()).append("\r\n");
                    buf.append("evicted_keys:").append(StatusColector.getEvictedKeys()).append("\r\n");
                    buf.append("keyspace_hits:").append(StatusColector.getTotalKeyspaceHits()).append("\r\n");
                    buf.append("keyspace_misses:").append(StatusColector.getTotalKeyspaceMisses()).append("\r\n");
                    buf.append("total_net_input_bytes:").append(StatusColector.getTotalNetworkInput()).append("\r\n");
                    buf.append("total_net_output_bytes:").append(StatusColector.getTotalNetworkOutput()).append("\r\n");
                    buf.append("instantaneous_input_kbps:").append(StatusColector.getInstantaneousNetworkInput()).append("\r\n");
                    buf.append("instantaneous_output_kbps:").append(StatusColector.getInstantaneousNetworkOutflow()).append("\r\n");

                    buf.append("\r\n");
                }

//                # Replication
//                role:master
//                connected_slaves:1
//                slave0:ip=192.168.0.90,port=6580,state=online,offset=335916,lag=0
//                master_failover_state:no-failover
//                master_replid:b6009356253b89e777cf31824216e8277b5e1c93
//                master_replid2:5781506848f3a64912bbe074b22ffb62905746d3
//                master_repl_offset:335916
//                second_repl_offset:287673
//                repl_backlog_active:1
//                repl_backlog_size:1048576
//                repl_backlog_first_byte_offset:1
//                repl_backlog_histlen:335916

//                # Replication
//                role:slave
//                master_host:192.168.0.90
//                master_port:6579
//                master_link_status:up
//                master_last_io_seconds_ago:6
//                master_sync_in_progress:0
//                slave_repl_offset:335930
//                slave_priority:100
//                slave_read_only:1
//                replica_announced:1
//                connected_slaves:0
//                master_failover_state:no-failover
//                master_replid:b6009356253b89e777cf31824216e8277b5e1c93
//                master_replid2:5781506848f3a64912bbe074b22ffb62905746d3
//                master_repl_offset:335930
//                second_repl_offset:287673
//                repl_backlog_active:1
//                repl_backlog_size:1048576
//                repl_backlog_first_byte_offset:1
//                repl_backlog_histlen:335930


                if (key == null || "Replication".equalsIgnoreCase(key)) {
                    buf.append("# Replication\r\n");
                    ArrayList<ReplicationNode> list = SyncMonitor.getReplication();
                    if (list == null || list.get(0).isMyself()) {
                        buf.append("role:master\r\n");
                        buf.append("master_repl_offset:").append(SyncMonitor.get()).append("\r\n");
                        if (list != null) {
                            int sno = list.size() - 1;
                            buf.append("connected_slaves:").append(sno).append("\r\n");
                            for (int i = 1; i < list.size(); ++i) {
                                ReplicationNode node = list.get(i);
                                InetSocketAddress slaveRedis = node.getRedisAddress();
                                InetSocketAddress slaveRds = node.getAddress();
                                // slave0:ip=192.168.0.90,port=6580,state=online,offset=335916,lag=0
                                buf.append("slave").append(i - 1).append(":ip=").append(slaveRedis.getAddress().getHostAddress())
                                        .append(",port=").append(slaveRedis.getPort()).append(",state=")
                                        .append(node.isAlive() ? "online" : "offline").append(",offset=")
                                        .append(SyncMonitor.getExchangedNo(slaveRds)).append(",lag=0\r\n");
                            }
                        }
                    } else {
                        buf.append("role:slave\r\n");
                        ReplicationNode master = list.get(0);
                        InetSocketAddress masterAddr = master.getRedisAddress();
                        buf.append("master_host:").append(masterAddr.getAddress().getHostAddress()).append("\r\n");
                        buf.append("master_port:").append(masterAddr.getPort()).append("\r\n");
                        buf.append("master_link_status:").append(master.isAlive() ? "up" : "down").append("\r\n");
                        // slave list
                        int sno = list.size() - 1;
                        buf.append("connected_slaves:").append(sno).append("\r\n");
                        for (int i = 1; i < list.size(); ++i) {
                            ReplicationNode slave = list.get(i);
                            InetSocketAddress slaveAddr = slave.getRedisAddress();
                            // slave0:ip=192.168.0.90,port=6580,state=online,offset=335916,lag=0
                            buf.append("slave").append(i - 1).append(":ip=").append(slaveAddr.getAddress().getHostAddress())
                                    .append(",port=").append(slaveAddr.getPort()).append(",state=")
                                    .append(slave.isAlive() ? "online" : "offline").append(",offset=")
                                    .append(slave.isMyself() ? StatusColector.getReceivedReplicaOffset() : 0l).append(",lag=0\r\n");
                        }
                    }
                    buf.append("\r\n");
                }

                if (key == null || "CPU".equalsIgnoreCase(key)) {
                    buf.append("# CPU\r\n");
                    buf.append("used_cpu_sys:").append(StatusColector.getCpuSystemLoad()).append("%\r\n");
                    buf.append("used_cpu_user:").append(StatusColector.getCpuProcessLoad()).append("%\r\n");
                    buf.append("total_cpus:").append(StatusColector.getCpuAvailables()).append("\r\n");
                    buf.append("\r\n");
                }

                if (key == null || "Cluster".equalsIgnoreCase(key)) {
                    buf.append("# Cluster\r\n");
                    if (ProxyConfig.isPretentAsACluster()) {
                        buf.append("cluster_enabled:1\r\n");
                    } else {
                        buf.append("cluster_enabled:0\r\n");
                    }
                    buf.append("\r\n");
                }

                List<ServiceMapping> mappings = ProxyController.INSTANCE.getAllServiceMappings();

                if (key == null || "Slice".equalsIgnoreCase(key)) {
                    buf.append("# Slice\r\n");
                    buf.append("slices:").append(mappings != null ? mappings.size() : -1).append("\r\n");
                    if (mappings != null && mappings.size() > 0) {
                        for (int i = 0; i < mappings.size(); ++i) {
                            ServiceMapping map = mappings.get(i);
                            buf.append("slice").append(i)
                                    .append(":master=").append(map.getHost())
                                    .append(",port=").append(map.getRedisPort())
                                    .append(",start=").append(map.getStart())
                                    .append(",stop=").append(map.getStop() - 1)
                                    .append(",stat=").append(map.getType());
                            List list = map.getSliceData();
                            if (list != null && list.size() > 0) {
                                buf.append(",dbs=").append(list.size());
                                for (int j = 0; j < list.size(); ++j) {
                                    try {
                                        List list1 = (List) list.get(j);
                                        long keys = ((Long) list1.get(0)).longValue();
                                        if (keys > 0) {
                                            buf.append(",db").append(j).append("=").append(keys);
                                        }
                                    } catch (Throwable t) {
                                        buf.append(",db").append(j).append("=").append("-1");
                                    }
                                }
                            }
                            buf.append(",free_conn=").append(map.getFreeConns())
                                    .append("\r\n");
                        }
                    }
                    buf.append("slices_conn_reusable:").append(RedisConnectionFactory.RedisChannelManagers.size()).append("\r\n");
                    buf.append("slices_conn_direct:").append(DirectRedisConnectionAdaptor.DirectRedisChannelManagers.size()).append("\r\n");
                    buf.append("\r\n");
                }

                if (key == null || "Keyspace".equalsIgnoreCase(key)) {
                    buf.append("# Keyspace\r\n");

                    int dbmax = 0;
                    ServiceMapping map = ProxyController.INSTANCE.getNormalManagerDirect(0);
                    List list = map != null ? map.getSliceData() : null;
                    // 计算 dbmax
                    if (list != null && list.size() > dbmax) {
                        dbmax = list.size();
                    }

                    if (dbmax > 0) {
                        long[] keys = new long[dbmax];
                        long[] max = new long[dbmax];
                        long[] blocks = new long[dbmax];
                        long[] expired = new long[dbmax];
                        long[] evicted = new long[dbmax];
                        for (ServiceMapping mapping : mappings) {
                            List sliceData = mapping.getSliceData();
                            if (sliceData != null) {
                                for (int j = 0; j < sliceData.size(); ++j) {
                                    try {
                                        List sliceDataItem = (List) sliceData.get(j);
                                        keys[j] += (Long) sliceDataItem.get(0);
                                        max[j] += (Long) sliceDataItem.get(1);
                                        blocks[j] += (Long) sliceDataItem.get(2);
                                        expired[j] += (Long) sliceDataItem.get(3);
                                        evicted[j] += (Long) sliceDataItem.get(4);
                                    } catch (Throwable t) {
                                        if (logger.isInfo()) {
                                            logger.warnLog(t, "ProcesserRedisImpCore::process_manager() Merge keys error: {}", t.getMessage());
                                        } else {
                                            logger.warnLog("ProcesserRedisImpCore::process_manager() Merge keys error: {}", t);
                                        }
                                    }
                                }
                            }
                        }

                        for (int i = 0; i < keys.length; ++i) {
                            if (keys[i] > 0) {
                                buf.append("db").append(i).append(":keys=").append(keys[i]);
//                                buf.append(",max=").append(max[i]);
                                buf.append(",expires=").append(expired[i]);
                                buf.append(",evicted=").append(evicted[i]);
                                buf.append("\r\n");
                            }
                        }
                    }
                }

                result.sendObject(buf.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                result.setErr(-9, "ERR unknown command-id: " + cmd);
            }
        } catch (NumberFormatException nfe) {
            if (logger.isInfo()) {
                logger.infoLog("ProcessorRedisImpCore::process_manage() Format error in manage({}): {}", key, nfe);
            }
            result.setErr(2, nfe.toString());
        } catch (IllegalStateException ise) {
            if (logger.isInfo()) {
                logger.infoLog("ProcessorRedisImpCore::process_manage() Format error in manage({}): {}", key, ise.getMessage());
            }
            result.setErr(-9, ise.getMessage());
        } catch (Exception e) {
            if (logger.isDebug()) {
                logger.debugLog(e, "ProcessorRedisImpCore::process_manage() Process error in pubsub {} <...> = '{}': {}"
                        , key, values, e);
            } else {
                logger.infoLog("ProcessorRedisImpCore::process_manage() Process error in pubsub {} <...> = '{}': {}"
                        , key, values, e);
            }
            return false;
        }
        return true;
    }

    private boolean process_cluster(byte[] bkey, DataResult result) throws IOException {
        String key = new String(bkey);
        ArrayList<ReplicationNode> list = SyncMonitor.getReplication();
        if (list != null) {
            if ("slots".equalsIgnoreCase(key)) {
                List slots_info = new ArrayList();
                ReplicationNode master = list.get(0);
                ArrayList one_node = new ArrayList();

                // 开始槽位号
                one_node.add(0);

                // 结束槽位号
                one_node.add(16383);

                // 槽位号对应的master节点地址
                ArrayList addr = new ArrayList();
                addr.add(master.getRedisAddress().getRemoteAliasOrHost());
                addr.add(master.getRedisAddress().getRemoteAliasOrPort());
                one_node.add(addr);

                // 槽位号对应的其他节点地址
                for (int i = 1; i < list.size(); ++i) {
                    ReplicationNode slave = list.get(i);
                    // 只有活着的备节点才会出现在slots列表里
                    if (slave != master && slave.isAlive()) {
                        addr = new ArrayList();
//                        addr.add(slave.getRedisAddress().getOriginalAddress());
//                        addr.add(slave.getRedisAddress().getPort());
                        addr.add(slave.getRedisAddress().getRemoteAliasOrHost());
                        addr.add(slave.getRedisAddress().getRemoteAliasOrPort());
                        one_node.add(addr);
                    }
                }
                // 加入一个完整节点信息
                slots_info.add(one_node);

                result.sendObject(slots_info);
                logger.infoLog("ProcessorRedisImpCore::process_cluster() CLUSTER SLOTS: {}", slots_info);
            } else if ("nodes".equalsIgnoreCase(key)) {
//            cluster nodes
//            $763
//            d58a0354ce015bd2ead9481664aebe6250b5fe18 192.168.0.90:6580@16580 slave 7a33a9ba5f1c9f34f78e5fad6649498d10fad995 0 1638160428074 1 connected
//            121e8186b551b254935f6aae2d57bce287c0fb23 192.168.0.90:6679@16679 master - 0 1638160429000 4 connected 5001-10000
//            689964bd00fc1c44f55996ccfe59548b262fa3fc 192.168.0.90:6680@16680 slave 121e8186b551b254935f6aae2d57bce287c0fb23 0 1638160429078 4 connected
//            85d9e535e5d0cb1c59b348224947de940c0c599d 192.168.0.90:6780@16780 slave 557bfcc0402af14c63eaf35efd8b282c0deeb7df 0 1638160430082 5 connected
//            557bfcc0402af14c63eaf35efd8b282c0deeb7df 192.168.0.90:6779@16779 master - 0 1638160431087 5 connected 10001-16383
//            7a33a9ba5f1c9f34f78e5fad6649498d10fad995 192.168.0.90:6579@16579 myself,master - 0 1638160428000 1 connected 0-5000

                StringBuilder buf = new StringBuilder(512);
                ReplicationNode master = list.get(0);
                for (ReplicationNode endPoint : list) {
                    boolean isMaster = endPoint == master;
//                    long timestamp = System.currentTimeMillis() - 2000;
                    buf.append(endPoint.getIdentify());
                    //       buf.append(names[i]);
                    buf.append(' ').append(endPoint.getRedisAddress().getRemoteAliasOrHost()).append(':').append(endPoint.getRedisAddress().getRemoteAliasOrPort());//.append('@').append(10000 + alive.getRedisPort());
                    if (isMaster) {
                        if (endPoint.isAlive()) {
                            buf.append(' ').append(endPoint.isMyself() ? "myself,master" : "master").append(" - 0");
                        } else {
                            buf.append(' ').append(endPoint.isMyself() ? "myself,master,fail" : "master,fail").append(" - 0");
                        }
                    } else {
                        if (endPoint.isAlive()) {
                            buf.append(endPoint.isMyself() ? " myself,slave " : " slave ").append(master.getIdentify()).append(" 0");
                        } else {
                            buf.append(endPoint.isMyself() ? " myself,slave,fail " : " slave,fail ").append(master.getIdentify()).append(" 0");
                        }
                    }
                    buf.append(' ').append(endPoint.getTimestamp()).append(' ').append(1).append(endPoint.isAlive() ? " connected" : " disconnected");
                    if (isMaster) {
                        buf.append(' ');
                        // 槽位号
                        buf.append(0).append('-').append(16383);
                    }
                    buf.append("\n");
                }

//                // 抓包确认：只有“cluster nodes”命令的回车是用的\n，其他命令（像cluster info、info等）里面的回车都是用的\r\n
//                buf.append("\n");

                result.sendObject(buf.toString().getBytes(StandardCharsets.UTF_8));

                logger.infoLog("ProcessorRedisImpCore::process_cluster() CLUSTER: cluster nodes.");
            } else if ("info".equalsIgnoreCase(key)) {
                StringBuilder buf = new StringBuilder();
                buf.append("cluster_state:ok").append("\r\n");
                buf.append("cluster_slots_assigned:16384").append("\r\n");
                buf.append("cluster_slots_ok:16384").append("\r\n");
                buf.append("cluster_slots_pfail:0").append("\r\n");
                buf.append("cluster_slots_fail:0").append("\r\n");
                buf.append("cluster_known_nodes:").append(list.size()).append("\r\n");
                buf.append("cluster_size:").append(1).append("\r\n");
//                buf.append("cluster_current_epoch:" ).append( addrs.size()).append("\r\n");
//                buf.append("cluster_my_epoch:0").append("\r\n");
                result.sendObject(buf.toString().getBytes(StandardCharsets.UTF_8));

                logger.infoLog("ProcessorRedisImpCore::process_cluster() CLUSTER: cluster info.");
            } else {
                result.setErr(-9, "ERR unsupport cluster msg: " + key);
                logger.warnLog("ProcessorRedisImpCore::process_cluster() Unsupport cluster msg {}.", key);
            }
        } else {
            result.setErr(-1, "ERR  This instance has cluster support disabled");
        }
        return true;
    }
}
