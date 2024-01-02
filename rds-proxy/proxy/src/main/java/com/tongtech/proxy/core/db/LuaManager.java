package com.tongtech.proxy.core.db;

import com.org.luaj.*;
import com.tongtech.proxy.util.PluginModule;
import org.apache.commons.codec.digest.DigestUtils;
import org.dom4j.Attribute;
import org.dom4j.Element;
import com.org.luaj.lib.RedisLib;
import com.org.luaj.script.LuaScriptEngine;
import com.tongtech.proxy.core.acl.AclFailedException;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.NioProcess;
import com.tongtech.proxy.core.protocol.RdsString;
import com.tongtech.proxy.core.sync.SyncManager;
import com.tongtech.proxy.core.sync.SyncSender;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.SingleThreadSequentialExecutor;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.tongtech.proxy.core.StaticContent.*;
import static com.tongtech.proxy.core.server.io.CodecContext.NULL_STRING_OBJECT;

public class LuaManager {
    private static final Log logger = ProxyConfig.getServerLog();

    public static final String LUA_SAVEKEY = "LuaScript";
    public static final byte[] LUA_SAVEKEY_BINARY = LUA_SAVEKEY.getBytes(StandardCharsets.UTF_8);

    private final static ConcurrentHashMap<String, Context> DefaultLuaCacher = new ConcurrentHashMap<>();

    private final static ConcurrentHashMap<String, ConcurrentHashMap<String, Context>> UsersLuaCacher = new ConcurrentHashMap<>();

    private final static LuaScriptEngine engine = new LuaScriptEngine();

    private static final SingleThreadSequentialExecutor ScriptProcessService = new SingleThreadSequentialExecutor();

    // lua功能扩展模块的集合，通过配置文件加载
    // 生效必须要企业版，且 lic_code >= 100
    private final static HashMap<String, Object> LuaObjects = new HashMap<>();

    private volatile static Thread CurrentLuaRunning = null;

    public static void init() {
        try {
            List<Element> elements = ProxyConfig.getPropertySubList("Server.LuaObjects");
            if (elements != null && elements.size() > 0) {
                for (int i = elements.size() - 1; i >= 0; --i) {
                    Element element = elements.get(i);
                    String objectname = element.getName();
                    String classname = element.getTextTrim();

                    logger.infoLog("LuaManager::() LUAOBJECT Begin to load {} = '{}'", objectname, classname);
                    try {
                        Class clazz = LuaManager.class.getClassLoader().loadClass(classname);
                        Object obj = clazz.newInstance();

                        logger.debugLog("MessageRedisImpCore::() LUAOBJECT Instance created successfully");

                        try {
                            List<Attribute> attributes = element.attributes();
                            if (attributes != null && attributes.size() > 0) {
                                for (Attribute attribute : attributes) {
                                    String n = attribute.getName();
                                    String v = attribute.getText();
                                    System.setProperty(classname + "-" + n, v);
                                    logger.debugLog("MessageRedisImpCore::() LUAOBJECT Load attribute {} = '{}'", n, v);
                                }
                                logger.infoLog("MessageRedisImpCore::() LUAOBJECT Call init succeeded");
                            }
                        } catch (Throwable t) {
                            logger.errorLog("MessageRedisImpCore::() LUAOBJECT init plugin {} failed: {}", classname, t);
                            continue;
                        }

                        LuaObjects.put(objectname, obj);

                        logger.infoLog("MessageRedisImpCore::() LUAOBJECT Object {} = '{}' loaded.", objectname, classname);
                    } catch (ClassNotFoundException e) {
                        logger.errorLog("MessageRedisImpCore::() LUAOBJECT Class '{}' is not found.", classname);
                    } catch (InstantiationException e) {
                        logger.errorLog("MessageRedisImpCore::() LUAOBJECT Create new instance for class '{}' failed: {}", classname, e.getMessage());
                    } catch (IllegalAccessException e) {
                        logger.errorLog("MessageRedisImpCore::() LUAOBJECT The nullary constructor of '{}' is not accessible.", classname);
                    } catch (ClassCastException e) {
                        logger.errorLog("MessageRedisImpCore::() LUAOBJECT The class '{}' must implements '{}'.", classname, PluginModule.class.getName());
                    } catch (IllegalStateException e) {
                        logger.errorLog("MessageRedisImpCore::() LUAOBJECT Load failed: {}", e.getMessage());
                    } catch (Throwable t) {
                        logger.errorLog("MessageRedisImpCore::() LUAOBJECT Load object '{}' failed: {}", classname, t);
                    }
                }
            }
        } catch (Throwable t) {
            logger.errorLog("MessageRedisImpCore::() LUAOBJECT genarol error occur: {}", t);
        }
    }

