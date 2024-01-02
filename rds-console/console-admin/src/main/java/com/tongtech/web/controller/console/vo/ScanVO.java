package com.tongtech.web.controller.console.vo;

import redis.clients.jedis.ScanResult;

import java.util.List;


public class ScanVO {

    public ScanVO() {
    }

    public ScanVO(Boolean isFirstPage, String endpoint, ScanResult<String> scanResult) {
        this.isFirstPage = isFirstPage;
        this.endpoint = endpoint;
        this.scanResult = scanResult;
    }

    private Boolean isFirstPage;

    private String endpoint;

    private ScanResult<String> scanResult;



    public Boolean getFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(Boolean firstPage) {
        isFirstPage = firstPage;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public ScanResult<String> getScanResult() {
        return scanResult;
    }

//    public void setScanResult(ScanResult<String> scanResult) {
//        this.scanResult = scanResult;
//    }
//

    /**
     * 在原来Result基础上在加入新的结果，返回Result中数据的总数量。
     * @param newResult
     * @return 返回Result中数据的总数量。
     */
    public int addScanResult(ScanResult<String> newResult) {
        if(this.scanResult == null) {
            this.scanResult = newResult;
        }
        else {
            List<String> lastResult = this.scanResult.getResult();
            lastResult.addAll(newResult.getResult());
            this.scanResult = new ScanResult<String>(newResult.getCursorAsBytes(), lastResult);
        }

        return this.scanResult.getResult().size();
    }
}
