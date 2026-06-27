package com.example.qrstat.controller;

import com.example.qrstat.dto.*;
import com.example.qrstat.service.QrCodeService;
import com.example.qrstat.service.QrImageService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

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
        return ApiResponse.ok(qrCodeService.update(code, request));
    }

    @PutMapping("/{code}/enabled")
    public ApiResponse<QrCodeResponse> updateEnabled(@PathVariable String code, @RequestParam boolean enabled) {
        return ApiResponse.ok(qrCodeService.updateEnabled(code, enabled));
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
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        qrImageService.writeQrImage(code, response.getOutputStream());
    }
}
