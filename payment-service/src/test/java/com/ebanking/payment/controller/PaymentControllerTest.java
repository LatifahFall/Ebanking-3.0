package com.ebanking.payment.controller;

import com.ebanking.payment.dto.PaymentRequest;
import com.ebanking.payment.dto.PaymentResponse;
import com.ebanking.payment.entity.PaymentStatus;
import com.ebanking.payment.entity.PaymentType;
import com.ebanking.payment.entity.ReversalReason;
import com.ebanking.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID paymentId = UUID.randomUUID();
    private UUID accountId = UUID.randomUUID();

    @Test
    @WithMockUser(username = "user-id")
    void shouldInitiatePayment() throws Exception {
        PaymentRequest request = new PaymentRequest();
        request.setFromAccountId(accountId);
        request.setToAccountId(UUID.randomUUID());
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("EUR");
        request.setPaymentType(PaymentType.STANDARD);

        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .fromAccountId(accountId)
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentService.initiatePayment(any(PaymentRequest.class), any(UUID.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    @WithMockUser
    void shouldGetPayment() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .amount(new BigDecimal("100.00"))
                .status(PaymentStatus.COMPLETED)
                .build();

        when(paymentService.getPayment(paymentId)).thenReturn(response);

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(paymentId.toString()))
                .andExpect(jsonPath("$.amount").value(100.00));
    }

    @Test
    @WithMockUser
    void shouldCancelPayment() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .status(PaymentStatus.CANCELLED)
                .build();

        when(paymentService.cancelPayment(paymentId)).thenReturn(response);

        mockMvc.perform(post("/api/payments/{id}/cancel", paymentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser
    void shouldReversePayment() throws Exception {
        PaymentResponse response = PaymentResponse.builder()
                .id(paymentId)
                .status(PaymentStatus.REVERSED)
                .build();

        when(paymentService.reversePayment(eq(paymentId), eq(ReversalReason.CUSTOMER_REQUEST)))
                .thenReturn(response);

        mockMvc.perform(post("/api/payments/{id}/reverse", paymentId)
                        .with(csrf())
                        .param("reason", "CUSTOMER_REQUEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVERSED"));
    }
}

