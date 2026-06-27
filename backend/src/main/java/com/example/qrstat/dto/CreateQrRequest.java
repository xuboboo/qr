package com.example.qrstat.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateQrRequest {

    @NotBlank(message = "二维码名称不能为空")
    @Size(max = 100, message = "二维码名称不能超过100个字符")
    private String name;

    @NotBlank(message = "目标链接不能为空")
    @Size(max = 2000, message = "目标链接不能超过2000个字符")
    private String targetUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
