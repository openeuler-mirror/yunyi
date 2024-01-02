package com.tongtech.console.service;

import com.tongtech.console.domain.CenterStatSrc;

/**
 * 中心节点统计信息的原始报文Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
public interface CenterStatSrcService
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
     * 批量删除中心节点统计信息的原始报文
     *
     * @param srcIds 需要删除的中心节点统计信息的原始报文主键集合
     * @return 结果
     */
    public int deleteCenterStatSrcBySrcIds(Long[] srcIds);

    /**
     * 删除中心节点统计信息的原始报文信息
     *
     * @param srcId 中心节点统计信息的原始报文主键
     * @return 结果
     */
    public int deleteCenterStatSrcBySrcId(Long srcId);
}
