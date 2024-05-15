package com.tongtech.proxy.core.server.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import com.tongtech.proxy.core.acl.AccessController;
import com.tongtech.proxy.core.acl.AclAuthen;
import com.tongtech.proxy.core.protocol.NioProcess;
import com.tongtech.proxy.core.slices.ResultCallback;
import com.tongtech.proxy.core.protocol.redis.callbacks.DefaultDataResultImp;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProcessCounter;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.SessionAttribute;
import com.tongtech.proxy.core.protocol.redis.ClientManager;
import com.tongtech.proxy.core.protocol.redis.ProcessorRedisImp;
import com.tongtech.proxy.core.pubsub.PSManager;
import com.tongtech.proxy.core.server.ConnectionCounter;
import com.tongtech.proxy.fw.AddressRestrictions;
import com.tongtech.proxy.core.StaticContent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.Vector;

/**
 * 自定义的Handler需要继承Netty规定好的HandlerAdapter
 * 才能被Netty框架所关联，有点类似SpringMVC的适配器模式
 * Netty每个连接会创建一个此对象，该对象的创建过程需要足够轻量，否则影响连接质量
 **/
public class RedisAdapter extends ChannelInboundHandlerAdapter {
    // 日志
    private static final Log logger = ProxyConfig.getServerLog();

    private final NioProcess messageProcesser;

    public RedisAdapter(ProcessorRedisImp messageRedisImp) throws Exception {
        messageProcesser = messageRedisImp;
    }

//    /**
//     * 记录 Infomation 日志，程序会在日志头加上"INFO"。
//     *
//     * @param logs String 日志内容
//     */
//    private void infolog(String logs, Object... objects) {
//        if (null != this.logger) {
//            logger.infoLog(logs, objects);
//        }
//    }
//
//    /**
//     * 记录 Warning 日志，程序会在日志头加上"ERROR"。
//     *
//     * @param logs String 日志内容
//     */
//    private void warnlog(String logs, Object... objects) {
//        if (null != this.logger) {
//            logger.warnLog(logs, objects);
//        }
//    }
//

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (!(obj instanceof Vector)) {
            logger.warnLog("RedisAdapter::channelRead() Unknow object {} received.", obj);
            return;
        }


        // 此处 data 的格式为：第一项为字符串形式的命令项（已经转为小写），
        // 后面都是字节数组形式的原始数据
        Vector<Object> data = (Vector<Object>) obj;
        SessionAttribute attribute = StaticContent.CachedSessionAttributes.get(ctx);

//        if (attribute.isReadSuspend()) {
//            return;
//        }

        DataResult result = attribute.getDataResult();

        // 判断客户端是否需要密码认证
        boolean sessionAuthened = attribute.isAuthed();

        if (!sessionAuthened) {
//            if (Configuration.isBinaryCompatible()) {
            // 如果isBinaryCompatible为真时，list内的字符串都是以byte数组的形式存的
            // exchangeList方法根据命令类型将其他参数转换为字符串
//            }

            try {
                if ("Auth".equalsIgnoreCase(data.get(0).toString())) {
                    if (data.size() == 1) {
                        ctx.writeAndFlush("-WRONGPASS invalid username-password pair");
                        logger.warnLog("RedisAdapter::channelRead() Client '{}' null authen."
                                , ctx.channel().remoteAddress());
                    } else if (data.size() == 2) {
                        String passwd = new String((byte[]) data.get(1), StandardCharsets.UTF_8);
                        if (ProxyConfig.getRedisPwd().equals(passwd)) {
                            AddressRestrictions.authSuccessed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
                            attribute.setAuthed(true);
                            logger.infoLog("RedisAdapter::channelRead() Client '{}' authen ok"
                                    , ctx.channel().remoteAddress());
                            ctx.writeAndFlush("OK");
                        } else {
                            ctx.writeAndFlush("-WRONGPASS invalid username-password pair");
                            logger.warnLog("RedisAdapter::channelRead() Client '{}' password {} authen failed."
                                    , ctx.channel().remoteAddress(), data.get(1));
                            if (AddressRestrictions.authFailed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress())) {
                                logger.warnLog("RedisAdapter::channelRead() Authen failed too many times.");
                                ctx.close();
                            }
                        }
                    } else if (data.size() >= 3) {
                        String user = new String((byte[]) data.get(1), StandardCharsets.UTF_8);
                        String passwd = new String((byte[]) data.get(2), StandardCharsets.UTF_8);
                        AclAuthen acl = AccessController.auth(user, passwd);
                        if (acl != null) {
                            AddressRestrictions.authSuccessed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
                            attribute.setAuthed(true);
                            result.setAcl(acl);
                            ctx.writeAndFlush("OK");
                            logger.infoLog("RedisAdapter::channelRead() User {} from '{}' authen ok"
                                    , user, ctx.channel().remoteAddress());
                        } else {
                            ctx.writeAndFlush("-WRONGPASS invalid username-password pair");
                            logger.warnLog("RedisAdapter::channelRead() User {} from '{}' password {} authen failed."
                                    , user, ctx.channel().remoteAddress(), data.get(1));
                            if (AddressRestrictions.authFailed(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress())) {
                                logger.warnLog("RedisAdapter::channelRead() Authen failed too many times.");
                                ctx.close();
                            }
                        }
                    } else {
                        ctx.writeAndFlush("-WRONGPASS invalid username-password pair");
                        logger.warnLog("RedisAdapter::channelRead() Client '{}' args({}) of authen is invalid."
                                , ctx.channel().remoteAddress(), data.size());
                    }
                } else if ("Quit".equalsIgnoreCase(data.get(0).toString())) {
                    ctx.writeAndFlush("+OK").sync();
                    ctx.close();
                    logger.infoLog("RedisAdapter::channelRead() Client request closed.");
                } else if ("Hello".equalsIgnoreCase(data.get(0).toString())) {
                    String para = "";
                    for (int i = 1; i < data.size(); ++i) {
                        para += "`" + data.get(i).toString() + "`, ";
                    }
                    ctx.writeAndFlush("-ERR unknown command `" + data.get(0).toString() + "`, with args beginning with: " + para);
                } else {
                    ctx.writeAndFlush("-NOAUTH Authentication required.");
                    logger.infoLog("RedisAdapter::channelRead() Unauthen client {} request '{}'."
                            , ctx.channel().remoteAddress(), data.get(0));
                }
            } catch (Throwable e) {
                ctx.writeAndFlush("-ERR Authen failed: " + e.getMessage()).sync();
                logger.warnLog("RedisAdapter::channelRead() Error occur: {}.", e);
            }
            return;
        }