    public static void serialize(SerializeData serializer) throws IOException {
        if (DefaultLuaCacher.size() > 0 || UsersLuaCacher.size() > 0) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            Context[] contexts = DefaultLuaCacher.values().toArray(new Context[0]);
            for (Context context : contexts) {
                oos.writeBoolean(false);
                oos.writeInt(context.originalScript.length);
                oos.write(context.originalScript);
                oos.flush();
                serializer.serialize(os.toByteArray());
                os.reset();
                logger.infoLog("LuaManager::serialize() Serialize script '{}' for all ok.", context.originalScript);
            }
            String[] users = UsersLuaCacher.keySet().toArray(new String[0]);
            for (String user : users) {
                ConcurrentHashMap<String, Context> userContext = UsersLuaCacher.get(user);
                if (userContext != null) {
                    contexts = userContext.values().toArray(new Context[0]);
                    for (Context context : contexts) {
                        oos.writeBoolean(true);
                        oos.writeUTF(user);
                        oos.writeInt(context.originalScript.length);
                        oos.write(context.originalScript);
                        oos.flush();
                        serializer.serialize(os.toByteArray());
                        os.reset();
                        logger.infoLog("LuaManager::serialize() Serialize script '{}' for {} ok.", context.originalScript, user);
                    }
                }
            }
        }
    }

    public static void unserialize(byte[] data) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));

        String user;
        byte[] script;
        try {
            Boolean haveUser = ois.readBoolean();
            if (haveUser) {
                user = ois.readUTF();
            } else {
                user = null;
            }
            int len = ois.readInt();
            script = new byte[len];
            ois.read(script);
            synchronize_load(user, script);
            logger.infoLog("LuaManager::unserialize() Unserialize script '{}' for {} ok."
                    , script, user != null ? user : "all");
        } catch (Throwable t) {
            logger.warnLog("LuaManager::unserialize() Unserialize {} failed: {}", Arrays.toString(data), t);
        }
    }

    public static void synchronize_load(String user, byte[] script) {
        ConcurrentHashMap<String, Context> cacher;
        if (user != null) {
            cacher = UsersLuaCacher.get(user);
            if (cacher == null) {
                synchronized (UsersLuaCacher) {
                    cacher = UsersLuaCacher.get(user);
                    if (cacher == null) {
                        cacher = new ConcurrentHashMap<>();
                        UsersLuaCacher.put(user, cacher);
                    }
                }
            }
        } else {
            cacher = DefaultLuaCacher;
        }
        try {
            CompiledScript cs = compiledScript(script, cacher);
            if (cs != null) {
                String hash = getSHA(script);
                cacher.put(hash, new Context(script, cs));
            }
        } catch (Throwable t) {
        }
    }

    public static void synchronize_flush(String user) {
        if (user != null) {
            synchronized (UsersLuaCacher) {
                UsersLuaCacher.remove(user);
            }
        } else {
            DefaultLuaCacher.clear();
        }
    }

    public static boolean process(int cmd, byte[] key, Vector<byte[]> values, DataResult result, List argv, NioProcess processor) throws IOException {
        try {
            ConcurrentHashMap<String, Context> cacher;
            String user = null;
            if (result.getAcl() == null) {
                cacher = DefaultLuaCacher;
            } else {
                user = result.getAcl().getUserName();
                cacher = UsersLuaCacher.get(user);
                if (cacher == null) {
                    synchronized (UsersLuaCacher) {
                        cacher = UsersLuaCacher.get(user);
                        if (cacher == null) {
                            cacher = new ConcurrentHashMap<>();
                            UsersLuaCacher.put(user, cacher);
                        }
                    }
                }
            }

            if (cmd == CMD_EVEL || cmd == CMD_EVELSHA) {
//            this.rdsBridge.setTableId(table_id);
//            this.rdsBridge.setAcl(result.getAcl());
                CompiledScript cs = null;
                if (cmd == CMD_EVEL) {
                    cs = compiledScript(key, cacher);
                } else {
                    Context context = cacher.get(new String(key, StandardCharsets.UTF_8));
                    cs = context != null ? context.compiledScript : null;
                }
                if (cs != null) {
                    final CompiledScript finalCs = cs;
                    ScriptProcessService.execute(() -> {
                        try {
                            Bindings bindings = engine.createBindings();

                            // 加载lua扩展模块
                            // Lua插件限制处
                            for (String name : LuaObjects.keySet()) {
                                bindings.put(name, LuaObjects.get(name));
                            }

                            int n = 0;
                            if (values.size() > 0) {
                                try {
                                    n = RdsString.parseInt(values.get(0));
                                } catch (Throwable t) {
                                }
                            }
                            // KEYS
                            LuaTable ks = new LuaTable();
                            if (n > 0 && values.size() >= n + 1) {
                                for (int i = 1; i < n + 1; ++i) {
                                    LuaString luaString = LuaString.valueOf(values.get(i));
                                    ks.set(i, luaString);
                                }
                            }
                            bindings.put("KEYS", ks);

                            // ARGV
                            LuaTable vs = new LuaTable();
                            if (values.size() > n + 1) {
                                for (int i = n + 1; i < values.size(); ++i) {
                                    LuaString luaString = LuaString.valueOf(values.get(i));
                                    vs.set(i - n, luaString);
                                }
                            }
                            bindings.put("ARGV", vs);

                            Object o = null;
                            synchronized (engine) {
                                CurrentLuaRunning = Thread.currentThread();
                                try {
                                    RedisLib.setRuntime(processor, result);
                                    o = finalCs.eval(bindings);
                                } finally {
                                    CurrentLuaRunning = null;
                                }
                            }

                            // 以下处理返回值
                            if (o instanceof LuaBoolean) {
                                if (((LuaBoolean) o).booleanValue()) {
                                    result.setOk();
                                } else {
                                    result.setOk(0);
                                }
                            } else if (o instanceof LuaLong) {
                                result.setOk(((LuaLong) o).v);
                            } else if (o instanceof LuaTable) {
                                result.sendObject(luatable2Java((LuaTable) o));
                            } else if (o instanceof LuaUserdata) {
                                LuaUserdata userdata = (LuaUserdata) o;
                                Object data = userdata.userdata();
                                result.sendObject(data);
                            } else if (o == LuaValue.NONE) {
                                // 当lua脚本中没有返回参数时，程序得到的返回值是LuaValue.NONE，redis执行此类脚本返回的是”$-1“
                                // result.sendObject(new MustListVector()); 返回的是”*0“
                                result.sendObject(NULL_STRING_OBJECT);
                            } else if (o == LuaValue.NIL) {
                                result.sendObject(NULL_STRING_OBJECT);
                            } else if (o instanceof LuaString) {
                                LuaString ls = (LuaString) o;
                                byte[] b = new byte[ls.m_length];
                                ls.copyInto(0, b, 0, b.length);
                                if (b.length > 0 && b[0] == '+') {
                                    result.send(new String(b, StandardCharsets.UTF_8));
                                } else {
                                    // bulk string，任意内容
                                    result.sendObject(b);
                                }
                            } else {
                                result.sendObject(o.toString().getBytes(StandardCharsets.UTF_8));
                            }
                        } catch (Throwable t) {
                            try {
                                result.sendObject(new IOException("ERR " + t));
                            } catch (IOException e) {
                                // do
                            }
                            logger.warnLog("ProcessorRedisImpCore::process_script() Script process '{}' failed: {}", key, t);
                        }
                    });
                    return true;
                } else {
                    result.setErr(-9, "NOSCRIPT No matching script. Please use EVAL.");
                    logger.warnLog("ProcessorRedisImpCore::process_script() No matching script for: {}", key);
                }
            } else if (cmd == CMD_SCRIPT) {
                String skey = new String(key, StandardCharsets.UTF_8);
                if ("exists".equalsIgnoreCase(skey)) {
                    ArrayList<Integer> exists = new ArrayList<>();
                    for (int i = 0; i < values.size(); i++) {
                        if (cacher.containsKey(values.get(i))) {
                            exists.add(Integer.valueOf(1));
                        } else {
                            exists.add(Integer.valueOf(0));
                        }
                    }
                    result.sendObject(exists);
                } else if ("flush".equalsIgnoreCase(skey)) {
                    cacher.clear();
                    result.setOk();
                    SyncManager sender = SyncManager.getInstance();
                    if (sender != null) {
                        SyncSender s_server = sender.getSyncServer(-1);
                        if (s_server != null) {
                            s_server.putData(SYNC_CMD | CMD_SCRIPT_FLUSH, user, null, 0, null);
                        }
                    }
                } else if ("load".equalsIgnoreCase(skey)) {
                    if (compiledScript(values.get(0), cacher) != null) {
                        result.sendObject(getSHA(values.get(0)).getBytes(StandardCharsets.UTF_8));
                        SyncManager sender = SyncManager.getInstance();
                        if (sender != null) {
                            SyncSender s_server = sender.getSyncServer(-1);
                            if (s_server != null) {
                                byte[] v = values.get(0);
                                s_server.putData(SYNC_CMD | CMD_SCRIPT_LOAD, user, new ArrayList<String>() {{
                                    this.add(escape(v, 0, v.length));
                                }}, 0, null);
                            }
                        }
                    } else {
                        result.setErr(-9, "ERR compile error.");
                    }
                } else if ("kill".equalsIgnoreCase(skey)) {
                    Thread cur = CurrentLuaRunning;
                    if (cur != null) {
                        cur.interrupt();
                    }
                    result.setOk();
                } else {
                    result.setErr(-9, "ERR unknow command: script " + key);
                }
            } else {
                result.setErr(-9, "ERR unknown command.");
                logger.warnLog("ProcessorRedisImpCore::process_script() Unknown script command: {}", cmd);
            }
        } catch (NumberFormatException nfe) {
            result.setErr(-4, "WRONGTYPE " + nfe.getMessage());
            if (logger.isInfo()) {
                logger.infoLog("ProcessorRedisImpCore::process_script() Format error: {}", nfe.getMessage());
            }
        } catch (NullPointerException npe) {
            result.setErr(-9, "ERR Error executing '" + cmd + "'");

            StringBuilder buf = new StringBuilder(64);
//            buf.append("ProcessorRedisImpCore::process() A fatal error occur when process '");
            boolean isagain = false;
            for (Object o : argv) {
                if (isagain) {
                    buf.append(' ');
                }
                buf.append(o);
                isagain = true;
            }
//            buf.append('\'');
            logger.errorLog(npe, "ProcessorRedisImpCore::process_script() A fatal error occur when process '{}': {}"
                    , buf, npe.getMessage());
        } catch (AclFailedException afe) {
            result.setErr(-9, "ACL failed: " + cmd);
            logger.warnLog("ProcessorRedisImpCore::process_script() Command '{}' from {} ({}) is rejected by ACL.", cmd, key);
        } catch (LuaError luaError) {
            result.setErr(-9, "ERR lua error: " + luaError.getMessage());
            logger.warnLog("ProcessorRedisImpCore::process_script() lua failed: {}", luaError);
        } catch (Throwable e) {
            result.setErr(-9, e.getMessage());
            logger.warnLog(e, "ProcessorRedisImpCore::process_script() error: {}", e);
        }

        return true;
    }

