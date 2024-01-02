package com.tongtech.proxy.core.utils;

import com.tongtech.proxy.core.center.ProxyData;

public class ProcessCounter extends Thread {
    private static final int INTEVAL = 10000;

    private static final int PARALLEL = 1 << 3;
    private static final int PARALLELMUST = PARALLEL - 1;

    // 每秒处理量统计
    private static final int[] Counters = new int[PARALLEL];
    private static final Integer[] Lockers = new Integer[PARALLEL];

    // 总处理量统计
    private static final long[] TotalProcess = new long[PARALLEL];
    private static volatile long TotalProcessed = 0;

    // 查询命令的总命中量
    private static final Object HitsLocker = new Object();
    private static long TotalKeyspaceHits = 0;

    // 查询命令的总失败量
    private static final Object MissesLocker = new Object();
    private static long TotalKeyspaceMisses = 0;

    // 网络流入流量
    private static final Object NetWorkInflowLocker = new Object();
    private static long TotalNetwrokInflow = 0;
    private static long InstantaneousInflowTimestamp = 0;
    private static long InstantaneousInflowLastData = 0;
    private static double InstantaneousInflow = 0;

    // 网络流出流量
    private static final Object NetWorkOutflowLocker = new Object();
    private static long TotalNetwrokOutflow = 0;
    private static long InstantaneousOutflowTimestamp = 0;
    private static long InstantaneousOutflowLastData;
    private static double InstantaneousOutflow = 0;
    // 总命中量

    static {

        for (int i = 0; i < PARALLEL; i++) {
            Counters[i] = 0;
            TotalProcess[i] = 0;
//            KeyspaceHits[i] = 0;
//            KeyspaceMisses[i] = 0;
            Lockers[i] = new Integer(i);
        }

        Thread thread = new Thread() {
            public void run() {
                while (true) {
                    long last_time = System.currentTimeMillis();
                    try {
                        sleep(INTEVAL);
                    } catch (Exception e) {
                    }
                    long inteval = System.currentTimeMillis() - last_time;
                    int total = 0;
                    long process = 0;
                    for (int i = 0; i < PARALLEL; i++) {
                        synchronized (Lockers[i]) {
                            total += Counters[i];
                            Counters[i] = 0;
                            process += TotalProcess[i];
                        }
                    }

                    TotalProcessed = process;

                    ProxyData.setProcessSpeed(total, inteval);
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    /**
     *
     */
    public static void increase() {
        int hash = Thread.currentThread().hashCode() & PARALLELMUST;
        synchronized (Lockers[hash]) {
            ++Counters[hash];
            ++TotalProcess[hash];
        }
    }

    public static long getTotalProcessed() {
        return TotalProcessed;
    }

    /**
     * 查询命中量
     */
    public static void keyspaceHits() {
        synchronized (HitsLocker) {
            ++TotalKeyspaceHits;
            if (TotalKeyspaceHits < 0) {
                TotalKeyspaceHits = 0;
            }
        }
    }

    public static long getTotalKeyspaceHits() {
        synchronized (HitsLocker) {
            return TotalKeyspaceHits;
        }
    }

    /**
     * 查询失败量
     */
    public static void keyspaceMisses() {
        synchronized (MissesLocker) {
            ++TotalKeyspaceMisses;
            if (TotalKeyspaceMisses < 0) {
                TotalKeyspaceMisses = 0;
            }
        }
    }

    public static long getTotalKeyspaceMisses() {
        synchronized (MissesLocker) {
            return TotalKeyspaceMisses;
        }
    }

    /**
     * 网络流入流量
     *
     * @return
     */
    public static long getTotalNetworkInflow() {
        synchronized (NetWorkInflowLocker) {
            return TotalNetwrokInflow;
        }
    }
    public static double getInstantaneousNetworkInflow() {
        synchronized (NetWorkInflowLocker) {
            return InstantaneousInflow;
        }
    }

    public static void addNetworkInflow(long l) {
        synchronized (NetWorkInflowLocker) {
            TotalNetwrokInflow += l;
            if (TotalNetwrokInflow < 0) {
                TotalNetwrokInflow -= Long.MIN_VALUE;
            }
        }
    }

    /**
     * 网络流出流量
     *
     * @return
     */
    public static long getTotalNetworkOutflow() {
        synchronized (NetWorkOutflowLocker) {
            return TotalNetwrokOutflow;
        }
    }

    public static double getInstantaneousNetworkOutflow() {
        synchronized (NetWorkInflowLocker) {
            return InstantaneousOutflow;
        }
    }

    public static void addNetworkOutflow(long l) {
        synchronized (NetWorkOutflowLocker) {
            TotalNetwrokOutflow += l;
            if (TotalNetwrokOutflow < 0) {
                TotalNetwrokOutflow -= Long.MIN_VALUE;
            }
        }
    }

    /**
     * 计算每秒瞬时流量
     */
    public static void calculateInstantaneous() {
        long timestamp = System.currentTimeMillis();
        synchronized (NetWorkInflowLocker) {
            long last_timestamp = InstantaneousInflowTimestamp;
            if (last_timestamp < timestamp) {
                if (last_timestamp > 0) {
                    long instant = (TotalNetwrokInflow - InstantaneousInflowLastData);
                    if (instant < 0) {
                        instant += Long.MAX_VALUE;
                    }
                    InstantaneousInflow = (instant * 100 / (timestamp + INTEVAL - last_timestamp)) / 100.0;
                }
                InstantaneousInflowTimestamp = timestamp + INTEVAL;
                InstantaneousInflowLastData = TotalNetwrokInflow;
            }
        }
        synchronized (NetWorkOutflowLocker) {
            long last_timestamp = InstantaneousOutflowTimestamp;
            if (last_timestamp < timestamp) {
                if (last_timestamp > 0) {
                    long instant = (TotalNetwrokOutflow - InstantaneousOutflowLastData);
                    if (instant < 0) {
                        instant += Long.MAX_VALUE;
                    }
                    InstantaneousOutflow = (instant * 100 / (timestamp + INTEVAL - last_timestamp)) / 100.0;
                }
                InstantaneousOutflowTimestamp = timestamp + INTEVAL;
                InstantaneousOutflowLastData = TotalNetwrokOutflow;
            }
        }
    }
}
