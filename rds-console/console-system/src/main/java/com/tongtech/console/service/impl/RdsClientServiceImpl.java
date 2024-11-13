package com.tongtech.console.service.impl;


import com.tongtech.common.exception.ServiceException;
import com.tongtech.common.utils.RedisUtils;
import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.enums.NodeStatusEnum;
import com.tongtech.console.service.RdsClientService;
import com.tongtech.console.service.RdsNodeService;
import com.tongtech.console.service.RdsServiceService;
import com.tongtech.console.utils.JedisDataClient;
import com.tongtech.console.enums.NodeSecureEnum;
import com.tongtech.console.enums.NodeTypeEnum;
import com.tongtech.console.enums.DeployModeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.*;
import redis.clients.jedis.util.Pool;

import java.util.*;

import static com.tongtech.console.enums.NodeSecureEnum.*;
import static com.tongtech.console.enums.DeployModeEnum.*;
import static com.tongtech.console.enums.NodeTypeEnum.PROXY;
import static com.tongtech.console.enums.NodeTypeEnum.WORKER;

@Service
public class RdsClientServiceImpl implements RdsClientService {

    protected final Logger logger = LoggerFactory.getLogger(RdsClientServiceImpl.class);

    private int maxIdle = 100;

    private long maxWaitMillis = 3 * 1000;

    private int maxActive = 100;

    private int minIdle = 10;

    private int timeout = 3000;

    private int maxAttempts = 5;

    @Autowired
    private RdsServiceService servService;

    @Autowired
    private RdsNodeService nodeService;


    private Map<Long, Pool> jedisPoolMap = new HashMap<Long, Pool>();

    private Map<Long, JedisCluster> jedisClusterMap = new HashMap<Long, JedisCluster>();

    @Override
    public int deleteConnections() {
        int ret = 0;
        Set<Long> serviceIds = jedisPoolMap.keySet();

        for(Long key: serviceIds) {
            try {
                Pool pool = jedisPoolMap.get(key);
                ret ++;
                pool.close();
            }catch(Throwable t) {
                logger.error("Close service"+ key + " the redisClient error", t);
            }
        }

        jedisPoolMap.clear();

        Set<Long> clusterServNames = jedisClusterMap.keySet();

        for(Long key: clusterServNames) {
            try {
                JedisCluster jc = jedisClusterMap.get(key);
                ret ++;
                jc.close();
            }catch(Throwable t) {
                logger.error("Close service"+ key + " the redisClient error", t);
            }
        }
        jedisClusterMap.clear();

        logger.info("RdsServiceImpl: All current client connection pool is clean!");
        return ret;
    }


    @Override
    public JedisDataClient getConnectionClient(Long serviceId) {
        RdsService serv = servService.selectRdsServiceByServiceId(serviceId);
        JedisDataClient client = null;

        try {
            Pool pool = null;
            switch(serv.getDeployModeEnum()) {
                case SINGLE:
                    pool = jedisPoolMap.get(serviceId);
                    if (pool != null) {
                        client = new JedisDataClient(serviceId, (Jedis) pool.getResource(), SINGLE);
                    } else {
                        pool = buildJedisSinglePool(serv);
                        jedisPoolMap.put(serviceId, pool);

                        client = new JedisDataClient(serviceId, (Jedis) pool.getResource(), SINGLE);
                    }
                    break;
                case SENTINEL_WORKER:
                    pool = jedisPoolMap.get(serviceId);
                    if (pool != null) {
                        client = new JedisDataClient(serviceId, (Jedis) pool.getResource(), SENTINEL_WORKER);
                    } else {
                        pool = buildJedisSentinelPool(serv);
                        jedisPoolMap.put(serviceId, pool);
                        client = new JedisDataClient(serviceId, (Jedis) pool.getResource(), SENTINEL_WORKER);
                    }
                    break;
                case CLUSTER:
                    JedisCluster jedisCluster = jedisClusterMap.get(serviceId);
                    if (jedisCluster != null) {
                        client = new JedisDataClient(serviceId, jedisCluster);
                    } else {
                        jedisCluster = buildJedisCluster(serv);
                        jedisClusterMap.put(serviceId, jedisCluster);

                        client = new JedisDataClient(serviceId, jedisCluster);
                    }
                    break;
                case SCALABLE:
                    pool = jedisPoolMap.get(serviceId);
                    if (pool != null) {
                        client = new JedisDataClient(serviceId, (Jedis) pool.getResource(), SCALABLE);
                    } else {
                        pool = buildJedisSinglePool(serv);
                        jedisPoolMap.put(serviceId, pool);
                        client = new JedisDataClient(serviceId, (Jedis) pool.getResource(), SCALABLE);
                    }
                    break;
            }

            if(client == null) {
                throw new ServiceException("Failed to create RDS client for Service(serviceName:" + serv.getServiceName() + ", deployMode:" + serv.getDeployMode() + ")");
            }

            testConnection(client);
        }
        catch (Exception e) {
            String displayName = "RDS服务(" + serv.getServiceName() + ")";
            logger.error(displayName + "connection error", e);

            deleteConnection(serviceId);
            String errorMsg = (e.getCause() == null) ? e.getMessage() : e.getCause().getMessage();
            if(errorMsg.indexOf("WRONGPASS") >=0) {
                throw new ServiceException(displayName + "认证失败，请检查该服务的密码是否正确配置！");
            }
            else {
                throw new ServiceException(displayName + "连接服务异常，请检查该服务是否正常运行！");
            }
        }

        return client;
    }


