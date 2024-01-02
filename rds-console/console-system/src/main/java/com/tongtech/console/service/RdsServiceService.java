package com.tongtech.console.service;

import java.util.Date;
import java.util.List;

import com.tongtech.console.domain.vo.RdsServiceQueryVo;
import com.tongtech.console.domain.RdsService;
import com.tongtech.probe.RestCenterClient;

/**
 * RDS服务Service接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
public interface RdsServiceService
{
    /**
     * 查询RDS服务
     *
     * @param serviceId RDS服务主键
     * @return RDS服务
     */
    RdsService selectRdsServiceByServiceId(Long serviceId);

    /**
     * 通过serviceName，查询某一个RDS服务
     * @param serviceName
     * @return
     */
    RdsService selectServiceBy(String serviceName);


    boolean existsServiceName(String serviceName);

    /**
     * 查询RDS服务列表
     *
     * @param rdsService RDS服务
     * @return RDS服务集合
     */
    List<RdsServiceQueryVo> selectRdsServiceList(RdsServiceQueryVo rdsService);

    /**
     * 查询RDS服务的数量
     *
     * @param rdsService RDS服务查询条件
     * @return RDS服务集合
     */
    Integer selectRdsServiceCount(RdsServiceQueryVo rdsService);

    List<RdsServiceQueryVo> selectListInDeployModes(RdsServiceQueryVo queryVo);

    RestCenterClient getCenterClient();

    RestCenterClient getCenterClient(RdsService centerServ);

    /**
     * 新增RDS服务
     *
     * @param rdsService RDS服务
     * @return 结果
     */
    public int insertRdsService(RdsService rdsService);

    /**
     * 修改RDS服务
     *
     * @param rdsService RDS服务
     * @return 结果
     */
    public int updateRdsService(RdsService rdsService);

    int resetRdsService(Long serviceId);

    int updateServicePassword(RdsService rdsService);

    /**
     * 批量删除RDS服务
     *
     * @param serviceIds 需要删除的RDS服务主键集合
     * @return 结果
     */
    public int deleteRdsServiceByServiceIds(Long[] serviceIds);

    /**
     * 删除RDS服务信息
     *
     * @param serviceId RDS服务主键
     * @return 结果
     */
    public int deleteRdsServiceByServiceId(Long serviceId);

    /**
     * 清除下属没有Node，且过期(一段时间没有更新) 的RDSService
     * 清除下属count(nodeId) == 0,  manualAdmin == xxxxx, 清除指定时间前没有更新（updateTime <= xxxxx）服务。
     *
     * @param manualAdmin
     * @param updateTime
     * @return
     */
    int deleteNoneNodeExpiredService(Boolean manualAdmin, Date updateTime);
}
