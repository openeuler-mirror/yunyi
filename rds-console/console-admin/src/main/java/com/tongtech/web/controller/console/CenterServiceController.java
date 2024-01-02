package com.tongtech.web.controller.console;

import com.tongtech.common.annotation.Log;
import com.tongtech.common.config.UhConsoleConfig;
import com.tongtech.common.constant.ConsoleConstants;
import com.tongtech.common.core.controller.BaseController;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.enums.BusinessType;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.domain.vo.RdsServiceQueryVo;
import com.tongtech.console.service.RdsNodeService;
import com.tongtech.console.service.RdsServiceService;
import com.tongtech.probe.RestCenterClient;
import com.tongtech.probe.RestCenterResult;
import com.tongtech.probe.stat.StatCenterNode;
import com.tongtech.console.task.RdsCenterDataTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.tongtech.common.enums.DeployEnvEnum.K8S;


/**
 * RDS服务Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
@RestController
@RequestMapping("/web-api/console/centerservice")
public class CenterServiceController extends BaseController
{
    @Autowired
    private RdsServiceService serviceService;

    @Autowired
    private RdsNodeService nodeService;


    @Autowired
    private RdsCenterDataTask centerDataTask;

    /**
     * 获取中心服务详细信息
     */
    @PreAuthorize("@ss.hasPermi('console:centerservice:query')")
    @GetMapping
    public AjaxResult getService()
    {
        return AjaxResult.success(serviceService.selectRdsServiceByServiceId(ConsoleConstants.CENTER_SERVICE_ID));
    }

    /**
     * 测试中心节点的管理端口是否可以正常连接
     */
    @PreAuthorize("@ss.hasPermi('console:centerservice:edit')")
    @GetMapping("/testAdminConnection")
    public AjaxResult testAdminConnection() {

        RestCenterClient client = serviceService.getCenterClient();
        if(client != null) {
            try {
                RestCenterResult<StatCenterNode> res = client.getCenters();
                if(res.getListData() != null && res.getListData().size() > 0) {
                    return AjaxResult.success("连接成功");
                }
                else {
                    return AjaxResult.error("接口数据异常，无法找到中心节点！");
                }

            } catch (IOException e) {
                logger.error("CenterServiceController.testAdminConnection() Error!", e);
                return AjaxResult.error("测试连接失败! Error:" + e.getMessage());
            }
        }
        else {
            return AjaxResult.error("测试连接失败! 无法获取客户端连接。");
        }
    }


    /**
     * 测试中心节点的管理端口是否可以正常连接
     *
     */
    @PreAuthorize("@ss.hasPermi('console:centerservice:edit')")
    @PostMapping("/testAdminConnectionNew")
    public AjaxResult testAdminConnectionNew(@RequestBody RdsService serivce) {

        RestCenterClient client = serviceService.getCenterClient(serivce);
        if(client != null) {
            try {
                RestCenterResult<StatCenterNode> res = client.getCenters();
                if(res.getListData() != null && res.getListData().size() > 0) {
                    return AjaxResult.success("连接成功");
                }
                else {
                    return AjaxResult.error("接口数据异常，无法找到中心节点！");
                }

            } catch (IOException e) {
                logger.error("CenterServiceController.testAdminConnection() Error!", e);
                return AjaxResult.error("测试连接失败! Error:" + e.getMessage());
            }
        }
        else {
            return AjaxResult.error("测试连接失败! 无法获取客户端连接。");
        }
    }


    /**
     * 清除所有中心节点，以及管理连接配置。
     * 只有在K8S模式下，才提供此操作。
     * 1.清空所有CenterNode, 2. 清空所有非手工维护的服务及下设节点。
     */
    @PreAuthorize("@ss.hasPermi('console:centerservice:edit')")
    @GetMapping("/clearCenterConfig")
    public AjaxResult clearCenterConfig() {
        RdsService centerServ = serviceService.selectRdsServiceByServiceId(ConsoleConstants.CENTER_SERVICE_ID);

        //重置center service数据
        serviceService.resetRdsService(ConsoleConstants.CENTER_SERVICE_ID);

        //删除中心节点
        nodeService.deleteNodeByServiceId(ConsoleConstants.CENTER_SERVICE_ID);

        //删除非手工维护的节点
        List<RdsServiceQueryVo> noneManualAdminService = serviceService.selectListInDeployModes(new RdsServiceQueryVo(new String[]
                {"single", "sentinel", "sentinel_worker", "cluster", "scalable"})); //查询出除 center 之外的其他所有服务

        for(RdsService serv : noneManualAdminService) {
            if(serv.isManualAdmin() == false) { //非手工维护的节点
                serviceService.deleteRdsServiceByServiceId(serv.getServiceId());
            }
        }

        return AjaxResult.success("中心服务配置已重置！");
    }



    /**
     * 修改RDS服务
     */
    @PreAuthorize("@ss.hasPermi('console:centerservice:edit')")
    @Log(title = "RDS服务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody RdsService serivce)
    {
        if(serivce.getServiceId() == ConsoleConstants.CENTER_SERVICE_ID) {
            if(UhConsoleConfig.getDeployEnvEnum() == K8S) {
                serivce.setManualAdmin(false); //中心服务的K8S模式下是自动维护的
            }
            else {
                serivce.setManualAdmin(true);
            }
            int res = serviceService.updateRdsService(serivce);

            //对center data进行处理分析(获取center节点，RDS服务列表）
            if(UhConsoleConfig.getDeployEnvEnum() == K8S) {
                centerDataTask.process();
            }

            return toAjax(res);
        }
        else {
            return AjaxResult.error("CenterServiceController.update() 函数仅支持中心服务的更新，不能更新非中心服务！！serviceId="
                    + serivce.getServiceId() );
        }
    }



}
