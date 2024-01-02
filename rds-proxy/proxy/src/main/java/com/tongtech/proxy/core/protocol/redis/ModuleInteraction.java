package com.tongtech.proxy.core.protocol.redis;

import com.tongtech.proxy.util.Interaction;
import com.tongtech.proxy.util.LOGLEVEL;
import com.tongtech.proxy.util.PluginModule;
import com.tongtech.proxy.core.protocol.redis.callbacks.DataResultPluginImp;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.tongtech.proxy.core.server.io.CodecContext.NULL_STRING_OBJECT;

public class ModuleInteraction implements Interaction, Processer {
    private final static Log logger = ProxyConfig.getServerLog();

    private final Processer MessageCore;
    private final PluginModule Plugin;
    private final String Name;
    private final HashSet<String> InteractedCmds = new HashSet<>();
    private final ThreadLocal<Long> currentTimestamp = new ThreadLocal<>();

    ModuleInteraction(Processer imp, PluginModule plugin) {
        this.MessageCore = imp;
        this.Plugin = plugin;
        this.Name = plugin.getClass().getName();

        String[] cmds = plugin.getCommands();
        for (String cmd : cmds) {
            InteractedCmds.add(cmd);
            logger.debugLog("ModuleInteraction::() PLUGIN command '{}' is intercepted by {}."
                    , cmd, this.Name);
        }

        logger.warnLog("ModuleInteraction::() PLUGIN '{}' is loaded for commands {} ok.", this.Name, Arrays.toString(cmds));
    }

