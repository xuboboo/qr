package com.example.qrstat.service;

import com.example.qrstat.config.AppProperties;
import com.example.qrstat.model.QrCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QrImageService {

    private final QrCodeService qrCodeService;
    private final AppProperties appProperties;

    // 二维码图片字节缓存，key=code
    private final ConcurrentHashMap<String, byte[]> imageCache = new ConcurrentHashMap<>();

    public QrImageService(QrCodeService qrCodeService, AppProperties appProperties) {
        this.qrCodeService = qrCodeService;
        this.appProperties = appProperties;
    }

    public void writeQrImage(String code, OutputStream outputStream) throws Exception {
        byte[] bytes = imageCache.get(code);
        if (bytes == null) {
            bytes = generateImageBytes(code);
            imageCache.put(code, bytes);
        }
        outputStream.write(bytes);
        outputStream.flush();
    }

    private byte[] generateImageBytes(String code) throws Exception {
        QrCode qrCode = qrCodeService.getEnabledByCodeOrThrow(code);
        String trackUrl = qrCodeService.buildTrackUrl(qrCode.getCode());

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        int size = appProperties.getQrSize();
        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                trackUrl, BarcodeFormat.QR_CODE, size, size, hints
        );

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return baos.toByteArray();
    }

    /** 清除指定二维码的图片缓存 */
    public void evictCache(String code) {
        imageCache.remove(code);
    }

    /** 清除全部图片缓存 */
    public void evictAll() {
        imageCache.clear();
    }
}
