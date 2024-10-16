package com.tongtech.probe.stat;

public class StatThroughput {
    /* 当前秒的命令执行数 */
    private Integer current;
    /* 前10秒平均每秒的命令执行数量 */
    private Double average10;
    /* 前60秒平均每秒的命令执行数量 */
    private Double average60;
    /* 上一个小时（整点）的命令执行总数 */
    private Double lasthour;

    public Integer getCurrent() {
        return current;
    }

    public void setCurrent(Integer current) {
        this.current = current;
    }

    public Double getAverage10() {
        return average10;
    }

    public void setAverage10(Double average10) {
        this.average10 = average10;
    }

    public Double getAverage60() {
        return average60;
    }

    public void setAverage60(Double average60) {
        this.average60 = average60;
    }

    public Double getLasthour() {
        return lasthour;
    }

    public void setLasthour(Double lasthour) {
        this.lasthour = lasthour;
    }

    @Override
    public String toString() {
        return "Throughput{" +
                "current=" + current +
                ", average10=" + average10 +
                ", average60=" + average60 +
                ", lasthour=" + lasthour +
                '}';
    }

}
