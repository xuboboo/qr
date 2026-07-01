package com.example.qrstat.service;

import com.example.qrstat.model.QrCode;
import com.example.qrstat.model.QrVisit;
import com.example.qrstat.repository.QrVisitRepository;
import com.example.qrstat.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class VisitService {

    private static final Logger log = LoggerFactory.getLogger(VisitService.class);

    private final QrCodeService qrCodeService;
    private final QrVisitRepository qrVisitRepository;
    private final LocationService locationService;

    public VisitService(QrCodeService qrCodeService, QrVisitRepository qrVisitRepository, LocationService locationService) {
        this.qrCodeService = qrCodeService;
        this.qrVisitRepository = qrVisitRepository;
        this.locationService = locationService;
    }

    @Transactional
    public String recordAndGetTargetUrl(String code, HttpServletRequest request) {
        QrCode qrCode = qrCodeService.getEnabledByCodeOrThrow(code);

        QrVisit visit = new QrVisit();
        visit.setQrCode(code);
        visit.setIp(IpUtil.normalizeIp(IpUtil.getClientIp(request)));
        visit.setLocation(locationService.getLocation(visit.getIp()));
        visit.setUserAgent(request.getHeader("User-Agent"));
        visit.setReferer(request.getHeader("Referer"));
        qrVisitRepository.insert(visit);

        return qrCode.getTargetUrl();
    }

    /**
     * 批量回填历史数据的 location（之前没有查询地区功能时积累的空记录）
     * @param batchSize 每批处理条数
     * @return 本次成功回填的数量
     */
    // 不加 @Transactional：内部含 HTTP 调用 + Thread.sleep，长事务会耗尽连接池
    public int backfillLocation(int batchSize) {
        List<QrVisit> nullLocationRecords = qrVisitRepository.findNullLocation(batchSize);
        int count = 0;
        for (QrVisit visit : nullLocationRecords) {
            if (visit.getIp() == null || visit.getIp().isEmpty()) {
                continue;
            }
            String location = locationService.getLocation(visit.getIp());
            if (location != null) {
                qrVisitRepository.updateLocation(visit.getId(), location);
                count++;
            }
            // ip-api.com 限制 45 次/分钟，加 1.5s 延迟避免被封
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (count > 0) {
            log.info("Backfilled location for {} records (batch of {})", count, nullLocationRecords.size());
        }
        return count;
    }

    /**
     * 全量自动回填，分批处理直到全部完成
     */
    @PostConstruct
    public void autoBackfillAll() {
        long total = qrVisitRepository.countNullLocation();
        if (total == 0) {
            return;
        }
        log.info("Starting auto backfill for {} records...", total);
        int batchSize = 30;
        int totalFilled = 0;
        while (true) {
            int filled = backfillLocation(batchSize);
            totalFilled += filled;
            long remaining = qrVisitRepository.countNullLocation();
            log.info("Auto backfill progress: filled={}, remaining={}", totalFilled, remaining);
            if (remaining == 0) {
                break;
            }
        }
        log.info("Auto backfill completed, total filled={}", totalFilled);
    }
}