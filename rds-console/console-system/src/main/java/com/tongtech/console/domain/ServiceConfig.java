package com.tongtech.console.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

/**
 * 服务配置信息对象 cnsl_service_config
 *
 * @author Zhang ChenLong
 * @date 2023-01-11
 */
public class ServiceConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 服务ID */
    private Long serviceId;

    /** 模版类型 */
    private String confType;

    /** 使用的模版ID */
    @Excel(name = "使用的模版ID")
    private Long templateId;

    /** 配置内容 */
    @Excel(name = "配置内容")
    private String confContent;

    public ServiceConfig() {

    }

    public ServiceConfig(Long serviceId, String confType) {
        this.serviceId = serviceId;
        this.confType = confType;
    }

    public void setServiceId(Long serviceId)
    {
        this.serviceId = serviceId;
    }

    public Long getServiceId()
    {
        return serviceId;
    }

    public String getConfType() {
        return confType;
    }

    public void setConfType(String confType) {
        this.confType = confType;
    }

    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateId()
    {
        return templateId;
    }
    public void setConfContent(String confContent)
    {
        this.confContent = confContent;
    }

    public String getConfContent()
    {
        return confContent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("serviceId", getServiceId())
            .append("tempType", getConfType())
            .append("templateId", getTemplateId())
            .append("confContent", getConfContent())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
