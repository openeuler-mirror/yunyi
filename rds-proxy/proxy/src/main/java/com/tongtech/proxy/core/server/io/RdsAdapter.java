package com.tongtech.proxy.core.server.io;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import com.tongtech.proxy.core.acl.AccessController;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.protocol.line.ProcessLineImp;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProcessCounter;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.SessionAttribute;
import com.tongtech.proxy.core.protocol.line.DataResultLineImp;
import com.tongtech.proxy.core.pubsub.PSManager;
import com.tongtech.proxy.core.server.ConnectionCounter;
import com.tongtech.proxy.fw.AddressRestrictions;
import com.tongtech.proxy.core.StaticContent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

@ChannelHandler.Sharable
public class RdsAdapter extends ChannelInboundHandlerAdapter {

    // 日志
    private final static Log logger = ProxyConfig.getServerLog();

    // 日志是否是debug级(级别大于debug时为真)
    private final static boolean DebugLog = logger.isDebug();

    // 日志是否是info级(级别大于debug时为真)
    //private final static boolean InfoLog=logger.isInfo();

    private final ProcessLineImp MessageProcess;
    //private final int Protocol;

    public RdsAdapter(ProcessLineImp messageProcess) {

        MessageProcess = messageProcess;
    }

    /**
     * 判断输入命令是否是同步类的命令
     *
     * @param data 输入
     * @return 如果是sync类的命令则返回“true”
     */
    private boolean isSync(List data) {
        if (data.size() > 2 && data.get(0) instanceof Long && data.get(1) instanceof Long) {
            return true;
        }
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // TODO Auto-generated method stub
        if (cause instanceof IOException) {
            logger.warnLog("RdsAdapter::exceptionCaught() IO error: {}", cause.getMessage());
        } else {
            logger.errorLog(cause, "RdsAdapter::exceptionCaught() Error: {}", cause);
        }
        ctx.close();
    }

//	private long run_expend=0;
//	private int times=0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof List)) {
            return;
        }

        SessionAttribute attribute = StaticContent.CachedSessionAttributes.get(ctx);

        if (msg == null) {
            logger.debugLog("RdsAdapter::channelRead() Null message received");
            return;
        }

        DataResult result = attribute.getDataResult();

        List argv = (List) msg;

