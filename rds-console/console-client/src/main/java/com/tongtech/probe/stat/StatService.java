package com.tongtech.probe.stat;

import java.io.Serializable;
import java.util.*;

public class StatService implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    private String type; //"DEFAULT", "SENTINEL", "CLUSTER", "SCALABLE"

    private String message;

    /* 活着的节点endpoint列表，如：[ "192.168.3.4:6223", "192.168.3.4:6224" ] */
    private String[] alive;

    private StatRuntime statistics;

    /* 服务中包含的工作节点列表 */
    private List<StatWorkerNode> nodes;

    /* Sentinel服务时master节点的endpoint， 如："192.168.3.4:6223"*/
    private String master;
    /* Sentinel需要同步节点的endpoint列表， ，如：[ "192.168.3.4:6223", "192.168.3.4:6224" ] */
    private String[] syncList;
    /* 每个shard中节点的数量，最小是1 */
    private Integer replicas;
    /* 热备节点的数量 */
    private Integer hotspares;
    /* 服务状态目前只有："RUNNING" 和 "CHANGING" */
    private String status;
    /* 热备节点endpoint列表, 如：["192.168.0.90:9202", "192.168.0.90:9201"]  */
    private String[] hotspare;
    /* shard最多数量 */
    private Integer maxShards;

    private List<Shard> shard;
    /* 服务中包含的代理节点列表 */
    private List<StatProxyNode> proxies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getAlive() {
        return alive;
    }

    public void setAlive(String[] alive) {
        this.alive = alive;
    }

    public StatRuntime getStatistics() {
        return statistics;
    }

    public void setStatistics(StatRuntime statistics) {
        this.statistics = statistics;
    }

    public List<StatWorkerNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<StatWorkerNode> nodes) {
        this.nodes = nodes;
    }

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public String[] getSyncList() {
        return syncList;
    }

    public void setSyncList(String[] syncList) {
        this.syncList = syncList;
    }

    public List<Shard> getShard() {
        return shard;
    }

    public void setShard(List<Shard> shard) {
        this.shard = shard;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public Integer getHotspares() {
        return hotspares;
    }

    public void setHotspares(Integer hotspares) {
        this.hotspares = hotspares;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getHotspare() {
        return hotspare;
    }

    public void setHotspare(String[] hotspare) {
        this.hotspare = hotspare;
    }

    public Integer getMaxShards() {
        return maxShards;
    }

    public void setMaxShards(Integer maxShards) {
        this.maxShards = maxShards;
    }

    public List<StatProxyNode> getProxies() {
        return proxies;
    }

    public void setProxies(List<StatProxyNode> proxies) {
        this.proxies = proxies;
    }

    /**
     * 对Nodes再加工，保证node中的属性和RdsNode一致
     * 需要赋值的属性：shard, slot, hotSpares
     */
    public void reprocessNodes() {
        String trimType = this.type.toUpperCase().trim();
        boolean isScalable = "SCALABLE".equals(trimType);
        boolean isCluster = "CLUSTER".equals(trimType);
        if(isScalable || isCluster) {
            Set<String> hotSparesEndpoint = null;
            if(hotspare != null) {
                hotSparesEndpoint = new HashSet<>(Arrays.asList(hotspare));
            }

            for (StatWorkerNode node : this.nodes) {
                if (hotSparesEndpoint != null && hotSparesEndpoint.contains(node.getEndpoint())) {
                    node.setHotSpares(true);
                }

                if(this.shard != null) {
                    if(node.getShardId() != null) {
                        //System.out.println("~~~~~~~~~~~~~~~this.shard.size():" + this.shard.size());
                        //System.out.println("~~~~~~~~~~~~~~~node.getShardId():" + node.getShardId());
                        if(node.getShardId() > 0 && node.getShardId() < this.shard.size()) {
                            //CLUSTER 模式下需要通过 shardId 来找到对应的shard
                            Shard s = this.shard.get(node.getShardId());
                            node.setShard(node.getShardId());
                            node.setSlot(s.getSlotsStr());
                        }
                    }
                    else {
                        int shardLen = this.shard.size();
                        for (int i = 0; i < shardLen; i++) {
                            Shard s = this.shard.get(i);
                            if (s.containsEndpoint(node.getEndpoint())) {
                                node.setShard(i);
                                node.setSlot(s.getSlotsStr());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String toString() {
        return "StatService{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", alive=" + Arrays.toString(alive) +
                ", statistics=" + statistics +
                ", nodes=" + nodes +
                ", master='" + master + '\'' +
                ", syncList=" + Arrays.toString(syncList) +
                ", replicas=" + replicas +
                ", hotspares=" + hotspares +
                ", status='" + status + '\'' +
                ", hotspare=" + Arrays.toString(hotspare) +
                ", maxShards=" + maxShards +
                ", shard=" + shard +
                ", proxies=" + proxies +
                '}';
    }

    public static class Shard {
        /* 节点Endpoint列表，如：[ "localhost:6231",  "localhost:6232" ] */
        private String[] endpoints;
        /* slots 区段， 如： ["8192-16383"] */
        private String[] slots;

        private Set<String> endpointsSet;

        public String[] getEndpoints() {
            return endpoints;
        }

        public boolean containsEndpoint(String endpoint) {
            if(endpointsSet == null) {
                endpointsSet = new HashSet<>(Arrays.asList(endpoints));
            }
            return endpointsSet.contains(endpoint);
        }

        public void setEndpoints(String[] endpoints) {
            this.endpoints = endpoints;
        }

        public String[] getSlots() {
            return slots;
        }

        /**
         * 把多个或一个slot平成，逗号间隔的字符串。如："1-2334,3000-5555,8192-16383",
         * @return
         */
        public String getSlotsStr() {
            if(slots == null || slots.length == 0) {
                return null;
            }
            else {
                if(slots.length == 1) { return slots[0]; }
                else {
                    StringBuilder buf = new StringBuilder();
                    buf.append(slots[0]);
                    for(int i = 1 ; i < slots.length ; i ++) {
                        buf.append(',').append(slots[i]);
                    }
                    return buf.toString();
                }
            }
        }


        public void setSlots(String[] slots) {
            this.slots = slots;
        }

        @Override
        public String toString() {
            return "Shard{" +
                    "endpoints=" + Arrays.toString(endpoints) +
                    ", slots=" + Arrays.toString(slots) +
                    '}';
        }
    }
}
