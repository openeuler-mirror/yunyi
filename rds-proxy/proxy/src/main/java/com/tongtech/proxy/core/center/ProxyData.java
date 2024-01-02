package com.tongtech.proxy.core.center;

import com.tongtech.proxy.Version;
import com.tongtech.proxy.core.acl.AccessController;
import com.tongtech.proxy.core.center.packet.*;
import com.tongtech.proxy.core.utils.*;
import com.tongtech.proxy.core.server.ProxyController;

import java.util.ArrayList;
import java.util.Vector;

public class ProxyData {
    // 定时发送数据的间隔时间，Center依靠此心跳判断Node还活着，必须小于1秒
    private static final int THREAD_SLEEP_INTERVAL = 900;
    private static final int DATA_REPORT_INTERVAL = 4300;

    // 针对2.2.C.1和后续版本，返回的命令结果代码不一样
    // 如果编译proxy用于2.2.C.1的Center，需将此值设置为true，否则是false
    private static final boolean Version1Compatible;
    static {
        boolean version1 = "2.2.C.1".equals(Version.Version);
        if (!version1) {
            version1 = "2.2.1.3".compareTo(Version.Version) >= 0;
        }
        Version1Compatible = version1;
    }

    // used memdb messages
    public static final int MESSAGETYPE_MEMDBACLCHG = 5;

    // proxy messages
    public static final int MESSAGETYPE_PROXYAUTHEN = 48;
    public static final int MESSAGETYPE_PROXYREQ = 49;
    public static final int MESSAGETYPE_PROXYRSP = 50;
    public static final int MESSAGETYPE_PROXYAUTHOK = 51;
    public static final int MESSAGETYPE_PROXYAUTHFAILED = 52;
    public static final int MESSAGETYPE_PROXYCOMMAND = 53;
    public static final int MESSAGETYPE_PROXYSENTINELCHG = 54;
    public static final int MESSAGETYPE_PROXYES = 63;

    public static final int OBJECTTYPE_RESPONSE = 1;
    public static final int OBJECTTYPE_MINIRUNTIME = 2;
    public static final int OBJECTTYPE_NODEINFO = 3;
    public static final int OBJECTTYPE_TOTALUSED = 5;
    public static final int OBJECTTYPE_PROCESSSPEED = 6;
    public static final int OBJECTTYPE_LICENSEREQUEST = 7;
    public static final int OBJECTTYPE_LICENSERESPONSE = 8;
    public static final int OBJECTTYPE_SYNCSPEED = 9;
    public static final int OBJECTTYPE_RUNTIME = 10;
    public static final int OBJECTTYPE_INSTANCE = 11;

    // 中心节点下发命令
    public static final int COMMANDTYPE_INIT = 0;
    public static final int COMMANDTYPE2_INIT = 32;
    public static final int COMMANDTYPE_SERVICENODECHANGE = 101;

    // 服务节点返回的命令执行状态
    public static final int COMMANDRESULT_UNEXIST = -2;
    public static final int COMMANDRESULT_UNKNOW = -1;
    public static final int COMMANDRESULT_INITIALIZATION = 0;
    public static final int COMMANDRESULT_SYNCALLING = 1;
    public static final int COMMANDRESULT_SYNCALLFAILED = 2;
    public static final int COMMANDRESULT_SYNCALLOK = 3;
    public static final int COMMANDRESULT_CLEANING = 4;
    public static final int COMMANDRESULT_CLEANOK = 5;
    public static final int COMMANDRESULT_GETDATAING = 6;
    public static final int COMMANDRESULT_GETDATAFAILED = 7;
    public static final int COMMANDRESULT_GETDATAOK = 8;
    public static final int COMMANDRESULT_RANGESET = 21;
    public static final int COMMANDRESULT_SENTINELCHGSET = 22;
    public static final int COMMANDRESULT_REMOVERANGEDATAFAILED = 23;
    public static final int COMMANDRESULT_REMOVERANGEDATASET = 24;
    public static final int COMMANDRESULT_MAINTENANCESTARTSET = 25;
    public static final int COMMANDRESULT_MAINTENANCEENDSET = 26;
    public static final int COMMANDRESULT_CLUSTERCHGSET = 27;

