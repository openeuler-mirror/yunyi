package com.tongtech.console.domain.vo;

import com.tongtech.console.domain.RdsNode;
import com.tongtech.probe.pojo.CmdsResult;

import java.util.*;

/**
 * 记录一批节点操作的执行结果。
 */
public class OperationResult {

    private Long serviceId;

    private String serviceName;

    private Map<Long, List<NodeResult>> nodesResults;

    private Set<String> errorNodeNames;

    private int resultCount;

    private int errorCount;

    public Long getServiceId() {
        return serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public Map<Long, List<NodeResult>> getNodesResults() {
        return nodesResults;
    }

    public int getResultCount() {
        return resultCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public Set<String> getErrorNodeNames() {
        return errorNodeNames;
    }

    public OperationResult() {
        this(null, null);
    }

    public OperationResult(Long serviceId, String serviceName) {
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.nodesResults = new LinkedHashMap<>(); //保证map中的元素按插入顺序排列
        this.errorNodeNames = new LinkedHashSet<>();
        this.resultCount = 0;
        this.errorCount = 0;
    }

    public List<NodeResult> addNodeResult(RdsNode node, CmdsResult.Result res) {
        return addNodeResult(new NodeResult(node, res));
    }

    public List<NodeResult> addNodeResult(NodeResult res) {
        Long nodeId = res.getNodeId();
        List<NodeResult> list = nodesResults.get(nodeId);
        if(list == null) {
            list = new ArrayList<>();
            nodesResults.put(nodeId, list);
        }

        list.add(res);
        this.resultCount ++;
        if(res.isSuccess() == false) {
            this.errorCount ++;
            errorNodeNames.add(res.getNodeName());
        }
        return list;
    }

    public static class NodeResult {
        private Long nodeId;
        private String nodeName;
        private String nodeType;
        private String cmd;
        private boolean success;
        private String msg;

        public NodeResult(RdsNode node, CmdsResult.Result cmdRes) {
            this.nodeId = node.getNodeId();
            this.nodeName = node.getNodeName();
            this.nodeType = node.getNodeType();
            this.success = cmdRes.isSuccess();
            this.cmd = cmdRes.getCmd();
            this.msg = cmdRes.getMsg();
        }

        public Long getNodeId() {
            return nodeId;
        }

        public String getNodeName() {
            return nodeName;
        }

        public String getNodeType() {
            return nodeType;
        }

        public String getCmd() {
            return cmd;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return "NodeResult{" +
                    ((success) ? "SUCCESS" : "FAILED") +
                    ", nodeId=" + nodeId +
                    ", nodeName='" + nodeName + '\'' +
                    ", nodeType='" + nodeType + '\'' +
                    ", cmd='" + cmd + '\'' +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "OperationResult{" +
                "serviceId=" + serviceId +
                ", serviceName='" + serviceName + '\'' +
                ", nodesResults=" + nodesResults +
                ", resultCount=" + resultCount +
                ", errorCount=" + errorCount +
                '}';
    }
}
