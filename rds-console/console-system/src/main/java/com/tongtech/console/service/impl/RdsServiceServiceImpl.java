package com.tongtech.console.service.impl;

import java.util.Date;
import java.util.List;

import com.tongtech.common.config.UhConsoleConfig;
import com.tongtech.common.constant.ConsoleConstants;
import com.tongtech.common.utils.DateUtils;
import javax.annotation.Resource;

import com.tongtech.common.utils.StringUtils;
import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.vo.RdsServiceQueryVo;
import com.tongtech.console.mapper.RdsServiceMapper;
import com.tongtech.console.mapper.ServiceConfigMapper;
import com.tongtech.console.mapper.ServiceStatMapper;
import com.tongtech.console.service.RdsNodeService;
import com.tongtech.probe.RestCenterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.service.RdsServiceService;

import static com.tongtech.common.enums.DeployEnvEnum.HOST;
import static com.tongtech.common.enums.DeployEnvEnum.K8S;
import static com.tongtech.console.enums.NodeStatusEnum.START;

/**
 * RDS服务Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
@Service
public class RdsServiceServiceImpl implements RdsServiceService
{
    @Resource
    private RdsServiceMapper rdsServiceMapper;

    @Autowired
    private ServiceConfigMapper serviceConfigMapper;

    @Autowired
    private RdsNodeService rdsNodeService;

    @Resource
    private ServiceStatMapper serviceStatMapper;

    /**
     * 查询RDS服务
     *
     * @param serviceId RDS服务主键
     * @return RDS服务
     */
    @Override
    public RdsServiceQueryVo selectRdsServiceByServiceId(Long serviceId)
    {
        return rdsServiceMapper.selectRdsServiceByServiceId(serviceId);
    }

    @Override
    public RdsServiceQueryVo selectServiceBy(String serviceName) {
        return  rdsServiceMapper.selectByServiceName(serviceName);
    }

    @Override
    public boolean existsServiceName(String serviceName) {
        return (rdsServiceMapper.selectByServiceName(serviceName) != null);
    }

    /**
     * 查询RDS服务列表
     *
     * @param queryVo RDS服务
     * @return RDS服务
     */
    @Override
    public List<RdsServiceQueryVo> selectRdsServiceList(RdsServiceQueryVo queryVo)
    {
        return rdsServiceMapper.selectRdsServiceList(queryVo);
    }

    @Override
    public Integer selectRdsServiceCount(RdsServiceQueryVo queryVo) {
        return rdsServiceMapper.selectRdsServiceCount(queryVo);
    }

    @Override
    public List<RdsServiceQueryVo> selectListInDeployModes(RdsServiceQueryVo queryVo) {
        return rdsServiceMapper.selectListInDeployModes(queryVo);
    }

    /**
     * 获得和中心管理端口连接的客户端
     *
     * @return 如配置有效返回RestCenterClient对象； 否则返回 null
     */
    @Override
    public RestCenterClient getCenterClient() {
        RdsService centerServ = rdsServiceMapper.selectRdsServiceByServiceId(ConsoleConstants.CENTER_SERVICE_ID);
        return getCenterClient(centerServ);
    }


    /**
     * 获得和中心管理端口连接的客户端
     * @param centerServ 中心服务对象
     * @return 如配置有效返回RestCenterClient对象； 否则返回 null
     */
    @Override
    public RestCenterClient getCenterClient(RdsService centerServ) {
        RestCenterClient centerClient = null;
        if(UhConsoleConfig.getDeployEnvEnum() == K8S) { //如果是K8S模式，通过中心服务中的地址来连接
            if(StringUtils.isNotEmpty(centerServ.getHostAddress()) && centerServ.getAdminPort() > 0) {
                StringBuilder url = new StringBuilder(centerServ.getHostAddress().length() + 10);
                url.append("http://").append(centerServ.getHostAddress().trim())
                        .append(':').append(centerServ.getAdminPort());
                centerClient = new RestCenterClient(url.toString());
            }
        }
        else if(UhConsoleConfig.getDeployEnvEnum() == HOST) { //如果是HOST模式，通过有一个可连接的中心节点进行连接
            List<RdsNode> centers = rdsNodeService.selectRdsNodesByServiceId(ConsoleConstants.CENTER_SERVICE_ID);
            for(RdsNode center : centers) {
                if(center.getNodeStatusEnum() == START) {
                    StringBuilder url = new StringBuilder(center.getHostAddress().length() + 10);
                    url.append("http://").append(center.getHostAddress())
                            .append(':').append(center.getAdminPort()).append('/');
                    centerClient = new RestCenterClient(url.toString());
                    break;
                }
            }
        }

        return centerClient;
    }


    /**
     * 新增RDS服务
     *
     * @param rdsService RDS服务
     * @return 结果
     */
    @Override
    public int insertRdsService(RdsService rdsService)
    {
        rdsService.setCreateTime(DateUtils.getNowDate());
        rdsService.setUpdateTime(DateUtils.getNowDate());
        if(UhConsoleConfig.getDeployEnvEnum() == K8S) {
            rdsService.setManualAdmin(false);
        }
        else {
            rdsService.setManualAdmin(true);
        }

        return rdsServiceMapper.insertRdsService(rdsService);
    }

    /**
     * 修改RDS服务
     *
     * @param rdsService RDS服务
     * @return 结果
     */
    @Override
    public int updateRdsService(RdsService rdsService)
    {
        rdsService.setUpdateTime(DateUtils.getNowDate());
        return rdsServiceMapper.updateRdsService(rdsService);
    }

    /**
     * 重置服务配置信息
     * @param serviceId
     * @return
     */
    @Override
    public int resetRdsService(Long serviceId) {
        return rdsServiceMapper.resetRdsService(serviceId);
    }


    /**
     * 修改RDS服务
     *
     * @param rdsService RDS服务
     * @return 结果
     */
    @Override
    public int updateServicePassword(RdsService rdsService)
    {
        rdsService.setUpdateTime(DateUtils.getNowDate());
        return rdsServiceMapper.updateServicePassword(rdsService);
    }

    /**
     * 批量删除RDS服务
     *
     * @param serviceIds 需要删除的RDS服务主键
     * @return 结果
     */
    @Override
    public int deleteRdsServiceByServiceIds(Long[] serviceIds)
    {
        int ret = 0;
        for(Long serviceId : serviceIds) {
            ret += deleteRdsServiceByServiceId(serviceId);
        }
        return ret;
    }

    /**
     * 删除RDS服务信息, 和其关联的node数据， config数据
     *
     * @param serviceId RDS服务主键
     * @return 结果
     */
    @Override
    public int deleteRdsServiceByServiceId(Long serviceId)
    {
        //删除Service的监控数据
        serviceStatMapper.deleteServiceStatByServiceId(serviceId);
        //删除关联的节点
        rdsNodeService.deleteNodeByServiceId(serviceId);
        //删除服务配置
        serviceConfigMapper.deleteServiceConfigByServiceId(serviceId);
        //删除服务
        return rdsServiceMapper.deleteRdsServiceByServiceId(serviceId);
    }


    /**
     * 清除下属没有Node，且过期(一段时间没有更新) 的RDSService
     * 清除下属count(nodeId) == 0,  manualAdmin == xxxxx, 清除指定时间前没有更新（updateTime <= xxxxx）服务。
     *
     * @param manualAdmin
     * @param updateTime
     * @return
     */
    @Override
    public int deleteNoneNodeExpiredService(Boolean manualAdmin, Date updateTime) {
        RdsServiceQueryVo param = new RdsServiceQueryVo();
        param.setManualAdmin(manualAdmin);
        param.setUpdateTime(updateTime);
        List<Long> serviceIds = rdsServiceMapper.selectNoneNodeServiceIdList(param);
        int count = 0;
        for(Long serviceId : serviceIds) {
            count += deleteRdsServiceByServiceId(serviceId);
        }
        return count;
    }



}
