package com.example.qrstat.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);
    private static final String API_URL = "http://ip-api.com/json/%s?fields=status,country,countryCode,regionName,city&lang=zh-CN";
    private static final int TIMEOUT_MS = 2000;

    private final RestTemplate restTemplate;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public LocationService() {
        this.restTemplate = new RestTemplate();
        // 设置超时
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        this.restTemplate.setRequestFactory(factory);
    }

    /**
     * 根据 IP 查询地区信息，结果格式："城市, 省份, 国家"
     * 带内存缓存，避免重复查询
     */
    public String getLocation(String ip) {
        if (ip == null || ip.isEmpty() || "unknown".equals(ip)) {
            return null;
        }

        // 本地 IP 直接返回
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "本地";
        }

        // 缓存命中
        if (cache.containsKey(ip)) {
            return cache.get(ip);
        }

        try {
            String url = String.format(API_URL, ip);
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            if (resp == null || !"success".equals(resp.get("status"))) {
                cache.put(ip, null);
                return null;
            }

            String city = (String) resp.getOrDefault("city", "");
            String region = (String) resp.getOrDefault("regionName", "");
            String country = (String) resp.getOrDefault("country", "");

            StringBuilder sb = new StringBuilder();
            if (!city.isEmpty()) sb.append(city);
            if (!region.isEmpty() && !region.equals(city)) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(region);
            }
            if (!country.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }

            String location = sb.length() > 0 ? sb.toString() : null;
            cache.put(ip, location);
            return location;

        } catch (Exception e) {
            log.debug("IP location query failed for {}: {}", ip, e.getMessage());
            cache.put(ip, null);
            return null;
        }
    }
}
