package com.ebanking.payment.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class AccountServiceClientTest {

    private WireMockServer wireMockServer;
    private AccountServiceClient accountServiceClient;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8089")
                .build();

        accountServiceClient = new AccountServiceClient(webClient);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void shouldGetAccount() {
        UUID accountId = UUID.randomUUID();
        String responseJson = """
                {
                    "id": "%s",
                    "userId": "123e4567-e89b-12d3-a456-426614174000",
                    "accountNumber": "ACC001",
                    "accountType": "SAVINGS",
                    "currency": "EUR",
                    "balance": 1000.00,
                    "status": "ACTIVE"
                }
                """.formatted(accountId);

        wireMockServer.stubFor(get(urlEqualTo("/api/accounts/" + accountId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson)));

        Account account = accountServiceClient.getAccount(accountId).block();

        assertThat(account).isNotNull();
        assertThat(account.getId()).isEqualTo(accountId);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void shouldCheckBalance() {
        UUID accountId = UUID.randomUUID();
        String balance = "5000.50";

        wireMockServer.stubFor(get(urlEqualTo("/api/accounts/" + accountId + "/balance"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(balance)));

        BigDecimal result = accountServiceClient.checkBalance(accountId).block();

        assertThat(result).isEqualByComparingTo(new BigDecimal("5000.50"));
    }

    @Test
    void shouldValidateAccountStatus() {
        UUID accountId = UUID.randomUUID();

        wireMockServer.stubFor(get(urlEqualTo("/api/accounts/" + accountId + "/status"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("\"ACTIVE\"")));

        Boolean isValid = accountServiceClient.validateAccountStatus(accountId).block();

        assertThat(isValid).isTrue();
    }

    @Test
    void shouldHandle404NotFound() {
        UUID accountId = UUID.randomUUID();

        wireMockServer.stubFor(get(urlEqualTo("/api/accounts/" + accountId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Account not found")));

        StepVerifier.create(accountServiceClient.getAccount(accountId))
                .expectErrorMatches(throwable ->
                        throwable instanceof AccountServiceClient.AccountNotFoundException)
                .verify();
    }

    @Test
    void shouldHandle500ServerError() {
        UUID accountId = UUID.randomUUID();

        wireMockServer.stubFor(get(urlEqualTo("/api/accounts/" + accountId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal server error")));

        StepVerifier.create(accountServiceClient.getAccount(accountId))
                .expectErrorMatches(throwable ->
                        throwable instanceof AccountServiceClient.AccountServiceException)
                .verify();
    }

    @Test
    void shouldHandleTimeout() {
        UUID accountId = UUID.randomUUID();

        wireMockServer.stubFor(get(urlEqualTo("/api/accounts/" + accountId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(15000)));

        StepVerifier.create(accountServiceClient.getAccount(accountId))
                .expectError()
                .verify();
    }
}

