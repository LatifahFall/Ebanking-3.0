# Guide de Migration : Analytics Service - Mock vers Backend Réel

## Vue d'ensemble

Ce document explique comment migrer le `AnalyticsBackendService` des données mock vers les vrais appels HTTP au backend analytics-service.

## État actuel

- **Mode actuel** : Mock (données générées depuis `AccountService` et `TransactionService`)
- **Flag de contrôle** : `useMock = true` dans `AnalyticsBackendService`
- **Structure** : Prête pour la migration avec méthode `httpCall<T>()` centralisée

## Prérequis

### 1. Configuration Backend

Assurez-vous que le service analytics-service backend est :
- ✅ Déployé et accessible
- ✅ Port configuré (par défaut : 8087)
- ✅ Endpoints documentés et testés
- ✅ Authentification configurée (JWT tokens)

### 2. Configuration Frontend

#### Variables d'environnement

Créer/modifier `src/environments/environment.ts` et `environment.prod.ts` :

```typescript
export const environment = {
  production: false, // ou true pour prod
  analyticsServiceUrl: 'http://localhost:8087/api/v1/analytics',
  // Ou pour production:
  // analyticsServiceUrl: 'https://api.yourdomain.com/api/v1/analytics',
};
```

#### Configuration HTTP Interceptor

Assurez-vous qu'un HTTP Interceptor est configuré pour ajouter les tokens d'authentification :

```typescript
// src/app/core/interceptors/auth.interceptor.ts
import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next(req);
};
```

## Étapes de Migration

### Étape 1 : Mettre à jour `getBaseUrl()`

**Fichier** : `src/app/core/services/analytics-backend.service.ts`

**Avant** :
```typescript
private getBaseUrl(): string {
  const customBase = localStorage.getItem('ANALYTICS_SERVICE_URL');
  if (customBase) {
    return customBase.replace(/\/+$/, '');
  }
  return '/api/v1/analytics';
}
```

**Après** :
```typescript
import { environment } from '../../../environments/environment';

private getBaseUrl(): string {
  // Priorité : localStorage > environment > default
  const customBase = localStorage.getItem('ANALYTICS_SERVICE_URL');
  if (customBase) {
    return customBase.replace(/\/+$/, '');
  }
  return environment.analyticsServiceUrl || '/api/v1/analytics';
}
```

### Étape 2 : Activer les appels HTTP

**Fichier** : `src/app/core/services/analytics-backend.service.ts`

**Avant** :
```typescript
private readonly useMock: boolean = true;
```

**Après** :
```typescript
private readonly useMock: boolean = !environment.production; // Mock en dev, HTTP en prod
// OU
private readonly useMock: boolean = false; // Toujours HTTP
```

### Étape 3 : Vérifier les transformations de données

Les DTOs backend doivent correspondre aux interfaces TypeScript. Vérifiez :

#### DashboardSummary

**Backend DTO** (Java) :
```java
public class DashboardSummaryDTO {
    private String userId;
    private BigDecimal currentBalance;
    private BigDecimal monthlySpending;
    private BigDecimal monthlyIncome;
    private Integer transactionsThisMonth;
    private List<CategoryBreakdownDTO> topCategories;
    private BalanceTrendDTO balanceTrend;
    private List<RecentTransactionDTO> recentTransactions;
    private LocalDateTime generatedAt;
}
```

**Frontend Interface** (TypeScript) :
```typescript
export interface DashboardSummary {
  userId: string;
  currentBalance: number;        // BigDecimal -> number
  monthlySpending: number;      // BigDecimal -> number
  monthlyIncome: number;        // BigDecimal -> number
  transactionsThisMonth: number;
  topCategories?: CategoryBreakdown[];
  balanceTrend?: BalanceTrend;
  recentTransactions?: RecentTransaction[];
  generatedAt: string;           // LocalDateTime -> ISO string
}
```

**Transformation nécessaire** : Aucune si le backend envoie déjà les bonnes valeurs. Sinon, ajouter un pipe `map()` :

```typescript
return this.httpCall<DashboardSummary>(...).pipe(
  map(response => ({
    ...response,
    generatedAt: new Date(response.generatedAt).toISOString() // Si nécessaire
  }))
);
```

#### CategoryBreakdown

**Backend DTO** :
```java
public class CategoryBreakdownDTO {
    private String category;
    private BigDecimal amount;
    private Integer count;
    private Double percentage;
}
```

**Frontend Interface** :
```typescript
export interface CategoryBreakdown {
  category: string;
  amount: number;      // BigDecimal -> number
  count: number;
  percentage: number;
}
```

