package com.ebanking.payment.controller;

import com.ebanking.payment.dto.PaymentRuleRequest;
import com.ebanking.payment.entity.PaymentRule;
import com.ebanking.payment.service.PaymentRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/payment-rules")
@RequiredArgsConstructor
@Slf4j
public class PaymentRuleController {

    private final PaymentRuleService paymentRuleService;

    @GetMapping
    public ResponseEntity<List<PaymentRule>> getAllRules() {
        List<PaymentRule> rules = paymentRuleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @PostMapping
    public ResponseEntity<PaymentRule> createRule(@Valid @RequestBody PaymentRuleRequest request) {
        PaymentRule rule = PaymentRule.builder()
                .ruleType(request.getRuleType())
                .ruleName(request.getRuleName())
                .description(request.getDescription())
                .conditions(request.getConditions())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .build();
        
        PaymentRule created = paymentRuleService.createRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentRule> updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentRuleRequest request) {
        PaymentRule existingRule = paymentRuleService.getRuleById(id);
        
        existingRule.setRuleType(request.getRuleType());
        existingRule.setRuleName(request.getRuleName());
        existingRule.setDescription(request.getDescription());
        existingRule.setConditions(request.getConditions());
        if (request.getEnabled() != null) {
            existingRule.setEnabled(request.getEnabled());
        }
        if (request.getPriority() != null) {
            existingRule.setPriority(request.getPriority());
        }
        
        PaymentRule updated = paymentRuleService.createRule(existingRule);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable UUID id) {
        paymentRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}

