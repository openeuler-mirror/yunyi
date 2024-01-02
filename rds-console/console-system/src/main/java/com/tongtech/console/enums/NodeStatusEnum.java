package com.tongtech.console.enums;

public enum NodeStatusEnum {

    NONE("none", "未安装"),

    START("start", "运行"),

    STARTING("starting", "启动中"),

    START_PART("start-part", "部分启动"),

    STOP("stop", "停止"),

    STOPPING("stopping", "停止中");

    private String name;

    private String info;


    NodeStatusEnum(String name, String info) {
        this.name = name;
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public String getInfo() { return info;  }

    public static NodeStatusEnum parse(String name) {
        if(NONE.name.equals(name)) {
            return NONE;
        }
        else if(START.name.equals(name)) {
            return START;
        }
        else if(STARTING.name.equals(name)) {
            return STARTING;
        }
        else if(STOP.name.equals(name)) {
            return STOP;
        }
        else if(STOPPING.name.equals(name)) {
            return STOPPING;
        }
        else {
            return null;
        }

    }

}
