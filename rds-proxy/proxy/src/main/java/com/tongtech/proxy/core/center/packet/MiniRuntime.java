package com.tongtech.proxy.core.center.packet;

import com.tongtech.proxy.core.server.ProxyController;
import com.tongtech.proxy.core.sync.SyncMonitor;

import java.util.ArrayList;

import static com.tongtech.proxy.core.center.ProxyData.OBJECTTYPE_MINIRUNTIME;

public class MiniRuntime {
    private static final int TYPE = OBJECTTYPE_MINIRUNTIME;

    public synchronized void serialize(ArrayList data) {
        int head_offset = data.size();
        data.add(TYPE);
        data.add(null);

        // 节点执行命令后的状态
        data.add(ProxyController.INSTANCE.getCommandStatus());

        // 是否为主节点
        data.add(SyncMonitor.isMaster() ? 1 : 0);
        // 预留状态
        data.add(ProxyController.INSTANCE.getCommandStatus2());

        // 预留5个长整数位置
        data.add(0);
        data.add(0);
        data.add(0);
        data.add(0);
        data.add(0);

        int len = data.size() - head_offset;
        data.set(head_offset + 1, len);
    }
}
