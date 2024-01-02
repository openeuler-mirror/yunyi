package com.tongtech.proxy.core.center.packet;

import com.tongtech.proxy.jmx.StatusColector;
import com.tongtech.proxy.Version;
import com.tongtech.proxy.core.StaticContent;
import com.tongtech.proxy.core.utils.CpuUtil;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.util.ArrayList;
import java.util.Vector;

import static com.tongtech.proxy.core.center.ProxyData.OBJECTTYPE_NODEINFO;

/**
 * 该类会在第一次连接center时发送，认证过了就不再发（认证不过直接断连接）
 * 未来可以将只发送1次的数据放到本类中
 */
public class NodeInfo {
    private static final int TYPE = OBJECTTYPE_NODEINFO;

    private volatile int SecureLevel;
    private volatile String RemoteAddr;
    private volatile int RdsPort;
    private volatile String RdsPassword;
    private volatile int RedisPort;
    private volatile String RedisPassword;

    /**
     * 对应Center的NodeInfo的proxyData方法
     *
     * @param data
     * @param offset
     */
    public synchronized void parse(Vector data, int offset) {
        //needTrusting = true;
        if (data == null || (Long) data.get(offset++) != TYPE) {
            return;
        }

        int length = ((Long) data.get(offset++)).intValue();

        SecureLevel = ((Long) data.get(offset++)).intValue();
        RemoteAddr = (String) data.get(offset++);
        RdsPort = ((Long) data.get(offset++)).intValue();

        RdsPassword = (String) data.get(offset++);
        RedisPort = ((Long) data.get(offset++)).intValue();
        RedisPassword = (String) data.get(offset++);
    }

    public synchronized void serialize(ArrayList data) {
        int head_offset = data.size();
        data.add(TYPE);
        data.add(null);

        // 以下内容只有memdb发给center的包中才有
        // 本节点安全级别
        data.add(ProxyConfig.getSecureLevel());
        // 本节点密码
        data.add(ProxyConfig.getAuthenString());
        // 本节点服务名
        data.add(ProxyConfig.getServiceName());
        // Redis Compatible port
        data.add(ProxyConfig.getRedisPort());
        // Redis password
        data.add(ProxyConfig.getRedisPwd());
        // Start time
        data.add(StaticContent.ServerStartTime);

        // 物理CPU数量
        data.add(CpuUtil.availableProcessors());
        // 物理内存
        data.add(StatusColector.getTotalPhysicalMemory());
        // 节点进程可申请到的JVM最大内存
        data.add(StatusColector.JVM_AVAILABLE);
        // 最大连接数
        data.add(ProxyConfig.getMaxConnections());
        // 版本号
        data.add(Version.Version);
        // 编译时间
        data.add(Version.BuildTime);

        // 节点对外公开的地址
        data.add(ProxyConfig.getAnnounceHost());
        // 节点对外公开的端口
        data.add(ProxyConfig.getAnnounceRedisPort());


        // 回写包长度
        int len = data.size() - head_offset;
        data.set(head_offset + 1, len);
    }
}
