package com.ebanking.payment.controller;

import com.ebanking.payment.dto.PaymentRuleRequest;
import com.ebanking.payment.entity.PaymentRule;
import com.ebanking.payment.service.PaymentRuleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentRuleController.class)
class PaymentRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentRuleService paymentRuleService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID ruleId = UUID.randomUUID();

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllRules() throws Exception {
        PaymentRule rule = PaymentRule.builder()
                .id(ruleId)
                .ruleName("Test Rule")
                .enabled(true)
                .build();

        when(paymentRuleService.getAllRules()).thenReturn(List.of(rule));

        mockMvc.perform(get("/api/admin/payment-rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(ruleId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateRule() throws Exception {
        PaymentRuleRequest request = new PaymentRuleRequest();
        request.setRuleType("AMOUNT_LIMIT");
        request.setRuleName("Max Amount Rule");
        request.setConditions("{\"maxAmount\": 10000}");

        PaymentRule rule = PaymentRule.builder()
                .id(ruleId)
                .ruleName("Max Amount Rule")
                .build();

        when(paymentRuleService.createRule(any(PaymentRule.class))).thenReturn(rule);

        mockMvc.perform(post("/api/admin/payment-rules")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ruleId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteRule() throws Exception {
        doNothing().when(paymentRuleService).deleteRule(ruleId);

        mockMvc.perform(delete("/api/admin/payment-rules/{id}", ruleId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(paymentRuleService).deleteRule(ruleId);
    }
}

