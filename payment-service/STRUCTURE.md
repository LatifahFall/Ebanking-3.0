# Structure détaillée du Payment Service

## Organisation des packages

### `controller/`
Contrôleurs REST pour les endpoints API.

**Fichiers prévus:**
- `PaymentController.java` - Endpoints REST pour la gestion des paiements
- `PaymentRuleController.java` - Endpoints REST pour la gestion des règles (admin)

### `service/`
Logique métier du service.

**Fichiers prévus:**
- `PaymentService.java` - Service principal d'orchestration des paiements
- `FraudDetectionService.java` - Service de détection de fraude
- `PaymentRuleService.java` - Service de gestion des règles de paiement
- `PaymentValidationService.java` - Service de validation des paiements
- `PaymentProcessingService.java` - Service de traitement des paiements (standard/instantané)

### `repository/`
Couche d'accès aux données.

**Fichiers prévus:**
- `PaymentRepository.java` - Repository JPA pour Payment
- `PaymentRuleRepository.java` - Repository JPA pour PaymentRule

### `entity/`
Entités JPA (modèle de données).

**Fichiers prévus:**
- `Payment.java` - Entité Payment
- `PaymentRule.java` - Entité PaymentRule
- `PaymentStatus.java` - Enum pour les statuts
- `PaymentType.java` - Enum pour les types de paiement

### `dto/`
Data Transfer Objects pour les requêtes/réponses API.

**Fichiers prévus:**
- `PaymentRequest.java` - DTO pour créer un paiement
- `PaymentResponse.java` - DTO pour la réponse
- `PaymentListResponse.java` - DTO pour la liste de paiements
- `PaymentRuleRequest.java` - DTO pour créer/modifier une règle

### `config/`
Classes de configuration Spring.

**Fichiers prévus:**
- `KafkaConfig.java` - Configuration Kafka (producers/consumers)
- `SecurityConfig.java` - Configuration Spring Security + Keycloak
- `JpaConfig.java` - Configuration JPA/Hibernate
- `WebClientConfig.java` - Configuration pour les appels HTTP (Account Service)

### `client/`
Clients pour les services externes.

**Fichiers prévus:**
- `AccountServiceClient.java` - Client HTTP pour Account Service
- `Account.java` - DTO pour les données Account

### `kafka/`
Handlers Kafka (producers et consumers).

**Fichiers prévus:**
- `PaymentEventProducer.java` - Producer pour payment.completed, payment.reversed
- `FraudEventProducer.java` - Producer pour fraud.detected
- `AccountEventConsumer.java` - Consumer pour account.created, account.updated
- `PaymentCompletedEvent.java` - DTO pour l'événement payment.completed
- `PaymentReversedEvent.java` - DTO pour l'événement payment.reversed
- `FraudDetectedEvent.java` - DTO pour l'événement fraud.detected
- `AccountCreatedEvent.java` - DTO pour l'événement account.created
- `AccountUpdatedEvent.java` - DTO pour l'événement account.updated

### `exception/`
Gestion des exceptions.

**Fichiers prévus:**
- `GlobalExceptionHandler.java` - Handler global pour les exceptions
- `PaymentNotFoundException.java` - Exception personnalisée
- `PaymentValidationException.java` - Exception de validation
- `FraudDetectedException.java` - Exception pour fraude détectée
- `InsufficientBalanceException.java` - Exception pour solde insuffisant
- `AccountNotFoundException.java` - Exception pour compte non trouvé

## Flux de données

```
Client Request
    ↓
PaymentController
    ↓
PaymentService
    ↓
├── PaymentValidationService → AccountServiceClient → Account Service (HTTP)
├── PaymentRuleService → PaymentRuleRepository → PostgreSQL
├── FraudDetectionService → PaymentRepository → PostgreSQL
└── PaymentProcessingService
    ↓
PaymentRepository → PostgreSQL
    ↓
KafkaProducer → Kafka
```

## Dependencies principales (Maven)

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Keycloak -->
    <dependency>
        <groupId>org.keycloak</groupId>
        <artifactId>keycloak-spring-boot-starter</artifactId>
    </dependency>
    
    <!-- HTTP Client -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- API Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    </dependency>
</dependencies>
```

