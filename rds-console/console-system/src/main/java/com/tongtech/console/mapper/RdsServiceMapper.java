package com.tongtech.console.mapper;

import java.util.List;

import com.tongtech.console.domain.vo.RdsServiceQueryVo;
import com.tongtech.console.domain.RdsService;

/**
 * RDS服务Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
public interface RdsServiceMapper
{
    /**
     * 查询RDS服务
     *
     * @param serviceId RDS服务主键
     * @return RDS服务
     */
    public RdsServiceQueryVo selectRdsServiceByServiceId(Long serviceId);

    /**
     * 通过serviceName获取服务信息
     * @param serviceName
     * @return
     */
    public RdsServiceQueryVo selectByServiceName(String serviceName);

    /**
     * 查询RDS服务列表
     *
     * @param rdsService RDS服务, 其中的serviceName和deployMode两个属性可用
     * @return RDS服务集合
     */
    public List<RdsServiceQueryVo> selectRdsServiceList(RdsServiceQueryVo rdsService);

    public Integer selectRdsServiceCount(RdsServiceQueryVo queryVo);

    /**
     * 查询serviceId列表，其下没有节点的service
     *
     * @param rdsService RDS服务
     * @return RDS服务集合
     */
    public List<Long> selectNoneNodeServiceIdList(RdsServiceQueryVo rdsService);

    /**
     * 查询包括(in)多个部署模式的列表
     * @param queryVo 根据 queryVo.deployModes 和 queryVo.serviceName进行查询
     * @return
     */
    public List<RdsServiceQueryVo> selectListInDeployModes(RdsServiceQueryVo queryVo);


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

    /**
     * 重置服务配置
     * @param serviceId
     * @return
     */
    public int resetRdsService(Long serviceId);

    /**
     * 修改服务密码
     * @param rdsService
     * @return
     */
    public int updateServicePassword(RdsService rdsService);

    /**
     * 删除RDS服务
     *
     * @param serviceId RDS服务主键
     * @return 结果
     */
    public int deleteRdsServiceByServiceId(Long serviceId);

    /**
     * 批量删除RDS服务
     *
     * @param serviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRdsServiceByServiceIds(Long[] serviceIds);
}
