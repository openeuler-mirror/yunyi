package com.tongtech.proxy.core.slices;

import io.netty.channel.Channel;

import java.io.IOException;
import java.util.List;

public interface ServiceMapping {
    void exchangeRedisData(ResultCallback callback, List data, int db) throws IOException;

    Channel getDirectRedisConnection(ResultCallback callback,int db);

    void releaseChannel(Channel channel);

    String getType();

    String getHost();

    int getStart();

    void setStart(int start);

    int getStop();

    void setStop(int stop);

    int getRedisPort();

    int getRdsPort();

    int getFreeConns();

    void closeChannel(Channel channel);

    void pullSliceDataFromMaster();

    List getSliceData();
}
