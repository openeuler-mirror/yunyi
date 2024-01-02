package com.tongtech.proxy.core.sync;

import com.tongtech.proxy.core.utils.Log;
import com.tongtech.proxy.core.utils.OriginalSocketAddress;
import com.tongtech.proxy.core.utils.ProxyConfig;

public class SyncEndPoint {
    private final static Log Log = ProxyConfig.getServerLog();

    //    final String RawAddressAndPort;
//    private final String RawAddress;
//    private final String AliasAddress;
    private final OriginalSocketAddress Address;

    private volatile boolean IsAlive = false;

    private volatile boolean IsMySelf = false;

    private volatile int RedisPort = 0;
    private volatile String Identify = "0000000000000000";

    private volatile SyncNodeConnection Connection = null;

    private volatile long AliveCheckedTimestamp;

    private final Object SerialNumberLock = new Object();
    private long SerialNumber = 0;

//    private int FailTimes = 0;

    /**
     * @param address 此处的 Address 是从ProxyDynConfig中来的，包含了别名的信息
     */
    public SyncEndPoint(OriginalSocketAddress address) {
        this.Address = address;
    }

    public OriginalSocketAddress getAddress() {
        return this.Address;
    }

    /**
     * 返回当前的连接状态
     *
     * @return
     */
    public boolean isAlive() {
        return IsAlive;
    }

    public String getHost() {
        return Address.getHostString();
    }

//    public String getRawAddress() {
//        return RawAddress;
//    }

    public int getPort() {
        return Address.getPort();
    }

    public synchronized int getRedisPort() {
        return RedisPort;
    }

    public String getRemoteHostAlias() {
        return Address.getRemoteHastAlias();
    }

    public int getRedisPortAlias() {
        return Address.getRedisPortAlias();
    }

    public synchronized String getIdentify() {
        return Identify;
    }

    public synchronized boolean isMyself() {
        return IsMySelf;
//        return Identify.equals(Configuration.getIdentify());
    }

    /**
     * 状态检查更新时间
     *
     * @return
     */
    public long getAliveCheckedTimestamp() {
        return this.AliveCheckedTimestamp;
    }

    /**
     * 重新判断连接状态
     *
     * @return
     */
    public boolean checkAlive() {

        AliveCheckedTimestamp = System.currentTimeMillis();

        if (IsMySelf) {
            return true;
        }

        SyncNodeConnection conn = null;
        synchronized (this) {
            conn = Connection;
        }

        // 判断是否已经连接成功
        if (conn != null) {
            // 上次测试是通的
            if (conn.alive()) {
                // 现在还是通的
                Log.debugLog("SyncEndPoint::checkAlive() Connection to {} is alive.", Address);
                return true;
            } else {
                // 现在不通了，尝试重联
                if (connect()) {
                    // 重联成功
                    Log.infoLog("SyncEndPoint::checkAlive() Connection to {} is reconnected.", Address);
                    return true;
                } else {
                    Log.infoLog("SyncEndPoint::checkAlive() Connection to {} is broken.", Address);
                    return false;
                }
            }
        }

        // 程序执行到此处说明连接已经失败了
        // 节点曾经连接正常，刚刚连不上了，尝试次数还没有达到FAILOVER_TIMES次
        if (connect()) {
            IsAlive = true;
            Log.infoLog("SyncEndPoint::checkAlive() Connection to {} is connected.", Address);
            return true;
        } else {
            IsAlive = false;
            Log.debugLog("SyncEndPoint::checkAlive() Connection to {} is failed.", Address);
            return false;
        }
    }

    public SyncNodeConnection getConnection() {
        return Connection;
    }

    /**
     * 尝试连接，如果成功联通，则返回时Connection为实际连接对象，否则为null
     */
    private boolean connect() {
        SyncNodeConnection conn = null;
        synchronized (this) {
            conn = Connection;
            Connection = null;
        }

        try {
            if (conn != null) {
                conn.close();
            }
            Log.debugLog("SyncEndPoint::connect() try to connect {}", Address);
            conn = SyncMonitorUtil.getConnection(Address);
            Log.debugLog("SyncEndPoint::connect() {} is connected.", Address);
            if (conn.alive()) {
                int redisPort = conn.getRedisPort();
                String identify = conn.getIdentify();
                synchronized (this) {
                    this.Connection = conn;
                    this.RedisPort = redisPort;
                    this.Identify = identify;
                    this.IsMySelf = ProxyConfig.getIdentify().equals(identify);
                }
                if (this.IsMySelf) {
                    conn.close();
                }
                Log.debugLog("SyncEndPoint::connect() {}({}) rds-port and redis-ort({}) are alive."
                        , this.Identify, Address, this.RedisPort);
                return true;
            }
        } catch (Throwable t) {
            Log.debugLog(t, "SyncEndPoint::connect() Create connection to {} failed: ", Address, t);
        }
        if (conn != null) {
            conn.close();
        }
        Log.debugLog("SyncEndPoint::connect() Create connection to {} failed: rds-port or redis-port is die.", Address);

        return false;
    }

    long getSerialNumber() {
        synchronized (SerialNumberLock) {
            return SerialNumber;
        }
    }

    boolean setSerialNumber(long serialNumber) {
        synchronized (SerialNumberLock) {
            if (SerialNumber < serialNumber || SerialNumber - serialNumber > Integer.MAX_VALUE/*序列号回滚了*/) {
                SerialNumber = serialNumber;
                return true;
            }
            return false;
        }
    }

    //    @Override
//    public void finalize() throws Throwable {
//        try{
//            Connection.close();
//        }catch (Throwable t){}
//        super.finalize();
//    }
//

    @Override
    public int hashCode() {
        return Address.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SyncEndPoint) {
            SyncEndPoint other = (SyncEndPoint) o;
//            if (this.RawAddressAndPort == null && other.RawAddressAndPort != null) {
//                return false;
//            }
            return this.Address.equals(other.Address);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("EndPoint[id=").append(Identify).append(", address=").append(Address.getHostString()).append(':').append(Address.getPort()).append(", redis=").append(RedisPort);
        return buf.toString();
    }
}
