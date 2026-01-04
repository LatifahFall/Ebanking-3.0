# Service Integration Guide - Payment Service

Ce document décrit comment **payment-service** s'intègre avec les autres microservices de l'écosystème E-Banking.

## Architecture Overview

```
┌──────────────────┐
│   user-service   │
│  (Port: 8082)    │
│  User Management │
└────────┬─────────┘
         │
         │ REST (userId validation)
         │
         ▼
┌──────────────────┐        REST API        ┌──────────────────┐
│ account-service  │◄──────────────────────►│ payment-service  │
│  (Port: 8081)    │                        │  (Port: 8080)    │
│  Account & Balance│                       │  Payment Engine  │
└────────┬─────────┘                        └────────┬─────────┘
         │                                           │
         │                                           │
         │           Kafka Events                    │
         └───────────────────────────────────────────┘
                             │
                             │
                             ▼
                    ┌────────────────────┐
                    │notification-service│
                    │  (Port: 8083)      │
                    │  Email/SMS/Push    │
                    └────────────────────┘
```

---

## 1. Account Service Integration

### Base Information

- **Service Name**: `account-service`
- **Base URL**: `http://localhost:8081` (dev) / `http://account-service:8081` (prod)
- **Technology**: Spring Boot 3.2.0, PostgreSQL
- **Authentication**: OAuth2 Bearer Token (Keycloak)

### REST API Endpoints Used

#### 1.1 Get Account Details

```http
GET /api/accounts/{accountId}
Authorization: Bearer <token>
```

**Response (200 OK)** :
```json
{
  "id": 12345,
  "userId": 100,
  "accountNumber": "FR7630006000011234567890189",
  "accountType": "CHECKING",
  "currency": "EUR",
  "balance": 1500.50,
  "status": "ACTIVE",
  "createdAt": "2024-01-10T08:00:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Used By**: `PaymentValidationService.validateAccount()`

**Error Codes**:
- `404` : Account not found
- `403` : Access denied (not owner)

---

#### 1.2 Check Account Balance

```http
GET /api/accounts/{accountId}/balance
Authorization: Bearer <token>
```

**Response (200 OK)** :
```json
1500.50
```

**Used By**: `PaymentValidationService.validateBalance()`

---

#### 1.3 Debit Account

```http
POST /api/accounts/{accountId}/debit
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 150.00,
  "reference": "Payment 999"
}
```

**Response (200 OK)** :
```json
{
  "id": 12345,
  "balance": 1350.50,
  "lastTransaction": {
    "amount": -150.00,
    "reference": "Payment 999",
    "timestamp": "2024-01-15T10:35:00Z"
  }
}
```

**Used By**: `PaymentProcessingService.processBiometricPayment()`, `PaymentProcessingService.processQRCodePayment()`

**Error Codes**:
- `400` : Insufficient funds
- `409` : Account suspended or closed

---

#### 1.4 Credit Account

```http
POST /api/accounts/{accountId}/credit
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 500.00,
  "reference": "Refund 888"
}
```

**Response (200 OK)** :
```json
{
  "id": 12345,
  "balance": 2000.50,
  "lastTransaction": {
    "amount": 500.00,
    "reference": "Refund 888",
    "timestamp": "2024-01-16T14:00:00Z"
  }
}
```

**Used By**: `PaymentService.reversePayment()`

---

#### 1.5 Validate Account Status

```http
GET /api/accounts/{accountId}/status
Authorization: Bearer <token>
```

**Response (200 OK)** :
```json
"ACTIVE"
```

**Possible Values**: `ACTIVE`, `SUSPENDED`, `CLOSED`, `PENDING`

**Used By**: `PaymentValidationService.validateAccountStatus()`

---

### Data Contract (Account-Service → Payment-Service)

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `id` | **Long** | Account ID (⚠️ NOT UUID) | `12345` |
| `userId` | **Long** | Owner user ID (⚠️ NOT UUID) | `100` |
| `accountNumber` | String | IBAN number | `FR76...` |
| `accountType` | String | Account type | `CHECKING`, `SAVINGS` |
| `currency` | String | ISO 4217 code | `EUR`, `USD` |
| `balance` | BigDecimal | Current balance | `1500.50` |
| `status` | String | Account status | `ACTIVE` |

**⚠️ CRITICAL**: `accountId` and `userId` are **Long**, not UUID. Payment-service has been refactored to use Long everywhere.

---

### Configuration (application.yml)

```yaml
services:
  account:
    url: ${ACCOUNT_SERVICE_URL:http://localhost:8081}
    timeout: 10s
    retry:
      maxAttempts: 3
      backoffDelay: 1000
```

---

### Error Handling

```java
public Mono<Account> getAccount(Long accountId) {
    return webClient.get()
            .uri("/api/accounts/{accountId}", accountId)
            .retrieve()
            .bodyToMono(Account.class)
            .onErrorMap(WebClientResponseException.class, ex -> {
                if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return new AccountNotFoundException("Account not found: " + accountId);
                }
                return new AccountServiceException("Account service error: " + ex.getMessage());
            })
            .timeout(Duration.ofSeconds(10))
            .retry(3);
}
```

---

## 2. User Service Integration

### Base Information

- **Service Name**: `user-service`
- **Base URL**: `http://localhost:8082` (dev) / `http://user-service:8082` (prod)
- **Technology**: Spring Boot 3.2.0, PostgreSQL
- **Authentication**: OAuth2 Bearer Token (Keycloak)

