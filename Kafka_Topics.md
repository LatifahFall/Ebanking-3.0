# 📡 Kafka Topics - Mapping Complet Banking 3.0

## 🎯 Vue d'ensemble

Ce document décrit **TOUS** les topics Kafka utilisés dans l'architecture et leur mapping dans les services **audit-service** et **analytics-service**.

---

## 📊 Tableau Récapitulatif Global

| # | Topic Kafka | Producer Service | Consumers | Description |
|---|-------------|------------------|-----------|-------------|
| 1 | `auth.events` | auth-service | audit, analytics, notification | Événements d'authentification |
| 2 | `user.created` | user-service | audit, analytics, notification, account | Création utilisateur |
| 3 | `user.updated` | user-service | audit, analytics | Mise à jour utilisateur |
| 4 | `user.activated` | user-service | audit, analytics, notification | Activation compte |
| 5 | `user.deactivated` | user-service | audit, analytics | Désactivation compte |
| 6 | `kyc.status.changed` | user-service | audit, analytics, notification | Changement statut KYC |
| 7 | `client.assigned` | user-service | audit, analytics | Assignation client à agent |
| 8 | `client.unassigned` | user-service | audit, analytics | Désassignation client |
| 9 | `account.created` | account-service | audit, analytics, notification, payment | Création compte |
| 10 | `account.updated` | account-service | audit, analytics, payment | Mise à jour compte |
| 11 | `account.balance.changed` | account-service | audit, analytics, notification | Changement solde |
| 12 | `account.suspended` | account-service | audit, analytics, notification, payment | Suspension compte |
| 13 | `account.closed` | account-service | audit, analytics, notification, payment | Fermeture compte |
| 14 | `transaction.completed` | payment-service | audit, analytics, notification | Transaction terminée |
| 15 | `payment.initiated` | payment-service | audit, analytics | Paiement initié |
| 16 | `payment.completed` | payment-service | audit, analytics, notification, account | Paiement terminé |
| 17 | `payment.failed` | payment-service | audit, analytics, notification | Paiement échoué |
| 18 | `payment.reversed` | payment-service | audit, analytics, notification, account | Paiement annulé |
| 19 | `fraud.detected` | payment-service, analytics | audit, analytics, notification, account | Fraude détectée |
| 20 | `crypto.transaction` | crypto-service | audit, analytics, notification | Transaction crypto |
| 21 | `notification.status` | notification-service | audit, analytics | Statut notification |
| 22 | `notification.audit` | notification-service | audit, analytics | Audit notification |

---

## 🔍 Détail par Service

### 1️⃣ AUDIT-SERVICE (Consumer)

#### Configuration (application.yml)
```yaml
audit:
  topics:
    # Auth Service
    auth-events: auth.events
    
    # User Service
    user-created: user.created
    user-updated: user.updated
    user-activated: user.activated
    user-deactivated: user.deactivated
    kyc-status-changed: kyc.status.changed
    client-assigned: client.assigned
    client-unassigned: client.unassigned
    
    # Account Service
    account-created: account.created
    account-updated: account.updated
    account-balance-changed: account.balance.changed
    account-suspended: account.suspended
    account-closed: account.closed
    
    # Payment Service
    transaction-completed: transaction.completed
    payment-initiated: payment.initiated
    payment-completed: payment.completed
    payment-failed: payment.failed
    payment-reversed: payment.reversed
    fraud-detected: fraud.detected
    
    # Crypto Service
    crypto-transaction: crypto.transaction
    
    # Notification Service
    notification-status: notification.status
    notification-audit: notification.audit
```

