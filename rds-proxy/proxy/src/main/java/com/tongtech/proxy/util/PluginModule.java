package com.tongtech.proxy.util;

import java.util.List;
import java.util.Properties;

public interface PluginModule {
    /**
     * 初始化插件并设置属性，会将proxy.xml中针对该类配置的多个属性项内容解析为Properties实例并作为参数传入，例如：
     * <Plugins>
     * <Class suffix="^_^">com.server.modules.HashSplit2MultiModule</Class>
     * </Plugins>
     * 在初始化com.server.modules.HashSplit2MultiModule的实例时，会调用实例的init方法
     * 并在传入的properties参数中包含key=suffix，value=^_^的项。
     * <p>
     * 该方法只在对象实例化时调用一次，传递配置文件中的属性值
     *
     * @param properties
     */
    void init(Properties properties);

    /**
     * 当前插件需要截获的命令（命令只能用小写字符，不支持大写），例如：
     *
     * @return 小写的命令列表
     * @Override public String[] getCommands() {
     * return new String[]{"hset", "hget", "hdel", "hgetall", "hexists", "hincrby", "hincrbyfloat", "hkeys", "hlen"
     * , "hmget", "hmset", "hsetnx", "hvals", "hscan", "del", "type"};
     */
    String[] getCommands();

    /**
     * 命令处理方法
     *
     * @param in        客户端输入的命令，实际是Vector，其中第一项是字符串形式的敏弓，后面各项均为byte[]类型
     * @param interaction 源命令执行方法，该参数是一个Processor接口的实现类，用于提供插件使用标准命令访问RDS数据的能力。
     * @param db        当前操作的数据库id，Processor的call方法需要指定数据库id，通常使用此参数的值
     * @return 命令处理结果，根据对象不同代表不同含义：
     * :        Boolean：返回的是执行成功（对应协议接口返回的+OK）
     * :        Long：返回的是整数
     * :        Double：返回的是浮点数（RESP3支持）
     * :        String：返回的是简单字符串
     * :        byte[]：返回的是bulk字符串
     * :        null：空的bulk字符串
     * :        List：返回的是数组（数组可以嵌套）
     * :        Map：返回的是map（RESP3协议支持map）
     * :        Exception：返回的是执行失败，getMessage返回失败原因
     */
    Object process(List in, Interaction interaction, int db);

    /**
     * 为输入的byte数组计算hash值，方法保证hash的返回值为正数
     *
     * @param field
     * @param max
     * @return
     */
    default int getHash(byte[] field, int max) {
        if (max <= 0) {
            max = Integer.MAX_VALUE;
        }

        int h = 0;
        if (field != null && field.length > 0) {
            for (byte k : field) {
                int n = k & 0xff;
                h = h + n * 61 + (n >> 3);
            }
        }
        return (h & 0x7fffffff) % max;
    }
}