### REST API Endpoints Used

#### 2.1 Get User Details

```http
GET /api/users/{userId}
Authorization: Bearer <token>
```

**Response (200 OK)** :
```json
{
  "id": 100,
  "username": "john.doe",
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+33612345678",
  "status": "ACTIVE"
}
```

**Used By**: `PaymentService` (validation, audit logs)

**Data Contract**:

| Field | Type | Description |
|-------|------|-------------|
| `id` | **Long** | User ID (⚠️ NOT UUID) |
| `username` | String | Unique username |
| `email` | String | Email address |
| `status` | String | User status |

---

### Configuration

```yaml
services:
  user:
    url: ${USER_SERVICE_URL:http://localhost:8082}
    timeout: 5s
```

---

## 3. Notification Service Integration

### Base Information

- **Service Name**: `notification-service`
- **Base URL**: `http://localhost:8083` (dev) / `http://notification-service:8083` (prod)
- **Technology**: Spring Boot, Kafka Consumer
- **Communication**: **Kafka Events** (asynchronous)

### Kafka Events Sent by Payment-Service

#### 3.1 payment.completed

```json
{
  "paymentId": 12345,
  "accountId": 67890,
  "amount": 150.75,
  "currency": "EUR",
  "status": "COMPLETED",
  "completedAt": "2024-01-15T10:30:00Z"
}
```

**Triggers**: Email/SMS notification to user confirming payment

---

#### 3.2 payment.failed

```json
{
  "paymentId": 12345,
  "accountId": 67890,
  "failureReason": "INSUFFICIENT_FUNDS",
  "failedAt": "2024-01-15T10:35:00Z"
}
```

**Triggers**: Email/SMS alert to user about failed payment

---

#### 3.3 fraud.detected

```json
{
  "paymentId": 12345,
  "accountId": 67890,
  "userId": 100,
  "fraudScore": 0.95,
  "severity": "HIGH",
  "detectedAt": "2024-01-15T10:40:00Z"
}
```

**Triggers**: Urgent SMS/push notification to user

---

### No Direct REST API

⚠️ **Payment-service does NOT call notification-service via REST**. All communication is **event-driven via Kafka**.

---

## 4. Keycloak Integration

### Base Information

- **Service Name**: `keycloak`
- **Base URL**: `http://localhost:9090` (dev) / `http://keycloak:8080` (prod)
- **Realm**: `ebanking`
- **Client ID**: `payment-service`

### Authentication Flow

