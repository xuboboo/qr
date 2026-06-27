package com.example.qrstat.service;

import com.example.qrstat.config.AppProperties;
import com.example.qrstat.model.QrCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrImageService {

    private final QrCodeService qrCodeService;
    private final AppProperties appProperties;

    public QrImageService(QrCodeService qrCodeService, AppProperties appProperties) {
        this.qrCodeService = qrCodeService;
        this.appProperties = appProperties;
    }

    public void writeQrImage(String code, OutputStream outputStream) throws Exception {
        QrCode qrCode = qrCodeService.getEnabledByCodeOrThrow(code);
        String trackUrl = qrCodeService.buildTrackUrl(qrCode.getCode());

        Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        int size = appProperties.getQrSize();
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                trackUrl,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
        );

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
    }
}
