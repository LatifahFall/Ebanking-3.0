package com.ebanking.payment.repository;

import com.ebanking.payment.entity.PaymentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRuleRepository extends JpaRepository<PaymentRule, UUID> {

    List<PaymentRule> findByEnabledTrue();

    List<PaymentRule> findByEnabledTrueOrderByPriorityDesc();
}

