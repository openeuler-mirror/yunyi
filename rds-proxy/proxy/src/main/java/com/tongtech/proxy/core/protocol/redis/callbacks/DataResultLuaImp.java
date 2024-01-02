package com.tongtech.proxy.core.protocol.redis.callbacks;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

public class DataResultLuaImp implements DataResult {

    //private final StringBuilder Buffer;
    private volatile Object Data = null;

    // 设置成static是为了进程内所有连接的一致性
    private volatile int TableId = 1;

    private volatile AclAuthen Acl = null;

    @Override
    public ChannelHandlerContext getSession() {
        return null;
    }

    @Override
    public void init(Object session, List request) {
        this.Data = null;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void setTableId(int id) {
        TableId = id;
    }

    @Override
    public int getTableId() {
        return TableId;
    }

    @Override
    public void setAcl(AclAuthen acl) {
        this.Acl = acl;
    }

    @Override
    public void aclAuth(String cmd, byte[] key) {
        if (this.Acl != null) {
            if (!this.Acl.authentication(cmd, key)) {
                throw DataResult.ACL_FAILED_EXCEPTION;
            }
        }
    }

    @Override
    public synchronized void setOk() throws IOException {
        //Data = new String[]{"+OK"};
        Data = Boolean.TRUE;
        notify();
    }

    @Override
    public synchronized void setOk(long l) throws IOException {
        // TODO Auto-generated method stub
        Data = new Long(l);
        notify();
    }

    @Override
    public synchronized void setErr(int err_code, String msg)
            throws IOException {
        // TODO Auto-generated method stub
        Data = new IllegalStateException(msg);
        notify();
    }

    @Override
    public synchronized ChannelFuture send(String msg) throws IOException {
        // TODO Auto-generated method stub
        Data = msg;
        notify();
        return null;
    }

    @Override
    public synchronized void sendObject(Object o) throws IOException {
        // TODO Auto-generated method stub
        Data = o;
        notify();
    }

    public Object getData() {
        return Data;
    }

    @Override
    public void callback(Object o) throws IOException {
        sendObject(o);
    }
}
