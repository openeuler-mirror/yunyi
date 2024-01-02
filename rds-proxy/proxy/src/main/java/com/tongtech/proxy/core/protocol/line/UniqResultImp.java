package com.tongtech.proxy.core.protocol.line;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class UniqResultImp implements DataResult {
    private final DataResult Parent;
    private final HashSet<String> ResultData;

    public UniqResultImp(DataResult parent) {
        this.Parent = parent;
        this.ResultData = new HashSet<>();
    }

    @Override
    public ChannelHandlerContext getSession() {
        return Parent.getSession();
    }

    @Override
    public void init(Object session, List request) {
        this.Parent.init(session,request);
        this.ResultData.clear();
    }


    @Override
    public void setOk() throws IOException {
        this.Parent.setOk();
    }

    @Override
    public void setOk(long i) throws IOException {
        this.Parent.setOk(i);
    }

    @Override
    public void setErr(int err_code, String msg) throws IOException {
        this.Parent.setErr(err_code, msg);
    }

    @Override
    public ChannelFuture send(String msg) throws IOException {
        this.Parent.send(msg);
        return null;
    }

    @Override
    public void sendObject(Object o) throws IOException {
        this.Parent.sendObject(o);
    }

    @Override
    public void flush() throws IOException {
        this.Parent.flush();
    }

    @Override
    public void setTableId(int id) {
        this.Parent.setTableId(id);
    }

    @Override
    public int getTableId() {
        return this.Parent.getTableId();
    }

    @Override
    public String toString() {
        return this.Parent.toString();
    }
}
