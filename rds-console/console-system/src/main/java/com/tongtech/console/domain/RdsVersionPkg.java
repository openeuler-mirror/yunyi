package com.tongtech.console.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

import static com.tongtech.common.utils.StringUtils.toReadableSize;

/**
 * 安装包信息对象 cnsl_rds_version_pkg
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
public class RdsVersionPkg extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 安装包ID */
    private Long packageId;

    /** 版本ID */
    @Excel(name = "版本ID")
    private Long versionId;

    /** 包名称 */
    @Excel(name = "包名称")
    private String pkgName;

    /** 包类类型 */
    @Excel(name = "包类类型")
    private String pkgType;

    /** 文件名称 */
    private String fileName;

    /** 文件大小 */
    @Excel(name = "文件大小")
    private Long fileSize;

    public void setPackageId(Long packageId)
    {
        this.packageId = packageId;
    }

    public Long getPackageId()
    {
        return packageId;
    }
    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getVersionId()
    {
        return versionId;
    }
    public void setPkgName(String pkgName)
    {
        this.pkgName = pkgName;
    }

    public String getPkgName()
    {
        return pkgName;
    }
    public void setPkgType(String pkgType)
    {
        this.pkgType = pkgType;
    }

    public String getPkgType()
    {
        return pkgType;
    }
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }
    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public String getFileSizeDesc() {
        if(fileSize != null) {
            return toReadableSize(fileSize);
        }
        else {
            return "";
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("packageId", getPackageId())
            .append("versionId", getVersionId())
            .append("pkgName", getPkgName())
            .append("pkgType", getPkgType())
            .append("fileName", getFileName())
            .append("fileSize", getFileSize())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
