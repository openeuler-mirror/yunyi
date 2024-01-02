package com.tongtech.proxy.core.sync;

import com.tongtech.proxy.core.utils.ProxyConfig;
import com.tongtech.proxy.core.utils.Log;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import static com.tongtech.proxy.core.server.io.CodecContext.NULL_ARRAY_OBJECT;

public class SyncSender {

    // 同步列表中的数据已经全部同步完成后，是否清理动态分配的大对象占用的内存
    // 实际应用中如果有很多的大对象同步操作，会造成同步列表占用大量内存
    private static final boolean CleanBigValue = !"False".equalsIgnoreCase(ProxyConfig.getProperty("Server.Common.CompressSyncList"));

    // 配置文件中读到的地址，保持配置文件的顺序
    private volatile InetSocketAddress[] Addresses;

    // Addresses列表中指向自己的连接是第几个
    private volatile int MySelfAddressCount = -1;

    // 同步列表长度
    private final int LengthOfList;

    // 日志类
    private static final Log Logger = ProxyConfig.getServerLog();

    // 到各server的连接类
    private final ArrayList<SyncConnector> SyncConns = new ArrayList<>();

    // 队列堆积警告标志
    private final ArrayList<Boolean> WarningSign = new ArrayList<>();

    // 队列最后1条发送记录的时间戳
//    private final ArrayList<Long> LastSend = new ArrayList<>();

    // 告警线
    private final int WarningLine;

    // 告警恢复
    private final int ResumeLine;

    // 列表头位置,即当前发送数据位置
    private final ArrayList<Integer> Begins = new ArrayList<>();

    // 列表尾位置,即插入下一条数据的位置
    private int End = 0;

    // 队列时间段内的最大值
    private final ArrayList<Integer> Maxes = new ArrayList<>();

    //private final int MaxSyncServer;

    // 是否要保证全部数据完整同步
    private final boolean MustSynchronized;

    private final Integer TableId;

    private final List[] SyncData;

    private final long DataTimestamp[];

    private long CurrentSN;

    public SyncSender(int table_id, int list_len, boolean must_sync) {

        TableId = Integer.valueOf(table_id);

        LengthOfList = list_len;

        MustSynchronized = must_sync;

        SyncData = new List[list_len];

        DataTimestamp = new long[list_len];

        int warning = 50;
        int w_line = list_len * warning / 100;
        int r_line = w_line >> 1;

        // 当SyncServer.Warning配置大于100或小于0时告警功能失效
        if (w_line < 0) {
            w_line = list_len + 1;
        }
        if (r_line <= 0) {
            r_line = 1;
        }
        WarningLine = w_line;
        ResumeLine = r_line;

        Logger.infoLog("SyncSender::() Sender for table--0 {} created", this);
    }

    /**
     * 该方法在SyncConnector对象中被调用，当Connector发现连接的对端时自己时调用本方法
     *
     * @param conn
     */
    synchronized void removeMyselfConnector(SyncConnector conn) {
        if (conn != null) {
            for (int i = 0; i < SyncConns.size(); i++) {/**/
                if (SyncConns.get(i) == conn) {
                    try {
                        InetSocketAddress myself = conn.getServerAddress();
                        for (int j = 0; Addresses != null && j < Addresses.length; ++j) {
                            if (myself.equals(Addresses[j])) {
                                MySelfAddressCount = j;
                                break;
                            }
                        }
                    } catch (Throwable t) {
                    }
                    removeConnector(i);
                    break;
                }
            }
        }
    }

