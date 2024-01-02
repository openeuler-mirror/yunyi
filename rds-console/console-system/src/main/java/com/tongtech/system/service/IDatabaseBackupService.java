package com.tongtech.system.service;

import java.io.File;
import java.util.List;

public interface IDatabaseBackupService {

    /**
     * 返回备份的文件
     * @return
     */
    File backupDatabase();

    /**
     * 返回被删除的文件
     *
     * @return
     */
    List<File> cleanExpiredBackup(int maxHistory);

    /**
     *
     * @param fileName 导入文件名
     */
    void restoreDatabase(String fileName);

}
