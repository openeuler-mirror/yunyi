package com.tongtech.console.task;

import com.tongtech.common.config.UhConsoleConfig;
import com.tongtech.common.constant.ConsoleConstants;
import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.utils.StringUtils;
import com.tongtech.console.domain.*;
import com.tongtech.console.enums.NodeStatusEnum;
import com.tongtech.console.enums.NodeTypeEnum;
import com.tongtech.console.service.*;
import com.tongtech.probe.stat.*;
import com.tongtech.console.enums.DeployModeEnum;
import com.tongtech.probe.RestCenterClient;
import com.tongtech.probe.RestCenterResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.tongtech.common.enums.DeployEnvEnum.HOST;
import static com.tongtech.common.utils.CompareUtils.notEquals;

@Component("rdsCenterDataTask")
public class RdsCenterDataTask {

    protected final Logger logger = LoggerFactory.getLogger(RdsCenterDataTask.class);

    @Autowired
    private CenterStatSrcService statSrcService;

    @Autowired
    private NodeStatService nodeStatService;

    @Autowired
    private ServiceStatService servStatService;

    @Autowired
    private RdsNodeService nodeService;

    @Autowired
    private RdsServiceService servService;

    @Autowired
    private RdsVersionService versionService;

    private transient RdsVersion defaulVersion; //处理时暂存；

    public synchronized void process() {
        RdsService centerServ = servService.selectRdsServiceByServiceId(ConsoleConstants.CENTER_SERVICE_ID);
        RestCenterClient centerClient = servService.getCenterClient(centerServ);
        if(centerClient != null) {
            try {
                //访问 Center 提供的Rest接口，并记录开始时间和执行时长
                long timeBegin = System.currentTimeMillis();
                RestCenterResult<StatCenterNode> centersResult = centerClient.getCenters();
                RestCenterResult<StatService> servicesResult = centerClient.getServices();
                RestCenterResult<StatSentinelNode> sentinelsResult = centerClient.getSentinels();
                RestCenterResult<StatLicense> licenseResult = centerClient.getLicenseUsing();
                long duration = System.currentTimeMillis() - System.currentTimeMillis();
                CenterStatSrc src = new CenterStatSrc(
                        centersResult.getSrc(),
                        servicesResult.getSrc(),
                        sentinelsResult.getSrc(),
                        licenseResult.getSrc(),
                        duration, new Date(timeBegin));
                statSrcService.insertCenterStatSrc(src); //保存原始报文

                getDefaultVersion();

                // 对报文进行处理
                Set<String> runningNodes = new LinkedHashSet<>(); //记录运行状态节点的 instance
                processCenters(src.getSrcId(), centersResult, centerServ, runningNodes);
                Map<String, RdsService>  serviceMap = processServices(src.getSrcId(), servicesResult, runningNodes);
                processSentinels(src.getSrcId(), serviceMap, sentinelsResult, runningNodes);
                processLicense(src.getSrcId(), licenseResult);

                System.out.println("updateExpiredNodeStatus, running:" + runningNodes);
                int expiredNodes = updateExpiredNodeStatus(runningNodes);
                System.out.println("Update expired nodes count=" + expiredNodes);
            } catch (IOException e)  {
                throw new RuntimeException(e);
            }
        }
    }

    private int updateExpiredNodeStatus(Set<String> runningNodes) {
        return nodeService.updateExpiredNodeStatus(runningNodes);
    }


