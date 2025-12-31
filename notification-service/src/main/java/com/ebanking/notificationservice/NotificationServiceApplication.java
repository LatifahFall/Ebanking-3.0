package com.ebanking.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Application principale du Notification Service
 *
 * ANNOTATIONS :
 * - @EnableKafka : Active les consumers Kafka
 * - @EnableRetry : Active les retries automatiques (@Retryable)
 * - @EnableAsync : Active l'exécution asynchrone (@Async)
 */
@SpringBootApplication
@EnableKafka
@EnableRetry
@EnableAsync
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}