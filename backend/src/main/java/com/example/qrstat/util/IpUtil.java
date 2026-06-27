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
}