**Transformation** : Aucune si le backend envoie déjà les bonnes valeurs.

#### BalanceTrend

**Backend DTO** :
```java
public class BalanceTrendDTO {
    private String period;
    private List<DataPointDTO> dataPoints;
}

public class DataPointDTO {
    private LocalDateTime timestamp;
    private BigDecimal value;
}
```

**Frontend Interface** :
```typescript
export interface BalanceTrend {
  period: string;
  dataPoints: DataPoint[];
}

export interface DataPoint {
  timestamp: string;  // LocalDateTime -> ISO string
  value: number;      // BigDecimal -> number
}
```

**Transformation** : Si nécessaire, mapper les timestamps :

```typescript
return this.httpCall<BalanceTrend>(...).pipe(
  map(trend => ({
    ...trend,
    dataPoints: trend.dataPoints.map(point => ({
      ...point,
      timestamp: new Date(point.timestamp).toISOString()
    }))
  }))
);
```

#### Alert

**Backend DTO** :
```java
public class AlertDTO {
    private String alertId;
    private String userId;
    private AlertType alertType;
    private Severity severity;
    private String title;
    private String message;
    private BigDecimal thresholdValue;
    private BigDecimal currentValue;
    private AlertStatus status;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private Boolean notified;
}
```

**Frontend Interface** :
```typescript
export interface Alert {
  alertId: string;
  userId: string;
  alertType: AlertType;
  severity: AlertSeverity;
  title: string;
  message: string;
  thresholdValue?: number;    // BigDecimal -> number
  currentValue?: number;       // BigDecimal -> number
  status: AlertStatus;
  triggeredAt: string;          // LocalDateTime -> ISO string
  resolvedAt?: string;         // LocalDateTime -> ISO string
  notified: boolean;
}
```

**Transformation** : Mapper les dates si nécessaire.

### Étape 4 : Gestion des erreurs

La méthode `httpCall<T>()` gère déjà :
- ✅ Retry automatique (2 tentatives) pour erreurs 5xx
- ✅ Exponential backoff (1s, 2s, 4s...)
- ✅ Fallback vers mock en cas d'erreur (optionnel)

**Pour désactiver le fallback mock** (recommandé en production) :

```typescript
// Dans chaque méthode, remplacer :
return this.httpCall<DashboardSummary>(
  'dashboard/summary',
  'GET',
  params,
  undefined,
  this.getDashboardSummaryMock(userId) // ❌ Retirer ce paramètre
);

// Par :
return this.httpCall<DashboardSummary>(
  'dashboard/summary',
  'GET',
  params
  // Pas de fallback - l'erreur sera propagée
);
```

### Étape 5 : Tests

#### Test manuel

1. Démarrer le backend analytics-service
2. Configurer `useMock = false`
3. Tester chaque endpoint :
   - Dashboard Summary
   - Spending Breakdown
   - Balance Trend
   - Recommendations
   - Admin Overview
   - Active Alerts
   - Resolve Alert

#### Test automatisé

Créer des tests unitaires pour chaque méthode :

```typescript
describe('AnalyticsBackendService', () => {
  let service: AnalyticsBackendService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AnalyticsBackendService]
    });
    service = TestBed.inject(AnalyticsBackendService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch dashboard summary', () => {
    const mockSummary: DashboardSummary = { /* ... */ };
    
    service.getDashboardSummary('user-123').subscribe(summary => {
      expect(summary).toEqual(mockSummary);
    });

    const req = httpMock.expectOne('/api/v1/analytics/dashboard/summary?userId=user-123');
    expect(req.request.method).toBe('GET');
    req.flush(mockSummary);
  });
});
```

## Endpoints Backend

### 1. GET /api/v1/analytics/dashboard/summary

**Query Parameters** :
- `userId` (string, required) : ID de l'utilisateur

**Response** : `DashboardSummary`

**Exemple** :
```
GET /api/v1/analytics/dashboard/summary?userId=3
```

### 2. GET /api/v1/analytics/spending/breakdown

**Query Parameters** :
- `userId` (string, required) : ID de l'utilisateur
- `period` (string, optional) : `MONTH` ou `WEEK` (défaut: `MONTH`)

**Response** : `CategoryBreakdown[]`

**Exemple** :
```
GET /api/v1/analytics/spending/breakdown?userId=3&period=MONTH
```

### 3. GET /api/v1/analytics/trends/balance

**Query Parameters** :
- `userId` (string, required) : ID de l'utilisateur
- `days` (number, optional) : Nombre de jours (défaut: 30)

