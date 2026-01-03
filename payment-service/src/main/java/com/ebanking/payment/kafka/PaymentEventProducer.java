package com.ebanking.payment.kafka;

import com.ebanking.payment.kafka.event.FraudDetectedEvent;
import com.ebanking.payment.kafka.event.PaymentCompletedEvent;
import com.ebanking.payment.kafka.event.PaymentReversedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class PaymentEventProducer {

    private final Optional<KafkaTemplate<String, Object>> kafkaTemplate;

    @Autowired(required = false)
    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = Optional.ofNullable(kafkaTemplate);
    }

    @Value("${payment.service.kafka.topics.producer.payment-completed}")
    private String paymentCompletedTopic;

    @Value("${payment.service.kafka.topics.producer.payment-reversed}")
    private String paymentReversedTopic;

    @Value("${payment.service.kafka.topics.producer.fraud-detected}")
    private String fraudDetectedTopic;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        if (kafkaTemplate.isPresent()) {
            kafkaTemplate.get().send(paymentCompletedTopic, event.getPaymentId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Published payment.completed event for payment: {}", event.getPaymentId());
                        } else {
                            log.error("Failed to publish payment.completed event", ex);
                        }
                    });
        } else {
            log.debug("Kafka not enabled, skipping payment.completed event for payment: {}", event.getPaymentId());
        }
    }

    public void publishPaymentReversed(PaymentReversedEvent event) {
        if (kafkaTemplate.isPresent()) {
            kafkaTemplate.get().send(paymentReversedTopic, event.getPaymentId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Published payment.reversed event for payment: {}", event.getPaymentId());
                        } else {
                            log.error("Failed to publish payment.reversed event", ex);
                        }
                    });
        } else {
            log.debug("Kafka not enabled, skipping payment.reversed event for payment: {}", event.getPaymentId());
        }
    }

    public void publishFraudDetected(FraudDetectedEvent event) {
        if (kafkaTemplate.isPresent()) {
            kafkaTemplate.get().send(fraudDetectedTopic, event.getPaymentId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Published fraud.detected event for payment: {}", event.getPaymentId());
                        } else {
                            log.error("Failed to publish fraud.detected event", ex);
                        }
                    });
        } else {
            log.debug("Kafka not enabled, skipping fraud.detected event for payment: {}", event.getPaymentId());
        }
    }
}

