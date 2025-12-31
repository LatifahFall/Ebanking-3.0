package com.ebanking.notificationservice;

import com.ebanking.notificationservice.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Test de contexte Spring Boot
 * Vérifie que l'application démarre correctement sans Kafka
 */
@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=PLAINTEXT://localhost:9092",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
        // Vérifie que le contexte Spring démarre correctement
    }
}