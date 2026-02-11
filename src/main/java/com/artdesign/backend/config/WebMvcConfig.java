package com.artdesign.backend.config;

import com.artdesign.backend.interceptor.ApiLoggingInterceptor;
import com.artdesign.backend.interceptor.AuthorizationInterceptor;
import com.artdesign.backend.repository.SystemLogRepository;
import com.artdesign.backend.service.UserService;
import com.artdesign.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SystemLogRepository systemLogRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 权限校验拦截器（先执行）
        registry.addInterceptor(new AuthorizationInterceptor(jwtUtil, userService))
                .addPathPatterns("/api/**", "/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register", "/api/auth/forget-password", "/api/test", "/api/actuator/**");

        // API 调用日志拦截器
        registry.addInterceptor(new ApiLoggingInterceptor(systemLogRepository, jwtUtil, userService))
                .addPathPatterns("/api/**", "/**")
                .excludePathPatterns("/api/actuator/**");
    }
}