    public synchronized void reloadConnector(InetSocketAddress[] addrs) {
        if (addrs == null || addrs.length == 0) {
            // 如果输入参数为空，关闭所有连接返回
            for (int i = 0; i < SyncConns.size(); i++) {
                try {
                    SyncConns.get(i).close();
                } catch (Exception e) {
                }
            }
            SyncConns.clear();
            Addresses = null;
            MySelfAddressCount = 0;
            Logger.debugLog("SyncSender::reloadConnector() New configuration is null, close all current connections");
            return;
        }

        // 删除取消掉的配置
        for (int i = 0; Addresses != null && i < Addresses.length; i++) {
            InetSocketAddress addr = Addresses[i];
            if (!isContains(addrs, addr)) {
                removeConnector(addr);
                Logger.debugLog("SyncSender::reloadConnector() Remove obsolete connection {}", addr);
            }
        }

        // 增加新的配置
        for (int i = 0; addrs != null && i < addrs.length; i++) {
            InetSocketAddress addr = addrs[i];
            if (!isContains(Addresses, addr)) {
                addConnector(addr);
                Logger.debugLog("SyncSender::reloadConnector() Add new connection to {}", addr);
            }
        }

        // 保存更新后的配置
        Addresses = addrs;
        MySelfAddressCount = 0;
    }

