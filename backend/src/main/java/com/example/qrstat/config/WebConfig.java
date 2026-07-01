package com.example.qrstat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final AppProperties appProperties;

    public WebConfig(AdminAuthInterceptor adminAuthInterceptor, AppProperties appProperties) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.appProperties = appProperties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 前端构建产物的 js/css 带 hash，可以设置一年强缓存
        registry.addResourceHandler("/qr/js/**", "/qr/css/**", "/qr/fonts/**", "/qr/favicon.svg")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
        // index.html 不缓存
        registry.addResourceHandler("/qr/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noStore());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 从 app.public-domain 提取源（去掉路径部分），支持跨域
        String domainOrigin = appProperties.getPublicDomain();
        if (domainOrigin != null) {
            int idx = domainOrigin.indexOf("://");
            if (idx != -1) {
                idx = domainOrigin.indexOf("/", idx + 3);
                if (idx != -1) {
                    domainOrigin = domainOrigin.substring(0, idx);
                }
            }
        }

        // 仅在开发环境（public-domain 为 localhost）时允许开发服务器源
        boolean isDev = domainOrigin != null && domainOrigin.contains("localhost");

        if (isDev) {
            registry.addMapping("/api/**")
                    .allowedOrigins(
                            "http://localhost:8081",
                            "http://127.0.0.1:8081",
                            "http://localhost:8001",
                            "http://127.0.0.1:8001",
                            domainOrigin
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);
        } else {
            registry.addMapping("/api/**")
                    .allowedOrigins(domainOrigin)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(false)
                    .maxAge(3600);
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/status");
    }
}
