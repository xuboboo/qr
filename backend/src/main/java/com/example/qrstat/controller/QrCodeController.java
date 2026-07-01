package com.example.qrstat.controller;

import com.example.qrstat.dto.*;
import com.example.qrstat.service.QrCodeService;
import com.example.qrstat.service.QrImageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qrcodes")
@Validated
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final QrImageService qrImageService;

    public QrCodeController(QrCodeService qrCodeService, QrImageService qrImageService) {
        this.qrCodeService = qrCodeService;
        this.qrImageService = qrImageService;
    }

    @PostMapping
    public ApiResponse<QrCodeResponse> create(@Valid @RequestBody CreateQrRequest request) {
        return ApiResponse.ok(qrCodeService.create(request));
    }

    @PutMapping("/{code}")
    public ApiResponse<QrCodeResponse> update(@PathVariable String code, @Valid @RequestBody UpdateQrRequest request) {
        qrImageService.evictCache(code);
        return ApiResponse.ok(qrCodeService.update(code, request));
    }

    @PutMapping("/{code}/enabled")
    public ApiResponse<QrCodeResponse> updateEnabled(@PathVariable String code, @RequestParam boolean enabled) {
        qrImageService.evictCache(code);
        return ApiResponse.ok(qrCodeService.updateEnabled(code, enabled));
    }

    @DeleteMapping("/{code}")
    public ApiResponse<Void> delete(@PathVariable String code) {
        qrImageService.evictCache(code);
        qrCodeService.delete(code);
        return ApiResponse.ok(null);
    }

    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody List<String> codes) {
        for (String code : codes) {
            qrImageService.evictCache(code);
        }
        qrCodeService.batchDelete(codes);
        return ApiResponse.ok(null);
    }

    @GetMapping("/search")
    public ApiResponse<List<QrCodeResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled) {
        return ApiResponse.ok(qrCodeService.search(keyword, enabled));
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(qrCodeService.summary());
    }

    @GetMapping
    public ApiResponse<List<QrCodeResponse>> list() {
        return ApiResponse.ok(qrCodeService.list());
    }

    @GetMapping("/{code}/stats")
    public ApiResponse<QrStatsResponse> stats(@PathVariable String code) {
        return ApiResponse.ok(qrCodeService.stats(code));
    }

    @GetMapping("/{code}/image")
    public void image(@PathVariable String code, HttpServletResponse response) throws Exception {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "public, max-age=86400");
        qrImageService.writeQrImage(code, response.getOutputStream());
    }
}
