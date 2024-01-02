package com.tongtech.proxy.util;

import java.util.List;

public interface Interaction {

    /**
     * 查询当前的日志级别
     *
     * @return
     */
    LOGLEVEL logLevel();

    /**
     * 用RDS的日志类输出日志
     *
     * @param level
     * @param msg
     */
    void log(LOGLEVEL level, Object msg);

    /**
     * 访问RDS内部数据的接口
     *
     * @param data data中第一个字段必须是小写的字符串形式的redis兼容的命令，
     *             后面跟命令参数，可以是字符串也可以是字节数组，建议是字节数组
     * @param db   操作对应的数据库表，从0开始。（RDS是从1开始，DataResultPluginImp内需要转换）
     * @return RDS操作响应的结果，根据对象不同代表不同含义（需要根据实际发送的命令判断返回内容）：
     * Boolean：返回的是执行成功（例如set命令的返回）
     * Long：返回的是数值
     * Double：返回的是浮点数（RESP3支持，对应RESP2将返回bulk字符串）
     * String：返回的是简单字符串
     * byte[]：返回的是bulk字符串
     * null：空的bulk字符串
     * List：返回的是数组（数组可以嵌套）
     * Map：返回的是map（RESP3协议支持map）
     * Exception: 返回的是执行失败，getMessage返回失败原因
     */
    Object call(List data, int db);
}
