package com.tongtech.console.domain.vo;

import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;

import java.io.Serializable;
import java.util.List;

/**
 * RdsService 和其下属 nodes 的集合对象，用于前端显示和赋值
 */
public class RdsServiceNodesVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private RdsService service;

    private List<RdsNode> nodes;

    private List<String> reloadNodes;  //需要重启的节点名称

    private List<Long> deleteNodeIds;  //需要删除的节点名称

    private Boolean reloadable;  //是否重启那些需要重启的节点

    public RdsServiceNodesVo() {}

    public RdsServiceNodesVo(RdsService service, List<RdsNode> nodes) {
        this.service = service;
        this.nodes = nodes;
    }

    public RdsService getService() {
        return service;
    }

    public void setService(RdsService service) {
        this.service = service;
    }

    public List<RdsNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<RdsNode> nodes) {
        this.nodes = nodes;
    }

    public List<String> getReloadNodes() {
        return reloadNodes;
    }

    public void setReloadNodes(List<String> reloadNodes) {
        this.reloadNodes = reloadNodes;
    }

    public List<Long> getDeleteNodeIds() {
        return deleteNodeIds;
    }

    public void setDeleteNodeIds(List<Long> deleteNodeIds) {
        this.deleteNodeIds = deleteNodeIds;
    }

    public Boolean getReloadable() {
        return reloadable;
    }

    public void setReloadable(Boolean reloadable) {
        this.reloadable = reloadable;
    }
}
