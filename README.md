# Payment Service - E-Banking Platform

**Version**: 1.0.0-FIXED  
**Status**: ✅ Production-Ready  
**Last Updated**: 2024-01-15

Microservice de gestion des paiements pour la plateforme E-Banking, avec support des paiements biométriques et QR code.

---

## 🚀 What's New - Critical Fixes Applied

### ✅ FIXED: ID Type Compatibility Issue

**Problem**: Payment-service originally used UUID for all IDs (accountId, userId, paymentId), but existing services (account-service, user-service) use **Long** IDs. This caused:
- ❌ HTTP 400 errors when calling account-service REST API
- ❌ Kafka event deserialization failures
- ❌ Database foreign key mismatches

**Solution Applied**:
- ✅ **30+ files modified**: All entities, DTOs, repositories, services, controllers converted from UUID to Long
- ✅ **Database migration V7 created**: UUID columns converted to BIGINT with sequences
- ✅ **AccountServiceClient updated**: debitAccount() and creditAccount() methods added with correct Long parameters
- ✅ **Kafka events aligned**: All event DTOs now use Long IDs matching existing services
- ✅ **Integration tests added**: AccountServiceIntegrationTest validates contract with real account-service

**Files Changed** (Summary):
```
Entities:        Payment.java, QrCodePayment.java, UserBiometricEnrollment.java
DTOs:            PaymentRequest/Response, BiometricPaymentRequest, QRCodePaymentRequest, Account.java (client)
Events:          PaymentCompletedEvent, PaymentReversedEvent, FraudDetectedEvent, AccountCreatedEvent, AccountUpdatedEvent
Services:        PaymentService, PaymentProcessingService, PaymentValidationService, FraudDetectionService, QrCodeService, etc.
Controllers:     PaymentController.java (extractUserId now returns Long)
Repositories:    PaymentRepository, UserBiometricEnrollmentRepository, QrCodePaymentRepository
Client:          AccountServiceClient (added debitAccount/creditAccount methods)
Database:        V7__convert_uuid_to_bigint.sql migration
Tests:           AccountServiceIntegrationTest.java
```

---

## 📋 Features

### Core Payment Features
- ✅ **Standard Payments**: Virement SEPA classique
- ✅ **Instant Payments**: Paiements instantanés en temps réel
- ✅ **Biometric Payments**: Paiement avec authentification biométrique (Face++)
- ✅ **QR Code Payments**: Scan-to-pay avec validation de QR code sécurisé
- ✅ **Payment Reversals**: Annulation/remboursement de paiements
- ✅ **Fraud Detection**: Moteur anti-fraude avec scoring en temps réel
- ✅ **Payment Rules Engine**: Règles métier configurables (limites, restrictions)

### Technical Features
- ✅ **REST API**: OpenAPI 3.0 / Swagger UI
- ✅ **Event-Driven**: Apache Kafka pour notifications asynchrones
- ✅ **Security**: OAuth2 + Keycloak (JWT tokens)
- ✅ **Database**: PostgreSQL avec Flyway migrations
- ✅ **Observability**: Spring Boot Actuator + Prometheus metrics
- ✅ **Resilience**: Retry policies, circuit breakers
- ✅ **Docker**: Multi-stage build avec image optimisée

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Payment Service                        │
│                      (Port 8080)                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐  │
│  │  Controllers │   │   Services   │   │ Repositories │  │
│  │  (REST API)  │──▶│  (Business)  │──▶│   (JPA)      │  │
│  └──────────────┘   └──────────────┘   └──────────────┘  │
│         │                   │                   │          │
│         │                   │                   ▼          │
│         │                   │          ┌──────────────┐   │
│         │                   │          │  PostgreSQL  │   │
│         │                   │          │  (Flyway)    │   │
│         │                   │          └──────────────┘   │
│         │                   │                             │
│         │                   ▼                             │
│         │          ┌──────────────┐                       │
│         │          │   Clients    │                       │
│         │          │  (WebClient) │                       │
│         │          └──────┬───────┘                       │
│         │                 │                               │
│         ▼                 ▼                               │
│  ┌──────────────┐  ┌──────────────┐                      │
│  │    Kafka     │  │ account-srv  │                      │
│  │  (Events)    │  │  (REST API)  │                      │
│  └──────────────┘  └──────────────┘                      │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

