package com.tongtech.console.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

/**
 * 节点配置信息对象 cnsl_node_config
 *
 * @author Zhang ChenLong
 * @date 2023-02-27
 */
public class NodeConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 节点ID */
    @Excel(name = "节点ID")
    private Long nodeId;

    /** 使用的模版ID */
    @Excel(name = "使用的模版ID")
    private Long templateId;

    /** 对应模版类型: cnsl_template_type */
    @Excel(name = "对应模版类型: cnsl_template_type")
    private String tempType;

    /** 配置内容 */
    @Excel(name = "配置内容")
    private String confContent;

    public void setNodeId(Long nodeId)
    {
        this.nodeId = nodeId;
    }

    public Long getNodeId()
    {
        return nodeId;
    }
    public void setTemplateId(Long templateId)
    {
        this.templateId = templateId;
    }

    public Long getTemplateId()
    {
        return templateId;
    }
    public void setTempType(String tempType)
    {
        this.tempType = tempType;
    }

    public String getTempType()
    {
        return tempType;
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
            .append("nodeId", getNodeId())
            .append("templateId", getTemplateId())
            .append("tempType", getTempType())
            .append("confContent", getConfContent())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
