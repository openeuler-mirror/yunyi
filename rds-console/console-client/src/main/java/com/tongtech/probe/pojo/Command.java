package com.tongtech.probe.pojo;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * 命令信息
 */
public class Command implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String cmd;
    private String type;

    //节点名称
    private String name;
    private String msg;

    /**
     * 文件名，可以是部署时指定的文件名
     */
    private String file;
    private Map<String, String> config;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command command = (Command) o;
        return cmd.equals(command.cmd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cmd);
    }
}
