package com.ebanking.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "qr_code_payment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodePayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "qr_token", nullable = false, unique = true, length = 255)
    private String qrToken;
    
    @Column(name = "qr_code_data", nullable = false, length = 1000)
    private String qrCodeData; // JSON data encoded in QR code
    
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;
    
    @Column(name = "to_account_id")
    private UUID toAccountId;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isUsed == null) {
            isUsed = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

