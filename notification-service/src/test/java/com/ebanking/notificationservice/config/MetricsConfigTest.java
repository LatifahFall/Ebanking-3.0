package com.ebanking.notificationservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour MetricsConfig
 */
class MetricsConfigTest {

    private MetricsConfig metricsConfig;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        metricsConfig = new MetricsConfig();
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void testEmailSentCounterCreated() {
        // When
        Counter counter = metricsConfig.emailSentCounter(meterRegistry);

        // Then
        assertNotNull(counter);
        assertEquals(0.0, counter.count());
        
        // Increment and verify
        counter.increment();
        assertEquals(1.0, counter.count());
    }

    @Test
    void testSmsSentCounterCreated() {
        // When
        Counter counter = metricsConfig.smsSentCounter(meterRegistry);

        // Then
        assertNotNull(counter);
        assertEquals("notification.sent", counter.getId().getName());
    }

    @Test
    void testPushSentCounterCreated() {
        // When
        Counter counter = metricsConfig.pushSentCounter(meterRegistry);

        // Then
        assertNotNull(counter);
        assertTrue(counter.getId().getTags().stream()
                .anyMatch(tag -> tag.getKey().equals("type") && tag.getValue().equals("push")));
    }

    @Test
    void testInAppSentCounterCreated() {
        // When
        Counter counter = metricsConfig.inAppSentCounter(meterRegistry);

        // Then
        assertNotNull(counter);
        assertTrue(counter.getId().getTags().stream()
                .anyMatch(tag -> tag.getKey().equals("type") && tag.getValue().equals("in_app")));
    }

    @Test
    void testEmailFailedCounterCreated() {
        // When
        Counter counter = metricsConfig.emailFailedCounter(meterRegistry);

        // Then
        assertNotNull(counter);
        assertEquals("notification.failed", counter.getId().getName());
    }

    @Test
    void testSmsFailedCounterCreated() {
        // When
        Counter counter = metricsConfig.smsFailedCounter(meterRegistry);

        // Then
        assertNotNull(counter);
        assertEquals("notification.failed", counter.getId().getName());
        assertTrue(counter.getId().getTags().stream()
                .anyMatch(tag -> tag.getKey().equals("type") && tag.getValue().equals("sms")));
    }

    @Test
    void testNotificationTimerCreated() {
        // When
        Timer timer = metricsConfig.notificationTimer(meterRegistry);

        // Then
        assertNotNull(timer);
        assertEquals("notification.duration", timer.getId().getName());
        assertEquals(0L, timer.count());
    }

    @Test
    void testKafkaEventsCounterCreated() {
        // When
        Counter counter = metricsConfig.kafkaEventsCounter(meterRegistry);

        // Then
        assertNotNull(counter);
        assertEquals("kafka.events.consumed", counter.getId().getName());
        assertTrue(counter.getId().getTags().stream()
                .anyMatch(tag -> tag.getKey().equals("service") && tag.getValue().equals("notification")));
    }

    @Test
    void testAllCountersAreIndependent() {
        // Given
        Counter emailCounter = metricsConfig.emailSentCounter(meterRegistry);
        Counter smsCounter = metricsConfig.smsSentCounter(meterRegistry);

        // When
        emailCounter.increment();
        emailCounter.increment();
        smsCounter.increment();

        // Then
        assertEquals(2.0, emailCounter.count());
        assertEquals(1.0, smsCounter.count());
    }

    @Test
    void testTimerCanRecordDuration() {
        // Given
        Timer timer = metricsConfig.notificationTimer(meterRegistry);

        // When
        timer.record(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Then
        assertEquals(1L, timer.count());
        assertTrue(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) >= 10);
    }
}
