package com.tongtech.proxy.core.protocol.redis.callbacks;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

public class DataResultPluginImp implements DataResult {

    //private final StringBuilder Buffer;
    private volatile Object Data = null;

    // 设置成static是为了进程内所有连接的一致性
    private volatile int TableId = 1;

    private volatile AclAuthen Acl = null;

    public DataResultPluginImp(int db) {
        TableId = db + 1;
    }

    @Override
    public ChannelHandlerContext getSession() {
        return null;
    }

    @Override
    public void init(Object session, List request) {
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
    public void setOk() throws IOException {
//        Data = "+OK";
        Data = new Boolean(true);
    }

    @Override
    public void setOk(long l) throws IOException {
        // TODO Auto-generated method stub
        Data = new Long(l);
    }

    @Override
    public void setErr(int err_code, String msg)
            throws IOException {
        // TODO Auto-generated method stub
        Data = new Exception(msg);
    }

    @Override
    public ChannelFuture send(String msg) throws IOException {
        // TODO Auto-generated method stub
        Data = msg;
        return null;
    }

    @Override
    public void sendObject(Object o) throws IOException {
        // TODO Auto-generated method stub
        Data = o;
    }

    public Object getData() {
        return Data;
    }
}