    /**
     * 验证客户端是否能正确连接
     * @param client
     */
    private void testConnection(JedisDataClient client) throws Exception {
        String checkKey = Long.toString(System.currentTimeMillis());
        client.del(checkKey);
    }

    @Override
    public int deleteConnection(Long serviceId) {
        int ret = 0;
        Pool p = jedisPoolMap.get(serviceId);
        if(p != null) {
            jedisPoolMap.remove(serviceId);
            p.close();
            ret ++;
        }

        JedisCluster jc = jedisClusterMap.remove(serviceId);
        if(jc != null) {
            jedisClusterMap.remove(serviceId);
            jc.close();
            ret ++;
        }

        return ret;
    }

    /**
     * scalable 模式和 single 模式都是采用单节点模式连接
     * @param server
     * @return
     */
    private Pool buildJedisSinglePool(RdsService server) {
        String displayName = "RDS服务(" + server.getServiceName() + ")";
        List<RdsNode> nodes = nodeService.selectRdsNodesByServiceId(server.getServiceId());
        DeployModeEnum mode = server.getDeployModeEnum();
        if(mode == SINGLE) {
            if (nodes.size() != 1) {
                throw new ServiceException(displayName + "，是单节点服务，但其工作节点数据量是：" + nodes.size());
            }
        }
        if(mode != SINGLE && mode != SCALABLE && mode != SENTINEL ) {
            throw new ServiceException(displayName + "服务部署模式错误，仅支持single, scalable, sentinel，当前部署模式：" + mode.getName());
        }

        String testResult = null;

        for(RdsNode node : nodes) {
            if((mode == SINGLE
                    || (mode == SCALABLE && node.getNodeTypeEnum() == PROXY )
                    || (mode == SENTINEL && node.getNodeTypeEnum() == WORKER ))
                    && node.getNodeStatusEnum() == NodeStatusEnum.START) {
                NodeSecureEnum secureMode = NodeSecureEnum.parse(server.getSecureMode());
                if (secureMode == ANONYMOUS) {
                    RedisUtils.testRedis(node.getHostAddress(), node.getRedisPort());
                    return new JedisPool(buildJedisPoolConfig(), node.getHostAddress(), node.getRedisPort());
                }
                else if(secureMode == PASSWORD) {
                    RedisUtils.testRedis(node.getHostAddress(), node.getRedisPort(), server.getPassword());
                    return new JedisPool(buildJedisPoolConfig(), node.getHostAddress(),
                                node.getRedisPort(), timeout, server.getPassword());
                }
            }
        }

        throw new ServiceException(displayName + " 无法连接！"
                + ((testResult == null) ? "" : "Error Message:" + testResult));
    }


    private Pool buildJedisSentinelPool(RdsService serv) {
        if(serv.getDeployModeEnum() != SENTINEL_WORKER) {
            throw new ServiceException("DeployMode must be SENTINEL_WORKER! (serviceName:" + serv.getServiceName() + ", deployMode:" + serv.getDeployMode() + ")");
        }

        Set<String> sentinelEndpoints = new HashSet<>();
        List<RdsNode> sentinelNodes = nodeService.selectRdsNodesByServiceId(serv.getSentinelServiceId());

        for (RdsNode snode : sentinelNodes) {
            if(NodeTypeEnum.SENTINEL == snode.getNodeTypeEnum()) {
                sentinelEndpoints.add(String.format("%s:%s", snode.getHostAddress(), snode.getServicePort()));
            }
        }

        if(sentinelEndpoints.size() == 0) {
            throw new ServiceException("Not found sentinel nodes for service(serviceName:" + serv.getServiceName() + ", deployMode:" + serv.getDeployMode() + ")");
        }

        RdsService sentinelServ = servService.selectRdsServiceByServiceId(serv.getSentinelServiceId());

        if(sentinelServ.getSecureMode() == 2) {
            return new JedisSentinelPool(serv.getServiceName(), sentinelEndpoints, serv.getPassword(), sentinelServ.getPassword()); //哨兵节点需要密码
        }
        else if(serv.getSecureMode() == 2) {
            return new JedisSentinelPool(serv.getServiceName(), sentinelEndpoints, serv.getPassword()); //工作节点需要密码
        }
        else {
            return new JedisSentinelPool(serv.getServiceName(), sentinelEndpoints); //无密码
        }

    }

    private JedisCluster buildJedisCluster(RdsService serv) {
        //获取节点
        Set<HostAndPort> clusterEndpoints = new HashSet<HostAndPort>();
        List<RdsNode> nodes = nodeService.selectRdsNodesByServiceId(serv.getServiceId());

        //创建Jedis操作对象
        for (RdsNode node : nodes) {
            RdsNode wNode = node;
            clusterEndpoints.add(new HostAndPort(wNode.getHostAddress(), wNode.getRedisPort()));
        }

        NodeSecureEnum secureMode = NodeSecureEnum.parse(serv.getSecureMode());
        //System.out.println("~~~~~~~~serv.getSecureMode()=" + serv.getSecureMode() + ", NodeSecureEnum=" + secureMode);
        if (secureMode == ANONYMOUS) {
            return new JedisCluster(clusterEndpoints, buildJedisPoolConfig());
        } else if(secureMode == PASSWORD) {
            return new JedisCluster(clusterEndpoints, timeout, timeout, maxAttempts, serv.getPassword(), buildJedisPoolConfig());
        }
        else { return null; }
    }

    private JedisPoolConfig buildJedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMinIdle(minIdle);
        return jedisPoolConfig;
    }

}
