package com.tongtech.proxy.jmx;

import com.tongtech.proxy.core.center.ProxyData;

import com.tongtech.proxy.core.utils.CpuUtil;
import com.tongtech.proxy.core.utils.ProcessCounter;

import com.tongtech.proxy.core.server.ConnectionCounter;
import com.tongtech.proxy.core.server.ProxyController;

import java.lang.management.ManagementFactory;
import java.util.Map;

import static com.tongtech.proxy.Version.Version;
import static com.tongtech.proxy.core.StaticContent.getMemorySizeHuman;

public class StatusColector {
    // cfg文件中配置的占用内存大小
//    private final static long USED_MEMORY_STATIC;

    private volatile static ProxyController Controller = null;

    // 物理内存大小
    // 物理内存使用过多的告警在CacheTable的run方法中触发
    private static final Object PhysicalMemoryLocker = new Object();
    private static long PhysicalMemoryTimestamp;
    private static long TOTAL_PHYSICAL_MEMORY;
    private static volatile String TOTAL_PHYSICAL_MEMORY_HUMAN;

    // 最大可用内存
    public static final long JVM_AVAILABLE;
    public static final String JVM_AVAILABLE_HUMAN;

    // 当前最新处理的同步命令的序列号
    private volatile static long ReceivedSyncSN = 0;

    static {
//        long l = -1;
//        try {
//            l = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
//        } catch (Throwable t) {
//        }
//        TOTAL_PHYSICAL_MEMORY = l;
//        TOTAL_PHYSICAL_MEMORY_HUMAN = getMemorySizeHuman(l);

        JVM_AVAILABLE = Runtime.getRuntime().maxMemory();
        JVM_AVAILABLE_HUMAN = getMemorySizeHuman(JVM_AVAILABLE);
    }

    public static void setController(ProxyController mdb) {
        Controller = mdb;
    }

    public static long getTotalPhysicalMemory() {
        synchronized (PhysicalMemoryLocker) {
            if (System.currentTimeMillis() - PhysicalMemoryTimestamp > 600_000) {
                PhysicalMemoryTimestamp = System.currentTimeMillis();
                try {
                    TOTAL_PHYSICAL_MEMORY = ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
                } catch (Throwable t) {
                }
                TOTAL_PHYSICAL_MEMORY_HUMAN = getMemorySizeHuman(TOTAL_PHYSICAL_MEMORY);
            }
        }
        return TOTAL_PHYSICAL_MEMORY;
    }

    public static String getTotalPhysicalMemoryHuman() {
        return TOTAL_PHYSICAL_MEMORY_HUMAN;
    }

    public static String getVersion() {
        return Version;
    }

    /**
     * 应用占用的总(物理)内存
     *
     * @return
     */
    public static long getMemoryTotal() {
        return Runtime.getRuntime().totalMemory();
    }

//    public static int getLuaSize() {
//        return MessageRedisImpCore.getLuaCount();
//    }

    public static long getSizeDynamic() {
        return 0;
    }

    public static long getSizeDynamicPeak() {
        return 0;
    }

    public static long getJvmAllocated() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getJvmFree() {
        return Runtime.getRuntime().freeMemory();
    }

    public static long getClientCurrentConnections() {
        return ConnectionCounter.getCurrentConnectionsRedis();
    }

    public static long getClientTotalConnections() {
        return ConnectionCounter.getTotalConnectionsRedis() + ConnectionCounter.getTotalConnectionsRds();
    }

    public static long getClientRejectConnections() {
        return ConnectionCounter.getConnectionRejectedRedis() + ConnectionCounter.getConnectionRejectedRds();
    }

    public static double getConnectionsRatio() {
        // 内存使用率 used_memory/used_memory_rss
        // 连接数使用率 connected_clients/ maxclients
        long total = getClientTotalConnections();
        return total > 0 ? (getClientCurrentConnections() * 100 / getClientTotalConnections()) / 100.0 : 0.0;
    }

    /**
     * 总处理量
     *
     * @return
     */
    public static long getTotalProcessed() {
        return ProcessCounter.getTotalProcessed();
    }

    public static long getProcessSecond() {
        return (long) ProxyData.getProcessSpeedCurrent();
    }

    public static float getProcessMinute() {
        return ProxyData.getProcessSpeedMinute();
    }

    public static Map<String, Integer> getKeyspace() {

        return null;
    }

    public static long getTotalKeys() {
        long total = 0;
        return total;
    }

    public static long getExpiredKeys() {
        return 0;
    }

    public static long getEvictedKeys() {
        return 0;
    }

    public static long getTotalKeyspaceHits() {
        return ProcessCounter.getTotalKeyspaceHits();
    }

    public static long getTotalKeyspaceMisses() {
        return ProcessCounter.getTotalKeyspaceMisses();
    }

    public static long getTotalNetworkInput() {
        return ProcessCounter.getTotalNetworkInflow();
    }

    public static double getInstantaneousNetworkInput() {
        return ProcessCounter.getInstantaneousNetworkInflow();
    }

    public static long getTotalNetworkOutput() {
        return ProcessCounter.getTotalNetworkOutflow();
    }

    public static double getInstantaneousNetworkOutflow() {
        return ProcessCounter.getInstantaneousNetworkOutflow();
    }

    public static long getCpuSystemLoad() {
        return CpuUtil.systemLoad();
    }

    public static long getCpuProcessLoad() {
        return CpuUtil.processLoad();
    }

    public static long getCpuAvailables() {
        return CpuUtil.availableProcessors();
    }

    public static long getReceivedReplicaOffset() {
        return ReceivedSyncSN;
    }

    public static void setReceivedSyncSN(long receivedSyncSN) {
        ReceivedSyncSN = receivedSyncSN;
    }
}
