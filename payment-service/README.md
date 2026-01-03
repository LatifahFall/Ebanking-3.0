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

### Variables d'environnement

Voir [ENV_VARIABLES.md](./ENV_VARIABLES.md) pour la liste complète des variables d'environnement.

### Configuration locale

Copier `application.yml.example` vers `application.yml` et configurer:
- Database connection
- Kafka brokers
- Account Service URL
- Keycloak configuration

### Démarrage rapide

1. **Prérequis locaux :**
   ```bash
   # Démarrer PostgreSQL
   docker run -d --name postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=payment_db -p 5432:5432 postgres:14
   
   # Démarrer Kafka
   docker-compose -f docker-compose.yml up kafka zookeeper
   ```

2. **Exécution avec Maven :**
   ```bash
   mvn clean install
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Exécution avec Docker :**
   ```bash
   docker build -f docker/Dockerfile -t payment-service:latest .
   docker run -p 8080:8080 \
     -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/payment_db \
     -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
     payment-service:latest
   ```

### Profils disponibles

- `dev` : Développement (SQL logging activé, Swagger activé)
- `test` : Tests (H2 in-memory database)
- `prod` : Production (optimisations activées, Swagger désactivé)

## API Documentation

Une fois le service démarré, accéder à la documentation Swagger :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **OpenAPI JSON** : http://localhost:8080/v3/api-docs

### Exemples d'utilisation API

#### Initier un paiement

```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
    "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
    "amount": 100.50,
    "currency": "EUR",
    "paymentType": "STANDARD",
    "reference": "PAY-001",
    "description": "Payment for services"
  }'
```

#### Obtenir un paiement

```bash
curl -X GET http://localhost:8080/api/payments/123e4567-e89b-12d3-a456-426614174000 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Lister les paiements

```bash
curl -X GET "http://localhost:8080/api/payments?accountId=123e4567-e89b-12d3-a456-426614174000&status=COMPLETED&page=0&size=20" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Annuler un paiement

```bash
curl -X POST http://localhost:8080/api/payments/123e4567-e89b-12d3-a456-426614174000/cancel \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### Reverser un paiement

```bash
curl -X POST "http://localhost:8080/api/payments/123e4567-e89b-12d3-a456-426614174000/reverse?reason=CUSTOMER_REQUEST" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## Health Checks

Le service expose des endpoints de santé via Spring Boot Actuator :

- **Health Check** : `GET /actuator/health`
- **Metrics** : `GET /actuator/metrics`
- **Prometheus** : `GET /actuator/prometheus`

### Vérification de santé

```bash
curl http://localhost:8080/actuator/health
```

Réponse attendue :
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

## Tests

```bash
# Tests unitaires
mvn test

# Tests d'intégration
mvn verify

# Tests avec couverture
mvn test jacoco:report
```

## Documentation

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Architecture détaillée du service
- [ENV_VARIABLES.md](./ENV_VARIABLES.md) - Variables d'environnement
- [STRUCTURE.md](./STRUCTURE.md) - Structure des dossiers et packages

