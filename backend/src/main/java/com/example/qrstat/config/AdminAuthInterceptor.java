package com.example.qrstat.config;

import com.example.qrstat.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AppProperties appProperties;
    private final AuthService authService;

    public AdminAuthInterceptor(AppProperties appProperties, AuthService authService) {
        this.appProperties = appProperties;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!appProperties.getSecurity().isEnabled()) {
            return true;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        // context-path 下需要截掉前缀（如 /qr），再与 api 路径匹配
        String cp = request.getContextPath();
        if (cp != null && !cp.isEmpty()) {
            path = path.substring(cp.length());
        }
        if (isPublicApi(path, request.getMethod())) {
            return true;
        }

        String token = getToken(request);
        if (authService.validateToken(token)) {
            return true;
        }

        writeUnauthorized(response);
        return false;
    }

    private boolean isPublicApi(String path, String method) {
        if (path == null) {
            return false;
        }

        if (path.equals("/api/auth/login")) {
            return true;
        }

        // 二维码图片允许公开访问，方便公众号文章插图、下载、预览；统计数据和管理接口仍然需要登录。
        return "GET".equalsIgnoreCase(method)
                && path.startsWith("/api/qrcodes/")
                && path.endsWith("/image");
    }

    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            String prefix = "Bearer ";
            if (authorization.startsWith(prefix)) {
                return authorization.substring(prefix.length()).trim();
            }
        }

        String headerToken = request.getHeader("X-Admin-Token");
        if (headerToken != null && headerToken.trim().length() > 0) {
            return headerToken.trim();
        }

        return "";
    }

    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write("{\"success\":false,\"message\":\"未登录或登录已过期\",\"data\":null}");
    }
}
