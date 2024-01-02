package com.tongtech.proxy.core.center.packet;

import com.tongtech.proxy.jmx.StatusColector;
import com.tongtech.proxy.core.server.ConnectionCounter;
import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.StaticContent;

import java.util.ArrayList;

import static com.tongtech.proxy.core.center.ProxyData.OBJECTTYPE_RUNTIME;

public class RunTime {
    private static final int TYPE = OBJECTTYPE_RUNTIME;

    public synchronized void serialize(ArrayList data) {
        int head_offset = data.size();
        data.add(TYPE);
        data.add(null);

        // 当前节点cluster分组号，不是集群状态时分组号为-1
        // proxy 没有此值，固定设置为0
        data.add(0);

        // 当前节点client的活跃连接数
        data.add(ConnectionCounter.getCurrentConnectionsRedis() + ConnectionCounter.getCurrentConnectionsRds());

        // 当前节点client的总连接数
        data.add(ConnectionCounter.getTotalConnectionsRedis() + ConnectionCounter.getTotalConnectionsRds());

        // 节点执行命令后的状态
        data.add(ProxyController.INSTANCE.getCommandStatus());

        long memory = StatusColector.getJvmAllocated();
        long memory_free = StatusColector.getJvmFree();
        // 当前节点对象占用内存总量
        data.add(memory - memory_free);

        // 当前节点已分配的内存总量
        data.add(memory_free);

        // 节点进程可申请到的最大内存
        // padding，该指标移到了NodeInfo中(20230830)
        data.add(0l);

        // 当前节点总的key 数量
        data.add(StatusColector.getTotalKeys());

        // 服务器自启动以来经过的秒数
        data.add(StaticContent.getStartedSeconds());

        // 网络流入
        data.add(StatusColector.getTotalNetworkInput());

        // 网络流出
        data.add(StatusColector.getTotalNetworkOutput());

        // 服务节点占用CPU
        data.add(StatusColector.getCpuProcessLoad());

        // 全系统占用CPU
        data.add(StatusColector.getCpuSystemLoad());

        // padding1
        data.add(0l);

        // padding2
        data.add(0l);

        // RDS协议当前连接数
        data.add(ConnectionCounter.getCurrentConnectionsRds());

        // Redis协议当前连接数
        data.add(ConnectionCounter.getCurrentConnectionsRedis());

        // padding5
        data.add(0l);

        // 有ttl设置尚未过期的key
        data.add(0);

        // 已经过期的key总数，递增
        data.add(0);

        // 查询命令命中次数
        data.add(0);

        // 查询命令失败次数
        data.add(0);

        // 被驱逐的key
        data.add(0);

//        // instance name
//        data.add(ProxyConfig.getInstance());

        int len = data.size() - head_offset;
        data.set(head_offset + 1, len);
    }
}
