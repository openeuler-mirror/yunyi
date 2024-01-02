package com.tongtech.console.mapper;

import java.util.List;
import com.tongtech.console.domain.RdsVersionPkg;

/**
 * 安装包信息Mapper接口
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
public interface RdsVersionPkgMapper
{
    /**
     * 查询安装包信息
     *
     * @param packageId 安装包信息主键
     * @return 安装包信息
     */
    public RdsVersionPkg selectRdsVersionPkgByPackageId(Long packageId);

    /**
     * 查询安装包信息列表
     *
     * @param rdsVersionPkg 安装包信息
     * @return 安装包信息集合
     */
    public List<RdsVersionPkg> selectRdsVersionPkgList(RdsVersionPkg rdsVersionPkg);

    /**
     * 新增安装包信息
     *
     * @param rdsVersionPkg 安装包信息
     * @return 结果
     */
    public int insertRdsVersionPkg(RdsVersionPkg rdsVersionPkg);

    /**
     * 修改安装包信息
     *
     * @param rdsVersionPkg 安装包信息
     * @return 结果
     */
    public int updateRdsVersionPkg(RdsVersionPkg rdsVersionPkg);

    /**
     * 删除安装包信息
     *
     * @param packageId 安装包信息主键
     * @return 结果
     */
    public int deleteRdsVersionPkgByPackageId(Long packageId);

    /**
     * 批量删除安装包信息
     *
     * @param packageIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRdsVersionPkgByPackageIds(Long[] packageIds);
}
