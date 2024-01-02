package com.tongtech.console.mapper;

import java.util.List;
import com.tongtech.console.domain.RdsVersion;

/**
 * 版本信息Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
public interface RdsVersionMapper
{
    /**
     * 查询版本信息
     *
     * @param versionId 版本信息主键
     * @return 版本信息
     */
    public RdsVersion selectRdsVersionByVersionId(Long versionId);

    /**
     * 查询版本信息列表
     *
     * @param rdsVersion 版本信息
     * @return 版本信息集合
     */
    public List<RdsVersion> selectRdsVersionList(RdsVersion rdsVersion);

    /**
     * 查询默认版本
     * @return 版本信息集合
     */
    public RdsVersion selectDefaultVersion();

    /**
     * 查询所有版本，by status
     * @param status  "1" 启用状态，"0" 停用状态， null 查询所有版本
     * @return
     */
    public List<RdsVersion> selectListByStatus(String status);

    /**
     * 新增版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    public int insertRdsVersion(RdsVersion rdsVersion);

    /**
     * 修改版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    public int updateRdsVersion(RdsVersion rdsVersion);

    /**
     * 删除版本信息
     *
     * @param versionId 版本信息主键
     * @return 结果
     */
    public int deleteRdsVersionByVersionId(Long versionId);

    /**
     * 批量删除版本信息
     *
     * @param versionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRdsVersionByVersionIds(Long[] versionIds);

    /**
     * 更新版本状态
     * @param rdsVersion
     * @return
     */
    public int updateStatus(RdsVersion rdsVersion);

    /**
     * 更新 defaultVersion
     * @return
     */
    public int updateDefaultVersion(RdsVersion rdsVersion);

    /**
     * 找到所有 defaultVersion = true 的记录，全部变更为 defaultVersiion = false
     * @param username 更新的用户名称
     * @return
     */
    public int updateToNoDefaultVersion(String username);

    /**
     * 清除表中default_group_id = groupId 的列值，设置为Null。
     * @param groupId
     * @return
     */
    public int clearDefaultGroupId(Long groupId);
}
