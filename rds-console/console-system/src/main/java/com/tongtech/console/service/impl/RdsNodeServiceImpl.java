package com.tongtech.console.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.tongtech.common.utils.DateUtils;
import javax.annotation.Resource;

import com.tongtech.common.utils.StringUtils;
import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.mapper.NodeConfigMapper;
import com.tongtech.console.mapper.NodeStatMapper;
import com.tongtech.console.mapper.RdsNodeMapper;
import com.tongtech.console.enums.NodeStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tongtech.console.service.RdsNodeService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 节点信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-24
 */
@Service
public class RdsNodeServiceImpl implements RdsNodeService
{
    @Resource
    private RdsNodeMapper rdsNodeMapper;

    @Autowired
    private NodeConfigMapper nodeConfigMapper;


    @Resource
    private NodeStatMapper nodeStatMapper;

    /**
     * 查询节点信息
     *
     * @param nodeId 节点信息主键
     * @return 节点信息
     */
    @Override
    public RdsNode selectRdsNodeByNodeId(Long nodeId)
    {
        return rdsNodeMapper.selectNodeByNodeId(nodeId);
    }

    @Override
    public RdsNode selectNodeBy(Long managerId, String nodeName) {
        RdsNode param = new RdsNode();
        param.setManagerId(managerId);
        param.setNodeName(nodeName);
        return rdsNodeMapper.selectNodeByManagerIdAndNodeName(param);
    }

    @Override
    public RdsNode selectNodeBy(String instance) {
        return rdsNodeMapper.selectNodeByInstance(instance);
    }

    /**
     * 获得某服务的节点信息列表, 根据节点类型不同会转换为不同类型：
     * RdsCenterNode, RdsSentinelNode, RdsProxyNode, RdsWorkNode
     * @param serviceId
     * @return
     */
    public List<RdsNode> selectRdsNodesByServiceId(Long serviceId) {
        return rdsNodeMapper.selectNodesByServiceId(serviceId);
    }


    /**
     * 查询节点信息列表
     *
     * @param rdsNode 节点信息
     * @return 节点信息
     */
    @Override
    public List<RdsNode> selectRdsNodeList(RdsNode rdsNode)
    {
        return rdsNodeMapper.selectNodeList(rdsNode);
    }


    @Override
    public List<RdsNode> selectSameNameNodeList(RdsNode node) {
        return rdsNodeMapper.selectSameNameNodeList(node);
    }


    @Override
    public List<RdsNode> selectSamePortNodeList(RdsNode node) {
        return rdsNodeMapper.selectSamePortNodeList(node);
    }


    /**
     * 新增节点信息
     *
     * @param node 节点信息
     * @return 结果
     */
    @Override
    public int insertRdsNode(RdsNode node)
    {
        node.setCreateTime(DateUtils.getNowDate());
        node.setUpdateTime(DateUtils.getNowDate());
        if( StringUtils.isEmpty(node.getInstance()) ) {
            generateInstanceName(node);//如果 instance 是空，自动生成instance名称
        }

        return rdsNodeMapper.insertNode(node);
    }

    /**
     * RdsNode中的instance 自动生成
     * 生成规则： instance = "S" + serviceId + "-" + nodeName;
     * @param node
     */
    private void generateInstanceName(RdsNode node) {
        String nodeName = node.getNodeName();
        StringBuilder buf = new StringBuilder(nodeName.length() + 10);
        buf.append('S').append(node.getServiceId()).append('-').append(nodeName);
        node.setInstance(buf.toString());
    }


    /**
     * 修改节点信息
     * 注意属性：manager_id， service_id， node_name， node_type 不会被更新。
     * @param rdsNode 节点信息
     * @return 结果
     */
    @Override
    public int updateRdsNode(RdsNode rdsNode)
    {
        rdsNode.setUpdateTime(DateUtils.getNowDate());
        return rdsNodeMapper.updateNode(rdsNode);
    }

    @Override
    public int updateNodeStatus(Long nodeId, NodeStatusEnum nodeStatus) {
        RdsNode node = new RdsNode();
        node.setNodeId(nodeId);
        node.setNodeStatus(nodeStatus.getName());
        node.setUpdateTime(DateUtils.getNowDate());
        return rdsNodeMapper.updateNodeStatus(node);
    }

    /**
     * 删除节点信息信息
     *
     * @param nodeId 节点信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRdsNodeByNodeId(Long nodeId)
    {
        //删除节点的监控统计数据
        nodeStatMapper.deleteNodeStatByNodeId(nodeId);
        //删除节点配置
        nodeConfigMapper.deleteNodeConfigByNodeId(nodeId);
        //删除节点本身
        return rdsNodeMapper.deleteNodeByNodeId(nodeId);

    }

    @Override
    public int deleteExpiredNode(Boolean manualAdmin, Date updateTime) {
        RdsService param = new RdsService();
        param.setManualAdmin(manualAdmin);
        param.setUpdateTime(updateTime);
        List<Long> nodeIds = rdsNodeMapper.selectNodeIdList(param);

        int count = 0;
        for(Long nodeId : nodeIds) {
            count += deleteRdsNodeByNodeId(nodeId);
        }
        return count;
    }

    @Override
    @Transactional
    public int updateNoneStopNodes(Date updateTime) {
        return rdsNodeMapper.updateNoneStopNodes(updateTime);
    }

    @Override
    @Transactional
    public int deleteNodeByServiceId(Long serviceId) {
        List<RdsNode> nodes = rdsNodeMapper.selectNodesByServiceId(serviceId);
        int count = 0;
        for(RdsNode node : nodes) {
            count += deleteRdsNodeByNodeId(node.getNodeId());
        }
        return count;
    }

    /**
     * 批量删除节点信息
     *
     * @param nodeIds 需要删除的节点信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRdsNodeByNodeIds(Long[] nodeIds)
    {
        int count = 0;
        for(Long nodeId : nodeIds) {
            count += deleteRdsNodeByNodeId(nodeId);
        }
        return count;
    }


    @Override
    public int updateExpiredNodeStatus(Set<String> runningInstances) {
        String[] instances = runningInstances.toArray(new String[runningInstances.size()]);
        return rdsNodeMapper.updateExpiredNodeStatus(instances);
    }

    @Override
    public List<RdsNode> selectRdsNodes() {
        return rdsNodeMapper.selectRdsNodes();
    }

}
