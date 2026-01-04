package com.ebanking.payment.repository;

import com.ebanking.payment.entity.QrCodePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface QrCodePaymentRepository extends JpaRepository<QrCodePayment, Long> {
    
    Optional<QrCodePayment> findByQrToken(String qrToken);
    
    Optional<QrCodePayment> findByPaymentId(Long paymentId);
    
    void deleteByExpiresAtBefore(LocalDateTime now);
}

