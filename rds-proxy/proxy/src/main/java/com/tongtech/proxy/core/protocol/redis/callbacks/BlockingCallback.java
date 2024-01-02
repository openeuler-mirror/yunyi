package com.tongtech.proxy.core.protocol.redis.callbacks;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.slices.ResultCallback;
import com.tongtech.proxy.core.protocol.DataResult;
import com.tongtech.proxy.core.protocol.SessionAttribute;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.slices.ServiceMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tongtech.proxy.core.StaticContent.*;

public class BlockingCallback implements ResultCallback {
    private final int command;
    private final DataResult result;
    private final List request;
    private final ArrayList<Channel> channels = new ArrayList();
    private volatile boolean isClose = false;

    public BlockingCallback(DataResult result, int cmd, List request) throws Throwable {
        this.result = result;
        this.command = cmd;
        this.request = request;
        SessionAttribute attribute = StaticContent.CachedSessionAttributes.get(result.getSession());
        attribute.setCallback(this);
        init(cmd, request);
    }

    private void init(int cmd, List request) throws Throwable {
        try {
            if (cmd == CMD_BRPOPLPUSH) {
                byte[] key = (byte[]) request.get(1);
                ServiceMapping node = ProxyController.INSTANCE.getNodeManager(key);
                Channel channel = node.getDirectRedisConnection(this, result.getTableId());
                if (channel != null) {
                    channels.add(channel);
                    ArrayList data = new ArrayList();
                    data.add("brpop");
                    data.add(key);
                    data.add(request.get(request.size() - 1));
                    channel.writeAndFlush(data);
                } else {
                    callback(null);
                }
            } else {
                ArrayList datas = new ArrayList();
                for (int i = 1; i < request.size() - 1; ++i) {
                    byte[] key = (byte[]) request.get(i);
                    ServiceMapping node = ProxyController.INSTANCE.getNodeManager(key);
                    Channel channel = node.getDirectRedisConnection(this, result.getTableId());
                    if (channel != null) {
                        synchronized (this) {
                            if (!isClose) {
                                channels.add(channel);
                                ArrayList data = new ArrayList();
                                data.add(request.get(0));
                                data.add(key);
                                data.add(request.get(request.size() - 1));
                                datas.add(data);
                            } else {
                                channel.close();
                                return;
                            }
                        }
                    }
                }

                synchronized (this) {
                    for (int i = 0; i < channels.size(); ++i) {
                        channels.get(i).writeAndFlush(datas.get(i));
                    }
                }
            }
        } catch (Throwable t) {
            channelClosed(result.getSession());
            throw t;
        }
    }

    /**
     * @param o 服务节点返回的内容，连接中断时返回 Exception
     * @throws IOException
     */
    @Override
    public void callback(Object o) throws IOException {
        if (this.command == CMD_BRPOPLPUSH) {
            if (o instanceof byte[]) {
                byte[] key = (byte[]) this.request.get(2);
                ServiceMapping node = ProxyController.INSTANCE.getNodeManager(key);
                if (node != null) {
                    ArrayList data = new ArrayList();
                    data.add("lpush");
                    data.add(key);
                    data.add(o);

                    node.exchangeRedisData(NullCallback.INSTANCE, data, result.getTableId());
                }
            }
            result.sendObject(o);
        } else {
            // blpop、brpop、bzpopmin、bzpopmax
            boolean need_result = false;
            synchronized (this) {
                isClose = true;
                if (channels.size() > 0) {
                    for (Channel channel : channels) {
                        try {
                            channel.close();
                        } catch (Throwable t) {
                        }
                    }
                    channels.clear();
                    need_result = true;
                }
            }
            if (need_result) {
                result.sendObject(o);
            }
        }
    }

    @Override
    public synchronized void channelClosed(ChannelHandlerContext ctx) {
        isClose = true;
        for (Channel channel : channels) {
            channel.close();
        }
        channels.clear();
    }
}
