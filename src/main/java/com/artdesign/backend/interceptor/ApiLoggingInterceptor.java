package com.artdesign.backend.interceptor;

import com.artdesign.backend.entity.SystemLog;
import com.artdesign.backend.entity.User;
import com.artdesign.backend.repository.SystemLogRepository;
import com.artdesign.backend.service.UserService;
import com.artdesign.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * API 调用日志拦截器
 * 记录每次 API 调用的请求参数和响应结果
 */
public class ApiLoggingInterceptor implements HandlerInterceptor {

    private final SystemLogRepository systemLogRepository;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    /** 需要排除的 URI 前缀，避免循环记录或记录无用请求 */
    private static final Set<String> EXCLUDED_PREFIXES = Set.of(
            "/api/ops/logs",
            "/api/actuator",
            "/api/ops/server-info");

    public ApiLoggingInterceptor(SystemLogRepository systemLogRepository, JwtUtil jwtUtil, UserService userService) {
        this.systemLogRepository = systemLogRepository;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("_logStartTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        try {
            String requestUri = request.getRequestURI();

            // 排除不需要记录的路径
            for (String prefix : EXCLUDED_PREFIXES) {
                if (requestUri.startsWith(prefix)) {
                    return;
                }
            }

            long startTime = (Long) request.getAttribute("_logStartTime");
            long duration = System.currentTimeMillis() - startTime;

            SystemLog log = new SystemLog();
            log.setRequestTime(LocalDateTime.now());
            log.setMethod(request.getMethod());
            log.setUri(requestUri);
            log.setDuration(duration);
            log.setResponseCode(response.getStatus());
            log.setIp(getClientIp(request));

            // 从 JWT Token 解析用户信息
            String token = request.getHeader("Authorization");
            if (token != null && !token.isEmpty()) {
                try {
                    String employeeId = jwtUtil.getEmployeeId(token);
                    if (employeeId != null) {
                        log.setEmployeeId(employeeId);
                        User user = userService.findByEmployeeId(employeeId);
                        if (user != null) {
                            log.setNickName(user.getNickName());
                        }
                    }
                } catch (Exception e) {
                    // Token 解析失败，不影响日志记录
                }
            }

            // 读取请求体
            if (request instanceof ContentCachingRequestWrapper) {
                ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;
                byte[] body = wrappedRequest.getContentAsByteArray();
                if (body.length > 0) {
                    log.setRequestParams(truncate(new String(body, StandardCharsets.UTF_8), 2000));
                }
            }
            // 如果不是 wrapped 的请求，尝试获取 query string
            if (log.getRequestParams() == null || log.getRequestParams().isEmpty()) {
                String queryString = request.getQueryString();
                if (queryString != null && !queryString.isEmpty()) {
                    log.setRequestParams(truncate(queryString, 2000));
                }
            }

            // 读取响应体
            if (response instanceof ContentCachingResponseWrapper) {
                ContentCachingResponseWrapper wrappedResponse = (ContentCachingResponseWrapper) response;
                byte[] body = wrappedResponse.getContentAsByteArray();
                if (body.length > 0) {
                    log.setResponseBody(truncate(new String(body, StandardCharsets.UTF_8), 2000));
                }
            }

            // 异步保存日志（避免阻塞响应）
            systemLogRepository.save(log);

        } catch (Exception e) {
            // 日志记录失败不影响业务
            e.printStackTrace();
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null)
            return null;
        if (str.length() <= maxLength)
            return str;
        return str.substring(0, maxLength) + "...(truncated)";
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理时取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
