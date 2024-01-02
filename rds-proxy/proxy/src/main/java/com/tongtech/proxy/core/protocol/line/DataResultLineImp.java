package com.tongtech.proxy.core.protocol.line;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

import static com.tongtech.proxy.core.StaticContent.MAX_RESULT_ROWS;

public class DataResultLineImp implements DataResult {
    private final static int MAX_STRING_LEN = 1024 * (1 + MAX_RESULT_ROWS);

    private final static char END_OF_LINE = '\n';

    private volatile boolean HaveData;
    private String Session;
    private final ChannelHandlerContext Writer;
    private final StringBuilder Buffer;

    private volatile AclAuthen Acl = null;

    private volatile int TableId = 1;

    public DataResultLineImp(ChannelHandlerContext ctx) {
        Writer = ctx;
        Buffer = new StringBuilder(1024);
    }

    @Override
    public ChannelHandlerContext getSession() {
        return this.Writer;
    }

    @Override
    public synchronized void init(Object session,List request) {
        // TODO Auto-generated method stub
        HaveData = false;
        Session = session.toString();
        Buffer.setLength(0);
    }

    @Override
    public synchronized void flush() throws IOException {
        if (Buffer.length() > 0) {
            // waitingSend(Writer.write(Buffer));
            Writer.writeAndFlush(Buffer);
            Buffer.setLength(0);
        }
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

//    @Override
//    public AclUser getAcl() {
//        return this.Acl;
//    }

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
        setOk(-1);
    }

    @Override
    public synchronized void setOk(long l) throws IOException {
        // TODO Auto-generated method stub

        // 判断socket是否已经关闭
//        if (Writer.isClosing()) {
//            throw new EOFException("socket is closed");
//        }

        Buffer.setLength(0);
        if (Session != null) {
            Buffer.append(Session);
            Buffer.append(' ');
        }
        Buffer.append("ok");
        if (l >= 0) {
            Buffer.append(' ').append(l);
        }
        Buffer.append(END_OF_LINE);
        Writer.writeAndFlush(Buffer);
        Buffer.setLength(0);
    }

    @Override
    public synchronized void setErr(int err_code, String msg)
            throws IOException {
        // TODO Auto-generated method stub

        // 判断socket是否已经关闭
//        if (Writer.isClosing()) {
//            throw new EOFException("socket is closed");
//        }

        Buffer.setLength(0);
        if (Session != null) {
            Buffer.append(Session);
            Buffer.append(' ');
        }
        Buffer.append("error ");
        Buffer.append(Integer.toString(err_code));
        Buffer.append(' ');
        Buffer.append(msg);
        Buffer.append(END_OF_LINE);
        Writer.writeAndFlush(Buffer);
        Buffer.setLength(0);
    }

    @Override
    public synchronized ChannelFuture send(String msg) throws IOException {
        // TODO Auto-generated method stub

//        if (Writer.isClosing()) {
//            throw new EOFException("socket is closed");
//        }

        Buffer.setLength(0);
        Buffer.append(msg);
        Buffer.append(END_OF_LINE);
        //waitingSend(Writer.write(Buffer));
        Writer.writeAndFlush(Buffer);
        Buffer.setLength(0);
        return null;
    }

    @Override
    public void sendObject(Object o) throws IOException {
    }

    @Override
    public String toString() {
        return "Connection to " + Writer.channel().remoteAddress();
    }

//    private void waitingSend(WriteFuture future) throws IOException {
//        if (future != null && !future.awaitUninterruptibly(1000)) {
//            throw new SocketTimeoutException("send is timeout");
//        }
//    }
}
