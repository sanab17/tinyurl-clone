package com.urlshortener.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Service class that handles the generation of QR Code graphics.
 * Leverages the Google ZXing library to produce PNG files encoded as Base64 data strings.
 */
@Service
public class QrCodeService {

    private static final Logger logger = LoggerFactory.getLogger(QrCodeService.class);

    /**
     * Generates a QR Code representing the given text, writes it as a PNG stream,
     * and encodes it into a Base64 string for direct embedding in HTML template image tags.
     * Returns null if generation encounters an exception.
     *
     * @param text   the payload content to encode (typically the shortened URL link)
     * @param width  the output width in pixels
     * @param height the output height in pixels
     * @return the Base64-encoded PNG image string, or null if generation fails
     */
    public String generateQrCodeBase64(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngData = pngOutputStream.toByteArray();

            logger.info("Successfully generated QR Code image for target link: {}", text);
            return Base64.getEncoder().encodeToString(pngData);
        } catch (Exception e) {
            logger.error("Failed to generate QR Code image for target link: {}. Error: {}", text, e.getMessage(), e);
            // Fallback: return null if generation fails
            return null;
        }
    }
}
