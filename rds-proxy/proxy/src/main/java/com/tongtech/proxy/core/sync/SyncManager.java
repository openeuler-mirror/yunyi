package com.tongtech.proxy.core.sync;

import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.ProxyDynConfig;
import com.tongtech.proxy.core.utils.Log;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static com.tongtech.proxy.core.StaticContent.SCHEDULED_THREAD_POOL_EXECUTOR;

/**
 * 同步管理类。通过配置决定同步服务类实例的数量，每个同步类实例有1个队列，有单独的线程负责数据同步。
 * 可以通过配置多个同步类实例实现多线程同步，提高数据同步的性能。
 *
 * @author ligang
 */
public class SyncManager extends Thread {
    // 统计线程执行间隔时间
    private static final int THREAD_SLEEP_TIME = 1000;

    //    private static final HashMap<Integer, SyncManager> Instances = new HashMap<>(10);
    private static final SyncManager Instance = new SyncManager();

    private final SyncSender[] SyncSenders;

    // 同步列表的并行数量
    // 对应配置文件中 Server.Notify.SyncListNumber
    private final int TotalLists;

    // 每个同步列表的长度
    // 对应文件中 Server.Notify.SyncListLength
    private final int ListLength;

//    // 需要同步的对端服务器的列表数组
//    private final InetSocketAddress[] RemoteAddresses;
//
//    // 需要同步的对端服务器最大数量的配置
//    private final int MaxConnectToServers;

    // private final RedoLog Redo;

    private final int TableId = 0;

    // 日志类
    private static final Log Logger = ProxyConfig.getServerLog();

    private static final boolean IsDebug = Logger.isDebug();

    private volatile long SyncTimestampLoad = 0;

    /**
     * 创建并返回同步管理类实例
     *
     * @return
     */
    public static SyncManager getInstance() {
        return Instance;
    }

    private SyncManager() {

        int listLength = 1000;
        try {
            listLength = Integer.parseInt(ProxyConfig.getProperty("Server.Notify.SyncListLength"));
            Logger.warnLog("SyncManager::() Load 'Server.Notify.SyncListLength' from proxy.xml is {}", listLength);
        } catch (Throwable t) {
        }
        ListLength = listLength;

        int totallist = 1;
        try {
            totallist = Integer.parseInt(ProxyConfig.getProperty("Server.Notify.SyncListNumber"));
            Logger.warnLog("SyncManager::() Load 'Server.Notify.SyncListNumber' from proxy.xml is {}", totallist);
        } catch (Throwable t) {
        }
        TotalLists = totallist;

        if (TotalLists > 0) {
            // Create sync-servers
            SyncSenders = new SyncSender[TotalLists];
            for (int i = 0; i < TotalLists; ++i) {
                SyncSenders[i] = new SyncSender(0, ListLength, false);
            }
//            SyncSenders[0] = new SyncSender(ListLength);
        } else {
            SyncSenders = null;
        }

        // start reload-thread
        SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(this, 0, 950, TimeUnit.MILLISECONDS);
    }

    /**
     * 返回同步服务处理类
     *
     * @return
     */
    public SyncSender getSyncServer(int hash) {
        int hash_server = 0;

        // 不需要同步
        if (TotalLists <= 0) {
            if (IsDebug) {
                Logger.debugLog("SyncManager::getSyncServer() List in null");
            }
            return null;
        }

        if (hash < 0) {
            hash_server = (int) (Math.random() * TotalLists);
            if (IsDebug) {
                Logger.debugLog("SyncManager::getSyncServer() Get server by random");
            }
        } else {
            hash_server = (hash & 0xffff) % TotalLists;
            if (IsDebug) {
                Logger.debugLog("SyncManager::getSyncServer() Get List{} by hash", hash_server);
            }
        }

        return SyncSenders[hash_server];
    }

    /**
     * close
     */
    public void close() {
        for (SyncSender sender : SyncSenders) {
            sender.close();
        }
    }

    /**
     * 更新同步列表
     */
    private void reloadSyncNodes() {
        long cur_load = ProxyDynConfig.getSyncTimestamp();
        if (SyncTimestampLoad != cur_load && TotalLists > 0) {
            Logger.debugLog("SyncManager::reload() Configuration is changed, try to update.");
            InetSocketAddress[] addresses = ProxyDynConfig.getSyncAddresses();
            for (SyncSender sender : SyncSenders) {
                sender.reloadConnector(addresses);
            }
            SyncTimestampLoad = cur_load;
            if (Logger.isInfo()) {
                StringBuilder buf = new StringBuilder(256);
                if (addresses != null && addresses.length > 0) {
                    buf.append('[');
                    for (InetSocketAddress addr : addresses) {
                        buf.append(' ').append(addr.getHostString()).append(":").append(addr.getPort());
                    }
                    buf.append(" ]");
                } else {
                    buf.append("[ null ]");
                }
                Logger.infoLog("SyncManager::reload() Configuration becomes {}.", buf);
            }
        } else if (SyncTimestampLoad != cur_load) {
            SyncTimestampLoad = cur_load;
            Logger.warnLog("SyncManager::reload() Configuration is changed, but ListNumbers in Table{} is zero."
                    , TableId);
        }
    }

    @Override
    public void run() {
        // 读配置
        reloadSyncNodes();
    }
}
