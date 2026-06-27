package com.example.qrstat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String publicDomain = "http://localhost:8080";
    private int qrSize = 300;
    private List<String> allowedTargetHosts = new ArrayList<String>();
    private boolean blockPrivateAddress = true;
    private Security security = new Security();

    public String getPublicDomain() {
        return publicDomain;
    }

    public void setPublicDomain(String publicDomain) {
        this.publicDomain = publicDomain;
    }

    public int getQrSize() {
        return qrSize;
    }

    public void setQrSize(int qrSize) {
        this.qrSize = qrSize;
    }

    public List<String> getAllowedTargetHosts() {
        return allowedTargetHosts;
    }

    public void setAllowedTargetHosts(List<String> allowedTargetHosts) {
        this.allowedTargetHosts = allowedTargetHosts;
    }

    public boolean isBlockPrivateAddress() {
        return blockPrivateAddress;
    }

    public void setBlockPrivateAddress(boolean blockPrivateAddress) {
        this.blockPrivateAddress = blockPrivateAddress;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Security {
        private boolean enabled = true;
        private String adminUsername = "admin";
        private String adminPassword = "ChangeMe123!";
        private int sessionExpireMinutes = 720;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getAdminUsername() {
            return adminUsername;
        }

        public void setAdminUsername(String adminUsername) {
            this.adminUsername = adminUsername;
        }

        public String getAdminPassword() {
            return adminPassword;
        }

        public void setAdminPassword(String adminPassword) {
            this.adminPassword = adminPassword;
        }

        public int getSessionExpireMinutes() {
            return sessionExpireMinutes;
        }

        public void setSessionExpireMinutes(int sessionExpireMinutes) {
            this.sessionExpireMinutes = sessionExpireMinutes;
        }
    }
}
