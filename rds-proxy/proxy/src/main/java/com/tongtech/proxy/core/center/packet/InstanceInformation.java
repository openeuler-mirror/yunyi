package com.tongtech.proxy.core.center.packet;


import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.ProxyConfig;

import java.util.List;

import static com.tongtech.proxy.core.center.ProxyData.OBJECTTYPE_INSTANCE;


public class InstanceInformation {
    private static Log logger = ProxyConfig.getServerLog();

    private static final int TYPE = OBJECTTYPE_INSTANCE;

    public synchronized void serialize(List data) {
        int head_offset = data.size();
        data.add(TYPE);
        data.add(null);

        // instance name
        data.add(ProxyConfig.getInstance());
        // Group name
        data.add(ProxyConfig.getGroupName());

        int len = data.size() - head_offset;
        data.set(head_offset + 1, len);
    }
}
