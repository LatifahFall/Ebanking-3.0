# 🏦 E-Banking Microservices - URLs Locales

Déployé en **local via API Gateway**

---

## 🌐 Adresse Locale

```
localhost:9090
```

---

## 📋 Services Disponibles

### 💰 Payment Service
**Health Check:**
```
http://localhost:9090/api/payments/actuator/health
```

**API Base URL:**
```
http://localhost:9090/api/payments/
```

**Port Direct:** `8083`

**Endpoints principaux:**
- `GET /api/payments/` - Liste des paiements
- `POST /api/payments/` - Créer un paiement
- `GET /api/payments/{id}` - Détails d'un paiement

---

### 🏦 Account Service
**Health Check:**
```
http://localhost:9090/api/accounts/actuator/health
```

**API Base URL:**
```
http://localhost:9090/api/accounts/
```

**Port Direct:** `8082`

**Endpoints principaux:**
- `GET /api/accounts/` - Liste des comptes
- `POST /api/accounts/` - Créer un compte
- `GET /api/accounts/{id}` - Détails d'un compte
- `GET /api/accounts/{id}/balance` - Solde du compte

---

### 👤 User Service
**Health Check:**
```
http://localhost:9090/api/users/actuator/health
```

**API Base URL:**
```
http://localhost:9090/api/users/
```

**Port Direct:** `8081`

**Endpoints principaux:**
- `GET /api/users/` - Liste des utilisateurs
- `POST /api/users/` - Créer un utilisateur
- `GET /api/users/{id}` - Détails d'un utilisateur
- `PUT /api/users/{id}` - Modifier un utilisateur

---

### 🔐 Auth Service
**Health Check:**
```
http://localhost:9090/api/auth/actuator/health
```

**API Base URL:**
```
http://localhost:9090/api/auth/
```

**Port Direct:** `8080`

**Endpoints principaux:**
- `POST /api/auth/login` - Connexion
- `POST /api/auth/register` - Inscription
- `POST /api/auth/refresh` - Rafraîchir le token
- `POST /api/auth/logout` - Déconnexion

---

### 🪙 Crypto Service
**Health Check:**
```
http://localhost:9090/api/crypto/actuator/health
```

**API Base URL:**
```
http://localhost:9090/api/crypto/
```

**Port Direct:** `8085`

**Endpoints principaux:**
- `GET /api/crypto/wallets` - Liste des wallets crypto
- `GET /api/crypto/coins` - Liste des cryptomonnaies
- `POST /api/crypto/transactions` - Effectuer une transaction
- `GET /api/crypto/holdings` - Avoirs crypto

---

### 📧 Notification Service
**Health Check:**
```
http://localhost:9090/api/notifications/actuator/health
```

**API Base URL:**
```
http://localhost:9090/api/notifications/
```

**Port Direct:** `8086`

**Endpoints principaux:**
- `GET /api/notifications/` - Liste des notifications
- `POST /api/notifications/send` - Envoyer une notification
- `GET /api/notifications/{id}` - Détails d'une notification
- `PUT /api/notifications/{id}/read` - Marquer comme lue

---

### 📊 Audit Service
**Health Check:**
```
http://localhost:9090/api/audit
```

**API Base URL:**
```
http://localhost:9090/api/audit/
```

**Port Direct:** `8087` (context-path: `/api/v1`)

**Endpoints principaux:**
- `GET /api/audit/events` - Liste des événements d'audit
- `GET /api/audit/events/{id}` - Détails d'un événement
- `GET /api/audit/search` - Rechercher dans les logs

---

### 📈 Analytics Service
**Health Check:**
```
http://localhost:9090/api/analytics
```

**API Base URL:**
```
http://localhost:9090/api/analytics/
```

**Port Direct:** `8088` (context-path: `/api/v1`)

**Endpoints principaux:**
- `GET /api/analytics/dashboard` - Tableau de bord analytique
- `GET /api/analytics/reports` - Rapports d'analyse
- `GET /api/analytics/alerts` - Alertes système

---

### 🤖 AI Service
**Health Check:**
```
http://localhost:9090/api/ai
```

**API Base URL:**
```
http://localhost:9090/api/ai/
```

**Port Direct:** `8089`

**Endpoints principaux:**
- `POST /api/chat/send` - Chat avec l'assistant AI
- `GET /api/ai/recommendations` - Recommandations intelligentes
- `POST /api/ai/analyze` - Analyse de données par IA

---

## 🧪 Tests Rapides (PowerShell)

```powershell
# Tester tous les services via API Gateway
$services = @("users", "accounts", "payments", "crypto/coins", "notifications", "audit", "analytics", "ai")
foreach ($svc in $services) {
    Write-Host "Testing /$svc..." -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest "http://localhost:9090/api/$svc" -TimeoutSec 3
        Write-Host "  ✓ $($response.StatusCode)" -ForegroundColor Green
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        if ($status -eq 403 -or $status -eq 401) {
            Write-Host "  ✓ $status OAuth2 (Secured)" -ForegroundColor Yellow
        } else {
            Write-Host "  ✗ $status" -ForegroundColor Red
        }
    }
}
```

