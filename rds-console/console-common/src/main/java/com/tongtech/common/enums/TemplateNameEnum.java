package com.tongtech.common.enums;

/**
 * 模板文件名和文件名枚举类
 */
public enum TemplateNameEnum {
    WORKER("工作节点默认模板", "serverNode.vm"),
    SENTINEL("哨兵节点默认模板", "sentinelNode.vm"),

    CENTER("中心节点默认模板", "cluster.vm"),
    DYNAMIC("服务节点动态配置", "dynamic.vm");

    private String name;
    private String fileName;

    TemplateNameEnum(String name, String fileName) {
        this.name = name;
        this.fileName = fileName;
    }

    public String getName() {
        return name;
    }

    public String getFileName() {
        return fileName;
    }
}
