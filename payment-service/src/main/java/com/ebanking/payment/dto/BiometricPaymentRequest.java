package com.ebanking.payment.dto;

import com.ebanking.payment.entity.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BiometricPaymentRequest {

    // Données du paiement standard
    @NotNull(message = "From account ID is required")
    private UUID fromAccountId;

    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String beneficiaryName;
    private String reference;
    private String description;

    // Données biométriques
    @NotNull(message = "Biometric data is required")
    @Valid
    private BiometricData biometricData;

    @Data
    public static class BiometricData {
        @NotNull(message = "Biometric type is required")
        private BiometricType type; // QR_CODE (remplace FACE)

        @NotBlank(message = "QR code token is required")
        private String qrToken; // Token du QR code scanné

        private String deviceId; // ID du dispositif utilisé
        private String sessionId; // ID de session pour traçabilité
    }

    public enum BiometricType {
        QR_CODE  // QR code authentication (replaces FACE)
    }

    // Méthode utilitaire pour convertir en PaymentRequest standard
    public PaymentRequest toPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccountId(this.fromAccountId);
        request.setToAccountId(this.toAccountId);
        request.setAmount(this.amount);
        request.setCurrency(this.currency);
        request.setPaymentType(PaymentType.BIOMETRIC);
        request.setBeneficiaryName(this.beneficiaryName);
        request.setReference(this.reference);
        request.setDescription(this.description);
        return request;
    }
}

