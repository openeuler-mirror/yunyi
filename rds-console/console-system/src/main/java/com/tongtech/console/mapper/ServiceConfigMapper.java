package com.tongtech.console.mapper;

import java.util.List;
import com.tongtech.console.domain.ServiceConfig;

/**
 * 服务配置信息Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-11
 */
public interface ServiceConfigMapper
{
    /**
     * 查询服务配置信息
     *  where service_id = #{serviceId} and conf_type = #{confType}
     * @param serviceConfig 服务配置信息主键 service_id, conf_type
     * @return 服务配置信息
     */
    public ServiceConfig selectServiceConfig(ServiceConfig serviceConfig);

    /**
     * 查询服务配置信息列表
     *
     * @param serviceConfig 服务配置信息
     * @return 服务配置信息集合
     */
    public List<ServiceConfig> selectServiceConfigList(ServiceConfig serviceConfig);

    /**
     * 新增服务配置信息
     *
     * @param serviceConfig 服务配置信息
     * @return 结果
     */
    public int insertServiceConfig(ServiceConfig serviceConfig);

    public int insertUpdateServiceConfig(ServiceConfig serviceConfig);


    /**
     * 修改服务配置信息
     *
     * @param serviceConfig 服务配置信息
     * @return 结果
     */
    public int updateServiceConfig(ServiceConfig serviceConfig);

    /**
     * 删除服务配置信息
     *
     * @param serviceId 服务配置信息主键
     * @return 结果
     */
    public int deleteServiceConfigByServiceId(Long serviceId);

    /**
     * 批量删除服务配置信息
     *
     * @param serviceIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteServiceConfigByServiceIds(Long[] serviceIds);
}
