# 📚 Guide Simple des Topics Kafka - Notification Service

## 🎯 Comprendre les Topics en 2 Minutes

### 📥 **Topics CONSUMER** (Vous RECEVEZ ces messages)

---

#### 1️⃣ `transaction.completed` 💸
**EN BREF**: "Une transaction bancaire est terminée"

**Scénario Réel**:
```
Client fait un virement → payment-service traite → ✅ Publié sur transaction.completed
→ Vous recevez l'événement → 📧 Vous envoyez un email de confirmation
```

**Exemple de Message**:
```json
{
  "userId": "user123",
  "userEmail": "client@mail.com",
  "transactionId": "txn_abc123",
  "amount": 150.0,
  "type": "TRANSFER"
}
```

**Ce que vous faites**: Envoi d'un email "Votre virement de 150€ a été effectué"

---

#### 2️⃣ `payment.completed` 💳
**EN BREF**: "Un paiement est complété"

**Scénario Réel**:
```
Client paie une facture → payment-service valide → ✅ Publié sur payment.completed
→ Vous recevez → 📧 Email + 📱 SMS (si montant > 1000€)
```

**Exemple de Message**:
```json
{
  "paymentId": "pay_xyz789",
  "amount": 250.0,
  "recipient": "COMPANY XYZ",
  "status": "COMPLETED"
}
```

**Ce que vous faites**: "Paiement de 250€ à COMPANY XYZ réussi"

---

#### 3️⃣ `auth.events` 🔐
**EN BREF**: "Événement de connexion ou sécurité"

**Scénario Réel**:
```
Client se connecte depuis Paris → auth-service détecte → ✅ LOGIN_SUCCESS
→ Vous recevez → 📧 Email "Connexion détectée depuis Paris"
```

**Sous-types d'événements**:
- ✅ `LOGIN_SUCCESS` → Email informatif
- ❌ `LOGIN_FAILED` → Alerte sécurité
- 🔢 `MFA_REQUIRED` → SMS avec code 2FA
- 🔑 `PASSWORD_RESET` → Email avec lien
- 📱 `NEW_DEVICE` → Alerte "Nouvel appareil"

**Exemple**:
```json
{
  "eventType": "NEW_DEVICE",
  "userId": "user789",
  "device": "iPhone 15 Pro",
  "location": "Paris, France"
}
```

---

#### 4️⃣ `fraud.detected` 🚨
**EN BREF**: "ALERTE! Activité suspecte"

**Scénario Réel**:
```
Algo détecte transaction inhabituelle → fraud-service alerte → ✅ URGENT
→ Vous recevez → 📧 Email + 📱 SMS + 🔔 Push + 💬 In-App (TOUT!)
```

**Exemple**:
```json
{
  "alertType": "SUSPICIOUS_TRANSACTION",
  "severity": "HIGH",
  "amount": 5000.0,
  "details": "Montant inhabituel pour ce compte"
}
```

**Ce que vous faites**: Alertes MULTI-CANAL urgentes (priorité maximale)

---

#### 5️⃣ `account.created` 🎉
**EN BREF**: "Nouveau compte bancaire créé"

**Scénario Réel**:
```
Client s'inscrit → account-service crée le compte → ✅ Compte créé
→ Vous recevez → 📧 Email de bienvenue
```

**Exemple**:
```json
{
  "userId": "user202",
  "userEmail": "newuser@mail.com",
  "userName": "Jane Smith",
  "accountType": "SAVINGS"
}
```

**Ce que vous faites**: "🎉 Bienvenue chez E-Banking 3.0! Votre compte SAVINGS est prêt"

---

#### 6️⃣ `kyc.status.changed` 📋
**EN BREF**: "Résultat de vérification d'identité"

**Scénario Réel**:
```
Admin valide les documents → user-service met à jour KYC → ✅ APPROVED
→ Vous recevez → 📧 Email + 🔔 Push "KYC approuvée"
```

**Statuts possibles**:
- ✅ `APPROVED` → "Félicitations! Compte vérifié"
- ❌ `REJECTED` → "Documents non conformes, soumettre à nouveau"
- ⏳ `PENDING` → "Vérification en cours (24-48h)"

**Exemple**:
```json
{
  "userId": "user303",
  "newStatus": "APPROVED",
  "reason": "Documents validés"
}
```

---

#### 7️⃣ `crypto.transaction` ₿
**EN BREF**: "Transaction cryptomonnaie"

**Scénario Réel**:
```
Client achète 0.05 BTC → crypto-service exécute → ✅ Achat confirmé
→ Vous recevez → 📧 Email + 🔔 Push avec détails crypto
```

**Exemple**:
```json
{
  "transactionType": "BUY",
  "cryptocurrency": "BTC",
  "amount": 0.05,
  "fiatAmount": 1500.0,
  "rate": 30000.0
}
```

**Ce que vous faites**: "💰 Achat de 0.05 BTC pour 1500€ confirmé (taux: 30000€/BTC)"

