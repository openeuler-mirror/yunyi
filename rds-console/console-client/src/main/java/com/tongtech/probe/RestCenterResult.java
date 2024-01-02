package com.tongtech.probe;

import java.util.Date;
import java.util.List;

public class RestCenterResult <T> {

    private List<T> listData;

    private T data;

    private String src;

    private Long duration;

    private Date createTime;

    private String authKey;

    public RestCenterResult(String src, List<T>  listData, Date createTime, Long durationy) {
        this(src, listData, createTime, durationy, null);
    }
    public RestCenterResult(String src, List<T>  listData, Date createTime, Long duration, String authKey) {
        this.src = src;
        this.listData = listData;
        this.createTime = createTime;
        this.duration = duration;
        this.authKey = authKey;
    }

    public RestCenterResult(String src, T  data, Date createTime, Long duration) {
        this(src,  data, createTime, duration, null);
    }
    public RestCenterResult(String src, T  data, Date createTime, Long duration, String authKey) {
        this.src = src;
        this.data = data;
        this.createTime = createTime;
        this.duration = duration;
        this.authKey = authKey;
    }


    public List<T> getListData() {
        return listData;
    }

    public T getData() {
        return data;
    }

    public String getSrc() {
        return src;
    }

    public Long getDuration() {
        return duration;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public String getAuthKey() {
        return authKey;
    }
}
