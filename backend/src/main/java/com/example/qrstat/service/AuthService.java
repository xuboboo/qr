package com.example.qrstat.service;

import com.example.qrstat.config.AppProperties;
import com.example.qrstat.dto.LoginResponse;
import com.example.qrstat.exception.BizException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private static final char[] TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final AppProperties appProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

    public AuthService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public LoginResponse login(String username, String password) {
        AppProperties.Security security = appProperties.getSecurity();

        if (!security.isEnabled()) {
            return new LoginResponse("security-disabled", security.getAdminUsername());
        }

        String actualUsername = username == null ? "" : username.trim();
        String actualPassword = password == null ? "" : password;

        if (!security.getAdminUsername().equals(actualUsername) || !security.getAdminPassword().equals(actualPassword)) {
            throw new BizException("用户名或密码错误");
        }

        cleanupExpiredSessions();

        String token = generateToken(48);
        long now = System.currentTimeMillis();
        long expireAt = now + security.getSessionExpireMinutes() * 60L * 1000L;
        sessions.put(token, new Session(security.getAdminUsername(), expireAt));

        return new LoginResponse(token, security.getAdminUsername());
    }

    public boolean validateToken(String token) {
        if (!appProperties.getSecurity().isEnabled()) {
            return true;
        }

        if (token == null || token.trim().length() == 0) {
            return false;
        }

        Session session = sessions.get(token.trim());
        if (session == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        if (session.getExpireAt() < now) {
            sessions.remove(token.trim());
            return false;
        }

        // 滑动过期：只要后台还在使用，就自动延长会话有效期。
        session.setExpireAt(now + appProperties.getSecurity().getSessionExpireMinutes() * 60L * 1000L);
        return true;
    }

    public void logout(String token) {
        if (token != null && token.trim().length() > 0) {
            sessions.remove(token.trim());
        }
    }

    private String generateToken(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(TOKEN_CHARS[secureRandom.nextInt(TOKEN_CHARS.length)]);
        }
        return builder.toString();
    }

    private void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Session>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Session> entry = iterator.next();
            if (entry.getValue().getExpireAt() < now) {
                iterator.remove();
            }
        }
    }

    private static class Session {
        private final String username;
        private long expireAt;

        Session(String username, long expireAt) {
            this.username = username;
            this.expireAt = expireAt;
        }

        public String getUsername() {
            return username;
        }

        public long getExpireAt() {
            return expireAt;
        }

        public void setExpireAt(long expireAt) {
            this.expireAt = expireAt;
        }
    }
}
