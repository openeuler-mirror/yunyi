package com.tongtech.console.mapper;

import java.util.Date;
import java.util.List;

import com.tongtech.console.domain.vo.RdsMonitorQueryVo;
import com.tongtech.console.domain.ServiceStat;

/**
 * 服务监控信息Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-03-18
 */
public interface ServiceStatMapper
{
    /**
     * 查询服务监控信息
     *
     * @param statId 服务监控信息主键
     * @return 服务监控信息
     */
    public ServiceStat selectServiceStatByStatId(Long statId);

    /**
     * 查询服务监控信息列表
     *
     * @param serviceStat 服务监控信息
     * @return 服务监控信息集合
     */
    public List<ServiceStat> selectServiceStatList(ServiceStat serviceStat);

    //public List<ServiceStat> selectMonitorList(RdsMonitorQueryVo queryVo);

    /**
     * 查询以一个服务ID，一个时间段的监控信息汇总平局
     * @param queryVo
     * @return
     */
    public ServiceStat selectSummaryServiceStat(RdsMonitorQueryVo queryVo);

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
     * 删除服务监控信息
     *
     * @param statId 服务监控信息主键
     * @return 结果
     */
    public int deleteServiceStatByStatId(Long statId);

    /**
     * 批量删除服务监控信息
     *
     * @param statIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteServiceStatByStatIds(Long[] statIds);

    /**
     * 删除服务监控信息信息, 通过serviceId
     *
     * @param serviceId 服务监控信息主键
     * @return 结果
     */
    public int deleteServiceStatByServiceId(Long serviceId);

    /**
     * 删除小于 createTime 参数时间之前的数据
     * @param createTime
     * @return
     */
    public int deleteByCreateTime(Date createTime);
}
