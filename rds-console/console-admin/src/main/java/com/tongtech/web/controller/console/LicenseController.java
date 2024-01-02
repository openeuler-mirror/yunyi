package com.tongtech.web.controller.console;


import com.tongtech.common.annotation.Log;
import com.tongtech.common.config.AppHomeConfig;
import com.tongtech.common.core.domain.AjaxResult;
import com.tongtech.common.enums.BusinessType;
import com.tongtech.common.exception.base.BaseException;
import com.tongtech.common.utils.file.FileUploadUtils;
import com.tongtech.console.domain.CenterLicenseInfo;
import com.tongtech.console.domain.ServiceConfig;
import com.tongtech.console.service.ServiceConfigService;
import com.tongtech.console.utils.CenterLicenseReader;

import com.tongtech.system.service.ISysConfigService;
import com.tongtech.system.service.SysObjectCacheService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.tongtech.common.config.AppHomeConfig.CENTER_LICENSE_FILE;
import static com.tongtech.common.config.AppHomeConfig.CENTER_PATH;
import static com.tongtech.common.constant.CacheConstants.TEMP_CENTER_LICENSE_KEY;
import static com.tongtech.common.constant.ConsoleConstants.*;

@RestController
@RequestMapping("/web-api/console/license")
public class LicenseController {


    @Autowired
    private ServiceConfigService configService;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private SysObjectCacheService redisCache;

    /**
     * 上传RDS授权信息文件，注意只是把安装信息放入到临时文件夹
     */
    @Log(title = "授权信息", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('console:license:edit')")
    @PostMapping("/import")
    public AjaxResult importLicense(MultipartFile file) throws Exception {
        return importToTemp(file);
    }

    /**
     * 上传RDS授权信息文件，在安装引导功能中。
     */
    @Log(title = "授权信息", businessType = BusinessType.IMPORT)
    @PostMapping("/importOnInit")
    public AjaxResult importLicenseOnInit(MultipartFile file) throws Exception {

        boolean sysInitialzed = Boolean.parseBoolean(sysConfigService.selectConfigByKey(CONFIG_SYS_INITIALIZED_KEY));

        if(sysInitialzed) { //如果已经初始化，就抛出异常
            throw new BaseException("初始化完成，不能再调用此接口");
        }

        return importToTemp(file);
    }

    private AjaxResult importToTemp(MultipartFile file) throws Exception {
        //判断文件是否为空
        if (!file.isEmpty()) {
            //文件名长度校验
            int fileNamelength = file.getOriginalFilename().length();
            if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
                return AjaxResult.error("文件名过长，请重新选择，长度不能超过100个字符！");
            }
            //文件名后缀校验
            String filename = file.getOriginalFilename();
            String EXTS = "lic";
            if (!filename.endsWith("." + EXTS)) {
                return AjaxResult.error("文件名后缀有问题，请上传以.lic结尾的文件！");
            }

            //文件大小校验
            long size = file.getSize();
            if (FileUploadUtils.DEFAULT_MAX_SIZE != -1 && size > FileUploadUtils.DEFAULT_MAX_TEXT_SIZE) {
                return AjaxResult.error("文件过大，超过了1MB，请重新选择！");
            }


            String uploadLicenseData = IOUtils.toString(file.getInputStream(), "UTF-8");
            redisCache.setCacheObject(TEMP_CENTER_LICENSE_KEY, uploadLicenseData, 20, TimeUnit.MINUTES); //放入上传的临时缓存

            return AjaxResult.success(loadCenterLic(uploadLicenseData));
        } else {
            return AjaxResult.error("请选择上传文件！");
        }
    }

    /**
     * 把临时Licesne文件移动到正式文件夹（中心节点目录中）让授权信息正式生效
     * @return
     */
    @GetMapping("/add")
    @Log(title = "授权信息", businessType = BusinessType.INSERT)
    public AjaxResult add() {
        String tempLicenseData = redisCache.getCacheObject(TEMP_CENTER_LICENSE_KEY);

        if(tempLicenseData != null) {
            CenterLicenseInfo licInfo = loadCenterLic(tempLicenseData);
            if(licInfo != null && licInfo.getUserName() != null) {

                ServiceConfig conf = configService.selectServiceConfigBy(CENTER_SERVICE_ID, CONFIG_TYPE_LICENSE);
                if(conf == null) {
                    conf = new ServiceConfig();
                    conf.setServiceId(CENTER_SERVICE_ID);
                    conf.setConfContent(tempLicenseData);
                    conf.setConfType(CONFIG_TYPE_LICENSE);
                    conf.setUpdateTime(new Date());
                    configService.insertServiceConfig(conf);
                }
                else {
                    conf.setConfContent(tempLicenseData);
                    conf.setUpdateTime(new Date());
                    configService.updateServiceConfig(conf);
                }

                return AjaxResult.success();
            }
        }

        return AjaxResult.error("未找已上传的授权信息，或授权信息错误！");
    }

    /**
     * 获取用户授权信息   RDS版本管理打包
     * @return
     */
    @GetMapping("/get")
    public AjaxResult getLicense() {
        ServiceConfig conf = configService.selectServiceConfigBy(CENTER_SERVICE_ID, CONFIG_TYPE_LICENSE);
        if(conf != null) {
            CenterLicenseInfo licInfo = loadCenterLic(conf.getConfContent());
            return AjaxResult.success(licInfo);
        }
        else {
            return AjaxResult.error("系统还没有License, 请上传License!");
        }
    }

    /**
     * 获取用户授权信息, 临时上传位置
     * @return
     */
    @GetMapping("/getTemp")
    public AjaxResult getTempLicense() {

        String tempLicenseData = redisCache.getCacheObject(TEMP_CENTER_LICENSE_KEY);

        if(tempLicenseData != null) {
            return AjaxResult.success(loadCenterLic(tempLicenseData));
        }
        else {
            return AjaxResult.error("为找已上传的授权信息");
        }


    }



    private AjaxResult importCenLic(MultipartFile file) throws Exception {
        //判断文件是否为空
        if (!file.isEmpty()) {
            //文件名长度校验
            int fileNamelength = file.getOriginalFilename().length();
            if (fileNamelength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
                return AjaxResult.error("文件名过长，请重新选择，长度不能超过100个字符！");
            }
            //文件名后缀校验
            String filename = file.getOriginalFilename();

            if (!filename.endsWith(".lic")) {
                return AjaxResult.error("文件名后缀有问题，请上传以.lic结尾的文件！");
            }
            //文件大小校验
            long size = file.getSize();
            if (FileUploadUtils.DEFAULT_MAX_SIZE != -1 && size > FileUploadUtils.DEFAULT_MAX_SIZE) {
                return AjaxResult.error("文件过大，超过了50M，请重新选择！");
            }

            //上传文件路径
            File licFile = AppHomeConfig.getAbsoluteFile(CENTER_PATH, CENTER_LICENSE_FILE);
            if (licFile.isFile() && licFile.exists()) //删除存在的原文件
            {
                licFile.delete();
            }

            file.transferTo(licFile);
            return AjaxResult.success("上传成功！");
        } else {
            return AjaxResult.error("请选择上传文件！");
        }
    }


    private CenterLicenseInfo loadCenterLic(String licenseData) {
        CenterLicenseReader licReader = new CenterLicenseReader();
        return licReader.loadLicenseInfo(licenseData);
    }

}