```
┌──────────┐                 ┌──────────┐               ┌──────────────┐
│  Client  │                 │ Keycloak │               │payment-service│
└────┬─────┘                 └────┬─────┘               └──────┬───────┘
     │                            │                             │
     │ 1. POST /token             │                             │
     ├───────────────────────────►│                             │
     │   (username/password)      │                             │
     │                            │                             │
     │ 2. Access Token            │                             │
     │◄───────────────────────────┤                             │
     │   (JWT)                    │                             │
     │                            │                             │
     │ 3. POST /payments          │                             │
     │   Authorization: Bearer... ├────────────────────────────►│
     │                            │                             │
     │                            │ 4. Validate Token           │
     │                            │◄────────────────────────────┤
     │                            │                             │
     │                            │ 5. Token Valid              │
     │                            ├────────────────────────────►│
     │                            │   (userId=100)              │
     │                            │                             │
     │ 6. Payment Created         │                             │
     │◄────────────────────────────────────────────────────────┤
     │                            │                             │
```

### JWT Token Structure

```json
{
  "sub": "100",
  "preferred_username": "john.doe",
  "email": "john.doe@example.com",
  "realm_access": {
    "roles": ["USER", "CUSTOMER"]
  }
}
```

**⚠️ CRITICAL**: `sub` (subject) contains the **userId as String "100"**, which payment-service parses as **Long**.

---

