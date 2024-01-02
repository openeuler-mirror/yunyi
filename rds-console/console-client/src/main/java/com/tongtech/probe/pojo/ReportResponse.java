package com.tongtech.probe.pojo;

import com.alibaba.fastjson2.JSON;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class ReportResponse implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int code;
    private String msg;
    private Map<String, Set<Command>> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Map<String, Set<Command>> getData() {
        return data;
    }

    public void setData(Map<String, Set<Command>> data) {
        this.data = data;
    }

    @Override
    public String toString(){
        return JSON.toJSONString(this);
    }
}
