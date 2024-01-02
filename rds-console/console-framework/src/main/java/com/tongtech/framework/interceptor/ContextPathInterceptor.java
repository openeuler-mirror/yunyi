package com.tongtech.framework.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@Component
public class ContextPathInterceptor implements HandlerInterceptor {

    @Value("${server.servlet.context-path}")
    private String contextPath;

    private final static String PATH_TOBE = "/uhrds";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        int startIdx = contextPath.length() + 1;
        String uri = request.getRequestURI();
        String resourcePath = "/public/" + uri.substring(startIdx);
        ClassPathResource jsRes = new ClassPathResource(resourcePath);
        if(jsRes.exists() ) {
            String content = StreamUtils.copyToString(jsRes.getInputStream(), StandardCharsets.UTF_8);
            if(contains(content)) {
                //System.out.println("~~~~~~~~~~ Replacing resourcePath:" + resourcePath);
                response.setHeader("Content-Type", "application/javascript;charset=utf-8");
                PrintWriter writer = response.getWriter();
                writer.write(replace(content));
                writer.flush();
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    public boolean contains(String inputContent) {
        return inputContent.contains(PATH_TOBE);
    }

    public String replace(String inputContent) {
        return inputContent.replace(PATH_TOBE, contextPath);
    }
}
