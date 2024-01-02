package com.tongtech.console.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

/**
 * 配置模版对象 cnsl_template
 *
 * @author Zhang ChenLong
 * @date 2023-01-15
 */
public class Template extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模版ID */
    private Long templateId;

    /** 模版组ID */
    private Long groupId;

    /** 模版名称 */
    @Excel(name = "模版名称")
    private String tempName;

    /** 模版内容 */
    @Excel(name = "模版内容")
    private String tempContent;

    /** 模版类型 */
    @Excel(name = "模版类型")
    private String tempType;

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateId()
    {
        return templateId;
    }
    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getGroupId()
    {
        return groupId;
    }
    public void setTempName(String tempName)
    {
        this.tempName = tempName;
    }

    public String getTempName()
    {
        return tempName;
    }
    public void setTempContent(String tempContent)
    {
        this.tempContent = tempContent;
    }

    public String getTempContent()
    {
        return tempContent;
    }
    public void setTempType(String tempType)
    {
        this.tempType = tempType;
    }

    public String getTempType()
    {
        return tempType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("templateId", getTemplateId())
            .append("groupId", getGroupId())
            .append("tempName", getTempName())
            .append("tempContent", getTempContent())
            .append("tempType", getTempType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
