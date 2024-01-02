package com.tongtech.proxy.core.protocol.redis.callbacks;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.redis.Processer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransationDataResultImp implements DataResult {

    // 设置成static是为了进程内所有连接的一致性
    private volatile int TableId = 0;

    private volatile AclAuthen Acl = null;

    private final DataResult FinalResult;
    private final Processer Processor;
    private final List<List> Requests;
    private final List Response = new ArrayList();

    public TransationDataResultImp(DataResult result, Processer process, List<List> requests) {
        this.Processor = process;
        this.Requests = requests;
        this.FinalResult = result;

        this.Acl = result.getAcl();
        this.TableId = result.getTableId();
//        this.luaCacher = result.getLuaCatcher();

        //
        if (this.Requests == null || this.Requests.size() == 0) {
            try {
                this.FinalResult.sendObject(this.Response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.Processor.process(this.Requests.remove(0), 0, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ChannelHandlerContext getSession() {
        return this.FinalResult.getSession();
    }

    @Override
    public synchronized void init(Object session,List request) {
        // TODO Auto-generated method stub
    }

    @Override
    public synchronized void flush() throws IOException {
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
//        this.luaCacher = new ConcurrentHashMap<>();
    }

    @Override
    public AclAuthen getAcl() {
        return this.Acl;
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
        callback("+OK");
    }

    @Override
    public synchronized void setOk(long l) throws IOException {
        // TODO Auto-generated method stub

        callback(new Long(l));
    }

    @Override
    public synchronized void setErr(int err_code, String msg)
            throws IOException {
        // TODO Auto-generated method stub

        callback("-" + msg);
    }

    @Override
    public synchronized ChannelFuture send(String msg) throws IOException {
        // TODO Auto-generated method stub
        callback(msg);
        return null;
    }

    @Override
    public synchronized void sendObject(Object o) {
        // TODO Auto-generated method stub
        callback(o);
    }

    @Override
    public synchronized void callback(Object o) {
        this.Response.add(o);
        if (this.Requests == null || this.Requests.size() == 0) {
            try {
                this.FinalResult.sendObject(this.Response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                this.Processor.process(this.Requests.remove(0), 0, this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Redis Compatible connection to " + FinalResult.getSession().channel().remoteAddress();
    }
}
