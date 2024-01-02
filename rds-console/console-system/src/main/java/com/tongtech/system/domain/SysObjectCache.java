package com.tongtech.system.domain;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 对象缓存对象 sys_object_cache
 *
 * @author Zhang ChenLong
 * @date 2023-04-04
 */
public class SysObjectCache implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** key */
    private String objKey;

    /** value */
    private byte[] objValue;

    /** expire time */
    private Long expireTime;

    private Long createTime;

    public SysObjectCache() {
    }

    public SysObjectCache(String objKey, byte[] objValue) {
        this.objKey = objKey;
        this.objValue = objValue;
        this.createTime = System.currentTimeMillis();
    }

    public SysObjectCache(String objKey, byte[] objValue, Long expireTime) {
        this.objKey = objKey;
        this.objValue = objValue;
        this.expireTime = expireTime;
        this.createTime = System.currentTimeMillis();
    }

    public void setObjKey(String objKey)
    {
        this.objKey = objKey;
    }
    public String getObjKey()
    {
        return objKey;
    }
    public byte[] getObjValue() {
        return objValue;
    }
    public void setObjValue(byte[] objValue) {
        this.objValue = objValue;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("objKey", getObjKey())
            .append("objValue", getObjValue())
            .append("createTime", getCreateTime())
            .append("expireTime", getExpireTime())
            .toString();
    }

}
