package com.ebanking.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_biometric_enrollment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBiometricEnrollment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "face_token", nullable = false, length = 255)
    private String faceToken;
    
    @Column(name = "biometric_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BiometricType biometricType;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;
    
    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enrolledAt == null) {
            enrolledAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum BiometricType {
        FACE,
        FINGERPRINT,
        VOICE,
        IRIS
    }
}

