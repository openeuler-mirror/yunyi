package com.tongtech.console.enums;

/**
 * 命令执行是否成功
 */
public enum NodeManagerStatusEnum {
    NONE("none"),
    RUNNING("running"),
    STOPPED("stopped");

    private String name;

    NodeManagerStatusEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static NodeManagerStatusEnum parse(String name) {
        if(NONE.name.equals(name)) {
            return NONE;
        }
        else if(RUNNING.name.equals(name)) {
            return RUNNING;
        }
        else if(STOPPED.name.equals(name)) {
            return STOPPED;
        }
        else {
            return null;
        }
    }
}
