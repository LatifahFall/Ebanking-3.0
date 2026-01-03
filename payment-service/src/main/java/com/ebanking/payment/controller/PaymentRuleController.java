package com.ebanking.payment.controller;

import com.ebanking.payment.dto.PaymentRuleRequest;
import com.ebanking.payment.entity.PaymentRule;
import com.ebanking.payment.service.PaymentRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Payment Rules (Admin)", description = "API for managing payment rules (Admin only)")
public class PaymentRuleController {

    private final PaymentRuleService paymentRuleService;

    @GetMapping
    @Operation(summary = "List all payment rules", description = "Retrieves all payment rules (admin only)")
    @ApiResponse(responseCode = "200", description = "Rules retrieved successfully")
    public ResponseEntity<List<PaymentRule>> getAllRules() {
        List<PaymentRule> rules = paymentRuleService.getAllRules();
        return ResponseEntity.ok(rules);
    }

    @PostMapping
    @Operation(summary = "Create a payment rule", description = "Creates a new payment rule for validating or restricting payments")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid rule request")
    })
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
    @Operation(summary = "Update a payment rule", description = "Updates an existing payment rule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rule updated successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found"),
            @ApiResponse(responseCode = "400", description = "Invalid rule request")
    })
    public ResponseEntity<PaymentRule> updateRule(
            @Parameter(description = "Rule UUID", required = true)
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
    @Operation(summary = "Delete a payment rule", description = "Deletes a payment rule by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Rule deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Rule not found")
    })
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Rule UUID", required = true)
            @PathVariable UUID id) {
        paymentRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}