**Response** : `BalanceTrend`

**Exemple** :
```
GET /api/v1/analytics/trends/balance?userId=3&days=30
```

### 4. GET /api/v1/analytics/insights/recommendations

**Query Parameters** :
- `userId` (string, required) : ID de l'utilisateur

**Response** : `string[]` (array de messages de recommandation)

**Exemple** :
```
GET /api/v1/analytics/insights/recommendations?userId=3
```

### 5. GET /api/v1/analytics/admin/overview

**Headers** :
- `Authorization: Bearer <token>` (admin only)

**Response** : `AdminOverview`

**Exemple** :
```
GET /api/v1/analytics/admin/overview
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 6. GET /api/v1/analytics/alerts/active

**Query Parameters** :
- `userId` (string, required) : ID de l'utilisateur

**Response** : `Alert[]`

**Exemple** :
```
GET /api/v1/analytics/alerts/active?userId=3
```

### 7. POST /api/v1/analytics/alerts/{alertId}/resolve

**Path Parameters** :
- `alertId` (string, required) : ID de l'alerte

**Request Body** : `{}` (empty object)

**Response** : `204 No Content`

**Exemple** :
```
POST /api/v1/analytics/alerts/alert-123/resolve
Content-Type: application/json
{}
```

## Configuration requise

### Authentification

Le backend analytics-service nécessite un token JWT dans les headers :

```
Authorization: Bearer <token>
```

L'interceptor HTTP doit être configuré pour ajouter automatiquement ce header.

### CORS

Si le frontend et le backend sont sur des domaines différents, configurer CORS côté backend :

```java
@CrossOrigin(origins = "http://localhost:4200") // Pour dev
// OU
@CrossOrigin(origins = "https://yourdomain.com") // Pour prod
```

### Timeout

Configurer un timeout pour les appels HTTP (optionnel) :

```typescript
private httpCall<T>(...): Observable<T> {
  // ...
  return request.pipe(
    timeout(10000), // 10 secondes
    // ...
  );
}
```

## Checklist de Migration

- [ ] Backend analytics-service déployé et accessible
- [ ] Variables d'environnement configurées
- [ ] HTTP Interceptor configuré pour l'authentification
- [ ] `getBaseUrl()` utilise `environment.analyticsServiceUrl`
- [ ] `useMock` configuré selon l'environnement
- [ ] Transformations de données vérifiées (BigDecimal -> number, LocalDateTime -> string)
- [ ] Tests manuels effectués pour tous les endpoints
- [ ] Tests unitaires créés
- [ ] Gestion d'erreurs testée (retry, fallback)
- [ ] Documentation mise à jour
- [ ] Code review effectué

## Dépannage

### Erreur 401 Unauthorized

**Cause** : Token d'authentification manquant ou invalide

**Solution** :
1. Vérifier que l'interceptor HTTP ajoute le token
2. Vérifier que le token est valide
3. Vérifier les permissions de l'utilisateur

### Erreur 404 Not Found

**Cause** : Endpoint incorrect ou service non démarré

**Solution** :
1. Vérifier l'URL du service dans `environment.analyticsServiceUrl`
2. Vérifier que le backend est démarré
3. Vérifier les logs backend

### Erreur 500 Internal Server Error

**Cause** : Erreur côté backend

**Solution** :
1. Vérifier les logs backend
2. Vérifier que les données envoyées sont valides
3. Le retry automatique devrait gérer les erreurs temporaires

### Timeout

**Cause** : Le backend prend trop de temps à répondre

**Solution** :
1. Augmenter le timeout dans `httpCall()`
2. Optimiser les requêtes backend
3. Vérifier la performance du backend

## Notes importantes

1. **Fallback Mock** : En développement, le fallback vers mock est utile pour continuer à travailler même si le backend est indisponible. En production, désactivez-le pour détecter les vrais problèmes.

2. **Retry Logic** : Le retry automatique ne s'applique qu'aux erreurs 5xx (serveur) et erreurs réseau. Les erreurs 4xx (client) ne sont pas retentées.

3. **Performance** : Les appels HTTP sont plus lents que les mocks. Pensez à ajouter des indicateurs de chargement dans les composants.

4. **Cache** : Considérez l'ajout d'un cache pour les données qui ne changent pas fréquemment (ex: recommendations).

## Support

Pour toute question ou problème lors de la migration, consulter :
- Documentation backend : `/backend/analytics-service/README.md`
- Logs backend : Vérifier les logs du service analytics-service
- Logs frontend : Console du navigateur (F12)

