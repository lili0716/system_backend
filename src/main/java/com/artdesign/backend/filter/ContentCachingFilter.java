package com.artdesign.backend.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * 内容缓存过滤器
 * 将请求和响应包装为可重复读取的版本，供日志拦截器使用
 */
@Component
@Order(1)
public class ContentCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(
                    (HttpServletRequest) request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(
                    (HttpServletResponse) response);

            try {
                chain.doFilter(wrappedRequest, wrappedResponse);
            } finally {
                // 确保响应体被写回到原始 response
                wrappedResponse.copyBodyToResponse();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
