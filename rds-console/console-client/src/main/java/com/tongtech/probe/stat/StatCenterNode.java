package com.tongtech.probe.stat;

public class StatCenterNode extends StatBaseNode {

    /* 哨兵端口 */
    private Integer sentinelPort;

    public Integer getSentinelPort() {
        return sentinelPort;
    }

    public void setSentinelPort(Integer sentinelPort) {
        this.sentinelPort = sentinelPort;
    }

    @Override
    public String toString() {
        return "StatCenter{" +
                "expired=" + expired +
                ", remote='" + remote + '\'' +
                ", port=" + port +
                ", sentinelPort=" + sentinelPort +
                ", runtime=" + runtime +
                '}';
    }
}

