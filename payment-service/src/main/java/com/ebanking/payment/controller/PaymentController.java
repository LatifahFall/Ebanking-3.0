package com.ebanking.payment.controller;

import com.ebanking.payment.dto.BiometricPaymentRequest;
import com.ebanking.payment.dto.QRCodePaymentRequest;
import com.ebanking.payment.dto.PaymentListResponse;
import com.ebanking.payment.dto.PaymentRequest;
import com.ebanking.payment.dto.PaymentResponse;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.ReversalReason;
import com.ebanking.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "API for managing payments and transfers")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Initiate a new payment",
            description = "Creates and processes a new payment. The payment will be validated, checked for fraud, and processed according to its type (STANDARD or INSTANT).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment request details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
                                      "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
                                      "amount": 100.50,
                                      "currency": "EUR",
                                      "paymentType": "STANDARD",
                                      "reference": "Payment ref 123",
                                      "description": "Payment description"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "403", description = "Fraud detected"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        PaymentResponse response = paymentService.initiatePayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/biometric/generate-qr")
    @Operation(
            summary = "Generate QR code for biometric payment",
            description = "Generates a QR code for a biometric payment. The user must scan this QR code with their mobile app to confirm the payment.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment request details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
                                      "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
                                      "amount": 100.50,
                                      "currency": "EUR",
                                      "reference": "Payment ref 123",
                                      "description": "Biometric payment"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "QR code generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "403", description = "Fraud detected"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateBiometricQrCode(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        Map<String, Object> response = paymentService.generateBiometricPaymentQrCode(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/biometric")
    @Operation(
            summary = "Initiate a biometric payment with QR code",
            description = "Validates a QR code token and processes the biometric payment. The QR code must be generated first using /biometric/generate-qr endpoint.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Biometric payment request with QR code token",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BiometricPaymentRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
                                      "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
                                      "amount": 100.50,
                                      "currency": "EUR",
                                      "biometricData": {
                                        "type": "QR_CODE",
                                        "qrToken": "qr-token-from-scanned-qr-code",
                                        "deviceId": "device-123",
                                        "sessionId": "session-456"
                                      },
                                      "reference": "Payment ref 123",
                                      "description": "Biometric payment"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Biometric payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment request or QR code token"),
            @ApiResponse(responseCode = "401", description = "QR code verification failed"),
            @ApiResponse(responseCode = "403", description = "Fraud detected"),
            @ApiResponse(responseCode = "404", description = "Account not found or QR code expired"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentResponse> initiateBiometricPayment(
            @Valid @RequestBody BiometricPaymentRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        PaymentResponse response = paymentService.initiateBiometricPayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/qrcode/generate")
    @Operation(
            summary = "Generate QR code for payment",
            description = "Generates a QR code for a payment that can be scanned to complete the payment",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment request details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
                                      "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
                                      "amount": 100.50,
                                      "currency": "EUR",
                                      "paymentType": "QR_CODE",
                                      "reference": "Payment ref 123",
                                      "description": "QR code payment"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "QR code generated successfully",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment request"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> generateQRCode(
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        String qrCodeBase64 = paymentService.generateQRCodeForPayment(request, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("qrCode", qrCodeBase64);
        response.put("message", "QR code generated successfully");
        response.put("format", "PNG (base64)");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/qrcode")
    @Operation(
            summary = "Initiate a QR code payment",
            description = "Creates and processes a payment authenticated with a scanned QR code. The QR code must be verified before processing the payment.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "QR code payment request with scanned QR code data",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = QRCodePaymentRequest.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
                                      "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
                                      "amount": 100.50,
                                      "currency": "EUR",
                                      "qrCodeData": "{\\"paymentId\\":\\"...\\",\\"userId\\":\\"...\\",\\"amount\\":\\"100.50\\",\\"currency\\":\\"EUR\\",\\"timestamp\\":\\"...\\",\\"hash\\":\\"...\\"}",
                                      "reference": "Payment ref 123",
                                      "description": "QR code payment"
                                    }
                                    """)
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "QR code payment initiated successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment request or QR code data"),
            @ApiResponse(responseCode = "401", description = "QR code verification failed"),
            @ApiResponse(responseCode = "403", description = "Fraud detected"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaymentResponse> initiateQRCodePayment(
            @Valid @RequestBody QRCodePaymentRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        PaymentResponse response = paymentService.initiateQRCodePayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Note: Enrollment endpoint removed - QR code payments don't require pre-enrollment

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResponse> getPayment(
            @Parameter(description = "Payment ID", required = true, example = "1")
            @PathVariable Long id) {
        PaymentResponse response = paymentService.getPayment(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List payments", description = "Retrieves a paginated list of payments with optional filters by account ID and status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PaymentListResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<PaymentListResponse> getPayments(
            @Parameter(description = "Account ID to filter by", example = "1")
            @RequestParam(required = false) Long accountId,
            @Parameter(description = "Payment status filter", example = "COMPLETED")
            @RequestParam(required = false) PaymentStatus status,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "DESC")
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
    @Operation(summary = "Cancel a payment", description = "Cancels a payment that is in PENDING or PROCESSING status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "409", description = "Payment cannot be cancelled in current status")
    })
    public ResponseEntity<PaymentResponse> cancelPayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable Long id) {
        PaymentResponse response = paymentService.cancelPayment(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reverse")
    @Operation(summary = "Reverse a payment", description = "Reverses a completed payment. The account will be credited with the reversed amount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment reversed successfully"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "409", description = "Payment cannot be reversed in current status")
    })
    public ResponseEntity<PaymentResponse> reversePayment(
            @Parameter(description = "Payment ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Reason for reversal", required = true, example = "CUSTOMER_REQUEST")
            @RequestParam ReversalReason reason) {
        PaymentResponse response = paymentService.reversePayment(id, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Extracts user ID from authentication token
     * 
     * SECURITY: In production (Keycloak enabled), this method MUST throw exception
     * if authentication is invalid. Never use default userId in production.
     * 
     * @param authentication Spring Security authentication object
     * @return userId as Long
     * @throws org.springframework.security.access.AccessDeniedException if auth invalid in production
     */
    private Long extractUserId(Authentication authentication) {
        // Check if authentication is missing or invalid
        if (authentication == null || authentication.getName() == null) {
            // PRODUCTION MODE: Reject request with 401 Unauthorized
            // Check if Keycloak is enabled via environment variable
            String keycloakEnabled = System.getenv("KEYCLOAK_ENABLED");
            if ("true".equalsIgnoreCase(keycloakEnabled)) {
                log.error("Authentication required but not provided (Keycloak enabled)");
                throw new org.springframework.security.access.AccessDeniedException(
                    "Authentication required. Please provide valid Bearer token."
                );
            }
            
            // DEVELOPMENT MODE ONLY: Use fixed userId for testing
            Long devUserId = 1L;
            log.warn("[DEV MODE] No authentication provided, using fixed dev userId: {}", devUserId);
            log.warn("[DEV MODE] This behavior is DISABLED in production (KEYCLOAK_ENABLED=true)");
            return devUserId;
        }
        
        // Extract userId from authentication.getName() (Keycloak subject claim)
        try {
            String userIdString = authentication.getName();
            Long userId = Long.parseLong(userIdString);
            log.debug("Extracted userId from authentication: {}", userId);
            return userId;
        } catch (NumberFormatException e) {
            log.error("Invalid userId format in authentication token: {}", authentication.getName(), e);
            
            // PRODUCTION MODE: Reject invalid token
            String keycloakEnabled = System.getenv("KEYCLOAK_ENABLED");
            if ("true".equalsIgnoreCase(keycloakEnabled)) {
                throw new org.springframework.security.access.AccessDeniedException(
                    "Invalid user ID format in authentication token"
                );
            }
            
            // DEVELOPMENT MODE: Fallback to dev user
            Long devUserId = 1L;
            log.warn("[DEV MODE] Failed to parse userId, using fixed dev userId: {}", devUserId);
            return devUserId;
        }
    }
}