//        infolog("MemoryDB::messageReceived() receive {}",msg);
        // 判断客户端是否需要密码认证
        boolean sessionAuthened = attribute.isAuthed();
        if (!sessionAuthened) {
            if (argv.size() > 0) {
                String cmd = String.valueOf(argv.get(0));
                if ("Exit".equalsIgnoreCase(cmd)) {
                    ctx.close();
                    return;
                } else if ("Auth".equalsIgnoreCase(cmd)) {
                    if (argv.size() == 2) {
                        String pass = argv.get(1).toString();
                        if (ProxyConfig.getAuthenString().equals(pass)) {
                            AddressRestrictions.authSuccessed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
                            attribute.setAuthed(true);
                            //session.write("OK");
                            logger.infoLog("RdsAdapter::channelRead() Client '{}' authen ok."
                                    , ctx.channel().remoteAddress());
                        } else {
                            logger.warnLog("RdsAdapter::channelRead() Client '{}' authen failed, authen password = '{}'"
                                    , ctx.channel().remoteAddress(), msg);
                            if (AddressRestrictions.authFailed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress())) {
                                logger.warnLog("RdsAdapter::channelRead() Authen failed too many times.");
                                ctx.close();
                            }
                        }
                    } else if (argv.size() == 3) {
                        String user = argv.get(1).toString();
                        String pass =  argv.get(2).toString();
                        AclAuthen acl = AccessController.auth(user, pass);
                        if (acl != null) {
                            AddressRestrictions.authSuccessed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
                            attribute.setAuthed(true);
                            result.setAcl(acl);
                            //session.write("OK");
                            logger.infoLog("RdsAdapter::channelRead() User {} from '{}' authen ok"
                                    , cmd, ctx.channel().remoteAddress());
                        } else {
                            //session.setErr("invalid username-password pair");
                            logger.warnLog("RdsAdapter::channelRead() User {} from '{}' password {} authen failed."
                                    , cmd, ctx.channel().remoteAddress(), argv.get(0));
                            if (AddressRestrictions.authFailed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress())) {
                                logger.warnLog("RdsAdapter::channelRead() Authen failed too many times.");
                                ctx.close();
                            }
                        }
                    }
                } else {
                    logger.warnLog("RdsAdapter::channelRead() Unauthened client {} send message '{}'."
                            , ctx.channel().remoteAddress(), msg);
                }
            }
            return;
        }

        if (MessageProcess != null && result != null) {
            try {
                // long start = System.nanoTime();

                if (isSync(argv)) {
                    MessageProcess.synchronize(argv, result);
                } else {
                    if (!MessageProcess.process(msg, result)) {
                        ctx.close();
                    }
                    ProcessCounter.increase();
                }
            } catch (NullPointerException npe) {
                logger.errorLog(npe, "RdsAdapter::channelRead() Null point exception occur: {}", npe.getMessage());
            } catch (Throwable t) {
                logger.warnLog("RdsAdapter::channelRead() A fatal error occur: {}", t);
                if (logger.isDebug()) {
                    for (StackTraceElement element : t.getStackTrace()) {
                        logger.debugLog("            "
                                + element.getClassName()
                                + "::" + element.getMethodName()
                                + "(" + element.getFileName() + ":"
                                + element.getLineNumber() + ")");
                    }
                }
            }
        } else {
            logger.warnLog("RdsAdapter::channelRead() Unporcessed message");
            ctx.writeAndFlush("Server internal error");
            ctx.close();
        }
    }

    /**
     * 建立连接时，返回消息
     *
     * @param ctx channelHandlerContext
     * @throws Exception 异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        SessionAttribute attribute = new SessionAttribute();
        StaticContent.CachedSessionAttributes.put(ctx, attribute);

        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        if (address != null) {
            attribute.setRemoteAddress(address);
//            hashtable.put(TITLE_REMOTEADDR, address);
        }
        ConnectionCounter.connectionOpenRds(address);

        DataResult result = new DataResultLineImp(ctx);

        // client id
        attribute.setClientId(StaticContent.ClientId.getAndIncrement());

        attribute.setDataResult(result);
//        hashtable.put(TITLE_DATARESULT, result);

        boolean authened = true;
        if ((ProxyConfig.getSecureLevel() & 2) > 0) {
            authened = false;
        }
        attribute.setAuthed(authened);
//        hashtable.put(TITLE_AUTHENED, authened);

        ctx.channel().config().setWriteBufferHighWaterMark(1024 * 1024);
        ctx.channel().config().setWriteBufferLowWaterMark(512 * 1024);

        try {
            AddressRestrictions.allow(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
        } catch (IOException ioe) {
            ConnectionCounter.connectionRejectedRds();
//            logger.warnLog("RedisCompatibleNetty::channelActive() {}", ioe.getMessage());
            logger.warnLog("RdsAdapter::channelActive() {}", ioe.getMessage());
            ctx.close();
            return;
        }

        if (ConnectionCounter.getCurrentConnectionsRds() > ProxyConfig.getMaxConnections()) {
            ConnectionCounter.connectionRejectedRds();
//            logger.warnLog("RedisCompatibleNetty::channelActive()  client '{}' is closed by reaching {} connections", address, MaxConnections);
            logger.warnLog("RdsAdapter::channelActive()  client '{}' is closed by reaching {} connections", address, ProxyConfig.getMaxConnections());
            ctx.writeAndFlush("Too many connections");
            ctx.close();
            return;
        }
    }

    /**
     * channel没有连接到远程节点
     *
     * @param ctx channelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SessionAttribute attribute = StaticContent.CachedSessionAttributes.remove(ctx);
        InetSocketAddress address = attribute.getRemoteAddress();
        ConnectionCounter.connectionCloseRds(address);
        ctx.close();

        DataResult result = attribute.getDataResult();
        // 取消已订阅的消息
        try {
            PSManager.sessionClosed(result);
        } catch (Throwable t) {
            logger.errorLog(t, "close channel failed");
        }
//        // 取消阻塞命令的监听
//        try {
//            BlockingCommand.channelClose(result);
//        } catch (Throwable t) {
//            logger.errorLog(t, "close channel failed");
//        }
//        // 取消Stream命令的阻塞监听
//        try {
//            BlockStream.channelClose(result);
//        } catch (Throwable t) {
//            logger.errorLog(t, "close channel failed");
//        }

        // 清理过期数据
        AddressRestrictions.clean();
    }
}
