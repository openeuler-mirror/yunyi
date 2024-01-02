package com.tongtech.proxy.core.server;

import com.tongtech.proxy.jmx.StatusColector;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import com.tongtech.proxy.Version;
import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.acl.AccessController;
import com.tongtech.proxy.core.center.ProxyData;
import com.tongtech.proxy.core.db.LuaManager;
import com.tongtech.proxy.core.protocol.redis.ProcessorRedisImp;
import com.tongtech.proxy.core.server.io.redis.RedisDecode;
import com.tongtech.proxy.core.server.io.redis.RedisEncode;
import com.tongtech.proxy.core.sync.SyncMonitor;
import com.tongtech.proxy.core.utils.*;
import com.tongtech.proxy.core.crypto.BogusSSLContextFactory;
import com.tongtech.proxy.core.protocol.line.ProcessLineImp;
import com.tongtech.proxy.core.server.io.RdsAdapter;
import com.tongtech.proxy.core.server.io.RedisAdapter;

import com.tongtech.proxy.core.server.io.textline.LineDecode;
import com.tongtech.proxy.core.server.io.textline.LineEncode;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.tongtech.proxy.core.StaticContent.SCHEDULED_THREAD_POOL_EXECUTOR;
import static com.tongtech.proxy.core.StaticContent.VENDER_ID;

public class ProxyServer {
    private static volatile ServerBootstrap bootstrap = null;
    private static volatile Channel channel_for_rds;
    private static volatile ServerBootstrap bootstrap_for_redis = null;
    private static volatile Channel channel_for_redis;
    private static final Log logger = ProxyConfig.getServerLog();

    public static void main(String[] args) throws IOException {
        if (args != null && args.length > 0) {
            if ("Stop".equalsIgnoreCase(args[0])) {
                StopServer.stopServer();
                System.exit(0);
            } else if ("version".equalsIgnoreCase(args[0])) {
                try {
                    System.out.println("\n\nProduct Name: " + Version.ProductName);
                    System.out.println("\nVersion: " + Version.Version);
                    System.out.println("\nBuild Date: " + Version.BuildTime);

                    System.out.println("\n\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        }

        try {
            // Creating SecurityManager
            try {
                if (System.getProperty("java.security.policy") != null) {
                    SecurityManager sm = new SecurityManager();
                    System.setSecurityManager(sm);
                }
            } catch (SecurityException se) {
                System.out.println(se.getMessage());
            } catch (Exception e) {
                System.out
                        .println("A fatal error occur when create a new SecurityManager: "
                                + e.toString());
            }

            try {
                long[] macs = StaticContent.getCurrentMacs();
                StringBuilder buf = new StringBuilder(128);
                if (macs == null) {
                    buf.append(" null");
                } else {
                    for (long l : macs) {
                        buf.append(' ').append(Long.toHexString(l));
                    }
                }
                logger.coreLog("The physical addresses:{}", buf);
            } catch (Exception e) {
                logger.coreLog("Search physical address failed: " + e.getMessage());
            }

            // delete file '$SERVER_HOME/logs/users.acl'
            AccessController.writeLogFile();

            logger.infoLog("Server is starting...");

            // 启动动态配置更新线程
            ProxyDynConfig.init();

            // 启动Center线程
            ProxyData.start();

            LuaManager.init();

            SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(SyncMonitor::checkPeers, 0, 3210, TimeUnit.MILLISECONDS);

            ProxyController main = ProxyController.INSTANCE;

            SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(main::updateSliceData, 1000, 4134, TimeUnit.MILLISECONDS);

            final ProcessLineImp rdsProcessor = new ProcessLineImp();

//            LocalChannelOption channelOption = new DefaultChannelSelectStrategy().select();

            EventLoopGroup bossGroup = new NioEventLoopGroup(ProxyConfig.getSocketIoThreads());
            EventLoopGroup workerGroup = new NioEventLoopGroup(ProxyConfig.getSocketProcessThreads());

            bootstrap = new ServerBootstrap();
            // 设置两个线程组boosGroup和workerGroup
            bootstrap.group(bossGroup, workerGroup)
                    // 设置服务端通道实现类型
                    .channel(NioServerSocketChannel.class)
                    // 设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG, ProxyConfig.getSocketBacklog())
                    .option(ChannelOption.SO_REUSEADDR, true)
                    // 设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 使用匿名内部类的形式初始化通道对象
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            // 给pipeline管道设置处理器
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            if ((ProxyConfig.getSecureLevel() & 1) > 0) {
                                pipeline.addFirst(new SslHandler(BogusSSLContextFactory.createServerSSLEngine()));
                            }
                            pipeline.addLast(new LineEncode(), new LineDecode());
                            pipeline.addLast(new RdsAdapter(rdsProcessor));
                        }
                    });
            // 绑定端口号，启动服务端
            ChannelFuture channelFuture = bootstrap.bind(ProxyConfig.getListeningPort());
            try {
                channelFuture.sync();
            } catch (Exception e) {
                throw new IOException("Listening port " + ProxyConfig.getListeningPort() + " failed: " + e.getMessage());
            }

            if (VENDER_ID < 3) {
                System.out.println("Begin to listen " + ProxyConfig.getListeningPort());
            } else {
                System.out.println("Start listening to port " + ProxyConfig.getListeningPort());
            }

            logger.coreLog("Server::main() Port "
                    + ProxyConfig.getListeningPort() + " is listened");

            channel_for_rds = channelFuture.channel();


            StatusColector.setController(main);

            /**
             * 以下代码启动Redis协议监听,port=0则忽略Redis端口监听
             */
            int redis_port = ProxyConfig.getRedisPort();
            if (redis_port > 0) {
                final ProcessorRedisImp messageRedisImp = new ProcessorRedisImp();

                bossGroup = new NioEventLoopGroup(ProxyConfig.getSocketIoThreads());
                workerGroup = new NioEventLoopGroup(ProxyConfig.getSocketProcessThreads());

                bootstrap_for_redis = new ServerBootstrap();
                // 设置两个线程组boosGroup和workerGroup
                bootstrap_for_redis.group(bossGroup, workerGroup)
                        // 设置服务端通道实现类型
                        .channel(NioServerSocketChannel.class)
                        // 设置线程队列得到连接个数
                        .option(ChannelOption.SO_BACKLOG, ProxyConfig.getSocketBacklog())
                        .option(ChannelOption.SO_REUSEADDR, true)
                        // 设置保持活动连接状态
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        // 使用匿名内部类的形式初始化通道对象
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                // 给pipeline管道设置处理器
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                if ((ProxyConfig.getSecureLevel() & 1) > 0) {
                                    pipeline.addFirst(new SslHandler(BogusSSLContextFactory.createServerSSLEngine()));
                                }
                                if (ProxyConfig.getChannelIdleTimeout() > 0) {
                                    pipeline.addLast(new IdleStateHandler(0, 0, ProxyConfig.getChannelIdleTimeout()));
                                }
                                pipeline.addLast(new RedisEncode(), new RedisDecode());
                                pipeline.addLast(new RedisAdapter(messageRedisImp));
                            }
                        }); // 给workerGroup的EventLoop对应的管道设置处理器
                // 绑定端口号，启动服务端
                channelFuture = bootstrap_for_redis.bind(redis_port);
                try {
                    channelFuture.sync();
                } catch (Exception e) {
                    throw new IOException("Listening port " + redis_port + " failed: " + e.getMessage());
                }

                channel_for_redis = channelFuture.channel();

                logger.coreLog("Server::main() Redis-Protocol is listened at {}.", redis_port);
            }

