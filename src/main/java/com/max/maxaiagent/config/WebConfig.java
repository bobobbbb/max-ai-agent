package com.max.maxaiagent.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.max.maxaiagent.interceptor.GloabalInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 先添加Sa-Token拦截器处理认证
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register", "/user/check-*", "/health/**");
        
        // 再添加全局拦截器设置用户上下文
        registry.addInterceptor(new GloabalInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register", "/user/check-*", "/health/**");
    }
}
