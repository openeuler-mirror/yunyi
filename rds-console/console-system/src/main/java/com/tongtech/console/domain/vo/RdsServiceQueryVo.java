package com.tongtech.console.domain.vo;

import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于RdsService的查询条件，和查询结果
 */
public class RdsServiceQueryVo extends RdsService {

    /**
     * 服务下的节点列表，列表显示时需要
     */
    private List<RdsNode> nodes;

    /**
     * 查询条件时可以指定多个deployMode
     */
    private List<String> deployModes;

    /**
     * 哨兵服务时，使用哨兵服务的主从服务数据量
     */
    private Integer workerServices;

    public RdsServiceQueryVo() {
        this.deployModes = new ArrayList();
    }

    public RdsServiceQueryVo(String[] deployModes) {
        this();
        addDeployModes(deployModes);
    }

    public List<String> getDeployModes() {
        return deployModes;
    }

    public void setDeployModes(List<String> deployModes) {
        this.deployModes = deployModes;
    }

    public boolean addDeployMode(String deployMode) {
        return this.deployModes.add(deployMode);
    }

    public void addDeployModes(String[] deployModes) {
        for(String deployMode : deployModes) {
            this.deployModes.add(deployMode);
        }
    }

    public List<RdsNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<RdsNode> nodes) {
        this.nodes = nodes;
    }

    public Integer getWorkerServices() {
        return workerServices;
    }

    public void setWorkerServices(Integer workerServices) {
        this.workerServices = workerServices;
    }
}
