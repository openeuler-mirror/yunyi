package com.tongtech.web.controller.console;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tongtech.common.utils.AESUtils;
import javax.servlet.http.HttpServletResponse;

import com.tongtech.common.utils.StringUtils;
import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.vo.RdsServiceNodesVo;
import com.tongtech.console.domain.vo.RdsServiceQueryVo;
import com.tongtech.console.enums.DeployModeEnum;
import com.tongtech.console.service.RdsNodeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tongtech.common.annotation.Log;
import com.tongtech.common.core.controller.BaseController;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.enums.BusinessType;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.service.RdsServiceService;
import com.tongtech.common.utils.poi.ExcelUtil;
import com.tongtech.common.core.page.TableDataInfo;


/**
 * RDS服务Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
@RestController
@RequestMapping("/web-api/console/rdsservice")
public class RdsServiceController extends BaseController
{
    @Autowired
    private RdsServiceService serviceService;

    @Autowired
    private RdsNodeService nodeService;

    /**
     * 查询RDS服务列表, 非分页方式
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:list')")
    @GetMapping("/list")
    public AjaxResult list(RdsServiceQueryVo rdsService)
    {
        return AjaxResult.success(serviceService.selectRdsServiceList(rdsService));
    }

    /**
     * 查询RDS服务列表，并附带子属性nodes列表，如果是哨兵服务则查询出使用该哨兵的"sentinel_worker"服务的数量（workerServices）
     * 默认去除了中心服务和哨兵服务， 但当指定deployMode条件后，则只查询deployMode指定的部署模式
     *
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:list')")
    @GetMapping("/listWithNodes")
    public TableDataInfo listWithNodes(RdsServiceQueryVo queryVo)
    {
        startPage();
        if(StringUtils.isEmpty(queryVo.getDeployMode())) {
            queryVo.addDeployModes(new String[] {"single", "sentinel_worker", "cluster", "scalable"});
        }
        else {
            queryVo.addDeployMode(queryVo.getDeployMode());
        }

        List<RdsServiceQueryVo> list = serviceService.selectListInDeployModes(queryVo);
        List<RdsNode> rdsNodesList = nodeService.selectRdsNodes();
        Map<Long, List<RdsNode>> rdsNodesMap = rdsNodesList.stream()
                .collect(Collectors.groupingBy(e -> e.getServiceId()));

        //查询并附加节点列表
        for(RdsServiceQueryVo serv : list) {
            serv.setNodes(rdsNodesMap.get(serv.getServiceId()));
            if(serv.getDeployModeEnum() == DeployModeEnum.SENTINEL) {
                RdsServiceQueryVo countParam = new RdsServiceQueryVo();
                countParam.setSentinelServiceId(serv.getServiceId());
                serv.setWorkerServices(serviceService.selectRdsServiceCount(countParam));
            }
        }

        return getDataTable(list);
    }

    /**
     * 导出RDS服务列表
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:export')")
    @Log(title = "RDS服务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RdsServiceQueryVo rdsService)
    {
        List<RdsServiceQueryVo> list = serviceService.selectRdsServiceList(rdsService);
        ExcelUtil<RdsServiceQueryVo> util = new ExcelUtil<RdsServiceQueryVo>(RdsServiceQueryVo.class);
        util.exportExcel(response, list, "RDS服务数据");
    }

    /**
     * 获取RDS服务详细信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:list')")
    @GetMapping(value = "/{serviceId}")
    public AjaxResult getInfo(@PathVariable("serviceId") Long serviceId)
    {
        RdsService serv = serviceService.selectRdsServiceByServiceId(serviceId);
        if(StringUtils.isNotEmpty(serv.getPassword())) {
            String password = AESUtils.encryptAES(serv.getPassword());
            serv.setPassword(password);
        }

        return AjaxResult.success(serv);
    }

    /**
     * 新增RDS服务
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:add')")
    @Log(title = "RDS服务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RdsService service)
    {
        if(StringUtils.isNotEmpty(service.getPassword())) {
            String password = AESUtils.decryptAES(service.getPassword());
            service.setPassword(password);
        }


        return toAjax(serviceService.insertRdsService(service));
    }



    /**
     * 修改RDS服务
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:edit')")
    @Log(title = "RDS服务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult update(@RequestBody RdsService service)
    {
        if(StringUtils.isNotEmpty(service.getPassword())) {
            String password = AESUtils.decryptAES(service.getPassword());
            service.setPassword(password);
        }

        return toAjax(serviceService.updateRdsService(service));
    }

    /**
     * 修改RDS服务
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:edit')")
    @Log(title = "RDS服务-密码", businessType = BusinessType.UPDATE)
    @PutMapping("/servicePassword")
    public AjaxResult updatePassword(@RequestBody RdsService service)
    {
        if(StringUtils.isNotEmpty(service.getPassword())) {
            String password = AESUtils.decryptAES(service.getPassword());
            service.setPassword(password);
        }
        return toAjax(serviceService.updateServicePassword(service));
    }





    /**
     * 获取RDS服务详细信息 和 其所属的node列表
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:query')")
    @GetMapping(value = "/serviceNodes/{serviceId}")
    public AjaxResult getServiceWithNodes(@PathVariable("serviceId") Long serviceId)
    {
        RdsService serv = serviceService.selectRdsServiceByServiceId(serviceId);

        if(StringUtils.isNotEmpty(serv.getPassword())) {
            String password = AESUtils.encryptAES(serv.getPassword());
            serv.setPassword(password);
        }

        List<RdsNode> nodes = nodeService.selectRdsNodesByServiceId(serviceId);
        return AjaxResult.success(new RdsServiceNodesVo(serv, nodes));
    }

    @GetMapping(value = "/existName/{serviceName}")
    public AjaxResult existsServiceName(@PathVariable("serviceName") String serviceName)
    {
        return AjaxResult.success(serviceService.existsServiceName(serviceName));
    }

}
