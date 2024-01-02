package com.tongtech.console.domain;

import com.tongtech.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 中心节点统计信息的原始报文对象 cnsl_center_stat_src
 *
 * @author Zhang ChenLong
 * @date 2023-03-15
 */
public class CenterStatSrc extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 源ID */
    private Long srcId;

    private String centersSrc;

    private String servicesSrc;

    private String sentinelsSrc;

    private String licenseSrc;

    /** 执行时长(毫秒) */
    private Long duration;

    public CenterStatSrc() {}

    public CenterStatSrc(String centersSrc, String servicesSrc, String sentinelsSrc, String licenseSrc, Long duration, Date createTime) {
        this.centersSrc = centersSrc;
        this.servicesSrc = servicesSrc;
        this.sentinelsSrc = sentinelsSrc;
        this.licenseSrc = licenseSrc;
    }

    public void setSrcId(Long srcId)
    {
        this.srcId = srcId;
    }

    public Long getSrcId()
    {
        return srcId;
    }

    public void setDuration(Long duration)
    {
        this.duration = duration;
    }

    public Long getDuration()
    {
        return duration;
    }

    public String getCentersSrc() {
        return centersSrc;
    }

    public void setCentersSrc(String centersSrc) {
        this.centersSrc = centersSrc;
    }

    public String getServicesSrc() {
        return servicesSrc;
    }

    public void setServicesSrc(String servicesSrc) {
        this.servicesSrc = servicesSrc;
    }

    public String getSentinelsSrc() {
        return sentinelsSrc;
    }

    public void setSentinelsSrc(String sentinelsSrc) {
        this.sentinelsSrc = sentinelsSrc;
    }

    public String getLicenseSrc() {
        return licenseSrc;
    }

    public void setLicenseSrc(String licenseSrc) {
        this.licenseSrc = licenseSrc;
    }

    @Override
    public String toString() {
        return "CenterStatSrc{" +
                "srcId=" + srcId +
                ", centersSrc='" + centersSrc + '\'' +
                ", servicesSrc='" + servicesSrc + '\'' +
                ", sentinelsSrc='" + sentinelsSrc + '\'' +
                ", licenseSrc='" + licenseSrc + '\'' +
                ", duration=" + duration +
                '}';
    }
}
