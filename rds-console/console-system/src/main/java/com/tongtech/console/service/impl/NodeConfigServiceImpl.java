package com.tongtech.console.service.impl;

import java.util.List;
import com.tongtech.common.utils.DateUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.tongtech.console.mapper.NodeConfigMapper;
import com.tongtech.console.domain.NodeConfig;
import com.tongtech.console.service.NodeConfigService;

/**
 * 节点配置信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-02-27
 */
@Service
public class NodeConfigServiceImpl implements NodeConfigService
{
    @Resource
    private NodeConfigMapper nodeConfigMapper;

    /**
     * 查询节点配置信息
     *
     * @param nodeId 节点配置信息主键
     * @return 节点配置信息
     */
    @Override
    public NodeConfig selectNodeConfigByNodeId(Long nodeId)
    {
        return nodeConfigMapper.selectNodeConfigByNodeId(nodeId);
    }

    /**
     * 查询节点配置信息列表
     *
     * @param nodeConfig 节点配置信息
     * @return 节点配置信息
     */
    @Override
    public List<NodeConfig> selectNodeConfigList(NodeConfig nodeConfig)
    {
        return nodeConfigMapper.selectNodeConfigList(nodeConfig);
    }

    /**
     * 新增节点配置信息
     *
     * @param nodeConfig 节点配置信息
     * @return 结果
     */
    @Override
    public int insertNodeConfig(NodeConfig nodeConfig)
    {
        return nodeConfigMapper.insertNodeConfig(nodeConfig);
    }

    /**
     * 修改节点配置信息
     *
     * @param nodeConfig 节点配置信息
     * @return 结果
     */
    @Override
    public int updateNodeConfig(NodeConfig nodeConfig)
    {
        nodeConfig.setUpdateTime(DateUtils.getNowDate());
        return nodeConfigMapper.updateNodeConfig(nodeConfig);
    }

    /**
     * 批量删除节点配置信息
     *
     * @param nodeIds 需要删除的节点配置信息主键
     * @return 结果
     */
    @Override
    public int deleteNodeConfigByNodeIds(Long[] nodeIds)
    {
        return nodeConfigMapper.deleteNodeConfigByNodeIds(nodeIds);
    }

    /**
     * 删除节点配置信息信息
     *
     * @param nodeId 节点配置信息主键
     * @return 结果
     */
    @Override
    public int deleteNodeConfigByNodeId(Long nodeId)
    {
        return nodeConfigMapper.deleteNodeConfigByNodeId(nodeId);
    }
}
