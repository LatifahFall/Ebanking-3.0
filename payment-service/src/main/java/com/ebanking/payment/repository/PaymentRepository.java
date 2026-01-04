package com.ebanking.payment.repository;

import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByFromAccountId(Long fromAccountId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> findByUserId(Long userId);

    List<Payment> findByFromAccountIdAndStatus(Long fromAccountId, PaymentStatus status);
}

