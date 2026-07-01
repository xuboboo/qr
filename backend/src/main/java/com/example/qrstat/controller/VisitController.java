package com.example.qrstat.controller;

import com.example.qrstat.dto.ApiResponse;
import com.example.qrstat.repository.QrVisitRepository;
import com.example.qrstat.service.VisitService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    private final QrVisitRepository qrVisitRepository;
    private final VisitService visitService;

    public VisitController(QrVisitRepository qrVisitRepository, VisitService visitService) {
        this.qrVisitRepository = qrVisitRepository;
        this.visitService = visitService;
    }

    @GetMapping("/referer")
    public ApiResponse<List<Map<String, Object>>> refererStats(
            @RequestParam(required = false) String code) {
        return ApiResponse.ok(qrVisitRepository.countByReferer(code));
    }

    @GetMapping("/hour")
    public ApiResponse<List<Map<String, Object>>> hourStats(
            @RequestParam(required = false) String code) {
        return ApiResponse.ok(qrVisitRepository.countByHour(code));
    }

    @GetMapping("/device")
    public ApiResponse<List<Map<String, Object>>> deviceStats(
            @RequestParam(required = false) String code) {
        return ApiResponse.ok(qrVisitRepository.countByDevice(code));
    }

    @GetMapping("/browser")
    public ApiResponse<List<Map<String, Object>>> browserStats(
            @RequestParam(required = false) String code) {
        return ApiResponse.ok(qrVisitRepository.countByBrowser(code));
    }

    @GetMapping("/location")
    public ApiResponse<List<Map<String, Object>>> locationStats(
            @RequestParam(required = false) String code) {
        return ApiResponse.ok(qrVisitRepository.countByLocation(code));
    }

    /**
     * 查询有待回填地区信息的历史数据量
     */
    @GetMapping("/location/pending")
    public ApiResponse<Map<String, Object>> pendingLocation() {
        Map<String, Object> data = new HashMap<>();
        data.put("count", qrVisitRepository.countNullLocation());
        return ApiResponse.ok(data);
    }

    /**
     * 触发历史数据地区信息回填（受管理员认证保护）
     * @param batchSize 每次处理的条数，默认 50
     * @return 本次回填结果
     */
    @PostMapping("/location/backfill")
    public ApiResponse<Map<String, Object>> backfillLocation(
            @RequestParam(defaultValue = "50") int batchSize) {
        int filled = visitService.backfillLocation(batchSize);
        long remaining = qrVisitRepository.countNullLocation();
        Map<String, Object> data = new HashMap<>();
        data.put("filled", filled);
        data.put("remaining", remaining);
        data.put("message", filled > 0
                ? String.format("成功回填 %d 条记录，还剩 %d 条待处理", filled, remaining)
                : "没有找到需要回填的记录");
        return ApiResponse.ok(data);
    }
}
