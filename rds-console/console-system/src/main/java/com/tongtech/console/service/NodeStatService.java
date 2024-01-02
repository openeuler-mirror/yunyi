package com.tongtech.console.service;

import java.util.List;
import com.tongtech.console.domain.NodeStat;

/**
 * 节点监控信息Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
public interface NodeStatService
{
    /**
     * 查询节点监控信息
     *
     * @param statId 节点监控信息主键
     * @return 节点监控信息
     */
    public NodeStat selectNodeStatByStatId(Long statId);

    /**
     * 查询节点监控信息列表
     *
     * @param nodeStat 节点监控信息
     * @return 节点监控信息集合
     */
    public List<NodeStat> selectNodeStatList(NodeStat nodeStat);

    /**
     * 新增节点监控信息
     *
     * @param nodeStat 节点监控信息
     * @return 结果
     */
    public int insertNodeStat(NodeStat nodeStat);

    /**
     * 修改节点监控信息
     *
     * @param nodeStat 节点监控信息
     * @return 结果
     */
    public int updateNodeStat(NodeStat nodeStat);

    /**
     * 批量删除节点监控信息
     *
     * @param statIds 需要删除的节点监控信息主键集合
     * @return 结果
     */
    public int deleteNodeStatByStatIds(Long[] statIds);

    /**
     * 删除节点监控信息信息
     *
     * @param statId 节点监控信息主键
     * @return 结果
     */
    public int deleteNodeStatByStatId(Long statId);
}
