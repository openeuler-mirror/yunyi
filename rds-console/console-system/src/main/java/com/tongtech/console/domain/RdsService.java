package com.tongtech.console.domain;

import com.tongtech.common.exception.ServiceException;

import com.tongtech.console.enums.DeployModeEnum;
import com.tongtech.probe.stat.StatService;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.tongtech.common.annotation.Excel;
import com.tongtech.common.core.domain.BaseEntity;

import java.util.List;

/**
 * RDS服务对象 cnsl_rds_service
 *
 * @author Zhang ChenLong
 * @date 2023-01-26
 */
public class RdsService extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 服务ID */
    private Long serviceId;

    /** 哨兵服务ID */
    private Long sentinelServiceId;

    /** 服务名称 */
    @Excel(name = "服务名称")
    private String serviceName;

    /** 服务地址 */
    private String hostAddress;

    /** 管理端口 */
    private Integer adminPort;

    /** 是否可以手工维护 */
    private boolean manualAdmin;

    /** 部署模式 */
    @Excel(name = "部署模式")
    private String deployMode;

    /** 部署模式 */
    private DeployModeEnum deployModeEnum;

    /** 认证方式 */
    private Integer secureMode;

    /** 版本 */
    private Long versionId;

    /** 模版组ID */
    private Long groupId;

    /** 认证密码 */
    private String password;

    /** 版本显示名称，显示专用 */
    private String versionDesc;

    /** 模版组名称，显示专用 */
    private String groupName;

    /**
     * 前端传入的变化的属性名称列表（主要对比提交前后的变化属性）
     */
    private List<String> changedProps;


    public RdsService() {}

    public RdsService(StatService statServ, RdsVersion version) {
        this(statServ.getName(), DeployModeEnum.parse(statServ.getType()), version);
    }

    public RdsService(String serviceName, DeployModeEnum deployMode, RdsVersion version) {
        this.serviceName = serviceName;
        this.manualAdmin = false;
        this.deployModeEnum = deployMode;
        if(this.deployModeEnum != null) {
            this.deployMode = deployModeEnum.getName();
        }
        else {
            throw new ServiceException("RdsService(serviceName:" + serviceName + ") creating error!  deployMode is null!");
        }

        this.secureMode = 0; //secureMode 会在节点信息解析时被赋值，这里设置为0(不需要认证)
        this.versionId = version.getVersionId();
        this.groupId = version.getDefaultGroupId();
    }


    public void setServiceId(Long serviceId)
    {
        this.serviceId = serviceId;
    }

    public Long getServiceId()
    {
        return serviceId;
    }

    public Long getSentinelServiceId() {
        return sentinelServiceId;
    }

    public void setSentinelServiceId(Long sentinelServiceId) {
        this.sentinelServiceId = sentinelServiceId;
    }

    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public String getServiceName()
    {
        return serviceName;
    }
    public void setDeployMode(String deployMode)
    {
        this.deployMode = deployMode;
    }

    public String getDeployMode()
    {
        return deployMode;
    }

    public DeployModeEnum getDeployModeEnum() {
        if(this.deployModeEnum == null) {
            this.deployModeEnum = DeployModeEnum.parse(this.deployMode);
        }

        return this.deployModeEnum;
    }

    public void setSecureMode(Integer secureMode)
    {
        this.secureMode = secureMode;
    }

    public Integer getSecureMode()
    {
        return secureMode;
    }
    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getVersionId()
    {
        return versionId;
    }
    public void setGroupId(Long groupId)
    {
        this.groupId = groupId;
    }

    public Long getGroupId()
    {
        return groupId;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getPassword()
    {
        return password;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getChangedProps() {
        return changedProps;
    }

    public void setChangedProps(List<String> changedProps) {
        this.changedProps = changedProps;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public Integer getAdminPort() {
        return adminPort;
    }

    public void setAdminPort(Integer adminPort) {
        this.adminPort = adminPort;
    }

    public boolean isManualAdmin() {
        return manualAdmin;
    }

    public void setManualAdmin(boolean manualAdmin) {
        this.manualAdmin = manualAdmin;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("serviceId", getServiceId())
            .append("sentinelServiceId", getSentinelServiceId())
            .append("serviceName", getServiceName())
            .append("hostAddress", getHostAddress())
            .append("adminPort", getAdminPort())
            .append("deployMode", getDeployMode())
            .append("secureMode", getSecureMode())
            .append("versionId", getVersionId())
            .append("versionDesc", getVersionDesc())
            .append("groupId", getGroupId())
            .append("password", getPassword())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
