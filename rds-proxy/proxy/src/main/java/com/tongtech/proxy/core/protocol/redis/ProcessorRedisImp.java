package com.tongtech.proxy.core.protocol.redis;

import com.tongtech.proxy.util.PluginModule;
import org.dom4j.Attribute;
import org.dom4j.Element;
import com.tongtech.proxy.core.protocol.NioProcess;
import com.tongtech.proxy.core.protocol.redis.callbacks.TransationDataResultImp;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.SessionAttribute;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import static com.tongtech.proxy.core.StaticContent.*;
import static com.tongtech.proxy.core.protocol.line.ProcessLineImp.TRANSACTION_CMDS;

public class ProcessorRedisImp implements NioProcess {

    // 日志
    private final Log logger;

    // 增加了plugin链的处理器
    private final Processer TopProcessor;

    // 原始处理器
    private final Processer CoreProcessor;

//    private volatile boolean ServerStarted = false;

    // 慢操作告警线
    private final long SLOW_THRESHOLD = ProxyConfig.getLongProperty("Server.Common.SlowOperationThreshold");

    public ProcessorRedisImp() {
        int max_index = 0;

        // 创建日志类实例
        this.logger = ProxyConfig.getServerLog();

        Processer processor = new ProcessorRedisImpCore(this);

        CoreProcessor = processor;

        // 以下代码初始化插件
        try {
            List<Element> elements = ProxyConfig.getPropertySubList("Server.Plugins");
            if (elements != null && elements.size() > 0) {
                // 反向获取插件列表
                for (int i = elements.size() - 1; i >= 0; --i) {
//                for (Element element : elements) {
                    Element element = elements.get(i);
                    String name = element.getTextTrim();

                    logger.infoLog("MessageRedisImp::() PLUGIN Begin to load '{}'", name);
                    try {
                        Class clazz = this.getClass().getClassLoader().loadClass(name);
                        PluginModule module = (PluginModule) clazz.newInstance();
                        logger.debugLog("MessageRedisImp::() PLUGIN Instance created successfully");

                        try {
                            List<Attribute> attributes = element.attributes();
                            if (attributes != null && attributes.size() > 0) {
                                Properties properties = new Properties();
                                for (Attribute attribute : attributes) {
                                    String n = attribute.getName();
                                    String v = attribute.getText();
                                    properties.put(n, v);
                                    logger.debugLog("MessageRedisImp::() PLUGIN Load attribute {} = '{}'", n, v);
                                }
                                module.init(properties);
                                logger.infoLog("MessageRedisImp::() PLUGIN Call init succeeded");
                            }
                        } catch (Throwable t) {
                            logger.errorLog("MessageRedisImp::() PLUGIN init plugin {} failed: {}", name, t);
                            continue;
                        }

                        processor = new ModuleInteraction(processor, module);

                        logger.warnLog("MessageRedisImp::() PLUGIN Plugin '{}' is loaded.", name);
                    } catch (ClassNotFoundException e) {
                        logger.errorLog("MessageRedisImp::() PLUGIN Class '{}' is not found.", name);
                    } catch (InstantiationException e) {
                        logger.errorLog("MessageRedisImp::() PLUGIN Create new instance for class '{}' failed: {}", name, e.getMessage());
                    } catch (IllegalAccessException e) {
                        logger.errorLog("MessageRedisImp::() PLUGIN The nullary constructor of '{}' is not accessible.", name);
                    } catch (ClassCastException e) {
                        logger.errorLog("MessageRedisImp::() PLUGIN The class '{}' must implements '{}'.", name, PluginModule.class.getName());
                    } catch (IllegalStateException e) {
                        logger.errorLog("MessageRedisImp::() PLUGIN load failed: {}", e.getMessage());
                    } catch (Throwable t) {
                        logger.errorLog("MessageRedisImp::() PLUGIN Load plugin '{}' failed: {}", name, t);
                    }
                }
            }
        } catch (Throwable t) {
            logger.errorLog("MessageRedisImp::() PLUGIN genarol error occur: {}", t);
        }
        TopProcessor = processor;
    }

