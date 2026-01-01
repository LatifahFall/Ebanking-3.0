// =============================================================================
// Base Event Class (Optional - for common fields)
// =============================================================================
package com.banking.account.kafka.event;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public abstract class BaseEvent {
    private UUID eventId = UUID.randomUUID();
    private Instant timestamp = Instant.now();
    private String eventType;
    private String source = "account-service";
    private String version = "1.0";
}