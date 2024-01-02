truncate table `sys_job`;
INSERT INTO `sys_job`
VALUES (1, 'ProcessCenterData', 'DEFAULT', 'rdsCenterDataTask.process', '0 0/1 * * * ?', '1', '1', '0', 'admin',
        CURRENT_TIMESTAMP(), 'admin', NULL, '连接中心服务管理监控端口，获取各服务和节点的状态、及监控信息'),
       (2, 'DatabaseBackup', 'DEFAULT', 'databaseBackupTask.process(7)', '0 30 1 * * ?', '1', '1', '0', 'admin',
        CURRENT_TIMESTAMP(), '', NULL, '数据库备份任务，每日1点30分备份数据库数据到 data/dbbak的文件, 同时会清理该目录下过期的备份文件. 参数为过期清理备份的时间（单位天），默认清理7天前的备份文件.'),
       (3, 'cleanLogs', 'DEFAULT', 'dataCleanupTask.cleanLogs(72)', '0 0 0/1 * * ?', '1', '1', '0', 'admin',
        CURRENT_TIMESTAMP(), '', NULL, '清理日志数据，参数是指定被清理数据的过期时间(单位小时，默认值72)'),
       (4, 'cleanStatistics', 'DEFAULT', 'dataCleanupTask.cleanStatistics(72)', '0 0 0/1 * * ?', '1', '1', '0', 'admin',
        CURRENT_TIMESTAMP(), '', NULL, '清理统计和监控数据，参数是指定被清理数据的过期时间(单位小时，默认值72)'),
       (5, 'cleanService', 'DEFAULT', 'dataCleanupTask.cleanService(60)', '0 0/10 * * * ?', '1', '1', '0', 'admin',
        CURRENT_TIMESTAMP(), '', NULL, '清理过期服务和服务下的节点，参数是指定被清理数据的过期时间(单位分钟，默认值60)'),
       (6, 'cleanInactiveNodeStatus', 'DEFAULT', 'dataCleanupTask.cleanInactiveNodeStatus(2)', '0 0/1 * * * ?', '1', '1', '0', 'admin',
        CURRENT_TIMESTAMP(), '', NULL, '设置不活跃的节点的状态为stop状态，参数是在指定时间内定未上报状态的节点(单位分钟，默认值2)');
