package com.tongtech.console.domain.vo;

import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.domain.RdsNode;

import java.util.List;

/**
 * 节点信息对象 cnsl_rds_node
 *
 * @author Zhang ChenLong
 * @date 2023-01-24
 */
public class RdsNodeStatsVo extends RdsNode
{
    private static final long serialVersionUID = 1L;

    private List<NodeStat> stats;

    public RdsNodeStatsVo(RdsNode node) {
        this.nodeId = node.getNodeId();
        this.managerId = node.getManagerId();
        this.managerName = node.getManagerName();
        this.serviceId = node.getServiceId();
        this.nodeName = node.getNodeName();
        this.instance = node.getInstance();
        this.nodeType = node.getNodeType();
        this.hostAddress = node.getHostAddress();
        this.nodeStatus = node.getNodeStatus();
        this.nodeStatusEnum = node.getNodeStatusEnum();
        this.servicePort = node.getServicePort();
        this.redisPort = node.getRedisPort();
        this.adminPort = node.getAdminPort();
        this.masterNode = node.isMasterNode();
        this.hotSpares = node.isHotSpares();
        this.slot = node.getSlot();
        this.shard = node.getShard();
    }

    /**
     * 为了界面显示使用，和nodeStatus属性一致。
     * @return
     */
    public String getStatus() {
        return this.nodeStatus;
    }

    public List<NodeStat> getStats() {
        return stats;
    }

    public void setStats(List<NodeStat> stats) {
        this.stats = stats;
    }
}
