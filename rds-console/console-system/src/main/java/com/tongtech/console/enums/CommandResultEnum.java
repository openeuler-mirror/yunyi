package com.tongtech.console.enums;

/**
 * 命令执行是否成功
 */
public enum CommandResultEnum {
    DONE("done"),
    FAILED("failed");

    private String name;

    CommandResultEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static CommandResultEnum parse(String name) {
        if(DONE.name.equals(name)) {
            return DONE;
        }
        else if(FAILED.name.equals(name)) {
            return FAILED;
        }
        else {
            return null;
        }
    }
}