    /**
     * 支持pipline模式
     *
     * @param object
     * @param result
     * @return
     * @throws IOException
     */
    @Override
    public boolean process(Object object, DataResult result) throws IOException {
        // 收到命令的时间
        long receive_time = System.currentTimeMillis();
        Vector<Object> argv = (Vector<Object>) object;
        if (argv.size() > 0) {
            // 处理事务
            if (TRANSACTION_CMDS.containsKey(argv.get(0))) {
                Object sess = result.getSession();
                if (result.getSession() != null) {
                    SessionAttribute attribute = CachedSessionAttributes.get(sess);
                    List tranList;
                    switch (TRANSACTION_CMDS.get(argv.get(0))) {
                        case CMD_MULTI:
                            tranList = attribute.getTransactionList();
                            if (tranList == null) {
                                tranList = new Vector<>();
                                attribute.setTransactionList(tranList);
                                result.setOk();
                            } else {
                                result.setErr(-9, "ERR MULTI calls can not be nested");
                            }
                            break;
                        case CMD_DISCARD:
                            tranList = attribute.getTransactionList();
                            if (tranList != null) {
                                attribute.setTransactionList(null);
                                result.setOk();
                            } else {
                                result.setErr(-9, "ERR DISCARD without MULTI");
                            }
                            break;
                        case CMD_EXEC:
                            tranList = attribute.getTransactionList();
                            if (tranList != null) {
                                try {
                                    new TransationDataResultImp(result, TopProcessor, tranList);
//                                    result.sendObject(new RawString("*" + tranList.size()));
//                                    for (Object o : tranList) {
//                                        TopProcessor.process((List) o, receive_time, result);
//                                    }
                                } finally {
                                    attribute.setTransactionList(null);
                                    if (logger.isDebug()) {
                                        logger.debugLog("MessageRedisImp::process() exec {} commands ok({})"
                                                , tranList.size(), (System.currentTimeMillis() - receive_time));
                                    }
                                }
                            } else {
                                result.setErr(-9, "ERR EXEC without MULTI");
                            }
                            break;
                        default:
                            result.setOk();
                    }
                }

                return true;
            } // 事务命令处理结束

            Object sess = result.getSession();
            List tranList = null;
            if (sess != null) {
                tranList = CachedSessionAttributes.get(sess).getTransactionList();
            }

            if (tranList != null) {
                if (tranList.size() < 100) {
                    tranList.add(argv);
                    result.send("+QUEUED");
                } else {
                    CachedSessionAttributes.get(sess).setTransactionList(null);
                    result.setErr(1, "ERR too many commands queued");
                    logger.warnLog("MessageRedisImp::process() Too many command queued, close the session({})", sess);
                    // 断开连接
                    return false;
                }
            } else {
                boolean ret;
                // Plugin功能限制
                // 注释掉部分放开后将只允许企业旗舰版执行插件
//                if (AuthorizationStatus >= 100) {
                ret = TopProcessor.process(argv, receive_time, result);
//                } else {
//                    ret = CoreProcesser.process(argv, receive_time, result);
//                }

                if (logger.isDebug()) {
                    logger.debugLog("MessageRedisImp::process() process ok({})"
                            , (System.currentTimeMillis() - receive_time));
                }
                if (SLOW_THRESHOLD > 0) {
                    long consuming = System.currentTimeMillis() - receive_time;
                    if (consuming > SLOW_THRESHOLD) {
                        logger.warnLog("MessageRedisImp::process() Time consuming {} ms for {}", consuming, argv);
                    }
                }
                return ret;
            }
        } else {
            logger.debugLog("MessageRedisImp::process() null received.");
        }
//        }
        return true;
    }
}
