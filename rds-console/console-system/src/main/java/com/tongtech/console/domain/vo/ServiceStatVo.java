package com.tongtech.console.domain.vo;

import com.tongtech.common.utils.DateUtils;
import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.domain.ServiceStat;
import java.util.List;

import static com.tongtech.console.enums.NodeStatusEnum.*;

public class ServiceStatVo extends ServiceStat {

    private List<NodeStat> children; //子节点统计信息

    private List<RdsNodeStatsVo> nodes; //子节点，和子节点下的统计 RdsNodeStatsVo.nodeStats

    private String status; //状态

    public ServiceStatVo() {}

    public ServiceStatVo(RdsService serv) {
        this.serviceId = serv.getServiceId();
        this.deployMode = serv.getDeployMode();
        this.name = serv.getServiceName();
        this.setCreateTime(DateUtils.getNowDate());
    }

    public ServiceStatVo(ServiceStat orgStat) {
        this.statId = orgStat.getStatId();
        this.srcId = orgStat.getSrcId();
        this.serviceId = orgStat.getServiceId();
        this.deployMode = orgStat.getDeployMode();
        this.name = orgStat.getName();
        this.currentConnections = orgStat.getCurrentConnections();
        this.totalConnections = orgStat.getTotalConnections();
        this.currentKeys = orgStat.getCurrentKeys();
        this.memoryUsed = orgStat.getMemoryUsed();
        this.memoryFree = orgStat.getMemoryFree();
        this.memoryTotal = orgStat.getMemoryTotal();
        this.memoryAvailable = orgStat.getMemoryAvailable();
        this.commandResult = orgStat.getCommandResult();
        this.networkInputBytes = orgStat.getNetworkInputBytes();
        this.inputPerSecond = orgStat.getInputPerSecond();
        this.networkOutputBytes = orgStat.getNetworkOutputBytes();
        this.outputPerSecond = orgStat.getOutputPerSecond();
        this.cpuProcessLoad = orgStat.getCpuProcessLoad();
        this.cpuSystemLoad = orgStat.getCpuSystemLoad();
        this.setCreateTime(orgStat.getCreateTime());
    }

    public List<NodeStat> getChildren() {
        return children;
    }

    public void setChildren(List<NodeStat> children) {
        this.children = children;
        boolean hasStart = false;
        boolean hasStop = false;
        //遍历 child node，通过它们状态的汇总得出，服务的状态
        for(NodeStat ns : children) {
            if(STOP.getName().equals(ns.getStatus())) {
                hasStop = true;
            }
            else if(START.getName().equals(ns.getStatus())) {
                hasStart = true;
            }
        }

        if( hasStart == true && hasStop == false ) {
            this.status = START.getName();
        }
        else if( hasStart == false && hasStop == true ) {
            this.status = STOP.getName();
        }
        else if( hasStart == true && hasStop == true ) {
            this.status = START_PART.getName();
        }
        else {
            this.status = STOP.getName();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<RdsNodeStatsVo> getNodes() {
        return nodes;
    }

    public void setNodes(List<RdsNodeStatsVo> nodes) {
        this.nodes = nodes;

        boolean hasStart = false;
        boolean hasStop = false;
        //遍历 child node，通过它们状态的汇总得出，服务的状态
        for(RdsNodeStatsVo ns : nodes) {
            if(STOP.getName().equals(ns.getNodeStatus())) {
                hasStop = true;
            }
            else if(START.getName().equals(ns.getNodeStatus())) {
                hasStart = true;
            }
        }

        if( hasStart == true && hasStop == false ) {
            this.status = START.getName();
        }
        else if( hasStart == false && hasStop == true ) {
            this.status = STOP.getName();
        }
        else if( hasStart == true && hasStop == true ) {
            this.status = START_PART.getName();
        }
        else {
            this.status = STOP.getName();
        }
    }
}