#### Consumers Kafka
```java
// Auth Events
@KafkaListener(topics = "${audit.topics.auth-events}")
public void consumeAuthEvents(...)

// User Events (7 topics)
@KafkaListener(topics = {
    "${audit.topics.user-created}",
    "${audit.topics.user-updated}",
    "${audit.topics.user-activated}",
    "${audit.topics.user-deactivated}",
    "${audit.topics.kyc-status-changed}",
    "${audit.topics.client-assigned}",
    "${audit.topics.client-unassigned}"
})
public void consumeUserEvents(...)

// Account Events (5 topics)
@KafkaListener(topics = {
    "${audit.topics.account-created}",
    "${audit.topics.account-updated}",
    "${audit.topics.account-balance-changed}",
    "${audit.topics.account-suspended}",
    "${audit.topics.account-closed}"
})
public void consumeAccountEvents(...)

// Payment Events (6 topics)
@KafkaListener(topics = {
    "${audit.topics.payment-initiated}",
    "${audit.topics.payment-completed}",
    "${audit.topics.payment-failed}",
    "${audit.topics.payment-reversed}",
    "${audit.topics.transaction-completed}",
    "${audit.topics.fraud-detected}"
})
public void consumePaymentEvents(...)

// Crypto Events
@KafkaListener(topics = "${audit.topics.crypto-transaction}")
public void consumeCryptoEvents(...)

// Notification Events (2 topics)
@KafkaListener(topics = {
    "${audit.topics.notification-status}",
    "${audit.topics.notification-audit}"
})
public void consumeNotificationEvents(...)
```

---

### 2️⃣ ANALYTICS-SERVICE (Consumer)

#### Configuration (application.yml)
```yaml
analytics:
  topics:
    # Auth Service
    auth-events: auth.events
    
    # User Service
    user-created: user.created
    user-updated: user.updated
    user-activated: user.activated
    user-deactivated: user.deactivated
    kyc-status-changed: kyc.status.changed
    client-assigned: client.assigned
    client-unassigned: client.unassigned
    
    # Account Service
    account-created: account.created
    account-updated: account.updated
    account-balance-changed: account.balance.changed
    account-suspended: account.suspended
    account-closed: account.closed
    
    # Payment Service
    transaction-completed: transaction.completed
    payment-initiated: payment.initiated
    payment-completed: payment.completed
    payment-failed: payment.failed
    payment-reversed: payment.reversed
    fraud-detected: fraud.detected
    
    # Crypto Service
    crypto-transaction: crypto.transaction
    
    # Notification Service
    notification-status: notification.status
    notification-audit: notification.audit
```

#### Consumers Kafka
```java
// Payment Events (5 topics)
@KafkaListener(topics = {
    "${analytics.topics.payment-completed}",
    "${analytics.topics.payment-failed}",
    "${analytics.topics.payment-initiated}",
    "${analytics.topics.payment-reversed}",
    "${analytics.topics.transaction-completed}"
})
public void consumePaymentEvents(...)

// Account Events (5 topics)
@KafkaListener(topics = {
    "${analytics.topics.account-created}",
    "${analytics.topics.account-updated}",
    "${analytics.topics.account-balance-changed}",
    "${analytics.topics.account-suspended}",
    "${analytics.topics.account-closed}"
})
public void consumeAccountEvents(...)

// User Events (7 topics)
@KafkaListener(topics = {
    "${analytics.topics.user-created}",
    "${analytics.topics.user-updated}",
    "${analytics.topics.user-activated}",
    "${analytics.topics.user-deactivated}",
    "${analytics.topics.kyc-status-changed}",
    "${analytics.topics.client-assigned}",
    "${analytics.topics.client-unassigned}"
})
public void consumeUserEvents(...)

// Auth Events
@KafkaListener(topics = "${analytics.topics.auth-events}")
public void consumeAuthEvents(...)

// Fraud Events
@KafkaListener(topics = "${analytics.topics.fraud-detected}")
public void consumeFraudEvents(...)

// Crypto Events
@KafkaListener(topics = "${analytics.topics.crypto-transaction}")
public void consumeCryptoEvents(...)

// Notification Events (2 topics)
@KafkaListener(topics = {
    "${analytics.topics.notification-status}",
    "${analytics.topics.notification-audit}"
})
public void consumeNotificationEvents(...)
```

---

## 🔧 Script de Création des Topics

