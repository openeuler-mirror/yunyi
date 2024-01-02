package com.tongtech.proxy.core.server;

import com.tongtech.proxy.core.center.ProxyData;
import com.tongtech.proxy.core.slices.ServiceMappingNormal;
import com.tongtech.proxy.core.slices.ServiceMapping;
import com.tongtech.proxy.core.slices.ServiceMappingReadOnly;
import com.tongtech.proxy.core.utils.*;

import java.util.ArrayList;
import java.util.List;

import static com.tongtech.proxy.core.center.ProxyData.*;
import static com.tongtech.proxy.core.utils.ScalableSlotsUtil.TOTAL_BLOCKS;

public class ProxyController {

    public static final SingleThreadSequentialExecutor CommandProcessService = new SingleThreadSequentialExecutor();

    // 日志
    private final static Log logger = ProxyConfig.getServerLog();

    public static final ProxyController INSTANCE = new ProxyController();

    // 节点执行完某些（有可能失败的命令，如：COMMANDTYPE_SYNCALL、COMMANDTYPE_GETDATA）命令以后的状态
    private volatile int CommandStatus = COMMANDRESULT_UNKNOW;

    // 第二命令结果通道，要求该通道的命令必须是能实时完成、不需要异步处理，且和低于通道命令不冲突的命令
    // 目前采用第二通道的命令有：MESSAGETYPE_MEMDBACLCHG、MESSAGETYPE_PROXYSENTINELCHG
    private volatile int CommandStatus2 = COMMANDRESULT_UNKNOW;

    private final Object CommandLocker = new Object();

    private ServiceMapping[] ServiceNodes = null;

    private final ArrayList<ServiceMapping> NORMAL_SERVICE_MAPPINGS = new ArrayList<>();

    private volatile ArrayList<ServiceMapping> ALL_SERVICE_MAPPINGS;

    /**
     * 解析从Center节点下发的系统配置冰凌
     *
     * @param data
     */
    public void parseCenterAuthor(List data) {
        try {
            int max_blocks = ((Long) data.get(1)).intValue();
            synchronized (CommandLocker) {
                if (TOTAL_BLOCKS != max_blocks) {
                    TOTAL_BLOCKS = max_blocks;
                    ServiceNodes = new ServiceMapping[max_blocks];
                }
            }
            logger.infoLog("ProxyController::parseCenterAuthor() Set Blocks is {}.", TOTAL_BLOCKS);
        } catch (Throwable t) {
            logger.warnLog("ProxyController::parseCenterAuthor() Parse {} error: {}", data, t);
        }
    }


    /**
     * 解析从Center节点下发的系统配置冰凌
     *
     * @param data
     */
    public void parseCenterCommand(List data) {
        synchronized (CommandLocker) {
            try {// todo something
                int pos = 1;// 0 是 MESSAGETYPE_PROXYCOMMAND 消息
                // 每个data包含且只包含1个命令
                if (pos < data.size()) {
                    // type < 10 是没有参数的命令
                    // type < 20 是只有1个参数的命令
                    // type < 30 是有2个参数的命令
                    // type < 40 是有3个参数的命令
                    int type = ((Long) data.get(pos++)).intValue();
                    switch (type) {
                        case COMMANDTYPE_INIT: // 没有参数
                            if (CommandStatus >= COMMANDRESULT_SHUTDOWNREQUEST) {
                                logger.warnLog("ProxyController::parseCenterCommand() The server received an unexpected command {} while shutting down.", type);
                                break;
                            }
                            // 恢复初始状态。为了保证执行顺序，采用异步队列方式设置状态
                            setCommandStatusAsync(COMMANDRESULT_INITIALIZATION);
                            logger.infoLog("ProxyController::parseCenterCommand() Run INIT command.");
                            break;
                        case COMMANDTYPE2_INIT: // 没有参数
                            if (CommandStatus >= COMMANDRESULT_SHUTDOWNREQUEST) {
                                logger.warnLog("ProxyController::parseCenterCommand() The server received an unexpected command {} while shutting down.", type);
                                break;
                            }
                            // 恢复初始状态。为了保证执行顺序，采用异步队列方式设置状态
                            setCommandStatus2(COMMANDRESULT2_INITIALIZATION);
                            logger.infoLog("ProxyController::parseCenterCommand() Run INIT2 command.");
                            break;
                        case COMMANDTYPE_SERVICENODECHANGE:
                            if (CommandStatus >= COMMANDRESULT_SHUTDOWNREQUEST) {
                                logger.warnLog("ProxyController::parseCenterCommand() The server received an unexpected command {} while shutting down.", type);
                                break;
                            }
                            serviceNodeChanged(data, pos);
                            logger.infoLog("ProxyController::parseCenterCommand() Get COMMANDTYPE_SERVICENODECHANGE command.");
                            break;
                        default:
                            logger.warnLog("ProxyController::parseCenterCommand() Unknown command: {}", type);
                    }
                }
            } catch (Throwable t) {
                if (logger.isInfo()) {
                    logger.warnLog(t, "ProxyController::parseCenterCommand() Parse {} error: {}", data, t.getMessage());
                } else {
                    logger.warnLog("ProxyController::parseCenterCommand() Parse {} error: {}", data, t);
                }
            }
        }
    }

