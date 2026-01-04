// ========================================
// Metrics Configuration
// ========================================

// File: src/main/java/com/banking/audit/config/MetricsConfig.java
package com.banking.audit.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter auditEventsProcessed(MeterRegistry registry) {
        return Counter.builder("audit.events.processed")
                .description("Total number of audit events processed")
                .tag("service", "audit")
                .register(registry);
    }

    @Bean
    public Counter auditEventsFailed(MeterRegistry registry) {
        return Counter.builder("audit.events.failed")
                .description("Total number of failed audit events")
                .tag("service", "audit")
                .register(registry);
    }
}