//---- 处理

        attribute.pushCmd(data);

        Queue<Object> cmdQueue = attribute.getCmdQueue();
        if (cmdQueue.size() == 1) {
            attribute.startProcess(ctx);
        }

    }

    /**
     * 建立连接时，返回消息
     *
     * @param ctx channelHandlerContext
     * @throws Exception 异常
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SessionAttribute attribute = new SessionAttribute();
        StaticContent.CachedSessionAttributes.put(ctx, attribute);

        attribute.setProcess(messageProcesser);

        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        if (address != null) {
            attribute.setRemoteAddress(address);
//            hashtable.put(TITLE_REMOTEADDR, address);
        }

        ConnectionCounter.connectionOpenRedis(address);

        DataResult result = new DefaultDataResultImp(ctx);

        attribute.setDataResult(result);

//        System.out.println("RedisAdapter::() ~ " + result);

        // client id
        attribute.setClientId(StaticContent.ClientId.getAndIncrement());
//        hashtable.put(TITLE_CLIENTID, ClientId.getAndIncrement());

        boolean authened = true;
        if ((ProxyConfig.getSecureLevel() & 2) > 0) {
            authened = false;
        }
        attribute.setAuthed(authened);
//        hashtable.put(TITLE_AUTHENED, authened);

        ctx.channel().config().setWriteBufferHighWaterMark(1024 * 1024);
        ctx.channel().config().setWriteBufferLowWaterMark(512 * 1024);

        ClientManager.active(ctx);

        try {
            AddressRestrictions.allow(((InetSocketAddress) ctx.channel().remoteAddress()).getAddress());
        } catch (IOException ioe) {
            ConnectionCounter.connectionRejectedRedis();
            logger.warnLog("RedisAdapter::channelActive() {}", ioe.getMessage());
            ctx.close();
            return;
        }

        if (ConnectionCounter.getCurrentConnectionsRedis() > ProxyConfig.getMaxConnections()) {
            ConnectionCounter.connectionRejectedRedis();
            logger.warnLog("RedisAdapter::channelActive()  client '{}' is closed by reaching {} connections", address, ProxyConfig.getMaxConnections());
            ctx.close();
            return;
        }
    }

    /**
     * 异常触发
     *
     * @param ctx   channelHandlerContext
     * @param cause 异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            logger.warnLog("IO error: {}", cause);
        } else {
            logger.warnLog(cause, "error: {}", cause);
        }
        ctx.close();
    }

    /**
     * channel没有连接到远程节点
     *
     * @param ctx channelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
//        logger.errorLog("RedisAdapter::channelClose() try to remove "+ctx);
        SessionAttribute attribute = StaticContent.CachedSessionAttributes.remove(ctx);
        InetSocketAddress address = attribute.getRemoteAddress();

        ConnectionCounter.connectionCloseRedis(address);

//        logger.errorLog("RedisAdapter::channelClose() try to remove "+ctx);
        ClientManager.deactive(ctx);
        try {
            ctx.close().sync();
        } catch (Throwable e) {
            e.printStackTrace();
        }
//        logger.errorLog("RedisAdapter::channelClose() try to remove "+ctx);

        DataResult result = attribute.getDataResult();

//        System.out.println("RedisAdapter::() = " + result);

        // 取消已订阅的消息
        try {
            PSManager.sessionClosed(result);
        } catch (Throwable t) {
            logger.errorLog(t, "close channel failed");
        }

        // 取消阻塞命令的监听
        try {
            ResultCallback callback = attribute.getCallback();
            if (callback != null) {
                callback.channelClosed(ctx);
            }
        } catch (Throwable t) {
            logger.errorLog(t, "close channel failed");
        }

        // 清理过期数据
        AddressRestrictions.clean();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.close();
        }
    }
}
