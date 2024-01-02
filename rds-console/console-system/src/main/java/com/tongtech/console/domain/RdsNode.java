package com.tongtech.console.domain;

import com.tongtech.console.enums.NodeStatusEnum;
import com.tongtech.console.enums.NodeTypeEnum;
import com.tongtech.probe.stat.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

import java.util.List;

import static com.tongtech.common.utils.CompareUtils.notEquals;

/**
 * 节点信息对象 cnsl_rds_node
 *
 * @author Zhang ChenLong
 * @date 2023-01-24
 */
public class RdsNode extends BaseEntity implements Comparable<RdsNode>
{
    private static final long serialVersionUID = 1L;

    /** 节点ID */
    protected Long nodeId;

    /** 节点管理器ID */
    protected Long managerId;

    /**
     * 节点管理器名称（查询显示用）
     */
    protected String managerName;

    /** 服务ID */
    protected Long serviceId;

    /** 节点名称 */
    @Excel(name = "节点名称")
    protected String nodeName;

    protected String instance;

    /** 节点类型 */
    @Excel(name = "节点类型")
    protected String nodeType;

    /** 节点地址  */
    @Excel(name = "节点地址")
    protected String hostAddress;

    /** 节点状态 */
    @Excel(name = "节点状态")
    protected String nodeStatus;

    protected NodeStatusEnum nodeStatusEnum;

    /** RDS端口 */
    protected Integer servicePort;

    /** redis端口 */
    protected Integer redisPort;

    /** center中的管理端口 */
    protected Integer adminPort;

    /** 主节点 */
    protected boolean masterNode;

    /** 是否为热备节点 */
    protected boolean hotSpares;

    /** 分片的插槽范围 */
    protected String slot;

    /** 分片的编号 */
    protected Integer shard;

    /**
     * 前端传入的变化的属性名称列表（主要对比提交前后的变化属性）
     */
    private List<String> changedProps;

    public RdsNode() {
    }

    public RdsNode(Long serviceId, StatBaseNode statNode) {
        this.serviceId = serviceId;
        this.managerId = null;
        this.instance = statNode.getInstance();
        this.nodeName = statNode.getInstance();
        this.hostAddress = statNode.getRemote();
        this.servicePort = statNode.getPort();

        if(statNode.getExpired()) {
            this.nodeStatus = NodeStatusEnum.STOP.getName();
        }
        else {
            this.nodeStatus = NodeStatusEnum.START.getName();
        }

        if(statNode instanceof StatCenterNode) {
            this.nodeType = NodeTypeEnum.CENTER.getName();
        }
        else if(statNode instanceof StatSentinelNode) {
            this.nodeType = NodeTypeEnum.SENTINEL.getName();
            StatSentinelNode ssNode = (StatSentinelNode)statNode;
        }
        else if(statNode instanceof StatWorkerNode) {
            StatWorkerNode swNode = ((StatWorkerNode)statNode);
            this.nodeType = NodeTypeEnum.WORKER.getName();
            this.redisPort = swNode.getRedisPort();
            this.masterNode = swNode.getMaster();
            this.hotSpares =  swNode.isHotSpares();
            this.shard = swNode.getShard();
            this.slot = swNode.getSlot();
        }
        else if(statNode instanceof StatProxyNode) {
            this.nodeType = NodeTypeEnum.PROXY.getName();
            this.redisPort = ((StatProxyNode)statNode).getRedisPort();
        }
        else {
            throw new RuntimeException("Unexpect node type class:" + statNode.getClass());
        }

    }

    public RdsNode(String hostAddress, Integer servicePort) {
        this.hostAddress = hostAddress;
        this.servicePort = servicePort;
    }

    public RdsNode(String hostAddress, Integer servicePort, Integer adminPort) {
        this.hostAddress = hostAddress;
        this.servicePort = servicePort;
        this.adminPort = adminPort;
    }

    public RdsNode(String hostAddress, NodeTypeEnum nodeType, Integer servicePort, Integer redisPort, Integer shard, String slot, boolean masterNode) {
        this.hostAddress = hostAddress;
        this.nodeType = nodeType.getName();
        this.servicePort = servicePort;
        this.redisPort = redisPort;
        this.shard = shard;
        this.slot = slot;
        this.masterNode = masterNode;
    }