    public int getCommandStatus2() {
        return CommandStatus2;
    }

    /**
     * 命令执行完状态变更
     *
     * @param status
     */
    public void setCommandStatus2(int status) {
        CommandStatus2 = status;
        ProxyData.notifyDaemon();
    }

    public int getCommandStatus() {
        return CommandStatus;
    }

    /**
     * 命令执行完状态变更
     *
     * @param status
     */
    private void setCommandStatus(int status) {
        CommandStatus = status;
        ProxyData.notifyDaemon();
    }

    public void setCommandStatusAsync(final int commandStatus) {
        CommandProcessService.execute(() -> {
            // 采用异步方式是为了避免有其它异步线程后做完而破坏状态更新的顺序
            setCommandStatus(commandStatus);
        });
    }

    private void serviceNodeChanged(final List data, final int init) {
        CommandProcessService.execute(() -> {
            try {
                int pos = init;
                while (data.size() > pos) {
                    List slice = (List) data.get(pos++);
                    String host = (String) slice.get(0);
                    int start = ((Long) slice.get(1)).intValue();
                    int stop = ((Long) slice.get(2)).intValue();
                    int level = ((Long) slice.get(3)).intValue();
                    int port = ((Long) slice.get(4)).intValue();
                    String passwd = ((String) slice.get(5));
                    int redis_port = ((Long) slice.get(6)).intValue();
                    String redis_passwd = ((String) slice.get(7));
                    boolean maintain = ((Long) slice.get(8)).intValue() > 0;

                    ServiceMapping manager;
                    if (!maintain) {
                        manager = new ServiceMappingNormal(host, level, port, passwd, redis_port, redis_passwd);
                        logger.infoLog("ProxyController::serviceNodeChanged() Data range [{} - {}] is point to {}:{}."
                                , start, stop, host, port);
                    } else {
                        manager = new ServiceMappingReadOnly(host, level, port, passwd, redis_port, redis_passwd);
                        logger.infoLog("ProxyController::serviceNodeChanged() Data range [{} - {}] is in maintenance mode."
                                , start, stop);
                    }

                    synchronized (CommandLocker) {
                        if (start > 0 && manager.equals(ServiceNodes[start - 1])) {
                            // 与前面的配置相同，延用前面的，合并对象防止碎片
                            manager = ServiceNodes[start - 1];
                            logger.infoLog("ProxyController::serviceNodeChanged() merge the {} manager at ServiceNodes[{}]."
                                    , manager.getType(), start - 1);
                        } else if (stop < ServiceNodes.length && manager.equals(ServiceNodes[stop])) {
                            // 与后面的配置想用，延用后面的，合并对象防止碎片
                            manager = ServiceNodes[stop];
                            logger.infoLog("ProxyController::serviceNodeChanged() merge the {} manager at ServiceNodes[{}]."
                                    , manager.getType(), stop);
                        }

                        for (int i = start; i < stop; ++i) {
                            ServiceNodes[i] = manager;
                            logger.debugLog("ProxyController::serviceNodeChanged() Set ServiceNodes[{}] = '{}'", i, manager);
                        }
                    }
                    logger.infoLog("ProxyController::serviceNodeChanged() Set ServiceNodes by {} ok.", slice);
                }
                synchronized (CommandLocker) {
                    ArrayList<ServiceMapping> all_map = new ArrayList<>();
                    NORMAL_SERVICE_MAPPINGS.clear();
                    ServiceMapping last = null;
                    ServiceMapping last_normal = null;
                    for (int i = 0; i < ServiceNodes.length; ++i) {
                        ServiceMapping mapping = ServiceNodes[i];
                        // 计算完整Mapping列表
                        if (mapping != null) {
                            if (mapping != last) {
                                if (last != null) {
                                    last.setStop(i);
                                }
                                last = mapping;
                                if (isInclude(all_map, mapping)) {
                                    // 发现列表中已经有该实例，此场景会出现在主备节点间分步迁移数据的过程中
                                    // 后续的 NORMAL_SERVICE_MAPPINGS 也不会重复处理该实例
                                    continue;
                                }
                                mapping.setStart(i);
                                all_map.add(mapping);
                            }
                        } else {
                            if (last != null) {
                                last.setStop(i);
                                last = null;
                            }
                        }

                        // 计算正常Mapping的节点
                        if (mapping instanceof ServiceMappingNormal) {
                            if (mapping != last_normal) {
                                last_normal = mapping;
                                NORMAL_SERVICE_MAPPINGS.add(mapping);
                            }
                        }
                    }
                    // 最后一个分片
                    if (last != null) {
                        last.setStop(ServiceNodes.length);
                    }

                    ALL_SERVICE_MAPPINGS = all_map;
                }
                // 已经在CommandProcessService的异步中了，直接同步设置状态
                setCommandStatus(COMMANDRESULT_PROXYSERVICENODECHANEGSET);
                logger.infoLog("ProxyController::serviceNodeChanged() Get COMMANDTYPE_SERVICENODECHANGE command.");
            } catch (Throwable t) {
                setCommandStatus(COMMANDRESULT_PROXYSERVICENODECHANGEFAILED);
                logger.warnLog(t, "ProxyController::serviceNodeChanged() Service node change update failed: {}", t.getMessage());
            }
        });
    }

