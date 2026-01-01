package com.banking.account.dto;

import com.banking.account.model.Account;
import com.banking.account.model.Transaction.TransactionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// =========================================================================
// CREATE ACCOUNT REQUEST
// =========================================================================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a valid 3-letter ISO code (e.g., EUR, USD)")
    @Builder.Default
    private String currency = "EUR";

    @DecimalMin(value = "0.00", inclusive = true, message = "Initial balance must be zero or positive")
    @Digits(integer = 15, fraction = 2, message = "Initial balance precision exceeded")
    @Builder.Default
    private BigDecimal initialBalance = BigDecimal.ZERO;
}