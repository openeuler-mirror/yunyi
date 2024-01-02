package com.tongtech.console.service.impl;

import com.tongtech.console.mapper.CenterStatSrcMapper;
import com.tongtech.console.mapper.NodeStatMapper;
import com.tongtech.console.mapper.ServiceStatMapper;
import com.tongtech.console.service.DataCleanupService;
import com.tongtech.system.mapper.SysJobLogMapper;
import com.tongtech.system.mapper.SysOperLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 数据清理服务，清除过期的日志和监控数据
 */
@Service
public class DataCleanupServiceImpl implements DataCleanupService {

    @Autowired
    private CenterStatSrcMapper centerStatSrcMapper;

    @Autowired
    private ServiceStatMapper serviceStatMapper;

    @Autowired
    private NodeStatMapper nodeStatMapper;

    @Autowired
    private SysJobLogMapper sysJobLogMapper;

    @Autowired
    private SysOperLogMapper sysOperLogMapper;

    @Override
    public int cleanupStat(Date createTime) {
        //       cnsl_center_stat_src, cnsl_service_stat, cnsl_node_stat,
        int deleted = 0;
        deleted += centerStatSrcMapper.deleteByCreateTime(createTime);
        deleted += serviceStatMapper.deleteByCreateTime(createTime);
        deleted += nodeStatMapper.deleteByCreateTime(createTime);

        return deleted;
    }

    @Override
    public int cleanupLogs(Date createTime) {
        //       cnsl_command_history, sys_oper_log(oper_time), sys_job_log
        int deleted = 0;
        deleted += sysOperLogMapper.deleteByOperTime(createTime);
        deleted += sysJobLogMapper.deleteByCreateTime(createTime);

        return deleted;
    }


}