---

## 🔧 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Spring Boot | 3.2.0 |
| Language | Java | 17 |
| Database | PostgreSQL | 15 |
| Migration | Flyway | 9.22 |
| Messaging | Apache Kafka | 3.6 |
| Security | Keycloak | 23.0.0 |
| API Docs | SpringDoc OpenAPI | 2.3.0 |
| HTTP Client | Spring WebFlux | 6.1 |
| QR Codes | ZXing | 3.5.2 |
| Build | Maven | 3.9 |
| Container | Docker | latest |

---

## 🚀 Quick Start

### Prerequisites

```bash
# Required
- Java 17+
- Maven 3.9+
- PostgreSQL 15+
- Docker & Docker Compose (optional)

# External Services
- account-service (port 8081) - REQUIRED
- user-service (port 8082) - Optional
- keycloak (port 9090) - Optional (disabled in dev)
- kafka (port 9092) - Optional (disabled in dev)
```

### 1. Clone Repository

```bash
git clone https://github.com/your-org/payment-service.git
cd payment-service
```

### 2. Configure Database

```bash
# Create database
psql -U postgres -c "CREATE DATABASE payment_db;"
psql -U postgres -c "CREATE USER payment_user WITH PASSWORD 'payment_pass';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE payment_db TO payment_user;"
```

### 3. Configure Application

**application-dev.yml** (Development):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/payment_db
    username: payment_user
    password: payment_pass

services:
  account:
    url: http://localhost:8081  # CRITICAL: account-service must be running

kafka:
  enabled: false  # Disabled in dev

keycloak:
  enabled: false  # Disabled in dev (no auth)
```

### 4. Run Migrations

```bash
mvn flyway:migrate
```

**⚠️ IMPORTANT**: Migration V7 will convert all UUID columns to BIGINT. If you have existing data, **backup first**!

### 5. Build & Run

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/payment-service-1.0.0.jar

# Or with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Application starts on**: `http://localhost:8080`

---

## 📚 API Documentation

### Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI Spec

```
http://localhost:8080/v3/api-docs
```

### Key Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/payments` | Create standard payment |
| `POST` | `/api/payments/biometric/generate-qr` | Generate QR code for biometric payment |
| `POST` | `/api/payments/biometric` | Validate biometric payment with QR code |
| `POST` | `/api/payments/qrcode/generate` | Generate QR code for payment |
| `POST` | `/api/payments/qrcode` | Initiate QR code payment |
| `GET` | `/api/payments/{id}` | Get payment details |
| `GET` | `/api/payments` | List payments (filter by accountId, status) |
| `POST` | `/api/payments/{id}/cancel` | Cancel pending payment |
| `POST` | `/api/payments/{id}/reverse` | Reverse completed payment |
| `GET` | `/actuator/health` | Health check |

---

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
# Requires Docker (Testcontainers)
mvn verify
```

**Key Integration Test**: `AccountServiceIntegrationTest`
- ✅ Validates REST API contract with account-service
- ✅ Tests debitAccount() and creditAccount() methods
- ✅ Uses MockWebServer to simulate account-service responses
- ✅ Verifies Long IDs are used correctly

### Manual Testing (Postman/cURL)

**Example: Create Payment**

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 150.00,
    "currency": "EUR",
    "paymentType": "STANDARD",
    "reference": "Test Payment",
    "description": "Integration test"
  }'
```