//    public static boolean process1(int cmd, byte[] key, Vector<byte[]> values, DataResult result, NioProcess processor)
//            throws IOException, ScriptException {
//        ConcurrentHashMap<String, Context> cacher;
//        String user = null;
//        if (result.getAcl() == null) {
//            cacher = DefaultLuaCacher;
//        } else {
//            user = result.getAcl().getUserName();
//            cacher = UsersLuaCacher.get(user);
//            if (cacher == null) {
//                synchronized (UsersLuaCacher) {
//                    cacher = UsersLuaCacher.get(user);
//                    if (cacher == null) {
//                        cacher = new ConcurrentHashMap<>();
//                        UsersLuaCacher.put(user, cacher);
//                    }
//                }
//            }
//        }
//
//        if (cmd == CMD_EVEL || cmd == CMD_EVELSHA) {
////            this.rdsBridge.setTableId(table_id);
////            this.rdsBridge.setAcl(result.getAcl());
//            CompiledScript cs = null;
//            if (cmd == CMD_EVEL) {
//                cs = compiledScript(key, cacher);
//            } else {
//                Context context = cacher.get(key);
//                cs = context != null ? context.compiledScript : null;
//            }
//            if (cs != null) {
//                Bindings bindings = engine.createBindings();
////                bindings.put("redis", rdsBridge);
//
//                // 加载lua扩展模块
//                // Lua插件限制处
////                if (AuthorizationStatus >= 100) {
//                for (String name : LuaObjects.keySet()) {
//                    bindings.put(name, LuaObjects.get(name));
//                }
////                }
//
//                int n = 0;
//                if (values.size() > 0) {
//                    try {
//                        n = Integer.parseInt(new String(values.get(0)));
//                    } catch (Throwable t) {
//                    }
//                }
//
//                // binding KEYS
//                LuaTable ks = new LuaTable();
//                if (n > 0 && values.size() >= n + 1) {
////                    LuaString[] ks = new LuaString[n];
//                    for (int i = 1; i < n + 1; ++i) {
////                        ks[i - 1] = values.get(i);
////                        ks.add(values.get(i));
//                        LuaString luaString = LuaString.valueOf(values.get(i));
//                        ks.set(i, luaString);
//                    }
//                }
//                bindings.put("KEYS", ks);
//
//                // binding ARGV
//                LuaTable vs = new LuaTable();
//                if (values.size() > n + 1) {
////                    ArrayList<String> vs = new ArrayList<>();
//                    for (int i = n + 1; i < values.size(); ++i) {
////                        vs[i- n - 1] = values.get(i);
////                        vs.add(values.get(i));
//                        LuaString luaString = LuaString.valueOf(values.get(i));
//                        vs.set(i - n, luaString);
//                    }
//                }
//                bindings.put("ARGV", vs);
//
//                Object o = null;
//                synchronized (engine) {
//                    CurrentLuaRunning = Thread.currentThread();
//                    try {
//                        RedisLib.setRuntime(processor, result);
//                        o = cs.eval(bindings);
//                    } finally {
//                        CurrentLuaRunning = null;
//                    }
//                }
//
//                // 以下处理返回值
//                if (o instanceof LuaBoolean) {
//                    if (((LuaBoolean) o).booleanValue()) {
//                        result.setOk();
//                    } else {
//                        result.setOk(0);
//                    }
//                } else if (o instanceof LuaLong) {
//                    result.setOk(((LuaLong) o).v);
//                } else if (o instanceof LuaTable) {
//                    result.sendObject(luatable2Java((LuaTable) o));
//                } else if (o instanceof LuaUserdata) {
//                    LuaUserdata userdata = (LuaUserdata) o;
//                    Object data = userdata.userdata();
//                    result.sendObject(data);
//                } else if (o == LuaValue.NONE) {
////                    result.sendObject(new MustListVector());
//                    // 当lua脚本中没有返回参数时，程序得到的返回值是LuaValue.NONE，redis执行此类脚本返回的是”$-1“
//                    // result.sendObject(new MustListVector()); 返回的是”*0“
//                    result.sendObject(NULL_STRING_OBJECT);
//                } else if (o == LuaValue.NIL) {
//                    result.sendObject(NULL_STRING_OBJECT);
//                } else if (o instanceof LuaString) {
//                    LuaString ls = (LuaString) o;
//                    byte[] b = new byte[ls.m_length];
//                    ls.copyInto(0, b, 0, b.length);
//                    if (b.length > 0 && b[0] == '+') {
//                        result.send(new String(b, StandardCharsets.UTF_8));
//                    } else {
//                        // bulk string，任意内容
//                        result.sendObject(b);
//                    }
//                } else {
////                    String str = o.toString();
////                    if (str.charAt(0) == '+' /*|| str.charAt(0) == '-'*//*发现lua有可能传回来字符串-1，不能判断‘-’*/) {
////                        result.send(str);
////                    } else {
//                    byte[] data = o.toString().getBytes(StandardCharsets.UTF_8);
//                    result.sendObject(data);
////                    }
//                }
//                return true;
//            } else {
//                result.setErr(-9, "NOSCRIPT No matching script. Please use EVAL.");
//            }
//        } else if (cmd == CMD_SCRIPT) {
//            if ("exists".equalsIgnoreCase(key)) {
//                MustListVector<Integer> list = new MustListVector<>();
//                for (int i = 0; i < values.size(); i++) {
//                    if (cacher.containsKey(values.get(i))) {
//                        list.add(1);
//                    } else {
//                        list.add(0);
//                    }
//                }
//                result.sendObject(list);
//            } else if ("flush".equalsIgnoreCase(key)) {
//                cacher.clear();
//                result.setOk();
//                SyncManager sender = SyncManager.getInstance(1, null);
//                if (sender != null) {
//                    SyncSender s_server = sender.getSyncServer(-1);
//                    if (s_server != null) {
//                        s_server.putData(SYNC_CMD | CMD_SCRIPT_FLUSH, user, values.get(0), 0, null);
//                    }
//                }
//            } else if ("load".equalsIgnoreCase(key)) {
//                if (compiledScript(values.get(0), cacher) != null) {
//                    byte[] script = BinaryStringUtil.getBytes(values.get(0));
//                    result.foundData(getSHA(script));
//                    result.finish();
//                    SyncManager sender = SyncManager.getInstance(1, null);
//                    if (sender != null) {
//                        SyncSender s_server = sender.getSyncServer(-1);
//                        if (s_server != null) {
//                            s_server.putData(SYNC_CMD | CMD_SCRIPT_LOAD, user, values.get(0), 0, null);
//                        }
//                    }
//                } else {
//                    result.setErr(-9, "ERR compile error.");
//                }
//            } else if ("kill".equalsIgnoreCase(key)) {
//                Thread cur = CurrentLuaRunning;
//                if (cur != null) {
//                    cur.interrupt();
//                }
//                result.setOk();
//            } else {
//                result.setErr(-9, "ERR unknow command: script " + key);
//            }
//        } else {
//            result.setErr(-9, "ERR unknow command.");
//            logger.warnLog("unknow script command: {}", cmd);
//        }
//        return true;
//    }

    private static List luatable2List(LuaTable table) {
        List list = new ArrayList();
        int len = table.length();
        for (int i = 0; i < len; ++i) {
            LuaValue o = table.get(i + 1);
            list.add(luaValue2JavaObject(o));
        }
        return list;
    }

    private static Map luatable2Map(LuaTable table) {
        Map map = new Hashtable();
        LuaValue[] keys = table.keys();
        for (LuaValue key : keys) {
            LuaValue value = table.get(key);
            map.put(luaValue2JavaObject(key), luaValue2JavaObject(value));
        }
        return map;
    }

    private static Object luatable2Java(LuaTable table) {
        if (table.isMap()) {
            return luatable2Map(table);
        } else {
            return luatable2List(table);
        }
    }

    /**
     * 将lua对象转换为java对象的主方法
     *
     * @param o
     * @return
     */
    private static Object luaValue2JavaObject(Object o) {
        if (o instanceof LuaUserdata) {
            Object jo = ((LuaUserdata) o).m_instance;
            if (jo instanceof LuaValue) {
                o = jo;
            }
        }
        if (o == LuaValue.NIL) {
            return null;
        } else if (o == LuaValue.NONE) {
            return null;
        } else if (o instanceof LuaTable) {
            return luatable2Java((LuaTable) o);
        } else if (o instanceof LuaLong) {
            return new Long(((LuaLong) o).v);
        } else if (o instanceof LuaString) {
            // bulk string，任意内容
            LuaString ls = (LuaString) o;
            byte[] b = new byte[ls.m_length];
            ls.copyInto(0, b, 0, b.length);
            return b;
        } else {
            return o.toString();
        }
    }

    /**
     * 编译脚本并将编译结果缓存
     *
     * @return
     */
    private static CompiledScript compiledScript(byte[] script, ConcurrentHashMap<String, Context> luaCacher) throws
            ScriptException {
        String sha = getSHA(script);
        CompiledScript compiledScript = null;
        synchronized (luaCacher) {
            if (luaCacher.get(sha) == null) {
                String str_script = new String(script, StandardCharsets.UTF_8);
                try {
                    CompiledScript cs = ((Compilable) engine).compile(str_script);
                    luaCacher.put(sha, new Context(script, cs));
                    logger.infoLog("MessageRedisImpCore::compiledScript() compile '{}' ok.", str_script);
                } catch (Throwable t) {
                    logger.warnLog(t, "MessageRedisImpCore::compiledScript() error in compile '{}': {}", str_script, t);
                    throw new ScriptException("ERR Error compiling script: " + t.getMessage());
                }
            }
            compiledScript = luaCacher.get(sha).compiledScript;
        }
        return compiledScript;
    }

    private static String getSHA(byte[] script) {

        return DigestUtils.sha1Hex(script);
    }

    private static class Context {
        private final byte[] originalScript;
        private final CompiledScript compiledScript;

        private Context(byte[] orgString, CompiledScript cs) {
            this.originalScript = orgString;
            this.compiledScript = cs;
        }
    }
}
