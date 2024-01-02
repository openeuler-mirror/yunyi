package com.tongtech.proxy.core.sync;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;
import com.tongtech.proxy.core.sync.codec.SynchronizationDecode;
import com.tongtech.proxy.core.sync.codec.SynchronizationEncode;
import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import static com.tongtech.proxy.core.StaticContent.lengthAsUtf8;
import static com.tongtech.proxy.core.StaticContent.splitString;


public class SyncConnectorImp extends Thread implements SyncConnector {

    private static final EventLoopGroup LOOP_GROUP = new NioEventLoopGroup(4);

    private static final byte[] ANTI_IDLE = "*1\n$4\nidle\n".getBytes(StandardCharsets.UTF_8);

    // 日志类
    private static final Log Logger = ProxyConfig.getServerLog();

    // 记录对端程序的启动时间
    // 如果连接成功后发现时间变了,说明对端服务重启过
    private volatile long RemoteStart = 0;

    private volatile boolean IsConnected = false;

    private final int ConnectionNumber;

    // SynchronizeServer
    private final SyncSender Sender;

    private final int TableId;

    // 取发送数据的数组
//    private final StringBuilder buffer = new StringBuilder(4096);

    private volatile boolean Closed = false;

    private final InetSocketAddress ServerAddress;

    private final String AddressString;

    private final SyncConnectorImp instance;

    private final Bootstrap bootstrap;

    private final Object SendingLocker = new Object();

    private int redisPort = 0;

    private final int SecureLevel;

    private final String Password;

    private volatile long ExchangedSN = 0;

