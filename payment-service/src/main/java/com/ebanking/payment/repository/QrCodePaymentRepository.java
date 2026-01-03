package com.ebanking.payment.repository;

import com.ebanking.payment.entity.QrCodePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QrCodePaymentRepository extends JpaRepository<QrCodePayment, UUID> {
    
    Optional<QrCodePayment> findByQrToken(String qrToken);
    
    Optional<QrCodePayment> findByPaymentId(UUID paymentId);
    
    void deleteByExpiresAtBefore(LocalDateTime now);
}

