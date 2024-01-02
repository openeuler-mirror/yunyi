package com.tongtech.console.enums;

/**
 * 节点管理器管理模式
 */
public enum NodeManagerModeEnum {

    PROBE("probe"),  //手动联机

    SSH("ssh"),      //SSH联机

    INNER("inner"),  //控制台内嵌

    K8S("k8s");      //Kubernetes

    private String name;

    NodeManagerModeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static NodeManagerModeEnum parse(String name) {
        if(PROBE.name.equals(name)) {
            return PROBE;
        }
        else if(SSH.name.equals(name)) {
            return SSH;
        }
        else if(INNER.name.equals(name)) {
            return INNER;
        }
        else if(K8S.name.equals(name)) {
            return K8S;
        }
        else {
            return null;
        }
    }
}
