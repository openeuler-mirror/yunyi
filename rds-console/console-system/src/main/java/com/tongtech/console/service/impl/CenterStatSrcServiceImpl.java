package com.tongtech.console.service.impl;

import com.tongtech.common.utils.DateUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.tongtech.console.mapper.CenterStatSrcMapper;
import com.tongtech.console.domain.CenterStatSrc;
import com.tongtech.console.service.CenterStatSrcService;

/**
 * 中心节点统计信息的原始报文Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
@Service
public class CenterStatSrcServiceImpl implements CenterStatSrcService
{
    @Resource
    private CenterStatSrcMapper centerStatSrcMapper;

    /**
     * 查询中心节点统计信息的原始报文
     *
     * @param srcId 中心节点统计信息的原始报文主键
     * @return 中心节点统计信息的原始报文
     */
    @Override
    public CenterStatSrc selectCenterStatSrcBySrcId(Long srcId)
    {
        return centerStatSrcMapper.selectCenterStatSrcBySrcId(srcId);
    }


    /**
     * 新增中心节点统计信息的原始报文
     *
     * @param centerStatSrc 中心节点统计信息的原始报文
     * @return 结果
     */
    @Override
    public int insertCenterStatSrc(CenterStatSrc centerStatSrc)
    {
        centerStatSrc.setCreateTime(DateUtils.getNowDate());
        return centerStatSrcMapper.insertCenterStatSrc(centerStatSrc);
    }

    /**
     * 获取最后一次插入的srcId;
     * @return
     */
    @Override
    public Long selectLastSrcId() {
        return centerStatSrcMapper.selectLastSrcId();
    }


    /**
     * 批量删除中心节点统计信息的原始报文
     *
     * @param srcIds 需要删除的中心节点统计信息的原始报文主键
     * @return 结果
     */
    @Override
    public int deleteCenterStatSrcBySrcIds(Long[] srcIds)
    {
        return centerStatSrcMapper.deleteCenterStatSrcBySrcIds(srcIds);
    }

    /**
     * 删除中心节点统计信息的原始报文信息
     *
     * @param srcId 中心节点统计信息的原始报文主键
     * @return 结果
     */
    @Override
    public int deleteCenterStatSrcBySrcId(Long srcId)
    {
        return centerStatSrcMapper.deleteCenterStatSrcBySrcId(srcId);
    }
}
