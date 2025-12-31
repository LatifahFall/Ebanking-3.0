package com.ebanking.payment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRuleRequest {

    @NotBlank(message = "Rule type is required")
    private String ruleType;

    @NotBlank(message = "Rule name is required")
    private String ruleName;

    private String description;

    @NotBlank(message = "Conditions are required")
    private String conditions;

    private Boolean enabled;

    @Min(value = 0, message = "Priority must be >= 0")
    private Integer priority;
}