    // proxy节点特有的状态
    public static final int COMMANDRESULT_PROXYSERVICENODECHANGEFAILED = 100;
    public static final int COMMANDRESULT_PROXYSERVICENODECHANEGSET = 101;

    public static final int COMMANDRESULT_SHUTDOWNREQUEST = 1000;
    public static final int COMMANDRESULT_SHUTDOWNPERMIT = 1001;

    // 第二通道命令返回结果
    public static final int COMMANDRESULT2_INITIALIZATION = 1;
    public static final int COMMANDRESULT_OLD_ACLCHGSET = 28;// TongRDS 2.2.1.3版本返回此状态
    public static final int COMMANDRESULT2_ACLCHGSET = 2;
    public static final int COMMANDRESULT_OLD_PROXYSENTINELCHGSET = 102;// TongRDS 2.2.1.3版本返回此状态
    public static final int COMMANDRESULT2_PROXYSENTINELCHGSET = 3;


    private final static ProcessSpeed ProcessSpeed = new ProcessSpeed();

    private final static NodeInfo Information = new NodeInfo();

    private final static RunTime ServiceRuntime = new RunTime();

    private final static MiniRuntime ServiceMiniRuntime = new MiniRuntime();

    private final static InstanceInformation InstanceInfo = new InstanceInformation();

    private final static Log Logger = ProxyConfig.getServerLog();

    private static volatile Thread DaemonThread = null;

    static void parseObject(Vector data) {
        if (data == null || data.size() == 0) {
            // 有可能是心跳包的回包
            Logger.warnLog("ProxyData::parseObject() Null message received.");
            return;
        }

        int offset = 0;
        int type = ((Long) data.get(offset++)).intValue();
        //int port = ((Long) data.get(offset++)).intValue();

        if (type == MESSAGETYPE_PROXYRSP) {
            // do nothing
        } else if (type == MESSAGETYPE_PROXYSENTINELCHG) {
            ProxyDynConfig.setClusterDataFromCenter(data);
            if (Version1Compatible) {
                ProxyController.INSTANCE.setCommandStatusAsync(COMMANDRESULT_OLD_PROXYSENTINELCHGSET);
            } else {
                ProxyController.INSTANCE.setCommandStatus2(COMMANDRESULT2_PROXYSENTINELCHGSET);
            }
        } else if (type == MESSAGETYPE_MEMDBACLCHG) {
            AccessController.setAclDataFromCenter(data);
            if (Version1Compatible) {
                ProxyController.INSTANCE.setCommandStatusAsync(COMMANDRESULT_OLD_ACLCHGSET);
            } else {
                ProxyController.INSTANCE.setCommandStatus2(COMMANDRESULT2_ACLCHGSET);
            }
        } else if (type == MESSAGETYPE_PROXYAUTHOK) {
            ProxyController.INSTANCE.parseCenterAuthor(data);
        } else if (type == MESSAGETYPE_PROXYAUTHFAILED) {
            Logger.warnLog("ProxyData::parseObject() Authen failed to login Center.");
        } else if (type == MESSAGETYPE_PROXYCOMMAND) {
            ProxyController.INSTANCE.parseCenterCommand(data);
        } else {
            Logger.warnLog("ProxyData::parseObject() Unknown message {}({}) received from Center.", type, data);
        }

        Logger.infoLog("ProxyData::parseObject() Receive message {} from center.", data);
    }

    /**
     * 该方法只在reportingData()中被调用
     *
     * @return
     */
    private static ArrayList getData(boolean total) {
        ArrayList data = new ArrayList(64);

        data.add(MESSAGETYPE_PROXYREQ);

        if (total) {
            Information.serialize(data);
            ProcessSpeed.serialize(data);
            ServiceRuntime.serialize(data);
            ServiceMiniRuntime.serialize(data);
            InstanceInfo.serialize(data);
        } else {
            ServiceMiniRuntime.serialize(data);
        }

        return data;
    }

    public static void setProcessSpeed(int times, long duration) {
        ProcessSpeed.setData(times, duration);
    }

    /**
     * 每秒处理量
     */
    public static float getProcessSpeedCurrent() {
        return ProcessSpeed.getCurrent();
    }

