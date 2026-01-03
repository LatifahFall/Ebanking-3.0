# Environment Variables

This document describes all environment variables used by the Payment Service.

## Database Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/payment_db` | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | - | Yes |
| `DATABASE_POOL_SIZE` | Maximum connection pool size | `20` | No |
| `DATABASE_POOL_MIN_IDLE` | Minimum idle connections | `10` | No |
| `DATABASE_CONNECTION_TIMEOUT` | Connection timeout (ms) | `30000` | No |
| `DATABASE_IDLE_TIMEOUT` | Idle timeout (ms) | `600000` | No |
| `DATABASE_MAX_LIFETIME` | Maximum connection lifetime (ms) | `1800000` | No |

## Kafka Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker addresses | `localhost:9092` | Yes |
| `KAFKA_CONSUMER_GROUP_ID` | Consumer group ID | `payment-service-group` | Yes |
| `KAFKA_PAYMENT_COMPLETED_TOPIC` | Topic for payment.completed events | `payment.completed` | No |
| `KAFKA_PAYMENT_REVERSED_TOPIC` | Topic for payment.reversed events | `payment.reversed` | No |
| `KAFKA_FRAUD_DETECTED_TOPIC` | Topic for fraud.detected events | `fraud.detected` | No |
| `KAFKA_ACCOUNT_CREATED_TOPIC` | Topic for account.created events | `account.created` | No |
| `KAFKA_ACCOUNT_UPDATED_TOPIC` | Topic for account.updated events | `account.updated` | No |

## Account Service Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `ACCOUNT_SERVICE_URL` | Account Service base URL | `http://localhost:8081` | Yes |
| `ACCOUNT_SERVICE_CONNECT_TIMEOUT` | Connection timeout (ms) | `5000` | No |
| `ACCOUNT_SERVICE_READ_TIMEOUT` | Read timeout (ms) | `10000` | No |

## Keycloak Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `KEYCLOAK_REALM` | Keycloak realm name | `ebanking` | Yes |
| `KEYCLOAK_AUTH_SERVER_URL` | Keycloak server URL | `http://localhost:8080/auth` | Yes |
| `KEYCLOAK_RESOURCE` | Client ID | `payment-service` | Yes |
| `KEYCLOAK_CREDENTIALS_SECRET` | Client secret | - | Yes (production) |
| `KEYCLOAK_SSL_REQUIRED` | Require SSL | `all` (prod), `none` (dev) | No |

## Server Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SERVER_PORT` | HTTP server port | `8080` | No |
| `SPRING_PROFILES_ACTIVE` | Active profiles (comma-separated) | `dev` | No |

## Logging Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `LOG_LEVEL` | Root log level | `INFO` | No |
| `LOG_LEVEL_PAYMENT` | Payment service log level | `DEBUG` | No |
| `LOG_LEVEL_KAFKA` | Kafka log level | `INFO` | No |
| `LOG_LEVEL_WEB` | Web log level | `INFO` | No |
| `LOG_FILE` | Log file path | `/var/log/payment-service/application.log` | No |

## Actuator Configuration

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `ACTUATOR_HEALTH_SHOW_DETAILS` | Show health details | `when-authorized` | No |

## Example Configuration File

Create a `.env` file or export these variables:

```bash
# Database
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payment_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password

# Kafka
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export KAFKA_CONSUMER_GROUP_ID=payment-service-group

# Account Service
export ACCOUNT_SERVICE_URL=http://account-service:8081

# Keycloak
export KEYCLOAK_REALM=ebanking
export KEYCLOAK_AUTH_SERVER_URL=http://keycloak:8080/auth
export KEYCLOAK_RESOURCE=payment-service
export KEYCLOAK_CREDENTIALS_SECRET=your_secret

# Server
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=prod

# Logging
export LOG_LEVEL=INFO
export LOG_FILE=/var/log/payment-service/application.log
```

## Docker/Kubernetes

When deploying with Docker or Kubernetes, set these as environment variables in your container configuration.

