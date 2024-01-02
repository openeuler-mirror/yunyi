package com.tongtech.console.service.impl;

import java.util.List;
import com.tongtech.common.utils.DateUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.tongtech.console.mapper.NodeStatMapper;
import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.service.NodeStatService;

/**
 * 节点监控信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
@Service
public class NodeStatServiceImpl implements NodeStatService
{
    @Resource
    private NodeStatMapper nodeStatMapper;

    /**
     * 查询节点监控信息
     *
     * @param statId 节点监控信息主键
     * @return 节点监控信息
     */
    @Override
    public NodeStat selectNodeStatByStatId(Long statId)
    {
        return nodeStatMapper.selectNodeStatByStatId(statId);
    }

    /**
     * 查询节点监控信息列表
     *
     * @param nodeStat 节点监控信息
     * @return 节点监控信息
     */
    @Override
    public List<NodeStat> selectNodeStatList(NodeStat nodeStat)
    {
        return nodeStatMapper.selectNodeStatList(nodeStat);
    }

    /**
     * 新增节点监控信息
     *
     * @param nodeStat 节点监控信息
     * @return 结果
     */
    @Override
    public int insertNodeStat(NodeStat nodeStat)
    {
        nodeStat.setCreateTime(DateUtils.getNowDate());
        return nodeStatMapper.insertNodeStat(nodeStat);
    }

    /**
     * 修改节点监控信息
     *
     * @param nodeStat 节点监控信息
     * @return 结果
     */
    @Override
    public int updateNodeStat(NodeStat nodeStat)
    {
        return nodeStatMapper.updateNodeStat(nodeStat);
    }

    /**
     * 批量删除节点监控信息
     *
     * @param statIds 需要删除的节点监控信息主键
     * @return 结果
     */
    @Override
    public int deleteNodeStatByStatIds(Long[] statIds)
    {
        return nodeStatMapper.deleteNodeStatByStatIds(statIds);
    }

    /**
     * 删除节点监控信息信息
     *
     * @param statId 节点监控信息主键
     * @return 结果
     */
    @Override
    public int deleteNodeStatByStatId(Long statId)
    {
        return nodeStatMapper.deleteNodeStatByStatId(statId);
    }
}
