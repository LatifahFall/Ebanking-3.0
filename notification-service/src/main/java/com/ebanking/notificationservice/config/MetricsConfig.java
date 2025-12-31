package com.ebanking.notificationservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration des métriques Prometheus personnalisées
 * Pour le monitoring et l'observabilité dans Kubernetes
 */
@Configuration
public class MetricsConfig {

    /**
     * Compteur pour les notifications envoyées par type
     */
    @Bean
    public Counter emailSentCounter(MeterRegistry registry) {
        return Counter.builder("notification.sent")
                .tag("type", "email")
                .description("Total number of emails sent")
                .register(registry);
    }

    @Bean
    public Counter smsSentCounter(MeterRegistry registry) {
        return Counter.builder("notification.sent")
                .tag("type", "sms")
                .description("Total number of SMS sent")
                .register(registry);
    }

    @Bean
    public Counter pushSentCounter(MeterRegistry registry) {
        return Counter.builder("notification.sent")
                .tag("type", "push")
                .description("Total number of push notifications sent")
                .register(registry);
    }

    @Bean
    public Counter inAppSentCounter(MeterRegistry registry) {
        return Counter.builder("notification.sent")
                .tag("type", "in_app")
                .description("Total number of in-app notifications sent")
                .register(registry);
    }

    /**
     * Compteur pour les échecs de notification par type
     */
    @Bean
    public Counter emailFailedCounter(MeterRegistry registry) {
        return Counter.builder("notification.failed")
                .tag("type", "email")
                .description("Total number of failed emails")
                .register(registry);
    }

    @Bean
    public Counter smsFailedCounter(MeterRegistry registry) {
        return Counter.builder("notification.failed")
                .tag("type", "sms")
                .description("Total number of failed SMS")
                .register(registry);
    }

    /**
     * Timer pour mesurer la durée d'envoi des notifications
     */
    @Bean
    public Timer notificationTimer(MeterRegistry registry) {
        return Timer.builder("notification.duration")
                .description("Time taken to send notifications")
                .register(registry);
    }

    /**
     * Compteur pour les événements Kafka consommés
     */
    @Bean
    public Counter kafkaEventsCounter(MeterRegistry registry) {
        return Counter.builder("kafka.events.consumed")
                .tag("service", "notification")
                .description("Total number of Kafka events consumed")
                .register(registry);
    }
}
