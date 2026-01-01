package com.banking.account.dto;// =========================================================================
// CLOSE ACCOUNT REQUEST
// =========================================================================

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloseAccountRequest {

    @NotBlank(message = "Closure reason is required")
    private String closureReason;

    @NotBlank(message = "Closed by (user/admin) is required")
    private String closedBy;

    private String notes;
}