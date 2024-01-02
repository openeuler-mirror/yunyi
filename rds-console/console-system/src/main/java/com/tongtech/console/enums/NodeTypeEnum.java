package com.tongtech.console.enums;

public enum NodeTypeEnum {
    WORKER("worker", "工作节点"),
    SENTINEL("sentinel", "哨兵节点"),
    CENTER("center", "中心节点"),
    PROXY("proxy", "代理节点");


    private String name;

    private String info;


    NodeTypeEnum(String name, String info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public String getInfo() { return info;  }

    public static NodeTypeEnum parse(String name) {
        if(WORKER.name.equals(name)) {
            return WORKER;
        }
        else if(SENTINEL.name.equals(name)) {
            return SENTINEL;
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
