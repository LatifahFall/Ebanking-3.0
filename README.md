# Ebanking-3.0
mettre les nom des topics , qui est le producer microservice , qui est consumer 
# Kafka Topics - Architecture Complète


## Topics PRODUITS par Account Service

| Topic | Producer | Consumer(s) |
|-------|----------|-------------|
| `account.created` | **account-service** | notification-service, payment-service, analytics-service, audit-service |
| `account.updated` | **account-service** | payment-service, analytics-service, audit-service |
| `account.balance.changed` | **account-service** | analytics-service, notification-service, audit-service |
| `account.suspended` | **account-service** | payment-service, notification-service, audit-service |
| `account.closed` | **account-service** | payment-service, notification-service, analytics-service, audit-service |

---

## Topics CONSOMMÉS par Account Service

| Topic | Producer | Consumer(s) |
|-------|----------|-------------|
| `payment.completed` | **payment-service** | **account-service**, analytics-service, audit-service |
| `payment.reversed` | **payment-service** | **account-service**, notification-service, audit-service |
| `fraud.detected` | **payment-service** | **account-service**, notification-service, audit-service |

## Topics PRODUITS par User Service

| Topic | Producer | Consumer(s) |
|-------|----------|-------------|
| `user.created` | **user-service** | account-service, audit-service, notification-service |
| `user.updated` | **user-service** | audit-service |
| `user.activated` | **user-service** | audit-service, , notification-service |
| `user.deactivated` | **user-service** | audit-service |
| `client.assigned` | **user-service** | audit-service |
| `client.unassigned` | **user-service** | audit-service |




