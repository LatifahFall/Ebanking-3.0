# Kafka Topics - Payment Service

Ce document décrit tous les topics Kafka utilisés par le payment-service pour la communication asynchrone avec les autres microservices (account-service, notification-service).

## Architecture Kafka

```
┌─────────────────┐         Kafka         ┌─────────────────┐
│ account-service │◄──────────────────────►│ payment-service │
└─────────────────┘                        └─────────────────┘
        │                                           │
        │                                           │
        │                                           ▼
        │                                   ┌────────────────────┐
        └──────────────────────────────────►│notification-service│
                                            └────────────────────┘
```

---

## 📤 PRODUCERS (Événements émis par payment-service)

### 1. `payment.completed`

**Déclenché** : Après la complétion réussie d'un paiement  
**Consommateurs** : account-service, notification-service  
**Format** :

```json
{
  "paymentId": 12345,
  "accountId": 67890,
  "amount": 150.75,
  "currency": "EUR",
  "transactionType": "STANDARD",
  "status": "COMPLETED",
  "completedAt": "2024-01-15T10:30:00Z"
}
```

**Champs** :
- `paymentId` (Long) : ID unique du paiement
- `accountId` (Long) : ID du compte débité
- `amount` (BigDecimal) : Montant du paiement
- `currency` (String) : Code devise ISO 4217 (EUR, USD, etc.)
- `transactionType` (String) : Type de paiement (STANDARD, INSTANT, BIOMETRIC, QR_CODE)
- `status` (String) : Statut final (COMPLETED)
- `completedAt` (Timestamp) : Date/heure de complétion

**Utilisation** :
- account-service met à jour le solde du compte
- notification-service envoie une notification de confirmation

---

### 2. `payment.reversed`

**Déclenché** : Lorsqu'un paiement complété est annulé/inversé  
**Consommateurs** : account-service, notification-service  
**Format** :

```json
{
  "paymentId": 12345,
  "accountId": 67890,
  "amount": 150.75,
  "currency": "EUR",
  "reversalReason": "CUSTOMER_REQUEST",
  "originalPaymentDate": "2024-01-15T10:30:00Z",
  "reversedAt": "2024-01-16T14:20:00Z"
}
```

**Champs** :
- `paymentId` (Long) : ID du paiement annulé
- `accountId` (Long) : ID du compte à re-créditer
- `amount` (BigDecimal) : Montant à restituer
- `currency` (String) : Code devise
- `reversalReason` (String) : Motif d'annulation (CUSTOMER_REQUEST, FRAUD, ERROR, etc.)
- `originalPaymentDate` (Timestamp) : Date du paiement original
- `reversedAt` (Timestamp) : Date de l'annulation

**Utilisation** :
- account-service re-crédite le compte source
- notification-service informe le client de l'annulation

---

### 3. `payment.failed`

**Déclenché** : Lorsqu'un paiement échoue  
**Consommateurs** : notification-service  
**Format** :

```json
{
  "paymentId": 12345,
  "accountId": 67890,
  "amount": 150.75,
  "currency": "EUR",
  "failureReason": "INSUFFICIENT_FUNDS",
  "errorCode": "ERR_BALANCE_001",
  "failedAt": "2024-01-15T10:35:00Z"
}
```

**Champs** :
- `paymentId` (Long) : ID du paiement échoué
- `accountId` (Long) : ID du compte source
- `amount` (BigDecimal) : Montant du paiement tenté
- `currency` (String) : Code devise
- `failureReason` (String) : Raison de l'échec
- `errorCode` (String) : Code d'erreur technique
- `failedAt` (Timestamp) : Date de l'échec

**Utilisation** :
- notification-service envoie une alerte d'échec au client

---

### 4. `fraud.detected`

**Déclenché** : Lorsque le moteur anti-fraude détecte une anomalie  
**Consommateurs** : account-service, notification-service  
**Format** :

```json
{
  "paymentId": 12345,
  "accountId": 67890,
  "userId": 111,
  "amount": 5000.00,
  "currency": "EUR",
  "fraudScore": 0.95,
  "fraudReasons": ["AMOUNT_TOO_HIGH", "UNUSUAL_LOCATION"],
  "detectedAt": "2024-01-15T10:40:00Z",
  "severity": "HIGH"
}
```

**Champs** :
- `paymentId` (Long) : ID du paiement suspect
- `accountId` (Long) : ID du compte concerné
- `userId` (Long) : ID de l'utilisateur
- `amount` (BigDecimal) : Montant suspect
- `currency` (String) : Code devise
- `fraudScore` (Double) : Score de fraude (0.0 à 1.0)
- `fraudReasons` (List<String>) : Liste des indicateurs de fraude
- `detectedAt` (Timestamp) : Date de détection
- `severity` (String) : Niveau de gravité (LOW, MEDIUM, HIGH, CRITICAL)

**Utilisation** :
- account-service peut suspendre temporairement le compte
- notification-service envoie une alerte de sécurité

