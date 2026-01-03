package com.ebanking.payment.service;

import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentRule;
import com.ebanking.payment.repository.PaymentRuleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentRuleService {

    private final PaymentRuleRepository paymentRuleRepository;
    private final ObjectMapper objectMapper;

    public boolean evaluatePayment(Payment payment) {
        List<PaymentRule> activeRules = paymentRuleRepository.findByEnabledTrueOrderByPriorityDesc();
        
        for (PaymentRule rule : activeRules) {
            if (!evaluateRule(rule, payment)) {
                log.warn("Payment {} failed rule: {}", payment.getId(), rule.getRuleName());
                return false;
            }
        }
        return true;
    }

    private boolean evaluateRule(PaymentRule rule, Payment payment) {
        try {
            JsonNode conditions = objectMapper.readTree(rule.getConditions());
            
            // Example: {"maxAmount": 10000, "currency": "EUR"}
            if (conditions.has("maxAmount")) {
                BigDecimal maxAmount = conditions.get("maxAmount").decimalValue();
                if (payment.getAmount().compareTo(maxAmount) > 0) {
                    return false;
                }
            }
            
            if (conditions.has("minAmount")) {
                BigDecimal minAmount = conditions.get("minAmount").decimalValue();
                if (payment.getAmount().compareTo(minAmount) < 0) {
                    return false;
                }
            }
            
            if (conditions.has("currency")) {
                String allowedCurrency = conditions.get("currency").asText();
                if (!payment.getCurrency().equals(allowedCurrency)) {
                    return false;
                }
            }
            
            return true;
        } catch (JsonProcessingException e) {
            log.error("Error parsing rule conditions: {}", rule.getRuleName(), e);
            return false;
        }
    }

    public List<PaymentRule> getActiveRules() {
        return paymentRuleRepository.findByEnabledTrueOrderByPriorityDesc();
    }

    @Transactional
    public PaymentRule createRule(PaymentRule rule) {
        return paymentRuleRepository.save(rule);
    }

    public PaymentRule getRuleById(UUID ruleId) {
        return paymentRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
    }

    @Transactional
    public void deleteRule(UUID ruleId) {
        if (!paymentRuleRepository.existsById(ruleId)) {
            throw new RuntimeException("Rule not found: " + ruleId);
        }
        paymentRuleRepository.deleteById(ruleId);
    }

    @Transactional
    public List<PaymentRule> getAllRules() {
        return paymentRuleRepository.findAll();
    }
}