            if (VENDER_ID < 3) {
                System.out.println("Begin to listen " + redis_port);
            } else {
                System.out.println("Start listening to port " + redis_port);
            }

            final PrintStream finalOut = System.out;
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    if (VENDER_ID < 3) {
                        logger.infoLog("Proxy is shutting down...");
                        finalOut.println("\nProxy is shutting down...");
                    }
                    channel_for_rds.close();
                    try {
                        channel_for_redis.close();
                    } catch (Throwable t) {
                    }
                    ServerBootstrapConfig config = bootstrap_for_redis.config();
                    try {
                        config.group().shutdownGracefully(1, 2, TimeUnit.SECONDS);
                    } catch (Throwable t) {
                    }
                    try {
                        config.childGroup().shutdownGracefully(1, 2, TimeUnit.SECONDS);
                    } catch (Throwable t) {
                    }

                    long inteval = ProxyConfig.getLongProperty("Server.Common.DataDump");

                    if (inteval > 0) {
                        if (VENDER_ID < 3) {
                            logger.infoLog("Saving data...");
                            finalOut.println("Try to save data automatically");
                        }
                    }

                    logger.coreLog("SERVER_STOP Proxy stop（in hook）.");
                    logger.close();
                    finalOut.println("Proxy stoped.");
                }
            });

            Timer timer = new Timer();//实例化Timer类
            TimerTask task = new TimerTask() {
                public void run() {
                    if (!SyncMonitor.isIncludingMyself()) {
                        logger.coreLog("TimerTask::run() No correct synchronization list found");
                        logger.close();
                        finalOut.println("No correct synchronization list found");
                        Runtime.getRuntime().exit(3);
                    }
                }
            };
            // 如果30秒钟内Server没有创建成功，程序会强行退出
            int deamonTime = 30_000;
            try {
                deamonTime = Integer.parseInt(ProxyConfig.getProperty("Server.Common.WaitingCenterCommand")) * 1000;
            } catch (Throwable t) {
            }
            if (deamonTime > 0) {
                timer.schedule(task, deamonTime);
            }

            logger.coreLog("Proxy start.");
            // 以下输出在节点管理器中用于判断是否启动成功，所以不能取消且必须是“Server started.”！
            System.out.println("\nProxy started.");

            // 重定向标准输出
            if (ProxyConfig.isRedirect()) {
                try {
                    String path = ProxyConfig.getServerHome();
                    File stdfile = new File(path + "/logs/stdout.log");
                    PrintStream out = new PrintStream(new OutputStreamLog(stdfile));
                    System.setOut(out);
                    System.setErr(out);
                } catch (Throwable t) {
                    if (logger != null) {
                        logger.warnLog("Server::() Redirect STDOUT failed: {}", t);
                    }
                }
            }
        } catch (Exception e) { // Display a message if anything goes wrong
            logger.coreLog(e, "SERVER_STOP Fatal error occur in starting proxy: {}", e);
            // e.printStackTrace();
            // 以下输出在节点管理器中用于判断是否启动失败，所以不能取消且必须是“Server start failed”开头！
            System.out.println("Proxy start failed: " + e);
            System.exit(1);
        }
    }
}
