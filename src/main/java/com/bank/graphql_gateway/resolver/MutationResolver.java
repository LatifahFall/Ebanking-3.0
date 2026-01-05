package com.bank.graphql_gateway.resolver;

import com.bank.graphql_gateway.model.*;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

@Controller
public class MutationResolver {

    private final WebClient.Builder webClient;

    public MutationResolver(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    // ==================== USER SERVICE MUTATIONS ====================

    @MutationMapping
    public UserDTO createUser(@Argument CreateUserInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/admin/users")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO activateUser(@Argument Long id) {
        return webClient.build()
                .patch()
                .uri("http://localhost:8081/admin/users/activate?userId={id}", id)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO deactivateUser(@Argument Long id) {
        return webClient.build()
                .patch()
                .uri("http://localhost:8081/admin/users/deactivate?userId={id}", id)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public UserDTO updateProfile(@Argument Long id, @Argument UpdateProfileInput input) {
        return webClient.build()
                .put()
                .uri("http://localhost:8081/me/{id}", id)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(UserDTO.class)
                .block();
    }

    @MutationMapping
    public AgentClientAssignmentDTO assignClient(@Argument AssignClientInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/admin/users/assignments")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(AgentClientAssignmentDTO.class)
                .block();
    }

    @MutationMapping
    public Boolean unassignClient(@Argument Long agentId, @Argument Long clientId) {
        webClient.build()
                .delete()
                .uri("http://localhost:8081/admin/users/assignments?agentId={agentId}&clientId={clientId}", agentId, clientId)
                .retrieve()
                .toBodilessEntity()
                .block();
        return true;
    }

    // ==================== ACCOUNT SERVICE MUTATIONS ====================

    @MutationMapping
    public AccountDTO createAccount(@Argument CreateAccountInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8082/api/accounts")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    @MutationMapping
    public AccountDTO updateAccount(@Argument Long id, @Argument UpdateAccountInput input) {
        return webClient.build()
                .put()
                .uri("http://localhost:8082/api/accounts/{id}", id)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    @MutationMapping
    public AccountDTO suspendAccount(@Argument Long id, @Argument SuspendAccountInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8082/api/accounts/{id}/suspend", id)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    @MutationMapping
    public AccountDTO closeAccount(@Argument Long id, @Argument CloseAccountInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8082/api/accounts/{id}/close", id)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();
    }

    // ==================== AUTH SERVICE MUTATIONS ====================

    @MutationMapping
    public TokenDTO login(@Argument LoginInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/auth/login")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(TokenDTO.class)
                .block();
    }

    @MutationMapping
    public TokenDTO refreshToken(@Argument RefreshTokenInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/auth/refresh")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(TokenDTO.class)
                .block();
    }

    @MutationMapping
    public Boolean logout(@Argument RefreshTokenInput input) {
        webClient.build()
                .post()
                .uri("http://localhost:8081/auth/logout")
                .bodyValue(input)
                .retrieve()
                .toBodilessEntity()
                .block();
        return true;
    }

    // ==================== PAYMENT SERVICE MUTATIONS ====================

    @MutationMapping
    public PaymentDTO createPayment(@Argument CreatePaymentInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8082/api/payments")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(PaymentDTO.class)
                .block();
    }

    @MutationMapping
    public PaymentDTO cancelPayment(@Argument Long id) {
        return webClient.build()
                .post()
                .uri("http://localhost:8082/api/payments/{id}/cancel", id)
                .retrieve()
                .bodyToMono(PaymentDTO.class)
                .block();
    }

    @MutationMapping
    public PaymentDTO reversePayment(@Argument Long id, @Argument String reason) {
        return webClient.build()
                .post()
                .uri("http://localhost:8082/api/payments/{id}/reverse?reason={reason}", id, reason)
                .retrieve()
                .bodyToMono(PaymentDTO.class)
                .block();
    }

    // ==================== CRYPTO SERVICE MUTATIONS ====================

    @MutationMapping
    public CryptoWalletDTO createCryptoWallet(@Argument Long userId) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/api/wallets?userId={userId}", userId)
                .retrieve()
                .bodyToMono(CryptoWalletDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoWalletDTO activateCryptoWallet(@Argument Long walletId) {
        return webClient.build()
                .patch()
                .uri("http://localhost:8081/api/wallets/activate?walletId={walletId}", walletId)
                .retrieve()
                .bodyToMono(CryptoWalletDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoWalletDTO deactivateCryptoWallet(@Argument Long walletId) {
        return webClient.build()
                .patch()
                .uri("http://localhost:8081/api/wallets/deactivate?walletId={walletId}", walletId)
                .retrieve()
                .bodyToMono(CryptoWalletDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoTransactionDTO buyCrypto(@Argument Long walletId, @Argument BuyCryptoInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/api/transactions/buy?walletId={walletId}", walletId)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(CryptoTransactionDTO.class)
                .block();
    }

    @MutationMapping
    public CryptoTransactionDTO sellCrypto(@Argument Long walletId, @Argument SellCryptoInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8081/api/transactions/sell?walletId={walletId}", walletId)
                .bodyValue(input)
                .retrieve()
                .bodyToMono(CryptoTransactionDTO.class)
                .block();
    }

    // ==================== NOTIFICATION SERVICE MUTATIONS ====================

    @MutationMapping
    public NotificationDTO sendNotification(@Argument SendNotificationInput input) {
        return webClient.build()
                .post()
                .uri("http://localhost:8084/api/notifications")
                .bodyValue(input)
                .retrieve()
                .bodyToMono(NotificationDTO.class)
                .block();
    }

    @MutationMapping
    public NotificationDTO markNotificationAsRead(@Argument Long id) {
        return webClient.build()
                .put()
                .uri("http://localhost:8084/api/notifications/{id}/read", id)
                .retrieve()
                .bodyToMono(NotificationDTO.class)
                .block();
    }
}
