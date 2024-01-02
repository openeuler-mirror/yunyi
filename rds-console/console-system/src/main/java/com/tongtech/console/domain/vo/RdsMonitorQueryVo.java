package com.tongtech.console.domain.vo;


import java.io.Serializable;
import java.util.Date;

/**
 * 服务监控的查询条件
 */
public class RdsMonitorQueryVo  implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long srcId;

    private Long serviceId;

    private Integer pastSecond; //过去时间，单位秒

    private Long beginCreateSecond; //开始时间(秒)

    private Long endCreateSecond; //结束时间(秒)

    public Long getSrcId() {
        return srcId;
    }

    public void setSrcId(Long srcId) {
        this.srcId = srcId;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getPastSecond() {
        return pastSecond;
    }

    public void setPastSecond(Integer pastSecond) {
        this.pastSecond = pastSecond;
    }

    public Long getBeginCreateSecond() {
        return beginCreateSecond;
    }

    public void setBeginCreateSecond(Long beginCreateSecond) {
        this.beginCreateSecond = beginCreateSecond;
    }

    public Long getEndCreateSecond() {
        return endCreateSecond;
    }

    public void setEndCreateSecond(Long endCreateSecond) {
        this.endCreateSecond = endCreateSecond;
    }
}
