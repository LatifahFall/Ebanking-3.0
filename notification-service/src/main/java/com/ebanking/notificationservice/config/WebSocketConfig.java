package com.ebanking.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration WebSocket pour les notifications Push en temps réel
 *
 * AVANTAGES :
 * - 100% GRATUIT
 * - Temps réel instantané
 * - Pas de service externe
 * - Simple à implémenter
 *
 * UTILISATION :
 * - Frontend se connecte à ws://localhost:8084/ws
 * - S'abonne au topic /user/queue/notifications
 * - Reçoit les notifications en temps réel
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active le broker de messages pour /topic (broadcast) et /queue (user-specific)
        config.enableSimpleBroker("/topic", "/queue");

        // Préfixe pour les messages envoyés depuis le client
        config.setApplicationDestinationPrefixes("/app");

        // Préfixe pour les messages user-specific
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket accessible depuis le frontend
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200") // Angular frontend
                .withSockJS(); // Fallback pour navigateurs sans WebSocket
    }
}