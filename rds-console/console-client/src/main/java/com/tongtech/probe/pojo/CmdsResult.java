package com.tongtech.probe.pojo;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * 命令执行结果
 */
public class CmdsResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String host;

    private Map<String, Result> results;

    public CmdsResult() {
        this.results = new Hashtable<String, Result>();
    }

    public CmdsResult(String host) {
        this.results = new Hashtable<String, Result>();
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public synchronized  Map<String, Result> getResults() {
        return results;
    }

    public void setResults(Map<String, Result> results) {
        this.results = results;
    }

    public synchronized void addResult(String name, Result result) {
        this.results.put(name, result);
    }

    public synchronized int dataSize() {
        int size = 0;
        if (results != null) {
            return results.size();
        }
        else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return "CmdsResult{" +
                "host='" + host + '\'' +
                ", results=" + results +
                '}';
    }

    public static class Result {
        private String type;
        private String name;
        private String cmd;
        private boolean success;
        private String msg;

        private long startTime; //开始时间，毫秒

        private long duration;  //持续时间，毫秒

        public Result() {  }

        public Result(String type, String name, String cmd) {
            this.type = type;
            this.name = name;
            this.cmd = cmd;
            this.startTime = System.currentTimeMillis();
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCmd() {
            return cmd;
        }

        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "type='" + type + '\'' +
                    ", name='" + name + '\'' +
                    ", cmd='" + cmd + '\'' +
                    ", success=" + success +
                    ", msg='" + msg + '\'' +
                    ", startTime='" + startTime + '\'' +
                    ", duration='" + duration + '\'' +
                    '}';
        }
    }
}
