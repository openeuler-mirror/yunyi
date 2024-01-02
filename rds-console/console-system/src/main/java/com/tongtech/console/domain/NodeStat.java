package com.tongtech.console.domain;

import com.tongtech.probe.stat.StatBaseNode;
import com.tongtech.probe.stat.StatRuntime;
import com.tongtech.probe.stat.StatWorkerNode;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.core.domain.BaseEntity;

import java.util.Date;

import static com.tongtech.console.enums.NodeStatusEnum.START;
import static com.tongtech.console.enums.NodeStatusEnum.STOP;

/**
 * 节点监控信息对象 cnsl_node_stat
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
public class NodeStat extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 监控信息ID */
    private Long statId;
    /** 源ID */
    private Long srcId;
    /** 节点ID */
    private Long nodeId;
    /** 服务ID */
    private Long serviceId;
    /** 节点名称 */
    private String name;
    /** 节点类型 */
    private String nodeType;
    /** 实例名 */
    private String instance;
    /** 是否过期 */
    private Boolean expired;
    /** 启动以来运行了多少秒 */
    private Long running;
    /** 当前连接数 */
    private Long currentConnections = 0L;
    /** 启动以来一共连接次数 */
    private Long totalConnections = 0L;
    /** 当前key总数 */
    private Long currentKeys = 0L;
    /** 当前内存使用量 */
    private Long memoryUsed = 0L;
    /** 当前内存剩余量 */
    private Long memoryFree = 0L;
    /** 实际占用内存总量 */
    private Long memoryTotal = 0L;
    /** 最大可用内存量 */
    private Long memoryAvailable = 0L;
    /** 启动以来返回结果的命令数累计 */
    private Long commandResult = 0L;
    /** 网络IO入流量总和 */
    private Long networkInputBytes = 0L;
    /** 网络IO每秒入流量 */
    private Double inputPerSecond = 0D;
    /** 网络IO出流量总和 */
    private Long networkOutputBytes = 0L;
    /** 网络IO每秒出流量 */
    private Double outputPerSecond = 0D;
    /** 当前程序CPU使用率 */
    private Double cpuProcessLoad = 0D;
    /** 当前系统CPU使用率 */
    private Double cpuSystemLoad = 0D;
    /** 每秒平均流量-前10秒内 */
    private Double throughputAverage10 = 0D;
    /** 每秒平均流量-前60秒内 */
    private Double throughputAverage60 = 0D;
    /** 显示专用属性, 节点状态，stop start */
    private String status;
    /** 创建时间秒（从1970 到现在的秒数）*/
    protected Long createSecond;

    public NodeStat() {

    }

    public NodeStat(Long srcId, Long serviceId) {
        this.srcId = srcId;
        this.serviceId = serviceId;
    }

    public NodeStat(Long srcId, RdsNode node, StatBaseNode statNode) {
        this.srcId = srcId;
        this.nodeId = node.getNodeId();
        this.serviceId = node.getServiceId();
        this.name = node.getNodeName();
        this.nodeType = node.getNodeType();
        this.instance = statNode.getInstance();
        this.setExpired(statNode.getExpired());
        StatRuntime rt = statNode.getRuntime();
        this.running = rt.getRunning();
        this.currentConnections = rt.getCurrentConnections();
        this.totalConnections = rt.getTotalConnections();
        this.currentKeys = rt.getKeys();
        this.memoryUsed = rt.getMemoryUsed();
        this.memoryFree = rt.getMemoryFree();
        this.memoryTotal = rt.getMemoryTotal();
        this.memoryAvailable = rt.getMemoryAvailable();
        this.commandResult = rt.getCommandResult();
        this.networkInputBytes = rt.getNetworkInputBytes();
        this.inputPerSecond = rt.getInputPerSecond();
        this.networkOutputBytes = rt.getNetworkOutputBytes();
        this.outputPerSecond = rt.getOutputPerSecond();
        this.cpuProcessLoad = rt.getCpuProcessLoad();
        this.cpuSystemLoad = rt.getCpuSystemLoad();

        if(statNode instanceof StatWorkerNode) {
            StatWorkerNode  worker = (StatWorkerNode)statNode;
            this.throughputAverage10 = worker.getThroughput().getAverage10();
            this.throughputAverage60 = worker.getThroughput().getAverage60();
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
    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getNodeId()
    {
        return nodeId;
    }
    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    public Long getSrcId() {
        return srcId;
    }

    public void setSrcId(Long srcId) {
        this.srcId = srcId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getNodeType()
    {
        return nodeType;
    }
    public void setExpired(Boolean expired)
    {
        this.expired = expired;
        this.status = (this.expired) ? STOP.getName() : START.getName();
    }

    public Boolean getExpired()
    {
        return expired;
    }
    public void setRunning(Long running)
    {
        this.running = running;
    }

    public Long getRunning()
    {
        return running;
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

    public void setNetworkOutputBytes(Long networkOutputBytes)
    {
        this.networkOutputBytes = networkOutputBytes;
    }

    public Long getNetworkOutputBytes()
    {
        return networkOutputBytes;
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

    public Double getThroughputAverage10() {
        return throughputAverage10;
    }

    public void setThroughputAverage10(Double throughputAverage10) {
        this.throughputAverage10 = throughputAverage10;
    }

    public Double getThroughputAverage60() {
        return throughputAverage60;
    }

    public void setThroughputAverage60(Double throughputAverage60) {
        this.throughputAverage60 = throughputAverage60;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
            .append("nodeId", getNodeId())
                .append("serviceId", getServiceId())
                .append("name", getName())
            .append("instance", getInstance())
            .append("nodeType", getNodeType())
            .append("expired", getExpired())
            .append("running", getRunning())
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
            .append("throughputAverage10", getThroughputAverage10())
            .append("throughputAverage60", getThroughputAverage60())
            .append("createTime", getCreateTime())
            .toString();
    }
}
