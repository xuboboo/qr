package com.example.qrstat.util;

import com.example.qrstat.config.AppProperties;
import com.example.qrstat.exception.BizException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;

@Component
public class UrlValidator {

    private final AppProperties appProperties;

    public UrlValidator(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void validateTargetUrl(String targetUrl) {
        URI uri;
        try {
            uri = new URI(targetUrl.trim());
        } catch (Exception e) {
            throw new BizException("目标链接格式不正确");
        }

        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new BizException("目标链接只支持 http 或 https");
        }

        String host = uri.getHost();
        if (host == null || host.trim().length() == 0) {
            throw new BizException("目标链接缺少域名");
        }

        if (appProperties.isBlockPrivateAddress() && isPrivateHost(host)) {
            throw new BizException("不允许跳转到 localhost 或内网地址");
        }

        List<String> allowedHosts = appProperties.getAllowedTargetHosts();
        if (!CollectionUtils.isEmpty(allowedHosts)) {
            boolean allowed = false;
            for (String allowedHost : allowedHosts) {
                if (allowedHost == null || allowedHost.trim().length() == 0) {
                    continue;
                }
                String normalized = allowedHost.trim().toLowerCase();
                String actual = host.toLowerCase();
                if (actual.equals(normalized) || actual.endsWith("." + normalized)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                throw new BizException("目标链接域名不在白名单内");
            }
        }
    }

    private boolean isPrivateHost(String host) {
        String lower = host.toLowerCase();
        if ("localhost".equals(lower) || lower.endsWith(".localhost")) {
            return true;
        }

        try {
            InetAddress address = InetAddress.getByName(host);
            return address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress();
        } catch (Exception e) {
            return false;
        }
    }
}