    /**
     * 检查list中有没有同一个实例
     *
     * @param list
     * @param map
     * @return
     */
    private static boolean isInclude(List list, Object map) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) == map) {
                return true;
            }
        }
        return false;
    }

    public ServiceMapping getNodeManager(byte[] key) {
        return getNodeManager(ScalableSlotsUtil.hashKey(key));
    }

    private ServiceMapping getNodeManager(int pos) {
        synchronized (CommandLocker) {
            if (ServiceNodes != null && pos >= 0 && pos < ServiceNodes.length) {
                return ServiceNodes[pos];
            }
        }
        return null;
    }

    /**
     * 从正常的（不包含维护中的，因为维护中的mapping虽然和正常的是不同的对象，但可能指向同一个slice）列表中指定一个Mapping
     *
     * @param pos
     * @return
     */
    public ServiceMapping getNormalManagerDirect(int pos) {
        synchronized (CommandLocker) {
            if (pos >= 0 && pos < NORMAL_SERVICE_MAPPINGS.size()) {
                return NORMAL_SERVICE_MAPPINGS.get(pos);
            }
        }
        return null;
    }

    /**
     * 从全部列表中选择一个，目前只用于Stream命令，选择第一分
     *
     * @return
     */
    public ServiceMapping getFirstManager() {
        ArrayList<ServiceMapping> mappings = getAllServiceMappings();
        if (mappings != null && mappings.size() > 0) {
            return mappings.get(0);
        }
        return null;
    }

    public ArrayList<ServiceMapping> getAllServiceMappings() {
        return this.ALL_SERVICE_MAPPINGS;
    }

    public void updateSliceData() {
        try {
            ArrayList<ServiceMapping> mappings = NORMAL_SERVICE_MAPPINGS;
            if (mappings != null && mappings.size() > 0) {
                for (ServiceMapping mapping : mappings) {
                    mapping.pullSliceDataFromMaster();
                }
            }
        } catch (Throwable t) {
            if (logger.isInfo()) {
                logger.warnLog(t, "ProxyController::updateSliceData() Update failed: {}", t.getMessage());
            } else {
                logger.warnLog("ProxyController::updateSliceData() Update failed: {}", t);
            }
        }
    }
}
