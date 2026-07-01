package com.example.qrstat.controller;

import com.example.qrstat.exception.BizException;
import com.example.qrstat.service.VisitService;
import com.example.qrstat.util.UrlValidator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class RedirectController {

    private final VisitService visitService;
    private final UrlValidator urlValidator;

    public RedirectController(VisitService visitService, UrlValidator urlValidator) {
        this.visitService = visitService;
        this.urlValidator = urlValidator;
    }

    @GetMapping("/q/{code}")
    public void redirect(@PathVariable String code,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        try {
            String targetUrl = visitService.recordAndGetTargetUrl(code, request);
            urlValidator.validateTargetUrl(targetUrl);
            response.sendRedirect(targetUrl);
        } catch (BizException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("<html><body><h3>二维码不可用</h3><p>" + escape(e.getMessage()) + "</p></body></html>");
        }
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
