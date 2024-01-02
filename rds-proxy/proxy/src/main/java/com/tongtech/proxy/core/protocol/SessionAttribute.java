package com.tongtech.proxy.core.protocol;

import com.tongtech.proxy.core.slices.ResultCallback;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Session对应的个性化属性类，要求每个getter和setter方法均要保证线程安全
 */
public class SessionAttribute {
    private DataResult dataResult = null;
    private String clientName = null;
    private long clientId = 0;
    // 当前连接事务处理用的命令列表
    private List transactionList = null;
    // 当前session是否认证通过（或不需要认证）
    private boolean authed = false;
    InetSocketAddress remoteAddress = null;
    private ResultCallback Callback = null;

    public synchronized DataResult getDataResult() {
        return dataResult;
    }

    public synchronized void setDataResult(DataResult dataResult) {
        this.dataResult = dataResult;
    }

    public synchronized String getClientName() {
        return clientName;
    }

    public synchronized void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public synchronized long getClientId() {
        return clientId;
    }

    public synchronized void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public synchronized InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public synchronized void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public synchronized List getTransactionList() {
        return transactionList;
    }

    public synchronized void setTransactionList(List transactionList) {
        this.transactionList = transactionList;
    }

    public synchronized boolean isAuthed() {
        return authed;
    }

    public synchronized void setAuthed(boolean authed) {
        this.authed = authed;
    }

    public synchronized ResultCallback getCallback() {
        return Callback;
    }

    public synchronized void setCallback(ResultCallback callback) {
        Callback = callback;
    }
}
