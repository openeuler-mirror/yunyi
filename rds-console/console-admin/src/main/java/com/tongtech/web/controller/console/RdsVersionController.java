package com.tongtech.web.controller.console;

import java.util.List;

import com.tongtech.console.domain.vo.RdsVersionVo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tongtech.common.annotation.Log;
import com.tongtech.common.core.controller.BaseController;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.enums.BusinessType;
import com.tongtech.console.domain.RdsVersion;
import com.tongtech.console.service.RdsVersionService;
import com.tongtech.common.core.page.TableDataInfo;

/**
 * 版本信息Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
@RestController
@RequestMapping("/web-api/console/rdsversion")
public class RdsVersionController extends BaseController
{
    @Autowired
    private RdsVersionService rdsVersionService;

    /**
     * 查询版本信息列表
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversion:list')")
    @GetMapping("/list")
    public TableDataInfo list(RdsVersion rdsVersion)
    {
        startPage();
        List<RdsVersion> list = rdsVersionService.selectRdsVersionList(rdsVersion);
        return getDataTable(list);
    }

    /**
     * 查询所有版本，by status
     * @param status  "1" 启用状态，"0" 停用状态， null 查询所有版本
     * @return
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(String status)
    {
        List<RdsVersion> list = rdsVersionService.selectListByStatus(status);
        return AjaxResult.success(list);
    }



    /**
     * 获取版本信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversion:query')")
    @GetMapping(value = "/{versionId}")
    public AjaxResult getInfo(@PathVariable("versionId") Long versionId)
    {
        RdsVersion version = rdsVersionService.selectRdsVersionByVersionId(versionId);
        RdsVersionVo vo = new RdsVersionVo(version, null);

        return AjaxResult.success(vo);
    }

    /**
     * 新增版本信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversion:add')")
    @Log(title = "版本信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RdsVersion rdsVersion)
    {
        rdsVersion.setCreateBy(getUsername());
        return toAjax(rdsVersionService.insertRdsVersion(rdsVersion));
    }

    /**
     * 修改版本信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversion:edit')")
    @Log(title = "版本信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RdsVersion rdsVersion)
    {
        rdsVersion.setUpdateBy(getUsername());
        return toAjax(rdsVersionService.updateRdsVersion(rdsVersion));
    }

    /**
     * 删除版本信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversion:remove')")
    @Log(title = "版本信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{versionIds}")
    public AjaxResult remove(@PathVariable Long[] versionIds)
    {
        return toAjax(rdsVersionService.deleteRdsVersionByVersionIds(versionIds));
    }


    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversion:edit')")
    @Log(title = "版本信息", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody RdsVersion paramVer)
    {
        paramVer.setUpdateBy(getUsername());
        return toAjax(rdsVersionService.updateStatus(paramVer));
    }


    /**
     * 改为默认版本
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversion:edit')")
    @Log(title = "版本信息", businessType = BusinessType.UPDATE)
    @PutMapping("/changeDefault/{versionId}")
    public AjaxResult changeDefault(@PathVariable("versionId") Long versionId)
    {
        return toAjax(rdsVersionService.updateDefaultVersion(versionId, getUsername()));
    }
}
