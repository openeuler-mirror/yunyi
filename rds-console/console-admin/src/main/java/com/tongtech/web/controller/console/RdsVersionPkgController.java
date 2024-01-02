package com.tongtech.web.controller.console;

import java.io.File;
import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.tongtech.common.config.AppHomeConfig;
import com.tongtech.common.exception.base.BaseException;
import com.tongtech.common.utils.file.FileUploadUtils;
import com.tongtech.common.utils.file.FileUtils;
import com.tongtech.common.utils.file.MimeTypeUtils;
import org.springframework.http.MediaType;
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
import com.tongtech.console.domain.RdsVersionPkg;
import com.tongtech.console.service.RdsVersionPkgService;
import com.tongtech.common.utils.poi.ExcelUtil;
import com.tongtech.common.core.page.TableDataInfo;
import org.springframework.web.multipart.MultipartFile;

import static com.tongtech.common.config.AppHomeConfig.PACKAGE_VERSION_PATH;

/**
 * 安装包信息Controller
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
@RestController
@RequestMapping("/web-api/console/rdsversionpkg")
public class RdsVersionPkgController extends BaseController
{
    @Autowired
    private RdsVersionPkgService rdsVersionPkgService;

    /**
     * 查询安装包信息列表
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:list')")
    @GetMapping("/list")
    public TableDataInfo list(RdsVersionPkg rdsVersionPkg)
    {
        startPage();
        List<RdsVersionPkg> list = rdsVersionPkgService.selectRdsVersionPkgList(rdsVersionPkg);
        return getDataTable(list);
    }


    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:add')")
    @Log(title = "安装包信息", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(MultipartFile file, Long versionId, String pkgType) throws Exception
    {
        if(versionId == null || pkgType == null) {
            throw new BaseException("Parameter Error for RdsVersionPkg upload!");
        }

        RdsVersionPkg paramPkg = new RdsVersionPkg();
        paramPkg.setVersionId(versionId);
        paramPkg.setPkgType(pkgType);
        List<RdsVersionPkg> resList = rdsVersionPkgService.selectRdsVersionPkgList(paramPkg);
        if(resList.size() >= 1) {
            return AjaxResult.error("每一种包类型只能上载一安装包，请勿重复上载！");
        }

        String fileName = FileUploadUtils.uploadToDir(AppHomeConfig.getAbsoluteFile(PACKAGE_VERSION_PATH, "v" + versionId), file, MimeTypeUtils.GZIP_EXTENSION);

        RdsVersionPkg pkg = new RdsVersionPkg();
        pkg.setVersionId(versionId);
        pkg.setPkgName(getName(fileName));
        pkg.setPkgType(pkgType);
        pkg.setCreateBy(getUsername());
        pkg.setFileName(fileName);
        pkg.setFileSize(file.getSize());

        System.out.println("insertRdsVersionPkg: " + pkg);
        rdsVersionPkgService.insertRdsVersionPkg(pkg);

        return AjaxResult.success("文件上传成功");
    }

    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:query')")
    @PostMapping(value = "/download/{packageId}")
    public void download(HttpServletResponse response, @PathVariable("packageId") Long packageId)
    {
        try
        {

            RdsVersionPkg pkg = rdsVersionPkgService.selectRdsVersionPkgByPackageId(packageId);
            File downloadFile = rdsVersionPkgService.getPkgFile(pkg);

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, pkg.getFileName());
            FileUtils.writeBytes(downloadFile, response.getOutputStream());
        }
        catch (Exception e)
        {
            logger.error("下载文件失败", e);
        }
    }



    /**
     * 导出安装包信息列表
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:export')")
    @Log(title = "安装包信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, RdsVersionPkg rdsVersionPkg)
    {
        List<RdsVersionPkg> list = rdsVersionPkgService.selectRdsVersionPkgList(rdsVersionPkg);
        ExcelUtil<RdsVersionPkg> util = new ExcelUtil<RdsVersionPkg>(RdsVersionPkg.class);
        util.exportExcel(response, list, "安装包信息数据");
    }

    /**
     * 获取安装包信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:query')")
    @GetMapping(value = "/{packageId}")
    public AjaxResult getInfo(@PathVariable("packageId") Long packageId)
    {
        return AjaxResult.success(rdsVersionPkgService.selectRdsVersionPkgByPackageId(packageId));
    }

    /**
     * 新增安装包信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:add')")
    @Log(title = "安装包信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody RdsVersionPkg rdsVersionPkg)
    {
        return toAjax(rdsVersionPkgService.insertRdsVersionPkg(rdsVersionPkg));
    }

    /**
     * 修改安装包信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:edit')")
    @Log(title = "安装包信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody RdsVersionPkg rdsVersionPkg)
    {
        return toAjax(rdsVersionPkgService.updateRdsVersionPkg(rdsVersionPkg));
    }

    /**
     * 删除安装包信息
     */
    @PreAuthorize("@ss.hasPermi('console:rdsversionpkg:remove')")
    @Log(title = "安装包信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{packageIds}")
    public AjaxResult remove(@PathVariable Long[] packageIds)
    {
        return toAjax(rdsVersionPkgService.deleteRdsVersionPkgByPackageIds(packageIds));
    }

    private String getName(String fileName) {
        String fname = fileName.trim();
        int end = fname.indexOf(".tar.gz");
        if(end <= 0) {
            end = fname.indexOf(".zip");
        }
        if(end <= 0) {
            end = fname.indexOf(".tgz");
        }

        if(end > 0) {
            return fname.substring(0, end);
        }
        else {
            return fname;
        }
    }
}
