package com.ebanking.payment.service;

import com.ebanking.payment.client.faceplusplus.FacePlusPlusClient;
import com.ebanking.payment.entity.UserBiometricEnrollment;
import com.ebanking.payment.exception.BiometricVerificationException;
import com.ebanking.payment.repository.UserBiometricEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiometricEnrollmentService {
    
    private final UserBiometricEnrollmentRepository enrollmentRepository;
    private final FacePlusPlusClient facePlusPlusClient;
    
    /**
     * Enregistre le visage d'un utilisateur (première fois)
     * 
     * @param userId ID de l'utilisateur
     * @param faceImageBase64 Image du visage en Base64
     * @param deviceId ID du dispositif utilisé
     * @return Le face_token stocké
     */
    @Transactional
    public String enrollUserFace(UUID userId, String faceImageBase64, String deviceId) {
        log.info("Enrolling biometric face for user: {}", userId);
        
        // Vérifier si l'utilisateur a déjà un enregistrement actif
        if (enrollmentRepository.existsByUserIdAndIsActiveTrue(userId)) {
            throw new BiometricVerificationException("User already has an active biometric enrollment");
        }
        
        // Détecter le visage avec Face++
        String faceToken = facePlusPlusClient.detectFace(faceImageBase64)
                .block(java.time.Duration.ofSeconds(10));
        
        if (faceToken == null || faceToken.isEmpty()) {
            throw new BiometricVerificationException("No face detected in image. Please ensure the image contains a clear face.");
        }
        
        // Désactiver l'ancien enregistrement s'il existe
        enrollmentRepository.findByUserId(userId)
                .ifPresent(oldEnrollment -> {
                    oldEnrollment.setIsActive(false);
                    enrollmentRepository.save(oldEnrollment);
                });
        
        // Créer le nouvel enregistrement
        UserBiometricEnrollment enrollment = UserBiometricEnrollment.builder()
                .userId(userId)
                .faceToken(faceToken)
                .biometricType(UserBiometricEnrollment.BiometricType.FACE)
                .deviceId(deviceId)
                .enrolledAt(LocalDateTime.now())
                .isActive(true)
                .build();
        
        enrollmentRepository.save(enrollment);
        
        log.info("Biometric enrollment successful for user: {}, face_token: {}", userId, faceToken);
        return faceToken;
    }
    
    /**
     * Récupère le face_token stocké d'un utilisateur
     */
    public String getStoredFaceToken(UUID userId) {
        return enrollmentRepository.findByUserIdAndIsActiveTrue(userId)
                .map(UserBiometricEnrollment::getFaceToken)
                .orElseThrow(() -> new BiometricVerificationException("User has not enrolled biometric data"));
    }
    
    /**
     * Vérifie si l'utilisateur a enregistré des données biométriques
     */
    public boolean hasBiometricEnrollment(UUID userId) {
        return enrollmentRepository.existsByUserIdAndIsActiveTrue(userId);
    }
    
    /**
     * Désactive l'enregistrement biométrique d'un utilisateur
     */
    @Transactional
    public void revokeBiometricEnrollment(UUID userId) {
        enrollmentRepository.findByUserIdAndIsActiveTrue(userId)
                .ifPresent(enrollment -> {
                    enrollment.setIsActive(false);
                    enrollmentRepository.save(enrollment);
                    log.info("Biometric enrollment revoked for user: {}", userId);
                });
    }
    
    /**
     * Met à jour la date de dernière vérification
     */
    @Transactional
    public void updateLastVerifiedAt(UUID userId) {
        enrollmentRepository.findByUserIdAndIsActiveTrue(userId)
                .ifPresent(enrollment -> {
                    enrollment.setLastVerifiedAt(LocalDateTime.now());
                    enrollmentRepository.save(enrollment);
                });
    }
}

