package com.tongtech.console.domain.vo;

import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.tongtech.console.enums.NodeStatusEnum.*;

public class ServiceNodeStatVo {

    /** 节点ID */
    private Long serviceId;
    /** 部署模式， 字典类型 cnsl_deploy_mode */
    private String deployMode;
    /** 服务名称 */
    private String name;

    private String status;

    private List<NodeVo> nodes;

    public ServiceNodeStatVo(RdsService rdsService, List<RdsNode> rdsNodes, List<NodeStat> nodeStats, DateTimeFormatter timeFormatter) {
        this.serviceId = rdsService.getServiceId();
        this.deployMode = rdsService.getDeployMode();
        this.name = rdsService.getServiceName();
        init(rdsNodes, nodeStats, timeFormatter);
    }

    private void init(List<RdsNode> rdsNodes, List<NodeStat> nodeStats, DateTimeFormatter timeFormatter) {
        nodes = new ArrayList<>();
        Map<Long, NodeVo> nodeVoMap = new LinkedHashMap<>();
        boolean hasStart = false;
        boolean hasStop = false;
        for (RdsNode rdsNode : rdsNodes) {
            NodeVo nodeVo = new NodeVo(rdsNode);
            nodeVoMap.put(rdsNode.getNodeId(),nodeVo);
            if (STOP.getName().equals(rdsNode.getNodeStatus()) || STOP.name().equals(rdsNode.getNodeStatus())) {
                hasStop = true;
            }
            if (START.getName().equals(rdsNode.getNodeStatus()) || START.name().equals(rdsNode.getNodeStatus())) {
                hasStart = true;
            }
            nodes.add(nodeVo);
        }
        if (hasStart) {
            if (hasStop) {
                this.status = START_PART.getName();
            } else {
                this.status = START.getName();
            }
        } else {
            this.status = STOP.getName();
        }
        for (NodeStat nodeStat : nodeStats) {
            Long nodeId = nodeStat.getNodeId();
            if (nodeVoMap.containsKey(nodeId)) {
                NodeVo nodeVo = nodeVoMap.get(nodeId);
                nodeVo.getStats().add(new NodeStatVo(nodeStat, timeFormatter));
            }
        }
    }

    static class NodeVo {
        /** 节点ID */
        private Long nodeId;

        private String instance;

        private List<NodeStatVo> stats = new ArrayList<>();

        NodeVo(RdsNode rdsNode) {
            this.nodeId = rdsNode.getNodeId();
            this.instance = rdsNode.getInstance();
        }

        public Long getNodeId() {
            return nodeId;
        }

        public String getInstance() {
            return instance;
        }

        public List<NodeStatVo> getStats() {
            return stats;
        }
    }

    static class NodeStatVo {
        /** 当前连接数 */
        private Long currentConnections;
        /** 当前key总数 */
        private Long currentKeys;
        /** 当前内存使用量 */
        private Long memoryUsed;
        /** 当前内存剩余量 */
        private Long memoryFree;
        /** 实际占用内存总量 */
        private Long memoryTotal;
        /** 最大可用内存量 */
        private Long memoryAvailable;
        /** 网络IO每秒入流量 */
        private Double inputPerSecond;
        /** 网络IO每秒出流量 */
        private Double outputPerSecond;
        /** 当前程序CPU使用率 */
        private Double cpuProcessLoad;
        /** 当前系统CPU使用率 */
        private Double cpuSystemLoad;

        private String time;

        NodeStatVo(NodeStat nodeStat, DateTimeFormatter timeFormatter) {
            this.currentConnections = nodeStat.getCurrentConnections();
            this.currentKeys = nodeStat.getCurrentKeys();
            this.memoryUsed = nodeStat.getMemoryUsed();
            this.memoryFree = nodeStat.getMemoryFree();
            this.memoryTotal = nodeStat.getMemoryTotal();
            this.memoryAvailable = nodeStat.getMemoryAvailable();
            this.inputPerSecond = nodeStat.getInputPerSecond();
            this.outputPerSecond = nodeStat.getOutputPerSecond();
            this.cpuProcessLoad = nodeStat.getCpuProcessLoad();
            this.cpuSystemLoad = nodeStat.getCpuSystemLoad();
            this.time = LocalDateTime.ofEpochSecond(nodeStat.getCreateSecond(), 0, ZoneOffset.ofHours(8)).format(timeFormatter);
        }

        public Long getCurrentConnections() {
            return currentConnections;
        }

        public Long getCurrentKeys() {
            return currentKeys;
        }

        public Long getMemoryUsed() {
            return memoryUsed;
        }

        public Long getMemoryFree() {
            return memoryFree;
        }

        public Long getMemoryTotal() {
            return memoryTotal;
        }

        public Long getMemoryAvailable() {
            return memoryAvailable;
        }

        public Double getInputPerSecond() {
            return inputPerSecond;
        }

        public Double getOutputPerSecond() {
            return outputPerSecond;
        }

        public Double getCpuProcessLoad() {
            return cpuProcessLoad;
        }

        public Double getCpuSystemLoad() {
            return cpuSystemLoad;
        }

        public String getTime() {
            return time;
        }
    }

    public Long getServiceId() {
        return serviceId;
    }

    public String getDeployMode() {
        return deployMode;
    }

    public String getName() {
        return name;
    }

    public List<NodeVo> getNodes() {
        return nodes;
    }

    public String getStatus() {
        return status;
    }
}