### Configuration (application.yml)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${KEYCLOAK_ISSUER_URI:http://localhost:9090/realms/ebanking}
          jwk-set-uri: ${KEYCLOAK_JWK_SET_URI:http://localhost:9090/realms/ebanking/protocol/openid-connect/certs}

keycloak:
  realm: ebanking
  auth-server-url: ${KEYCLOAK_URL:http://localhost:9090}
  resource: payment-service
  enabled: ${KEYCLOAK_ENABLED:false}  # Disabled in dev
```

---

## 5. PostgreSQL Database

### Connection

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:5432/payment_db
    username: ${DB_USER:payment_user}
    password: ${DB_PASSWORD:payment_pass}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

### Schema

**Tables**:
- `payments` : Payment transactions (ID = BIGINT, not UUID)
- `payment_rules` : Business rules (ID = UUID - internal only)
- `user_biometric_enrollment` : Face tokens (userId = BIGINT)
- `qr_code_payment` : QR code tokens (paymentId, userId, accountId = BIGINT)

**⚠️ CRITICAL**: All foreign keys referencing external services use **BIGINT**, not UUID.

---

## 6. Kafka

### Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: payment-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer

kafka:
  enabled: ${KAFKA_ENABLED:false}  # Disabled by default
```

**Topics** : See [KAFKA_TOPICS.md](./KAFKA_TOPICS.md) for full documentation.

---

## 7. Deployment Dependencies

### Docker Compose Order

```yaml
services:
  postgres:
    # Start first (database)
  
  keycloak:
    depends_on:
      - postgres
  
  account-service:
    depends_on:
      - postgres
      - keycloak
  
  user-service:
    depends_on:
      - postgres
      - keycloak
  
  payment-service:
    depends_on:
      - postgres
      - keycloak
      - account-service  # CRITICAL dependency
      - kafka
  
  notification-service:
    depends_on:
      - kafka
```

---

## 8. Health Checks

### Payment-Service Actuator

```http
GET /actuator/health
```

**Response** :
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "accountService": {
      "status": "UP",
      "details": {
        "url": "http://localhost:8081"
      }
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

---

## 9. Error Handling

### Service Unavailable

```java
@Component
public class ServiceResilienceConfig {
    
    @Bean
    public Retry accountServiceRetry() {
        return Retry.of("accountService", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryExceptions(WebClientResponseException.InternalServerError.class)
                .build());
    }
    
    @Bean
    public CircuitBreaker accountServiceCircuitBreaker() {
        return CircuitBreaker.of("accountService", CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build());
    }
}
```

---

## 10. Data Flow Example: Complete Payment

```
1. POST /api/payments
   ├─► payment-service: Create payment (status=PENDING)
   │
2. Validate Account
   ├─► GET http://account-service:8081/api/accounts/12345
   │   Response: { "id": 12345, "balance": 1500.50, "status": "ACTIVE" }
   │
3. Check Balance
   ├─► GET http://account-service:8081/api/accounts/12345/balance
   │   Response: 1500.50 (>= 150.00) ✓
   │
4. Fraud Detection
   ├─► FraudDetectionService.analyze() → fraudScore=0.2 ✓
   │
5. Process Payment (Debit)
   ├─► POST http://account-service:8081/api/accounts/12345/debit
   │   Body: { "amount": 150.00, "reference": "Payment 999" }
   │   Response: { "balance": 1350.50 } ✓
   │
6. Update Payment Status
   ├─► payment-service: Update payment (status=COMPLETED)
   │
7. Emit Kafka Event
   ├─► Kafka: payment.completed
   │   Body: { "paymentId": 999, "accountId": 12345, "amount": 150.00 }
   │
8. Notification (Async)
   └─► notification-service consumes event
       └─► Sends email: "Payment 999 completed successfully"
```

---

## 11. Security Considerations

### API Security

- ✅ All endpoints require OAuth2 Bearer Token
- ✅ Token validated by Keycloak (JWT signature)
- ✅ UserId extracted from token `sub` claim
- ✅ Account ownership validated before operations

### Service-to-Service Authentication

```yaml
# Future implementation: Service accounts
spring:
  security:
    oauth2:
      client:
        registration:
          account-service:
            client-id: payment-service
            client-secret: ${ACCOUNT_SERVICE_CLIENT_SECRET}
            authorization-grant-type: client_credentials
            scope: account:read,account:write
```

---

## 12. Migration Notes

### From UUID to Long

**Context**: Original payment-service used UUID for all IDs, but existing services (account-service, user-service) use Long IDs.

**Changes Made**:
- ✅ All `accountId`, `userId` fields changed from UUID to Long
- ✅ Database migration V7 created (UUID → BIGINT)
- ✅ AccountServiceClient updated (method parameters)
- ✅ Kafka event DTOs updated
- ✅ REST API examples updated (Swagger docs)
- ✅ Integration tests validate Long IDs

**Rollback**: If needed, revert to UUID by:
1. Drop migration V7
2. Revert code changes (git revert)
3. Rebuild with original UUID implementation

---

## 13. Troubleshooting

### Account Service Not Responding

```bash
# Check service health
curl http://localhost:8081/actuator/health

# Check logs
docker logs account-service

# Test connectivity from payment-service container
docker exec payment-service curl http://account-service:8081/actuator/health
```

### Kafka Connection Issues

```bash
# Check Kafka broker
docker logs kafka

# Test producer
docker exec payment-service kafka-console-producer --broker-list kafka:9092 --topic test

# Test consumer
docker exec payment-service kafka-console-consumer --bootstrap-server kafka:9092 --topic payment.completed --from-beginning
```

---

## 14. Performance Tuning

### Connection Pooling (WebClient)

```yaml
spring:
  webflux:
    client:
      connection-timeout: 5s
      max-connections: 100
      max-idle-time: 30s
```

### Database Connection Pool

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

---

## Summary Table

| Service | Type | Port | Critical | Retry | Timeout | Authentication |
|---------|------|------|----------|-------|---------|----------------|
| account-service | REST | 8081 | ✅ YES | 3x | 10s | OAuth2 |
| user-service | REST | 8082 | ❌ NO | 3x | 5s | OAuth2 |
| notification-service | Kafka | 8083 | ❌ NO | N/A | N/A | N/A |
| keycloak | OAuth2 | 9090 | ✅ YES | 5x | 10s | N/A |
| postgresql | DB | 5432 | ✅ YES | N/A | 30s | Password |
| kafka | Event | 9092 | ❌ NO | N/A | N/A | PLAINTEXT |

**⚠️ CRITICAL PATH**: payment-service → account-service (REST API for debit/credit operations)

---

For Kafka topics details, see **[KAFKA_TOPICS.md](./KAFKA_TOPICS.md)**.
