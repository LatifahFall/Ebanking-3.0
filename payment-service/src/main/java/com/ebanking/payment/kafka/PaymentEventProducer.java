package com.ebanking.payment.kafka;

import com.ebanking.payment.kafka.event.FraudDetectedEvent;
import com.ebanking.payment.kafka.event.PaymentCompletedEvent;
import com.ebanking.payment.kafka.event.PaymentReversedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${payment.service.kafka.topics.producer.payment-completed}")
    private String paymentCompletedTopic;

    @Value("${payment.service.kafka.topics.producer.payment-reversed}")
    private String paymentReversedTopic;

    @Value("${payment.service.kafka.topics.producer.fraud-detected}")
    private String fraudDetectedTopic;

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        kafkaTemplate.send(paymentCompletedTopic, event.getPaymentId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published payment.completed event for payment: {}", event.getPaymentId());
                    } else {
                        log.error("Failed to publish payment.completed event", ex);
                    }
                });
    }

    public void publishPaymentReversed(PaymentReversedEvent event) {
        kafkaTemplate.send(paymentReversedTopic, event.getPaymentId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published payment.reversed event for payment: {}", event.getPaymentId());
                    } else {
                        log.error("Failed to publish payment.reversed event", ex);
                    }
                });
    }

    public void publishFraudDetected(FraudDetectedEvent event) {
        kafkaTemplate.send(fraudDetectedTopic, event.getPaymentId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Published fraud.detected event for payment: {}", event.getPaymentId());
                    } else {
                        log.error("Failed to publish fraud.detected event", ex);
                    }
                });
    }
}

