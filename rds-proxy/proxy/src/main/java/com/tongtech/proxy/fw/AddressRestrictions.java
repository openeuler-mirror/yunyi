package com.tongtech.proxy.fw;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AddressRestrictions {
//    private static final Log logger = Configuration.getServerLog();

    private static final long DELAYTIME = 5000;

    private static volatile HashSet<InetAddress> Whitelist = null;
    private static volatile HashSet<InetAddress> Blacklist = null;

    private static volatile int MaxAuthTimes = 0;
    private static volatile ConcurrentHashMap<InetAddress, Counter> ClientAuthTimes = null;
    private static volatile ConcurrentHashMap<InetAddress, Counter> Dynamiclist = null;

    // 每10分钟执行1一次clean，清理10分钟前的Counter，防止恶意客户端的内存泄露攻击造成 ClientAuthTimes 或 Dynamiclist 内对象过多
    private static final long CleanInterval = 10 * 60 * 1000;
    private static long LastCleanTime = 0;


    /**
     * 支持地址段输入，递归的方式解析各段的地址范围
     *
     * @param list
     * @param addr
     */
    private static void setCollection(Set<InetAddress> list, String addr) {
        int begin = 0, end = -1;
        boolean isv6 = false;
        int idx = addr.indexOf('-');
        // idx》=0，有‘-’，怀疑有地址段
        if (idx >= 0) {
            // 找到地址段的开始位置
            for (int i = idx - 1; i >= 0; --i) {
                char c = addr.charAt(i);
                if (c == '.' || c == ':') {
                    begin = i + 1;
                    if (c == ':') {
                        isv6 = true;
                    }
                    break;
                }
            }
            // 找到地址段的结束位置
            for (int i = idx + 1; i < addr.length(); ++i) {
                end = i + 1;
                char c = addr.charAt(i);
                if (c == '.' || c == ':') {
                    end = i;
                    if (c == ':') {
                        isv6 = true;
                    }
                    break;
                }
            }
            if (end > begin) {
                try {
                    // 获取地址段的开始值
                    int start = 0;
                    if (begin < idx) {
                        String s_start = addr.substring(begin, idx);
                        if (isv6) {
                            start = Integer.parseInt(s_start, 16);
                        } else {
                            start = Integer.parseInt(s_start);
                        }
                    }

                    // 获取地址段的结束值
                    int stop = 0;
                    String s_end = addr.substring(idx + 1, end);
                    if (isv6) {
                        stop = Integer.parseInt(s_end, 16);
                    } else {
                        stop = Integer.parseInt(s_end);
                    }

                    // 循环递归调用处理全部的地址段配置
                    for (int i = start; i <= stop; ++i) {
                        StringBuilder buf = new StringBuilder(32);
                        if (begin > 0) {
                            buf.append(addr, 0, begin);
                        }
                        buf.append(Integer.toString(i, isv6 ? 16 : 10));
                        if (end < addr.length()) {
                            buf.append(addr.substring(end));
                        }
                        setCollection(list, buf.toString());
                    }
                    return;
                } catch (Throwable t) {
                }
            }
        }

        try {
            // 此处真正创建地址
            list.add(InetAddress.getByName(addr));
//            logger.infoLog("AddressRestrictions::setCollection() Set address {} ok.", addr);
        } catch (Throwable e) {
//            logger.warnLog("AddressRestrictions::setCollection() Error to analysis '{}': {}", addr, e);
        }
    }

    /**
     * 初始化方法
     * 设置白名单，只要调用该方法首先会清除黑名单。如果输入的字符串是空代表不允许任何接入请求
     *
     * @param slist
     */
    public static void setWhitelist(String slist) {
        Blacklist = null;
        HashSet<InetAddress> list = new HashSet<>();

        try {
            String[] lists = slist.trim().split(",");
            for (String s : lists) {
                s = s.trim();
                if (s.length() > 0) {
//                    logger.infoLog("AddressRestrictions::setWhitelist() Set white-list '{}'", s);
                    setCollection(list, s);
//                    logger.infoLog("AddressRestrictions::setWhitelist() Set white-list ok.");
                }
            }
        } catch (Throwable t) {
//            logger.warnLog("AddressRestrictions::setWhitelist() Set whites '{}' failed: {}", slist, t);
        }
        Whitelist = list;
    }

    /**
     * 初始化方法
     * 只有输入了至少1个有效的地址，才会设置黑名单同时清空白名单
     *
     * @param slist
     */
    public static void setBlacklist(String slist) {
        HashSet<InetAddress> list = new HashSet<>();
        try {
            String[] lists = slist.split(",");
            for (String s : lists) {
                s = s.trim();
                if (s.length() > 0) {
//                    logger.infoLog("AddressRestrictions::setBlacklist() Set black-list '{}'", s);
                    setCollection(list, s);
//                    logger.infoLog("AddressRestrictions::setBlacklist() Set black-list ok.");
                }
            }
        } catch (Throwable t) {
//            logger.warnLog("AddressRestrictions::setBlacklist() Set blacks '{}' failed: {}", slist, t);
        }

        if (list.size() > 0) {
            Blacklist = list;
            Whitelist = null;
        } else {
            Blacklist = null;
        }
    }

    /**
     * 初始化方法
     * 当输入参数大于0时开始记录客户端失败次数
     *
     * @param times
     */
    public static void setAuthTimes(int times) {
        if (times > 0) {
            MaxAuthTimes = times;
            Dynamiclist = new ConcurrentHashMap<>();
            ClientAuthTimes = new ConcurrentHashMap<>();
        } else {
            ClientAuthTimes = null;
            Dynamiclist = null;
            MaxAuthTimes = 0;
        }
    }

    public static int getMaxAuthTimes() {
        return MaxAuthTimes;
    }

    /**
     * 客户端密码验证成功调用此方法
     *
     * @param addr
     */
    public static void authSuccessed(InetAddress addr) {
        ConcurrentHashMap<InetAddress, Counter> auths = ClientAuthTimes;
        if (auths != null) {
            auths.remove(addr);
        }
    }

    /**
     * 客户端密码验证失败调用此方法
     *
     * @param addr
     * @return
     */
    public static boolean authFailed(InetAddress addr) {
        int max = MaxAuthTimes;
        ConcurrentHashMap<InetAddress, Counter> dynamic = Dynamiclist;
        ConcurrentHashMap<InetAddress, Counter> auths = ClientAuthTimes;
        if (auths != null) {
            Counter counter = auths.get(addr);
            if (counter == null) {
                counter = new Counter();
                auths.put(addr, counter);
            }
            int i = counter.incrementAndGet();
            if (i >= max) {
                auths.remove(addr);
                dynamic.put(addr, counter);
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否允许客户端接入，有3个拒绝条件：1、多次密码验证失败；2、白名单中没有；3、黑名单中有
     *
     * @param addr
     * @throws IOException
     */
    public static void allow(InetAddress addr) throws IOException {
        ConcurrentHashMap<InetAddress, Counter> dynamic = Dynamiclist;
        if (dynamic != null && dynamic.containsKey(addr)) {
            Counter counter = dynamic.get(addr);
            if (counter.timestamp + DELAYTIME < System.currentTimeMillis()) {
                dynamic.remove(addr);
            } else {
                throw new IOException(addr.getHostName() + " authentication failed too many times.");
            }
        }

        HashSet<InetAddress> wlist = Whitelist;
        HashSet<InetAddress> blist = Blacklist;
        if (wlist != null && !wlist.contains(addr)) {
            throw new IOException(addr.getHostName() + " access is blocked by whitelist.");
        } else if (blist != null && blist.contains(addr)) {
            throw new IOException(addr.getHostName() + " access is blocked by blacklist.");
        }
    }

    /**
     * 清理过期是数据（10分钟前的），防止过期对象长期不能释放造成内存泄露
     * 本方法在客户端关闭连接（或连接中断）时被调用
     */
    public static void clean() {
        ConcurrentHashMap<InetAddress, Counter> clients = ClientAuthTimes;
        ConcurrentHashMap<InetAddress, Counter> dynamics = Dynamiclist;
        if (clients != null && dynamics != null) {
            boolean needclean = false;
            synchronized (AddressRestrictions.class) {
                if (LastCleanTime + CleanInterval < System.currentTimeMillis()) {
                    LastCleanTime = System.currentTimeMillis();
                    needclean = true;
                }
            }
            if (needclean) {
                long exptime = System.currentTimeMillis() - CleanInterval;
                for (InetAddress addr : clients.keySet()) {
                    Counter counter = clients.get(addr);
                    if (counter.timestamp < exptime) {
                        clients.remove(addr);
                    }
                }
                for (InetAddress addr : dynamics.keySet()) {
                    Counter counter = dynamics.get(addr);
                    if (counter.timestamp < exptime) {
                        dynamics.remove(addr);
                    }
                }
            }
        }
    }

    private static class Counter {
        private long timestamp = 0;
        private int times = 0;

        private synchronized int incrementAndGet() {
            ++times;
            timestamp = System.currentTimeMillis();
            return times;
        }
    }

//    public static void main(String[] arg) {
//        HashSet<InetAddress> addresses = new HashSet<>();
//        setCollection(addresses, "-1.3-2.0.1-2");
//        System.out.println("Addresses(" + addresses.size() + "):");
//        for (InetAddress address : addresses) {
//            System.out.println(address);
//        }
//    }
}
