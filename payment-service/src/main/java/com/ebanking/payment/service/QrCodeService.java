package com.ebanking.payment.service;

import com.ebanking.payment.entity.QrCodePayment;
import com.ebanking.payment.exception.BiometricVerificationException;
import com.ebanking.payment.repository.QrCodePaymentRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {
    
    private final QrCodePaymentRepository qrCodePaymentRepository;
    
    @Value("${qr-code.expiration-minutes:5}")
    private int expirationMinutes;
    
    @Value("${qr-code.width:300}")
    private int qrCodeWidth;
    
    @Value("${qr-code.height:300}")
    private int qrCodeHeight;
    
    /**
     * Génère un QR code pour un paiement
     * 
     * @param paymentId ID du paiement
     * @param userId ID de l'utilisateur
     * @param amount Montant du paiement
     * @param currency Devise
     * @param fromAccountId Compte source
     * @param toAccountId Compte destination (optionnel)
     * @return QR code en Base64 (image PNG)
     */
    @Transactional
    public String generateQrCode(UUID paymentId, UUID userId, BigDecimal amount, 
                                String currency, UUID fromAccountId, UUID toAccountId) {
        log.info("Generating QR code for payment: {}, user: {}", paymentId, userId);
        
        // Générer un token unique et sécurisé
        String qrToken = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        
        // Créer les données JSON pour le QR code
        Map<String, Object> qrData = new HashMap<>();
        qrData.put("token", qrToken);
        qrData.put("paymentId", paymentId.toString());
        qrData.put("userId", userId.toString());
        qrData.put("amount", amount.toString());
        qrData.put("currency", currency);
        qrData.put("timestamp", System.currentTimeMillis());
        
        String qrCodeData = convertMapToJson(qrData);
        
        // Calculer la date d'expiration
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        
        // Sauvegarder dans la base de données
        QrCodePayment qrCodePayment = QrCodePayment.builder()
                .paymentId(paymentId)
                .userId(userId)
                .qrToken(qrToken)
                .qrCodeData(qrCodeData)
                .amount(amount)
                .currency(currency)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .expiresAt(expiresAt)
                .isUsed(false)
                .build();
        
        qrCodePaymentRepository.save(qrCodePayment);
        
        // Générer l'image QR code
        try {
            BufferedImage qrImage = generateQrCodeImage(qrCodeData);
            String base64Image = imageToBase64(qrImage);
            
            log.info("QR code generated successfully for payment: {}", paymentId);
            return base64Image;
        } catch (Exception e) {
            log.error("Error generating QR code image for payment: {}", paymentId, e);
            throw new BiometricVerificationException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }
    
    /**
     * Valide un token QR code et marque le paiement comme vérifié
     * 
     * @param qrToken Token du QR code
     * @param userId ID de l'utilisateur (pour vérification)
     * @return QrCodePayment si valide
     * @throws BiometricVerificationException si invalide
     */
    @Transactional
    public QrCodePayment validateQrCode(String qrToken, UUID userId) {
        log.info("Validating QR code token: {} for user: {}", qrToken, userId);
        
        QrCodePayment qrCodePayment = qrCodePaymentRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new BiometricVerificationException("Invalid QR code token"));
        
        // Vérifier que le QR code n'a pas expiré
        if (LocalDateTime.now().isAfter(qrCodePayment.getExpiresAt())) {
            log.warn("QR code token expired: {}", qrToken);
            throw new BiometricVerificationException("QR code has expired");
        }
        
        // Vérifier que le QR code n'a pas déjà été utilisé
        if (qrCodePayment.getIsUsed()) {
            log.warn("QR code token already used: {}", qrToken);
            throw new BiometricVerificationException("QR code has already been used");
        }
        
        // Vérifier que l'utilisateur correspond
        if (!qrCodePayment.getUserId().equals(userId)) {
            log.warn("QR code token user mismatch. Expected: {}, Got: {}", qrCodePayment.getUserId(), userId);
            throw new BiometricVerificationException("QR code does not belong to this user");
        }
        
        // Marquer comme utilisé
        qrCodePayment.setIsUsed(true);
        qrCodePayment.setVerifiedAt(LocalDateTime.now());
        qrCodePaymentRepository.save(qrCodePayment);
        
        log.info("QR code validated successfully for payment: {}", qrCodePayment.getPaymentId());
        return qrCodePayment;
    }
    
    /**
     * Génère l'image QR code à partir des données
     */
    private BufferedImage generateQrCodeImage(String data) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, qrCodeWidth, qrCodeHeight, hints);
        
        BufferedImage image = new BufferedImage(qrCodeWidth, qrCodeHeight, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < qrCodeWidth; x++) {
            for (int y = 0; y < qrCodeHeight; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        
        return image;
    }
    
    /**
     * Convertit une image en Base64
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * Convertit une Map en JSON simple (pour éviter d'ajouter une dépendance JSON)
     */
    private String convertMapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * Nettoie les QR codes expirés (exécuté périodiquement)
     */
    @Scheduled(fixedRate = 3600000) // Toutes les heures
    @Transactional
    public void cleanupExpiredQrCodes() {
        log.info("Cleaning up expired QR codes");
        LocalDateTime now = LocalDateTime.now();
        qrCodePaymentRepository.deleteByExpiresAtBefore(now);
        log.info("Expired QR codes cleaned up");
    }
    
    /**
     * Récupère un QR code payment par payment ID
     */
    public Optional<QrCodePayment> findByPaymentId(UUID paymentId) {
        return qrCodePaymentRepository.findByPaymentId(paymentId);
    }
}
