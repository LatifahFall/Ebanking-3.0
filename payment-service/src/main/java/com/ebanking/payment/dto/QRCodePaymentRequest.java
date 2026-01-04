package com.ebanking.payment.dto;

import com.ebanking.payment.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO pour les paiements avec authentification QR code
 */
@Data
public class QRCodePaymentRequest {

    // Données du paiement standard
    @NotNull(message = "From account ID is required")
    private Long fromAccountId;

    private Long toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    private String beneficiaryName;
    private String reference;
    private String description;

    // QR code scanné (contenu du QR code)
    @NotBlank(message = "QR code data is required")
    private String qrCodeData;

    // Méthode utilitaire pour convertir en PaymentRequest standard
    public PaymentRequest toPaymentRequest() {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccountId(this.fromAccountId);
        request.setToAccountId(this.toAccountId);
        request.setAmount(this.amount);
        request.setCurrency(this.currency);
        request.setPaymentType(PaymentType.QR_CODE);
        request.setBeneficiaryName(this.beneficiaryName);
        request.setReference(this.reference);
        request.setDescription(this.description);
        return request;
    }
}

