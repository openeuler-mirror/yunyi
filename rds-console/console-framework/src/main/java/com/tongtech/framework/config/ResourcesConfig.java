package com.tongtech.framework.config;

import com.tongtech.common.config.AppHomeConfig;
import com.tongtech.framework.interceptor.ContextPathInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.tongtech.common.constant.Constants;
import com.tongtech.framework.interceptor.RepeatSubmitInterceptor;

/**
 * 通用配置
 *
 * @author XiaoZhangTongZhi
 */
@Configuration
public class ResourcesConfig implements WebMvcConfigurer
{
    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Autowired
    private ContextPathInterceptor contextPathInterceptor;

//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry)
//    {
//        /** 本地文件上传路径 */
//        registry.addResourceHandler(Constants.RESOURCE_PREFIX + "/**")
//                .addResourceLocations("file:" + RdsConsoleConfig.getProfile() + "/");
//    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {

        /** 本地文件上传路径 */
        registry.addResourceHandler("/web-api/" + Constants.RESOURCE_PREFIX + "/**")
                .addResourceLocations("file:" + AppHomeConfig.getProfile() + "/");

        /** 加入静态资源的映射 */
        registry.addResourceHandler("/index.html").addResourceLocations("classpath:/public/index.html");
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/public/static/");
        registry.addResourceHandler("/favicon.ico").addResourceLocations("classpath:/public/favicon.ico");

    }

    /**
     * 自定义拦截规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        //registry.addInterceptor(contextPathInterceptor).addPathPatterns("/**/*.js");
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }

    /**
     * 跨域配置
     */
    @Bean
    public CorsFilter corsFilter()
    {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 设置访问源地址
        config.addAllowedOriginPattern("*");
        // 设置访问源请求头
        config.addAllowedHeader("*");
        // 设置访问源请求方法
        config.addAllowedMethod("*");
        // 有效期 1800秒
        config.setMaxAge(1800L);
        // 添加映射路径，拦截一切请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        // 返回新的CorsFilter
        return new CorsFilter(source);
    }
}
