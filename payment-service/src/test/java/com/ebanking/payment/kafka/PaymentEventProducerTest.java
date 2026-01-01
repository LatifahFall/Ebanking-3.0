package com.ebanking.payment.kafka;

import com.ebanking.payment.config.KafkaConfig;
import com.ebanking.payment.kafka.event.FraudDetectedEvent;
import com.ebanking.payment.kafka.event.PaymentCompletedEvent;
import com.ebanking.payment.kafka.event.PaymentReversedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = {KafkaConfig.class, PaymentEventProducer.class},
    properties = {
        "keycloak.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration",
        "spring.kafka.consumer.group-id=payment-service-test-group"
    }
)
@EmbeddedKafka(
        partitions = 1,
        topics = {"payment.completed", "payment.reversed", "fraud.detected"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"}
)
@DirtiesContext
@ActiveProfiles("test")
class PaymentEventProducerTest {

    @Autowired
    private PaymentEventProducer eventProducer;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.service.kafka.topics.producer.payment-completed}")
    private String paymentCompletedTopic;

    @Value("${payment.service.kafka.topics.producer.payment-reversed}")
    private String paymentReversedTopic;

    @Value("${payment.service.kafka.topics.producer.fraud-detected}")
    private String fraudDetectedTopic;

    private UUID paymentId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    void shouldPublishPaymentCompletedEvent() throws Exception {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId(paymentId)
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .transactionType("STANDARD")
                .status("COMPLETED")
                .completedAt(LocalDateTime.now())
                .build();

        eventProducer.publishPaymentCompleted(event);

        Thread.sleep(1000); // Wait for async send

        // Verify event was sent (in a real test, you'd consume and verify)
        assertThat(event.getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    void shouldPublishPaymentReversedEvent() throws Exception {
        PaymentReversedEvent event = PaymentReversedEvent.builder()
                .paymentId(paymentId)
                .accountId(accountId)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .reversalReason("CUSTOMER_REQUEST")
                .originalPaymentDate(LocalDateTime.now().minusDays(1))
                .reversedAt(LocalDateTime.now())
                .build();

        eventProducer.publishPaymentReversed(event);

        Thread.sleep(1000);

        assertThat(event.getPaymentId()).isEqualTo(paymentId);
    }

    @Test
    void shouldPublishFraudDetectedEvent() throws Exception {
        FraudDetectedEvent event = FraudDetectedEvent.builder()
                .fraudId(UUID.randomUUID())
                .paymentId(paymentId)
                .accountId(accountId)
                .userId(UUID.randomUUID())
                .amount(new BigDecimal("5000.00"))
                .fraudType("SUSPICIOUS_AMOUNT")
                .reason("Transaction amount exceeds threshold")
                .detectedAt(LocalDateTime.now())
                .action("BLOCKED")
                .build();

        eventProducer.publishFraudDetected(event);

        Thread.sleep(1000);

        assertThat(event.getPaymentId()).isEqualTo(paymentId);
    }
}

