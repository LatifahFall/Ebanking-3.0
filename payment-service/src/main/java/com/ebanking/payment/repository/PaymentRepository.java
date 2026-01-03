package com.ebanking.payment.repository;

import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByFromAccountId(UUID fromAccountId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> findByUserId(UUID userId);

    List<Payment> findByFromAccountIdAndStatus(UUID fromAccountId, PaymentStatus status);
}

