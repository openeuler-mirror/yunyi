package com.tongtech.console.mapper;

import java.util.List;
import com.tongtech.console.domain.Template;

/**
 * 配置模版Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-15
 */
public interface TemplateMapper
{
    /**
     * 查询配置模版
     *
     * @param templateId 配置模版主键
     * @return 配置模版
     */
    public Template selectTemplateByTemplateId(Long templateId);

    /**
     * 查询配置模版列表
     *
     * @param template 配置模版
     * @return 配置模版集合
     */
    public List<Template> selectTemplateList(Template template);

    /**
     * 根据groupId 和 tempType 找到对应的模版
     * @param template
     * @return
     */
    public Template selectTemplateByGroupType(Template template);

    /**
     * 新增配置模版
     *
     * @param template 配置模版
     * @return 结果
     */
    public int insertTemplate(Template template);

    /**
     * 修改配置模版
     *
     * @param template 配置模版
     * @return 结果
     */
    public int updateTemplate(Template template);

    /**
     * 删除配置模版
     *
     * @param templateId 配置模版主键
     * @return 结果
     */
    public int deleteTemplateByTemplateId(Long templateId);

    /**
     * 批量删除配置模版
     *
     * @param templateIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTemplateByTemplateIds(Long[] templateIds);
}
