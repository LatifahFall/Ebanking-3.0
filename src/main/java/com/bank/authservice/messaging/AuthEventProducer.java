package com.bank.authservice.messaging;

import com.bank.authservice.messaging.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Event producer abstraction.
 * Kafka / RabbitMQ will be plugged here later.
 */
@Component
@Slf4j
public class AuthEventProducer {

    public void publishLoginSuccess(UserLoginSuccessEvent event) {
        log.info("[EVENT] Login success: {}", event);
    }

    public void publishLoginFailure(UserLoginFailureEvent event) {
        log.warn("[EVENT] Login failure: {}", event);
    }

    public void publishLogout(UserLogoutEvent event) {
        log.info("[EVENT] Logout: {}", event);
    }

    public void publishTokenRefreshed(TokenRefreshedEvent event) {
        log.info("[EVENT] Token refreshed: {}", event);
    }
}
