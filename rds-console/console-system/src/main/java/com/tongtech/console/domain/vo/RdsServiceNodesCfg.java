package com.tongtech.console.domain.vo;

import com.tongtech.common.exception.ServiceException;
import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.enums.NodeTypeEnum;
import com.tongtech.console.enums.DeployModeEnum;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.tongtech.console.enums.DeployModeEnum.*;

/**
 * 在通过模版生成正式配置，用于转换为RdsService相关的模版配置信息
 */
public class RdsServiceNodesCfg implements Serializable  {

    private static final long serialVersionUID = 1L;

    private String serviceName;

    private String deployMode;

    private Long groupId;

    private Integer secureMode;

    private String password;

    private List<Shard> shards;

    private List<RdsNode> nodes; //格式 "localhost:6241"  第一个是主节点

    public RdsServiceNodesCfg(RdsServiceNodesVo vo) {
        this(vo.getService(), vo.getNodes());
    }

    public RdsServiceNodesCfg(RdsService serv, List<RdsNode> nodes) {

        this.serviceName = serv.getServiceName();
        this.deployMode = convertDeployMode(serv);
        this.groupId = serv.getGroupId();
        this.secureMode = serv.getSecureMode();
        this.password = serv.getPassword();

        DeployModeEnum mode = serv.getDeployModeEnum();

        this.nodes = new ArrayList<RdsNode>();
        shards = new ArrayList<Shard>();
        if(mode == SINGLE) {
            for(RdsNode node : nodes) {
                this.nodes.add(node);
            }
        }
        else if(mode == SENTINEL_WORKER) {
            List<RdsNode> noneMasterNodes = new ArrayList<RdsNode>();
            RdsNode masterNode = null;
            for( RdsNode node : nodes ) {
                NodeTypeEnum nodeType = NodeTypeEnum.parse(node.getNodeType());
                if(masterNode == null && node.isMasterNode()) {
                    masterNode = node;
                }
                else {
                    noneMasterNodes.add(node);
                }
            }

            //通过此操作保证 masterNode 是放在列表最前面。
            if(masterNode != null) this.nodes.add(masterNode);
            for(RdsNode node : noneMasterNodes) {
                this.nodes.add(node);
            }
        }
        else if(mode == CLUSTER) {
            //int len = vo.getNodes().size();
            //for(int i = 0 ; i < len ; i++) {
            for( RdsNode node : nodes ){
                Shard shard = getShard(node);
                if(node.isMasterNode()) {
                    shard.masterNode = node.getServiceEndpoint();
                }
                else {
                    shard.noneMasterNodes.add(node.getServiceEndpoint());
                }
            }

            //对每一个shard进行赋值
            for(Shard shard : this.shards) {
                StringBuilder buf = new StringBuilder();
                buf.append(shard.masterNode);
                for(String node : shard.noneMasterNodes) {
                    buf.append(',').append(node);
                }

                shard.endpoints = buf.toString();
            }
        }
        else {
            throw new ServiceException("Unsupported service deployMode '" + mode + "' in RdsServiceNodesCfg");
        }
        //TODO 未实现 SCALABLE 模式
    }

    /**
     * 配置文件中的 DeployMode 和软件中定义不一致！
     * 此函数把系统存放的DeployMode转换为配置需要的值。
     * @param serv
     * @return
     */
    private String convertDeployMode(RdsService serv) {
        DeployModeEnum m = serv.getDeployModeEnum();
        if(m == SINGLE) {
            return "default";
        }
        else if(m == SENTINEL_WORKER) {
            return "sentinel";
        }
        else {
            return serv.getDeployMode();
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    /**
     * 获取所有工作节点列表的字符串，格式如： "192.168.0.81:6379,192.168.0.82:6379,192.168.0.83:6379"
     * 其中第一个是主节点, 端口为redisPort
     * @return
     */
    public String getEndpoints() {
        StringBuilder buf = new StringBuilder("");
        for(RdsNode node : this.nodes) {
            buf.append(node.getRedisEndpoint()).append(',');
        }
        if(buf.length() > 1) { //删除最后一个逗号
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    public String getDeployMode() {
        return deployMode;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Integer getSecureMode() {
        return secureMode;
    }

    public String getPassword() {
        return password;
    }

    public List<Shard> getShards() {
        return shards;
    }

    public List<RdsNode> getNodes() {
        return nodes;
    }

    //    public List<RdsNode> getWorkerNodes() {
//        return workerNodes;
//    }

    private Shard getShard(RdsNode node) {
        int size = this.shards.size();
        int index = node.getShard();
        if(index < size) {
            return this.shards.get(index);
        }
        else {
            Shard s = new Shard();
            s.index = index;
            s.slot = node.getSlot();
            this.shards.add(s);
            return s;
        }
    }

    public static class Shard {
        private Integer index;
        private String endpoints;  //192.168.0.60:6200,192.168.0.60:6200  第一个是主节点
        private String slot;

        private String masterNode;
        private List<String> noneMasterNodes = new ArrayList<String>();

        public Integer getIndex() {
            return index;
        }

        public String getEndpoints() {
            return endpoints;
        }

        public String getSlot() {
            return slot;
        }

    }

}