---

## 📥 CONSUMERS (Événements reçus par payment-service)

### 5. `account.created`

**Émetteur** : account-service  
**But** : Informer payment-service de la création d'un nouveau compte  
**Format** :

```json
{
  "accountId": 67890,
  "userId": 111,
  "accountType": "CHECKING",
  "currency": "EUR",
  "status": "ACTIVE",
  "createdAt": "2024-01-10T08:00:00Z"
}
```

**Champs** :
- `accountId` (Long) : ID du nouveau compte
- `userId` (Long) : ID du propriétaire
- `accountType` (String) : Type de compte (CHECKING, SAVINGS, etc.)
- `currency` (String) : Devise du compte
- `status` (String) : Statut (ACTIVE, PENDING, etc.)
- `createdAt` (Timestamp) : Date de création

**Utilisation** :
- payment-service met en cache les informations du compte
- Initialise les règles de paiement pour ce compte

---

### 6. `account.updated`

**Émetteur** : account-service  
**But** : Notifier des changements sur un compte existant  
**Format** :

```json
{
  "accountId": 67890,
  "userId": 111,
  "status": "SUSPENDED",
  "updatedAt": "2024-01-15T09:00:00Z",
  "changes": {
    "previousStatus": "ACTIVE",
    "newStatus": "SUSPENDED"
  }
}
```

**Champs** :
- `accountId` (Long) : ID du compte modifié
- `userId` (Long) : ID du propriétaire
- `status` (String) : Nouveau statut
- `updatedAt` (Timestamp) : Date de la modification
- `changes` (Object) : Détails des changements

**Utilisation** :
- payment-service met à jour son cache
- Bloque les paiements si le compte est suspendu

---

### 7. `account.suspended`

**Émetteur** : account-service  
**But** : Notifier la suspension d'un compte (sécurité ou fraude)  
**Format** :

```json
{
  "accountId": 67890,
  "userId": 111,
  "reason": "FRAUD_SUSPECTED",
  "suspendedAt": "2024-01-15T10:00:00Z",
  "expiresAt": "2024-01-16T10:00:00Z"
}
```

**Champs** :
- `accountId` (Long) : ID du compte suspendu
- `userId` (Long) : ID du propriétaire
- `reason` (String) : Raison de la suspension
- `suspendedAt` (Timestamp) : Date de suspension
- `expiresAt` (Timestamp) : Date d'expiration de la suspension (optionnel)

**Utilisation** :
- payment-service rejette immédiatement tous les paiements de ce compte
- Annule les paiements en attente

---

### 8. `account.closed`

**Émetteur** : account-service  
**But** : Notifier la clôture définitive d'un compte  
**Format** :

```json
{
  "accountId": 67890,
  "userId": 111,
  "closedAt": "2024-02-01T12:00:00Z",
  "reason": "CUSTOMER_REQUEST"
}
```

**Champs** :
- `accountId` (Long) : ID du compte clôturé
- `userId` (Long) : ID du propriétaire
- `closedAt` (Timestamp) : Date de clôture
- `reason` (String) : Raison de la clôture

**Utilisation** :
- payment-service supprime le compte de son cache
- Archive l'historique des paiements
- Rejette définitivement tous les nouveaux paiements

---

### 9. `account.balance.changed`

**Émetteur** : account-service  
**But** : Notifier en temps réel des changements de solde  
**Format** :

```json
{
  "accountId": 67890,
  "previousBalance": 1000.00,
  "newBalance": 850.00,
  "changeAmount": -150.00,
  "currency": "EUR",
  "changeReason": "DEBIT",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**Champs** :
- `accountId` (Long) : ID du compte
- `previousBalance` (BigDecimal) : Solde avant changement
- `newBalance` (BigDecimal) : Nouveau solde
- `changeAmount` (BigDecimal) : Montant du changement (négatif si débit)
- `currency` (String) : Code devise
- `changeReason` (String) : Raison du changement (DEBIT, CREDIT, etc.)
- `timestamp` (Timestamp) : Date du changement

**Utilisation** :
- payment-service met à jour son cache de soldes
- Vérifie si le nouveau solde permet d'exécuter des paiements en attente

---

## Configuration Kafka

### application.yml (payment-service)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      properties:
        spring.json.add.type.headers: false
    
    consumer:
      group-id: payment-service
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "*"
        spring.json.value.default.type: com.ebanking.payment.event.AccountCreatedEvent

kafka:
  topics:
    payment-completed: payment.completed
    payment-reversed: payment.reversed
    payment-failed: payment.failed
    fraud-detected: fraud.detected
    account-created: account.created
    account-updated: account.updated
    account-suspended: account.suspended
    account-closed: account.closed
    account-balance-changed: account.balance.changed
  
  enabled: ${KAFKA_ENABLED:false}  # Désactivé par défaut
```

---

## Implémentation (Code actuel)

### Producers (EventProducer.java)

