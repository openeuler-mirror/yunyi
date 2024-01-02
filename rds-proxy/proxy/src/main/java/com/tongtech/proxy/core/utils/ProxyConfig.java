package com.tongtech.proxy.core.utils;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import com.tongtech.proxy.core.utils.config.PropertyPlaceholderHelper;
import com.tongtech.proxy.fw.AddressRestrictions;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.PrintStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class ProxyConfig {

    private final static int DEFAULT_PORT = 6200;
    private final static int DEFAULT_MAX_RETURNED = 10000;
    private final static int DEFAULT_SLOW_OPERATION_THRESHOLD = 150;
    private final static int DEFAULT_SLOW_OPERATION_MAX_LEN = 128;

    private final static File ConfigFile = new File(getServerHome() + "/etc/proxy.xml");

    // 配置信息
    private final static Element Root;

    // 动态更新的配置项
    private final static Properties DynamicElements = new Properties();

    private final static Charset AppCharset;

    // 系统日志类
    private final static Log Logger;

    // 当前jvm的唯一标示
    private final static String SERVER_IDENTIFY;

    // 服务器开放端口的安全级别
    // 0: telnet; 1: SSL; 2: password; 3: SSL + password. 缺省为1
    private final static int SocketSecureLevel;

    private volatile static String AuthenString;

    // 主监听端口
    private final static int ListeningPort;

    private final static OriginalSocketAddress LocalAddress;

    // redis协议监听端口
    private final static int RedisPort;

    // redis协议密码，对应配置的"Server.Listen.RedisPwd"
    // 如果配置文件中无此项，则使用AuthenString的密码
    // 是否需要密码认证，由SocketSecureLevel的值决定
    private volatile static String RedisPwd;

    private final static String ServiceName;

    // 是否重定向标准输出到文件
    private final static boolean Redirect;

    private volatile static int MaxConnections;

    // private final static ArrayList<ArrayList> Clusters;

    private final static int SOCKET_PROCESS_THREADS;

    private final static int SOCKET_IO_THREADS;

    private final static int SOCKET_BACKLOG;

    private volatile static boolean ANTI_REDIS;

    private volatile static long ConfigFileTimestamp;

    private volatile static long ConfigCheckTimestamp;

    private volatile static boolean PRETENT_AS_A_CLUSTER = false;

    private volatile static boolean MasterDeterminateByConfig;

    // 是否过滤掉危险命令
//    private volatile static boolean DangerousCommandFilter;

    // 连接的空闲时间
    private final static int ChannelIdleTimeout;

    private volatile static String Instance = "proxy";

    private volatile static String GroupName;

    private volatile static int SlowOperationThreshold;

    private final static int SlowOperationMaxLen;

    //  Redis协议端口对外声明的访问地址
    private volatile static String AnnounceHost;

    //  Redis协议端口对外声明的访问端口，缺省为RedisPort
    private volatile static int AnnounceRedisPort;

    // 初始化配置类，读配置文件
    static {

        // 屏蔽stderr
        System.setErr(new PrintStream(new NullOutputStream()));


//        Element root = null;
//        try {
//            Timestamp = ConfigFile.lastModified();
//
//            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//            dbf.setExpandEntityReferences(false);
//
//            SAXReader reader = new SAXReader();
//
//            Document doc = reader.read(ConfigFile);
//            root = doc.getRootElement();
//            System.out.println("load config-file '"
//                    + ConfigFile.getAbsolutePath() + "' ok.");
//        } catch (Exception others) {
//            System.out.println("\nProxyConfig::() load config-file error: " + others.getMessage());
//            System.exit(2);
//        }

        Root = getRoot(true);

        ConfigFileTimestamp = ConfigFile.lastModified();

        if ("true".equalsIgnoreCase(getProperty("Server.Common.RedirectStdOut"))) {
            Redirect = true;
        } else {
            Redirect = false;
        }

        // 初始化日志,之后的代码才能正常写日志
        int backupdate = 0;
        try {
            backupdate = Integer.parseInt(getProperty("Server.Log.BackDates"));
        } catch (Throwable t) {
        }
        String logfile = getServerHome() + "/logs/"
                + getProperty("Server.Log.File", "server.log");
        String loglevel = getProperty("Server.Log.Level", "warn");
        Logger = new Log(new File(logfile), backupdate);
        Logger.setLogLevel(loglevel);

//        Charset temp_charset;
//        try {
//            temp_charset = Charset.forName(getProperty("Server.Common.Charset"));
//        } catch (Exception e) {
//            temp_charset = StandardCharsets.UTF_8;
//        }
//        AppCharset = temp_charset;
        AppCharset = StandardCharsets.UTF_8;

        long iden = System.nanoTime();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface inter = interfaces.nextElement();
                byte[] hardware = inter.getHardwareAddress();
                if (hardware == null) {
                    continue;
                }
                for (int i = 0; i < hardware.length; i++) {
                    iden = (iden << 8) | (hardware[i] & 0xff);
                }
                if (iden != 0) {
                    break;
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 设置监听端口
        int app_port = 0;
        try {
            app_port = Integer.parseInt(getProperty("Server.Listen.Port"));
        } catch (Exception e) {
            app_port = DEFAULT_PORT;
        }
        ListeningPort = app_port;
        Logger.warnLog("ProxyConfig::() Main protocol  listening port is '{}'", ListeningPort);

        app_port = 0;
        try {
            app_port = Integer.parseInt(getProperty("Server.Listen.RedisPort"));
        } catch (Exception e) {
        }
        if (app_port < 0) {
            app_port = 0;
        }
        RedisPort = app_port;
        Logger.warnLog("ProxyConfig::() Redis protocol listening port is '{}'", RedisPort);

        // 初始化SERVER_IDENTIFY
        SERVER_IDENTIFY = Long.toHexString(iden) + "-" + ListeningPort;

        String local_addr = getProperty("Server.Common.AccessibleAddress");
        if (local_addr != null && local_addr.length() > 0) {
            LocalAddress = new OriginalSocketAddress(local_addr, ListeningPort);
        } else {
            LocalAddress = new OriginalSocketAddress(ListeningPort);
        }

        // 初始化SocketSecureLevel参数
        int secure_level;
        try {
            secure_level = Integer.parseInt(getProperty("Server.Listen.Secure"));
            Logger.warnLog("ProxyConfig::() Configuration 'Server.Listen.Secure' is "
                    + secure_level);
        } catch (Exception e) {
            secure_level = 1;
            Logger.warnLog("ProxyConfig::() Read configuration 'Server.Listen.Secure' failed, use default "
                    + secure_level + " (ssl-socket)");
        }
        SocketSecureLevel = secure_level;

        String passwd = getProperty("Server.Listen.Password");

        // 没配置password程序会直接退出.理由是安全配置不正确
        if ((secure_level & 2) > 0 && passwd == null) {
            Logger.coreLog("ProxyConfig::() The 'Server.Listen.Secure' in proxy.xml = {}, buf no found 'Server.Listen.Password' or not encrypt by SM4, server will stop."
                    , secure_level);
            Logger.coreLog("ProxyConfig::()  Password '{}' decrypt error, server stoped."
                    , getProperty("Server.Listen.Password"));
            System.exit(2);
        }

        AuthenString = passwd;

        String redis_pwd = getProperty("Server.Listen.RedisPassword");

        RedisPwd = redis_pwd != null ? redis_pwd : passwd;

        ServiceName = getProperty("Server.Common.Service", "default");
        Logger.warnLog("ProxyConfig::() The ServiceName is '{}'", ServiceName);


        int maxConnection = 1000;
        try {
            maxConnection = Integer.parseInt(getProperty("Server.Listen.MaxConnections"));
        } catch (Throwable t) {
        }
        if (maxConnection < 10) {
            maxConnection = 10;
        } else if (maxConnection > 10000000) {
            maxConnection = 10000000;
        }
        MaxConnections = maxConnection;
        Logger.warnLog("ProxyConfig::() The Max connections of client is {}", MaxConnections);

        int threads = 4;
        try {
            threads = Integer.parseInt(ProxyConfig.getProperty(
                    "Server.Listen.Threads"));
        } catch (Throwable t) {
        }
        if (threads < 1) {
            threads = 1;
//        } else if (threads > MAX_THREADS) {
//            threads = MAX_THREADS;
        }
        SOCKET_PROCESS_THREADS = threads;
        Logger.warnLog("ProxyConfig::() The threads of processor is {}", threads);

        threads = (threads >>> 1) + 1;
        try {
            threads = Integer.parseInt(ProxyConfig.getProperty(
                    "Server.Listen.IO-Threads"));
            Logger.warnLog("ProxyConfig::() Configed the io-threads is {}", threads);
        } catch (Exception e) {
        }
        if (threads < 1) {
            threads = 1;
//        } else if (threads > MAX_THREADS) {
//            threads = MAX_THREADS;
        }
        SOCKET_IO_THREADS = threads;

        threads = 256;
        try {
            threads = Integer.parseInt(ProxyConfig.getProperty(
                    "Server.Listen.Backlog"));
            Logger.warnLog("ProxyConfig::() Configed the backlog is {}", threads);
        } catch (Exception e) {
        }

        SOCKET_BACKLOG = threads;

        int slow_operation = DEFAULT_SLOW_OPERATION_THRESHOLD;
        try {
            slow_operation = Integer.parseInt(getProperty("Server.Common.SlowOperationThreshold"));
            Logger.warnLog("ProxyConfig::() Configed the slow of operations is {}", slow_operation);
            if (slow_operation < 0) {
                slow_operation = Integer.MAX_VALUE;
            }
        } catch (Throwable t) {
        }
        SlowOperationThreshold = slow_operation;

        int slow_max_len = DEFAULT_SLOW_OPERATION_MAX_LEN;
        try {
            slow_max_len = Integer.parseInt(getProperty("Server.Common.SlowOperationMaxLen"));
            Logger.warnLog("ProxyConfig::() Configed the slow of operations is {}", slow_operation);
        } catch (Throwable t) {
        }
        SlowOperationMaxLen = Math.max(slow_max_len, 1);

        // 设置登录失败次数
        int max_authfailed = 0;
        try {
            max_authfailed = Integer.parseInt(getProperty("Server.Firewall.AuthFailedTimes"));
        } catch (Throwable t) {
        }
        if (max_authfailed > 0) {
            AddressRestrictions.setAuthTimes(max_authfailed);
        }

        // 设置黑白名单
        String addrlist = getProperty("Server.Firewall.Blacklist");
        if (addrlist != null) {
            AddressRestrictions.setBlacklist(addrlist);
        }
        addrlist = getProperty("Server.Firewall.Whitelist");
        if (addrlist != null) {
            AddressRestrictions.setWhitelist(addrlist);
        }

        // 屏蔽掉redis的痕迹，可能因此造成部分客户端不兼容，缺省为false
        ANTI_REDIS = "True".equalsIgnoreCase(getProperty("Server.Common.AntiRedis"));

        // 禁止危险命令执行
//        DangerousCommandFilter = "True".equalsIgnoreCase(getProperty("Server.Common.DangerousCommandFilter"));

        // 空闲时间
        int idletimeout = 0;
        try {
            idletimeout = Integer.parseInt(getProperty("Server.Listen.IdleTimeout"));
        } catch (Throwable t) {
        }
        ChannelIdleTimeout = idletimeout;

        // 进程名字
        String instance = getProperty("Server.Common.Instance");
        if (instance != null) {
            Instance = instance;
        }

        // 主节点判断策略
        MasterDeterminateByConfig = "Config".equalsIgnoreCase(getProperty("Server.Common.MasterDeterminate"));

        // 模拟cluster（cluster nodes命令）
        PRETENT_AS_A_CLUSTER = "True".equalsIgnoreCase(getProperty("Server.Common.PretendCluster"));

        // 节点分组信息
        GroupName = getProperty("Server.Common.Group");

        // Redis服务对外声明的地址
        String announce_ip = getProperty("Server.Listen.AnnounceHost");
        if (announce_ip != null) {
            announce_ip = announce_ip.trim();
            if (announce_ip.length() == 0) {
                announce_ip = null;
            }
        }
        if (AnnounceHost == null && announce_ip != null || AnnounceHost != null && !AnnounceHost.equals(announce_ip)) {
            AnnounceHost = announce_ip;
            Logger.warnLog("ProxyConfig::() Load AnnounceHost '{}' ok.", AnnounceHost);
        }

        // Redis服务对外声明的端口号
        int announce_port = 0;
        try {
            announce_port = Integer.parseInt(getProperty("Server.Listen.AnnounceRedisPort"));
        } catch (Throwable t) {
        }
        if (announce_port > 0 && AnnounceRedisPort != announce_port) {
            {
                AnnounceRedisPort = announce_port;
                Logger.warnLog("ProxyConfig::() Load AnnounceRedisPort '{}' ok.", AnnounceRedisPort);
            }
        }
    }

    private static Element getRoot(boolean mustLoad) {
        Element root = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setExpandEntityReferences(false);

            SAXReader reader = new SAXReader();

            Document doc = reader.read(ConfigFile);
            root = doc.getRootElement();
            if (mustLoad) {
                System.out.println("load config-file '" + ConfigFile.getAbsolutePath() + "' ok.");
            }
        } catch (Throwable t) {
            if (mustLoad) {
                System.out.println("\nLoad config-file error: " + t.getMessage());
                System.exit(2);
            } else {
                Logger.warnLog("Configuration()getRoot() load file '{}' failed: {}", ConfigFile, t);
            }
        } finally {
            DynamicElements.clear();
            GetConfigFromVolume.fillPropertiesFromVolume("proxy", DynamicElements);
        }
        return root;
    }

    public final static synchronized void reload() {

        if (ConfigFile.isFile()) {
            long load_time = System.currentTimeMillis() >> 16; // 64秒
            if (ConfigFileTimestamp == ConfigFile.lastModified() && ConfigCheckTimestamp == load_time) {
                return;
            }
            ConfigFileTimestamp = ConfigFile.lastModified();
            ConfigCheckTimestamp = load_time;
        } else {
            ConfigFileTimestamp = 0;
            return;
        }

        Logger.infoLog("ProxyConfig::reload() try to reload config");

        Element root = getRoot(false);

        // 修改日志级别
        String loglevel = getProperty(root, "Server.Log.Level", "warn");
        int log_level = Logger.getLogLevel();
        Logger.setLogLevel(loglevel);
        if (log_level != Logger.getLogLevel()) {
            Logger.warnLog("ProxyConfig::reload() Set log level to {}", loglevel);
        }

        // 判断密码配置是否有效
        if ((SocketSecureLevel & 2) > 0) {
            // Password
            String passwd = getProperty(root, "Server.Listen.Password");
            if (passwd != null && !passwd.equals(AuthenString)) {
                AuthenString = passwd;
                Logger.infoLog("ProxyConfig::reload() Reload new Password ok.");
            }
            // RedisPwd
            String redis_pwd = getProperty(root, "Server.Listen.RedisPassword");
            if (redis_pwd == null) {
                // SocketSecureLevel不允许reload，因此如果满足(SocketSecureLevel & 2) > 0，AuthenString就不可能为null
                redis_pwd = AuthenString;
            }
            if (redis_pwd != null && !redis_pwd.equals(RedisPwd)) {
                // 因此此处redis_pwd不可能为空
                RedisPwd = redis_pwd;
            }
        }

        int maxConnection = 1000;
        try {
            maxConnection = Integer.parseInt(getProperty(root, "Server.Listen.MaxConnections"));
        } catch (Throwable t) {
        }
        if (maxConnection < 10) {
            maxConnection = 10;
        } else if (maxConnection > 10000000) {
            maxConnection = 10000000;
        }
        if (MaxConnections != maxConnection) {
            MaxConnections = maxConnection;
            Logger.warnLog("ProxyConfig::reload() The Max connections of client is {}", MaxConnections);
        }

        int slow_operation = DEFAULT_SLOW_OPERATION_THRESHOLD;
        try {
            slow_operation = Integer.parseInt(getProperty(root, "Server.Common.SlowOperationThreshold"));
            Logger.infoLog("ProxyConfig::reload() Configed the slow of operations is {}", slow_operation);
            if (slow_operation < 0) {
                slow_operation = Integer.MAX_VALUE;
            }
        } catch (Throwable t) {
        }
        if (SlowOperationThreshold != slow_operation) {
            SlowOperationThreshold = slow_operation;
            Logger.warnLog("ProxyConfig::reload() set SlowOperationThreshold to {}", slow_operation);
        }

        // 设置登录失败次数
        int max_authfailed = 0;
        try {
            max_authfailed = Integer.parseInt(getProperty(root, "Server.Firewall.AuthFailedTimes"));
        } catch (Throwable t) {
        }
        if (AddressRestrictions.getMaxAuthTimes() != max_authfailed) {
            AddressRestrictions.setAuthTimes(max_authfailed);
            Logger.warnLog("ProxyConfig::reload() set AuthFailedTimes to {}", max_authfailed);
        }

        // 屏蔽掉redis的痕迹，可能因此造成部分客户端不兼容
        boolean bool_data = "True".equalsIgnoreCase(getProperty(root, "Server.Common.AntiRedis"));
        if (ANTI_REDIS != bool_data) {
            ANTI_REDIS = bool_data;
            Logger.warnLog("ProxyConfig::reload() set AntiRedis to {}", bool_data);
        }

        // 禁止危险命令执行
//        bool_data = "True".equalsIgnoreCase(getProperty(root, "Server.Common.DangerousCommandFilter"));
//        if (DangerousCommandFilter != bool_data) {
//            DangerousCommandFilter = bool_data;
//            Logger.warnLog("ProxyConfig::reload() set DangerousCommandFilter to {}", bool_data);
//        }

        // 进程名字
        String instance = getProperty(root, "Server.Common.Instance");
        if (instance != null) {
            Instance = instance;
        }

        // 主节点切换策略
        MasterDeterminateByConfig = "Config".equalsIgnoreCase(getProperty(root, "Server.Common.MasterDeterminate"));

        PRETENT_AS_A_CLUSTER = "True".equalsIgnoreCase(getProperty(root, "Server.Common.PretendCluster"));

        // 节点分组信息
        GroupName = getProperty(root, "Server.Common.Group");

        // Redis服务对外声明的地址
        String announce_ip = getProperty(root, "Server.Listen.AnnounceHost");
        if (announce_ip != null) {
            announce_ip = announce_ip.trim();
            if (announce_ip.length() == 0) {
                announce_ip = null;
            }
        }
        if (AnnounceHost == null && announce_ip != null || AnnounceHost != null && !AnnounceHost.equals(announce_ip)) {
            AnnounceHost = announce_ip;
            Logger.warnLog("ProxyConfig::() Load AnnounceHost '{}' ok.", AnnounceHost);
        }

        // Redis服务对外声明的端口号
        int announce_port = 0;
        try {
            announce_port = Integer.parseInt(getProperty(root, "Server.Listen.AnnounceRedisPort"));
        } catch (Throwable t) {
        }
        if (announce_port > 0 && AnnounceRedisPort != announce_port) {
            {
                AnnounceRedisPort = announce_port;
                Logger.warnLog("ProxyConfig::() Load AnnounceRedisPort '{}' ok.", AnnounceRedisPort);
            }
        }
    }

    // 日志
    public final static Log getServerLog() {
        return Logger;
    }

    /**
     * 当前Server的唯一标示 取值是第一块有效网卡的MAC地址-端口号
     */
    public final static String getIdentify() {
        return SERVER_IDENTIFY;
    }

    public final static String getAuthenString() {
        return AuthenString;
    }

    public final static int getRedisPort() {
        return RedisPort;
    }

    public final static String getRedisPwd() {
        return RedisPwd;
    }

    public static OriginalSocketAddress getLocalAddress() {
        return LocalAddress;
    }

    /**
     * 返回服务器开放端口的安全级别
     *
     * @return 安全级别
     */
    public final static int getSecureLevel() {
        return SocketSecureLevel;
    }

    /**
     * 取进程的主目录位置，启动程序时通过-Ddhcp.root=...参数指定
     *
     * @return
     */
    public static String getServerHome() {
        return System.getProperty("server.home", ".");
    }

    private static Element getElement(Element root, String key) {
        if (key == null || root == null) {
            return null;
        }

        if (key.startsWith("Server.")) {
            key = key.substring(7);
        }

        String[] keys = key.split("\\.");
        Element e = root;
        for (String s : keys) {
            e = e.element(s);
            if (e == null) {
                return null;
            }
        }
        return e;
    }

    public static String getEnv(String key, String def) {
        String ret = System.getenv(key);
        return ret != null ? ret : def;
    }

    public static String getProperty(String key, String def) {
        String ret = getProperty(key);
        return ret == null ? def : ret;
    }

    public static String getProperty(Element root, String key, String def) {
        String ret = getProperty(root, key);
        return ret == null ? def : ret;
    }

    public static String getProperty(String key) {
        return getProperty(Root, key);
    }

    public static String getProperty(Element root, String key) {
        Element e = getElement(root, key);

        String ret = e != null ? PropertyPlaceholderHelper.INSTANCE.replacePlaceholders(e.getTextTrim(), DynamicElements) : null;
        return ret;
    }

    /**
     * 取指定key的子项的内容
     *
     * @param key
     * @return
     */
    public static List<Element> getPropertySubList(String key) {
        ArrayList<Element> list = new ArrayList<>();
        Element e = getElement(Root, key);
        if (e != null) {
            for (Element element : e.elements()) {
                list.add(element);
            }
        }
        return list;
    }

    /**
     * 查找整数形的配置项，无配置返回-1，配置非法返回-2
     *
     * @param key
     * @return
     */
    public static long getLongProperty(String key) {
        String value = getProperty(key);
        if (value == null) {
            return -1;
        }
        long data = -2;
        try {
            data = Long.parseLong(value);
        } catch (Exception e) {
            Logger.warnLog("ProxyConfig::getLongProperty() Error '{}' config: {}", key, value);
        }
        return data;
    }

    /**
     * 返回统一的监听端口
     *
     * @return
     */
    public static int getListeningPort() {
        return ListeningPort;
    }

    /**
     * 系统对外接口采用的字符集
     *
     * @return
     */
    public static Charset getAppCharset() {
        return AppCharset;
    }

    /**
     * 返回当前节点对应的服务名称
     *
     * @return
     */
    public static String getServiceName() {
        return ServiceName;
    }

    public static boolean isRedirect() {
        // 对应配置文件的 Server.Common.RedirectStdOut
        return Redirect;
    }


    public static int getMaxConnections() {
        return MaxConnections;
    }

    public static int getSocketProcessThreads() {
        return SOCKET_PROCESS_THREADS;
    }

    public static int getSocketIoThreads() {
        return SOCKET_IO_THREADS;
    }


    public static int getSocketBacklog() {
        return SOCKET_BACKLOG;
    }

    public static boolean isAntiRedis() {
        return ANTI_REDIS;
    }

    //    public static boolean isDangerousCommandFilter() {
//        return DangerousCommandFilter;
//    }
    public static int getSlowOperationThreshold() {
        return SlowOperationThreshold;
    }

    public static int getSlowOperationMaxLen() {
        return SlowOperationMaxLen;
    }

    public static boolean firstInConfigIsMaster() {
        return MasterDeterminateByConfig;
    }

    public static int getChannelIdleTimeout() {
        return ChannelIdleTimeout;
    }

    public static String getInstance() {
        return Instance;
    }

    public static boolean isPretentAsACluster() {
        return PRETENT_AS_A_CLUSTER;
    }

    public static String getGroupName() {
        return GroupName;
    }

    public static String getAnnounceHost() {
        return AnnounceHost;
    }

    public static int getAnnounceRedisPort() {
        return AnnounceRedisPort;
    }

}