    /**
     * 递归调用，将list中的字符串转换回原始内容的字节数组
     *
     * @param list
     */
    private void exchangeString(List list) {
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); ++i) {
                if (list.get(i) instanceof String) {
                    list.set(i, ((String) list.get(i)).getBytes(StandardCharsets.UTF_8));
                } else if (list.get(i) instanceof Integer) {
                    list.set(i, new Long(((Integer) list.get(i)).longValue()));
                } else if (list.get(i) instanceof List) {
                    exchangeString((List) list.get(i));
                }
            }
        }
    }

    /**
     * 将普通list转换成MustList类型，同时将String类型转换为原始字节数组
     *
     * @param list
     * @return
     */
    private List changeToMustList(List list) {
        Vector vector = new Vector(list.size());
        for (int i = 0; i < list.size(); ++i) {
            Object o = list.get(i);
            if (o instanceof List) {
                vector.add(changeToMustList((List) o));
            } else if (o instanceof String) {
                // 插件返回的是字符串，直接转为字节数组
                vector.add(((String) o).getBytes(StandardCharsets.UTF_8));
            } else {
                vector.add(o);
            }
        }

        return vector;
    }

    //
    // Override to Interaction
    //
    @Override
    public void log(LOGLEVEL level, Object msg) {
        try {
            StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
            if (level == LOGLEVEL.ERROR) {
                logger.errorLog("MODULE-PLUGIN {}::{}() {}", traceElements[2].getClassName(), traceElements[2].getMethodName(), msg);
            } else if (level == LOGLEVEL.WARN) {
                logger.warnLog("MODULE-PLUGIN {}::{}() {}", traceElements[2].getClassName(), traceElements[2].getMethodName(), msg);
            } else if (level == LOGLEVEL.DEBUG) {
                logger.debugLog("MODULE-PLUGIN {}::{}(line={}) {}"
                        , traceElements[2].getClassName(), traceElements[2].getMethodName(), traceElements[2].getLineNumber(), msg);
            } else {
                logger.infoLog("MODULE-PLUGIN {}::{}() {}", traceElements[2].getClassName(), traceElements[2].getMethodName(), msg);
            }
        } catch (Throwable t) {
            logger.warnLog("ModuleInteraction::log error occur: {}", t);
        }
    }

    @Override
    public LOGLEVEL logLevel() {
        int level = logger.getLogLevel();
        if (level == Log.iLogLevelWarning) {
            return LOGLEVEL.WARN;
        } else if (level == Log.iLogLevelInfo) {
            return LOGLEVEL.INFO;
        } else if (level == Log.iLogLevelDebug || level == Log.iLogLevelDump) {
            return LOGLEVEL.DEBUG;
        } else {
            return LOGLEVEL.ERROR;
        }
    }

    /**
     * 访问RDS内部数据的接口
     *
     * @param data data中第一个字段必须是小写的字符串形式的redis兼容的命令，
     *             后面跟命令参数，可以是字符串也可以是字节数组，建议是字节数组
     * @param db   操作对应的数据库表，与Redis一致从0开始。（RDS是从1开始，DataResultPluginImp内部需要转换）
     * @return RDS操作响应的结果，根据对象不同代表不同含义（需要根据实际发送的命令判断返回内容）：
     * :        Boolean：返回的是执行成功（对应协议接口返回的+OK）
     * :        Long：返回的是整数
     * :        Double：返回的是浮点数（RESP3支持）
     * :        String：返回的是简单字符串
     * :        byte[]：返回的是bulk字符串
     * :        List：返回的是数组（数组可以嵌套）
     * :        Map：返回的是map（RESP3协议支持map）
     * :        Exception：返回的是执行失败，getMessage返回失败原因
     */
    @Override
    public Object call(List data, int db) {
        Object retData = null;
        DataResultPluginImp resultPluginImp = new DataResultPluginImp(db);
//        Commands.exchangeList(data);
        try {
            Long curtime = currentTimestamp.get();
            long timestamp = curtime != null ? curtime.longValue() : System.currentTimeMillis();

            MessageCore.process(data, timestamp, resultPluginImp);
            retData = resultPluginImp.getData();
            if (retData instanceof String) {
                // 返回的是简单字符串类型，去掉开头的“+”
                String str = (String) retData;
                if (str.charAt(0) == '+') {
                    retData = str.substring(1);
                }
            } else if (retData instanceof List) {
                // 返回的是list
                exchangeString((List) retData);
            } else if (retData instanceof Integer) {
                retData = new Long(((Integer) retData).longValue());
            } else if (retData instanceof Float) {
                retData = new Double(((Float) retData).doubleValue());
            } else if (retData instanceof List) {
                List list = (List) retData;
                if (list.size() == 1) {
                    // 返回的是普通list，并且只有1行
                    retData = list.get(0);
                    if (retData instanceof String) {
                        // bulk string
                        retData = ((String) retData).getBytes(StandardCharsets.UTF_8);
                    }
                } else {
                    exchangeString(list);
                }
            } else if (retData instanceof Map) {

            }
        } catch (Exception e) {
            retData = new Exception(e.getMessage());
        }

        return retData;
    }

    //
    // Override to ProcessCore
    //
    @Override
    public String name() {
        return this.Name;
    }

    @Override
    public boolean process(List data, long receive_time, DataResult result) throws IOException {
        if (data == null || data.size() == 0) {
            return true;
        }
        String cmd = data.get(0).toString();
        if (InteractedCmds.contains(cmd)) {
            // 被当前插件拦截的命令
            try {
                currentTimestamp.set(receive_time);
                Object ret = Plugin.process(data, this, result.getTableId() - 1);

                result.init(null, data);
                if (ret == null) {
//                    result.foundData(null);
//                    result.finish();
                    result.sendObject(NULL_STRING_OBJECT);
                } else if (ret instanceof Boolean) {
                    result.setOk();
                } else if (ret instanceof Exception) {
                    result.setErr(-9, ((Exception) ret).getMessage());
                } else if (ret instanceof Integer) {
                    result.setOk(((Integer) ret).longValue());
                } else if (ret instanceof Long) {
                    result.setOk(((Long) ret).longValue());
                } else if (ret instanceof List) {
                    result.sendObject(changeToMustList((List) ret));
                } else {
                    // ret is String (simple string), double，byte[] (bulk string), ,and so on
                    result.sendObject(ret);
                }
            } catch (Throwable t) {
                try {
                    result.setErr(-9, t.getMessage());
                } catch (Throwable e) {
                }
                logger.warnLog(t, "ModuleInteraction::process() Error occur in '{}', the next processor is '{}'"
                        , this.Name, MessageCore.name());
            } finally {
                currentTimestamp.remove();
            }
            return true;
        } else {
            // 未被拦截的命令
            return MessageCore.process(data, receive_time, result);
        }
    }
}
