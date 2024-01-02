package com.tongtech.proxy.core.protocol.redis.callbacks;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.SessionAttribute;
import com.tongtech.proxy.core.protocol.SlowLogs;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tongtech.proxy.core.StaticContent.CachedSessionAttributes;

public class DefaultDataResultImp implements DataResult {

    Log logger = ProxyConfig.getServerLog();

    // private boolean HaveData;
    //private String Session;
    private final ChannelHandlerContext Writer;
    //private final StringBuilder Buffer;
    private final ArrayList<String> Data = new ArrayList<>();

    private volatile int TableId = 0;

    private volatile AclAuthen Acl = null;

    private volatile long Timestamp;

    private volatile List Request;

    public DefaultDataResultImp(ChannelHandlerContext ctx) {
        this.Writer = ctx;
    }

    @Override
    public ChannelHandlerContext getSession() {
        return this.Writer;
    }

    @Override
    public synchronized void init(Object session, List request) {
        // TODO Auto-generated method stub
        Data.clear();
        Timestamp = System.currentTimeMillis();
        Request = request;
        //Buffer.setLength(0);
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
        Writer.writeAndFlush("+OK");
    }

    @Override
    public synchronized void setOk(long l) throws IOException {
        // TODO Auto-generated method stub

        Writer.writeAndFlush(new Long(l));
    }

    @Override
    public synchronized void setErr(int err_code, String msg)
            throws IOException {
        // TODO Auto-generated method stub

        Writer.writeAndFlush("-" + msg);
    }

    @Override
    public synchronized ChannelFuture send(String msg) throws IOException {
        // TODO Auto-generated method stub
        return Writer.writeAndFlush(msg);
    }

    @Override
    public synchronized void sendObject(Object o) {
        // TODO Auto-generated method stub
        Writer.writeAndFlush(o);
    }

    @Override
    public synchronized void callback(Object o) {
        sendObject(o);
        long consuming = System.currentTimeMillis() - Timestamp;
        if (consuming >= ProxyConfig.getSlowOperationThreshold()) {
            try {
                if (Writer != null) {
                    SessionAttribute attribute = CachedSessionAttributes.get(Writer);
                    SlowLogs.add(Request, consuming, Writer, attribute != null ? attribute.getClientName() : null);
                }
            } catch (Throwable t) {
            }
        }
    }

    @Override
    public String toString() {
        return "Redis Compatible connection to " + Writer.channel().remoteAddress();
    }
}
