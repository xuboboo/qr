package com.example.qrstat.service;

import com.example.qrstat.config.AppProperties;
import com.example.qrstat.dto.*;
import com.example.qrstat.exception.BizException;
import com.example.qrstat.model.QrCode;
import com.example.qrstat.model.QrVisit;
import com.example.qrstat.repository.QrCodeRepository;
import com.example.qrstat.repository.QrVisitRepository;
import com.example.qrstat.util.CodeGenerator;
import com.example.qrstat.util.UserAgentUtil;
import com.example.qrstat.util.UrlValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final QrVisitRepository qrVisitRepository;
    private final AppProperties appProperties;
    private final UrlValidator urlValidator;

    public QrCodeService(QrCodeRepository qrCodeRepository,
                         QrVisitRepository qrVisitRepository,
                         AppProperties appProperties,
                         UrlValidator urlValidator) {
        this.qrCodeRepository = qrCodeRepository;
        this.qrVisitRepository = qrVisitRepository;
        this.appProperties = appProperties;
        this.urlValidator = urlValidator;
    }

    @Transactional
    public QrCodeResponse create(CreateQrRequest request) {
        String name = request.getName().trim();
        String targetUrl = request.getTargetUrl().trim();
        urlValidator.validateTargetUrl(targetUrl);

        String code = generateUniqueCode();
        QrCode qrCode = new QrCode();
        qrCode.setCode(code);
        qrCode.setName(name);
        qrCode.setTargetUrl(targetUrl);
        qrCode.setEnabled(true);
        qrCodeRepository.insert(qrCode);

        return toResponse(qrCodeRepository.findByCode(code), true);
    }

    @Transactional
    public QrCodeResponse update(String code, UpdateQrRequest request) {
        QrCode old = getByCodeOrThrow(code);
        String name = request.getName().trim();
        String targetUrl = request.getTargetUrl().trim();
        urlValidator.validateTargetUrl(targetUrl);

        qrCodeRepository.update(old.getCode(), name, targetUrl);
        return toResponse(qrCodeRepository.findByCode(code), true);
    }

    @Transactional
    public QrCodeResponse updateEnabled(String code, boolean enabled) {
        getByCodeOrThrow(code);
        qrCodeRepository.updateEnabled(code, enabled);
        return toResponse(qrCodeRepository.findByCode(code), true);
    }

    public QrCode getByCodeOrThrow(String code) {
        QrCode qrCode = qrCodeRepository.findByCode(code);
        if (qrCode == null) {
            throw new BizException("二维码不存在");
        }
        return qrCode;
    }

    public QrCode getEnabledByCodeOrThrow(String code) {
        QrCode qrCode = getByCodeOrThrow(code);
        if (!Boolean.TRUE.equals(qrCode.getEnabled())) {
            throw new BizException("二维码已停用");
        }
        return qrCode;
    }

    public List<QrCodeResponse> list() {
        List<QrCode> list = qrCodeRepository.findAll();
        List<QrCodeResponse> result = new ArrayList<QrCodeResponse>();
        for (QrCode qrCode : list) {
            result.add(toResponse(qrCode, true));
        }
        return result;
    }

    public QrStatsResponse stats(String code) {
        QrCode qrCode = getByCodeOrThrow(code);

        QrStatsResponse response = new QrStatsResponse();
        response.setQrCode(toResponse(qrCode, false));
        response.setPv(qrVisitRepository.countPv(code));
        response.setUv(qrVisitRepository.countUv(code));
        response.setTodayPv(qrVisitRepository.countTodayPv(code));
        response.setLast7Days(last7Days(code));
        response.setLatestVisits(toVisitResponses(qrVisitRepository.latestVisits(code, 50)));
        return response;
    }

    public String buildTrackUrl(String code) {
        String domain = appProperties.getPublicDomain();
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        return domain + "/q/" + code;
    }

    public String buildQrImageUrl(String code) {
        String domain = appProperties.getPublicDomain();
        if (domain.endsWith("/")) {
            domain = domain.substring(0, domain.length() - 1);
        }
        return domain + "/api/qrcodes/" + code + "/image";
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 20; i++) {
            String code = CodeGenerator.randomCode(8);
            if (!qrCodeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new BizException("短码生成失败，请重试");
    }

    private QrCodeResponse toResponse(QrCode qrCode, boolean withStats) {
        QrCodeResponse response = new QrCodeResponse();
        response.setId(qrCode.getId());
        response.setCode(qrCode.getCode());
        response.setName(qrCode.getName());
        response.setTargetUrl(qrCode.getTargetUrl());
        response.setEnabled(qrCode.getEnabled());
        response.setCreatedAt(qrCode.getCreatedAt());
        response.setUpdatedAt(qrCode.getUpdatedAt());
        response.setTrackUrl(buildTrackUrl(qrCode.getCode()));
        response.setQrImageUrl(buildQrImageUrl(qrCode.getCode()));

        if (withStats) {
            response.setPv(qrVisitRepository.countPv(qrCode.getCode()));
            response.setUv(qrVisitRepository.countUv(qrCode.getCode()));
            response.setTodayPv(qrVisitRepository.countTodayPv(qrCode.getCode()));
        }

        return response;
    }

    private List<DayStatResponse> last7Days(String code) {
        LocalDate start = LocalDate.now().minusDays(6);
        List<DayStatResponse> dbRows = qrVisitRepository.countByDay(code, start);
        Map<String, DayStatResponse> rowMap = new HashMap<String, DayStatResponse>();
        for (DayStatResponse row : dbRows) {
            rowMap.put(row.getDate(), row);
        }

        List<DayStatResponse> result = new ArrayList<DayStatResponse>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = 0; i < 7; i++) {
            String day = start.plusDays(i).format(formatter);
            DayStatResponse row = rowMap.get(day);
            if (row == null) {
                result.add(new DayStatResponse(day, 0L, 0L));
            } else {
                result.add(row);
            }
        }
        return result;
    }

    private List<VisitResponse> toVisitResponses(List<QrVisit> visits) {
        List<VisitResponse> responses = new ArrayList<VisitResponse>();
        for (QrVisit visit : visits) {
            VisitResponse response = new VisitResponse();
            response.setId(visit.getId());
            response.setQrCode(visit.getQrCode());
            response.setIp(visit.getIp());
            response.setUserAgent(visit.getUserAgent());
            response.setReferer(visit.getReferer());
            response.setVisitDate(visit.getVisitDate() == null ? null : new java.sql.Date(visit.getVisitDate().getTime()).toString());
            response.setCreatedAt(visit.getCreatedAt());
            response.setDevice(UserAgentUtil.detectDevice(visit.getUserAgent()));
            response.setBrowser(UserAgentUtil.detectBrowser(visit.getUserAgent()));
            responses.add(response);
        }
        return responses;
    }
}
