package com.banking.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// =========================================================================
// SUSPEND ACCOUNT REQUEST
// =========================================================================

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuspendAccountRequest {

    @NotBlank(message = "Suspension reason is required")
    private String reason;

    @NotBlank(message = "Suspended by (user/admin) is required")
    private String suspendedBy;

    private String notes;
}