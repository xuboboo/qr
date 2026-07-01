package com.example.qrstat.util;

import javax.servlet.http.HttpServletRequest;

public final class IpUtil {

    private IpUtil() {
    }

    public static String getClientIp(HttpServletRequest request) {
        String ip = getHeader(request, "X-Forwarded-For");
        if (isValid(ip)) {
            return ip.split(",")[0].trim();
        }

        ip = getHeader(request, "X-Real-IP");
        if (isValid(ip)) {
            return ip;
        }

        ip = getHeader(request, "Proxy-Client-IP");
        if (isValid(ip)) {
            return ip;
        }

        ip = getHeader(request, "WL-Proxy-Client-IP");
        if (isValid(ip)) {
            return ip;
        }

        return request.getRemoteAddr();
    }

    private static String getHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? null : value.trim();
    }

    private static boolean isValid(String ip) {
        return ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 规范化 IP 显示：IPv6 loopback → IPv4 loopback
     */
    public static String normalizeIp(String ip) {
        if (ip == null) return "";
        // IPv6 loopback
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "0000:0000:0000:0000:0000:0000:0000:0001".equals(ip)) {
            return "127.0.0.1";
        }
        // IPv6 内嵌 IPv4 映射
        if (ip.startsWith("0:0:0:0:0:ffff:")) {
            String v4 = ip.substring(16);
            // 简单验证：包含 3 个点说明是 IPv4
            if (v4.contains(".") && v4.split("\\.").length == 4) {
                return v4;
            }
        }
        return ip;
    }
}
