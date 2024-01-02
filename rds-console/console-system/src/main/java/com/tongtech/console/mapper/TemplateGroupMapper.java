package com.tongtech.console.mapper;

import java.util.List;
import com.tongtech.console.domain.TemplateGroup;

/**
 * 配置模版Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
public interface TemplateGroupMapper
{
    /**
     * 查询配置模版
     *
     * @param groupId 配置模版主键
     * @return 配置模版
     */
    public TemplateGroup selectTemplateGroupByGroupId(Long groupId);

    /**
     * 查询版本相关配置模版列表
     * @param versionId 版本ID
     * @return
     */
    public List<TemplateGroup> selectTemplateGroupByVersionId(Long versionId);

    /**
     * 查询配置模版列表
     *
     * @param templateGroup 配置模版
     * @return 配置模版集合
     */
    public List<TemplateGroup> selectTemplateGroupList(TemplateGroup templateGroup);

    /**
     * 新增配置模版
     *
     * @param templateGroup 配置模版
     * @return 结果
     */
    public int insertTemplateGroup(TemplateGroup templateGroup);

    /**
     * 修改配置模版
     *
     * @param templateGroup 配置模版
     * @return 结果
     */
    public int updateTemplateGroup(TemplateGroup templateGroup);


    /**
     * 删除配置模版
     *
     * @param groupId 配置模版主键
     * @return 结果
     */
    public int deleteTemplateGroupByGroupId(Long groupId);

    /**
     * 批量删除配置模版
     *
     * @param groupIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTemplateGroupByGroupIds(Long[] groupIds);
}
