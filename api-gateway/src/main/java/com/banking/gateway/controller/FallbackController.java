package com.banking.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Fallback controller for circuit breaker patterns.
 * Returns user-friendly error messages when services are unavailable.
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public ResponseEntity<Map<String, Object>> authServiceFallback() {
        return createFallbackResponse(
            "Authentication Service",
            "The authentication service is temporarily unavailable. Please try again later."
        );
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> userServiceFallback() {
        return createFallbackResponse(
            "User Service",
            "The user service is temporarily unavailable. Please try again later."
        );
    }

    @GetMapping("/account")
    public ResponseEntity<Map<String, Object>> accountServiceFallback() {
        return createFallbackResponse(
            "Account Service",
            "The account service is temporarily unavailable. Please try again later."
        );
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        return createFallbackResponse(
            "Payment Service",
            "The payment service is temporarily unavailable. Please try again later."
        );
    }

    @GetMapping("/crypto")
    public ResponseEntity<Map<String, Object>> cryptoServiceFallback() {
        return createFallbackResponse(
            "Crypto Service",
            "The cryptocurrency service is temporarily unavailable. Please try again later."
        );
    }

    @GetMapping("/notification")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        return createFallbackResponse(
            "Notification Service",
            "The notification service is temporarily unavailable. Please try again later."
        );
    }

    @GetMapping("/audit")
    public ResponseEntity<Map<String, Object>> auditServiceFallback() {
        return createFallbackResponse(
            "Audit Service",
            "The audit service is temporarily unavailable. Please try again later."
        );
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> analyticsServiceFallback() {
        return createFallbackResponse(
            "Analytics Service",
            "The analytics service is temporarily unavailable. Please try again later."
        );
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName, String message) {
        Map<String, Object> response = Map.of(
            "timestamp", Instant.now(),
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "error", "Service Unavailable",
            "message", message,
            "service", serviceName
        );
        
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(response);
    }
}
