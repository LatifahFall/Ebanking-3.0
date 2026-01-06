# 🚀 Configuration de Déploiement Frontend

Ce document explique comment le frontend est configuré pour se connecter aux services backend déployés.

## 📋 Fichiers d'Environnement

### `src/environments/environment.ts` (Développement)
- **`useMock: true`** : Utilise les données mock pour le développement local
- URLs pointent vers `localhost` (pour développement local)

### `src/environments/environment.prod.ts` (Production)
- **`useMock: false`** : Utilise les vrais appels HTTP vers les backends déployés
- URLs pointent vers `http://34.22.142.65/api/...` (GCP GKE)

## 🔧 Services Configurés

Tous les services suivants utilisent maintenant les variables d'environnement :

### 1. **Auth Service** (`auth.service.ts`)
- **Base URL**: `environment.authServiceUrl`
- **Endpoints**:
  - `POST /login`
  - `POST /register`
  - `POST /refresh`
  - `POST /logout`
  - `POST /verify-token`
  - `POST /token-info`
  - `POST /mfa/verify`

### 2. **User Service** (`user.service.ts`)
- **Base URL**: `environment.userServiceUrl`
- **Endpoints**:
  - `GET /admin/users`
  - `POST /admin/users`
  - `GET /admin/users/{id}`
  - `PUT /admin/users/{id}`
  - `GET /admin/users/agents/{id}/clients`
  - `POST /admin/users/assignments`
  - `GET /me/{id}`
  - `PUT /me/{id}`

### 3. **Account Service** (`account.service.ts`)
- **Base URL**: `environment.accountServiceUrl`
- **Endpoints**:
  - `GET /?userId={userId}`
  - `GET /{id}`
  - `POST /` (créer un compte)

### 4. **Payment Service** (`payment.service.ts`)
- **Base URL**: `environment.paymentServiceUrl`
- **Endpoints**:
  - `POST /` (initiate payment)
  - `GET /{id}`
  - `GET /` (liste avec pagination)
  - `POST /{id}/cancel`
  - `POST /{id}/reverse`
  - `POST /qrcode/generate`
  - `POST /qrcode`

### 5. **Crypto Service** (`crypto.service.ts`)
- **Base URL**: `environment.cryptoServiceUrl`
- **Endpoints**:
  - `GET /wallets/user/{userId}`
  - `POST /wallets`
  - `GET /holdings/wallet/{walletId}`
  - `GET /transactions/wallet/{walletId}`
  - `POST /buy`
  - `POST /sell`
  - `POST /convert`
  - `GET /coins/details`
  - `GET /coins/prices`

### 6. **Notification Service** (`notification.service.ts`)
- **Base URL**: `environment.notificationServiceUrl`
- **Endpoints**:
  - `GET /` (liste des notifications)
  - `PUT /{id}/read` (marquer comme lu)

### 7. **Analytics Backend Service** (`analytics-backend.service.ts`)
- **Base URL**: `environment.analyticsServiceUrl`
- **Endpoints**:
  - `GET /dashboard/summary?userId={userId}`
  - `GET /dashboard/spending-breakdown?userId={userId}&period={period}`
  - `GET /dashboard/balance-trend?userId={userId}&days={days}`
  - `GET /admin/overview`
  - `GET /alerts?userId={userId}`
  - `POST /alerts/{id}/resolve`
  - `GET /recommendations?userId={userId}`

## 🔄 Mode Mock vs Production

### Mode Mock (`useMock: true`)
- Toutes les méthodes retournent des données mock
- Pas d'appels HTTP réels
- Utile pour le développement et les tests

### Mode Production (`useMock: false`)
- Tous les appels HTTP sont effectués vers les backends déployés
- En cas d'erreur, certains services peuvent fallback vers mock (selon l'implémentation)
- Utilisé lors du déploiement sur Vercel

## 🌐 URLs de Production

Tous les services pointent vers :
```
http://34.22.142.65/api/{service}/
```

Où `{service}` peut être :
- `auth`
- `users`
- `accounts`
- `payments`
- `crypto`
- `notifications`
- `v1/analytics`
- `audit`

## ⚙️ Configuration Vercel

Pour déployer sur Vercel, assurez-vous que :

1. **Build Command**: `npm run build` (ou `ng build`)
2. **Output Directory**: `dist/ebanking-spa/browser`
3. **Environment Variables**: Aucune variable d'environnement n'est nécessaire car tout est dans les fichiers `environment.*.ts`

### Build Production
```bash
ng build --configuration production
```

Cela utilisera automatiquement `environment.prod.ts` qui :
- Active les appels HTTP réels (`useMock: false`)
- Utilise les URLs de production (`http://34.22.142.65/api/...`)

## 🔒 CORS et Sécurité

⚠️ **Note importante** : Les backends doivent être configurés pour accepter les requêtes depuis le domaine Vercel.

Si vous rencontrez des erreurs CORS :
1. Vérifiez que les backends autorisent le domaine Vercel
2. Vérifiez que les headers d'authentification sont correctement envoyés
3. Vérifiez que les tokens JWT sont valides

## 🧪 Test Local avec Backends Déployés

Pour tester localement avec les backends déployés (sans mock) :

1. Modifiez temporairement `environment.ts` :
   ```typescript
   export const environment = {
     production: false,
     useMock: false, // ← Changez à false
     apiBaseUrl: 'http://34.22.142.65/api',
     // ... autres URLs
   };
   ```

2. Lancez l'application :
   ```bash
   ng serve
   ```

3. ⚠️ **Attention** : Vous pourriez rencontrer des erreurs CORS si les backends n'autorisent pas `localhost`.

## 📝 Notes

- Les services utilisent `catchError()` pour fallback vers mock en cas d'erreur HTTP
- Certains services (comme `AnalyticsBackendService`) ont une logique de retry avec backoff exponentiel
- Les tokens JWT sont stockés dans `localStorage` et envoyés automatiquement via les interceptors HTTP (à implémenter si nécessaire)

