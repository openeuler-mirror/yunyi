package com.tongtech.console.mapper;

import java.util.List;
import com.tongtech.console.domain.NodeConfig;

/**
 * 节点配置信息Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-02-27
 */
public interface NodeConfigMapper
{
    /**
     * 查询节点配置信息
     *
     * @param nodeId 节点配置信息主键
     * @return 节点配置信息
     */
    public NodeConfig selectNodeConfigByNodeId(Long nodeId);

    /**
     * 查询节点配置信息列表
     *
     * @param nodeConfig 节点配置信息
     * @return 节点配置信息集合
     */
    public List<NodeConfig> selectNodeConfigList(NodeConfig nodeConfig);

    /**
     * 新增节点配置信息
     *
     * @param nodeConfig 节点配置信息
     * @return 结果
     */
    public int insertNodeConfig(NodeConfig nodeConfig);


    /**
     * 新增或更新节点配置信息，新增时如果主键冲突(node_id, temp_type)，则变为新增。
     *
     * @param nodeConfig 节点配置信息
     * @return 结果
     */
    public int insertUpdateNodeConfig(NodeConfig nodeConfig);

    /**
     * 修改节点配置信息
     *
     * @param nodeConfig 节点配置信息
     * @return 结果
     */
    public int updateNodeConfig(NodeConfig nodeConfig);

    /**
     * 删除节点配置信息
     *
     * @param nodeId 节点配置信息主键
     * @return 结果
     */
    public int deleteNodeConfigByNodeId(Long nodeId);

    /**
     * 批量删除节点配置信息
     *
     * @param nodeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNodeConfigByNodeIds(Long[] nodeIds);
}
