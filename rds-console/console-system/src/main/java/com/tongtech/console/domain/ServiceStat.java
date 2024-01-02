package com.tongtech.console.domain;

import com.tongtech.common.core.domain.BaseEntity;
import com.tongtech.probe.stat.StatRuntime;
import com.tongtech.probe.stat.StatService;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;
import java.util.OptionalDouble;


/**
 * 服务监控信息对象 cnsl_service_stat
 *
 * @author Zhang ChenLong
 * @date 2023-03-18
 */
public class ServiceStat extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 监控信息ID */
    protected Long statId;
    /** 源ID */
    protected Long srcId;
    /** 节点ID */
    protected Long serviceId;
    /** 部署模式， 字典类型 cnsl_deploy_mode */
    protected String deployMode;
    /** 服务名称 */
    protected String name;
    /** 当前连接数 */
    protected Long currentConnections;
    /** 启动以来一共连接次数 */
    protected Long totalConnections;
    /** 当前key总数 */
    protected Long currentKeys;
    /** 当前内存使用量 */
    protected Long memoryUsed;
    /** 当前内存剩余量 */
    protected Long memoryFree;
    /** 实际占用内存总量，JVM当前使用的堆内存总量 */
    protected Long memoryTotal;
    /** 最大可用内存量，JVM -Xmx 的参数值 */
    protected Long memoryAvailable;
    /** 启动以来返回结果的命令数累计 */
    protected Long commandResult;
    /** 网络IO入流量总和 */
    protected Long networkInputBytes;
    /** 网络IO每秒入流量 */
    protected Double inputPerSecond;
    /** 网络IO出流量总和 */
    protected Long networkOutputBytes;
    /** 网络IO每秒出流量 */
    protected Double outputPerSecond;
    /** 当前程序CPU使用率，百分比值 */
    protected Double cpuProcessLoad;
    /** 当前系统CPU使用率, 百分比值 */
    protected Double cpuSystemLoad;
    /** 创建时间秒（从1970 到现在的秒数）*/
    protected Long createSecond;

    public ServiceStat() {}

    public ServiceStat(Long srcId, RdsService serv, StatService statServ) {
        this.srcId = srcId;
        this.serviceId = serv.getServiceId();
        this.deployMode = serv.getDeployMode();
        this.name = serv.getServiceName();

        StatRuntime rt = statServ.getStatistics();
        /* 当前连接数 */
        this.currentConnections = rt.getCurrentConnections();
        /* 启动以来一共连接次数 */
        this.totalConnections = rt.getTotalConnections();
        /* 当前key总数 */
        this.currentKeys = rt.getKeys();
        /* 当前内存使用量 */
        this.memoryUsed = rt.getMemoryUsed();
        /* 当前内存剩余量 */
        this.memoryFree = rt.getMemoryFree();
        /* 实际占用内存总量，JVM当前使用的堆内存总量 */
        this.memoryTotal = rt.getMemoryTotal();
        /* 最大可用内存量，JVM -Xmx 的参数值 */
        this.memoryAvailable = rt.getMemoryAvailable();
        /* 启动以来返回结果的命令数累计 */
        this.commandResult = rt.getCommandResult();
        /* 网络IO入流量总和 */
        this.networkInputBytes = rt.getNetworkInputBytes();
        /* 网络IO每秒入流量 */
        this.inputPerSecond = rt.getInputPerSecond();
        /* 网络IO出流量总和 */
        this.networkOutputBytes = rt.getNetworkOutputBytes();
        /* 网络IO每秒出流量 */
        this.outputPerSecond = rt.getOutputPerSecond();
        /* 当前程序CPU使用率，百分比值 */
        this.cpuProcessLoad = rt.getCpuProcessLoad();
        /* 当前系统CPU使用率, 百分比值 */
        this.cpuSystemLoad = rt.getCpuSystemLoad();
    }


    public ServiceStat(Long srcId, RdsService serv, List<NodeStat> nodeStats) {
        this.srcId = srcId;
        this.serviceId = serv.getServiceId();
        this.deployMode = serv.getDeployMode();
        this.name = serv.getServiceName();

        if(nodeStats != null && nodeStats.size() > 0) {
            /* 当前连接数 */
            this.currentConnections = nodeStats.stream().mapToLong(NodeStat::getCurrentConnections).sum();
            /* 启动以来一共连接次数 */
            this.totalConnections = nodeStats.stream().mapToLong(NodeStat::getTotalConnections).sum();
            /* 当前key总数 */
            this.currentKeys = 0L;
            /* 当前内存使用量 */
            this.memoryUsed = nodeStats.stream().mapToLong(NodeStat::getMemoryUsed).sum();
            /* 当前内存剩余量 */
            this.memoryFree = nodeStats.stream().mapToLong(NodeStat::getMemoryFree).sum();
            /* 实际占用内存总量，JVM当前使用的堆内存总量 */
            this.memoryTotal = nodeStats.stream().mapToLong(NodeStat::getMemoryTotal).sum();
            /* 最大可用内存量，JVM -Xmx 的参数值 */
            this.memoryAvailable = nodeStats.stream().mapToLong(NodeStat::getMemoryAvailable).sum();
            /* 启动以来返回结果的命令数累计 */
            this.commandResult = 0L;
            /* 网络IO入流量总和 */
            this.networkInputBytes = 0L;
            /* 网络IO每秒入流量 */
            this.inputPerSecond = 0D;
            /* 网络IO出流量总和 */
            this.networkOutputBytes = 0L;
            /* 网络IO每秒出流量 */
            this.outputPerSecond = 0D;
            /* 当前程序CPU使用率，百分比值 */
            OptionalDouble optCpuProcessLoad = nodeStats.stream().mapToDouble(NodeStat::getCpuProcessLoad).average();
            this.cpuProcessLoad = optCpuProcessLoad.getAsDouble();
            /* 当前系统CPU使用率, 百分比值 */
            OptionalDouble optCpuSystemLoad = nodeStats.stream().mapToDouble(NodeStat::getCpuSystemLoad).average();
            this.cpuSystemLoad = optCpuSystemLoad.getAsDouble();

        }
    }

    public void setStatId(Long statId)
    {
        this.statId = statId;
    }

    public Long getStatId()
    {
        return statId;
    }
    public void setSrcId(Long srcId)
    {
        this.srcId = srcId;
    }

    public Long getSrcId()
    {
        return srcId;
    }
    public void setServiceId(Long serviceId)
    {
        this.serviceId = serviceId;
    }

    public Long getServiceId()
    {
        return serviceId;
    }
    public void setDeployMode(String deployMode)
    {
        this.deployMode = deployMode;
    }

    public String getDeployMode()
    {
        return deployMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCurrentConnections(Long currentConnections)
    {
        this.currentConnections = currentConnections;
    }

    public Long getCurrentConnections()
    {
        return currentConnections;
    }
    public void setTotalConnections(Long totalConnections)
    {
        this.totalConnections = totalConnections;
    }

    public Long getTotalConnections()
    {
        return totalConnections;
    }
    public void setCurrentKeys(Long currentKeys)
    {
        this.currentKeys = currentKeys;
    }

    public Long getCurrentKeys()
    {
        return currentKeys;
    }
    public void setMemoryUsed(Long memoryUsed)
    {
        this.memoryUsed = memoryUsed;
    }

    public Long getMemoryUsed()
    {
        return memoryUsed;
    }
    public void setMemoryFree(Long memoryFree)
    {
        this.memoryFree = memoryFree;
    }

    public Long getMemoryFree()
    {
        return memoryFree;
    }
    public void setMemoryTotal(Long memoryTotal)
    {
        this.memoryTotal = memoryTotal;
    }

    public Long getMemoryTotal()
    {
        return memoryTotal;
    }
    public void setMemoryAvailable(Long memoryAvailable)
    {
        this.memoryAvailable = memoryAvailable;
    }

    public Long getMemoryAvailable()
    {
        return memoryAvailable;
    }
    public void setCommandResult(Long commandResult)
    {
        this.commandResult = commandResult;
    }

    public Long getCommandResult()
    {
        return commandResult;
    }
    public void setNetworkInputBytes(Long networkInputBytes)
    {
        this.networkInputBytes = networkInputBytes;
    }

    public Long getNetworkInputBytes()
    {
        return networkInputBytes;
    }
    public void setInputPerSecond(Double inputPerSecond)
    {
        this.inputPerSecond = inputPerSecond;
    }

    public Double getInputPerSecond()
    {
        return inputPerSecond;
    }
    public void setNetworkOutputBytes(Long networkOutputBytes)
    {
        this.networkOutputBytes = networkOutputBytes;
    }

    public Long getNetworkOutputBytes()
    {
        return networkOutputBytes;
    }
    public void setOutputPerSecond(Double outputPerSecond)
    {
        this.outputPerSecond = outputPerSecond;
    }

    public Double getOutputPerSecond()
    {
        return outputPerSecond;
    }
    public void setCpuProcessLoad(Double cpuProcessLoad)
    {
        this.cpuProcessLoad = cpuProcessLoad;
    }

    public Double getCpuProcessLoad()
    {
        return cpuProcessLoad;
    }
    public void setCpuSystemLoad(Double cpuSystemLoad)
    {
        this.cpuSystemLoad = cpuSystemLoad;
    }

    public Double getCpuSystemLoad()
    {
        return cpuSystemLoad;
    }

    public Long getCreateSecond() {
        return createSecond;
    }

    public void setCreateSecond(Long createSecond) {
        this.createSecond = createSecond;
    }

    @Override
    public void setCreateTime(Date createTime) {
        super.setCreateTime(createTime);
        this.createSecond = createTime.getTime() / 1000; //同时更新创建秒
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("statId", getStatId())
            .append("srcId", getSrcId())
            .append("serviceId", getServiceId())
            .append("deployMode", getDeployMode())
            .append("name", name)
            .append("currentConnections", getCurrentConnections())
            .append("totalConnections", getTotalConnections())
            .append("currentKeys", getCurrentKeys())
            .append("memoryUsed", getMemoryUsed())
            .append("memoryFree", getMemoryFree())
            .append("memoryTotal", getMemoryTotal())
            .append("memoryAvailable", getMemoryAvailable())
            .append("commandResult", getCommandResult())
            .append("networkInputBytes", getNetworkInputBytes())
            .append("inputPerSecond", getInputPerSecond())
            .append("networkOutputBytes", getNetworkOutputBytes())
            .append("outputPerSecond", getOutputPerSecond())
            .append("cpuProcessLoad", getCpuProcessLoad())
            .append("cpuSystemLoad", getCpuSystemLoad())
            .append("createTime", getCreateTime())
            .toString();
    }

}
