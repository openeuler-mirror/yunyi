package com.tongtech.proxy.core.protocol;

import java.io.IOException;


public interface NioProcess {

    // 当程序收到请求时调用该函数
    boolean process(Object in, DataResult out) throws IOException;

}
