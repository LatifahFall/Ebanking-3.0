# 🚀 API Gateway - E-Banking REST API

## ⚡ Démarrage Ultra-Rapide

### Option 1: Maven (5 minutes)
```bash
cd api-gateway
.\quick-start-gateway.ps1
```

### Option 2: Docker (Recommandé si vous avez déjà des images)
```bash
cd api-gateway
.\docker-start-gateway.ps1
```

## 📌 Ce que fait l'API Gateway

✅ **Route toutes les requêtes** vers vos microservices existants  
✅ **Sécurité centralisée** (JWT/Keycloak) - activable/désactivable  
✅ **CORS géré** automatiquement  
✅ **Circuit Breaker** - protection contre les pannes  
✅ **Rate Limiting** - protection contre les abus  
✅ **Monitoring** - métriques et health checks  

## 🎯 Routes Disponibles

Toutes les requêtes passent maintenant par **http://localhost:8080**

| Service | Route Gateway | Service Réel |
|---------|---------------|--------------|
| Auth | `http://localhost:8080/api/auth/**` | `http://localhost:8080` |
| User | `http://localhost:8080/api/users/**` | `http://localhost:8081` |
| Account | `http://localhost:8080/api/accounts/**` | `http://localhost:8082` |
| Payment | `http://localhost:8080/api/payments/**` | `http://localhost:8083` |
| Crypto | `http://localhost:8080/api/crypto/**` | `http://localhost:8085` |
| Notification | `http://localhost:8080/api/notifications/**` | `http://localhost:8086` |
| Audit | `http://localhost:8080/api/audit/**` | `http://localhost:8087` |
| Analytics | `http://localhost:8080/api/analytics/**` | `http://localhost:8088` |

## ⚙️ Configuration

### 1. Copier le fichier .env
```bash
copy .env.example .env
```

### 2. Éditer .env avec vos URLs
```properties
# Mode développement (sans auth)
KEYCLOAK_ENABLED=false

# URLs de vos microservices
USER_SERVICE_URL=http://localhost:8081
ACCOUNT_SERVICE_URL=http://localhost:8082
PAYMENT_SERVICE_URL=http://localhost:8083
# etc...
```

### 3. Pour Docker, utilisez les noms de conteneurs
```properties
USER_SERVICE_URL=http://user-service:8081
ACCOUNT_SERVICE_URL=http://account-service:8082
# etc...
```

## 🧪 Tests Rapides

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Test d'une Route
```bash
# Via Gateway
curl http://localhost:8080/api/users

# Direct (pour comparaison)
curl http://localhost:8081/api/users
```

### Voir toutes les routes
```bash
curl http://localhost:8080/actuator/gateway/routes
```

## 🔧 Mode Dev vs Prod

### Mode Dev (Sans Authentication)
```bash
set KEYCLOAK_ENABLED=false
mvn spring-boot:run
```
➡️ Toutes les routes accessibles sans token

### Mode Prod (Avec Keycloak)
```bash
set KEYCLOAK_ENABLED=true
set KC_URL=http://keycloak:8180
set KC_REALM=bank-realm
mvn spring-boot:run
```
➡️ JWT token requis pour toutes les routes `/api/**`

## 🐳 Docker

### Build
```bash
docker build -t api-gateway:latest .
```

### Run avec Docker Compose
```bash
docker-compose up -d
```

### Voir les logs
```bash
docker logs -f api-gateway
```

### Arrêter
```bash
docker-compose down
```

## 📊 Monitoring

- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus
- **Routes**: http://localhost:8080/actuator/gateway/routes

## 🚨 Troubleshooting

### Port 8080 déjà utilisé
```bash
# Changer le port
set SERVER_PORT=9090
mvn spring-boot:run
```

### Service non accessible
```bash
# Vérifier que le service est up
curl http://localhost:8081/api/users

# Vérifier les logs du Gateway
docker logs api-gateway
```

### Circuit Breaker ouvert
```bash
# Redémarrer le Gateway
docker restart api-gateway
```

## 📚 Documentation Complète

Voir [API_GATEWAY_GUIDE.md](../API_GATEWAY_GUIDE.md) pour la documentation complète.

## 🎉 Résumé

**Avec l'API Gateway, vous n'avez pas besoin de modifier vos microservices !**

✅ Vos images Docker existantes restent intactes  
✅ Pas de rebuild nécessaire  
✅ Déploiement indépendant et rapide  
✅ Toutes les routes REST centralisées en un seul point  

**Temps de mise en place : ~15 minutes** 🚀
