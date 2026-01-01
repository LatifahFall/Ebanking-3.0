// PaymentReversedEvent - CORRIGÉ
package com.banking.account.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PaymentReversedEvent extends BaseEvent {

    private UUID paymentId;
    private Long accountId;
    private BigDecimal amount;
    private String currency;

    private String reversalReason; // FRAUD | CUSTOMER_REQUEST | TECHNICAL_ERROR | ...
    private Instant originalPaymentDate;
    private Instant reversedAt;

    private Map<String, String> metadata;

    @Builder.Default
    private String eventType = "PAYMENT_REVERSED";
}