
// KycStatusChangedEvent.java
package com.banking.account.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycStatusChangedEvent {
    private UUID kycId;
    private UUID userId;
    private String previousStatus;
    private String newStatus; // APPROVED, REJECTED, PENDING
    private String rejectionReason;
    private Instant changedAt;
}