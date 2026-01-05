package com.bank.graphql_gateway.resolver;

import com.bank.graphql_gateway.model.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Controller
public class QueryResolver {

    private final WebClient.Builder webClient;

    public QueryResolver(WebClient.Builder webClient) {
        this.webClient = webClient;
    }

    @QueryMapping
    public String health() {
        return "GraphQL Gateway is UP";
    }

    // ==================== USER SERVICE QUERIES ====================
    
    @QueryMapping
    public List<UserDTO> users() {
        try {
            PageResponse<UserDTO> page = webClient.build()
                    .get()
                    .uri("http://localhost:8081/admin/users/search")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PageResponse<UserDTO>>() {})
                    .block();
            return page != null && page.getContent() != null ? page.getContent() : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch users from User Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public UserDTO userById(@Argument Long id) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8081/admin/users/{id}", id)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user by ID from User Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public UserDTO me(@Argument Long id) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8081/me/{id}", id)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user profile from User Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<UserDTO> clientsByAgent(@Argument Long agentId) {
        try {
            List<UserDTO> clients = webClient.build()
                    .get()
                    .uri("http://localhost:8081/agent/clients/{agentId}", agentId)
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
    public UserDTO agentByClient(@Argument Long clientId) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8081/admin/users/clients/{clientId}/agent", clientId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch agent by client from User Service: " + e.getMessage(), e);
        }
    }

    // ==================== ACCOUNT SERVICE QUERIES ====================
    
    @QueryMapping
    public AccountDTO accountById(@Argument Long id) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8082/api/accounts/{id}", id)
                    .retrieve()
                    .bodyToMono(AccountDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch account by ID from Account Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<AccountDTO> accountsByUserId(@Argument Long userId) {
        try {
            List<AccountDTO> accounts = webClient.build()
                    .get()
                    .uri("http://localhost:8082/api/accounts?userId={userId}", userId)
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
    public BalanceDTO accountBalance(@Argument Long id) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8082/api/accounts/{id}/balance", id)
                    .retrieve()
                    .bodyToMono(BalanceDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch account balance from Account Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<TransactionDTO> accountTransactions(@Argument Long id) {
        try {
            List<TransactionDTO> transactions = webClient.build()
                    .get()
                    .uri("http://localhost:8082/api/accounts/{id}/transactions", id)
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
    public PaymentDTO paymentById(@Argument Long id) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8082/api/payments/{id}", id)
                    .retrieve()
                    .bodyToMono(PaymentDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch payment by ID from Payment Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<PaymentDTO> paymentsByUserId(@Argument Long userId) {
        try {
            List<PaymentDTO> payments = webClient.build()
                    .get()
                    .uri("http://localhost:8082/api/payments?userId={userId}", userId)
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
    public List<PaymentDTO> paymentsByAccountId(@Argument Long accountId) {
        try {
            List<PaymentDTO> payments = webClient.build()
                    .get()
                    .uri("http://localhost:8082/api/payments?accountId={accountId}", accountId)
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
    public CryptoWalletDTO cryptoWalletByUserId(@Argument Long userId) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8081/api/wallets/user/{userId}", userId)
                    .retrieve()
                    .bodyToMono(CryptoWalletDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch crypto wallet by user ID from Crypto Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<CryptoTransactionDTO> cryptoTransactionsByWalletId(@Argument Long walletId) {
        try {
            List<CryptoTransactionDTO> transactions = webClient.build()
                    .get()
                    .uri("http://localhost:8081/api/transactions/wallet/{walletId}", walletId)
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
    public List<CryptoCoinDTO> cryptoCoins() {
        try {
            List<CryptoCoinDTO> coins = webClient.build()
                    .get()
                    .uri("http://localhost:8081/api/coins/details")
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
    public CryptoCoinDTO cryptoCoinById(@Argument String coinId) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8081/api/coins/{coinId}", coinId)
                    .retrieve()
                    .bodyToMono(CryptoCoinDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch crypto coin by ID from Crypto Service: " + e.getMessage(), e);
        }
    }

    // ==================== NOTIFICATION SERVICE QUERIES ====================
    
    @QueryMapping
    public List<NotificationDTO> notificationsByUserId(@Argument String userId) {
        try {
            List<NotificationDTO> notifications = webClient.build()
                    .get()
                    .uri("http://localhost:8084/api/notifications/user/{userId}", userId)
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
    public List<NotificationDTO> inAppNotificationsByUserId(@Argument String userId) {
        try {
            List<NotificationDTO> notifications = webClient.build()
                    .get()
                    .uri("http://localhost:8084/api/notifications/in-app/{userId}", userId)
                    .retrieve()
                    .bodyToFlux(NotificationDTO.class)
                    .collectList()
                    .block();
            return notifications != null ? notifications : Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch in-app notifications by user ID from Notification Service: " + e.getMessage(), e);
        }
    }

    // ==================== AUDIT SERVICE QUERIES ====================
    
    @QueryMapping
    public List<AuditEventDTO> auditEvents() {
        try {
            List<AuditEventDTO> events = webClient.build()
                    .get()
                    .uri("http://localhost:8083/audit/events")
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
    public AuditEventDTO auditEventById(@Argument String eventId) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8083/audit/events/{eventId}", eventId)
                    .retrieve()
                    .bodyToMono(AuditEventDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch audit event by ID from Audit Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<AuditEventDTO> auditEventsByUserId(@Argument Long userId) {
        try {
            List<AuditEventDTO> events = webClient.build()
                    .get()
                    .uri("http://localhost:8083/audit/users/{userId}/events", userId)
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
    public List<AuditEventDTO> auditEventsByType(@Argument String eventType) {
        try {
            List<AuditEventDTO> events = webClient.build()
                    .get()
                    .uri("http://localhost:8083/audit/events/type/{eventType}", eventType)
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
    public List<AlertDTO> activeAlerts(@Argument String userId) {
        try {
            List<AlertDTO> alerts = webClient.build()
                    .get()
                    .uri("http://localhost:8087/api/v1/analytics/alerts/active?userId={userId}", userId)
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
    public DashboardSummaryDTO dashboardSummary(@Argument String userId) {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8087/api/v1/analytics/dashboard/summary?userId={userId}", userId)
                    .retrieve()
                    .bodyToMono(DashboardSummaryDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch dashboard summary from Analytics Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<CategoryBreakdownDTO> spendingBreakdown(@Argument String userId, @Argument String period) {
        try {
            String periodParam = period != null ? period : "MONTH";
            List<CategoryBreakdownDTO> breakdown = webClient.build()
                    .get()
                    .uri("http://localhost:8087/api/v1/analytics/spending/breakdown?userId={userId}&period={period}", 
                         userId, periodParam)
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
    public BalanceTrendDTO balanceTrend(@Argument String userId, @Argument Integer days) {
        try {
            int daysParam = days != null ? days : 30;
            return webClient.build()
                    .get()
                    .uri("http://localhost:8087/api/v1/analytics/trends/balance?userId={userId}&days={days}", 
                         userId, daysParam)
                    .retrieve()
                    .bodyToMono(BalanceTrendDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch balance trend from Analytics Service: " + e.getMessage(), e);
        }
    }

    @QueryMapping
    public List<String> recommendations(@Argument String userId) {
        try {
            List<String> recommendations = webClient.build()
                    .get()
                    .uri("http://localhost:8087/api/v1/analytics/insights/recommendations?userId={userId}", userId)
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
    public AdminOverviewDTO adminOverview() {
        try {
            return webClient.build()
                    .get()
                    .uri("http://localhost:8087/api/v1/analytics/admin/overview")
                    .retrieve()
                    .bodyToMono(AdminOverviewDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch admin overview from Analytics Service: " + e.getMessage(), e);
        }
    }
}
