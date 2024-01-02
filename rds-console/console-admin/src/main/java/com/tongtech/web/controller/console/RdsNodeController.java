package com.tongtech.web.controller.console;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.tongtech.console.domain.RdsNode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tongtech.common.annotation.Log;
import com.tongtech.common.core.controller.BaseController;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.enums.BusinessType;
import com.tongtech.console.service.RdsNodeService;
import com.tongtech.common.utils.poi.ExcelUtil;
import com.tongtech.common.core.page.TableDataInfo;

/**
 * 节点信息Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-24
 */
@RestController
@RequestMapping("/web-api/console/rdsnode")
public class RdsNodeController extends BaseController
{
    @Autowired
    private RdsNodeService nodeService;

    /**
     * 查询节点信息列表，返回所有数据（不分页）
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:list')")
    @GetMapping("/list")
    public AjaxResult list(RdsNode rdsNode)
    {
        return AjaxResult.success(nodeService.selectRdsNodeList(rdsNode));
    }


    /**
     * 查询是否有相同名称的节点在同一个服务或同一个节点管理器中
     *
     * @param node 节点信息 只有三个参数可用：managerId(必须), nodeName(必须)
     * @return 节点信息集合
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:list')")
    @GetMapping("/listSameName")
    public AjaxResult listSameName(RdsNode node)
    {
        return AjaxResult.success(nodeService.selectSameNameNodeList(node));
    }


    /**
     *  查询是否有相同端口的节点，在相同主机地址的情况下。
     *  可选传入 serviceId 属性，用来在搜索范围中排除 serviceId
     *
     * @param node  hostAddress(必须)， serviceId（可选，排除），servicePort(必须, 同时去匹配 servicePort 和 redisPort)
     * @return
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:list')")
    @GetMapping("/listSamePort")
    public AjaxResult listSamePort(RdsNode node)
    {
        return AjaxResult.success(nodeService.selectSamePortNodeList(node));
    }


    /**
     * 查询节点信息列表，返回分页数据
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:list')")
    @GetMapping("/listPaging")
    public TableDataInfo listPaging(RdsNode rdsNode)
    {
        startPage();
        List<RdsNode> list = nodeService.selectRdsNodeList(rdsNode);
        return getDataTable(list);
    }


    /**
     * 导出节点信息列表
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:query')")
    @Log(title = "节点信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RdsNode rdsNode)
    {
        List<RdsNode> list = nodeService.selectRdsNodeList(rdsNode);
        ExcelUtil<RdsNode> util = new ExcelUtil<RdsNode>(RdsNode.class);
        util.exportExcel(response, list, "节点信息数据");
    }

    /**
     * 获取节点信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsservice:query')")
    @GetMapping(value = "/{nodeId}")
    public AjaxResult getInfo(@PathVariable("nodeId") Long nodeId)
    {
        return AjaxResult.success(nodeService.selectRdsNodeByNodeId(nodeId));
    }


}
