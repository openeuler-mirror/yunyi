package com.tongtech;

import com.tongtech.common.config.AppHomeConfig;
import com.tongtech.system.service.IDatabaseBackupService;
import com.tongtech.system.service.ISysConfigService;
import com.tongtech.system.service.impl.DatabaseBackupServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.Scanner;

import static com.tongtech.common.constant.ConsoleConstants.*;

/**
 * 启动程序
 *
 * @author XiaoZhangTongZhi
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ConsoleApplication
{
    public static void main(String[] args)
    {
        if(args.length == 2 && MAIN_ARG_RESTORE.equals(args[0])) {
            //备份恢复测试，开始从备份文件中恢复数据库文件
            String fileName = args[1];
            File restoreFile = AppHomeConfig.getAbsoluteFile(AppHomeConfig.DATABASE_BACKUP_PATH, args[1]);

            System.out.println("Now begin to restore database data from:" +  restoreFile);// 提示用户输入字符串
            System.out.println("Current data in database will be replaced! Please be confirm(Y/n):");
            Scanner scanner = new Scanner(System.in);// 获得控制台输入流

            String text = scanner.nextLine();// 获得用户输入
            if(text != null && text.trim().equals("Y")) {
                System.out.println("Database restoring .....");

                //这里没有启动Spring容器，直接
                IDatabaseBackupService backupService = new DatabaseBackupServiceImpl();
                backupService.restoreDatabase(fileName);
                System.out.println("Database restoring completed.");
            }
            else {
                System.out.println("Quit database restoring.");
            }
        }
        else if(args.length == 1) {
            ConfigurableApplicationContext context = SpringApplication.run(ConsoleApplication.class, args);
            ISysConfigService configService = context.getBean("sysConfigServiceImpl", ISysConfigService.class);

            if(MAIN_ARG_INITIALIZE.equals(args[0]) || MAIN_ARG_INITIALIZE_SHORT.equals(args[0])) {
                System.out.println("System is start with arguments '--initialize or -i'. System is initializing ......");
                configService.setConfigValueByKey(CONFIG_SYS_INITIALIZED_KEY, "false");
                configService.setConfigValueByKey(CONFIG_SYS_DEVELOPMENT_MODE_KEY, "false");
                configService.initSysMenu();
                System.out.println("Console Started!");
            }
            else if(MAIN_ARG_DEVELOPMENT.equals(args[0]) || MAIN_ARG_DEVELOPMENT_SHORT.equals(args[0])) {

                System.out.println("System is start with arguments '--development or -d'. System is initializing as development mode!");

                configService.setConfigValueByKey(CONFIG_SYS_INITIALIZED_KEY, "false");
                configService.setConfigValueByKey(CONFIG_SYS_DEVELOPMENT_MODE_KEY, "true");
                configService.initSysMenu();
                System.out.println("Console Started!");
            }

        }
        else {
            SpringApplication.run(ConsoleApplication.class, args);
            System.out.println("Console Started!");
        }
    }
}
