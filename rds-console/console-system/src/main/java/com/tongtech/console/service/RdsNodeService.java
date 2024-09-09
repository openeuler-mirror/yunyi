package com.tongtech.console.service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.enums.NodeStatusEnum;

/**
 * 节点信息Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-24
 */
public interface RdsNodeService
{
    /**
     * 查询节点信息
     *
     * @param nodeId 节点信息主键
     * @return 节点信息
     */
    RdsNode selectRdsNodeByNodeId(Long nodeId);


    RdsNode selectNodeBy(Long managerId, String nodeName);

    RdsNode selectNodeBy(String instance);

    List<RdsNode> selectRdsNodesByServiceId(Long serviceId);

    /**
     * 查询节点信息列表
     *
     * @param rdsNode 节点信息
     * @return 节点信息集合
     */
    List<RdsNode> selectRdsNodeList(RdsNode rdsNode);

    /**
     * 查询是否有相同名称的节点在同一个节点管理器中
     *
     * @param node 节点信息 只有两个参数可用：managerId(必须), nodeName(必须)
     * @return 节点信息集合
     */
    List<RdsNode> selectSameNameNodeList(RdsNode node);

    /**
     *  查询是否有相同端口的节点，在相同主机地址的情况下。
     *  可选传入 serviceId 属性，用来在搜索范围中排除 serviceId
     *
     * @param node  hostAddress(必须)， serviceId（可选，排除），servicePort(必须, 同时去匹配 servicePort 和 redisPort)
     * @return
     */
    List<RdsNode> selectSamePortNodeList(RdsNode node);

    /**
     * 新增节点信息
     *
     * @param rdsNode 节点信息
     * @return 结果
     */
    int insertRdsNode(RdsNode rdsNode);

    /**
     * 修改节点信息
     *
     * @param rdsNode 节点信息
     * @return 结果
     */
    int updateRdsNode(RdsNode rdsNode);

    /**
     * 更新节点状态
     * @param nodeId
     * @param nodeStatus
     * @return
     */
    int updateNodeStatus(Long nodeId, NodeStatusEnum nodeStatus);

    /**
     * 批量删除节点信息
     *
     * @param nodeIds 需要删除的节点信息主键集合
     * @return 结果
     */
    int deleteRdsNodeByNodeIds(Long[] nodeIds);

    /**
     * 批量删除服务下的所有节点信息
     * @param serviceId
     * @return 结果
     */
    int deleteNodeByServiceId(Long serviceId);

    /**
     * 删除节点信息信息
     *
     * @param nodeId 节点信息主键
     * @return 结果
     */
    int deleteRdsNodeByNodeId(Long nodeId);

    /**
     * 清除过期没有更新的 RDSNode
     * 只清除 manualAdmin == xxxxx, 清除指定时间前没有更新（updateTime <= xxxxx）的节点。
     *
     * @param manualAdmin
     * @param updateTime
     * @return
     */
    int deleteExpiredNode(Boolean manualAdmin, Date updateTime);


    /**
     * 更新nodes状态 , 只要指定时间前没有更新（update_time）的节点, 状态不是 'none' and 'stop'
     * 更新状态为 stop
     * @param updateTime
     * @return
     */
    public int updateNoneStopNodes(Date updateTime);


    public int updateExpiredNodeStatus(Set<String> runningNodes);

    List<RdsNode> selectRdsNodes();
}
