package com.tongtech.common.enums;

import com.tongtech.common.utils.StringUtils;

/**
 * RDS 部署模式（单点、哨兵、集群、可伸缩集群）
 */
public enum DeployEnvEnum {
    /**主机/虚机 */
    HOST("host"),
    /** K8S容器云 */
    K8S("k8s");

    private String name;

    DeployEnvEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static DeployEnvEnum parse(String name) {
        if(StringUtils.isNotEmpty(name)) {
            String iName = name.toLowerCase();
            if (HOST.name.equals(iName)) {
                return HOST;
            } else if (K8S.name.equals(iName)) {
                return K8S;
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }
}
