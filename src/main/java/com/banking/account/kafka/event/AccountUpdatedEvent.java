package com.banking.account.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdatedEvent {
    private Long accountId;
    private String previousStatus; // ⭐ AJOUTÉ
    private String newStatus; // ⭐ RENOMMÉ depuis status
    private String updateReason; // ⭐ AJOUTÉ
    private Instant updatedAt; // ⭐ Instant au lieu de LocalDateTime
}