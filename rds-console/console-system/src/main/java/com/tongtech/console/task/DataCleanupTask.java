package com.tongtech.console.task;

import com.tongtech.console.service.DataCleanupService;
import com.tongtech.console.service.RdsNodeService;
import com.tongtech.console.service.RdsServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static javax.management.timer.Timer.ONE_HOUR;
import static javax.management.timer.Timer.ONE_MINUTE;

@Component("dataCleanupTask")
public class DataCleanupTask {

    protected final Logger logger = LoggerFactory.getLogger(DataCleanupTask.class);

    @Autowired
    private DataCleanupService cleanupService;
    @Autowired
    private RdsNodeService nodeService;
    @Autowired
    private RdsServiceService serviceService;

    /**
     * 删除hoursAgo小时前的, 日志数据
     * 包括表：cnsl_command_history, sys_oper_log(oper_time), sys_job_log
     * @param hoursAgo
     */
    public synchronized void cleanLogs(Integer hoursAgo) {
        Date timeAgo = new Date(System.currentTimeMillis() - ONE_HOUR * hoursAgo);
        cleanupService.cleanupLogs(timeAgo);
        logger.info("cleanLogs(" + hoursAgo + "hours), clean time before:" + timeAgo);
    }

    /**
     * 删除hoursAgo小时前的, 统计和监控数据
     * 包括表：cnsl_center_stat_src, cnsl_service_stat, cnsl_node_stat,
     * @param hoursAgo
     */
    public synchronized void cleanStatistics(Integer hoursAgo) {
        Date timeAgo = new Date(System.currentTimeMillis() - ONE_HOUR * hoursAgo);
        cleanupService.cleanupStat(timeAgo);
        logger.info("cleanStatistics(" + hoursAgo + "hours), clean time before:" + timeAgo);
    }


    /**
     * 清除过期没有更新的 RDSService 和 其下的节点
     * 只清除 manualAdmin == false 类型的服务和服务节点。
     * 基本清除规则:
     *    1. 清除指定时间前没有更新（update_time）的节点。
     *    2. 如果服务下的节点都被清除，且服务也符合指定时间前没有更新（update_time），就删除该服务。
     *
     * @param minutesAgo 删除 minutesAgo分钟前的数据
     *
     */
    public synchronized void cleanService(Integer minutesAgo) {
        Date timeAgo = new Date(System.currentTimeMillis() - ONE_MINUTE * minutesAgo);
        Boolean manualAdmin = false;

        nodeService.deleteExpiredNode(manualAdmin, timeAgo);
        serviceService.deleteNoneNodeExpiredService(manualAdmin, timeAgo);

        logger.info("cleanService(" + minutesAgo + "minutes), clean time before:" + timeAgo);
    }

    /**
     * 设置不活跃的节点的状态为 stop 状态。
     * 遍历所有的节点，只要指定时间前没有更新（update_time）的节点，就把其状态转为 stop
     * @param minutesAgo
     */
    public synchronized void cleanInactiveNodeStatus(Integer minutesAgo) {
        Date timeAgo = new Date(System.currentTimeMillis() - ONE_MINUTE * minutesAgo);
        nodeService.updateNoneStopNodes(timeAgo);
    }


}
