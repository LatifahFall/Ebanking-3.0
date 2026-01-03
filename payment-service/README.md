# Payment Service

Service de gestion des paiements et virements bancaires pour E-Banking 3.0.

## Vue d'ensemble

Le Payment Service gère le cycle de vie complet des paiements, incluant:
- Initiation et traitement des paiements
- Virements standards et instantanés
- Détection de fraude
- Application de règles métier
- Intégration avec Account Service
- Publication d'événements Kafka

## Structure du projet

```
payment-service/
├── src/
│   ├── main/
│   │   ├── java/com/ebanking/payment/
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── service/             # Business Logic
│   │   │   ├── repository/          # Data Access Layer
│   │   │   ├── entity/              # JPA Entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── config/              # Configuration Classes
│   │   │   ├── client/              # External Service Clients
│   │   │   ├── kafka/               # Kafka Producers & Consumers
│   │   │   └── exception/           # Exception Handlers
│   │   └── resources/
│   │       ├── application.yml      # Application Configuration
│   │       └── db/migration/        # Flyway/Liquibase Migrations
│   └── test/
│       └── java/com/ebanking/payment/
│           └── [test packages]
├── docker/
│   └── Dockerfile
├── ARCHITECTURE.md                  # Architecture détaillée
└── README.md                        # Ce fichier
```

## Couches de l'architecture

### Controller Layer
Gère les endpoints REST et la validation des requêtes HTTP.

### Service Layer
- **PaymentService**: Orchestration principale des paiements
- **FraudDetectionService**: Détection de fraude
- **PaymentRuleService**: Gestion des règles métier
- **PaymentValidationService**: Validation des paiements

### Repository Layer
Accès aux données PostgreSQL via JPA/Hibernate.

### Client Layer
- **AccountServiceClient**: Communication avec Account Service (HTTP REST)

### Kafka Layer
- **KafkaProducer**: Publication d'événements (payment.completed, payment.reversed, fraud.detected)
- **KafkaConsumer**: Consommation d'événements (account.created, account.updated)

## Technologies

- **Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA / Hibernate
- **Messaging**: Spring Kafka
- **Security**: Spring Security + Keycloak
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven ou Gradle

## Endpoints API

### Payment Management
- `POST /api/payments` - Initier un paiement
- `GET /api/payments/{id}` - Obtenir un paiement
- `GET /api/payments` - Lister les paiements (avec filtres)
- `POST /api/payments/{id}/cancel` - Annuler un paiement
- `POST /api/payments/{id}/reverse` - Reverser un paiement

### Health & Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Métriques Prometheus

## Topics Kafka

### Producer
- `payment.completed` - Paiement complété
- `payment.reversed` - Paiement reversé
- `fraud.detected` - Fraude détectée

### Consumer
- `account.created` - Nouveau compte créé
- `account.updated` - Compte modifié

Voir ARCHITECTURE.md pour plus de détails.

## Développement

### Prérequis
- Java 17+
- Maven 3.8+ ou Gradle 7+
- PostgreSQL 14+
- Kafka 3.x
- Docker (optionnel)

### Configuration

Copier `application.yml.example` vers `application.yml` et configurer:
- Database connection
- Kafka brokers
- Account Service URL
- Keycloak configuration

### Exécution

```bash
# Avec Maven
mvn spring-boot:run

# Avec Gradle
./gradlew bootRun

# Avec Docker
docker-compose up
```

## Tests

```bash
# Tests unitaires
mvn test

# Tests d'intégration
mvn verify
```

## Documentation

Voir [ARCHITECTURE.md](./ARCHITECTURE.md) pour l'architecture détaillée.

