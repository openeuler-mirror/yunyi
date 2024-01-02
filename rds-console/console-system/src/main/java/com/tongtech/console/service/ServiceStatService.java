package com.tongtech.console.service;

import java.util.List;

import com.tongtech.console.domain.vo.RdsMonitorQueryVo;
import com.tongtech.console.domain.vo.ServiceStatVo;
import com.tongtech.console.domain.ServiceStat;

/**
 * 服务监控信息Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-03-18
 */
public interface ServiceStatService
{
    /**
     * 查询服务监控信息
     *
     * @param statId 服务监控信息主键
     * @return 服务监控信息
     */
    public ServiceStat selectServiceStatByStatId(Long statId);


    ServiceStatVo selectSummaryServiceStat(RdsMonitorQueryVo queryVo);

    /**
     * 查询服务监控信息列表
     *
     * @param serviceStat 服务监控信息
     * @return 服务监控信息集合
     */
    public List<ServiceStat> selectServiceStatList(ServiceStat serviceStat);

    /**
     * 新增服务监控信息
     *
     * @param serviceStat 服务监控信息
     * @return 结果
     */
    public int insertServiceStat(ServiceStat serviceStat);

    /**
     * 修改服务监控信息
     *
     * @param serviceStat 服务监控信息
     * @return 结果
     */
    public int updateServiceStat(ServiceStat serviceStat);

    /**
     * 批量删除服务监控信息
     *
     * @param statIds 需要删除的服务监控信息主键集合
     * @return 结果
     */
    public int deleteServiceStatByStatIds(Long[] statIds);

    /**
     * 删除服务监控信息信息
     *
     * @param statId 服务监控信息主键
     * @return 结果
     */
    public int deleteServiceStatByStatId(Long statId);

}
