package com.tongtech.console.mapper;

import java.util.Date;
import java.util.List;

import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;

/**
 * 节点信息Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-24
 */
public interface RdsNodeMapper
{
    /**
     * 查询节点信息
     *
     * @param nodeId 节点信息主键
     * @return 节点信息
     */
    public RdsNode selectNodeByNodeId(Long nodeId);

    /**
     * 获得节点信息
     *
     * @param instance 节点实例名称
     * @return 节点信息
     */
    public RdsNode selectNodeByInstance(String instance);

    /**
     * 查询服务serivce下的RDS节点信息列表
     * @return
     */
    public List<RdsNode> selectNodesByServiceId(Long serviceId);

    /**
     * node.managerId , node.nodeName 来唯一获得一个 RdsNode对象
     * @return
     */
    public RdsNode selectNodeByManagerIdAndNodeName(RdsNode node);


    /**
     * 查询 nodeId
     * @param rdsService 查询参数：updateTime, manualAdmin, serviceId(可选）
     * @return
     */
    public List<Long> selectNodeIdList(RdsService rdsService);

    /**
     * 更新nodes状态 , 只要指定时间前没有更新（update_time）的节点, 状态不是 'none' and 'stop'
     * 更新状态为 stop
     * @param updateTime
     * @return
     */
    public int updateNoneStopNodes(Date updateTime);

    /**
     * 查询节点信息列表
     *
     * @param rdsNode 节点信息
     * @return 节点信息集合
     */
    public List<RdsNode> selectNodeList(RdsNode rdsNode);

    /**
     * 查询是否有相同名称的节点在同一个节点管理器中
     *
     * @param rdsNode 节点信息 只有两个参数可用：managerId(必须), nodeName(必须)
     * @return 节点信息集合
     */
    public List<RdsNode> selectSameNameNodeList(RdsNode rdsNode);

    /**
     *  查询是否有相同端口的节点，在相同主机地址的情况下。
     *  可选传入 serviceId 属性，用来在搜索范围中排除 serviceId
     *
     *         host_address =  #{hostAddress}
     *         <if test="serviceId != null"> and n.service_id != #{serviceId}</if>
     *         and (service_port = #{servicePort} or redis_port = #{servicePort})
     *
     * @param rdsNode  hostAddress(必须)， serviceId（可选，排除），servicePort(必须, 同时去匹配 servicePort 和 redisPort)
     * @return
     */
    public List<RdsNode> selectSamePortNodeList(RdsNode rdsNode);


    /**
     * 新增节点信息
     *
     * @param rdsNode 节点信息
     * @return 结果
     */
    public int insertNode(RdsNode rdsNode);

    /**
     * 修改节点信息
     *
     * @param rdsNode 节点信息
     * @return 结果
     */
    public int updateNode(RdsNode rdsNode);

    /**
     * 节点状态信息
     *
     * @param rdsNode 节点信息
     * @return 结果
     */
    public int updateNodeStatus(RdsNode rdsNode);

    /**
     * 删除节点信息
     *
     * @param nodeId 节点信息主键
     * @return 结果
     */
    public int deleteNodeByNodeId(Long nodeId);

    /**
     * 删除node 信息 by serviceId
     * @param serviceId
     * @return
     */
    public int deleteNodesByServiceId(Long serviceId);

    /**
     * 批量删除节点信息
     *
     * @param nodeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNodeByNodeIds(Long[] nodeIds);


    public int updateExpiredNodeStatus(String[] runningInstances);


    List<RdsNode> selectRdsNodes();
}
