package com.tongtech.console.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

/**
 * 配置模版对象 cnsl_template_group
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
public class TemplateGroup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模版组ID */
    private Long groupId;

    /** 模版组名称 */
    @Excel(name = "模版组名称")
    private String groupName;

    /** 版本信息，用于显示 */
    private String versions;

    private Long[] versionIds;

    private String templateCount;

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getGroupId()
    {
        return groupId;
    }
    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public String getVersions() {
        return versions;
    }

    public void setVersions(String versions) {
        this.versions = versions;
    }

    public Long[] getVersionIds() {
        return versionIds;
    }

    public void setVersionIds(Long[] versionIds) {
        this.versionIds = versionIds;
    }

    public String getTemplateCount() {
        return templateCount;
    }

    public void setTemplateCount(String templateCount) {
        this.templateCount = templateCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("groupId", getGroupId())
            .append("groupName", getGroupName())
            .append("versions", getVersions())
            .append("versionIds", getVersionIds())
            .append("templateCount", getTemplateCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
