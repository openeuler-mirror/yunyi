package com.tongtech.proxy.core.utils;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import com.tongtech.proxy.core.center.CenterConfig;
import com.tongtech.proxy.core.rescue.FindSuitableServer;
import com.tongtech.proxy.core.rescue.ProxyDataRescue;
import com.tongtech.proxy.core.utils.config.PropertyPlaceholderHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static com.tongtech.proxy.core.StaticContent.SCHEDULED_THREAD_POOL_EXECUTOR;
import static com.tongtech.proxy.core.center.ProxyData.MESSAGETYPE_PROXYSENTINELCHG;

public class ProxyDynConfig {
    private static final Log Logger = ProxyConfig.getServerLog();

    private static long Timestamp_Centers = 0;
    private static final CenterConfig CENTER_CONFIG = new CenterConfig();

    private static long Timestamp_Services = 0;
    private static final Vector<OriginalSocketAddress> SyncNodes = new Vector<>();

    private static long Timestamp_Load = 0;

    private final static File ConfigFile = new File(ProxyConfig.getServerHome() + "/etc/dynamic.xml");

    private final static Object CenterDataLocker = new Object();

    private static Vector CenterData = null;

    private static volatile boolean IsCenterData = false;

    private static volatile boolean IsSynchronizeEnd = false;

    // 保证Sync配置不会被改，当Center端的配置该服务为Sentinel状态时，CenterSentinel有值，但CenterCluster为空
    // CenterSentinel有值时dynamic.xml中的Synchroniz配置不能被手工修改
    private static volatile Vector<OriginalSocketAddress> CenterSentinel = null;

    // 最新的从Center取到的数据，20211130增加
    // 增加此项是为了判断每次从Center获得的数据是否有更新，没有更新则不重写dynamic.xml文件，
    // 之前的程序里，此文件会一直被刷新
    private static Vector LastDataFromCenter = null;

    private static final Thread Instance = new Thread(() -> {
        try {
            ProxyConfig.reload();
        } catch (Throwable e) {
            Logger.warnLog("DynamicConfig::run() Load proxy.xml failed: {}", e.toString());
        }
        try {
            ProxyDynConfig.reload();
        } catch (Throwable e) {
            Logger.warnLog("DynamicConfig::run() Load dynamic.xml failed: {}", e.toString());
        }
    });

    public static void init() {
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(Instance, 0, 1100, TimeUnit.MILLISECONDS);
    }

