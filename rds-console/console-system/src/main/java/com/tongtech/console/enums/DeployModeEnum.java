package com.tongtech.console.enums;

import com.tongtech.common.utils.StringUtils;

/**
 * RDS 部署模式（单点、哨兵、集群、可伸缩集群）
 */
public enum DeployModeEnum {
    SINGLE("single", "default"),
    SENTINEL("sentinel"),
    SENTINEL_WORKER("sentinel_worker"),
    CLUSTER("cluster"),
    SCALABLE("scalable"),
    CENTER("center");

    private String name;

    private String name2;

    DeployModeEnum(String name) {
        this.name = name;
    }

    DeployModeEnum(String name, String name2) {
        this.name = name;
        this.name2 = name2;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static DeployModeEnum parse(String name) {
        if(StringUtils.isNotEmpty(name)) {
            String iName = name.trim().toLowerCase();
            if (SINGLE.name.equals(iName) || SINGLE.name2.equals(iName)) {
                return SINGLE;
            } else if (SENTINEL_WORKER.name.equals(iName)) {
                return SENTINEL_WORKER;
            } else if (SENTINEL.name.equals(iName)) {
                return SENTINEL;
            } else if (CLUSTER.name.equals(iName)) {
                return CLUSTER;
            } else if (SCALABLE.name.equals(iName)) {
                return SCALABLE;
            } else if (CENTER.name.equals(iName)) {
                return CENTER;
            } else {
                return null;
            }
        }
        else {
            return null;
        }
    }
}
