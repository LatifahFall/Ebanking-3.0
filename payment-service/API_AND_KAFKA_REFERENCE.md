# Payment Service - API & Kafka Reference

Ce document fournit une référence complète de tous les endpoints REST et topics Kafka du Payment Service pour les développeurs GraphQL et les équipes de déploiement Kafka.

**Base URL**: `http://localhost:8082/api/payments` (dev) ou selon configuration production

**Service Port**: `8082` (par défaut, configurable via `SERVER_PORT`)

**Version API**: 1.0.0

---

## Table des matières

1. [Endpoints REST](#endpoints-rest)
2. [Topics Kafka - Producer](#topics-kafka---producer)
3. [Topics Kafka - Consumer](#topics-kafka---consumer)
4. [Configuration Kafka](#configuration-kafka)

---

## Endpoints REST

### 1. POST /api/payments

**Description**: Crée et traite un nouveau paiement (STANDARD ou INSTANT)

**Méthode**: `POST`

**Body Request**:
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 100.50,
  "currency": "EUR",
  "paymentType": "STANDARD",
  "reference": "Payment ref 123",
  "description": "Payment description"
}
```

**Paramètres**:
- `fromAccountId` (Long, requis): ID du compte source
- `toAccountId` (Long, optionnel): ID du compte destinataire
- `amount` (BigDecimal, requis): Montant du paiement (min: 0.01)
- `currency` (String, requis): Code devise (ISO 4217, ex: "EUR", "USD")
- `paymentType` (String, requis): Type de paiement - `STANDARD` ou `INSTANT`
- `reference` (String, optionnel): Référence du paiement
- `description` (String, optionnel): Description du paiement

**Réponses**:
- `201 Created`: Paiement créé avec succès
  ```json
  {
    "id": 1,
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 100.50,
    "currency": "EUR",
    "paymentType": "STANDARD",
    "status": "COMPLETED",
    "reference": "Payment ref 123",
    "description": "Payment description",
    "createdAt": "2026-01-04T10:00:00",
    "completedAt": "2026-01-04T10:00:01"
  }
  ```
- `400 Bad Request`: Requête invalide
- `403 Forbidden`: Fraude détectée
- `404 Not Found`: Compte non trouvé
- `500 Internal Server Error`: Erreur serveur

**Event Kafka publié**: `payment.completed` (si succès)

---

### 2. POST /api/payments/biometric/generate-qr

**Description**: Génère un QR code pour un paiement biométrique

**Méthode**: `POST`

**Body Request**:
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 100.50,
  "currency": "EUR",
  "reference": "Payment ref 123",
  "description": "Biometric payment"
}
```

**Paramètres**: Identiques à POST /api/payments (sans paymentType)

**Réponses**:
- `201 Created`: QR code généré avec succès
  ```json
  {
    "paymentId": 1,
    "qrCode": "data:image/png;base64,iVBORw0KG...",
    "qrCodeData": "{\"token\":\"...\",\"paymentId\":\"1\",...}",
    "qrToken": "qr-token-123",
    "status": "PENDING",
    "message": "Scan this QR code with your mobile app to confirm the payment"
  }
  ```
- `400 Bad Request`: Requête invalide
- `403 Forbidden`: Fraude détectée
- `500 Internal Server Error`: Erreur serveur

---

### 3. POST /api/payments/biometric

**Description**: Traite un paiement biométrique avec token QR code

**Méthode**: `POST`

**Body Request**:
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 100.50,
  "currency": "EUR",
  "biometricData": {
    "type": "QR_CODE",
    "qrToken": "qr-token-from-scanned-qr-code",
    "deviceId": "device-123",
    "sessionId": "session-456"
  },
  "reference": "Payment ref 123",
  "description": "Biometric payment"
}
```

**Paramètres**:
- `biometricData.type` (String, requis): Type biométrique - `QR_CODE`
- `biometricData.qrToken` (String, requis): Token du QR code scanné
- `biometricData.deviceId` (String, optionnel): ID du device
- `biometricData.sessionId` (String, optionnel): ID de session

**Réponses**:
- `201 Created`: Paiement biométrique traité avec succès
- `400 Bad Request`: Requête invalide ou token QR code invalide
- `401 Unauthorized`: Vérification QR code échouée
- `403 Forbidden`: Fraude détectée
- `404 Not Found`: Compte non trouvé ou QR code expiré
- `500 Internal Server Error`: Erreur serveur

**Event Kafka publié**: `payment.completed` (si succès)

---

### 4. POST /api/payments/qrcode/generate

**Description**: Génère un QR code pour un paiement

**Méthode**: `POST`

**Body Request**:
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 100.50,
  "currency": "EUR",
  "paymentType": "QR_CODE",
  "reference": "Payment ref 123",
  "description": "QR code payment"
}
```

**Réponses**:
- `200 OK`: QR code généré avec succès
  ```json
  {
    "paymentId": 1,
    "qrCode": "data:image/png;base64,iVBORw0KG...",
    "qrCodeData": "{\"token\":\"...\",\"paymentId\":\"1\",\"userId\":\"1\",\"amount\":\"100.50\",\"currency\":\"EUR\",\"timestamp\":1234567890}",
    "qrToken": "qr-token-123",
    "message": "QR code generated successfully",
    "format": "PNG (base64)"
  }
  ```
- `400 Bad Request`: Requête invalide
- `404 Not Found`: Compte non trouvé
- `500 Internal Server Error`: Erreur serveur

---

### 5. POST /api/payments/qrcode

**Description**: Traite un paiement avec QR code scanné

**Méthode**: `POST`

**Body Request**:
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 100.50,
  "currency": "EUR",
  "qrCodeData": "{\"token\":\"...\",\"paymentId\":\"1\",\"userId\":\"1\",\"amount\":\"100.50\",\"currency\":\"EUR\",\"timestamp\":1234567890}",
  "reference": "Payment ref 123",
  "description": "QR code payment"
}
```

**Paramètres**:
- `qrCodeData` (String, requis): Données JSON du QR code scanné (contient token, paymentId, userId, amount, currency, timestamp)

**Réponses**:
- `201 Created`: Paiement QR code traité avec succès
- `400 Bad Request`: Requête invalide ou données QR code invalides
- `401 Unauthorized`: Vérification QR code échouée
- `403 Forbidden`: Fraude détectée
- `404 Not Found`: Compte non trouvé
- `500 Internal Server Error`: Erreur serveur

**Event Kafka publié**: `payment.completed` (si succès)

---

### 6. GET /api/payments/{id}

**Description**: Récupère les détails d'un paiement par son ID

**Méthode**: `GET`

**Paramètres URL**:
- `id` (Long, requis): ID du paiement

**Réponses**:
- `200 OK`: Paiement trouvé
  ```json
  {
    "id": 1,
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 100.50,
    "currency": "EUR",
    "paymentType": "STANDARD",
    "status": "COMPLETED",
    "reference": "Payment ref 123",
    "description": "Payment description",
    "createdAt": "2026-01-04T10:00:00",
    "completedAt": "2026-01-04T10:00:01"
  }
  ```
- `404 Not Found`: Paiement non trouvé

---

### 7. GET /api/payments

**Description**: Liste paginée des paiements avec filtres optionnels

**Méthode**: `GET`

**Query Parameters**:
- `accountId` (Long, **requis**): ID du compte pour filtrer
- `status` (String, optionnel): Filtrer par statut - `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `CANCELLED`, `REVERSED`
- `page` (int, défaut: 0): Numéro de page (0-indexed)
- `size` (int, défaut: 20): Taille de la page
- `sortBy` (String, défaut: "createdAt"): Champ de tri
- `sortDir` (String, défaut: "DESC"): Direction du tri - `ASC` ou `DESC`

**Exemple**: `GET /api/payments?accountId=1&status=COMPLETED&page=0&size=20&sortBy=createdAt&sortDir=DESC`

**Réponses**:
- `200 OK`: Liste des paiements
  ```json
  {
    "payments": [
      {
        "id": 1,
        "fromAccountId": 1,
        "toAccountId": 2,
        "amount": 100.50,
        "currency": "EUR",
        "paymentType": "STANDARD",
        "status": "COMPLETED",
        "createdAt": "2026-01-04T10:00:00"
      }
    ],
    "totalElements": 1,
    "currentPage": 0,
    "pageSize": 20,
    "hasNext": false,
    "hasPrevious": false,
    "totalPages": 1
  }
  ```
- `400 Bad Request`: Paramètres invalides (accountId manquant)

---

### 8. POST /api/payments/{id}/cancel

**Description**: Annule un paiement en statut PENDING ou PROCESSING

**Méthode**: `POST`

**Paramètres URL**:
- `id` (Long, requis): ID du paiement

**Réponses**:
- `200 OK`: Paiement annulé avec succès
  ```json
  {
    "id": 1,
    "status": "CANCELLED",
    ...
  }
  ```
- `404 Not Found`: Paiement non trouvé
- `409 Conflict`: Paiement ne peut pas être annulé dans son statut actuel

---

### 9. POST /api/payments/{id}/reverse

**Description**: Inverse un paiement complété (crédite le compte)

**Méthode**: `POST`

**Paramètres URL**:
- `id` (Long, requis): ID du paiement

**Query Parameters**:
- `reason` (String, requis): Raison de l'inversion - `CUSTOMER_REQUEST`, `FRAUD`, `ERROR`, `DISPUTE`

**Exemple**: `POST /api/payments/1/reverse?reason=CUSTOMER_REQUEST`

**Réponses**:
- `200 OK`: Paiement inversé avec succès
  ```json
  {
    "id": 1,
    "status": "REVERSED",
    "reversalReason": "CUSTOMER_REQUEST",
    "reversedAt": "2026-01-04T11:00:00",
    ...
  }
  ```
- `404 Not Found`: Paiement non trouvé
- `409 Conflict`: Paiement ne peut pas être inversé dans son statut actuel

**Event Kafka publié**: `payment.reversed` (si succès)

---

## Topics Kafka - Producer

Le Payment Service publie des événements sur les topics suivants :

### 1. payment.completed

**Topic**: `payment.completed` (configurable via `KAFKA_TOPIC_PAYMENT_COMPLETED`)

**Description**: Publié lorsqu'un paiement est complété avec succès

**Structure de l'événement**:
```json
{
  "paymentId": 1,
  "accountId": 1,
  "amount": 100.50,
  "currency": "EUR",
  "transactionType": "STANDARD",
  "status": "COMPLETED",
  "completedAt": "2026-01-04T10:00:01Z",
  "metadata": {
    "qrCodeVerified": true,
    "verifiedAt": "2026-01-04T10:00:01Z"
  }
}
```

**Champs**:
- `paymentId` (Long): ID du paiement
- `accountId` (Long): ID du compte source
- `amount` (BigDecimal): Montant du paiement
- `currency` (String): Code devise
- `transactionType` (String): Type de transaction (STANDARD, INSTANT, QR_CODE, BIOMETRIC)
- `status` (String): Statut du paiement (COMPLETED)
- `completedAt` (LocalDateTime, format ISO): Date de complétion
- `metadata` (Map<String, Object>, optionnel): Métadonnées additionnelles

**Consommateur recommandé**: Account Service, Notification Service

**Key**: `paymentId` (String)

**Quand est-il publié**:
- Après succès d'un paiement STANDARD
- Après succès d'un paiement INSTANT
- Après succès d'un paiement QR_CODE
- Après succès d'un paiement BIOMETRIC

---

### 2. payment.reversed

**Topic**: `payment.reversed` (configurable via `KAFKA_TOPIC_PAYMENT_REVERSED`)

**Description**: Publié lorsqu'un paiement est inversé/annulé

**Structure de l'événement**:
```json
{
  "paymentId": 1,
  "accountId": 1,
  "amount": 100.50,
  "currency": "EUR",
  "reversalReason": "CUSTOMER_REQUEST",
  "originalPaymentDate": "2026-01-04T10:00:00Z",
  "reversedAt": "2026-01-04T11:00:00Z",
  "metadata": {
    "originalReference": "Payment ref 123"
  }
}
```

**Champs**:
- `paymentId` (Long): ID du paiement inversé
- `accountId` (Long): ID du compte source
- `amount` (BigDecimal): Montant inversé
- `currency` (String): Code devise
- `reversalReason` (String): Raison de l'inversion (CUSTOMER_REQUEST, FRAUD, ERROR, DISPUTE)
- `originalPaymentDate` (LocalDateTime, format ISO): Date du paiement original
- `reversedAt` (LocalDateTime, format ISO): Date de l'inversion
- `metadata` (Map<String, Object>, optionnel): Métadonnées additionnelles

**Consommateur recommandé**: Account Service

**Key**: `paymentId` (String)

**Quand est-il publié**:
- Après succès d'une inversion de paiement (endpoint POST /api/payments/{id}/reverse)

---

### 3. fraud.detected

**Topic**: `fraud.detected` (configurable via `KAFKA_TOPIC_FRAUD_DETECTED`)

**Description**: Publié lorsqu'une fraude est détectée sur un paiement

**Structure de l'événement**:
```json
{
  "fraudId": "550e8400-e29b-41d4-a716-446655440000",
  "paymentId": 1,
  "accountId": 1,
  "userId": 1,
  "amount": 100.50,
  "fraudType": "SUSPICIOUS_AMOUNT",
  "reason": "Amount exceeds daily limit",
  "detectedAt": "2026-01-04T10:00:00Z",
  "action": "BLOCKED"
}
```

**Champs**:
- `fraudId` (UUID): ID unique de la détection de fraude
- `paymentId` (Long): ID du paiement suspect
- `accountId` (Long): ID du compte source
- `userId` (Long): ID de l'utilisateur
- `amount` (BigDecimal): Montant du paiement suspect
- `fraudType` (String): Type de fraude détectée (ex: SUSPICIOUS_AMOUNT, BLACKLISTED_ACCOUNT, etc.)
- `reason` (String): Raison de la détection
- `detectedAt` (LocalDateTime, format ISO): Date de détection
- `action` (String): Action prise (BLOCKED, CANCELLED, etc.)

**Consommateur recommandé**: Notification Service, Security Service

**Key**: `paymentId` (String)

**Quand est-il publié**:
- Lorsqu'une analyse de fraude détecte un problème avant le traitement du paiement

---

## Topics Kafka - Consumer

Le Payment Service consomme des événements des topics suivants :

### 1. account.created

**Topic**: `account.created` (configurable via `KAFKA_TOPIC_ACCOUNT_CREATED`)

**Description**: Consommé pour maintenir un cache local des comptes valides

**Structure de l'événement attendu**:
```json
{
  "accountId": 1,
  "userId": 1,
  "accountNumber": "ACC-123456",
  "status": "ACTIVE"
}
```

**Champs**:
- `accountId` (Long): ID du compte
- `userId` (Long): ID du propriétaire du compte
- `accountNumber` (String): Numéro de compte
- `status` (String): Statut du compte (ACTIVE, INACTIVE, SUSPENDED, CLOSED)

**Action effectuée**:
- Mise en cache du compte pour validation rapide des paiements
- Le cache est utilisé par le service de détection de fraude

**Producer**: Account Service

**Group ID**: `payment-service-group` (configurable via `KAFKA_CONSUMER_GROUP_ID`)

---

### 2. account.updated

**Topic**: `account.updated` (configurable via `KAFKA_TOPIC_ACCOUNT_UPDATED`)

**Description**: Consommé pour mettre à jour le cache des comptes et la blacklist de fraude

**Structure de l'événement attendu**:
```json
{
  "accountId": 1,
  "userId": 1,
  "accountNumber": "ACC-123456",
  "status": "SUSPENDED"
}
```

**Champs**: Identiques à `account.created`

**Action effectuée**:
- Mise à jour du cache local du compte
- Si le statut devient `SUSPENDED` ou `INACTIVE`: ajout à la blacklist de fraude
- Si le statut devient `ACTIVE`: retrait de la blacklist de fraude

**Producer**: Account Service

**Group ID**: `payment-service-group` (configurable via `KAFKA_CONSUMER_GROUP_ID`)

---

## Configuration Kafka

### Variables d'environnement

#### Topics Producer

| Variable | Description | Défaut |
|----------|-------------|--------|
| `KAFKA_TOPIC_PAYMENT_COMPLETED` | Topic pour payment.completed | `payment.completed` |
| `KAFKA_TOPIC_PAYMENT_REVERSED` | Topic pour payment.reversed | `payment.reversed` |
| `KAFKA_TOPIC_FRAUD_DETECTED` | Topic pour fraud.detected | `fraud.detected` |

#### Topics Consumer

| Variable | Description | Défaut |
|----------|-------------|--------|
| `KAFKA_TOPIC_ACCOUNT_CREATED` | Topic pour account.created | `account.created` |
| `KAFKA_TOPIC_ACCOUNT_UPDATED` | Topic pour account.updated | `account.updated` |

#### Configuration Kafka Générale

| Variable | Description | Défaut |
|----------|-------------|--------|
| `KAFKA_BOOTSTRAP_SERVERS` | Serveurs Kafka (ex: localhost:9092) | `localhost:9092` |
| `KAFKA_CONSUMER_GROUP_ID` | Group ID du consumer | `payment-service-group` |
| `SPRING_KAFKA_ENABLED` | Activer/désactiver Kafka | `true` |

### Configuration dans application.yml

```yaml
payment:
  service:
    kafka:
      topics:
        producer:
          payment-completed: ${KAFKA_TOPIC_PAYMENT_COMPLETED:payment.completed}
          payment-reversed: ${KAFKA_TOPIC_PAYMENT_REVERSED:payment.reversed}
          fraud-detected: ${KAFKA_TOPIC_FRAUD_DETECTED:fraud.detected}
        consumer:
          account-created: ${KAFKA_TOPIC_ACCOUNT_CREATED:account.created}
          account-updated: ${KAFKA_TOPIC_ACCOUNT_UPDATED:account.updated}
```

### Notes pour le déploiement Kafka

1. **Création des topics**: Les topics doivent être créés dans Kafka avant le déploiement
2. **Partitions**: Recommandé 3 partitions minimum pour haute disponibilité
3. **Réplication**: Recommandé 3 réplicas pour production
4. **Retention**: Configurer selon les besoins de l'entreprise (ex: 7 jours)
5. **Compaction**: Optionnel pour les topics où les événements récents remplacent les anciens
6. **Consumer Group**: Le service utilise `payment-service-group` - s'assurer que tous les instances utilisent le même group ID

### Commandes Kafka utiles

```bash
# Lister les topics
kafka-topics.sh --list --bootstrap-server localhost:9092

# Créer un topic
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic payment.completed \
  --partitions 3 \
  --replication-factor 3

# Consulter les messages d'un topic
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic payment.completed \
  --from-beginning

# Vérifier le consumer group
kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 \
  --group payment-service-group \
  --describe
```

---

## Notes pour l'intégration GraphQL

### Mapping REST → GraphQL

1. **Query payments**: Utiliser `GET /api/payments?accountId=X` pour récupérer la liste
2. **Query payment**: Utiliser `GET /api/payments/{id}` pour les détails
3. **Mutation createPayment**: Utiliser `POST /api/payments`
4. **Mutation cancelPayment**: Utiliser `POST /api/payments/{id}/cancel`
5. **Mutation reversePayment**: Utiliser `POST /api/payments/{id}/reverse`
6. **Mutation generateQRCode**: Utiliser `POST /api/payments/qrcode/generate`
7. **Mutation processQRCodePayment**: Utiliser `POST /api/payments/qrcode`

### Authentification

- En développement: Le service utilise un `userId` par défaut (1) si aucune authentification n'est fournie
- En production: Keycloak doit être activé (`KEYCLOAK_ENABLED=true`)
- Format: Bearer Token dans le header `Authorization`

### Gestion des erreurs

Tous les endpoints retournent des erreurs au format :
```json
{
  "timestamp": "2026-01-04T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error description",
  "validationErrors": null
}
```

---

## Support

Pour toute question sur l'API ou les topics Kafka, consulter:
- Documentation Swagger UI: `http://localhost:8082/swagger-ui.html`
- README du service: `README.md`
- Architecture: `ARCHITECTURE.md`

