package com.tongtech.proxy.jmx;

public interface ProxyMBean {

    String getId();

    String getVersion();

    /**
     * 通过创建socket并发送ping命令的方式探测Redis仿真端口工作是否正常
     *
     * @return
     */
    boolean getPing();

    // 进程占用的总内存
    long getMemoryTotal();

    long getJvmAllocated();

    long getJvmFree();

    long getJvmMax();

    // 系统物理内存
    long getTotalPhysicalMemory();

    // 取当前客户端的连接数
    long getClientCurrentConnections();

    // 可同时保持的最大连接数
    long getMaxConnections();

    // 当前连接数和最大连接数的比率
    double getConnectedRatio();

    // 取自启动以来创建的总的连接的数量
    long getClientTotalConnections();

    // 创建连接的使用率
    double getConnectionsRatio();

    // 当前每秒处理
    long getProcessSecond();

    // 当前每分钟处理能力
    long getProcessMinute();
}
