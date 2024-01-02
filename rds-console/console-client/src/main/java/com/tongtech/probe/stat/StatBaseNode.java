package com.tongtech.probe.stat;

import java.io.Serializable;

public class StatBaseNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /* 节点实例名称，全局唯一 */
    protected String instance;

    /* 数据是否过期, 大于3秒钟没有收到心跳就会变成true */
    protected Boolean expired;
    /* 外部连接的主机地址 */
    protected String remote;
    /* 服务端口 */
    protected Integer port;
    /* 性能统计 */
    protected StatRuntime runtime;

    protected String endpoint = null;

    public String getInstance() {
        return instance;
    }

    public String getEndpoint() {
        if(endpoint == null) {
            endpoint = remote + ":" + port;
        }

        return endpoint;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public StatRuntime getRuntime() {
        return runtime;
    }

    public void setRuntime(StatRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public String toString() {
        return "StatSentinel{" +
                "expired=" + expired +
                ", remote='" + remote + '\'' +
                ", port=" + port +
                ", runtime=" + runtime +
                '}';
    }

}
