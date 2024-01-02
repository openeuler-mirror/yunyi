package com.tongtech.console.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tongtech.common.utils.StringUtils;

import java.util.Arrays;
import java.util.Date;

/**
 * 中心节点信息
 */
public class CenterLicenseInfo {

    private static String KEY_USERNAME = "enduser";
    private static String KEY_PRODUCT = "product";
    private static String KEY_TYPE = "licenseType";
    private static String KEY_PROJECT = "project";

    private long type;  //license的类型码，大于等于 100 是企业版，小于100是标准版

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiredTime; //到期时间，如：Tue Jun 14 17:47:30 CST 2022
    private long totalMemory; //内存用量，字节数
    private String totalMemoryDesc; //内存用量的描述，如：20KB,30GB,50GB, 1.2TB
    private String userName; //用户名称, 如："某某有限公司"
    private String product; //产品名称，如："TongRDS 2.2"

    private String project; //产品所使用的项目 如："上海电力公司项目"

    private String typeDesc; //证书类型描述，如："临时证书 90 天授权"

    private String[] context; //license中的context原始数据, 如：[最终用户：东方通, 产品名称：TongRDS 2.2, 证书类型：临时证书 90 天授权]

    public CenterLicenseInfo() {}

    public CenterLicenseInfo(long licenseType, long expiredTime, long totalMemory, String[] context) {
        this.expiredTime = new Date(expiredTime);
        this.totalMemory = totalMemory;
        this.totalMemoryDesc = StringUtils.toReadableSize(totalMemory);
        this.type = licenseType;
        this.context = context;
        for(String item : context) {
            if(item != null && item.length() > 0) {
                int idx = item.indexOf(":");
                String key="" , value = "";
                if(idx > 0) {
                    key = item.substring(0, idx);
                    idx++;
                    if(item.length() > idx) {
                        value = item.substring(idx).trim();
                    }
                }

                if(KEY_USERNAME.equals(key)) {
                    this.userName = value;
                }
                else if(KEY_PRODUCT.equals(key)) {
                    this.product = value;
                }
                else if(KEY_TYPE.equals(key)) {
                    this.typeDesc = value;
                }
                else if(KEY_PROJECT.equals((key))) {
                    this.project = value;
                }
            }
        }
    }

    public Date getExpiredTime() {
        return expiredTime;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    public String getTotalMemoryDesc() {
        return totalMemoryDesc;
    }

    public String getUserName() {
        return userName;
    }

    public String getProduct() {
        return product;
    }

    public long getType() {
        return type;
    }

    public String getProject() {
        return project;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public String[] getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "CenterLicenseInfo{" + "\n" +
                "type=" + type + "\n" +
                ", expiredTime=" + expiredTime + "\n" +
                ", totalMemory=" + totalMemory + "\n" +
                ", totalMemoryDesc='" + totalMemoryDesc + '\'' + "\n" +
                ", userName='" + userName + '\'' + "\n" +
                ", product='" + product + '\'' + "\n" +
                ", project='" + project + '\'' + "\n" +
                ", typeDesc='" + typeDesc + '\'' + "\n" +
                ", context=" + Arrays.toString(context) + "\n" +
                '}';
    }
}
