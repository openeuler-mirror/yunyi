package com.org.luaj.lib;

import com.org.luaj.*;
import com.tongtech.proxy.core.protocol.NioProcess;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.redis.callbacks.DataResultLuaImp;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

import static com.tongtech.proxy.core.server.io.CodecContext.NULL_ARRAY_OBJECT;
import static com.tongtech.proxy.core.server.io.CodecContext.NULL_STRING_OBJECT;

/**
 * 此类如果被混淆则lua中不能调到process、log等方法。因此只能放这里
 */
public class RedisLib extends OneArgFunction {
    private final static Log logger = ProxyConfig.getServerLog();

    private static volatile NioProcess MessageImp;

    private static volatile DataResult RedisDataResult;

    public final static int LOG_WARNING = 1;
    public final static int LOG_NOTICE = 2;
    public final static int LOG_VERBOSE = 3;
    public final static int LOG_DEBUG = 4;

    public static void setRuntime(NioProcess message, DataResult result) {
        MessageImp = message;
        RedisDataResult = result;
    }

    public RedisLib() {
    }

    public LuaValue call(LuaValue arg) {
        LuaTable t = new LuaTable(0, 10);
        t.set("LOG_WARNING", LOG_WARNING);
        t.set("LOG_NOTICE", LOG_NOTICE);
        t.set("LOG_VERBOSE", LOG_VERBOSE);
        t.set("LOG_DEBUG", LOG_DEBUG);

        bind(t, RedisLib0.class, new String[]{
                "replicate_commands"});
        bind(t, RedisLib2.class, new String[]{
                "log"});
        bind(t, RedisLibV.class, new String[]{
                "call", "pcall"});
        env.set("redis", t);
        PackageLib.instance.LOADED.set("redis", t);
        return t;
    }

    static final class RedisLib0 extends ZeroArgFunction {
        @Override
        public LuaValue call() {
            switch (opcode) {
                case 0:
                    return LuaBoolean.TRUE;
            }
            return LuaValue.NIL;
        }
    }

    static final class RedisLib2 extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue arg1, LuaValue arg2) {
            switch (opcode) {
                case 0:
                    log(arg1.checkint(), arg2.tojstring());
                    return LuaBoolean.TRUE;
            }
            return LuaValue.NIL;
        }
    }

    static final class RedisLibV extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            if (MessageImp != null && RedisDataResult != null) {
                switch (opcode) {
                    case 0:
                        // process
                        try {
                            return process(args);
                        } catch (Throwable e) {
                            throw new LuaError(e);
                        }
                    case 1:
                        // pprocess
                        try {
                            return process(args);
                        } catch (Throwable t) {
                            return LuaValue.varargsOf(new LuaString[]{LuaString.valueOf("ERR"), LuaString.valueOf(t.getMessage())});
                        }
                }
            }
            return LuaValue.NONE;
        }
    }

    public static void log(int level, String msg) {
        if (level == LOG_WARNING) {
            logger.warnLog("LUA-SCRIPT: {}", msg);
        } else if (level == LOG_NOTICE) {
            logger.infoLog("LUA-SCRIPT: {}", msg);
//        } else if (level == LOG_VERBOSE) {
//            logger.infoLog("LUA-SCRIPT: {}", msg);
        } else {
            logger.debugLog("LUA-SCRIPT: {}", msg);
        }
    }

    public static Varargs process(Varargs args) throws Exception {
        Vector data = new Vector<>(11);
        String cmd = null;
        for (int i = 1; i <= args.narg(); ++i) {
            LuaString v = args.checkstring(i);
            if (cmd == null) {
                cmd = v.tojstring().toLowerCase(Locale.ROOT);
                if ("eval".equalsIgnoreCase(cmd) || "evalsha".equalsIgnoreCase(cmd)) {
                    // 不允许嵌套执行脚本
                    return null;
                }
                data.add(cmd);
            } else {
                byte[] b = new byte[v.length()];
                System.arraycopy(v.m_bytes, v.m_offset, b, 0, b.length);
                data.add(b);
            }
        }

        DataResultLuaImp resultLuaImp = new DataResultLuaImp();
        resultLuaImp.setTableId(RedisDataResult.getTableId());
        resultLuaImp.setAcl(RedisDataResult.getAcl());

        synchronized (resultLuaImp) {
            resultLuaImp.init(null, data);
            MessageImp.process(data, resultLuaImp);
            if (resultLuaImp.getData() == null) {
                resultLuaImp.wait(10_000);
            }
        }

        Object o = resultLuaImp.getData();

        if (o instanceof Exception) {
            throw (Exception) o;
        }

        LuaValue v = java2lua(o);

        return v;
    }

    /**
     * 将java的list转换为lua的table
     *
     * @param list
     * @return
     */
    private static LuaValue list2table(List list) {
        LuaTable table = new LuaTable();
        if (list.size() > 0) {
            table.presize(list.size());
            for (int i = 0; i < list.size(); ++i) {
                table.set(i + 1, java2lua(list.get(i)));
            }
        }
        return table;
    }

    private static LuaValue java2lua(Object o) {
        LuaValue ret;
        if (o == NULL_STRING_OBJECT) {
            ret = LuaValue.NIL;
        } else if (o == NULL_ARRAY_OBJECT) {
            ret = new LuaTable();
        } else if (o instanceof List) {
            ret = list2table((List) o);
        } else if (o instanceof Long || o instanceof Integer) {
            ret = LuaNumber.valueOf(((Number) o).longValue());
        } else if (o instanceof Float || o instanceof Double) {
            ret = LuaNumber.valueOf(((Number) o).doubleValue());
//        } else if (o instanceof String) {
//            byte[] bs = BinaryStringUtil.getBytes(o.toString(), true);
//            ret = LuaString.valueOf(bs);
        } else if (o instanceof byte[]) {
            ret = LuaString.valueOf((byte[]) o);
        } else if (o instanceof Boolean) {
            ret = LuaBoolean.TRUE;
//        } else if (o instanceof String) {
//            byte[] b = ((String) o).getBytes(StandardCharsets.UTF_8);
//            ret = LuaString.valueOf(b);
        } else {
            ret = LuaString.valueOf(o.toString());
        }
        return ret;
    }
}
