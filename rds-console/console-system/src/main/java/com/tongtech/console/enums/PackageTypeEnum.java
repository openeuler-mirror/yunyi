package com.tongtech.console.enums;

/**
 * 安装包类型
 */
public enum PackageTypeEnum {

    WORKER("worker"), //工作哨兵节点
    CENTER("center"),  //中心节点
    PROXY("proxy");    //代理节点

    private String name;

    PackageTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PackageTypeEnum parseByNodeType(String nodeType) {
        if("worker".equals(nodeType) || "sentinel".equals(nodeType)) {
            return WORKER;
        }
        else if("center".equals(nodeType)) {
            return CENTER;
        }
        else if("proxy".equals(nodeType)) {
            return PROXY;
        }
        else {
            return null;
        }

    }

    public static PackageTypeEnum parse(String name) {
        if(WORKER.name.equals(name)) {
            return WORKER;
        }
        else if(CENTER.name.equals(name)) {
            return CENTER;
        }
        else if(PROXY.name.equals(name)) {
            return PROXY;
        }
        else {
            return null;
        }

    }

}