    public RdsNode(String instance, String hostAddress, NodeTypeEnum nodeType, Integer servicePort, Integer redisPort, boolean masterNode) {
        this.instance = instance;
        this.hostAddress = hostAddress;
        this.nodeType = nodeType.getName();
        this.servicePort = servicePort;
        this.redisPort = redisPort;
        this.masterNode = masterNode;
    }

    /**
     * 从另外一个RdsNode对象中获得配置属性和状态属性
     */
    public void setFrom(RdsNode other) {
        this.hostAddress = other.hostAddress;
        this.servicePort = other.servicePort;
        this.redisPort = other.redisPort;
        this.masterNode = other.masterNode;
        this.hotSpares = other.hotSpares;
        this.shard = other.shard;
        this.slot = other.slot;
        this.nodeStatus = other.nodeStatus;
    }

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getNodeId()
    {
        return nodeId;
    }
    public void setManagerId(Long managerId)
    {
        this.managerId = managerId;
    }

    public Long getManagerId()
    {
        return managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public void setServiceId(Long serviceId)
    {
        this.serviceId = serviceId;
    }

    public Long getServiceId()
    {
        return serviceId;
    }
    public void setNodeName(String nodeName)
    {
        this.nodeName = nodeName;
    }

    public String getNodeName()
    {
        return nodeName;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public NodeTypeEnum getNodeTypeEnum() {
        return NodeTypeEnum.parse(this.nodeType);
    }

    public NodeStatusEnum getNodeStatusEnum() {
        if(nodeStatusEnum == null && nodeStatus != null) {
            this.nodeStatusEnum = NodeStatusEnum.parse(this.nodeStatus);
        }
        return this.nodeStatusEnum;
    }

    /**
     * 返回形如：hostAddress:servicePort 格式的地址
     * @return
     */
    public String getServiceEndpoint() {
        if(this.hostAddress != null && this.servicePort != null) {
            StringBuilder buf = new StringBuilder(this.hostAddress.length() + 9);
            buf.append(this.hostAddress).append(':').append(this.servicePort);
            return buf.toString();
        }
        else {
            return "";
        }
    }

    /**
     * 返回形如：hostAddress:redisPort 格式的地址
     * @return
     */
    public String getRedisEndpoint() {
        if(this.hostAddress != null && this.redisPort != null) {
            StringBuilder buf = new StringBuilder(this.hostAddress.length() + 9);
            buf.append(this.hostAddress).append(':').append(this.redisPort);
            return buf.toString();
        }
        else {
            return "";
        }
    }



    public void setHostAddress(String hostAddress)
    {
        this.hostAddress = hostAddress;
    }

    public String getHostAddress()
    {
        return hostAddress;
    }
    public void setNodeStatus(String nodeStatus)
    {
        this.nodeStatus = nodeStatus;
    }

    public String getNodeStatus()
    {
        return nodeStatus;
    }

    public Integer getServicePort() {
        return servicePort;
    }

    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(Integer adminPort) {
        this.adminPort = adminPort;
    }

    public boolean isMasterNode() {
        return masterNode;
    }

    public void setMasterNode(boolean masterNode) {
        this.masterNode = masterNode;
    }

    public boolean isHotSpares() {
        return hotSpares;
    }

    public void setHotSpares(boolean hotSpares) {
        this.hotSpares = hotSpares;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Integer getShard() {
        return shard;
    }

    public void setShard(Integer shard) {
        this.shard = shard;
    }

    public List<String> getChangedProps() {
        return changedProps;
    }

    public void setChangedProps(List<String> changedProps) {
        this.changedProps = changedProps;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("nodeId", getNodeId())
            .append("managerId", getManagerId())
            .append("managerName", getManagerName())
            .append("serviceId", getServiceId())
            .append("nodeName", getNodeName())
            .append("nodeType", getNodeType())
            .append("hostAddress", getHostAddress())
            .append("servicePort", getServicePort())
            .append("redisPort", getRedisPort())
            .append("adminPort", getAdminPort())
            .append("masterNode", isMasterNode())
            .append("hostSpares", isHotSpares())
            .append("slot", getSlot())
            .append("shard", getShard())
            .append("nodeStatus", getNodeStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }

    /**
     * 按 nodeType , nodeName  顺序进行排序
     * @param o the object to be compared.
     * @return
     */
    @Override
    public int compareTo(RdsNode o) {
        int ret = this.nodeType.compareTo(o.getNodeType());
        if(ret == 0) {
            return this.nodeName.compareTo(o.getNodeName());
        }
        else {
            return ret;
        }
    }
}
