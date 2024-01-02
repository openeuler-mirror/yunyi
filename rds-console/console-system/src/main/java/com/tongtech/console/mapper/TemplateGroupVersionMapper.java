package com.tongtech.console.mapper;

import java.util.List;
import com.tongtech.console.domain.TemplateGroupVersion;

/**
 * 模版组适用版本Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-16
 */
public interface TemplateGroupVersionMapper
{
    /**
     * 查询模版组适用版本
     *
     * @param groupId 模版组适用版本主键
     * @return 模版组适用版本
     */
    public List<TemplateGroupVersion> selectByGroupId(Long groupId);

    /**
     * 查询模版组适用版本列表
     *
     * @param templateGroupVersion 模版组适用版本
     * @return 模版组适用版本集合
     */
    public List<TemplateGroupVersion> selectList(TemplateGroupVersion templateGroupVersion);

    /**
     * 新增模版组适用版本
     *
     * @param templateGroupVersion 模版组适用版本
     * @return 结果
     */
    public int insertGroupVersion(TemplateGroupVersion templateGroupVersion);

    /**
     * 删除模版组相关的
     *
     * @param groupId 模版组适用版本主键
     * @return 结果
     */
    public int deleteByGroupId(Long groupId);

    /**
     * 删除版本相关的
     * @param versionId
     * @return
     */
    public int deleteByVersionId(Long versionId);

}
