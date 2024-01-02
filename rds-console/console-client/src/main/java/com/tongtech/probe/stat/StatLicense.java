package com.tongtech.probe.stat;

import java.io.Serializable;

public class StatLicense implements Serializable {
    private static final long serialVersionUID = 1L;

    /* 仿HTTP中的status地返回码， 200为正常，400以上是有问题 */
    private Integer code;
    /* 正常返回"ok", 错误返回错误信息 */
    private String msg;
    /* 总共可以用内存 */
    private Long total;
    /* 已用内存 */
    private Long used;
    /* 已用内存百分比（字符串格式，如：39%） */
    private String percent;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getUsed() {
        return used;
    }

    public void setUsed(Long used) {
        this.used = used;
    }

    public String getPercent() {
        return percent;
    }

    public void setPercent(String percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "StatLicense{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", total=" + total +
                ", used=" + used +
                ", percent='" + percent + '\'' +
                '}';
    }

}
