package com.tongtech.probe.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 安装点（probe）整体状态监控信息。包括每一个安装节点的状态监控信息（Statisticses）
 */
public class NodeStatisticses implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String host;

    private String os;
    //private final String os = OSUtil.getOsName();

    private String pid; //进程id

    private List<StatisticsPojo> nodes;

    public NodeStatisticses() {

    }

    public NodeStatisticses(String host, String os, String pid) {
        this.host = host;
        this.os = os;
        this.pid = pid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public List<StatisticsPojo> getNodes() {
        return nodes;
    }

    public void setNodes(List<StatisticsPojo> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
//        StringBuffer buf = new StringBuffer("[");
//        nodes.forEach(stat -> {
//            buf.append(stat.toString()).append(',');
//        });
//        buf.append("]");
        return "NodeStatisticses{" +
                "host='" + host + '\'' +
                ", os='" + os + '\'' +
                ", pid='" + pid + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}
