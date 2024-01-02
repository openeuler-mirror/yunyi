package com.tongtech.console.mapper;

import java.util.Date;

import com.tongtech.console.domain.CenterStatSrc;

/**
 * 中心节点统计信息的原始报文Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
public interface CenterStatSrcMapper
{
    /**
     * 查询中心节点统计信息的原始报文
     *
     * @param srcId 中心节点统计信息的原始报文主键
     * @return 中心节点统计信息的原始报文
     */
    public CenterStatSrc selectCenterStatSrcBySrcId(Long srcId);

    /**
     * 新增中心节点统计信息的原始报文
     *
     * @param centerStatSrc 中心节点统计信息的原始报文
     * @return 结果
     */
    public int insertCenterStatSrc(CenterStatSrc centerStatSrc);

    /**
     * 获取最后一次插入的srcId;
     * @return
     */
    public Long selectLastSrcId();


    /**
     * 删除中心节点统计信息的原始报文
     *
     * @param srcId 中心节点统计信息的原始报文主键
     * @return 结果
     */
    public int deleteCenterStatSrcBySrcId(Long srcId);

    /**
     * 批量删除中心节点统计信息的原始报文
     *
     * @param srcIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCenterStatSrcBySrcIds(Long[] srcIds);

    /**
     * 删除小于 createTime 参数时间之前的数据
     * @param createTime
     * @return
     */
    public int deleteByCreateTime(Date createTime);
}
