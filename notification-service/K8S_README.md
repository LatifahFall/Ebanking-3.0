# Notification Service - E-Banking 3.0

Service de notifications multicanaux (Email, SMS, Push, In-App) pour la plateforme E-Banking 3.0.

## 🚀 Fonctionnalités

- ✅ **Multi-canaux**: Email (SMTP), SMS (Twilio), Push, In-App
- ✅ **Préférences utilisateur**: Gestion fine des canaux et DND
- ✅ **WebSocket**: Notifications temps réel via STOMP
- ✅ **Kafka**: Consommation d'événements asynchrones
- ✅ **Audit**: Traçabilité complète
- ✅ **Métriques Prometheus**: Monitoring production-ready
- ✅ **Health checks**: Liveness & Readiness pour Kubernetes
- ✅ **Logs structurés JSON**: Compatible ELK Stack

## 📦 Build & Run

### Local Development

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

### Docker

```bash
# Build image
docker build -t notification-service:latest .

# Run container
docker run -p 8084:8084 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/notification_db \
  -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  notification-service:latest
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Port HTTP | `8084` |
| `SPRING_DATASOURCE_URL` | URL PostgreSQL | `jdbc:postgresql://localhost:5432/notification_db` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka brokers | `localhost:9092` |
| `TWILIO_ACCOUNT_SID` | Twilio Account SID | - |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token | - |
| `SPRING_MAIL_HOST` | SMTP Host | `smtp.gmail.com` |

## 📊 Monitoring

### Health Checks

- **Liveness**: `GET /actuator/health/liveness`
- **Readiness**: `GET /actuator/health/readiness`
- **Health**: `GET /actuator/health`

### Metrics Prometheus

- **Endpoint**: `GET /actuator/prometheus`
- **Métriques custom**:
  - `notification.sent{type=email|sms|push|in_app}` - Notifications envoyées
  - `notification.failed{type=email|sms}` - Échecs
  - `notification.duration` - Durée d'envoi
  - `kafka.events.consumed` - Événements Kafka consommés

## 🎯 API Endpoints

### Notifications

```http
POST   /api/notifications              # Créer notification
GET    /api/notifications              # Lister notifications
GET    /api/notifications/{id}         # Détail notification
PUT    /api/notifications/{id}/read    # Marquer comme lu
DELETE /api/notifications/{id}         # Supprimer

POST   /api/notifications/bulk         # Envoi groupé
GET    /api/notifications/user/{userId} # Notifications utilisateur
GET    /api/notifications/stats/{userId} # Statistiques
```

### Préférences

```http
GET    /api/notifications/preferences/{userId}       # Obtenir préférences
POST   /api/notifications/preferences                # Créer préférences
PUT    /api/notifications/preferences/{userId}       # Mettre à jour
DELETE /api/notifications/preferences/{userId}       # Supprimer
```

### WebSocket

```javascript
// Connexion
const socket = new SockJS('http://localhost:8084/ws-notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
  // Abonnement notifications utilisateur
  stompClient.subscribe('/topic/notifications/user/123', (message) => {
    console.log('Nouvelle notification:', JSON.parse(message.body));
  });
});
```

## 🧪 Tests

```bash
# Tous les tests
mvn test

# Tests spécifiques
mvn test -Dtest=NotificationServiceTest
mvn test -Dtest=EmailServiceTest
mvn test -Dtest=RealEmailTest  # Test email réel
mvn test -Dtest=RealSmsTest    # Test SMS réel
```

## 🐳 Kubernetes Deployment

### Déploiement

```bash
# Build & tag image
docker build -t your-registry/notification-service:latest .
docker push your-registry/notification-service:latest

# Apply manifests
kubectl apply -f k8s/
```

### Exemple Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: notification-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: notification-service
  template:
    metadata:
      labels:
        app: notification-service
    spec:
      containers:
      - name: notification-service
        image: notification-service:latest
        ports:
        - containerPort: 8084
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres:5432/notification_db
        - name: KAFKA_BOOTSTRAP_SERVERS
          value: kafka:9092
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8084
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8084
          initialDelaySeconds: 40
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

## 📈 Architecture

```
┌─────────────┐
│   Kafka     │──────┐
└─────────────┘      │
                     ▼
┌─────────────┐  ┌──────────────────────┐
│  Frontend   │──│ Notification Service │
└─────────────┘  └──────────────────────┘
   (WebSocket)            │
                          ├──▶ PostgreSQL (Audit)
                          ├──▶ SMTP (Email)
                          ├──▶ Twilio (SMS)
                          └──▶ Push Service
```

## 🔐 Sécurité

- ✅ Image Docker non-root user
- ✅ Health checks HTTPS ready
- ✅ Secrets externalisés (K8s Secrets)
- ✅ Resource limits configurés

## 📚 Technologies

- **Java 17** (LTS)
- **Spring Boot 3.2.0**
- **PostgreSQL** (persistence)
- **Apache Kafka** (event streaming)
- **Twilio** (SMS)
- **WebSocket/STOMP** (real-time)
- **Prometheus** (métriques)
- **Docker** (containerization)

## 📝 Logs

Les logs sont au format JSON pour faciliter l'ingestion dans ELK Stack:

```json
{
  "timestamp": "2025-12-30T23:15:30.123+01:00",
  "level": "INFO",
  "thread": "http-nio-8084-exec-1",
  "logger": "NotificationService",
  "message": "Notification sent successfully: id=123"
}
```

## 🤝 Contributing

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 License

E-Banking 3.0 © 2025