    private void processCenters(Long srcId, RestCenterResult<StatCenterNode> centersResult, RdsService centerServ, Set<String> runningNodes) throws IOException {

        //1.获得中心服务对应的节点(from database)，并存为：list<RdsNode>，和 Map<instance, RdsNode>形式
        List<RdsNode> centers = nodeService.selectRdsNodesByServiceId(ConsoleConstants.CENTER_SERVICE_ID);
        Map<String, RdsNode> centersMap = new HashMap<>(); //Map( instance, RdsNode )
        for(RdsNode c : centers) {
            centersMap.put(c.getInstance(), c);
        }

        //2. 过滤出非过期的中心节点状态
        List<StatCenterNode> statCenters = centersResult.getListData().stream()
                .filter(n->n.getExpired() == false).collect(Collectors.toList());
        List<NodeStat> centerStats = new ArrayList<>();

        for(StatCenterNode statCenter : statCenters) {
            RdsNode center = centersMap.get(statCenter.getInstance());//get RdsNode by instance
            RdsNode newCenter = new RdsNode(centerServ.getServiceId(), statCenter);
            if(center == null) { ///如不存在对应节点就进行节点插入操作
                center = newCenter;
                nodeService.insertRdsNode(center);
            }
            else { //存在对应节点
                if( nodeChanged(center, newCenter) ) {
                    center.setFrom(newCenter);
                    nodeService.updateRdsNode(center);
                }
                else {
                    nodeService.updateNodeStatus(center.getNodeId(), NodeStatusEnum.START);
                }
            }

            NodeStat centerStat = new NodeStat(srcId, center, statCenter);
            centerStats.add(centerStat);
            runningNodes.add(statCenter.getInstance());
            nodeStatService.insertNodeStat(centerStat);
        }

        //3. 插入 service_stat表记录。
        ServiceStat serviceStat = new ServiceStat(srcId, centerServ, centerStats);
        servStatService.insertServiceStat(serviceStat);

    }

    private Map<String, RdsService>  processServices(Long srcId, RestCenterResult<StatService> servicesResult, Set<String> runningNodes) throws IOException {

        List<StatService> statServices = servicesResult.getListData();
        Map<String, RdsService> serviceMap = new HashMap<String, RdsService>();
        Map<Long, RdsService> serviceUpdates = new HashMap<>(); //需要更新的服务信息


        for(StatService statServ : statServices) {

            //1.获得服务，如果服务不存在就新建并插入服务（新服务是 manualAdmin = false）
            RdsService serv = servService.selectServiceBy(statServ.getName());
            if(serv == null) {
                //TODO 仅接口未变更时，临时使用
                if("SENTINEL".equals(statServ.getType())) {
                    statServ.setType("SENTINEL_WORKER");
                }

                serv = new RdsService(statServ, this.defaulVersion);
                servService.insertRdsService(serv);
            }
            serviceMap.put(serv.getServiceName(), serv); //插入RdsService Map对象中。

            //2.获得服务对应的节点(from database)，并存为：list<RdsNode>，和 Map<instance, RdsNode>形式
            List<RdsNode> nodes = nodeService.selectRdsNodesByServiceId(serv.getServiceId());
            Map<String, RdsNode> nodesMap = nodes.stream().collect(Collectors.toMap(RdsNode::getInstance, node -> node));

            if(statServ.getNodes() != null) {
                List<StatWorkerNode> statWorkerNodes = statServ.getNodes().stream()
                        .filter(n->n.getExpired() == false).collect(Collectors.toList());
                for(StatWorkerNode statNode : statWorkerNodes) {
                    RdsNode node = nodesMap.get(statNode.getInstance());
                    RdsNode newNode = new RdsNode(serv.getServiceId(), statNode);
                    if (node == null) { ////如不存在对应节点就进行节点插入操作
                        node = newNode;
                        nodeService.insertRdsNode(node);
                    }
                    else {
                        if( nodeChanged(node, newNode) ) {
                            node.setFrom(newNode);
                            nodeService.updateRdsNode(node);
                        }
                        else {
                            nodeService.updateNodeStatus(node.getNodeId(), NodeStatusEnum.START);
                        }
                    }

                    runningNodes.add(statNode.getInstance());
                    nodeStatService.insertNodeStat(new NodeStat(srcId, node, statNode));

                    if (updatesServiceSecure(statNode.getSecureLevel(), node.getNodeTypeEnum(), serv)) {
                        serviceUpdates.put(serv.getServiceId(), serv);
                    }
                }
            }

            //处理代理节点-如果存在
            if(statServ.getProxies() != null) {
                List<StatProxyNode> statProxyNodes = statServ.getProxies().stream()
                        .filter(n -> n.getExpired() == false).collect(Collectors.toList());
                if (statProxyNodes != null) {
                    for (StatProxyNode statNode : statProxyNodes) {
                        RdsNode node = nodesMap.get(statNode.getInstance());
                        RdsNode newNode = new RdsNode(serv.getServiceId(), statNode);
                        if (node == null) {
                            node = newNode;
                            nodeService.insertRdsNode(node);
                        } else {
                            if (nodeChanged(node, newNode)) {
                                node.setFrom(newNode);
                                nodeService.updateRdsNode(node);
                            } else {
                                nodeService.updateNodeStatus(node.getNodeId(), NodeStatusEnum.START);
                            }
                        }

                        runningNodes.add(statNode.getInstance());
                        nodeStatService.insertNodeStat(new NodeStat(srcId, node, statNode));

                        if (updatesServiceSecure(statNode.getSecureLevel(), node.getNodeTypeEnum(), serv)) {
                            serviceUpdates.put(serv.getServiceId(), serv);
                        }
                    }
                }
            }

            //4. 插入 service_stat表记录。
            ServiceStat serviceStat = new ServiceStat(srcId, serv, statServ);
            servStatService.insertServiceStat(serviceStat);
        }

        //更新需要更新的服务
        for(RdsService upServ : serviceUpdates.values()) {
            servService.updateRdsService(upServ);
        }

        return serviceMap;
    }


