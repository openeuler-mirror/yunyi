package com.tongtech.common.config;

import com.tongtech.common.enums.DeployEnvEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 读取项目相关配置
 *
 * @author XiaoZhangTongZhi
 */
@Component
@ConfigurationProperties(prefix = "console")
public class UhConsoleConfig
{
    /** 项目名称 */
    private static String name;

    /** 版本 */
    private static String version;

    /** 版权年份 */
    private static String copyrightYear;

    /** 获取地址开关 */
    private static boolean addressEnabled;

    /** 验证码类型 */
    private static String captchaType;

    /** SSH 连接时的超时时间, 单位毫秒 */
    private static int sshConnectTimeout = 10000;

    /** SSH 命令执行通道超时时间, 单位毫秒 */
    private static int sshChannelTimeout = 20000;

    /** 部署环境，"host" 主机，"k8s" 容器云 */
    private static String deployEnv;

    /** 集成嵌入 */
    private static boolean embedding;

    private static DeployEnvEnum deployEnvEnum;

    public static String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public static String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public static String getCopyrightYear()
    {
        return copyrightYear;
    }

    public void setCopyrightYear(String copyrightYear)
    {
        this.copyrightYear = copyrightYear;
    }

    public static boolean isAddressEnabled()
    {
        return addressEnabled;
    }

    public void setAddressEnabled(boolean addressEnabled)
    {
        UhConsoleConfig.addressEnabled = addressEnabled;
    }

    public static String getCaptchaType() {
        return captchaType;
    }

    public void setCaptchaType(String captchaType) {
        UhConsoleConfig.captchaType = captchaType;
    }

    public static int getSshConnectTimeout() {
        return sshConnectTimeout;
    }

    public void setSshConnectTimeout(int sshConnectTimeout) {
        UhConsoleConfig.sshConnectTimeout = sshConnectTimeout;
    }

    public static int getSshChannelTimeout() {
        return sshChannelTimeout;
    }

    public void setSshChannelTimeout(int sshChannelTimeout) {
        UhConsoleConfig.sshChannelTimeout = sshChannelTimeout;
    }

    public static String getDeployEnv() {
        return deployEnv;
    }

    public static DeployEnvEnum getDeployEnvEnum() {
        if(deployEnvEnum == null) {
            deployEnvEnum = DeployEnvEnum.parse(deployEnv);
        }
        return deployEnvEnum;
    }

    public void setDeployEnv(String deployEnv) {
        if(deployEnv != null) {
            UhConsoleConfig.deployEnv = deployEnv.trim().toLowerCase();
        }
        else {
            UhConsoleConfig.deployEnv = null;
        }
    }

    public static boolean isEmbedding() {
        return embedding;
    }

    public void setEmbedding(boolean embedding) {
        UhConsoleConfig.embedding = embedding;
    }
}
