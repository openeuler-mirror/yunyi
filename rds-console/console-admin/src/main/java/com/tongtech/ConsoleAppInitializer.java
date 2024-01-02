package com.tongtech;

import com.tongtech.system.service.ISysConfigService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static com.tongtech.common.constant.ConsoleConstants.CONFIG_SYS_DEVELOPMENT_MODE_KEY;
import static com.tongtech.common.constant.ConsoleConstants.CONFIG_SYS_INITIALIZED_KEY;

//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class ConsoleAppInitializer {
    public static void main(String[] args)
    {
        ConfigurableApplicationContext context = SpringApplication.run(ConsoleApplication.class, args);

        ISysConfigService configService = context.getBean("sysConfigServiceImpl", ISysConfigService.class);


        System.out.println("System is start with arguments '--initialize or -i'. System is initializing ......");
        configService.setConfigValueByKey(CONFIG_SYS_INITIALIZED_KEY, "false");
        configService.setConfigValueByKey(CONFIG_SYS_DEVELOPMENT_MODE_KEY, "false");
        configService.initSysMenu();
        System.out.println("Console Started!");

        SpringApplication.exit(context);
    }

}