    private void processSentinels(Long srcId, Map<String, RdsService> serviceMap, RestCenterResult<StatSentinelNode> sentinelsResult, Set<String> runningNodes) throws IOException {
        List<StatSentinelNode> statSentinels = sentinelsResult.getListData().stream()
                .filter(n->n.getExpired() == false).collect(Collectors.toList()); //过滤出有效的哨兵节点

        Map<Long, RdsService> serviceUpdates = new HashMap<>(); //需要更新的服务信息
        Map<String, ServiceStatInfo> servStatInfos = new HashMap<>();
        for(StatSentinelNode statSentinel : statSentinels) {

            RdsService serv = null;

            //找到对应的哨兵服务，如果不存在就创建新的服务
            String serviceName = statSentinel.getGroup();
            if(StringUtils.isNotEmpty(serviceName)) {
                serv = servService.selectServiceBy(serviceName);
                if (serv == null) {
                    serv = new RdsService(serviceName, DeployModeEnum.SENTINEL, this.defaulVersion);
                    servService.insertRdsService(serv);
                }
            }

            RdsNode sentinel = nodeService.selectNodeBy(statSentinel.getInstance()); //找到对应哨兵节点对象
            RdsNode newSentinel = new RdsNode((serv == null) ? -1L : serv.getServiceId(), statSentinel);
            if(sentinel == null) { //如不存在对应节点就进行节点插入操作
                sentinel = newSentinel;
                nodeService.insertRdsNode(sentinel);
            }
            else {
                if( nodeChanged(sentinel, newSentinel) ) {
                    sentinel.setFrom(newSentinel);
                    nodeService.updateRdsNode(sentinel);
                }
                else {
                    nodeService.updateNodeStatus(sentinel.getNodeId(), NodeStatusEnum.START);
                }
            }

            runningNodes.add(statSentinel.getInstance());
            NodeStat nodeStat = new NodeStat(srcId, sentinel, statSentinel);
            nodeStatService.insertNodeStat(nodeStat);

            //1. 更新 ServiceStatInfo 用于记录和产生ServiceStat对象
            ServiceStatInfo servStatInfo = servStatInfos.get(serviceName);
            if(servStatInfo == null) {
                servStatInfo = new ServiceStatInfo(srcId, serv);
                servStatInfos.put(serviceName, servStatInfo);
            }
            servStatInfo.addNode(nodeStat);

            //2. 更新secureLevel
            if (updatesServiceSecure(statSentinel.getSecureLevel(), sentinel.getNodeTypeEnum(), serv)) {
                serviceUpdates.put(serv.getServiceId(), serv);
            }

            //3. 更新其他使用这个sentinel service的服务中的 sentinelServiceId
            for(String workerServiceName : statSentinel.getServices().keySet()) {
                RdsService workerServ = serviceMap.get(workerServiceName);
                if(workerServ != null && !workerServ.getSentinelServiceId().equals(serv.getServiceId())) {
                    workerServ.setSentinelServiceId(serv.getServiceId());
                    serviceUpdates.put(workerServ.getServiceId(), workerServ);
                }
            }

        }

        //更新需要更新的服务
        for(RdsService upServ : serviceUpdates.values()) {
            servService.updateRdsService(upServ);
        }

        //插入 service_stat表记录。
        for(ServiceStatInfo statInfo: servStatInfos.values()) {
            servStatService.insertServiceStat(statInfo.create());
        }

    }

