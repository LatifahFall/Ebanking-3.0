package com.bank.authservice.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Logical event consumer.
 * Kafka listeners will be added later.
 */
@Component
@Slf4j
public class AuthEventConsumer {

    public void onUserCreated(String userId) {
        log.info("[EVENT RECEIVED] User created: {}", userId);
    }

    public void onUserUpdated(String userId) {
        log.info("[EVENT RECEIVED] User updated: {}", userId);
    }
}