    /**
     * 判断数组中是否包含指定元素
     *
     * @param addrs
     * @param addr
     * @return
     */
    private boolean isContains(InetSocketAddress[] addrs, InetSocketAddress addr) {
        if (addrs == null || addrs.length == 0) {
            return false;
        }
        for (InetSocketAddress s : addrs) {
            if (s != null && s.equals(addr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在SyncConns中删除和输入相同的InetSocketAddress
     * in lock
     *
     * @param addr
     */
    private void removeConnector(InetSocketAddress addr) {
        for (int i = SyncConns.size() - 1; i >= 0; i--) {
            try {
                if (SyncConns.get(i).getServerAddress().equals(addr)) {
                    removeConnector(i);
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * 删除SyncConns中指定位置的连接
     * in lock
     *
     * @param i
     */
    private void removeConnector(int i) {
        if (i >= 0 && i < SyncConns.size()) {
            SyncConnector conn = SyncConns.get(i);
            SyncConns.set(i, null);
            if (conn != null) {
                conn.close();
            }
            Maxes.set(i, 0);
            Begins.set(i, 0);
            WarningSign.set(i, false);
        }
    }

    // in lock
    private void addConnector(InetSocketAddress address) {
        if (address != null) {
            int i = 0;
            for (; i < SyncConns.size(); i++) {
                if (SyncConns.get(i) == null) {
                    break;
                }
            }
            SyncConnectorImp connector = new SyncConnectorImp("Sender-" + i, this, TableId, i, address, ProxyConfig.getSecureLevel(), ProxyConfig.getAuthenString());

            if (i == SyncConns.size()) {
//                SyncConns.add(new SyncConnectorMina("Sender-" + TableId + "-" + i, this, i, address));
                SyncConns.add(connector);
                Maxes.add(i, 0);
                Begins.add(i, End);
                WarningSign.add(i, false);
            } else {
//                SyncConns.set(i, new SyncConnectorMina("Sender-" + TableId + "-" + i, this, i, address));
                SyncConns.set(i, connector);
                Maxes.set(i, 0);
                Begins.set(i, End);
                WarningSign.set(i, false);
            }
        }
    }

    /**
     * @param cmd
     * @param key
     * @param value
     * @param timestamp
     * @param idxes
     * @throws Exception
     */
    public void putData(int cmd, String key, List<String> value, long timestamp,
                        List<String> idxes) throws IOException {

        // 同步命令
        int sub_cmd = cmd & 0xfffff00;

        ArrayList data = new ArrayList(7);
        // SN
        data.add(null);
        // time-stamp
        data.add(timestamp);
        // cmd
        data.add("sync_cmd_" + (sub_cmd >> 8));
        // table_id
        data.add(TableId);
        // key
        data.add(key);
        // value
        if (value != null) {
            data.add(value);
        } else {
            data.add(NULL_ARRAY_OBJECT);
        }
        // indexes
        if (idxes != null) {
            data.add(idxes);
        }

        synchronized (this) {
            try {
                // 准备写同步列表
                int local_end = End;
                CurrentSN = SyncMonitor.getAndIncrease();
                // update SN
                data.set(0, CurrentSN);
                DataTimestamp[local_end] = System.currentTimeMillis();
                SyncData[local_end] = data;

                // 列表结束位置后移
                local_end++;

                // 列表结束位置已经移出列表
                if (local_end == LengthOfList) {
                    local_end = 0;
                }
                End = local_end;

                /**
                 * 以下循环更新各队列的开始位置（Begins中的值）
                 * 由于发送线程调用getData方法时会通过Begins的值判断是否需要清理同步列表中的动态数据（新增功能）
                 * 如果不先更新会导致判断错误而清空数据
                 */
                for (int i = 0; i < SyncConns.size(); i++) {
                    SyncConnector conn = SyncConns.get(i);
                    if (conn != null && conn.isClose()) {
                        // 指定连接已经关闭,关闭的原因通常是一个指向自己的连接
                        // 所以这样的连接是不需要同步数据的
                        removeConnector(i);
                        continue;
                    }
                    if (conn == null) {
                        continue;
                    }

                    int local_begin = Begins.get(i);

                    // 如果移动后的结束位置和开始位置相同，说明列表已经满了，丢弃列表中最早的（Begins.get(i)对应的位置）项
                    if (local_end == local_begin) {
                        local_begin++;
                        // 整理列表开始位置
                        if (local_begin == LengthOfList) {
                            local_begin = 0;
                        }
                        Begins.set(i, local_begin);

                        // 记录已经超出队列,直接设置为最大长度
                        Maxes.set(i, LengthOfList);
                        Logger.warnLog("SyncSender::putData() List is full, discarded the oldest data on '{}'"
                                , conn.getServerAddress());
                    }

                    // 计算当前队列最大值
                    int max = length(i);
                    if (Maxes.get(i) < max) {
                        Maxes.set(i, max);
                    }
                    if (max > WarningLine && !WarningSign.get(i)) {
                        // warning
                        Logger.warnLog("SyncSender::putData() Data piled on '{}'"
                                , conn.getServerAddress());
                        WarningSign.set(i, true);
                    } else if (WarningSign.get(i) && max < ResumeLine) {
                        Logger.warnLog("SyncSender::putData() The piles is resuming on '{}'"
                                , conn.getServerAddress());
                        WarningSign.set(i, false);
                    }

                }

                // System.out.println("SynchronizeServer::putSyncData Begin = "
                // + local_begin + ", End = " + local_end);
            } catch (Exception e) {
                if (Logger.isInfo()) {
                    StringBuilder buf = new StringBuilder(128);
                    for (int i = 0; idxes != null && i < idxes.size(); i++) {
                        if (i > 0) {
                            buf.append(' ');
                        }
                        buf.append(idxes.get(i));
                    }

                    Logger.warnLog(e, "SyncSender::putData() Put data {}<{}> = {} failed: {}"
                            , key, buf, value, e.getMessage());
                } else {
                    Logger.warnLog("SyncSender::putData() Put data {}<...> = {} failed: {}"
                            , key, value, e);
                }
                throw e;
            } finally {
                // 尝试唤醒所有相关的数据同步线程
                notifyAll();
            }
        }
    }

    /**
     * 清理剩余数据
     */
    public synchronized void close() {
        for (int i = 0; i < SyncConns.size(); i++) {
            try {
                SyncConns.get(i).close();
            } catch (Exception e) {
                Logger.infoLog("SyncSender::close() Error occur when close _{}: {}", i, e);
            }
        }
        Logger.infoLog("Sender::close() Sender-{} closed.", TableId);
    }

    /**
     * 读当前队列到各server同步数据的长度
     *
     * @param idx
     */
    private int length(int idx) {
        // 程序里面凡是closeSycnConnector的地方都同步清空了变量，
        // 所以SyncConns.get(idx)==null 和 SyncConns.get(idx).isClose()必然同时发生
//        if (idx < 0 || idx >= SyncConns.size() || SyncConns.get(idx) == null || SyncConns.get(idx).isClose()) {
        if (idx < 0 || idx >= SyncConns.size() || SyncConns.get(idx) == null) {
            return -1;
        }
        int len = End - Begins.get(idx);
        if (len < 0) {
            len += LengthOfList;
        }
        return len;
    }

    /**
     * 读当前队列到各server同步数据的最大长度
     */
    public synchronized HashMap<InetSocketAddress, Integer> getMax() {
        HashMap<InetSocketAddress, Integer> max = new HashMap<>();
        for (int i = 0; i < SyncConns.size(); i++) {
            SyncConnector conn = SyncConns.get(i);
            if (conn != null && !conn.isClose()) {
                int len = length(i);
                Maxes.set(i, len);
                if (conn.isConnected()) {
                    max.put(conn.getServerAddress(), len);
                } else {
                    max.put(conn.getServerAddress(), -1);
                }
            }
        }
        return max;
    }

    /**
     * 取数据函数
     *
     * @return true：如果有数据则返回真
     */
    synchronized List getData(int server_no) {

        int local_begin = Begins.get(server_no);

        if (local_begin == End) {
            // 发送队列为空
            try {
                // 休息1秒后如果还是空则返回
                // 线程返回后会检测连接是否健康，所以在此处不能无限循环
                wait(1000);
                local_begin = Begins.get(server_no);
            } catch (InterruptedException e) {
                return null;
            }
        }

        if (local_begin == End) {
            // 发送队列为空
            return null;
        }

        boolean isSlowest = false;

        // 以下代码判断指定的队列是否是最慢的一个队列，20220617增加
        if (CleanBigValue) {
            int last_no = -1;
            int the_maxlen = -1;// 最慢的列表长度
            int the_secondlen = -2;// 倒数第二慢的列表长度
            for (int i = 0; i < Begins.size(); ++i) {
                int len = length(i);
                if (len <= 0) {
                    continue;
                } else if (the_maxlen <= len) {
                    the_secondlen = the_maxlen;
                    the_maxlen = len;
                    last_no = i;
                }
            }
            // 当前的server_no是唯一最慢的（不能是并列最慢）
            isSlowest = last_no == server_no && the_maxlen != the_secondlen;
        }
        // 同步最慢判断完毕

        /**
         * 生成报文
         */


        List ret = SyncData[local_begin];

        if (isSlowest) {
            clearBigValue(local_begin);
            if (Logger.isDebug()) {
                Logger.debugLog("SyncSender::getData() Dynamic value at {} is cleaned by the lastest list-{}", local_begin, server_no);
            }
        } else {
            if (Logger.isDebug()) {
                Logger.debugLog("SyncSender::getData() Dynamic value at {} is get by list-{}", local_begin, server_no);
            }
        }

        // 列表开始位置后移
        local_begin++;
        // 调整列表开始位置
        if (local_begin >= LengthOfList) {
            local_begin = 0;
        }
        Begins.set(server_no, local_begin);
//        }

        return ret;
    }

    /**
     * 根据远端服务器重启时间判断已过期的同步记录(远端服务器会通过rescue功能恢复这些数据)
     *
     * @param server_no
     * @param start_time
     */
    synchronized void remoteRestart(int server_no, long start_time) {
        int begin = Begins.get(server_no);
        int end = End;
        // if (end < 0) {
        // end += LengthOfList;
        // }

        // int old_len = end - begin;
        // if (old_len < 0) {
        // old_len += LengthOfList;
        // }
        // System.out.println("SynchronizeServer::remoteRestart() len in "
        // + server_no + " is " + old_len + ", start is " + start_time);

        for (int i = 0; i < 20; i++) {
            int half = getHalf(begin, end);

            if (Logger.isDebug()) {
                Logger.debugLog("SyncSender::remoteRestart() Loop {} times, head = {}, half = {}, tail = {}"
                        , i, begin, half, end);
            }

            if (begin == half || half == end) {
                // System.out
                // .println("SynchronizeServer::remoteRestart() loop end.");
                break;
            }

            long begin_time = DataTimestamp[begin];
            long half_time = DataTimestamp[half];
            // long end_time = SyncDatas.getLong(COLUMN_TIMESTAMP_OFFSET, end);

            // System.out
            // .println("SynchronizeServer::remoteRestart() timestamp is "
            // + begin_time + "(" + begin + ") " + half_time + "("
            // + half + ") " + end_time + "(" + end + ").");

            // 判断循环是否结束
            if (begin_time >= start_time) {
                // begin对应的项就是有效项
                break;
            }

            // 调整范围继续循环
            if (half_time >= start_time) {
                // 等于的点在begin和half之间
                end = half;
            } else {
                // 等于的点在half和end之间
                begin = half;
            }
        }

        Begins.set(server_no, begin);

        // infolog("SynchronizeServer::remoteRestart() The head of the list change to "+begin);
    }

    /**
     * 根据输入计算给定列表段的中间位置,目前remoteRestart由调用
     *
     * @param head 列表头,最先进入的列表项的位置
     * @param tail 列表尾,最后进入的列表项的位置
     * @return
     */
    private int getHalf(int head, int tail) {
        if (head < tail) {
            return (head + tail) / 2;
        } else if (head > tail) {
            int half = (head + tail + LengthOfList) / 2;
            if (half >= LengthOfList) {
                half -= LengthOfList;
            }
            return half;
        } else {
            return head;
        }
    }


    private void clearBigValue(int pos) {
//        SyncDatas.clearBigValue(COLUMN_VALUE_OFFSET, pos);
        SyncData[pos] = null;
    }

//    private void protocolObject(Object o, ByteArrayOutputStream buf) throws IOException {
//        if (o == null) {
//            buf.write('$');
//            buf.write('-');
//            buf.write('1');
//            buf.write('\n');
////            buf.append("$-1").append('\n');
//        } else if (o instanceof List) {
//            List l = (List) o;
//            buf.write('*');
//            buf.write(Integer.toString(l.size()).getBytes());
//            buf.write('\n');
////            buf.append('*').append(l.size()).append('\n');
//            for (int i = 0; i < l.size(); ++i) {
//                protocolObject(l.get(i), buf);
//            }
//        } else if (o instanceof Long) {
//            long l = ((Long) o).longValue();
//            buf.write(':');
//            buf.write(Long.toString(l).getBytes());
//            buf.write('\n');
////            buf.append(':').append(l).append('\n');
//        } else if (o instanceof Integer) {
//            int l = ((Integer) o).intValue();
//            buf.write(':');
//            buf.write(Integer.toString(l).getBytes());
//            buf.write('\n');
////            buf.append(':').append(l).append('\n');
//        } else if (o instanceof byte[]) {
//            byte[] b = (byte[]) o;
//            buf.write('$');
//            buf.write(Integer.toString(b.length).getBytes());
//            buf.write('\n');
//            buf.write(b);
//            buf.write('\n');
//        } else if (o instanceof RdsString) {
//            byte[] b = ((RdsString) o).message();
//            buf.write('$');
//            buf.write(Integer.toString(b.length).getBytes());
//            buf.write('\n');
//            buf.write(b);
//            buf.write('\n');
//        } else {
//            byte[] str = o.toString().getBytes(StandardCharsets.UTF_8);
//            buf.write('$');
//            buf.write(Integer.toString(str.length).getBytes());
//            buf.write('\n');
//            buf.write(str);
//            buf.write('\n');
//        }
//    }
}
