package com.tongtech.proxy.core.utils;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;

public class CpuUtil {

    public static int systemLoad() {
        try {
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osmxb.getSystemCpuLoad();
            int percentCpuLoad = (int) (cpuLoad * 100);
            return percentCpuLoad;
        } catch (Throwable t) {
        }
        return -1;
    }

    public static int processLoad() {
        try {
            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osmxb.getProcessCpuLoad();
            int percentCpuLoad = (int) (cpuLoad * 100);
            return percentCpuLoad;
        } catch (Throwable t) {
        }
        return -1;
    }

    private static Object LockerCpu = new Object();
    private static long TimestampLoadCpu;
    private static int PhysicalCpus;

    public static int availableProcessors() {
        synchronized (LockerCpu) {
            if (System.currentTimeMillis() - TimestampLoadCpu > 600_000) {
                TimestampLoadCpu = System.currentTimeMillis();
                try {
                    OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                    PhysicalCpus = osmxb.getAvailableProcessors();
                } catch (Throwable t) {
                }
            }
        }
        return PhysicalCpus;
    }
//    public static int availableProcessors() {
//        try {
//            OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//            return osmxb.getAvailableProcessors();
//        } catch (Throwable t) {
//        }
//        return -1;
//    }
}
