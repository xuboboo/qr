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
    private List<String> allowedTargetHosts = new ArrayList<>();
    private boolean blockPrivateAddress = true;

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
}
