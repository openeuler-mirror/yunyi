package com.tongtech.probe.stat;

import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;

/**
 * 监控的各指标信息
 */
public class StatRuntime implements Serializable {
    private static final long serialVersionUID = 1L;
    /* 启动以来运行了多少秒 */
    private Long running;
    /* 当前连接数 */
    private Long currentConnections;
    /* 启动以来一共连接次数 */
    private Long totalConnections;
    /* 当前key总数 */
    private Long keys;
    /* 当前内存使用量 */
    private Long memoryUsed;
    /* 当前内存剩余量  */
    private Long memoryFree;
    /* 实际占用内存总量，JVM当前使用的堆内存总量 */
    private Long memoryTotal;
    /* JVM  MAX 的值 */
    private Long memoryAvailable;
    /* 启动以来返回结果的命令数累计 */
    private Long commandResult;
    /*  */
    private Long networkInputBytes;
    /*  */
    @JSONField(name="inputPerSecond(KB/s)")
    private Double inputPerSecond;
    /*  */
    private Long networkOutputBytes;
    /*  */
    @JSONField(name="outputPerSecond(KB/s)")
    private Double outputPerSecond;
    /* 当前程序CPU使用率 */
    @JSONField(name="cpuProcessLoad(%)")
    private Double cpuProcessLoad;
    /* 当前系统CPU使用率 */
    @JSONField(name="cpuSystemLoad(%)")
    private Double cpuSystemLoad;

    public Long getRunning() {
        return running;
    }

    public void setRunning(Long running) {
        this.running = running;
    }

    public Long getCurrentConnections() {
        return currentConnections;
    }

    public void setCurrentConnections(Long currentConnections) {
        this.currentConnections = currentConnections;
    }

    public Long getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(Long totalConnections) {
        this.totalConnections = totalConnections;
    }

    public Long getKeys() {
        return keys;
    }

    public void setKeys(Long keys) {
        this.keys = keys;
    }

    public Long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public Long getMemoryFree() {
        return memoryFree;
    }

    public void setMemoryFree(Long memoryFree) {
        this.memoryFree = memoryFree;
    }

    public Long getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(Long memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public Long getMemoryAvailable() {
        return memoryAvailable;
    }

    public void setMemoryAvailable(Long memoryAvailable) {
        this.memoryAvailable = memoryAvailable;
    }

    public Long getCommandResult() {
        return commandResult;
    }

    public void setCommandResult(Long commandResult) {
        this.commandResult = commandResult;
    }

    public Long getNetworkInputBytes() {
        return networkInputBytes;
    }

    public void setNetworkInputBytes(Long networkInputBytes) {
        this.networkInputBytes = networkInputBytes;
    }

    public Long getNetworkOutputBytes() {
        return networkOutputBytes;
    }

    public void setNetworkOutputBytes(Long networkOutputBytes) {
        this.networkOutputBytes = networkOutputBytes;
    }

    public Double getInputPerSecond() {
        return inputPerSecond;
    }

    public void setInputPerSecond(Double inputPerSecond) {
        this.inputPerSecond = inputPerSecond;
    }

    public Double getOutputPerSecond() {
        return outputPerSecond;
    }

    public void setOutputPerSecond(Double outputPerSecond) {
        this.outputPerSecond = outputPerSecond;
    }

    public Double getCpuProcessLoad() {
        return cpuProcessLoad;
    }

    public void setCpuProcessLoad(Double cpuProcessLoad) {
        this.cpuProcessLoad = cpuProcessLoad;
    }

    public Double getCpuSystemLoad() {
        return cpuSystemLoad;
    }

    public void setCpuSystemLoad(Double cpuSystemLoad) {
        this.cpuSystemLoad = cpuSystemLoad;
    }

    @Override
    public String toString() {
        return "StatRuntime{" +
                "running=" + running +
                ", currentConnections=" + currentConnections +
                ", totalConnections=" + totalConnections +
                ", keys=" + keys +
                ", memoryUsed=" + memoryUsed +
                ", memoryFree=" + memoryFree +
                ", memoryTotal=" + memoryTotal +
                ", memoryAvailable=" + memoryAvailable +
                ", commandResult=" + commandResult +
                ", networkInputBytes=" + networkInputBytes +
                ", inputPerSecond=" + inputPerSecond +
                ", networkOutputBytes=" + networkOutputBytes +
                ", outputPerSecond=" + outputPerSecond +
                ", cpuProcessLoad=" + cpuProcessLoad +
                ", cpuSystemLoad=" + cpuSystemLoad +
                '}';
    }
}
