package com.tongtech.probe.pojo;

import com.alibaba.fastjson2.JSON;

import java.util.List;
import java.util.Map;

public class StatisticsPojo extends Statistics {
    // 实例标识名
    private String id;

    // 版本号
    private String version;

    // 节点是否活着
    private boolean alive;

    // jvm已经分配的内存
    private Long jvmAllocated;

    // jvm已经分配但空闲的内存
    private Long jvmFree;

    // jvm可分配的最大内存
    private Long jvmMax;

    // jvm占用的总内存
    private Long memoryTotal;

    // 储存数据占用的内存
    private Long usedMemory;

    // cfg配置文件中配置的内存大小
    private Long memoryStatic;

    // 动态分配的超长数据的总内存大小
    private Long memoryDynamic;

    // 动态分配的超长数据的总个数
    private Long sizeDynamic;

    // 系统可用内存
    private Long totalPhysicalMemory;

    // 内存使用率
    private Double memoryUsedRatio;

    // 当前接入的client连接数量
    private Long currentConnections;

    // 可接入的client的最大连接数量
    private Long maxConnections;

    // 当前接入的client数量和最大接入数量的比值
    private Double connectedRatio;

    // 当前接入的client数量于启动以来接入的总数量（包括已经关闭的连接）的比值
    private Double ConnectionRatio;

    // 自启动以来创建过的客户端连接总数
    private Long totalConnections;

    // 客户端连接使用率
    private Double connectionsRatio;

    // 当前每秒钟处理请求量
    private Long processSecond;

    // 最近一分钟处理请求量
    private Long processMinute;

    // 集群状态，list中的每1项（String数组）代表一个cluster的分片
    // String数组为定长，各项内容分别为：分片主节点名称、主节点地址、主节点状态、分片状态（success 或 fail）、分配槽位号列表
    private List<String[]> clusters;

    // 各表中数据量
    private Map<String, Integer> keys;

    // 授权的总内存使用量，只有Center节点有此配置
    private Long totalLicense;

    // 已经占用的license授权内存量，只有Center节点有此
    private Long usedLicense;

    // license过期时间
    private Long licenseExpired;

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    public synchronized String getVersion() {
        return version;
    }

    public synchronized void setVersion(String version) {
        this.version = version;
    }

    public synchronized Long getMemoryTotal() {
        return memoryTotal;
    }

    public synchronized void setMemoryTotal(Long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public synchronized Long getUsedMemory() {
        return usedMemory;
    }

    public synchronized void setUsedMemory(Long usedMemory) {
        this.usedMemory = usedMemory;
    }

    public synchronized Long getMemoryStatic() {
        return memoryStatic;
    }

    public synchronized void setMemoryStatic(Long memoryStatic) {
        this.memoryStatic = memoryStatic;
    }

    public synchronized Long getMemoryDynamic() {
        return memoryDynamic;
    }

    public synchronized void setMemoryDynamic(Long memoryDynamic) {
        this.memoryDynamic = memoryDynamic;
    }

    public synchronized Long getSizeDynamic() {
        return sizeDynamic;
    }

    public synchronized void setSizeDynamic(Long sizeDynamic) {
        this.sizeDynamic = sizeDynamic;
    }

    public synchronized Long getTotalPhysicalMemory() {
        return totalPhysicalMemory;
    }

    public synchronized void setTotalPhysicalMemory(Long totalPhysicalMemory) {
        this.totalPhysicalMemory = totalPhysicalMemory;
    }

    public synchronized Double getMemoryUsedRatio() {
        return memoryUsedRatio;
    }

    public synchronized void setMemoryUsedRatio(Double memoryUsedRatio) {
        this.memoryUsedRatio = memoryUsedRatio;
    }

    public synchronized Long getJvmAllocated() {
        return jvmAllocated;
    }

    public synchronized void setJvmAllocated(Long jvmAllocated) {
        this.jvmAllocated = jvmAllocated;
    }

    public synchronized Long getJvmFree() {
        return jvmFree;
    }

    public synchronized void setJvmFree(Long jvmFree) {
        this.jvmFree = jvmFree;
    }

    public synchronized Long getJvmMax() {
        return jvmMax;
    }

    public synchronized void setJvmMax(Long jvmMax) {
        this.jvmMax = jvmMax;
    }

    public synchronized Long getCurrentConnections() {
        return currentConnections;
    }

    public synchronized void setCurrentConnections(Long currentConnections) {
        this.currentConnections = currentConnections;
    }

    public synchronized Long getMaxConnections() {
        return maxConnections;
    }

    public synchronized void setMaxConnections(Long maxConnections) {
        this.maxConnections = maxConnections;
    }

    public synchronized Double getConnectedRatio() {
        return connectedRatio;
    }

    public synchronized void setConnectedRatio(Double connectedRatio) {
        this.connectedRatio = connectedRatio;
    }

    public Double getConnectionRatio() {
        return ConnectionRatio;
    }

    public void setConnectionRatio(Double connectionRatio) {
        ConnectionRatio = connectionRatio;
    }

    public synchronized Long getTotalConnections() {
        return totalConnections;
    }

    public synchronized void setTotalConnections(Long totalConnections) {
        this.totalConnections = totalConnections;
    }

    public synchronized Double getConnectionsRatio() {
        return connectionsRatio;
    }

    public synchronized void setConnectionsRatio(Double connectionsRatio) {
        this.connectionsRatio = connectionsRatio;
    }

    public synchronized Long getProcessSecond() {
        return processSecond;
    }

    public synchronized void setProcessSecond(Long processSecond) {
        this.processSecond = processSecond;
    }

    public synchronized Long getProcessMinute() {
        return processMinute;
    }

    public synchronized void setProcessMinute(Long processMinute) {
        this.processMinute = processMinute;
    }

    public synchronized List<String[]> getClusters() {
        return clusters;
    }

    public synchronized void setClusters(List<String[]> clusters) {
        this.clusters = clusters;
    }

    public synchronized Map<String, Integer> getKeys() {
        return keys;
    }

    public synchronized void setKeys(Map<String, Integer> keys) {
        this.keys = keys;
    }

    public synchronized Long getTotalLicense() {
        return totalLicense;
    }

    public synchronized void setTotalLicense(Long totalLicense) {
        this.totalLicense = totalLicense;
    }

    public synchronized Long getUsedLicense() {
        return usedLicense;
    }

    public synchronized void setUsedLicense(Long usedLicense) {
        this.usedLicense = usedLicense;
    }

    public Long getLicenseExpired() {
        return licenseExpired;
    }

    public void setLicenseExpired(Long licenseExpired) {
        this.licenseExpired = licenseExpired;
    }

    @Override
    public String toString() {
//        if (isAlive()) {
            return JSON.toJSONString(this);
//        } else {
//            StringBuilder buf = new StringBuilder();
//            buf.append("{\"nodeName\":\"").append(getName())
//                    .append("\",\"nodeType\":\"").append(getType())
//                    .append("\",\"alive\":").append("false}");
//            return buf.toString();
//        }
    }
}
