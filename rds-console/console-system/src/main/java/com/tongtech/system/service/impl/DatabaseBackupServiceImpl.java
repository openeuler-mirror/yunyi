package com.tongtech.system.service.impl;


import com.tongtech.common.config.AppHomeConfig;
import com.tongtech.common.utils.DateUtils;
import com.tongtech.system.service.IDatabaseBackupService;
import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;



import org.h2.tools.Script;

import java.util.List;

@Service
public class DatabaseBackupServiceImpl implements IDatabaseBackupService {

    protected final Logger logger = LoggerFactory.getLogger(DatabaseBackupServiceImpl.class);

    private static String BACKUP_PREFIX = "export";

    private static String SPLITER = "-";

    @Autowired
    private DataSource dataSource;

    @Override
    public File backupDatabase() {
        File backDir = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH);
        if(backDir.exists() == false) { backDir.mkdirs(); }

        String fileName =  BACKUP_PREFIX + SPLITER + DateUtils.dateTimeNow() + ".zip";
        File backFile = new File(backDir, fileName);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            Script.process(connection, backFile.getAbsolutePath(), "", "compression zip");
        } catch (SQLException e) {
            throw new RuntimeException("Backup database error!", e);
        }finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                }
            }
        }
        logger.info("Database backup in file:" + backFile.getAbsolutePath());

        return backFile;
    }

    @Override
    public List<File> cleanExpiredBackup(int maxHistory) {
        File backDir = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH);
        File[] hisBackFiles = backDir.listFiles();
        List<File> deletedFiles = new ArrayList<File>();
        for(File hisBackFile : hisBackFiles) {
            String hisFileName = hisBackFile.getName();
            String[] ns = hisFileName.split(SPLITER);
            if(ns != null && ns.length >= 2 && BACKUP_PREFIX.equals(ns[0])) {
                String dateTimeStr = ns[1].substring(0, ns[1].length() - 4);
                try {
                    Date exportDate = DateUtils.parseDate(dateTimeStr, DateUtils.YYYYMMDDHHMMSS);
                    long range = System.currentTimeMillis() - exportDate.getTime();
                    long maxHistoryRange = maxHistory * 24L * 3600L * 1000L;
                    if(range > maxHistoryRange) {
                        hisBackFile.delete();
                        deletedFiles.add(hisBackFile);
                        logger.info("Expired database export file was deleted. File=" + hisBackFile.getAbsoluteFile());
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return deletedFiles;
    }


    @Override
    public void restoreDatabase(String fileName) {
        File databasePath =  AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_PATH);
        File databaseFile = new File(databasePath, AppHomeConfig.DATABASE_NAME + ".mv.db");
        if(databaseFile.exists() == false) {
            throw new RuntimeException("The original database file("
                    + databaseFile.getPath() + ") not exists, when restore database." );
        }

        String url = "jdbc:h2:file:" +
                databasePath.getAbsolutePath() + File.separator + AppHomeConfig.DATABASE_NAME +
                ";MODE=MySQL";
        File importFile = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH, fileName);

        if(importFile.exists() == false) {
            throw new RuntimeException("The import database dump file("
                    + importFile.getPath() + ") not exists, when restore database." );
        }

        //删除旧的数据库文件
        databaseFile.delete();

        //TODO 这里写死的数据库的用户名和密码
        String runArgs[] = {
                "-url", url,
                "-user", "root",
                "-password", "123456",
                "-script", importFile.getAbsolutePath(),
                "-options", "compression", "zip"
        };

        RunScript run = new RunScript();
        try {
            //生成新的数据库文件，并导入
            run.runTool(runArgs);
            logger.info("Database was restored. INFO:" + Arrays.toString(runArgs));
        } catch (SQLException e) {
            throw new RuntimeException("Restore database error!", e);
        }
    }
}