---

## 🧪 Tests avec cURL (CMD/PowerShell)

```bash
# Account Service
curl http://localhost:9090/api/accounts/actuator/health

# User Service
curl http://localhost:9090/api/users/actuator/health

# Payment Service
curl http://localhost:9090/api/payments/actuator/health

# Crypto Service
curl http://localhost:9090/api/crypto/actuator/health

# Notification Service
curl http://localhost:9090/api/notifications/actuator/health

# Audit Service
curl http://localhost:9090/api/audit

# Analytics Service
curl http://localhost:9090/api/analytics

# AI Service
curl http://localhost:9090/api/ai
```

---

## 📊 Architecture Locale

```
                    Browser/Client
                           |
                           ↓
                 [API Gateway :9090]
            (Spring Cloud Gateway + Redis)
                           |
        ┌──────────────────┼──────────────────┐
        |                  |                  |
   /api/users        /api/accounts      /api/payments
   (8081)               (8082)            (8083)
        |                  |                  |
   PostgreSQL          PostgreSQL         PostgreSQL
   (userdb)          (account_db)    (ebanking_payment)
        
        |                  |                  |
  /api/crypto       /api/notifications  /api/audit
   (8085)               (8086)            (8087)
        |                  |                  |
   PostgreSQL         PostgreSQL         PostgreSQL
   (cryptodb)      (notification_db)   (audit_db)
        
        |                  |
 /api/analytics        /api/ai
   (8088)            (8089)
        |                  |
   PostgreSQL         OpenAI API
  (analytics_db)
```

---

## 🛠️ Infrastructure Locale

- **Platform:** Windows Local Development
- **API Gateway:** Spring Cloud Gateway (Port 9090)
- **Java:** 17.0.12
- **Spring Boot:** 3.2.0
- **Spring Cloud:** 2023.0.0
- **Databases:** 
  - PostgreSQL 17.4 (localhost:5432, password: aabir)
  - Redis 7.4.7 (localhost:6379)
- **Message Queue:** Apache Kafka (localhost:9092)
- **Services Actifs:** 8/8 microservices

---

## 🔒 Sécurité

⚠️ **Configuration Actuelle:**
- **Keycloak:** Désactivé (`KEYCLOAK_ENABLED=false`)
- **OAuth2:** Configuré mais en mode développement
- **Auth Service:** Disponible sur port 8080

**Pour activer la sécurité complète:**
1. Démarrer Keycloak: `docker-compose up keycloak`
2. Configurer `.env`: `KEYCLOAK_ENABLED=true`
3. Redémarrer les services

---

## 📱 Ports des Services

| Service | Port Direct | API Gateway Path | Status |
|---------|-------------|------------------|--------|
| Auth | 8080 | `/api/auth/**` | ✅ |
| User | 8081 | `/api/users/**` | ⚠️ |
| Account | 8082 | `/api/accounts/**` | ✅ |
| Payment | 8083 | `/api/payments/**` | ⚠️ |
| Crypto | 8085 | `/api/crypto/**` | ✅ |
| Notification | 8086 | `/api/notifications/**` | ✅ |
| Audit | 8087 | `/api/audit/**` | ✅ |
| Analytics | 8088 | `/api/analytics/**` | ⚠️ |
| AI | 8089 | `/api/ai/**` | ✅ |
| **Gateway** | **9090** | **All routes** | **✅** |

**Légende:**
- ✅ Service actif et répondant
- ⚠️ Service non démarré ou en erreur

---

## 🚀 Commandes de Gestion

### Démarrer tous les services
```powershell
.\start-all-services.ps1
```

### Arrêter tous les services
```powershell
Get-Process java | Stop-Process -Force
```

### Vérifier les ports actifs
```powershell
netstat -ano | findstr "LISTENING" | findstr ":808"
netstat -ano | findstr "LISTENING" | findstr ":9090"
```

### Test complet via API Gateway
```powershell
.\test-api-endpoints.ps1
```

---

## 📅 Dernière Mise à Jour

**Date:** 6 janvier 2026  
**Version:** 1.0.0  
**Environment:** Local Development  
**Status:** 🟡 Partial (6/8 services actifs)

---

## 🔧 Services à Démarrer

Pour atteindre 100% de fonctionnalité, démarrer:

1. **User Service (8081)**
   ```powershell
   cd C:\Users\Hp\Desktop\rest\user-service
   java -jar target\UserService-0.0.1-SNAPSHOT.jar --server.port=8081 --DB_PASSWORD=aabir
   ```

2. **Payment Service (8083)**
   ```powershell
   cd C:\Users\Hp\Desktop\rest\payment-service\payment-service
   java -jar target\payment-service-0.0.1-SNAPSHOT.jar --server.port=8083 --DB_PASSWORD=aabir
   ```

3. **Analytics Service (8088)**
   ```powershell
   cd C:\Users\Hp\Desktop\rest\analytics-service
   java -jar target\AnalyticsService-0.0.1-SNAPSHOT.jar --server.port=8088 --DB_PASSWORD=aabir
   ```

---

**🎉 Merci d'utiliser notre plateforme E-Banking locale !**
