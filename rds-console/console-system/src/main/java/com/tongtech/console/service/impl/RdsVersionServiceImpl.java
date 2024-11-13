package com.tongtech.console.service.impl;

import java.util.List;

import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.utils.DateUtils;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.tongtech.console.mapper.RdsVersionMapper;
import com.tongtech.console.domain.RdsVersion;
import com.tongtech.console.service.RdsVersionService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 版本信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-01-12
 */
@Service
public class RdsVersionServiceImpl implements RdsVersionService
{
    @Resource
    private RdsVersionMapper rdsVersionMapper;

    /**
     * 查询版本信息
     *
     * @param versionId 版本信息主键
     * @return 版本信息
     */
    @Override
    public RdsVersion selectRdsVersionByVersionId(Long versionId)
    {
        return rdsVersionMapper.selectRdsVersionByVersionId(versionId);
    }

    @Override
    public List<RdsVersion> selectListByStatus(String status) {
        return rdsVersionMapper.selectListByStatus(status);
    }

    @Override
    public RdsVersion selectDefaultVersion() {
        return rdsVersionMapper.selectDefaultVersion();
    }

    /**
     * 查询版本信息列表
     *
     * @param rdsVersion 版本信息
     * @return 版本信息
     */
    @Override
    public List<RdsVersion> selectRdsVersionList(RdsVersion rdsVersion)
    {
        return rdsVersionMapper.selectRdsVersionList(rdsVersion);
    }

    /**
     * 新增版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    @Override
    @Transactional
    public int insertRdsVersion(RdsVersion rdsVersion)
    {
        if(rdsVersion.isDefaultVersion()) {
            //新加默认版本，首先把原默认版本设置为非。
            rdsVersionMapper.updateToNoDefaultVersion(rdsVersion.getCreateBy());
        }

        rdsVersion.setCreateTime(DateUtils.getNowDate());
        return rdsVersionMapper.insertRdsVersion(rdsVersion);
    }

    /**
     * 修改版本信息
     *
     * @param rdsVersion 版本信息
     * @return 结果
     */
    @Override
    @Transactional
    public int updateRdsVersion(RdsVersion rdsVersion)
    {
        RdsVersion orgVer = rdsVersionMapper.selectRdsVersionByVersionId(rdsVersion.getVersionId());
        if(rdsVersion.isDefaultVersion() != orgVer.isDefaultVersion()) {
            if(orgVer.isDefaultVersion() == true) {
                throw new ServiceException("默认版本不能被停用！请选择非默认版本，设置为默认版！");
            }
            else {
                //修改非默认版本》默认版本，首先把原默认版本设置为非。
                rdsVersionMapper.updateToNoDefaultVersion(rdsVersion.getUpdateBy());
            }
        }

        rdsVersion.setUpdateTime(DateUtils.getNowDate());
        return rdsVersionMapper.updateRdsVersion(rdsVersion);
    }

    /**
     * 批量删除版本信息
     *
     * @param versionIds 需要删除的版本信息主键
     * @return 结果
     */
    @Override
    @Transactional
    public int deleteRdsVersionByVersionIds(Long[] versionIds)
    {
        int count = 0;
        if(versionIds != null) {
            for(Long vid : versionIds) {
                count +=deleteRdsVersionByVersionId(vid);
            }
        }

        return count;
    }

    /**
     * 删除版本信息信息
     *
     * @param versionId 版本信息主键
     * @return 结果
     */
    @Override
    public int deleteRdsVersionByVersionId(Long versionId)
    {
        return rdsVersionMapper.deleteRdsVersionByVersionId(versionId);
    }

    @Override
    public int updateStatus(RdsVersion rdsVersion) {
        RdsVersion orgVer = rdsVersionMapper.selectRdsVersionByVersionId(rdsVersion.getVersionId());
        if( orgVer.isDefaultVersion() == true && "0".equals(rdsVersion.getStatus()) ) {
            throw new ServiceException("不能停用默认版本！");
        }
        else {
            rdsVersion.setUpdateTime(DateUtils.getNowDate());
            return rdsVersionMapper.updateStatus(rdsVersion);
        }
    }

    @Override
    @Transactional
    public int updateDefaultVersion(Long versionId, String updateUsername) {
        //找到所有 defaultVersion = true 的记录，全部变更为 defaultVersiion = false
        rdsVersionMapper.updateToNoDefaultVersion(updateUsername);

        //设置新的 defaultVersiion
        RdsVersion rdsVersion  = new RdsVersion();
        rdsVersion.setVersionId(versionId);
        rdsVersion.setDefaultVersion(true);
        rdsVersion.setStatus("1");
        rdsVersion.setUpdateTime(DateUtils.getNowDate());
        return rdsVersionMapper.updateDefaultVersion(rdsVersion);
    }


}
