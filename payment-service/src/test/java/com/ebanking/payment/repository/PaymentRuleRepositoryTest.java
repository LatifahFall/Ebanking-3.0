package com.ebanking.payment.repository;

import com.ebanking.payment.entity.PaymentRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PaymentRuleRepositoryTest {

    @Autowired
    private PaymentRuleRepository paymentRuleRepository;

    private PaymentRule enabledRule;
    private PaymentRule disabledRule;

    @BeforeEach
    void setUp() {
        enabledRule = PaymentRule.builder()
                .ruleType("AMOUNT_LIMIT")
                .ruleName("Max Amount Rule")
                .description("Maximum amount limit")
                .conditions("{\"maxAmount\": 10000}")
                .enabled(true)
                .priority(10)
                .build();

        disabledRule = PaymentRule.builder()
                .ruleType("DAILY_LIMIT")
                .ruleName("Daily Limit Rule")
                .description("Daily limit rule")
                .conditions("{\"dailyLimit\": 5000}")
                .enabled(false)
                .priority(5)
                .build();
    }

    @Test
    void shouldSavePaymentRule() {
        PaymentRule saved = paymentRuleRepository.save(enabledRule);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getRuleName()).isEqualTo("Max Amount Rule");
        assertThat(saved.getEnabled()).isTrue();
    }

    @Test
    void shouldFindByEnabledTrue() {
        paymentRuleRepository.save(enabledRule);
        paymentRuleRepository.save(disabledRule);

        List<PaymentRule> enabledRules = paymentRuleRepository.findByEnabledTrue();

        assertThat(enabledRules).hasSize(1);
        assertThat(enabledRules.get(0).getEnabled()).isTrue();
    }

    @Test
    void shouldFindByEnabledTrueOrderByPriorityDesc() {
        PaymentRule lowPriorityRule = PaymentRule.builder()
                .ruleType("TYPE1")
                .ruleName("Low Priority")
                .conditions("{}")
                .enabled(true)
                .priority(5)
                .build();

        paymentRuleRepository.save(enabledRule);
        paymentRuleRepository.save(lowPriorityRule);

        List<PaymentRule> rules = paymentRuleRepository.findByEnabledTrueOrderByPriorityDesc();

        assertThat(rules).hasSize(2);
        assertThat(rules.get(0).getPriority()).isGreaterThanOrEqualTo(rules.get(1).getPriority());
    }
}

