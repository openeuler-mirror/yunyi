package com.tongtech.web.controller.system;

import com.tongtech.common.exception.base.BaseException;
import com.tongtech.framework.interceptor.ContextPathInterceptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tongtech.common.config.UhConsoleConfig;
import com.tongtech.common.utils.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 因为是单页面应用，把各个不同的页面路由路径，都定向到/public/index.html资源上。
 *
 * @author Zhang Chenlong
 */
@RestController
public class SysIndexController
{
    /** 系统基础配置 */
    @Autowired
    private UhConsoleConfig consoleConfig;

    @Autowired
    private ContextPathInterceptor contextPathInterceptor;


    /**
     * 访问首页
     * 如果 classes下"/public/index.html"资源存在，就定位到该资源输出。
     * 如果不存在，就提示"....请通过前端地址访问。"
     */
    @RequestMapping({"/",
            "/index",
            "/login",
            "/system/**",
            "/user/**",
            "/monitor/**",
            "/tool/**",
            "/rds/**",
            "/log/**"
    })
    public String index()
    {
        try {
            ClassPathResource indexRes = new ClassPathResource("/public/index.html");
            if(indexRes.exists() ) {
                String content = StreamUtils.copyToString(indexRes.getInputStream(), StandardCharsets.UTF_8);
                return content;
//                if(contextPathInterceptor.contains(content)) {
//                    //System.out.println("~~~~~~~~~~ /public/index.html replaced!");
//                    return contextPathInterceptor.replace(content);
//                }
//                else {
//                    return content;
//                }
            }
            else {
                return StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", consoleConfig.getName(), consoleConfig.getVersion());
            }
        } catch ( IOException e) {
            LoggerFactory.getLogger(SysIndexController.class).error("err", e);
            throw new BaseException(e.getMessage());
        }
    }


}
