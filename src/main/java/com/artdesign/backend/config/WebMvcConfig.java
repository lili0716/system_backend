package com.artdesign.backend.config;

import com.artdesign.backend.interceptor.ApiLoggingInterceptor;
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
        registry.addInterceptor(new ApiLoggingInterceptor(systemLogRepository, jwtUtil, userService))
                .addPathPatterns("/api/**", "/**")
                .excludePathPatterns("/api/actuator/**");
    }
}
