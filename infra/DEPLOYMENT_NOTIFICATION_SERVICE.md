# Déploiement du Notification Service

## 📋 Vue d'ensemble

Ce guide vous aide à déployer le microservice `notification-service` dans votre environnement Kubernetes, de la même manière que `account-service`.

## 🏗️ Architecture

```
notification-service/
├── Deployment (2 replicas)
├── Service (ClusterIP)
├── ConfigMaps (configuration partagée)
└── Dépendances: PostgreSQL, Redis, Kafka
```

## 🚀 Méthode 1 : Déploiement automatique (Recommandé)

Utilisez le script PowerShell fourni :

```powershell
cd c:\Users\sara slimani\Projets_dev\projet_atlas\Ebanking-3.0
.\infra\scripts\deploy-notification-service.ps1
```

Le script va :
1. ✅ Construire l'image Docker
2. ✅ Vérifier le namespace
3. ✅ Appliquer les ConfigMaps
4. ✅ Vérifier les dépendances (PostgreSQL, Redis, Kafka)
5. ✅ Déployer le service
6. ✅ Afficher les logs

## 🔧 Méthode 2 : Déploiement manuel

### Étape 1 : Construction de l'image Docker

```powershell
docker build -t notification-service:latest ./simulation_microservices_pr_test_infra/notification-service
```

### Étape 2 : Vérification du namespace

```powershell
kubectl get namespace ebanking
# Si le namespace n'existe pas :
kubectl apply -f ./infra/k8s/00-namespace.yaml
```

### Étape 3 : Application des ConfigMaps

```powershell
kubectl apply -f ./infra/k8s/01-configmaps.yaml
```

### Étape 4 : Déploiement des dépendances (si nécessaire)

Si PostgreSQL, Redis et Kafka ne sont pas encore déployés :

```powershell
# PostgreSQL
kubectl apply -f ./infra/k8s/databases/postgres.yaml

# Redis
kubectl apply -f ./infra/k8s/databases/redis.yaml

# Kafka
kubectl apply -f ./infra/k8s/messaging/kafka.yaml
```

### Étape 5 : Déploiement du notification-service

```powershell
kubectl apply -f ./infra/k8s/services/notification-service.yaml
```

## 🔍 Vérification du déploiement

### Vérifier les pods

```powershell
kubectl get pods -n ebanking -l app=notification-service
```

Résultat attendu :
```
NAME                                   READY   STATUS    RESTARTS   AGE
notification-service-xxxxxxxxx-xxxxx   1/1     Running   0          2m
notification-service-xxxxxxxxx-xxxxx   1/1     Running   0          2m
```

### Vérifier le service

```powershell
kubectl get svc -n ebanking notification-service
```

### Voir les logs

```powershell
# Logs d'un pod spécifique
kubectl logs -n ebanking <pod-name>

# Logs en temps réel de tous les pods
kubectl logs -n ebanking -l app=notification-service -f
```

## 🧪 Test local avec Docker Compose

Avant de déployer dans Kubernetes, vous pouvez tester localement :

```powershell
docker-compose up notification-service
```

## 📊 Configuration

Le service utilise les configurations suivantes (définies dans [01-configmaps.yaml](../k8s/01-configmaps.yaml)) :

- **Port**: 8083
- **Base de données**: PostgreSQL (notification_db)
- **Cache**: Redis
- **Messaging**: Kafka
- **Replicas**: 2

## 🔧 Commandes utiles

### Port-forward pour accéder au service localement

```powershell
kubectl port-forward -n ebanking svc/notification-service 8083:80
```

Ensuite, accédez au service sur : `http://localhost:8083`

### Redémarrer le déploiement

```powershell
kubectl rollout restart deployment/notification-service -n ebanking
```

### Voir les détails du déploiement

```powershell
kubectl describe deployment notification-service -n ebanking
```

### Supprimer le déploiement

```powershell
kubectl delete -f ./infra/k8s/services/notification-service.yaml
```

## 🐛 Troubleshooting

### Le pod ne démarre pas

```powershell
# Voir les événements du pod
kubectl describe pod <pod-name> -n ebanking

# Voir les logs du pod
kubectl logs <pod-name> -n ebanking
```

### ImagePullBackOff

Si l'image Docker n'est pas trouvée, assurez-vous de l'avoir construite :

```powershell
docker images | Select-String notification-service
```

### Les dépendances ne sont pas prêtes

Vérifiez que PostgreSQL, Redis et Kafka sont déployés :

```powershell
kubectl get pods -n ebanking
```

## 📝 Différences avec account-service

| Aspect | Account Service | Notification Service |
|--------|----------------|---------------------|
| Port | 8082 | 8083 |
| Database | account_db | notification_db |
| Kafka Group | account-service-group | notification-service-group |
| Health Check | HTTP (/actuator/health) | Process (pgrep) |
| Resources | 512Mi-1Gi / 250m-500m | 256Mi-512Mi / 100m-250m |

## ✅ Checklist de déploiement

- [ ] Image Docker construite
- [ ] Namespace `ebanking` créé
- [ ] ConfigMaps appliqués
- [ ] PostgreSQL déployé et prêt
- [ ] Redis déployé et prêt
- [ ] Kafka déployé et prêt
- [ ] Notification-service déployé
- [ ] Pods en état `Running`
- [ ] Logs vérifiés sans erreurs

## 🎯 Prochaines étapes

1. ✅ Account Service déployé
2. ✅ Notification Service déployé
3. ⏭️ Déployer payment-service (suivre le même processus)

## 📚 Ressources

- [Documentation Kubernetes](https://kubernetes.io/docs/)
- [Docker Build](https://docs.docker.com/engine/reference/commandline/build/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
