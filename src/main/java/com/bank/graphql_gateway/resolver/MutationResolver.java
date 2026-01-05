package com.bank.graphql_gateway.resolver;

import com.bank.graphql_gateway.model.*;
import com.bank.graphql_gateway.security.SecurityContext;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
public class MutationResolver {

    private final WebClient.Builder webClient;
    private final SecurityContext securityContext;

    public MutationResolver(WebClient.Builder webClient, SecurityContext securityContext) {
        this.webClient = webClient;
        this.securityContext = securityContext;
    }

    private WebClient.RequestHeadersSpec<?> buildRequestWithAuth(
            WebClient.RequestHeadersSpec<?> spec, DataFetchingEnvironment env) {
        String authHeader = securityContext.getAuthorizationHeader(env);
        if (authHeader != null) {
            return spec.header(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return spec;
    }

    // ==================== USER SERVICE MUTATIONS ====================

    @MutationMapping
    public UserDTO createUser(@Argument CreateUserInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/admin/users")
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO activateUser(@Argument Long id, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .patch()
                        .uri("http://localhost:8081/admin/users/activate?userId={id}", id),
                env)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO deactivateUser(@Argument Long id, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .patch()
                        .uri("http://localhost:8081/admin/users/deactivate?userId={id}", id),
                env)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO updateProfile(@Argument Long id, @Argument UpdateProfileInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .put()
                        .uri("http://localhost:8081/me/{id}", id)
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public AgentClientAssignmentDTO assignClient(@Argument AssignClientInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/admin/users/assignments")
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(AgentClientAssignmentDTO.class)
                .block();
    }

    @MutationMapping
    public Boolean unassignClient(@Argument Long agentId, @Argument Long clientId, DataFetchingEnvironment env) {
        buildRequestWithAuth(
                webClient.build()
                        .delete()
                        .uri("http://localhost:8081/admin/users/assignments?agentId={agentId}&clientId={clientId}", agentId, clientId),
                env)
                .retrieve()
                .toBodilessEntity()
                .block();
        return true;
    }

    // ==================== ACCOUNT SERVICE MUTATIONS ====================

    @MutationMapping
    public AccountDTO createAccount(@Argument CreateAccountInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8082/api/accounts")
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    @MutationMapping
    public AccountDTO updateAccount(@Argument Long id, @Argument UpdateAccountInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .put()
                        .uri("http://localhost:8082/api/accounts/{id}", id)
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    @MutationMapping
    public AccountDTO suspendAccount(@Argument Long id, @Argument SuspendAccountInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8082/api/accounts/{id}/suspend", id)
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    @MutationMapping
    public AccountDTO closeAccount(@Argument Long id, @Argument CloseAccountInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8082/api/accounts/{id}/close", id)
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    // ==================== AUTH SERVICE MUTATIONS ====================

    @MutationMapping
    public TokenDTO login(@Argument LoginInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/auth/login")
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(TokenDTO.class)
                .block();
    }

    @MutationMapping
    public TokenDTO refreshToken(@Argument RefreshTokenInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/auth/refresh")
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(TokenDTO.class)
                .block();
    }

    @MutationMapping
    public Boolean logout(@Argument RefreshTokenInput input, DataFetchingEnvironment env) {
        buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/auth/logout")
                        .bodyValue(input),
                env)
                .retrieve()
                .toBodilessEntity()
                .block();
        return true;
    }

    // ==================== PAYMENT SERVICE MUTATIONS ====================

    @MutationMapping
    public PaymentDTO createPayment(@Argument CreatePaymentInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8082/api/payments")
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(PaymentDTO.class)
                .block();
    }

    @MutationMapping
    public PaymentDTO cancelPayment(@Argument Long id, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8082/api/payments/{id}/cancel", id),
                env)
                .retrieve()
                .bodyToMono(PaymentDTO.class)
                .block();
    }

    @MutationMapping
    public PaymentDTO reversePayment(@Argument Long id, @Argument String reason, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8082/api/payments/{id}/reverse?reason={reason}", id, reason),
                env)
                .retrieve()
                .bodyToMono(PaymentDTO.class)
                .block();
    }

    // ==================== CRYPTO SERVICE MUTATIONS ====================

    @MutationMapping
    public CryptoWalletDTO createCryptoWallet(@Argument Long userId, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/api/wallets?userId={userId}", userId),
                env)
                .retrieve()
                .bodyToMono(CryptoWalletDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoWalletDTO activateCryptoWallet(@Argument Long walletId, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .patch()
                        .uri("http://localhost:8081/api/wallets/activate?walletId={walletId}", walletId),
                env)
                .retrieve()
                .bodyToMono(CryptoWalletDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoWalletDTO deactivateCryptoWallet(@Argument Long walletId, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .patch()
                        .uri("http://localhost:8081/api/wallets/deactivate?walletId={walletId}", walletId),
                env)
                .retrieve()
                .bodyToMono(CryptoWalletDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoTransactionDTO buyCrypto(@Argument Long walletId, @Argument BuyCryptoInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/api/transactions/buy?walletId={walletId}", walletId)
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(CryptoTransactionDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoTransactionDTO sellCrypto(@Argument Long walletId, @Argument SellCryptoInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8081/api/transactions/sell?walletId={walletId}", walletId)
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(CryptoTransactionDTO.class)
                .block();
    }

    // ==================== NOTIFICATION SERVICE MUTATIONS ====================

    @MutationMapping
    public NotificationDTO sendNotification(@Argument SendNotificationInput input, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .post()
                        .uri("http://localhost:8084/api/notifications")
                        .bodyValue(input),
                env)
                .retrieve()
                .bodyToMono(NotificationDTO.class)
                .block();
    }

    @MutationMapping
    public NotificationDTO markNotificationAsRead(@Argument Long id, DataFetchingEnvironment env) {
        return buildRequestWithAuth(
                webClient.build()
                        .put()
                        .uri("http://localhost:8084/api/notifications/{id}/read", id),
                env)
                .retrieve()
                .bodyToMono(NotificationDTO.class)
                .block();
    }

    // ==================== ANALYTICS SERVICE MUTATIONS ====================

    @MutationMapping
    public Boolean resolveAlert(@Argument String alertId, DataFetchingEnvironment env) {
        try {
            buildRequestWithAuth(
                    webClient.build()
                            .post()
                            .uri("http://localhost:8087/api/v1/analytics/alerts/{alertId}/resolve", alertId),
                    env)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve alert in Analytics Service: " + e.getMessage(), e);
        }
    }
}
