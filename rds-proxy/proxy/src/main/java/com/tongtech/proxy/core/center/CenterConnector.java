package com.tongtech.proxy.core.center;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import com.tongtech.proxy.core.center.codec.CenterDecoder;
import com.tongtech.proxy.core.center.codec.CenterEncoder;
import com.tongtech.proxy.core.utils.OriginalSocketAddress;
import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.ProxyDynConfig;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static com.tongtech.proxy.core.center.ProxyData.MESSAGETYPE_PROXYAUTHEN;

public class CenterConnector {
    private static final Log Logger = ProxyConfig.getServerLog();

    private static final int SOCKET_CONNECT_TIMEOUT = 500;
    private static final int SOCKET_DATA_TIMEOUT = 500;

    private static final EventLoopGroup LOOP_GROUP = new NioEventLoopGroup(2);

    // 中心节点地址
    private final String CenterRemoteAddress;

    // 中心节点端口
    private final int CenterRemotePort;

    private final Bootstrap bootstrap;

    private volatile Channel ActiveChannel = null;

    private volatile boolean isClosed = false;

    public CenterConnector(OriginalSocketAddress address) {

        CenterRemoteAddress = address.getOriginalAddress();
        CenterRemotePort = address.getPort();

        bootstrap = new Bootstrap();
        bootstrap.group(LOOP_GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, SOCKET_CONNECT_TIMEOUT)
//                .option(ChannelOption.SO_TIMEOUT, 1000)
                .option(ChannelOption.SO_TIMEOUT, SOCKET_DATA_TIMEOUT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 给pipeline管道设置处理器
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addFirst(new SslHandler(BogusSSLContextFactory.createClientSSLEngine()));
                        pipeline.addLast(new CenterEncoder(), new CenterDecoder());
                        pipeline.addLast(new Adeptor());
                    }
                });
    }

    String getCenterAddress() {
        return CenterRemoteAddress;
    }

    int getCenterPort() {
        return CenterRemotePort;
    }

    /**
     * 有2个线程可能会调用到，需要线程安全
     */
    private void closeConnection() {
        synchronized (bootstrap) {
            if (ActiveChannel != null) {
                try {
                    ActiveChannel.close();
                } catch (Throwable e) {
                } finally {
                    ActiveChannel = null;
                }
            }
        }
    }

    void close() {
        isClosed = true;
        closeConnection();
    }

    /**
     * 关闭当前连接，尝试创建新连接并发消息
     *
     * @param msg
     * @return
     */
    boolean createAndSend(List msg) {
        if (isClosed) {
            return false;
        }

        closeConnection();

        try {
            // NioSocketConnector可以设置缺省地址，然后调用无菜蔬的connect方法连接，或调用带参数的connect方法连接
            // Core模块中只有连接center时采用的带参数的connect方法，
            // 为的是sky walking插件可以通过拦截带参数的方法截获core节点与center节点的通信
//            ConnectFuture future = Connector.connect(CenterAddress);
//            ChannelFuture future = bootstrap.connect(new InetSocketAddress(CenterRemoteAddress, CenterRemotePort)).sync();


            ChannelFuture future = null;
            // k8s下面CenterRemoteAddress可能返回多个地址，需要遍历找到能用的那一个
            InetAddress[] addresses = InetAddress.getAllByName(CenterRemoteAddress);
            if (addresses != null && addresses.length > 0) {
                int offset = ((int) System.nanoTime()) & 0xf;
                for (int i = 0; i < addresses.length; ++i) {
                    int cur_pos = (offset + i) % addresses.length;
                    InetAddress addr = null;
                    try {
                        addr = addresses[cur_pos];
                        future = bootstrap.connect(new InetSocketAddress(addr, CenterRemotePort)).sync();
                        // 第一个包是认证包,认证包没有回包
                        future.channel().writeAndFlush(getAuthPackage()).sync();
                        Logger.infoLog("CenterConnector::createAndSend() Connect to '{}' in '{}' ok.", addr, addresses);
                        break;
                    } catch (Throwable t) {
                        Logger.infoLog("CenterConnector::createAndSend() Connect to '{}' failed.", addr);
                    }
                }
            }

            Channel channel = future.channel();
            synchronized (bootstrap) {
//                DataExchangeOk = true;
                ActiveChannel = channel;
            }
            // 第一个包是认证包,认证包没有回包
            channel.writeAndFlush(getAuthPackage()).sync();
            boolean isOk = sendMessage(msg);
            Logger.infoLog("CenterConnector::createAndSend() try to connect to {}:{} {}"
                    , CenterRemoteAddress, CenterRemotePort, (isOk ? " ok." : " failed."));
            return isOk;
        } catch (Throwable e) {
            closeConnection();
        }
        return false;
    }

    // 连接状态正常标志
//    private boolean DataExchangeOk;

    /**
     * 该方法发送的报文要求是必须有回应的报文，否则将导致连接被被关闭（如用此方法发送）。
     * <p>
     * 该方法线程不安全
     *
     * @param msg
     * @return
     */
    boolean sendMessage(List msg) {
        if (isClosed) {
            return false;
        }

        Channel channel = null;
        synchronized (bootstrap) {
            channel = ActiveChannel;
        }

        if (channel != null) {
            if (channel.isActive()) {
                try {
                    channel.writeAndFlush(msg).sync();

                    Logger.debugLog("CenterConnector::sendMessage() Exchange data to {}:{} ok."
                            , CenterRemoteAddress, CenterRemotePort);

                    return true;
                } catch (Exception e2) {
                    closeConnection();
                    //  delayToConnect();
                }
            } else {
                closeConnection();
            }
        }
        return false;
    }

    private ArrayList getAuthPackage() {
        ArrayList auth = new ArrayList();
        auth.add(MESSAGETYPE_PROXYAUTHEN);
        auth.add(ProxyConfig.getListeningPort());
        auth.add(ProxyConfig.getServiceName());
        auth.add(ProxyDynConfig.getCenterPassword());
        auth.add(ProxyConfig.getLocalAddress().getOriginalAddress());
        return auth;
    }

    private class Adeptor extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object o) {
            // TODO Auto-generated method stub

            if (o instanceof Vector) {
                ProxyData.parseObject((Vector) o);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // TODO Auto-generated method stub
            // System.out.println("Connector::sessionOpened()");
        }

//        @Override
//        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
//            if (evt instanceof IdleStateEvent) {
//                IdleStateEvent event = (IdleStateEvent) evt;
//                if (event.state() == IdleState.WRITER_IDLE) {
//                    ctx.writeAndFlush("idle");
//                }
//            }
//            ctx.fireUserEventTriggered(evt);
//        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
                throws Exception {
            // TODO Auto-generated method stub
            // if (IsDebug) {
            // e.printStackTrace();
            // }
            // System.out.println("Connector::exceptionCaught()");

            Logger.warnLog("CenterConnector::exceptionCaught() General exception occur at +"
                    + ctx.channel() + ": " + e.getMessage());
            closeConnection();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            // TODO Auto-generated method stub
            // System.out.println("Connector::sessionClosed()");
            // delayToConnect();
        }
    }
}
