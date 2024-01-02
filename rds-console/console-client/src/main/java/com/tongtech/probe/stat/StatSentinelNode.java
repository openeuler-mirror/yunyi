package com.tongtech.probe.stat;

import com.tongtech.probe.util.StringUtil;

import java.util.TreeMap;

public class StatSentinelNode extends StatBaseNode {

    private String group;

    private boolean myself;

    /**
     * 安全级别, 0 Telnet none password, 1 SSL none password, 2 Telnet and password, 3 SSL and password
     * 为之后扩展预留，目前setinels接口返回JSon 中暂无此属性。
     * */
    private Integer secureLevel;

    private TreeMap<String, StatEndPoint[]> services;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isMyself() {
        return myself;
    }

    public void setMyself(boolean myself) {
        this.myself = myself;
    }

    public Integer getSecureLevel() {
        return secureLevel;
    }

    public void setSecureLevel(Integer secureLevel) {
        this.secureLevel = secureLevel;
    }

    public TreeMap<String, StatEndPoint[]> getServices() {
        return services;
    }

    public void setServices(TreeMap<String, StatEndPoint[]> services) {
        this.services = services;
    }

    public static class StatEndPoint {
        private String endPoint;

        private Boolean alive;

        private Boolean master;

        private Integer port;

        private String host;

        public StatEndPoint() { }

        public StatEndPoint(String endPoint, Boolean alive, Boolean master) {
            setEndPoint(endPoint);
            this.alive = alive;
            this.master = master;
        }

        public Integer getPort() {
            return port;
        }

        public String getHost() {
            return host;
        }

        public String getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(String endPoint) {
            this.endPoint = endPoint;
            if(StringUtil.isNotEmpty(endPoint)) {
                String[] s = endPoint.split(":");
                if(s != null && s.length == 2) {
                    host = s[0].trim();
                    try {
                        port = Integer.parseInt(s[1].trim());
                    }
                    catch (NumberFormatException e) {
                        port = null;
                    }
                }
            }
        }

        public Boolean getAlive() {
            return alive;
        }

        public void setAlive(Boolean alive) {
            this.alive = alive;
        }

        public Boolean getMaster() {
            return master;
        }

        public void setMaster(Boolean master) {
            this.master = master;
        }
    }

}
