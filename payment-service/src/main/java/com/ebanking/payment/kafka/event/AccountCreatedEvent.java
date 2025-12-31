package com.ebanking.payment.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountCreatedEvent {

    @JsonProperty("accountId")
    private UUID accountId;

    @JsonProperty("userId")
    private UUID userId;

    @JsonProperty("accountNumber")
    private String accountNumber;

    @JsonProperty("accountType")
    private String accountType;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("status")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}

