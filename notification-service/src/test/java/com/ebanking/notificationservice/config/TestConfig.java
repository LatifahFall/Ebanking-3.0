package com.ebanking.notificationservice.config;

import com.ebanking.notificationservice.kafka.NotificationEventConsumer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Configuration de test pour désactiver complètement Kafka
 * Cette classe mock tous les beans Kafka pour éviter les tentatives de connexion
 */
@TestConfiguration
public class TestConfig {

    @MockBean
    private NotificationEventConsumer notificationEventConsumer;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private ConsumerFactory<String, String> consumerFactory;

    @MockBean
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
}