---

#### 8️⃣ `notification.requested` 📨
**EN BREF**: "API générique: n'importe quel service demande une notification"

**Scénario Réel**:
```
Auth-service a besoin d'envoyer un code OTP → Publie sur notification.requested
→ Vous recevez → 📱 Envoi SMS avec code
```

**Flexibilité**: Peut envoyer EMAIL, SMS, PUSH ou IN_APP selon la demande

**Exemple**:
```json
{
  "sourceService": "auth-service",
  "notificationData": {
    "userId": "user505",
    "recipient": "+33612345678",
    "type": "SMS",
    "message": "Votre code OTP: 123456"
  }
}
```

---

## 📤 **Topics PRODUCER** (Vous PUBLIEZ ces messages)

---

#### 1️⃣ `notification.status` ✉️
**EN BREF**: "Résultat d'envoi de notification"

**Scénario**:
```
Vous envoyez un email → ✅ Succès → Vous publiez sur notification.status
→ Analytics-service reçoit → 📊 Met à jour statistiques
```

**Événements**:
- ✅ `NOTIFICATION_SENT` → Envoyé avec succès
- ❌ `NOTIFICATION_FAILED` → Échec (raison incluse)
- 📬 `NOTIFICATION_DELIVERED` → Confirmation de livraison
- 👁️ `NOTIFICATION_READ` → Notification lue (In-App)

**Exemple**:
```json
{
  "eventType": "NOTIFICATION_SENT",
  "notificationId": 12345,
  "status": "SENT",
  "provider": "EMAIL"
}
```

**Qui consomme?**: analytics-service, audit-service, services sources

---

#### 2️⃣ `notification.audit` 📊
**EN BREF**: "Journal d'audit pour compliance"

**Scénario**:
```
Chaque notification envoyée → Vous publiez sur notification.audit
→ Audit-service enregistre → 🗃️ Traçabilité complète
```

**Pourquoi?**: 
- Conformité RGPD
- Preuve légale
- Investigation incidents

**Exemple**:
```json
{
  "eventType": "NOTIFICATION_SENT",
  "timestamp": "2024-12-16T19:00:00",
  "notificationData": {
    "userId": "user606",
    "message": "Email sent to user@example.com"
  }
}
```

**Qui consomme?**: audit-service (obligation légale)

---

#### 3️⃣ `notification.metrics` 📈
**EN BREF**: "Statistiques agrégées"

**Scénario**:
```
Toutes les 5 minutes → Vous calculez les stats → Vous publiez
→ Analytics-service → 📊 Dashboards en temps réel
```

**Métriques**:
- Nombre total envoyé
- Répartition par canal (Email/SMS/Push)
- Taux d'échec
- Temps moyen de livraison

**Exemple**:
```json
{
  "timestamp": "2024-12-16T20:00:00",
  "metrics": {
    "totalSent": 1523,
    "emailsSent": 890,
    "smsSent": 423,
    "failures": 15,
    "avgDeliveryTime": "2.3s"
  }
}
```

**Qui consomme?**: analytics-service, Prometheus, Grafana

---

## 🔄 Flux Complet (Exemple)

### Scénario: Client fait un virement de 150€

```
1. Client clique sur "Envoyer" dans l'app
   ↓
2. Payment-Service traite le virement
   ↓
3. ✅ Virement réussi → Payment-Service publie sur `transaction.completed`
   ↓
4. 📥 Notification-Service (VOUS) reçoit l'événement
   ↓
5. Vous générez un email de confirmation HTML
   ↓
6. Vous envoyez l'email via SMTP
   ↓
7. 📤 Vous publiez sur `notification.status` (NOTIFICATION_SENT)
   ↓
8. 📤 Vous publiez sur `notification.audit` (traçabilité)
   ↓
9. Analytics-Service reçoit et met à jour les dashboards
   ↓
10. ✅ Client reçoit "Votre virement de 150€ a été effectué"
```

---

## 🎯 Résumé Ultra-Rapide

| Type | Nombre | Rôle |
|------|--------|------|
| **CONSUMER** | 8 topics | Vous ÉCOUTEZ les autres services |
| **PRODUCER** | 3 topics | Vous INFORMEZ les autres services |

**Votre Mission**: 
- 📥 Recevoir des événements métier
- 📧 Envoyer des notifications (Email/SMS/Push/In-App)
- 📤 Publier les résultats

**Architecture**: Event-Driven (Kafka) → Découplage total → Scalabilité infinie

---

## ✅ Checklist Kafka

- [x] ✅ 8 consumers implémentés
- [x] ✅ 3 producers opérationnels
- [x] ✅ 58 tests passing
- [x] ✅ Dot notation (convention Kafka)
- [x] ✅ Dead Letter Queue (DLQ) pour erreurs
- [x] ✅ Prometheus monitoring

**Status**: 🎉 **100% OPÉRATIONNEL**
