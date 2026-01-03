
# Account Service - E-Banking Microservice

Microservice Spring Boot pour la gestion des comptes bancaires avec architecture événementielle (Kafka), PostgreSQL, Redis et Docker.

## 🚀 Technologies

- Java 17
- Spring Boot 3.2.0
- PostgreSQL 15
- Apache Kafka
- Redis (cache)
- Docker & Docker Compose
- Flyway (migrations DB)
- Maven

## 📋 Prérequis

- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Git

## 🏗️ Architecture

```
account-service/
├── src/
│   ├── main/
│   │   ├── java/com/banking/account/
│   │   │   ├── config/          # Configurations (Kafka, Redis, etc.)
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # Custom Exceptions
│   │   │   ├── kafka/           # Kafka Producers/Consumers
│   │   │   │   └── event/       # Event Models
│   │   │   ├── model/           # JPA Entities
│   │   │   ├── repository/      # Spring Data JPA
│   │   │   └── service/         # Business Logic
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/    # Flyway SQL scripts
│   └── test/                    # Unit & Integration Tests
├── docker-compose.dev.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔧 Installation & Démarrage

### 1. Cloner le projet

```bash
git clone <repository-url>
cd account-service
```

### 2. Démarrer avec Docker Compose (recommandé)

```bash
# Démarre tous les services (PostgreSQL, Kafka, Zookeeper, Redis, account-service)
docker-compose up -d

# Voir les logs
docker-compose logs -f account-service

# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes (⚠️ supprime toutes les données)
docker-compose down -v
```

### 3. Mode développement (sans builder l'image du service)

```bash
# Démarrer uniquement les dépendances
docker-compose up -d postgres kafka zookeeper redis

# Compiler et lancer l'application
mvn clean install
mvn spring-boot:run
```

## 📡 APIs REST

**Base URL** : `http://localhost:8081/api/accounts`

### Endpoints

1. **Créer un compte**  
   `POST /api/accounts`  
   ```json
   {
     "userId": 1,
     "accountType": "CHECKING",
     "currency": "EUR",
     "initialBalance": 1000.00
   }
   ```

2. **Récupérer un compte**  
   `GET /api/accounts/{id}`

3. **Liste des comptes par utilisateur**  
   `GET /api/accounts?userId=1`

4. **Modifier un compte** (ex: suspension)  
   `PUT /api/accounts/{id}`  
   ```json
   {
     "status": "SUSPENDED"
   }
   ```

5. **Consulter le solde**  
   `GET /api/accounts/{id}/balance`

6. **Historique des transactions**  
   `GET /api/accounts/{id}/transactions?limit=50`

7. **Relevé de compte**  
   `GET /api/accounts/{id}/statement?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59`

## 📨 Événements Kafka

### Topics consommés (Consumer)
| Topic               | Description |
|---------------------|-----------|
| payment.completed   | Déclenché quand un paiement est effectué |
| payment.reversed    | Déclenché quand un paiement est annulé |
| fraud-detected      | Alerte de fraude (suspension automatique si action = BLOCKED) |

### Topics publiés (Producer)
| Topic                     | Description |
|---------------------------|-----------|
| account.created           | Publié après création d'un compte |
| account.updated           | Publié après modification d'un compte |
| account.balance.changed   | Publié quand le solde change |
| account.suspended         | Publié lors de la suspension d'un compte |
| account.closed            | Publié lors de la clôture d'un compte |

## 🗄️ Base de données

### Tables principales

**accounts**
- id, account_number (UNIQUE), user_id, account_type, currency, balance, status, champs d'audit (suspension/closure), created_at, updated_at

**transactions**
- id, account_id (FK), type (CREDIT/DEBIT), amount, balance_after, reference (UNIQUE pour idempotence), description, created_at

## 🧪 Tests

```bash
# Tests unitaires
mvn test

# Tests avec coverage
mvn clean test jacoco:report

# Tests d'intégration (Testcontainers)
mvn verify
```

## 📊 Monitoring & Observabilité

### Actuator
```bash
curl http://localhost:8082/actuator/health
curl http://localhost:8082/actuator/metrics
curl http://localhost:8082/actuator/prometheus
```

### Kafka UI
http://localhost:8090

## 🔐 Sécurité

- Validation des inputs (Bean Validation)
- Gestion globale des exceptions
- Logs structurés
- Health checks

## 🐛 Troubleshooting

### Kafka ne démarre pas
```bash
docker-compose logs kafka
docker-compose restart kafka
```

### Base de données non accessible
```bash
docker-compose ps postgres
docker exec -it dev-postgres psql -U postgres -d account_db
```

### Port déjà utilisé
Modifier les ports dans `docker-compose.dev.yml` 

## 📝 Variables d'environnement

| Variable                  | Description                  | Défaut       |
|---------------------------|------------------------------|--------------|
| DB_HOST                   | Host PostgreSQL              | localhost    |
| DB_PORT                   | Port PostgreSQL              | 5432         |
| DB_NAME                   | Nom de la BDD                | account_db   |
| DB_USER                   | Utilisateur DB               | postgres     |
| DB_PASSWORD               | Mot de passe DB              | postgres     |
| KAFKA_BOOTSTRAP_SERVERS   | Serveurs Kafka               | localhost:9092 |
| REDIS_HOST                | Host Redis                   | localhost    |
| REDIS_PORT                | Port Redis                   | 6379         |
| SERVER_PORT               | Port du service              | 8081         |



## 📚 Documentation complémentaire

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Kafka](https://spring.io/projects/spring-kafka)
- [Flyway](https://flywaydb.org/)
- [PostgreSQL](https://www.postgresql.org/)



