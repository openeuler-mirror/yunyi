package com.tongtech.console.service.impl;

import java.util.List;
import com.tongtech.common.utils.DateUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import com.tongtech.console.mapper.ServiceConfigMapper;
import com.tongtech.console.domain.ServiceConfig;
import com.tongtech.console.service.ServiceConfigService;

/**
 * 服务配置信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-11
 */
@Service
public class ServiceConfigServiceImpl implements ServiceConfigService
{
    @Resource
    private ServiceConfigMapper serviceConfigMapper;

    /**
     * 查询服务配置信息
     *
     * @param serviceId 服务配置信息主键
     * @return 服务配置信息
     */
    @Override
    public ServiceConfig selectServiceConfigBy(Long serviceId, String confType)
    {
        ServiceConfig conf = new ServiceConfig();
        conf.setServiceId(serviceId);
        conf.setConfType(confType);
        return serviceConfigMapper.selectServiceConfig(conf);
    }

    /**
     * 查询服务配置信息列表
     *
     * @param serviceConfig 服务配置信息
     * @return 服务配置信息
     */
    @Override
    public List<ServiceConfig> selectServiceConfigList(ServiceConfig serviceConfig)
    {
        return serviceConfigMapper.selectServiceConfigList(serviceConfig);
    }

    /**
     * 新增服务配置信息
     *
     * @param serviceConfig 服务配置信息
     * @return 结果
     */
    @Override
    public int insertServiceConfig(ServiceConfig serviceConfig)
    {
        return serviceConfigMapper.insertServiceConfig(serviceConfig);
    }

    /**
     * 修改服务配置信息
     *
     * @param serviceConfig 服务配置信息
     * @return 结果
     */
    @Override
    public int updateServiceConfig(ServiceConfig serviceConfig)
    {
        serviceConfig.setUpdateTime(DateUtils.getNowDate());
        return serviceConfigMapper.updateServiceConfig(serviceConfig);
    }

    /**
     * 批量删除服务配置信息
     *
     * @param serviceIds 需要删除的服务配置信息主键
     * @return 结果
     */
    @Override
    public int deleteServiceConfigByServiceIds(Long[] serviceIds)
    {
        return serviceConfigMapper.deleteServiceConfigByServiceIds(serviceIds);
    }

    /**
     * 删除服务配置信息信息
     *
     * @param serviceId 服务配置信息主键
     * @return 结果
     */
    @Override
    public int deleteServiceConfigByServiceId(Long serviceId)
    {
        return serviceConfigMapper.deleteServiceConfigByServiceId(serviceId);
    }
}
