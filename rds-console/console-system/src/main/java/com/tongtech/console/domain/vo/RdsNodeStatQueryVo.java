package com.tongtech.console.domain.vo;


import java.util.Objects;

public class RdsNodeStatQueryVo {

    private Long serviceId;

    private Long beginCreateSecond;

    private Long endCreateSecond;
    /**
     * 显示时间间隔
     * <pre>
     * DAY : 1d
     * HOUR  1h
     * MINUTE：1m
     * SECOND：1s
     * </pre>
     */
    private Interval interval;


    public enum Interval{
        SECOND("%Y-%m-%d %H:%i:%s",0L,60L,"mm分ss秒"),
        MINUTE("%Y-%m-%d %H:%i",60L,60L * 60,"HH时mm分"),
        HOUR("%Y-%m-%d %H", 60L * 60,24L * 60 * 60,"dd日HH时"),
        DAY("%Y-%m-%d",24L * 60 * 60,30L * 24 * 60 * 60,"MM月dd日"),
        MONTH("%Y-%m",30L * 24 * 60 * 60,12L * 30 * 24 * 60 * 60,"yyyy年MM月"),
        YEAR("%Y",12L * 24 * 60 * 60,Long.MAX_VALUE,"yyyy年");
        private final String groupTimeFormater;
        private final long minDiffSeconds;
        private final long maxDiffSeconds;
        private final String consoleTimePattern;

        Interval(String groupTimeFormater, long minDiffSeconds, long maxDiffSeconds, String consoleTimePattern) {
            this.groupTimeFormater = groupTimeFormater;
            this.minDiffSeconds = minDiffSeconds;
            this.maxDiffSeconds = maxDiffSeconds;
            this.consoleTimePattern = consoleTimePattern;
        }

        public String getGroupTimeFormater() {
            return groupTimeFormater;
        }

        public String getConsoleTimePattern() {
            return consoleTimePattern;
        }

        public long getMaxDiffSeconds() {
            return maxDiffSeconds;
        }

        public long getMinDiffSeconds() {
            return minDiffSeconds;
        }
    }

    public RdsNodeStatQueryVo initParams(){
        if(Objects.isNull(beginCreateSecond) || Objects.isNull(endCreateSecond)){
            endCreateSecond = System.currentTimeMillis() / 1000;
            beginCreateSecond = endCreateSecond - 10 * 60;
            interval = Interval.MINUTE;
            return this;
        }
        long diffSeconds = endCreateSecond - beginCreateSecond;
        if(diffSeconds <= 0){
            interval = Interval.SECOND;
            return this;
        }
        for(Interval interval : Interval.values()){
            if(diffSeconds > interval.minDiffSeconds && diffSeconds <= interval.maxDiffSeconds){
                this.interval = interval;
                return this;
            }
        }
        return this;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getBeginCreateSecond() {
        return beginCreateSecond;
    }

    public void setBeginCreateSecond(Long beginCreateSecond) {
        this.beginCreateSecond = beginCreateSecond;
    }

    public Long getEndCreateSecond() {
        return endCreateSecond;
    }

    public void setEndCreateSecond(Long endCreateSecond) {
        this.endCreateSecond = endCreateSecond;
    }

    public Interval getInterval() {
        return interval;
    }


}
