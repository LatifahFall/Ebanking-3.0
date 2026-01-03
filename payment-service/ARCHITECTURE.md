# Architecture - Payment Service

## Vue d'ensemble

Le Payment Service est responsable de la gestion des paiements et virements bancaires. Il gère le cycle de vie complet des paiements, de l'initiation à la complétion, incluant la détection de fraude et l'application de règles métier.

## Diagramme d'architecture

```mermaid
graph TB
    subgraph "External Services"
        API_GW[API Gateway]
        ACCOUNT_SVC[Account Service]
        KEYCLOAK[Keycloak OAuth2/OIDC]
    end

    subgraph "Payment Service"
        subgraph "API Layer"
            REST_API[REST Controller]
            EXCEPTION[Exception Handler]
        end

        subgraph "Service Layer"
            PAYMENT_SVC[PaymentService]
            FRAUD_SVC[FraudDetectionService]
            RULE_SVC[PaymentRuleService]
            VALIDATION_SVC[PaymentValidationService]
        end

        subgraph "Integration Layer"
            ACCOUNT_CLIENT[AccountServiceClient]
            KAFKA_PROD[KafkaProducer]
            KAFKA_CONS[KafkaConsumer]
        end

        subgraph "Domain Layer"
            PAYMENT_ENTITY[Payment Entity]
            PAYMENT_RULE[PaymentRule Entity]
            INSTANT_PAYMENT[InstantPayment Entity]
        end

        subgraph "Repository Layer"
            PAYMENT_REPO[PaymentRepository]
            RULE_REPO[PaymentRuleRepository]
        end
    end

    subgraph "Infrastructure"
        POSTGRES[(PostgreSQL)]
        KAFKA[Kafka Event Bus]
    end

    API_GW --> REST_API
    REST_API --> PAYMENT_SVC
    REST_API --> EXCEPTION

    PAYMENT_SVC --> FRAUD_SVC
    PAYMENT_SVC --> RULE_SVC
    PAYMENT_SVC --> VALIDATION_SVC
    PAYMENT_SVC --> ACCOUNT_CLIENT
    PAYMENT_SVC --> KAFKA_PROD
    PAYMENT_SVC --> PAYMENT_REPO

    FRAUD_SVC --> PAYMENT_REPO
    RULE_SVC --> RULE_REPO

    ACCOUNT_CLIENT --> ACCOUNT_SVC
    KAFKA_PROD --> KAFKA
    KAFKA_CONS --> KAFKA
    KAFKA_CONS --> PAYMENT_SVC

    PAYMENT_REPO --> POSTGRES
    RULE_REPO --> POSTGRES

    REST_API --> KEYCLOAK

    style PAYMENT_SVC fill:#e1f5ff
    style FRAUD_SVC fill:#fff4e1
    style KAFKA_PROD fill:#e8f5e9
    style KAFKA_CONS fill:#e8f5e9
```

## Diagramme de classe détaillé

```mermaid
classDiagram
    class PaymentService {
        -PaymentRepository paymentRepository
        -PaymentRuleService ruleService
        -FraudDetectionService fraudService
        -PaymentValidationService validationService
        -AccountServiceClient accountClient
        -KafkaProducer kafkaProducer
        +initiatePayment(request) Payment
        +processPayment(paymentId) Payment
        +cancelPayment(paymentId) Payment
        +reversePayment(paymentId, reason) Payment
        +getPayment(paymentId) Payment
        +getPaymentsByAccount(accountId, filters) List~Payment~
    }

    class Payment {
        +UUID id
        +UUID fromAccountId
        +UUID toAccountId
        +BigDecimal amount
        +String currency
        +PaymentType type
        +PaymentStatus status
        +String beneficiaryName
        +String reference
        +ZonedDateTime timestamp
        +Map metadata
        +validate()
        +execute()
        +cancel()
    }

    class InstantPayment {
        +UUID id
        +Payment payment
        +Boolean isInstant
        +processInstant()
    }

    class FraudDetectionService {
        -PaymentRepository paymentRepository
        +analyzeTransaction(payment) FraudAnalysisResult
        +checkBlacklist(accountId) Boolean
        +detectAnomaly(payment) Boolean
        +blockTransaction(paymentId)
    }

    class PaymentRuleService {
        -PaymentRuleRepository ruleRepository
        +evaluatePayment(payment) ValidationResult
        +getActiveRules() List~PaymentRule~
        +createRule(rule) PaymentRule
    }

    class PaymentRule {
        +UUID id
        +String ruleType
        +Map conditions
        +Boolean enabled
        +ZonedDateTime createdAt
        +evaluate(payment) Boolean
    }

    class PaymentValidationService {
        -AccountServiceClient accountClient
        +validateAccount(accountId) ValidationResult
        +validateBalance(accountId, amount) ValidationResult
        +validatePaymentRequest(request) ValidationResult
    }

    class AccountServiceClient {
        +String baseUrl
        +getAccount(accountId) Account
        +checkBalance(accountId) BigDecimal
        +validateAccountStatus(accountId) Boolean
    }

    class KafkaProducer {
        +publishPaymentCompleted(payment)
        +publishPaymentReversed(payment, reason)
        +publishFraudDetected(fraudEvent)
    }

    class KafkaConsumer {
        +consumeAccountCreated(event)
        +consumeAccountUpdated(event)
    }

    PaymentService --> Payment : manages
    PaymentService --> FraudDetectionService : uses
    PaymentService --> PaymentRuleService : uses
    PaymentService --> PaymentValidationService : uses
    PaymentService --> AccountServiceClient : uses
    PaymentService --> KafkaProducer : uses
    PaymentService --> KafkaConsumer : uses

    Payment --> InstantPayment : extends
    FraudDetectionService --> Payment : analyzes
    PaymentRuleService --> PaymentRule : evaluates
```