```java
@Component
@RequiredArgsConstructor
public class EventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        kafkaTemplate.send("payment.completed", event);
    }
    
    public void publishPaymentReversed(PaymentReversedEvent event) {
        kafkaTemplate.send("payment.reversed", event);
    }
    
    public void publishFraudDetected(FraudDetectedEvent event) {
        kafkaTemplate.send("fraud.detected", event);
    }
}
```

### Consumers (AccountEventConsumer.java)

```java
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {
    
    @KafkaListener(topics = "account.created", groupId = "payment-service")
    public void handleAccountCreated(AccountCreatedEvent event) {
        // Mise en cache du compte
    }
    
    @KafkaListener(topics = "account.updated", groupId = "payment-service")
    public void handleAccountUpdated(AccountUpdatedEvent event) {
        // Mise à jour du cache
    }
    
    @KafkaListener(topics = "account.suspended", groupId = "payment-service")
    public void handleAccountSuspended(AccountSuspendedEvent event) {
        // Blocage des paiements
    }
    
    @KafkaListener(topics = "account.closed", groupId = "payment-service")
    public void handleAccountClosed(AccountClosedEvent event) {
        // Suppression du cache
    }
    
    @KafkaListener(topics = "account.balance.changed", groupId = "payment-service")
    public void handleBalanceChanged(AccountBalanceChangedEvent event) {
        // Mise à jour du cache de soldes
    }
}
```

---

## Activation/Désactivation Kafka

**Par défaut, Kafka est désactivé** dans `application.yml` :

```yaml
kafka:
  enabled: false
```

Pour activer Kafka :

```bash
# Via variable d'environnement
export KAFKA_ENABLED=true

# Via application.yml
kafka:
  enabled: true
```

Lorsque Kafka est désactivé :
- Les producers n'émettent **pas** d'événements (no-op)
- Les consumers ne démarrent **pas**
- Le service fonctionne de manière **synchrone** avec account-service via REST

---

## Migration Progressive

1. **Phase 1** : REST uniquement (actuel)
   - payment-service → REST → account-service
   - Kafka désactivé

2. **Phase 2** : Hybride REST + Kafka
   - Opérations critiques (débit/crédit) via REST
   - Notifications via Kafka
   - Kafka activé pour les événements non-bloquants

3. **Phase 3** : Full Event-Driven
   - Toutes les opérations via Kafka
   - Compensation automatique en cas d'échec
   - Architecture SAGA

---

## Considérations Techniques

### Idempotence

Tous les consumers doivent être **idempotents** pour gérer les relivraisons :

```java
@Transactional
public void handleAccountCreated(AccountCreatedEvent event) {
    if (!accountCache.contains(event.getAccountId())) {
        // Traiter l'événement uniquement si non déjà traité
        accountCache.put(event.getAccountId(), event);
    }
}
```

### Ordre des Messages

Utiliser **partitions Kafka** par `accountId` pour garantir l'ordre :

```java
// Dans le producer
kafkaTemplate.send("account.updated", 
    event.getAccountId().toString(), // Key = accountId
    event
);
```

### Dead Letter Queue (DLQ)

En cas d'échec répété, les messages sont envoyés vers une DLQ :

```yaml
spring:
  kafka:
    listener:
      ack-mode: manual
    consumer:
      max-poll-records: 10
      properties:
        max.poll.interval.ms: 300000
```

---

## Monitoring & Observabilité

### Métriques Kafka

```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
```

**Métriques clés** :
- `kafka.producer.record-send-total` : Nombre d'événements émis
- `kafka.consumer.records-consumed-total` : Nombre d'événements consommés
- `kafka.consumer.fetch-latency-avg` : Latence moyenne de fetch

### Logs Kafka

```java
@Slf4j
@Component
public class KafkaLogger {
    
    @EventListener
    public void onKafkaSend(KafkaEvent event) {
        log.info("Kafka event sent: topic={}, key={}, partition={}", 
            event.getTopic(), event.getKey(), event.getPartition());
    }
}
```

---

## Résumé

| Topic | Direction | Producteur | Consommateurs | Critique |
|-------|-----------|-----------|---------------|----------|
| `payment.completed` | OUT | payment-service | account-service, notification-service | ⚠️ Oui |
| `payment.reversed` | OUT | payment-service | account-service, notification-service | ⚠️ Oui |
| `payment.failed` | OUT | payment-service | notification-service | Non |
| `fraud.detected` | OUT | payment-service | account-service, notification-service | ⚠️ Oui |
| `account.created` | IN | account-service | payment-service | Non |
| `account.updated` | IN | account-service | payment-service | Non |
| `account.suspended` | IN | account-service | payment-service | ⚠️ Oui |
| `account.closed` | IN | account-service | payment-service | ⚠️ Oui |
| `account.balance.changed` | IN | account-service | payment-service | Non |

**Types d'IDs utilisés** : `accountId`, `userId`, `paymentId` sont tous de type **Long** (alignés avec account-service et user-service).
