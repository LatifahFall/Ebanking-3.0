package com.ebanking.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.ebanking.payment.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // JPA configuration is handled by application.yml
    // This class enables JPA repositories, auditing, and transaction management
}