    public SyncConnectorImp(String name, SyncSender sender, int table_id, int no,
                            InetSocketAddress address, int secureLevel, String password) {

        Sender = sender;
        TableId = table_id;
        ConnectionNumber = no;

        ServerAddress = address;

        AddressString = address.toString();

        SecureLevel = secureLevel;

        Password = password;

        bootstrap = new Bootstrap();
        bootstrap.group(LOOP_GROUP)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .option(ChannelOption.SO_TIMEOUT, 1000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        // 给pipeline管道设置处理器
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        if ((SecureLevel & 1) > 0) {
                            pipeline.addFirst(new SslHandler(BogusSSLContextFactory.createClientSSLEngine()));
                        }
                        pipeline.addLast(new IdleStateHandler(0, 5, 0));
                        pipeline.addLast(new SynchronizationEncode(), new SynchronizationDecode());
                        pipeline.addLast(new NettyAdeptor());
                    }
                });

        instance = this;

        this.setDaemon(true);
        this.start();

        Logger.infoLog("SyncConnectorImp::() Connector {}({}) to {} is created.", name, TableId, address);
    }

    @Override
    public void close() {
        Closed = true;
    }

    @Override
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * 判断当前连接是否有效
     *
     * @return
     */
    @Override
    public boolean isConnected() {
        return IsConnected;
    }

    @Override
    public boolean isClose() {
        return Closed;
    }

    @Override
    public InetSocketAddress getServerAddress() {
        return ServerAddress;
    }

    @Override
    public int getRedisPort() {
        return redisPort;
    }

    @Override
    public void run() {
        Channel channel = null;
        List data = null;
        while (!this.Closed) {
            try {
                if (channel == null || !channel.isActive()) {
                    synchronized (SendingLocker) {
                        try {
                            if (channel != null) {
                                channel.close();
                            }
                            IsConnected = false;
                            ChannelFuture future = bootstrap.connect(ServerAddress);
                            future.sync();
                            channel = future.channel();
                            Logger.debugLog("SyncConnectorImp::run() creating connection {} to '{}'", channel, AddressString);
                            SendingLocker.wait(3000);
                            if (!IsConnected) {
                                channel.close();
                                channel = null;
                            }
                            Logger.debugLog("SyncConnectorImp::run() connection {} to '{}' is created.", channel, AddressString);
                        } catch (Throwable e) {
                            if (channel != null) {
                                channel.close();
                                channel = null;
                            }
                            Logger.debugLog("SyncConnectorImp::run() create connect to {} failed: {}"
                                    , AddressString, e.getMessage());
                        }
                    }
                }

                if (channel == null || !channel.isActive()) {
                    // 满足条件说明上面创建连接失败了
                    // 休息一下返回去重新创建
                    try {
                        sleep(3000);
                    } catch (Throwable t) {
                    }
                    continue;
                }

                Logger.infoLog("SyncConnectorImp::run() create connect {} to {} ok.", channel, AddressString);

                // 执行到此处连接已经创建成功
                int resend_times = 0;
                while (!Closed && channel != null && channel.isActive()) {

                    // 从发送列表中读1条数据
                    if (data == null) {
                        data = Sender.getData(ConnectionNumber);
                    }

                    // 发送列表为空
                    if (data == null) {
                        continue;
                    }

                    channel.writeAndFlush(data).sync();
                    data = null;
                }
            } catch (Throwable t) {
                // 由于连接中断导致channel关闭了，例如备份节点死了
                // 或者连接正常，但重复了多次后仍然不能完成同步
                // 无论上述哪种情况，都需要确保连接中断，然后重建连接
                try {
                    try {
                        // 保证连接是关闭状态
                        channel.close();
                    } catch (Throwable t3) {
                    }
                    Logger.warnLog("SyncConnectorImp::run() general error occur when send data to {} failed: {}", channel, t);
                } catch (Throwable t2) {
                }
            }
        }
        Logger.warnLog("SyncConnectorImp::run() sending thread for {} exit.", AddressString);
    }

    private class NettyAdeptor extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            // TODO Auto-generated method stub
            // System.out.println("Connector::sessionOpened()");
            if (!Closed) {
                if ((SecureLevel & 2) > 0) {
                    int utf8Len = lengthAsUtf8(Password);
                    ctx.write(("*2\n$4\nauth\n$" + utf8Len + "\n" + Password + "\n"));
                }
                ctx.writeAndFlush("*1\n$5\ncheck\n");
                if (Logger.isDebug()) {
                    Logger.debugLog("SyncConnectorImp::channelActive() New connection {} opened.", ctx.channel());
                }
            } else {
                ctx.close();
                Logger.warnLog("SyncConnectorImp::channelActive() close a duplicate connection");
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            // TODO Auto-generated method stub
            // 按空格分解命令

            // System.out.println("Connector::messageReceived() Receive " + cmd);

            if (Closed) {
                ctx.close();
                Logger.infoLog("SyncConnectorImp::channelRead() Connector({}) is closed.", AddressString);
                return;
            }

            if (msg == null || "".equals(msg)) {
                Logger.infoLog("SyncConnectorImp::channelRead() Null message received from {}", AddressString);
                return;
            }

            String command = msg.toString();

            Vector<String> argv = splitString(command, ' ', 4);

            if (argv.size() < 1 || argv.get(0) == null) {
                Logger.infoLog("SyncConnectorImp::channelRead() Header is null");
                return;
            }

            /**
             * 以下是协议解析接口的处理流程
             */
            if (argv.get(0).charAt(0) >= '0' && argv.get(0).charAt(0) <= '9') {
                long l = Long.parseLong(argv.get(0));
                if (SyncMonitor.setExchangedNo(ServerAddress, l)) {
                    ExchangedSN = l;
                }
            } else  /*if (argv.get(0).charAt(0) > '9')*/ {
                // 非数字开头
                if ("CK".equalsIgnoreCase(argv.get(0))) {
                    // CK包是check的响应包
                    // 收到CK包，说明重新连接，且连接已经正常了
                    if (ProxyConfig.getIdentify().equals(argv.get(2))) {
                        // 连接的是自己
                        Sender.removeMyselfConnector(instance);
                        Logger.warnLog("SyncConnectorImp::channelRead() The connection {} is itself, close it."
                                , ctx.channel());
                        return;
                    }

                    // 新增取 redis_port 功能，取完才算连接成功
                    synchronized (SendingLocker) {
                        try {
                            redisPort = Integer.parseInt(argv.get(3));
                        } catch (Throwable t) {
                        }
                        IsConnected = true;
                        SendingLocker.notifyAll();
                    }

                    long start = 0;
                    try {
                        start = Long.parseLong(argv.get(1));
                    } catch (NumberFormatException nfe) {
                        start = 0;
                        if (Logger.isDebug()) {
                            Logger.debugLog("SyncConnectorImp::channelRead() Receive error message '{}' from {}"
                                    , msg, AddressString);
                        }
                    }
                    if (start > RemoteStart) {
                        // 本机刚刚启动，或者远程主机重启了
                        RemoteStart = start;
                    }
//                    // 尝试重新发送Connector缓存的数据
//                    // 如果没有缓存则尝试从同步列表中取
//                    sendMessage();
                    Logger.infoLog("SyncConnectorImp::channelRead() New connection {} opened.", ctx.channel());
                } else /*if ("OK".equalsIgnoreCase(argv.get(0))) */ {
                    // idle的响应包
                    if (Logger.isDebug()) {
                        Logger.debugLog("SyncConnectorImp::channelRead() Receive '{}' from {}"
                                , msg, ctx.channel());
                    }
                }
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
//                IdleStateEvent event = (IdleStateEvent) evt;
//                if (event.state() == IdleState.WRITER_IDLE) {
                ctx.writeAndFlush(ANTI_IDLE);
//                }
            }
//            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
                throws Exception {
            // TODO Auto-generated method stub

            Logger.warnLog("SyncConnectorImp::exceptionCaught() General exception occur at {}: {}"
                    , ctx.channel(), e);
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            // TODO Auto-generated method stub
        }
    }
}
