package com.tongtech.proxy.core.protocol.redis;

import com.tongtech.proxy.core.protocol.DataResult;

import java.io.IOException;
import java.util.List;

public interface Processer {
    String name();
    boolean process(List argv, long receive_time, DataResult result) throws IOException;
}
