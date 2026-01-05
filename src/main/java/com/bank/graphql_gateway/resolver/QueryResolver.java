package com.bank.graphql_gateway.resolver;

import com.bank.graphql_gateway.model.*;
import com.bank.graphql_gateway.security.SecurityContext;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Controller
public class QueryResolver {

    private final WebClient.Builder webClient;
    private final SecurityContext securityContext;

    public QueryResolver(WebClient.Builder webClient, SecurityContext securityContext) {
        this.webClient = webClient;
        this.securityContext = securityContext;
    }

    /**
     * Helper method to build WebClient with Authorization header if present.
     * Forwards Bearer token to microservices for authentication and authorization.
     */
    private WebClient.RequestHeadersSpec<?> buildRequestWithAuth(WebClient.RequestHeadersSpec<?> spec, DataFetchingEnvironment env) {
        String authHeader = securityContext.getAuthorizationHeader(env);
        if (authHeader != null && !authHeader.isEmpty()) {
            return spec.header(HttpHeaders.AUTHORIZATION, authHeader);
        }
        return spec;
    }

    @QueryMapping
    public String health() {
        return "GraphQL Gateway is UP";
    }

    // ==================== USER SERVICE QUERIES ====================
    
    @QueryMapping
    public List<UserDTO> users(DataFetchingEnvironment env) {
        try {
            PageResponse<UserDTO> page = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/admin/users/search"),
                    env)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PageResponse<UserDTO>>() {})
                    .block();
            return page != null && page.getContent() != null ? page.getContent() : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch users from User Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public UserDTO userById(@Argument Long id, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/admin/users/{id}", id),
                    env)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user by ID from User Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public UserDTO me(@Argument Long id, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/me/{id}", id),
                    env)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user profile from User Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<UserDTO> clientsByAgent(@Argument Long agentId, DataFetchingEnvironment env) {
        try {
            List<UserDTO> clients = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/agent/clients/{agentId}", agentId),
                    env)
                    .retrieve()
                    .bodyToFlux(UserDTO.class)
                    .collectList()
                    .block();
            return clients != null ? clients : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch clients by agent from User Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public UserDTO agentByClient(@Argument Long clientId, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/admin/users/clients/{clientId}/agent", clientId),
                    env)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch agent by client from User Service: " + e.getMessage(), e);
        }
    }

    // ==================== ACCOUNT SERVICE QUERIES ====================
    
    @QueryMapping
    public AccountDTO accountById(@Argument Long id, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/accounts/{id}", id),
                    env)
                    .retrieve()
                    .bodyToMono(AccountDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch account by ID from Account Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<AccountDTO> accountsByUserId(@Argument Long userId, DataFetchingEnvironment env) {
        try {
            List<AccountDTO> accounts = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/accounts?userId={userId}", userId),
                    env)
                    .retrieve()
                    .bodyToFlux(AccountDTO.class)
                    .collectList()
                    .block();
            return accounts != null ? accounts : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch accounts by user ID from Account Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public BalanceDTO accountBalance(@Argument Long id, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/accounts/{id}/balance", id),
                    env)
                    .retrieve()
                    .bodyToMono(BalanceDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch account balance from Account Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<TransactionDTO> accountTransactions(@Argument Long id, DataFetchingEnvironment env) {
        try {
            List<TransactionDTO> transactions = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/accounts/{id}/transactions", id),
                    env)
                    .retrieve()
                    .bodyToFlux(TransactionDTO.class)
                    .collectList()
                    .block();
            return transactions != null ? transactions : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch account transactions from Account Service: " + e.getMessage(), e);
        }
    }

    // ==================== AUTH SERVICE QUERIES ====================
    
    @QueryMapping
    public Boolean verifyToken(@Argument String token) {
        try {
            TokenValidationInput input = new TokenValidationInput();
            input.setToken(token);
            return webClient.build()
                    .post()
                    .uri("http://localhost:8081/auth/verify-token")
                    .bodyValue(input)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify token from Auth Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public TokenInfoDTO tokenInfo(@Argument String token) {
        try {
            TokenValidationInput input = new TokenValidationInput();
            input.setToken(token);
            return webClient.build()
                    .post()
                    .uri("http://localhost:8081/auth/token-info")
                    .bodyValue(input)
                    .retrieve()
                    .bodyToMono(TokenInfoDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get token info from Auth Service: " + e.getMessage(), e);
        }
    }

    // ==================== PAYMENT SERVICE QUERIES ====================
    
    @QueryMapping
    public PaymentDTO paymentById(@Argument Long id, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/payments/{id}", id),
                    env)
                    .retrieve()
                    .bodyToMono(PaymentDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch payment by ID from Payment Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<PaymentDTO> paymentsByUserId(@Argument Long userId, DataFetchingEnvironment env) {
        try {
            List<PaymentDTO> payments = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/payments?userId={userId}", userId),
                    env)
                    .retrieve()
                    .bodyToFlux(PaymentDTO.class)
                    .collectList()
                    .block();
            return payments != null ? payments : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch payments by user ID from Payment Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<PaymentDTO> paymentsByAccountId(@Argument Long accountId, DataFetchingEnvironment env) {
        try {
            List<PaymentDTO> payments = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/payments?accountId={accountId}", accountId),
                    env)
                    .retrieve()
                    .bodyToFlux(PaymentDTO.class)
                    .collectList()
                    .block();
            return payments != null ? payments : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch payments by account ID from Payment Service: " + e.getMessage(), e);
        }
    }

    // ==================== CRYPTO SERVICE QUERIES ====================
    
    @QueryMapping
    public CryptoWalletDTO cryptoWalletByUserId(@Argument Long userId, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/api/wallets/user/{userId}", userId),
                    env)
                    .retrieve()
                    .bodyToMono(CryptoWalletDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch crypto wallet by user ID from Crypto Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<CryptoTransactionDTO> cryptoTransactionsByWalletId(@Argument Long walletId, DataFetchingEnvironment env) {
        try {
            List<CryptoTransactionDTO> transactions = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/api/transactions/wallet/{walletId}", walletId),
                    env)
                    .retrieve()
                    .bodyToFlux(CryptoTransactionDTO.class)
                    .collectList()
                    .block();
            return transactions != null ? transactions : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch crypto transactions by wallet ID from Crypto Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<CryptoCoinDTO> cryptoCoins(DataFetchingEnvironment env) {
        try {
            List<CryptoCoinDTO> coins = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8081/api/coins"),
                    env)
                    .retrieve()
                    .bodyToFlux(CryptoCoinDTO.class)
                    .collectList()
                    .block();
            return coins != null ? coins : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch crypto coins from Crypto Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public CryptoCoinDTO cryptoCoinById(@Argument Long id, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8082/api/coins/{id}", id),
                    env)
                    .retrieve()
                    .bodyToMono(CryptoCoinDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch crypto coin by ID from Crypto Service: " + e.getMessage(), e);
        }
    }

    // ==================== NOTIFICATION SERVICE QUERIES ====================
    
    @QueryMapping
    public List<NotificationDTO> notificationsByUserId(@Argument Long userId, DataFetchingEnvironment env) {
        try {
            List<NotificationDTO> notifications = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8085/api/notifications/user/{userId}", userId),
                    env)
                    .retrieve()
                    .bodyToFlux(NotificationDTO.class)
                    .collectList()
                    .block();
            return notifications != null ? notifications : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch notifications by user ID from Notification Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<InAppNotificationDTO> inAppNotificationsByUserId(@Argument Long userId, DataFetchingEnvironment env) {
        try {
            List<InAppNotificationDTO> notifications = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8085/api/notifications/in-app/user/{userId}", userId),
                    env)
                    .retrieve()
                    .bodyToFlux(InAppNotificationDTO.class)
                    .collectList()
                    .block();
            return notifications != null ? notifications : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch in-app notifications by user ID from Notification Service: " + e.getMessage(), e);
        }
    }

    // ==================== AUDIT SERVICE QUERIES ====================
    
    @QueryMapping
    public List<AuditEventDTO> auditEvents(DataFetchingEnvironment env) {
        try {
            List<AuditEventDTO> events = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8083/audit/events"),
                    env)
                    .retrieve()
                    .bodyToFlux(AuditEventDTO.class)
                    .collectList()
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch audit events from Audit Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public AuditEventDTO auditEventById(@Argument Long id, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8086/api/audit/events/{id}", id),
                    env)
                    .retrieve()
                    .bodyToMono(AuditEventDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch audit event by ID from Audit Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<AuditEventDTO> auditEventsByUserId(@Argument Long userId, DataFetchingEnvironment env) {
        try {
            List<AuditEventDTO> events = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8086/api/audit/events/user/{userId}", userId),
                    env)
                    .retrieve()
                    .bodyToFlux(AuditEventDTO.class)
                    .collectList()
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch audit events by user ID from Audit Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<AuditEventDTO> auditEventsByType(@Argument String eventType, DataFetchingEnvironment env) {
        try {
            List<AuditEventDTO> events = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8083/audit/events/type/{eventType}", eventType),
                    env)
                    .retrieve()
                    .bodyToFlux(AuditEventDTO.class)
                    .collectList()
                    .block();
            return events != null ? events : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch audit events by type from Audit Service: " + e.getMessage(), e);
        }
    }

    // ==================== ANALYTICS SERVICE QUERIES ====================
    
    @QueryMapping
    public List<AlertDTO> activeAlerts(@Argument String userId, DataFetchingEnvironment env) {
        try {
            List<AlertDTO> alerts = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8087/api/v1/analytics/alerts/active?userId={userId}", userId),
                    env)
                    .retrieve()
                    .bodyToFlux(AlertDTO.class)
                    .collectList()
                    .block();
            return alerts != null ? alerts : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch active alerts from Analytics Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public DashboardSummaryDTO dashboardSummary(@Argument String userId, DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8087/api/v1/analytics/dashboard/summary?userId={userId}", userId),
                    env)
                    .retrieve()
                    .bodyToMono(DashboardSummaryDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch dashboard summary from Analytics Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<CategoryBreakdownDTO> spendingBreakdown(@Argument String userId, @Argument String period, DataFetchingEnvironment env) {
        try {
            String periodParam = period != null ? period : "MONTH";
            List<CategoryBreakdownDTO> breakdown = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8087/api/v1/analytics/spending/breakdown?userId={userId}&period={period}", 
                                 userId, periodParam),
                    env)
                    .retrieve()
                    .bodyToFlux(CategoryBreakdownDTO.class)
                    .collectList()
                    .block();
            return breakdown != null ? breakdown : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch spending breakdown from Analytics Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public BalanceTrendDTO balanceTrend(@Argument String userId, @Argument Integer days, DataFetchingEnvironment env) {
        try {
            int daysParam = days != null ? days : 30;
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8087/api/v1/analytics/trends/balance?userId={userId}&days={days}", 
                                 userId, daysParam),
                    env)
                    .retrieve()
                    .bodyToMono(BalanceTrendDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch balance trend from Analytics Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<String> recommendations(@Argument String userId, DataFetchingEnvironment env) {
        try {
            List<String> recommendations = buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8087/api/v1/analytics/insights/recommendations?userId={userId}", userId),
                    env)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .collectList()
                    .block();
            return recommendations != null ? recommendations : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch recommendations from Analytics Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public AdminOverviewDTO adminOverview(DataFetchingEnvironment env) {
        try {
            return buildRequestWithAuth(
                    webClient.build()
                            .get()
                            .uri("http://localhost:8087/api/v1/analytics/admin/overview"),
                    env)
                    .retrieve()
                    .bodyToMono(AdminOverviewDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch admin overview from Analytics Service: " + e.getMessage(), e);
        }
    }
}
