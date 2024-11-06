package com.tongtech.console.service.impl;

import com.tongtech.common.utils.DateUtils;
import com.tongtech.console.domain.NodeStat;
import com.tongtech.console.domain.RdsNode;
import com.tongtech.console.domain.RdsService;
import com.tongtech.console.domain.ServiceStat;
import com.tongtech.console.domain.vo.*;
import com.tongtech.console.mapper.NodeStatMapper;
import com.tongtech.console.mapper.RdsNodeMapper;
import com.tongtech.console.mapper.RdsServiceMapper;
import com.tongtech.console.mapper.ServiceStatMapper;
import com.tongtech.console.service.ServiceStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.tongtech.common.constant.ConsoleConstants.STATISTIC_GROUPS;
import static com.tongtech.common.constant.ConsoleConstants.STATISTIC_GROUP_MIN_SECOND;

/**
 * 服务监控信息Service业务层处理
 *
 * @author Zhang ChenLong
 * @date 2023-03-18
 */
@Service
public class ServiceStatServiceImpl implements ServiceStatService {
    @Resource
    private ServiceStatMapper serviceStatMapper;

    @Autowired
    private NodeStatMapper nodeStatMapper;

    @Autowired
    private RdsServiceMapper servMapper;

    @Autowired
    private RdsNodeMapper nodeMapper;

    /**
     * 查询服务监控信息
     *
     * @param statId 服务监控信息主键
     * @return 服务监控信息
     */
    @Override
    public ServiceStat selectServiceStatByStatId(Long statId) {
        return serviceStatMapper.selectServiceStatByStatId(statId);
    }


    @Override
    public ServiceStatVo selectSummaryServiceStat(RdsMonitorQueryVo queryVo) {

        RdsService serv = servMapper.selectRdsServiceByServiceId(queryVo.getServiceId());
        ServiceStat servStat = serviceStatMapper.selectSummaryServiceStat(queryVo);
        ServiceStatVo servStatVo;
        if (servStat != null) {
            servStat.setDeployMode(serv.getDeployMode());
            servStat.setName(serv.getServiceName());
            servStatVo = new ServiceStatVo(servStat);
        } else {
            servStatVo = new ServiceStatVo(serv);
        }

        //获得node列表
        List<RdsNodeStatsVo> nodes = new ArrayList<>();
        List<RdsNode> rdsNodes = nodeMapper.selectNodesByServiceId(serv.getServiceId());
        for (RdsNode rn : rdsNodes) {
            RdsNodeStatsVo nodeVo = new RdsNodeStatsVo(rn);

            //或者node的统计数据
            NodeStatQueryVo nodeQuery = new NodeStatQueryVo(rn.getNodeId(), queryVo.getBeginCreateSecond(), (int) (queryVo.getPastSecond() / STATISTIC_GROUPS));
            List<NodeStat> stats;
            if (queryVo.getPastSecond() <= STATISTIC_GROUP_MIN_SECOND) {
                stats = nodeStatMapper.selectMonitorList(nodeQuery);
            } else {
                stats = nodeStatMapper.selectMonitorGroupList(nodeQuery);
            }
            nodeVo.setStats(stats);

            nodes.add(nodeVo);// 插入node
        }


        servStatVo.setNodes(nodes);

        return servStatVo;
    }

    /**
     * 查询服务监控信息列表
     *
     * @param serviceStat 服务监控信息
     * @return 服务监控信息
     */
    @Override
    public List<ServiceStat> selectServiceStatList(ServiceStat serviceStat) {
        return serviceStatMapper.selectServiceStatList(serviceStat);
    }

    /**
     * 新增服务监控信息
     *
     * @param serviceStat 服务监控信息
     * @return 结果
     */
    @Override
    public int insertServiceStat(ServiceStat serviceStat) {
        serviceStat.setCreateTime(DateUtils.getNowDate());
        return serviceStatMapper.insertServiceStat(serviceStat);
    }

    /**
     * 修改服务监控信息
     *
     * @param serviceStat 服务监控信息
     * @return 结果
     */
    @Override
    public int updateServiceStat(ServiceStat serviceStat) {
        return serviceStatMapper.updateServiceStat(serviceStat);
    }

    /**
     * 批量删除服务监控信息
     *
     * @param statIds 需要删除的服务监控信息主键
     * @return 结果
     */
    @Override
    public int deleteServiceStatByStatIds(Long[] statIds) {
        return serviceStatMapper.deleteServiceStatByStatIds(statIds);
    }

    /**
     * 删除服务监控信息信息
     *
     * @param statId 服务监控信息主键
     * @return 结果
     */
    @Override
    public int deleteServiceStatByStatId(Long statId) {
        return serviceStatMapper.deleteServiceStatByStatId(statId);
    }

    @Override
    public ServiceNodeStatVo selectServiceNodeStatGroup(RdsNodeStatQueryVo queryVo) {
        RdsService serv = servMapper.selectRdsServiceByServiceId(queryVo.getServiceId());
        if (Objects.nonNull(serv)) {
            List<RdsNode> rdsNodes = nodeMapper.selectNodesByServiceId(serv.getServiceId());
            List<NodeStat> nodeStats = rdsNodes.isEmpty() ? Collections.emptyList() : nodeStatMapper.selectNodeStatGroup(queryVo.initParams());
            DateTimeFormatter dtf = nodeStats.isEmpty() ? null : DateTimeFormatter.ofPattern(queryVo.getInterval().getConsoleTimePattern());
            return new ServiceNodeStatVo(serv, rdsNodes, nodeStats, dtf);
        }
        return null;
    }
}