    private void processLicense(Long srcId, RestCenterResult<StatLicense> licenseResult) throws IOException {
        //statSrcService.insertCenterStatSrc(new CenterStatSrc("sentinel", sentinelsResult));
        //TODO 建立 cnsl_stat_license 表, 对licesne使用情况进行记录
    }


    /**
     * 生成sentinel service 的名字，生成规则是 SS-ServiceName1-ServiceName2-ServiceName3-...
     * @param statSentinel
     * @return
     */
    private String getSentinelServiceName(StatSentinelNode statSentinel) {
        Set<String> services = statSentinel.getServices().keySet();
        if(services.size() == 0) {
            throw new ServiceException("Parse StatSentinelNode error! services should not be empty!" + statSentinel);
        }

        StringBuffer sentinelServiceName = new StringBuffer("SS");
        for(String serviceName : services) {
            sentinelServiceName.append("-").append(serviceName);
        }
        return sentinelServiceName.toString();
    }

    private synchronized void getDefaultVersion() {
        this.defaulVersion = versionService.selectDefaultVersion();
        if(this.defaulVersion == null) {
            throw new ServiceException("Failed to find default RDSVersion! Please set default RDSVersion.");
        }
    }

    /**
     * 判别是否需要更新RdsService： secureMode，sentinelPassworded 的属性，
     * 如果需要对 RdsService serv属性进行变更，并返回true
     * @param newSecureLevel 安全级别
     * @param nodeType  节点类型
     * @param serv  需要变更安全属性的 RdsService 对象
     * @return true RdsService属性变更，true RdsService属性没有变更
     */
    private boolean updatesServiceSecure(Integer newSecureLevel, NodeTypeEnum nodeType, RdsService serv) {
        boolean changed = false;

        DeployModeEnum deployMode = serv.getDeployModeEnum();
        if(newSecureLevel != null && !newSecureLevel.equals(serv.getSecureMode()))  {
            if(deployMode == DeployModeEnum.SINGLE
                    || deployMode == DeployModeEnum.SENTINEL_WORKER
                    ||deployMode == DeployModeEnum.CLUSTER) {
                if(nodeType == NodeTypeEnum.WORKER) {
                    serv.setSecureMode(newSecureLevel);
                    changed = true;
                }
            }
            else if(deployMode == DeployModeEnum.SCALABLE) {
                if(nodeType == NodeTypeEnum.PROXY) {
                    serv.setSecureMode(newSecureLevel);
                    changed = true;
                }
            }
//目前sentinel的报文中未给出 SecureLevel属性，因此禁用下面的处理
//            else if(deployMode == DeployModeEnum.SENTINEL) {
//                if(nodeType == SENTINEL) {
//                    serv.setSecureMode(newSecureLevel);
//                    changed = true;
//                }
//            }
        }

        return changed;
    }

    /**
     * 对比两个RdsNode的配置类属性是否都相同，如果有不同就说明newNode有变更。
     * @param orgNode
     * @param newNode
     * @return
     */
    private boolean nodeChanged(RdsNode orgNode, RdsNode newNode) {
        if(UhConsoleConfig.getDeployEnvEnum() == HOST) { //主机模式下不允许使用状态来更新节点信息。
            return false;
        }
        else {
            if( notEquals(orgNode.getHostAddress(), newNode.getHostAddress()) ||
                    notEquals(orgNode.getServicePort(), newNode.getServicePort()) ||
                    notEquals(orgNode.getRedisPort(), newNode.getRedisPort()) ||
                    notEquals(orgNode.isMasterNode(), newNode.isMasterNode()) ||
                    notEquals(orgNode.isHotSpares(), newNode.isHotSpares()) ||
                    notEquals(orgNode.getShard(), newNode.getShard()) ||
                    notEquals(orgNode.getSlot(), newNode.getSlot()) )
            {
                return true;
            }
            else {
                return false;
            }
        }
    }

    private static class ServiceStatInfo {
        private Long srcId;
        private RdsService serv;
        private List<NodeStat> nodeStats;
        public ServiceStatInfo(Long srcId, RdsService serv) {
            this.srcId = srcId;
            this.serv = serv;
            this.nodeStats = new ArrayList<NodeStat>();
        }
        public void addNode(NodeStat nodeStat) {
            this.nodeStats.add(nodeStat);
        }
        public ServiceStat create() {
            return new ServiceStat(srcId, serv, nodeStats);
        }
    }

}