    /**
     * 最近1分钟内平均每秒处理量
     */
    public static float getProcessSpeedMinute() {
        return ProcessSpeed.getTimes_60();
    }

    /**
     * 唤醒数据上报线程
     */
    public static void notifyDaemon() {
        Thread t = DaemonThread;
        if (t != null) {
            synchronized (t) {
                t.notify();
            }
        }
    }

    /**
     * 启动到中央统计服务器的连接维护线程，
     * 在LicenseChecker的构造方法中调用
     * 只能被调用1次
     */
    public synchronized static void start() {
        if (DaemonThread == null) {
            DaemonThread = new Thread() {
                private volatile OriginalSocketAddress[] Addresses;

                // 到各Center的连接类
                private volatile ArrayList<CenterConnector> CenterConns = new ArrayList<>();

                // 更新到各Center的连接类的时间戳，时间戳有变化则更新连接类
                private volatile long CenterConnTimestamp;

                private volatile long Timestamp = System.currentTimeMillis() + DATA_REPORT_INTERVAL;

                @Override
                public void run() {
                    while (true) {
                        try {
                            long cur_time = ProxyDynConfig.getCenterTimestamp();
                            // 以下判断是否需要更新配置信息
                            if (cur_time != CenterConnTimestamp) {
                                CenterConnTimestamp = cur_time;
                                reloadConnector(ProxyDynConfig.getCenterAddresses());
                            }

                            ArrayList data;
                            if (System.currentTimeMillis() > Timestamp) {
                                // 更新数据，计算每秒流量
                                ProcessCounter.calculateInstantaneous();
                                data = getData(true);
                                Timestamp = System.currentTimeMillis() + DATA_REPORT_INTERVAL;
                            } else {
                                data = getData(false);
                            }
                            reportingData(data);

                            synchronized (DaemonThread) {
                                try {
                                    DaemonThread.wait(THREAD_SLEEP_INTERVAL);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (Throwable t) {
                            Logger.warnLog("ProxyData::run() daemon run error: {}", t);
                        }
                    }
                }

                private void reportingData(ArrayList data) {
                    // 以下维护连接状态
                    ArrayList<CenterConnector> conns = CenterConns;
                    try {
                        if (conns.size() > 0) {
                            for (CenterConnector conn : conns) {
                                if (conn.sendMessage(data)) {
                                    if (Logger.isDebug()) {
                                        Logger.debugLog("ProxyData::reportingData() report data to server {}:{} succeeded."
                                                , conn.getCenterAddress(), conn.getCenterPort());
                                    }
                                    return;
                                }
                            }
                            data = getData(true);
                            for (CenterConnector conn : conns) {
                                if (conn.createAndSend(data)) {
                                    Logger.infoLog("ProxyData::reportingData() create and report data to server {}:{} succeeded."
                                            , conn.getCenterAddress(), conn.getCenterPort());
                                    return;
                                }
                            }
                        }
                    } catch (Throwable t) {
                        Logger.warnLog("ProxyData::reportingData() error when exchange data from Center: {}", t);
                    }
                }

                private void reloadConnector(OriginalSocketAddress[] addrs) {
                    ArrayList<CenterConnector> newConns = new ArrayList<>();
                    ArrayList<CenterConnector> oldConns = CenterConns;

                    // 创建新的配置数据对象
                    for (int i = 0; addrs != null && i < addrs.length; i++) {
                        OriginalSocketAddress addr = addrs[i];
                        try {
                            CenterConnector conn = new CenterConnector(addr);
                            newConns.add(conn);
                        } catch (Throwable e) {
                        }
                    }
                    // 更新全局对象
                    CenterConns = newConns;
                    // 保存更新后的配置
                    Addresses = addrs;

                    // 强制全部关闭旧的连接
                    try {
                        for (int i = 0; i < oldConns.size(); i++) {
                            try {
                                oldConns.get(i).close();
                            } catch (Throwable e) {
                            }
                        }
                    } catch (Throwable t) {
                    }
                }
            };
            DaemonThread.setDaemon(true);
            DaemonThread.start();
        }
    }
}
