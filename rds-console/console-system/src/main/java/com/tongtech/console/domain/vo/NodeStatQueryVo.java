package com.tongtech.console.domain.vo;


import java.io.Serializable;

/**
 * 用来节点统计进行查询的查询条件
 */
public class NodeStatQueryVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long nodeId;

    private Integer groupSeconds; // 查询时分组的时间间隔(秒)

    private Long beginCreateSecond; //开始时间(秒)

    private Long endCreateSecond; //结束时间(秒)

    public Long getBeginCreateSecond() {
        return beginCreateSecond;
    }

    public void setBeginCreateSecond(Long beginCreateSecond) {
        this.beginCreateSecond = beginCreateSecond;
    }

    public Long getEndCreateSecond() {
        return endCreateSecond;
    }

    public void setEndCreateSecond(Long endCreateSecond) {
        this.endCreateSecond = endCreateSecond;
    }

    public NodeStatQueryVo(Long nodeId, Long beginCreateSecond, Integer groupSeconds) {
        this.nodeId = nodeId;
        this.beginCreateSecond = beginCreateSecond;
        this.groupSeconds = groupSeconds;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getGroupSeconds() {
        return groupSeconds;
    }

    public void setGroupSeconds(Integer groupSeconds) {
        this.groupSeconds = groupSeconds;
    }
}
