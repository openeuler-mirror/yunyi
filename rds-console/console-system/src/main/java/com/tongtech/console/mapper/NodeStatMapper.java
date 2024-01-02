package com.tongtech.console.mapper;

import java.util.Date;
import java.util.List;

import com.tongtech.console.domain.vo.NodeStatQueryVo;
import com.tongtech.console.domain.NodeStat;

/**
 * 节点监控信息Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
public interface NodeStatMapper
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
     * 监控查询某一个节点的状态信息, 按时间段分组
     *   nodeId  节点ID
     *   groupSeconds; 查询时分组的时间间隔(秒)
     *   beginCreateSecond; 开始时间(秒)
     * @param queryVo
     * @return
     */
    public List<NodeStat>  selectMonitorGroupList(NodeStatQueryVo queryVo);


    /**
     * 监控查询某一个节点的状态信息
     *   nodeId  节点ID
     *   beginCreateSecond; 开始时间(秒)
     * @param queryVo
     * @return
     */
    public List<NodeStat>  selectMonitorList(NodeStatQueryVo queryVo);

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
     * 删除节点监控信息
     *
     * @param statId 节点监控信息主键
     * @return 结果
     */
    public int deleteNodeStatByStatId(Long statId);

    /**
     * 批量删除节点监控信息
     *
     * @param statIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteNodeStatByStatIds(Long[] statIds);


    /**
     * 删除节点监控信息, 通过节点ID
     *
     * @param nodeId
     * @return 结果
     */
    public int deleteNodeStatByNodeId(Long nodeId);

    /**
     * 删除小于 createTime 参数时间之前的数据
     * @param createTime
     * @return
     */
    public int deleteByCreateTime(Date createTime);
}
