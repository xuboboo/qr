package com.example.qrstat.service;

import com.example.qrstat.model.QrCode;
import com.example.qrstat.model.QrVisit;
import com.example.qrstat.repository.QrVisitRepository;
import com.example.qrstat.util.IpUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@Service
public class VisitService {

    private final QrCodeService qrCodeService;
    private final QrVisitRepository qrVisitRepository;

    public VisitService(QrCodeService qrCodeService, QrVisitRepository qrVisitRepository) {
        this.qrCodeService = qrCodeService;
        this.qrVisitRepository = qrVisitRepository;
    }

    @Transactional
    public String recordAndGetTargetUrl(String code, HttpServletRequest request) {
        QrCode qrCode = qrCodeService.getEnabledByCodeOrThrow(code);

        QrVisit visit = new QrVisit();
        visit.setQrCode(code);
        visit.setIp(IpUtil.getClientIp(request));
        visit.setUserAgent(request.getHeader("User-Agent"));
        visit.setReferer(request.getHeader("Referer"));
        qrVisitRepository.insert(visit);

        return qrCode.getTargetUrl();
    }
}
