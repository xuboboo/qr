package com.example.qrstat.util;

public final class UserAgentUtil {

    private UserAgentUtil() {
    }

    public static String detectDevice(String userAgent) {
        if (userAgent == null) {
            return "未知";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os")) {
            return "Mac";
        }
        return "其他";
    }

    public static String detectBrowser(String userAgent) {
        if (userAgent == null) {
            return "未知";
        }
        String ua = userAgent.toLowerCase();
        if (ua.contains("micromessenger")) {
            return "微信内置浏览器";
        }
        if (ua.contains("alipayclient")) {
            return "支付宝内置浏览器";
        }
        if (ua.contains("qq/")) {
            return "QQ内置浏览器";
        }
        if (ua.contains("edg/")) {
            return "Edge";
        }
        if (ua.contains("chrome")) {
            return "Chrome";
        }
        if (ua.contains("safari")) {
            return "Safari";
        }
        if (ua.contains("firefox")) {
            return "Firefox";
        }
        return "其他";
    }
}
