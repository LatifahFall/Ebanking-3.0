package com.ebanking.payment.exception;

import com.ebanking.payment.controller.PaymentController;
import com.ebanking.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PaymentController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID paymentId = UUID.randomUUID();

    @Test
    @WithMockUser
    void shouldHandlePaymentNotFoundException() throws Exception {
        when(paymentService.getPayment(paymentId))
                .thenThrow(new PaymentNotFoundException("Payment not found: " + paymentId));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Payment Not Found"))
                .andExpect(jsonPath("$.message").value("Payment not found: " + paymentId));
    }

    @Test
    @WithMockUser
    void shouldHandleAccountNotFoundException() throws Exception {
        when(paymentService.getPayment(paymentId))
                .thenThrow(new AccountNotFoundException("Account not found"));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Account Not Found"));
    }

    @Test
    @WithMockUser
    void shouldHandlePaymentValidationException() throws Exception {
        when(paymentService.getPayment(paymentId))
                .thenThrow(new PaymentValidationException("Validation failed"));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    @WithMockUser
    void shouldHandleInsufficientBalanceException() throws Exception {
        when(paymentService.getPayment(paymentId))
                .thenThrow(new InsufficientBalanceException("Insufficient balance"));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Insufficient Balance"));
    }

    @Test
    @WithMockUser
    void shouldHandleFraudDetectedException() throws Exception {
        when(paymentService.getPayment(paymentId))
                .thenThrow(new FraudDetectedException("Fraud detected"));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Fraud Detected"));
    }

    @Test
    @WithMockUser
    void shouldHandlePaymentProcessingException() throws Exception {
        when(paymentService.getPayment(paymentId))
                .thenThrow(new PaymentProcessingException("Processing error"));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Payment Processing Error"));
    }

    @Test
    @WithMockUser
    void shouldHandleIllegalStateException() throws Exception {
        when(paymentService.getPayment(paymentId))
                .thenThrow(new IllegalStateException("Invalid state"));

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Invalid State"));
    }
}
