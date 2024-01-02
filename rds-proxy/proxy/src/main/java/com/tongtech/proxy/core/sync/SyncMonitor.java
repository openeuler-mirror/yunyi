package com.tongtech.proxy.core.sync;

import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.OriginalSocketAddress;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.ProxyDynConfig;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

public class SyncMonitor {
    private static final Log Logger = ProxyConfig.getServerLog();

    private static long sn = 0;
    private static final long max = Long.MAX_VALUE;

    private static final ConcurrentHashMap<InetSocketAddress, SyncEndPoint> Peers = new ConcurrentHashMap<>();

    private static volatile long SyncTimestampLoad = 0;

    private static volatile ArrayList<SyncEndPoint> PearsList = null;

    private static volatile SyncEndPoint Master = null;

    public static synchronized long getAndIncrease() {
        if (sn >= max || sn < 0) {
            sn = 0;
        }
        return ++sn;
    }

    public static synchronized long get() {
        return sn;
    }

    public static void checkPeers() {
        try {
            reloadConnections();
            ArrayList<SyncEndPoint> points = PearsList;

            if (points != null) {
                for (SyncEndPoint point : points) {
                    point.checkAlive();
                }
            }
        } catch (Throwable t) {
            Logger.warnLog(t, "SyncMonitor::checkPeers() Error occur: {}", t.getMessage());
        } finally {
            try {
                findMaster();
            } catch (Throwable t2) {
            }
        }
    }

    private static void reloadConnections() {
        long cur_load = ProxyDynConfig.getSyncTimestamp();

        ArrayList<SyncEndPoint> list = new ArrayList<>();
        synchronized (Peers) {
            if (SyncTimestampLoad != cur_load) {
                SyncTimestampLoad = cur_load;
                OriginalSocketAddress[] addresses = ProxyDynConfig.getSyncAddresses();
                Peers.clear();
                if (addresses != null) {
                    for (OriginalSocketAddress address : addresses) {
                        if (address != null) {
                            SyncEndPoint endPoint = new SyncEndPoint(address);
                            Peers.put(address, endPoint);
                            list.add(endPoint);
                        }
                    }
                }
                PearsList = list;
                Logger.infoLog("SyncMonitor::reloadConnections() Load address {} ok.", Arrays.toString(addresses));
            }
        }
    }

    /**
     * 定时调用
     */
    private static void findMaster() {
        ArrayList<SyncEndPoint> connectors = PearsList;

        // 查找主节点
        SyncEndPoint master = null;
        if (connectors != null && connectors.size() > 0) {
            if (ProxyConfig.firstInConfigIsMaster()) {
                master = connectors.get(0);
            } else {
                // 找第一个活跃的节点
                for (int i = 0; i < connectors.size(); ++i) {
                    SyncEndPoint connector = connectors.get(i);
                    if (connector.isAlive()) {
                        master = connector;
                        break;
                    }
                }
            }
        }
        Master = master;
    }

    /**
     * ProxyServer.main()主方法中调用，
     *
     * @return 只有在收到了Center下发的同步列表且列表中不包含自己时返回才会false，否则返回true。
     */
    public static boolean isIncludingMyself() {
        ArrayList<SyncEndPoint> connectors = PearsList;
        if (connectors != null && connectors.size() > 0) {
            for (SyncEndPoint endPoint : connectors) {
                if (endPoint.isMyself()) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean setExchangedNo(InetSocketAddress address, long no) {
        if (address != null) {
            SyncEndPoint status = Peers.get(address);
            if (status != null) {
                synchronized (status) {
                    if (status.getSerialNumber() < no || status.getSerialNumber() - no > Integer.MAX_VALUE/*序列号回滚了*/) {
                        return status.setSerialNumber(no);
                    }
                }
            }
        }
        return false;
    }

    public static long getExchangedNo(InetSocketAddress address) {
        if (address != null) {
            SyncEndPoint status = Peers.get(address);
            if (status != null) {
                return status.getSerialNumber();
            }
        }
        return 0;
    }

    public static boolean isMaster() {
        if (Peers.size() == 0) {
            return true;
        }
        SyncEndPoint master = Master;
        if (master != null) {
            return master.isMyself();
        }
        return false;
    }

    /**
     * 取复制列表
     *
     * @return 返回空或同步列表，返回列表必不为空，其中第一项为master地址，如果为空则当前节点是master节点，后续项为slave节点地址。
     * 因此返回值至少会包含1项，且各项均为connected状态
     */
    public static synchronized ArrayList<ReplicationNode> getReplication() {
        if (Peers.size() == 0) {
            return null;
        }
        ArrayList<SyncEndPoint> connectors = PearsList;

        ArrayList<ReplicationNode> list = new ArrayList<>();
        SyncEndPoint master = Master;
        if (master != null) {
            ReplicationNode node = new ReplicationNode(master.isMyself(), master.isAlive(), master.getAddress()
                    , new OriginalSocketAddress(master.getHost(), master.getRemoteHostAlias(), master.getRedisPort(), master.getRedisPortAlias())
                    , master.getIdentify(), master.getAliveCheckedTimestamp());
            list.add(node);
        }
        if (connectors != null) {
            for (int i = 0; i < connectors.size(); ++i) {
                SyncEndPoint connector = connectors.get(i);
                if (connector != master) {
                    ReplicationNode node = new ReplicationNode(connector.isMyself(), connector.isAlive(), connector.getAddress()
                            , new OriginalSocketAddress(connector.getHost(), connector.getRemoteHostAlias(), connector.getRedisPort(), connector.getRedisPortAlias())
                            , connector.getIdentify(), connector.getAliveCheckedTimestamp());
                    list.add(node);
                }
            }
        }

        if (list.size() == 0) {
            return null;
        }
        return list;
    }

    public static int getReplications() {
        return Peers.size();
    }
}