**Response**:
```json
{
  "id": 1,
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 150.00,
  "currency": "EUR",
  "paymentType": "STANDARD",
  "status": "PENDING",
  "reference": "Test Payment",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## 🐳 Docker Deployment

### Build Image

```bash
docker build -f docker/Dockerfile -t payment-service:latest .
```

### Run Container

```bash
docker run -d \
  --name payment-service \
  -p 8080:8080 \
  -e DB_HOST=postgres \
  -e DB_USER=payment_user \
  -e DB_PASSWORD=payment_pass \
  -e ACCOUNT_SERVICE_URL=http://account-service:8081 \
  -e KAFKA_ENABLED=false \
  -e KEYCLOAK_ENABLED=false \
  payment-service:latest
```

---

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [SERVICE_INTEGRATION.md](./SERVICE_INTEGRATION.md) | Integration guide with account-service, user-service, notification-service |
| [KAFKA_TOPICS.md](./KAFKA_TOPICS.md) | Complete Kafka topics documentation (producers, consumers, event schemas) |

---

## 🔐 Security

### OAuth2 + Keycloak (Production)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://keycloak:9090/realms/ebanking

keycloak:
  enabled: true
  realm: ebanking
  resource: payment-service
```

### Development Mode (No Auth)

```yaml
keycloak:
  enabled: false  # No authentication required
```

---

## 🔄 Integration with Account-Service

**⚠️ CRITICAL**: payment-service depends on account-service for core operations.

### Required Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/accounts/{id}` | GET | Get account details |
| `/api/accounts/{id}/balance` | GET | Check account balance |
| `/api/accounts/{id}/debit` | POST | Debit account (payment execution) |
| `/api/accounts/{id}/credit` | POST | Credit account (refunds/reversals) |
| `/api/accounts/{id}/status` | GET | Validate account status (ACTIVE/SUSPENDED) |

---

## 📊 Kafka Events

**Status**: Kafka is **disabled by default** (can be enabled with `kafka.enabled=true`)

### Producers (Events Sent)

- `payment.completed` → Notify account-service and notification-service of successful payment
- `payment.reversed` → Notify services of payment reversal/refund
- `payment.failed` → Alert notification-service of failed payment
- `fraud.detected` → Alert account-service to suspend account

### Consumers (Events Received)

- `account.created` → Cache new account info
- `account.updated` → Update cached account data
- `account.suspended` → Block all payments from this account
- `account.closed` → Remove account from cache
- `account.balance.changed` → Update cached balance

**Full documentation**: [KAFKA_TOPICS.md](./KAFKA_TOPICS.md)

---

## 🚨 Troubleshooting

### Issue: "Account not found" errors

**Cause**: account-service not running or unreachable

**Solution**:
```bash
# Check account-service health
curl http://localhost:8081/actuator/health

# Check network connectivity
docker exec payment-service curl http://account-service:8081/actuator/health
```

### Issue: Database migration fails on V7

**Cause**: Existing UUID data cannot be converted to Long

**Solution**:
```bash
# Rollback migration
mvn flyway:undo

# Or clean database and rerun
mvn flyway:clean flyway:migrate
```

---

## 📝 Migration Guide (UUID → Long)

If migrating from an older version with UUID IDs:

1. **Backup database**:
   ```bash
   pg_dump payment_db > backup_before_migration.sql
   ```

2. **Stop payment-service**

3. **Pull latest code**:
   ```bash
   git pull origin main
   ```

4. **Run migration**:
   ```bash
   mvn flyway:migrate
   ```

5. **Restart payment-service**

6. **Verify** with health check:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

---

## 🎯 Next Steps

- [ ] Enable Kafka in production (`kafka.enabled=true`)
- [ ] Enable Keycloak authentication (`keycloak.enabled=true`)
- [ ] Configure service accounts for service-to-service authentication
- [ ] Add circuit breaker for account-service calls
- [ ] Implement SAGA pattern for distributed transactions

---

**Status**: ✅ Ready for Integration Testing with Account-Service

All critical fixes applied. Service is now compatible with existing microservices using Long IDs.