package com.tongtech.console.task;

import com.tongtech.system.service.IDatabaseBackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("databaseBackupTask")
public class DatabaseBackupTask {

    protected final Logger logger = LoggerFactory.getLogger(DatabaseBackupTask.class);

    @Autowired
    private IDatabaseBackupService backupService;

    public void process(Integer maxHistoryDay) {
        backupService.cleanExpiredBackup(maxHistoryDay);
        backupService.backupDatabase();
    }

}