```bash
#!/bin/bash

# Configuration
BROKER="localhost:9092"
PARTITIONS=3
REPLICATION=1

# Liste complète des topics
TOPICS=(
    # Auth
    "auth.events"
    
    # User
    "user.created"
    "user.updated"
    "user.activated"
    "user.deactivated"
    "kyc.status.changed"
    "client.assigned"
    "client.unassigned"
    
    # Account
    "account.created"
    "account.updated"
    "account.balance.changed"
    "account.suspended"
    "account.closed"
    
    # Payment
    "transaction.completed"
    "payment.initiated"
    "payment.completed"
    "payment.failed"
    "payment.reversed"
    "fraud.detected"
    
    # Crypto
    "crypto.transaction"
    
    # Notification
    "notification.status"
    "notification.audit"
)

echo "Creating ${#TOPICS[@]} Kafka topics..."

for topic in "${TOPICS[@]}"; do
    echo "  Creating topic: $topic"
    kafka-topics --create \
        --bootstrap-server $BROKER \
        --topic "$topic" \
        --partitions $PARTITIONS \
        --replication-factor $REPLICATION \
        --if-not-exists 2>/dev/null
done

echo "✓ All topics created successfully!"

# Vérifier
echo ""
echo "Listing all topics:"
kafka-topics --list --bootstrap-server $BROKER
```

---

## 📝 Format des Messages par Topic

### auth.events
```json
{
  "eventType": "LOGIN_SUCCESS|LOGIN_FAILED|MFA_REQUIRED|PASSWORD_RESET|NEW_DEVICE",
  "userId": "user123",
  "sessionId": "session_abc",
  "ip": "192.168.1.1",
  "device": "Chrome/Windows",
  "location": "Casablanca, MA",
  "timestamp": "2026-01-03T14:00:00Z"
}
```

### user.created
```json
{
  "userId": "user456",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+212600000000",
  "createdAt": "2026-01-03T14:00:00Z"
}
```

### account.created
```json
{
  "accountId": "acc_123",
  "userId": "user456",
  "accountNumber": "MA1234567890",
  "accountType": "SAVINGS",
  "currency": "MAD",
  "balance": 0.00,
  "status": "ACTIVE",
  "createdAt": "2026-01-03T14:00:00Z"
}
```

### payment.completed
```json
{
  "paymentId": "pay_xyz789",
  "userId": "user456",
  "fromAccountId": "acc_123",
  "toAccountId": "acc_456",
  "amount": 250.00,
  "currency": "MAD",
  "status": "COMPLETED",
  "timestamp": "2026-01-03T17:00:00Z"
}
```

### fraud.detected
```json
{
  "userId": "user101",
  "alertType": "SUSPICIOUS_TRANSACTION",
  "severity": "HIGH",
  "details": "Transaction inhabituelle détectée",
  "transactionId": "txn_suspect_123",
  "amount": 5000.0,
  "timestamp": "2026-01-03T13:00:00Z"
}
```

### notification.status
```json
{
  "eventType": "NOTIFICATION_SENT",
  "notificationId": "notif_12345",
  "userId": "user606",
  "status": "SENT",
  "provider": "EMAIL",
  "sentAt": "2026-01-03T18:00:05Z"
}
```

---

## 🎯 Résumé

### Total Topics: **22**

**Par Service Producer:**
- auth-service: 1 topic
- user-service: 7 topics
- account-service: 5 topics
- payment-service: 6 topics
- crypto-service: 1 topic
- notification-service: 2 topics

**Services Consumers:**
- **audit-service**: Écoute les 22 topics
- **analytics-service**: Écoute les 22 topics
- **notification-service**: Écoute ~10 topics
- **account-service**: Écoute ~5 topics
- **payment-service**: Écoute ~3 topics

---

## ✅ Checklist de Validation

- [ ] Tous les topics créés dans Kafka
- [ ] audit-service écoute correctement les 22 topics
- [ ] analytics-service écoute correctement les 22 topics
- [ ] Format des messages respecté
- [ ] Consumer groups configurés
- [ ] Monitoring actif (Prometheus)
- [ ] Alertes configurées

---

**Dernière mise à jour**: 04 Janvier 2026  
**Version**: 1.0.0