package com.ebanking.payment.controller;

import com.ebanking.payment.dto.PaymentListResponse;
import com.ebanking.payment.dto.PaymentRequest;
import com.ebanking.payment.dto.PaymentResponse;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.ReversalReason;
import com.ebanking.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        UUID userId = extractUserId(authentication);
        PaymentResponse response = paymentService.initiatePayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID id) {
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaymentListResponse> getPayments(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir, sortBy));
        
        if (accountId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<PaymentResponse> payments = paymentService.getPaymentsByAccount(accountId, status, pageable);
        
        PaymentListResponse response = PaymentListResponse.builder()
                .payments(payments)
                .totalElements(payments.size())
                .currentPage(page)
                .pageSize(size)
                .hasNext(payments.size() == size)
                .hasPrevious(page > 0)
                .totalPages((int) Math.ceil((double) payments.size() / size))
                .build();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable UUID id) {
        PaymentResponse response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<PaymentResponse> reversePayment(
            @PathVariable UUID id,
            @RequestParam ReversalReason reason) {
        PaymentResponse response = paymentService.reversePayment(id, reason);
        return ResponseEntity.ok(response);
    }

    private UUID extractUserId(Authentication authentication) {
        // Extract user ID from authentication token
        // This is a placeholder - actual implementation depends on your auth setup
        try {
            String userIdString = authentication.getName();
            return UUID.fromString(userIdString);
        } catch (Exception e) {
            log.warn("Could not extract userId from authentication, using random UUID");
            return UUID.randomUUID();
        }
    }
}

