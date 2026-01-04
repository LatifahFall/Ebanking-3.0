package com.ebanking.payment.integration;

import com.ebanking.payment.client.Account;
import com.ebanking.payment.client.AccountServiceClient;
import com.ebanking.payment.client.AccountStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AccountServiceClient
 * Tests REST API communication with account-service using MockWebServer
 * 
 * IMPORTANT: This test validates that the contract matches the real account-service API:
 * - Account IDs are Long (not UUID)
 * - Endpoints: /api/accounts/{id}, /api/accounts/{id}/balance, /api/accounts/{id}/debit, /api/accounts/{id}/credit
 */
class AccountServiceIntegrationTest {

    private MockWebServer mockWebServer;
    private AccountServiceClient accountServiceClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        accountServiceClient = new AccountServiceClient(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGetAccount_Success() throws Exception {
        // Given: account-service returns an account with Long ID
        Long accountId = 12345L;
        Account mockAccount = Account.builder()
                .id(accountId)
                .userId(100L)
                .accountNumber("FR7630006000011234567890189")
                .accountType("CHECKING")
                .currency("EUR")
                .balance(BigDecimal.valueOf(1500.50))
                .status(AccountStatus.ACTIVE)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(mockAccount)));

        // When: calling getAccount
        Account result = accountServiceClient.getAccount(accountId).block();

        // Then: validate response matches account-service contract
        assertNotNull(result);
        assertEquals(accountId, result.getId()); // CRITICAL: ID must be Long
        assertEquals(100L, result.getUserId()); // CRITICAL: userId must be Long
        assertEquals("FR7630006000011234567890189", result.getAccountNumber());
        assertEquals("CHECKING", result.getAccountType());
        assertEquals("EUR", result.getCurrency());
        assertEquals(BigDecimal.valueOf(1500.50), result.getBalance());
        assertEquals("ACTIVE", result.getStatus());

        // Verify request
        assertEquals("/api/accounts/" + accountId, mockWebServer.takeRequest().getPath());
    }

    @Test
    void testGetAccount_NotFound() {
        // Given: account-service returns 404
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\":\"Account not found\"}"));

        // When/Then: should throw AccountNotFoundException
        assertThrows(AccountServiceClient.AccountNotFoundException.class, () -> {
            accountServiceClient.getAccount(99999L).block();
        });
    }

    @Test
    void testCheckBalance_Success() throws Exception {
        // Given: account-service returns balance
        Long accountId = 12345L;
        BigDecimal balance = BigDecimal.valueOf(2500.75);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(balance.toString()));

        // When: calling checkBalance
        BigDecimal result = accountServiceClient.checkBalance(accountId).block();

        // Then: validate balance
        assertNotNull(result);
        assertEquals(balance, result);

        // Verify request
        assertEquals("/api/accounts/" + accountId + "/balance", mockWebServer.takeRequest().getPath());
    }

    @Test
    void testDebitAccount_Success() throws Exception {
        // Given: account-service returns updated account after debit
        Long accountId = 12345L;
        BigDecimal debitAmount = BigDecimal.valueOf(150.00);
        String reference = "Payment 999";

        Account updatedAccount = Account.builder()
                .id(accountId)
                .userId(100L)
                .accountNumber("FR7630006000011234567890189")
                .accountType("CHECKING")
                .currency("EUR")
                .balance(BigDecimal.valueOf(1350.50)) // 1500.50 - 150.00
                .status(AccountStatus.ACTIVE)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(updatedAccount)));

        // When: calling debitAccount
        Account result = accountServiceClient.debitAccount(accountId, debitAmount, reference).block();

        // Then: validate updated balance
        assertNotNull(result);
        assertEquals(accountId, result.getId());
        assertEquals(BigDecimal.valueOf(1350.50), result.getBalance());

        // Verify request
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals("/api/accounts/" + accountId + "/debit", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
        
        // Verify request body contains amount and reference
        String requestBody = recordedRequest.getBody().readUtf8();
        assertTrue(requestBody.contains("\"amount\":150.00") || requestBody.contains("\"amount\":150.0"));
        assertTrue(requestBody.contains("\"reference\":\"Payment 999\""));
    }

    @Test
    void testDebitAccount_InsufficientFunds() {
        // Given: account-service returns 400 for insufficient funds
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"error\":\"Insufficient funds\"}"));

        // When/Then: should throw AccountServiceException
        assertThrows(AccountServiceClient.AccountServiceException.class, () -> {
            accountServiceClient.debitAccount(12345L, BigDecimal.valueOf(10000), "Payment 999").block();
        });
    }

    @Test
    void testCreditAccount_Success() throws Exception {
        // Given: account-service returns updated account after credit
        Long accountId = 12345L;
        BigDecimal creditAmount = BigDecimal.valueOf(500.00);
        String reference = "Refund 888";

        Account updatedAccount = Account.builder()
                .id(accountId)
                .userId(100L)
                .accountNumber("FR7630006000011234567890189")
                .accountType("CHECKING")
                .currency("EUR")
                .balance(BigDecimal.valueOf(2000.50)) // 1500.50 + 500.00
                .status(AccountStatus.ACTIVE)
                .build();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(updatedAccount)));

        // When: calling creditAccount
        Account result = accountServiceClient.creditAccount(accountId, creditAmount, reference).block();

        // Then: validate updated balance
        assertNotNull(result);
        assertEquals(accountId, result.getId());
        assertEquals(BigDecimal.valueOf(2000.50), result.getBalance());

        // Verify request
        var recordedRequest = mockWebServer.takeRequest();
        assertEquals("/api/accounts/" + accountId + "/credit", recordedRequest.getPath());
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    void testValidateAccountStatus_Active() throws Exception {
        // Given: account-service returns ACTIVE status
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("\"ACTIVE\""));

        // When: calling validateAccountStatus
        Boolean result = accountServiceClient.validateAccountStatus(12345L).block();

        // Then: should return true
        assertTrue(result);
    }

    @Test
    void testValidateAccountStatus_Suspended() throws Exception {
        // Given: account-service returns SUSPENDED status
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("\"SUSPENDED\""));

        // When: calling validateAccountStatus
        Boolean result = accountServiceClient.validateAccountStatus(12345L).block();

        // Then: should return false
        assertFalse(result);
    }

    @Test
    @org.junit.jupiter.api.Timeout(value = 5, unit = java.util.concurrent.TimeUnit.SECONDS)
    void testAccountServiceTimeout() {
        // Given: account-service takes too long (> 3 seconds)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBodyDelay(5, java.util.concurrent.TimeUnit.SECONDS));

        // When/Then: should timeout and throw exception
        assertThrows(Exception.class, () -> {
            accountServiceClient.getAccount(12345L).block(java.time.Duration.ofSeconds(3));
        });
    }

    @Test
    void testAccountService_ServerError() {
        // Given: account-service returns 500
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\":\"Internal server error\"}"));

        // When/Then: should throw AccountServiceException
        assertThrows(AccountServiceClient.AccountServiceException.class, () -> {
            accountServiceClient.getAccount(12345L).block();
        });
    }
}
