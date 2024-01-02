package com.tongtech.console.service;

import java.util.Date;

public interface DataCleanupService {

    int cleanupStat(Date createTime);

    int cleanupLogs(Date createTime);

}