    private synchronized static void reload() {
        //File config  = new File(Configuration.getServerHome() + "/etc/dynamic.xml");

        if (ConfigFile.exists()) {
            if (Logger.isDebug()) {
                Logger.debugLog("DynamicConfig::reload() try to load file '" + ConfigFile.getAbsolutePath() + "'.");
            }
            if (ConfigFile.lastModified() != Timestamp_Load) {
                if (Timestamp_Load != 0) {
                    Logger.warnLog("DynamicConfig::reload() file '" + ConfigFile.getAbsolutePath() + "' is modified, reload it.");
                }

                Element root = null;
                try {
                    SAXReader reader = new SAXReader();
                    InputStream inputStream = new FileInputStream(ConfigFile);
                    Document doc = reader.read(inputStream);
                    inputStream.close();

                    root = doc.getRootElement();
                } catch (Throwable t) {
                    Logger.errorLog("DynamicConfig::reload() load config-file '"
                            + ConfigFile.getAbsolutePath() + "' error: " + t.getMessage());
                    return;
                }

                Timestamp_Load = ConfigFile.lastModified();

                try {
                    Element element = null;
                    ArrayList<OriginalSocketAddress> addrs = null;

                    Properties volumePro = new Properties();
                    GetConfigFromVolume.fillPropertiesFromVolume("dynamic", volumePro);

                    /**
                     * <Center></Center>
                     */
                    addrs = new ArrayList<>();
                    element = root.element("Center");
                    if (element != null) {
//                        addrs = new ArrayList<>();
                        List<Element> list = element.elements();
                        for (Element el : list) {
                            try {
                                if ("Password".equalsIgnoreCase(el.getName())) {
                                    CENTER_CONFIG.setPassword(el.getTextTrim());
                                } else {
                                    String host = PropertyPlaceholderHelper.INSTANCE.replacePlaceholders(el.elementTextTrim("Host"), volumePro);
                                    int port = Integer.parseInt(el.elementTextTrim("Port"));
                                    OriginalSocketAddress s = new OriginalSocketAddress(host, port);
                                    if (!addrs.contains(s)) {
                                        addrs.add(s);
                                    }
                                    if (Logger.isInfo()) {
                                        Logger.infoLog("DynamicConfig::reload() Load center ("
                                                + s.getHostName() + ", " + s.getPort() + ") ok.");
                                    }
                                }
                            } catch (Exception e) {
                                if (Logger.isDebug()) {
                                    Logger.debugLog("DynamicConfig::reload() Load center '"
                                            + (el != null ? el.asXML() : "null") + "' failed.");
                                }
                            }
                        }
                    }
                    if (!isEqual(CENTER_CONFIG.getAddresses(), addrs)) {
                        synchronized (CENTER_CONFIG) {
                            Timestamp_Centers = Timestamp_Load;
                            CENTER_CONFIG.setAddresses(addrs);
                        }
                        Logger.warnLog("DynamicConfig::reload() Centers are reloaded.");
                    }

                    /**
                     * <Synchronize></Synchronize>
                     */
                    //                    if (true) {
                    // 是企业版，或是专业版且没有初始化过
                    element = root.element("Synchronize");
                    addrs = new ArrayList<>();
                    if (element != null) {
//                        addrs = new ArrayList<>();
                        List<Element> list = element.elements();
                        for (Element el : list) {
                            try {
                                String host = el.elementTextTrim("Host");
                                int port = Integer.parseInt(el.elementTextTrim("Port"));
                                String host_alias = el.elementTextTrim("HostAlias");
                                if (host_alias != null && host_alias.length() == 0) {
                                    host_alias = null;
                                }
                                int port_alias = 0;
                                try {
                                    port_alias = Integer.parseInt(el.elementTextTrim("RedisPortAlias"));
                                } catch (Throwable t) {
                                }
                                OriginalSocketAddress s = new OriginalSocketAddress(host, host_alias, port, port_alias);
                                if (!addrs.contains(s)) {
                                    addrs.add(s);
                                    if (Logger.isInfo()) {
                                        Logger.infoLog("DynamicConfig::reload() Load synchronize ("
                                                + s.getHostName() + ", " + s.getPort() + ") ok.");
                                    }
                                } else {
                                    if (Logger.isInfo()) {
                                        Logger.infoLog("DynamicConfig::reload() Duplicate synchronize ("
                                                + s.getHostName() + ", " + s.getPort() + ") found.");
                                    }
                                }
                            } catch (Exception e) {
                                if (Logger.isDebug()) {
                                    Logger.debugLog("DynamicConfig::reload() Load synchronize '"
                                            + (el != null ? el.asXML() : "null") + "' failed.");
                                }
                            }
                        }
                    }
                    // 判断是否私自改了配置文件的Synchronize配置
                    if (CenterSentinel != null) {
                        // CenterSentinel不为空说明Center下发了配置
                        if (!(CenterSentinel.size() == 0 && addrs.size() == 0)
                                && !isEqual(CenterSentinel, addrs)) {
                            // CenterCluster.size() == 0 说明Center下发了配置，但不是cluster
                            // clusters == null说明配置文件中没有Clusters配置
                            // 配置文件被私自修改，与Center节点的Cluster配置不一样了
                            updateConfigToFile(CenterSentinel.toArray(new OriginalSocketAddress[0]), getCenterPassword(), getCenterAddresses());
                            Logger.warnLog("DynamicConfig::reload() Cluster configuration is not allowed to be modified locally, recovery!");
                            return;
                        }
                    }
                    if (!isEqual(SyncNodes, addrs)) {
                        synchronized (SyncNodes) {
                            Timestamp_Services = Timestamp_Load;
                            SyncNodes.clear();
                            if (addrs != null) {
                                SyncNodes.addAll(addrs);
                            }
                        }
                        Logger.warnLog("DynamicConfig::reload() List of sync are reloaded.");
                    }

                    if (IsCenterData && !IsSynchronizeEnd) {
                        // 程序执行到此处时，已经成功读取了Center发来的同步列表，但还没有同步过数据
                        IsSynchronizeEnd = true; // 程序启动后只会在收到Center授权数据后执行一次
                        if (addrs.size() > 1) {
                            // 以下操作耗时比较长，可能最多需要10秒
                            for (int i = 0; i < 3; ++i) {
                                OriginalSocketAddress server = FindSuitableServer.getServer(addrs);
                                if (server != null) {
                                    if (ProxyDataRescue.getDataFromOther(server)) {
                                        Logger.infoLog("DynamicConfig::reload() Get data from {} ok.", server);
                                        break;
                                    }
                                    Logger.infoLog("DynamicConfig::reload() Get data from {} failed {} times, try again.", server, i);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Throwable t) {
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception others) {
                    if (Logger.isInfo()) {
                        Logger.infoLog(others, "DynamicConfig::reload() File parse error: {}", others);
                    } else {
                        Logger.warnLog("DynamicConfig::reload() File parse error: {}", others);
                    }
                }
            }
        } else { // if (ConfigFile.exists()) {
            Timestamp_Load = 0;
            synchronized (SyncNodes) {
                Timestamp_Services = Timestamp_Load;
                SyncNodes.clear();
            }
            Logger.debugLog("DynamicConfig::reload() file '" + ConfigFile.getAbsolutePath() + "' is not exists.");
        }

        // 来自CenterServer的对cluster的更新指令
        Vector cluster_data = null;
        synchronized (CenterDataLocker) {
            if (CenterData != null) {
                cluster_data = CenterData;
                CenterData = null;
                if (Logger.isDebug()) {
                    Logger.debugLog("DynamicConfig::reload() process cluster data");
                }
            }
        }
        if (cluster_data != null) {
            int offset = 0;
            int type = ((Long) cluster_data.get(offset++)).intValue();
            if (type == MESSAGETYPE_PROXYSENTINELCHG) {
                CenterSentinel = new Vector<>();
                int nodes = ((Long) cluster_data.get(offset++)).intValue();
                if (Logger.isDebug()) {
                    Logger.debugLog("DynamicConfig::reload() sync-nodes is {}", nodes);
                }
                if (nodes > 0) {
//                    int secureLevel = ((Long) cluster_data.get(offset++)).intValue();
//                    String password = (String) cluster_data.get(offset++);
                    try {
                        // 如果是整数，说明是新版本的Center发的包，包含安全级别和密码
                        ((Long) cluster_data.get(offset)).intValue();
                        // 同样的包也会发给服务节点
                        // 新加的这2项是为了当服务节点的安全级别和proxy节点不同时能正确连接进proxy
                        // proxy不需要知道密码，直接跳过
                        offset += 2;
                    } catch (Throwable t) {
                    }
                    for (int i = 0; i < nodes; ++i) {
                        String rawAddr = (String) cluster_data.get(offset++);
                        OriginalSocketAddress address = OriginalSocketAddress.getSocketAddress(rawAddr);
                        if (address != null) {
                            CenterSentinel.add(address);
                        }
                    }
                }

                updateConfigToFile(CenterSentinel.toArray(new OriginalSocketAddress[0]), getCenterPassword(), getCenterAddresses());

                IsCenterData = true;

                Logger.infoLog("DynamicConfig::reload() The Synchronize configuration was modified by the center-server");
            }
        }
    }

    /**
     * 中心统计服务器配置更新时间
     *
     * @return
     */
    public static long getCenterTimestamp() {
        synchronized (CENTER_CONFIG) {
            return Timestamp_Centers;
        }
    }

    /**
     * 中心管理服务器地址
     *
     * @return
     */
    public static OriginalSocketAddress[] getCenterAddresses() {
        synchronized (CENTER_CONFIG) {
            return CENTER_CONFIG.getAddresses().toArray(new OriginalSocketAddress[0]);
        }
    }

    /**
     * 中心管理服务器登陆密码
     *
     * @return
     */
    public static String getCenterPassword() {
        synchronized (CENTER_CONFIG) {
            return CENTER_CONFIG.getPassword();
        }
    }

    /**
     * 各服务节点配置更新时间
     *
     * @return
     */
    public static long getSyncTimestamp() {
        synchronized (SyncNodes) {
            return Timestamp_Services;
        }
    }

    /**
     * 各服务节点配置
     *
     * @return
     */
    public static OriginalSocketAddress[] getSyncAddresses() {
        synchronized (SyncNodes) {
            return SyncNodes.toArray(new OriginalSocketAddress[0]);
        }
    }


    private static boolean isEqual(List list1, List list2) {
        if (list1 == null && list2 == null) {
            return true;
        }

        if (list1 == null || list2 == null
                || list1.size() != list2.size()) {
            return false;
        }

        // 精确判断内容和顺序完全一致
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将动态配置写到文件file中
     **/
    private static void updateConfigToFile(
            OriginalSocketAddress[] syncs,
            String center_password, OriginalSocketAddress[] centers) {

        try {
            // 暂停15毫秒是为了保证文件的更新时间会有变化
            // 如果程序运行很快，并在1毫秒内写回文件。可能造成因为文件时间戳没变而使程序无法更新到配置
            Thread.sleep(15);
        } catch (Exception e) {
        }

        try {
            Document document = DocumentHelper.createDocument();
            Element root = document.addElement("Server");
            Element node = null, sub_node = null;

            if (centers != null && centers.length > 0) {
                node = root.addElement("Center");
                node.addElement("Password").setText(center_password);
                for (OriginalSocketAddress addr : centers) {
                    sub_node = node.addElement("EndPoint");
                    sub_node.addElement("Host").setText(addr.getOriginalAddress());
                    sub_node.addElement("Port").setText(Integer.toString(addr.getPort()));
                }
            }

            if (syncs != null && syncs.length > 0) {
                node = root.addElement("Synchronize");
                for (OriginalSocketAddress addr : syncs) {
                    sub_node = node.addElement("EndPoint");
                    sub_node.addElement("Host").setText(addr.getOriginalAddress());
                    if (addr.getRemoteHastAlias() != null) {
                        sub_node.addElement("HostAlias").setText(addr.getRemoteHastAlias());
                    }
                    sub_node.addElement("Port").setText(Integer.toString(addr.getPort()));
                    if (addr.getRedisPortAlias() > 0) {
                        sub_node.addElement("RedisPortAlias").setText(Integer.toString(addr.getRedisPortAlias()));
                    }
                }
            }

//            if (clusters != null && clusters.length > 0) {
//                node = root.addElement("Clusters");
//                for (ClusterGroup clusterGroup : clusters) {
//                    sub_node = node.addElement("Cluster");
////                    int secure = clusterGroup.getSecurelevel();
////                    if (secure != Configuration.getSecureLevel()) {
////                        sub_node.addElement("Secure").setText(Integer.toString(secure));
////                    }
////                    String pwd = clusterGroup.getRemotePassword();
////                    if (pwd != null && !pwd.equals(Configuration.getAuthenString())) {
////                        pwd = SM4Util.encrypt(pwd);
////                        sub_node.addElement("Password").setText(pwd);
////                    }
//                    sub_node.addElement("EndPoints").setText(clusterGroup.toStringEndPoints());
//                    sub_node.addElement("Slots").setText(clusterGroup.toStringSlots());
//                }
//            }

            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(document.getXMLEncoding());
//            FileWriter file = null;
            try (FileWriter file = new FileWriter(ConfigFile)) {
//                file = ;
                XMLWriter writer = new XMLWriter(file, format);
                writer.write(document);
                writer.flush();
                writer.close();
//            } finally {
//                if (file != null) {
//                    file.close();
//                }
            }
        } catch (Exception e) {
            Logger.warnLog("ProxyDynConfig::updateConfigToFile() Write config to dynamic.xml failed: {}", e);
        }
    }

    /**
     * 从center端发来的同步或集群配置，数据类型根据data.get(0)的值区分。
     *
     * @param data
     */
    public static void setClusterDataFromCenter(Vector data) {
        if (((Long) data.get(1)).intValue() == 0) {
            // data.get(1)存的是数据项的个数，==0说明Center未配置当前节点的服务。
            // 收到此包说明在Center端取消了对当前服务的配置，Center端不再强制要求配置统一，该服务的各节点的配置由节点自己负责
            synchronized (CenterDataLocker) {
                LastDataFromCenter = null;
                CenterData = null;
                CenterSentinel = null;
            }
            Logger.infoLog("DynamicConfig::setClusterDataFromCenter() The service '{}' of the current node is not configured in 'cluster.properties' of the center"
                    , ProxyConfig.getServiceName());
        } else {
            synchronized (CenterDataLocker) {
                if (LastDataFromCenter == null || !LastDataFromCenter.equals(data)) {
                    CenterData = data;
                    LastDataFromCenter = data;
                    Logger.infoLog("DynamicConfig::setClusterDataFromCenter() Get dynamic data {} from center"
                            , data);
                } else {
                    Logger.debugLog("DynamicConfig::setClusterDataFromCenter() Same dynamic data {} from center"
                            , data);
                }
            }
        }
    }
}
