package com.tongtech.probe.stat;

import java.util.List;

public class StatWorkerNode extends StatBaseNode {

    private Boolean valid;

    private Boolean current;
    /* 哨兵端口 */
    private Integer redisPort;
    /* 安全级别, 0 Telnet none password, 1 SSL none password, 2 Telnet and password, 3 SSL and password */
    private Integer secureLevel;
    /* 启动以来运行了多少秒 */
    private Long running;
    /* 是否为主节点 */
    private Boolean master;

    /** 是否为热备节点, 需要额外运算赋值 */
    private boolean hotSpares;

    /** 分片的插槽范围, 需要额外运算赋值 */
    private String slot;

    /** 分片的编号, 需要额外运算赋值 */
    private Integer shard;

    /**
     *  在CLUSTER模式下（不包括SCALABLE模式），标识所属分片的索引号（0 based)
     *  目前只有在 在CLUSTER模式下的工作节点，才会有该属性。
     *  用来对应 StatService.shard[] 中对应的数组index,
     *  因为该模式下 StatService.shard.endpoints 记录的地址，是配置中地址可能和node.remote中记录的地址不一致。
     *     比如：StatService.shard.endpoints[0] = "localhost:6311" 而与之对应 node.remote = '192.168.0.90：:6311"
     */
    private Integer shardId;

    private List<TableUsing> used;

    private StatThroughput throughput;


    public boolean isHotSpares() {
        return hotSpares;
    }

    public void setHotSpares(boolean hotSpares) {
        this.hotSpares = hotSpares;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Integer getShard() {
        return shard;
    }

    public void setShard(Integer shard) {
        this.shard = shard;
    }

    public Integer getShardId() {
        return shardId;
    }

    public void setShardId(Integer shardId) {
        this.shardId = shardId;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public Boolean getCurrent() {
        return current;
    }

    public void setCurrent(Boolean current) {
        this.current = current;
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

    public List<TableUsing> getUsed() {
        return used;
    }

    public Integer getSecureLevel() {
        return secureLevel;
    }

    public void setSecureLevel(Integer secureLevel) {
        this.secureLevel = secureLevel;
    }

    public void setUsed(List<TableUsing> used) {
        this.used = used;
    }

    public StatThroughput getThroughput() {
        return throughput;
    }

    public void setThroughput(StatThroughput throughput) {
        this.throughput = throughput;
    }

    public Boolean getMaster() {
        return master;
    }

    public void setMaster(Boolean master) {
        this.master = master;
    }

    @Override
    public String toString() {
        return "StatWorkerNode{" +
                "valid=" + valid +
                ", current=" + current +
                ", redisPort=" + redisPort +
                ", secureLevel=" + secureLevel +
                ", running=" + running +
                ", master=" + master +
                ", hotSpares=" + hotSpares +
                ", slot='" + slot + '\'' +
                ", shard=" + shard +
                ", shardId=" + shardId +
                ", used=" + used +
                ", throughput=" + throughput +
                ", instance='" + instance + '\'' +
                ", expired=" + expired +
                ", remote='" + remote + '\'' +
                ", port=" + port +
                ", runtime=" + runtime +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }

    public static class TableUsing {
        /* table 名字 如："table-1" */
        private String name;
        /* Key 的最大数 */
        private Long capacity;
        /* 当前使用的Key数量 */
        private Long used;

        private Long meanSquare;
        /* 使用比例, 如："0.0%"*/
        private String usedRate;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getCapacity() {
            return capacity;
        }

        public void setCapacity(Long capacity) {
            this.capacity = capacity;
        }

        public Long getUsed() {
            return used;
        }

        public void setUsed(Long used) {
            this.used = used;
        }

        public Long getMeanSquare() {
            return meanSquare;
        }

        public void setMeanSquare(Long meanSquare) {
            this.meanSquare = meanSquare;
        }

        public String getUsedRate() {
            return usedRate;
        }

        public void setUsedRate(String usedRate) {
            this.usedRate = usedRate;
        }

        @Override
        public String toString() {
            return "TableUsing{" +
                    "name='" + name + '\'' +
                    ", capacity=" + capacity +
                    ", used=" + used +
                    ", meanSquare=" + meanSquare +
                    ", usedRate='" + usedRate + '\'' +
                    '}';
        }
    }


}
