package com.tongtech.console.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

/**
 * 模版组适用版本对象 cnsl_template_group_version
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
public class TemplateGroupVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模版组ID */
    private Long groupId;

    /** 版本ID */
    @Excel(name = "版本ID")
    private Long versionId;

    public TemplateGroupVersion() {}

    public TemplateGroupVersion(Long groupId, Long versionId) {
        this.groupId = groupId;
        this.versionId = versionId;
    }

    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getGroupId()
    {
        return groupId;
    }
    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("groupId", getGroupId())
            .append("versionId", getVersionId())
            .toString();
    }
}