## Flux de traitement d'un paiement

```mermaid
sequenceDiagram
    participant Client
    participant API as REST API
    participant PS as PaymentService
    participant VS as ValidationService
    participant RS as RuleService
    participant FS as FraudService
    participant AC as AccountClient
    participant KP as KafkaProducer
    participant K as Kafka

    Client->>API: POST /payments
    API->>PS: initiatePayment(request)
    
    PS->>VS: validatePaymentRequest()
    VS->>AC: validateAccount(fromAccountId)
    AC-->>VS: Account valid
    VS-->>PS: Validation OK

    PS->>RS: evaluatePayment(payment)
    RS-->>PS: Rules passed

    PS->>FS: analyzeTransaction(payment)
    alt Fraude détectée
        FS-->>PS: Fraud detected
        PS->>KP: publishFraudDetected()
        KP->>K: fraud.detected
        PS-->>API: Payment blocked
    else Pas de fraude
        FS-->>PS: Analysis OK
        
        PS->>PS: Save payment (PENDING)
        
        alt Paiement instantané
            PS->>PS: processInstantPayment()
        else Paiement standard
            PS->>PS: processStandardPayment()
        end

        PS->>PS: Update payment (COMPLETED)
        PS->>KP: publishPaymentCompleted()
        KP->>K: payment.completed
        PS-->>API: Payment completed
        API-->>Client: 201 Created
    end
```

## Topics Kafka

### Producer Topics

1. **payment.completed**
   - Publié quand un paiement est complété avec succès
   - Consommé par: Account Service, Notification Service

2. **payment.reversed**
   - Publié quand un paiement est annulé/reversé
   - Consommé par: Account Service

3. **fraud.detected**
   - Publié quand une fraude est détectée
   - Consommé par: Notification Service

### Consumer Topics

1. **account.created**
   - Consommé pour maintenir un cache local des comptes valides

2. **account.updated**
   - Consommé pour bloquer les paiements si compte suspendu/inactif

## Technologies

- **Language**: Java (Spring Boot)
- **Database**: PostgreSQL
- **Messaging**: Apache Kafka
- **Authentication**: Keycloak (OAuth2/OIDC)
- **API**: REST API
- **Containerization**: Docker
- **Orchestration**: Kubernetes

## Structure de données

### Payment Entity

```sql
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    from_account_id UUID NOT NULL,
    to_account_id UUID,
    amount DECIMAL(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    payment_type VARCHAR(20) NOT NULL, -- STANDARD, INSTANT, RECURRING
    status VARCHAR(20) NOT NULL, -- PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED
    beneficiary_name VARCHAR(255),
    reference VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    reversed_at TIMESTAMP,
    reversal_reason VARCHAR(255)
);

CREATE INDEX idx_payments_from_account ON payments(from_account_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);
```

### PaymentRule Entity

```sql
CREATE TABLE payment_rules (
    id UUID PRIMARY KEY,
    rule_type VARCHAR(50) NOT NULL,
    conditions JSONB NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_payment_rules_enabled ON payment_rules(enabled);
```

## Sécurité

- Authentification OAuth2/OIDC via Keycloak
- Validation des permissions par rôle
- Chiffrement des données sensibles
- Audit trail de toutes les opérations
- Rate limiting pour prévenir les abus

## Observabilité

- Health checks: `/actuator/health`
- Métriques Prometheus: `/actuator/metrics`
- Logging structuré (JSON)
- Tracing distribué (Jaeger)
- Alertes sur erreurs critiques

