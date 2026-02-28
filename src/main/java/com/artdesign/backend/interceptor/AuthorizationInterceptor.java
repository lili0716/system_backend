package com.artdesign.backend.interceptor;

import com.artdesign.backend.entity.User;
import com.artdesign.backend.service.UserService;
import com.artdesign.backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * 权限校验拦截器
 * 在所有接口调用前验证用户是否拥有相应权限
 */
public class AuthorizationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    /** 需要排除的 URI 路径，如登录、注册等不需要权限校验的接口 */
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/register",
            "/api/auth/forget-password",
            "/api/test",
            "/api/actuator",
            "/api/init");

    public AuthorizationInterceptor(JwtUtil jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestUri = request.getRequestURI();

        // 排除不需要权限校验的路径
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestUri.startsWith(excludedPath)) {
                return true;
            }
        }

        // 获取 Authorization 头
        String token = request.getHeader("Authorization");

        // 兼容 SSE (EventSource 等无法携带 Header 的场景)，尝试从 URL Query 提取 token 参数
        if (token == null || token.isEmpty()) {
            token = request.getParameter("token");
        }

        // 检查 token 是否存在
        if (token == null || token.isEmpty()) {
            return handleUnauthorized(response, "Token 不存在");
        }

        try {
            // 验证 token 并获取用户信息
            String employeeId = jwtUtil.getEmployeeId(token);
            if (employeeId == null) {
                return handleUnauthorized(response, "Token 无效或已过期");
            }

            // 获取用户信息
            User user = userService.findByEmployeeId(employeeId);
            if (user == null) {
                return handleUnauthorized(response, "用户不存在");
            }

            // 检查用户状态
            if (!"1".equals(user.getStatus())) {
                return handleForbidden(response, "账号已禁用");
            }

            // 权限校验逻辑
            // 这里可以根据具体的业务需求和权限体系进行扩展
            // 例如：检查用户是否拥有当前接口的权限

            // 示例：检查用户是否为管理员（如果接口需要管理员权限）
            // boolean isAdmin = user.getRoles().stream().anyMatch(r ->
            // Boolean.TRUE.equals(r.getIsAdmin()));
            // if (!isAdmin) {
            // return handleForbidden(response, "权限不足");
            // }

            // 将用户信息存储到请求中，供后续业务代码使用
            request.setAttribute("currentUser", user);
            request.setAttribute("employeeId", employeeId);

            return true;
        } catch (Exception e) {
            return handleUnauthorized(response, "Token 验证失败: " + e.getMessage());
        }
    }

    private boolean handleUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\": 401, \"msg\": \"" + message + "\"}");
        writer.flush();
        writer.close();
        return false;
    }

    private boolean handleForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\": 403, \"msg\": \"" + message + "\"}");
        writer.flush();
        writer.close();
        return false;
    }
}
