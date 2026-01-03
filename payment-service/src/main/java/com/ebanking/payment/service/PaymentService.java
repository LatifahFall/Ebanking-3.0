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
    public PaymentResponse initiatePayment(PaymentRequest request, UUID userId) {
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
            eventProducer.publishFraudDetected(fraudEvent);
            
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
        eventProducer.publishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    /**
     * Génère un QR code pour un paiement biométrique
     * L'utilisateur doit scanner ce QR code avec son app mobile pour confirmer le paiement
     */
    @Transactional
    public Map<String, Object> generateBiometricPaymentQrCode(PaymentRequest request, UUID userId) {
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
            eventProducer.publishFraudDetected(fraudEvent);

            throw new PaymentValidationException("Fraud detected: " + fraudResult.getReason());
        }

        // Générer le QR code
        String qrCodeImage = qrCodeService.generateQrCode(
                payment.getId(),
                userId,
                request.getAmount(),
                request.getCurrency(),
                request.getFromAccountId(),
                request.getToAccountId()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", payment.getId());
        response.put("qrCode", qrCodeImage); // Base64 PNG image
        response.put("status", payment.getStatus().name());
        response.put("message", "Scan this QR code with your mobile app to confirm the payment");

        return response;
    }

    /**
     * Valide un QR code et traite le paiement biométrique
     */
    @Transactional
    public PaymentResponse initiateBiometricPayment(BiometricPaymentRequest request, UUID userId) {
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
        eventProducer.publishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    /**
     * Génère un QR code pour un paiement
     */
    public String generateQRCodeForPayment(PaymentRequest request, UUID userId) {
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

        payment = paymentRepository.save(payment);

        // Générer le QR code
        String qrCodeBase64 = qrCodeService.generateQrCode(
                payment.getId(),
                userId,
                payment.getAmount(),
                payment.getCurrency(),
                payment.getFromAccountId(),
                payment.getToAccountId()
        );

        log.info("QR code generated for payment: {}", payment.getId());
        return qrCodeBase64;
    }

    @Transactional
    public PaymentResponse initiateQRCodePayment(QRCodePaymentRequest request, UUID userId) {
        // Convertir en PaymentRequest standard
        PaymentRequest standardRequest = request.toPaymentRequest();

        // Validation standard
        validationService.validatePaymentRequest(standardRequest)
                .block();

        // Créer le paiement
        Payment payment = Payment.builder()
                .fromAccountId(standardRequest.getFromAccountId())
                .toAccountId(standardRequest.getToAccountId())
                .amount(standardRequest.getAmount())
                .currency(standardRequest.getCurrency())
                .paymentType(PaymentType.QR_CODE)
                .status(PaymentStatus.PENDING)
                .beneficiaryName(standardRequest.getBeneficiaryName())
                .reference(standardRequest.getReference())
                .description(standardRequest.getDescription())
                .userId(userId)
                .build();

        payment = paymentRepository.save(payment);

        // Vérifier le QR code - extraire le token du JSON
        try {
            // Le QR code contient un JSON avec un token
            String qrToken = extractTokenFromQRData(request.getQrCodeData());
            com.ebanking.payment.entity.QrCodePayment qrCodePayment = qrCodeService.validateQrCode(qrToken, userId);
            
            // Vérifier que le payment ID correspond
            if (!qrCodePayment.getPaymentId().equals(payment.getId())) {
                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);
                throw new PaymentValidationException("QR code payment ID mismatch");
            }
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
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
            eventProducer.publishFraudDetected(fraudEvent);

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
        eventProducer.publishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse processPayment(UUID paymentId) {
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
        eventProducer.publishPaymentCompleted(event);

        return mapToResponse(payment);
    }

    @Transactional
    public PaymentResponse cancelPayment(UUID paymentId) {
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
    public PaymentResponse reversePayment(UUID paymentId, ReversalReason reason) {
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
        eventProducer.publishPaymentReversed(event);

        return mapToResponse(payment);
    }

    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByAccount(UUID accountId, PaymentStatus status, Pageable pageable) {
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

