package com.tongtech.proxy.core.protocol;

import com.tongtech.proxy.core.slices.ResultCallback;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProcessCounter;
import com.tongtech.proxy.core.utils.ProxyConfig;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Session对应的个性化属性类，要求每个getter和setter方法均要保证线程安全
 */
public class SessionAttribute {
    private static final Log logger = ProxyConfig.getServerLog();

    private DataResult dataResult = null;
    private String clientName = null;
    private long clientId = 0;
    // 当前连接事务处理用的命令列表
    private List transactionList = null;
    // 当前session是否认证通过（或不需要认证）
    private boolean authed = false;
    InetSocketAddress remoteAddress = null;
    private ResultCallback Callback = null;

    //private final Queue<Object> cmdQueue = new LinkedBlockingDeque<>();

    private Queue<Object> cmdQueue = new LinkedBlockingDeque<>();

    private NioProcess process;

    public NioProcess getProcess() {
        return process;
    }

    public void setProcess(NioProcess process) {
        this.process = process;
    }

    public Queue<Object> getCmdQueue() {
        return cmdQueue;
    }

    public Object popCmd() {
        return cmdQueue.poll();
    }

    public void pushCmd(Object cmd) {
        cmdQueue.add(cmd);
    }

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


    public void startProcess(ChannelHandlerContext ctx) {

        if (cmdQueue.size() == 0) return;

        Object v = cmdQueue.peek();

        if (process != null && dataResult != null) {
            try {
                if (!process.process(v, dataResult)) {
                    ctx.close();
                }
                // 此处影响输出统计日志
                ProcessCounter.increase();
            } catch (Throwable t) {
                ctx.writeAndFlush("-ERR " + t.getMessage());
                getCmdQueue().poll();
                startProcess(ctx);
                 logger.warnLog("RedisAdapter::channelRead() A fatal error occur: {}", t);
            }
        } else {
             logger.warnLog("RedisAdapter::channelRead() Unporcessed message");
            ctx.writeAndFlush("-ERR Server internal error");
            getCmdQueue().clear();
            ctx.close();
        }

    }
}
