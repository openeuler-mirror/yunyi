package com.tongtech.probe.stat;

public class StatProxyNode extends StatBaseNode {

    private Boolean valid;
    /* 哨兵端口 */
    private Integer redisPort;
    /* 安全级别, 0 Telnet none password, 1 SSL none password, 2 Telnet and password, 3 SSL and password */
    private Integer secureLevel;
    /* 启动以来运行了多少秒 */
    private Long running;

    private StatThroughput throughput;

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public Long getRunning() {
        return running;
    }

    public void setRunning(Long running) {
        this.running = running;
    }

    public Integer getSecureLevel() {
        return secureLevel;
    }

    public void setSecureLevel(Integer secureLevel) {
        this.secureLevel = secureLevel;
    }

    public StatThroughput getThroughput() {
        return throughput;
    }

    public void setThroughput(StatThroughput throughput) {
        this.throughput = throughput;
    }

    @Override
    public String toString() {
        return "StatNode{" +
                "valid=" + valid +
                ", expired=" + expired +
                ", remote='" + remote + '\'' +
                ", port=" + port +
                ", redisPort=" + redisPort +
                ", running=" + running +
                ", throughput=" + throughput +
                ", runtime=" + runtime +
                '}';
    }


}
