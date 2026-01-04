package com.ebanking.payment.service;

import com.ebanking.payment.dto.BiometricPaymentRequest;
import com.ebanking.payment.dto.QRCodePaymentRequest;
import com.ebanking.payment.dto.PaymentRequest;
import com.ebanking.payment.dto.PaymentResponse;
import com.ebanking.payment.entity.Payment;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.entity.ReversalReason;
import com.ebanking.payment.exception.PaymentNotFoundException;
import com.ebanking.payment.exception.PaymentValidationException;
import com.ebanking.payment.kafka.PaymentEventProducer;
import com.ebanking.payment.kafka.event.FraudDetectedEvent;
import com.ebanking.payment.kafka.event.PaymentCompletedEvent;
import com.ebanking.payment.kafka.event.PaymentReversedEvent;
import com.ebanking.payment.repository.PaymentRepository;
import com.ebanking.payment.repository.QrCodePaymentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidationService validationService;
    private final PaymentRuleService ruleService;
    private final FraudDetectionService fraudDetectionService;
    private final PaymentProcessingService processingService;
    private final PaymentEventProducer eventProducer;
    private final BiometricVerificationService biometricVerificationService;
    private final QrCodeService qrCodeService;
    private final QrCodePaymentRepository qrCodePaymentRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request, Long userId) {
        // Validate payment request
        validationService.validatePaymentRequest(request)
                .block(); // Block for synchronous processing

        // Create payment entity
        Payment payment = Payment.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentType(request.getPaymentType())
                .status(PaymentStatus.PENDING)
                .beneficiaryName(request.getBeneficiaryName())
                .reference(request.getReference())
                .description(request.getDescription())
                .userId(userId)
                .build();

        payment = paymentRepository.save(payment);

        // Evaluate payment rules
        if (!ruleService.evaluatePayment(payment)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentValidationException("Payment failed rule validation");
        }

        // Fraud detection
        FraudDetectionService.FraudAnalysisResult fraudResult = fraudDetectionService.analyzeTransaction(payment);
        if (fraudResult.isFraud()) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);
            
            // Publish fraud event
            FraudDetectedEvent fraudEvent = FraudDetectedEvent.builder()
                    .fraudId(UUID.randomUUID())
                    .paymentId(payment.getId())
                    .accountId(payment.getFromAccountId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .fraudType(fraudResult.getFraudType().name())
                    .reason(fraudResult.getReason())
                    .detectedAt(LocalDateTime.now())
                    .action("BLOCKED")
                    .build();
            safePublishFraudDetected(fraudEvent);
            
            throw new PaymentValidationException("Fraud detected: " + fraudResult.getReason());
        }

        // Process payment based on type
        if (payment.getPaymentType() == PaymentType.INSTANT) {
            payment = processingService.processInstantPayment(payment.getId());
        } else if (payment.getPaymentType() == PaymentType.BIOMETRIC) {
            payment = processingService.processBiometricPayment(payment.getId());
        } else if (payment.getPaymentType() == PaymentType.QR_CODE) {
            payment = processingService.processQRCodePayment(payment.getId());
        } else {
            payment = processingService.processStandardPayment(payment.getId());
        }

        // Publish payment completed event
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionType(payment.getPaymentType().name())
                .status(payment.getStatus().name())
                .completedAt(payment.getCompletedAt())
                .build();
        safePublishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    /**
     * Génère un QR code pour un paiement biométrique
     * L'utilisateur doit scanner ce QR code avec son app mobile pour confirmer le paiement
     */
    @Transactional
    public Map<String, Object> generateBiometricPaymentQrCode(PaymentRequest request, Long userId) {
        // Validation standard
        validationService.validatePaymentRequest(request)
                .block(); // Block for synchronous processing

        // Créer le paiement en statut PENDING (attente de validation QR code)
        Payment payment = Payment.builder()
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentType(PaymentType.BIOMETRIC)
                .status(PaymentStatus.PENDING)
                .beneficiaryName(request.getBeneficiaryName())
                .reference(request.getReference())
                .description(request.getDescription())
                .userId(userId)
                .build();

        payment = paymentRepository.save(payment);

        // Évaluation des règles
        if (!ruleService.evaluatePayment(payment)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentValidationException("Payment failed rule validation");
        }

        // Détection de fraude
        FraudDetectionService.FraudAnalysisResult fraudResult = fraudDetectionService.analyzeTransaction(payment);
        if (fraudResult.isFraud()) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);

            FraudDetectedEvent fraudEvent = FraudDetectedEvent.builder()
                    .fraudId(UUID.randomUUID())
                    .paymentId(payment.getId())
                    .accountId(payment.getFromAccountId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .fraudType(fraudResult.getFraudType().name())
                    .reason(fraudResult.getReason())
                    .detectedAt(LocalDateTime.now())
                    .action("BLOCKED")
                    .build();
            safePublishFraudDetected(fraudEvent);

            throw new PaymentValidationException("Fraud detected: " + fraudResult.getReason());
        }

        // Générer le QR code (retourne maintenant l'image ET le JSON qrCodeData)
        QrCodeService.QrCodeGenerationResult qrResult = qrCodeService.generateQrCode(
                payment.getId(),
                userId,
                request.getAmount(),
                request.getCurrency(),
                request.getFromAccountId(),
                request.getToAccountId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", payment.getId());
        response.put("qrCode", qrResult.getQrCodeImage()); // Base64 PNG image
        response.put("qrCodeData", qrResult.getQrCodeData()); // JSON avec token, paymentId, etc.
        response.put("qrToken", qrResult.getQrToken()); // Token pour référence
        response.put("status", payment.getStatus().name());
        response.put("message", "Scan this QR code with your mobile app to confirm the payment");

        return response;
    }

    /**
     * Valide un QR code et traite le paiement biométrique
     */
    @Transactional
    public PaymentResponse initiateBiometricPayment(BiometricPaymentRequest request, Long userId) {
        // Vérifier les données biométriques (QR code token) - cela valide aussi le QR code
        biometricVerificationService.verifyBiometric(userId, request.getBiometricData());

        // Convertir en PaymentRequest standard
        PaymentRequest standardRequest = request.toPaymentRequest();

        // Récupérer le paiement associé au QR code
        // Note: validateQrCode a déjà été appelé dans verifyBiometric et a marqué le QR code comme utilisé
        var qrCodePaymentOpt = qrCodePaymentRepository.findByQrToken(
                request.getBiometricData().getQrToken()
        );
        
        if (qrCodePaymentOpt.isEmpty()) {
            throw new PaymentValidationException("QR code payment not found");
        }
        
        var qrCodePayment = qrCodePaymentOpt.get();

        Payment payment = paymentRepository.findById(qrCodePayment.getPaymentId())
                .orElseThrow(() -> new PaymentValidationException("Payment not found"));

        // Vérifier que le paiement correspond aux données de la requête
        if (!payment.getAmount().equals(standardRequest.getAmount()) ||
            !payment.getCurrency().equals(standardRequest.getCurrency()) ||
            !payment.getFromAccountId().equals(standardRequest.getFromAccountId())) {
            throw new PaymentValidationException("QR code payment data does not match request");
        }

        // Ajouter les données biométriques dans les métadonnées
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("biometricType", request.getBiometricData().getType().name());
        metadata.put("deviceId", request.getBiometricData().getDeviceId());
        metadata.put("sessionId", request.getBiometricData().getSessionId());
        metadata.put("verifiedAt", LocalDateTime.now().toString());
        metadata.put("qrToken", request.getBiometricData().getQrToken());

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);
            payment.setMetadata(metadataJson);
            paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("Error serializing biometric metadata", e);
        }

        // Traitement du paiement biométrique (traitement instantané)
        payment = processingService.processBiometricPayment(payment.getId());

        // Publier l'événement
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionType(payment.getPaymentType().name())
                .status(payment.getStatus().name())
                .completedAt(payment.getCompletedAt())
                .build();
        safePublishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    /**
     * Génère un QR code pour un paiement
     */
    @Transactional
    public Map<String, Object> generateQRCodeForPayment(PaymentRequest request, Long userId) {
        try {
            log.debug("Generating QR code for payment request: fromAccountId={}, toAccountId={}, amount={}", 
                    request.getFromAccountId(), request.getToAccountId(), request.getAmount());
            
            // Validation standard
            validationService.validatePaymentRequest(request)
                    .block(); // Block for synchronous processing
            
            // Créer un paiement temporaire pour générer le QR code
            Payment payment = Payment.builder()
                    .fromAccountId(request.getFromAccountId())
                    .toAccountId(request.getToAccountId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .paymentType(PaymentType.QR_CODE)
                    .status(PaymentStatus.PENDING)
                    .beneficiaryName(request.getBeneficiaryName())
                    .reference(request.getReference())
                    .description(request.getDescription())
                    .userId(userId)
                    .build();

            log.debug("Saving payment entity...");
            payment = paymentRepository.save(payment);
            log.debug("Payment saved with ID: {}", payment.getId());

            // Générer le QR code (retourne maintenant l'image ET le JSON qrCodeData)
            log.debug("Generating QR code image...");
            QrCodeService.QrCodeGenerationResult qrResult = qrCodeService.generateQrCode(
                    payment.getId(),
                    userId,
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getFromAccountId(),
                    payment.getToAccountId()
            );

            log.info("QR code generated successfully for payment: {}", payment.getId());
            
            // Retourner un Map avec toutes les informations nécessaires
            Map<String, Object> result = new HashMap<>();
            result.put("paymentId", payment.getId());
            result.put("qrCode", qrResult.getQrCodeImage());  // Image PNG base64
            result.put("qrCodeData", qrResult.getQrCodeData());  // JSON avec token, paymentId, etc.
            result.put("qrToken", qrResult.getQrToken());  // Token pour référence
            return result;
        } catch (Exception e) {
            log.error("Error generating QR code for payment: fromAccountId={}, toAccountId={}, amount={}", 
                    request.getFromAccountId(), request.getToAccountId(), request.getAmount(), e);
            throw e; // Re-throw pour que le GlobalExceptionHandler puisse le gérer
        }
    }

    @Transactional
    public PaymentResponse initiateQRCodePayment(QRCodePaymentRequest request, Long userId) {
        // SECURITY: Validate QR code data format BEFORE processing
        validateQrCodeDataFormat(request.getQrCodeData());
        
        // Convertir en PaymentRequest standard pour validation
        PaymentRequest standardRequest = request.toPaymentRequest();

        // Variable pour stocker le paiement récupéré
        Payment payment;
        
        // Vérifier le QR code - extraire le token et le paymentId du JSON
        try {
            // Parse QR code JSON safely
            Map<String, Object> qrData = parseQrCodeData(request.getQrCodeData());
            
            // Validate required fields
            validateQrDataFields(qrData);
            
            // Extract token from parsed Map (more reliable than string parsing)
            String qrToken = (String) qrData.get("token");
            if (qrToken == null || qrToken.trim().isEmpty()) {
                throw new PaymentValidationException("Invalid QR code format: token not found in JSON");
            }
            
            // Valider le QR code (vérifie expiration, usage, user) - cela retourne le qrCodePayment avec le paymentId
            com.ebanking.payment.entity.QrCodePayment qrCodePayment = qrCodeService.validateQrCode(qrToken, userId);
            
            // Utiliser le paymentId du qrCodePayment (source de vérité dans la base de données)
            Long paymentId = qrCodePayment.getPaymentId();
            
            // Récupérer le paiement EXISTANT associé au QR code (ne pas en créer un nouveau)
            payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new PaymentValidationException("Payment not found for QR code"));
            
            // Vérification optionnelle : s'assurer que le paymentId dans le qrCodeData correspond (pour sécurité)
            String paymentIdStr = (String) qrData.get("paymentId");
            if (paymentIdStr != null && !paymentIdStr.trim().isEmpty()) {
                try {
                    Long paymentIdFromQr = Long.parseLong(paymentIdStr);
                    if (!paymentIdFromQr.equals(paymentId)) {
                        log.warn("Payment ID mismatch in QR code data: qrCodeData={}, qrCodePayment={}. Using paymentId from database.", 
                                paymentIdFromQr, paymentId);
                        // On continue quand même car le paymentId de la base de données est la source de vérité
                    }
                } catch (NumberFormatException e) {
                    log.warn("Invalid paymentId format in QR code data: {}", paymentIdStr);
                    // On continue car on utilise le paymentId de la base de données
                }
            }
            
            // Vérifier que le paiement correspond aux données de la requête
            // Utiliser compareTo pour BigDecimal (plus fiable que equals)
            if (payment.getAmount().compareTo(standardRequest.getAmount()) != 0 ||
                !payment.getCurrency().equals(standardRequest.getCurrency()) ||
                !payment.getFromAccountId().equals(standardRequest.getFromAccountId())) {
                log.warn("QR code payment data mismatch: payment={}, request={}", 
                        payment.getAmount(), standardRequest.getAmount());
                throw new PaymentValidationException("QR code payment data does not match request");
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("Invalid QR code JSON format", e);
            throw new PaymentValidationException("Invalid QR code data format: malformed JSON");
        } catch (PaymentValidationException e) {
            // Already logged, just rethrow
            throw e;
        } catch (Exception e) {
            log.error("QR code verification failed", e);
            throw new PaymentValidationException("QR code verification failed: " + e.getMessage());
        }

        // Ajouter les métadonnées QR code
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("qrCodeVerified", true);
        metadata.put("verifiedAt", LocalDateTime.now().toString());

        String metadataJson;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.error("Error serializing QR code metadata", e);
            metadataJson = "{}";
        }
        payment.setMetadata(metadataJson);

        // Évaluation des règles
        if (!ruleService.evaluatePayment(payment)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new PaymentValidationException("Payment failed rule validation");
        }

        // Détection de fraude
        FraudDetectionService.FraudAnalysisResult fraudResult = fraudDetectionService.analyzeTransaction(payment);
        if (fraudResult.isFraud()) {
            payment.setStatus(PaymentStatus.CANCELLED);
            paymentRepository.save(payment);

            FraudDetectedEvent fraudEvent = FraudDetectedEvent.builder()
                    .fraudId(UUID.randomUUID())
                    .paymentId(payment.getId())
                    .accountId(payment.getFromAccountId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .fraudType(fraudResult.getFraudType().name())
                    .reason(fraudResult.getReason())
                    .detectedAt(LocalDateTime.now())
                    .action("BLOCKED")
                    .build();
            safePublishFraudDetected(fraudEvent);

            throw new PaymentValidationException("Fraud detected: " + fraudResult.getReason());
        }

        // Traitement du paiement QR code
        payment = processingService.processQRCodePayment(payment.getId());

        // Publier l'événement
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionType(payment.getPaymentType().name())
                .status(payment.getStatus().name())
                .completedAt(payment.getCompletedAt())
                .build();
        safePublishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment cannot be processed. Status: " + payment.getStatus());
        }

        payment = processingService.processStandardPayment(paymentId);

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionType(payment.getPaymentType().name())
                .status(payment.getStatus().name())
                .completedAt(payment.getCompletedAt())
                .build();
        safePublishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed payment");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse reversePayment(Long paymentId, ReversalReason reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Can only reverse completed payments");
        }

        payment.setStatus(PaymentStatus.REVERSED);
        payment.setReversedAt(LocalDateTime.now());
        payment.setReversalReason(reason.name());
        payment = paymentRepository.save(payment);

        PaymentReversedEvent event = PaymentReversedEvent.builder()
                .paymentId(payment.getId())
                .accountId(payment.getFromAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .reversalReason(reason.name())
                .originalPaymentDate(payment.getCompletedAt())
                .reversedAt(payment.getReversedAt())
                .build();
        safePublishPaymentReversed(event);

        return mapToResponse(payment);
    }

    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByAccount(Long accountId, PaymentStatus status, Pageable pageable) {
        List<Payment> payments;
        if (status != null) {
            payments = paymentRepository.findByFromAccountIdAndStatus(accountId, status);
        } else {
            payments = paymentRepository.findByFromAccountId(accountId);
        }
        
        return payments.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .fromAccountId(payment.getFromAccountId())
                .toAccountId(payment.getToAccountId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentType(payment.getPaymentType())
                .status(payment.getStatus())
                .beneficiaryName(payment.getBeneficiaryName())
                .reference(payment.getReference())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .completedAt(payment.getCompletedAt())
                .reversedAt(payment.getReversedAt())
                .reversalReason(payment.getReversalReason())
                .userId(payment.getUserId())
                .build();
    }

    /**
     * SECURITY: Validates QR code data format before parsing
     * Prevents JSON injection and malformed data attacks
     */
    private void validateQrCodeDataFormat(String qrCodeData) {
        if (qrCodeData == null || qrCodeData.trim().isEmpty()) {
            throw new PaymentValidationException("QR code data cannot be null or empty");
        }
        
        // Check length limits (prevent DoS via large payloads)
        if (qrCodeData.length() > 10000) {
            throw new PaymentValidationException("QR code data exceeds maximum allowed size");
        }
        
        // Basic JSON structure validation
        qrCodeData = qrCodeData.trim();
        if (!qrCodeData.startsWith("{") || !qrCodeData.endsWith("}")) {
            throw new PaymentValidationException("QR code data must be valid JSON object");
        }
        
        // Check for required fields presence (basic validation)
        if (!qrCodeData.contains("\"token\"") || !qrCodeData.contains("\"paymentId\"")) {
            throw new PaymentValidationException("QR code data missing required fields (token, paymentId)");
        }
        
        log.debug("QR code data format validation passed");
    }
    
    /**
     * Parses QR code JSON data using Jackson ObjectMapper for secure parsing
     */
    private Map<String, Object> parseQrCodeData(String qrCodeData) throws com.fasterxml.jackson.core.JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        
        // Configure mapper for security
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
        
        // Parse JSON to Map
        @SuppressWarnings("unchecked")
        Map<String, Object> qrData = mapper.readValue(qrCodeData, Map.class);
        
        return qrData;
    }
    
    /**
     * Validates required fields in parsed QR data
     */
    private void validateQrDataFields(Map<String, Object> qrData) {
        // Validate token field
        if (!qrData.containsKey("token") || !(qrData.get("token") instanceof String)) {
            throw new PaymentValidationException("QR code data must contain valid 'token' field");
        }
        
        String token = (String) qrData.get("token");
        if (token.trim().isEmpty()) {
            throw new PaymentValidationException("QR code token cannot be empty");
        }
        
        // Validate token format (should be UUID-like or secure random string)
        if (token.length() < 32 || token.length() > 256) {
            throw new PaymentValidationException("QR code token has invalid length");
        }
        
        // Validate paymentId field
        if (!qrData.containsKey("paymentId")) {
            throw new PaymentValidationException("QR code data must contain 'paymentId' field");
        }
        
        // PaymentId can be string or number
        Object paymentId = qrData.get("paymentId");
        if (paymentId == null) {
            throw new PaymentValidationException("QR code paymentId cannot be null");
        }
        
        log.debug("QR code data fields validation passed");
    }
    
    /**
     * KAFKA ERROR HANDLING: Safely publishes events with error handling
     * Prevents silent failures and logs all Kafka errors
     */
    private void safePublishPaymentCompleted(PaymentCompletedEvent event) {
        try {
            eventProducer.publishPaymentCompleted(event);
            log.info("Successfully published PaymentCompletedEvent for payment {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("CRITICAL: Failed to publish PaymentCompletedEvent for payment {}. Event data: {}", 
                    event.getPaymentId(), event, e);
            // TODO: Implement outbox pattern for retry - store in outbox table for async retry
            // For now, log the failure but don't block the payment transaction
        }
    }
    
    private void safePublishFraudDetected(FraudDetectedEvent event) {
        try {
            eventProducer.publishFraudDetected(event);
            log.warn("Successfully published FraudDetectedEvent for payment {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("CRITICAL: Failed to publish FraudDetectedEvent for payment {}. Fraud type: {}, Event data: {}", 
                    event.getPaymentId(), event.getFraudType(), event, e);
            // TODO: Implement outbox pattern - fraud events MUST be delivered for security monitoring
        }
    }
    
    private void safePublishPaymentReversed(PaymentReversedEvent event) {
        try {
            eventProducer.publishPaymentReversed(event);
            log.info("Successfully published PaymentReversedEvent for payment {}", event.getPaymentId());
        } catch (Exception e) {
            log.error("CRITICAL: Failed to publish PaymentReversedEvent for payment {}. Event data: {}", 
                    event.getPaymentId(), event, e);
            // TODO: Implement outbox pattern for retry
        }
    }

    /**
     * Extrait le token du JSON du QR code
     */
    private String extractTokenFromQRData(String qrCodeData) {
        // Simple JSON parsing pour extraire le token
        // Format attendu: {"token":"...","paymentId":"...",...}
        if (qrCodeData.contains("\"token\"")) {
            int tokenStart = qrCodeData.indexOf("\"token\"") + 8;
            int tokenEnd = qrCodeData.indexOf("\"", tokenStart);
            if (tokenEnd > tokenStart) {
                return qrCodeData.substring(tokenStart, tokenEnd);
            }
        }
        throw new PaymentValidationException("Invalid QR code format: token not found");
    }
}



