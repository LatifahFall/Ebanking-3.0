package com.ebanking.payment.service;

import com.ebanking.payment.dto.BiometricPaymentRequest;
import com.ebanking.payment.entity.QrCodePayment;
import com.ebanking.payment.exception.BiometricVerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiometricVerificationService {

    private final QrCodeService qrCodeService;

    @Value("${biometric.verification.enabled:true}")
    private boolean verificationEnabled;



    /**
     * Vérifie les données biométriques de l'utilisateur
     *
     * @param userId ID de l'utilisateur
     * @param biometricData Données biométriques à vérifier
     * @return true si la vérification réussit
     * @throws BiometricVerificationException si la vérification échoue
     */
    public boolean verifyBiometric(UUID userId, BiometricPaymentRequest.BiometricData biometricData) {
        if (!verificationEnabled) {
            log.warn("Biometric verification is disabled, skipping verification");
            return true;
        }

        log.info("Verifying biometric data for user: {}, type: {}", userId, biometricData.getType());

        try {
            // Vérifications de base
            validateBiometricData(biometricData);

            // Appel au service de vérification biométrique (externe)
            boolean verified = performBiometricVerification(userId, biometricData);

            if (!verified) {
                log.warn("Biometric verification failed for user: {}", userId);
                throw new BiometricVerificationException("Biometric verification failed");
            }

            log.info("Biometric verification successful for user: {}", userId);
            return true;

        } catch (BiometricVerificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during biometric verification for user: {}", userId, e);
            throw new BiometricVerificationException("Biometric verification error: " + e.getMessage(), e);
        }
    }


    private void validateBiometricData(BiometricPaymentRequest.BiometricData biometricData) {
        if (biometricData.getQrToken() == null || biometricData.getQrToken().isEmpty()) {
            throw new BiometricVerificationException("QR code token is required");
        }

        if (biometricData.getType() == null) {
            throw new BiometricVerificationException("Biometric type is required");
        }
    }

    /**
     * Effectue la vérification biométrique avec QR code
     */
    private boolean performBiometricVerification(UUID userId, BiometricPaymentRequest.BiometricData biometricData) {
        log.debug("Performing QR code verification for user: {}", userId);

        // Vérifier le type biométrique
        if (biometricData.getType() != BiometricPaymentRequest.BiometricType.QR_CODE) {
            throw new BiometricVerificationException("Only QR_CODE biometric type is supported");
        }

        // Valider le QR code token
        QrCodePayment qrCodePayment = qrCodeService.validateQrCode(biometricData.getQrToken(), userId);
        
        log.info("QR code verification successful for user: {}, payment: {}", userId, qrCodePayment.getPaymentId());
        return true;
    }

    /**
     * Vérifie si l'utilisateur peut utiliser les paiements biométriques (QR code)
     * Avec QR code, pas besoin d'enrollment préalable
     */
    public boolean hasBiometricEnrollment(UUID userId) {
        // Avec QR code, tous les utilisateurs peuvent utiliser les paiements biométriques
        return true;
    }
}

