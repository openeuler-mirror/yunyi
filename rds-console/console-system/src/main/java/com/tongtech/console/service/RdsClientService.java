package com.tongtech.console.service;

import com.tongtech.console.utils.JedisDataClient;

public interface RdsClientService {


    JedisDataClient getConnectionClient(Long serviceId);

    /**
     * 断开当前所有的连接。
     * @return 返回断开连接的数量
     */
    int deleteConnections();

    int deleteConnection(Long serviceId);
}
