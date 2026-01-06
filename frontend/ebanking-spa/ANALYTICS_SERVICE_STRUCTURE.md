# Analytics Backend Service Structure

Ce document décrit la structure du service `AnalyticsBackendService` et comment il est préparé pour la migration vers les appels HTTP réels.

## Vue d'ensemble

**Fichier:** `src/app/core/services/analytics-backend.service.ts`

Le service `AnalyticsBackendService` est structuré pour correspondre exactement aux endpoints du backend Analytics Service, avec une implémentation mock pour l'instant et une structure prête pour la migration vers les appels HTTP réels.

---

## Structure du Service

### Configuration

```typescript
private readonly useMock: boolean = true; // TODO: Set from environment variable
```

- **Flag `useMock`**: Contrôle si le service utilise des mocks ou des appels HTTP réels
- **Méthode `getBaseUrl()`**: Retourne l'URL de base configurable (par défaut: `/api/v1/analytics`)
- **Mock Storage**: `mockAlerts` pour simuler le stockage des alertes

### Pattern d'Implémentation

Chaque méthode suit le même pattern :

```typescript
methodName(params): Observable<ResponseType> {
  if (this.useMock) {
    return this.methodNameMock(params);
  }
  
  // TODO: Replace with real HTTP calls to /api/v1/analytics/*
  return this.http.get<ResponseType>(`${this.getBaseUrl()}/endpoint`, { params }).pipe(
    catchError((error) => {
      console.error('Error, falling back to mock:', error);
      return this.methodNameMock(params);
    })
  );
}
```

**Avantages:**
- ✅ Facile à basculer entre mock et HTTP réel
- ✅ Fallback automatique vers mock en cas d'erreur
- ✅ Structure prête pour la migration
- ✅ Commentaires TODO clairs pour la migration

---

## Méthodes du Service

### Dashboard Endpoints

#### 1. `getDashboardSummary(userId: string): Observable<DashboardSummary>`

**Backend Endpoint:** `GET /api/v1/analytics/dashboard/summary?userId={userId}`

**Description:** Récupère le résumé du dashboard pour un utilisateur.

**Mock Implementation:**
- Génère des données réalistes basées sur `userId`
- Inclut: balance, dépenses mensuelles, revenus, transactions
- Génère des `DataPoint` pour le graphique de tendance (30 derniers jours)
- Inclut les top catégories et transactions récentes

**Exemple de réponse:**
```typescript
{
  userId: "3",
  currentBalance: 174129.40,
  monthlySpending: 3450.75,
  monthlyIncome: 5200.00,
  transactionsThisMonth: 47,
  topCategories: [...],
  balanceTrend: { period: "30 days", dataPoints: [...] },
  recentTransactions: [...],
  generatedAt: "2024-01-15T10:30:00.000Z"
}
```

---

#### 2. `getSpendingBreakdown(userId: string, period: 'MONTH' | 'WEEK'): Observable<CategoryBreakdown[]>`

**Backend Endpoint:** `GET /api/v1/analytics/spending/breakdown?userId={userId}&period={MONTH|WEEK}`

**Description:** Récupère la répartition des dépenses par catégorie.

**Paramètres:**
- `userId`: ID de l'utilisateur
- `period`: Période ('MONTH' ou 'WEEK')

**Mock Implementation:**
- Génère 5 catégories principales
- Ajuste les montants selon la période (semaine = ~25% du mois)
- Pourcentages qui totalisent 100%

**Exemple de réponse:**
```typescript
[
  {
    category: "Food & Dining",
    amount: 1035.23,
    count: 15,
    percentage: 30.0
  },
  {
    category: "Transportation",
    amount: 862.69,
    count: 10,
    percentage: 25.0
  },
  // ...
]
```

---

#### 3. `getBalanceTrend(userId: string, days: number = 30): Observable<BalanceTrend>`

**Backend Endpoint:** `GET /api/v1/analytics/trends/balance?userId={userId}&days={30}`

**Description:** Récupère l'évolution du solde sur une période donnée.

**Paramètres:**
- `userId`: ID de l'utilisateur
- `days`: Nombre de jours (défaut: 30)

**Mock Implementation:**
- Génère des `DataPoint` pour chaque jour
- Simule une évolution progressive avec variations aléatoires
- Période dynamique basée sur `days`

**Exemple de réponse:**
```typescript
{
  period: "30 days",
  dataPoints: [
    { timestamp: "2024-01-01T00:00:00.000Z", value: 150000.00 },
    { timestamp: "2024-01-02T00:00:00.000Z", value: 150250.50 },
    // ...
  ]
}
```

---

#### 4. `getRecommendations(userId: string): Observable<string[]>`

**Backend Endpoint:** `GET /api/v1/analytics/insights/recommendations?userId={userId}`

**Description:** Récupère les recommandations personnalisées pour l'utilisateur.

**Mock Implementation:**
- Retourne une liste de 5 recommandations génériques
- Basées sur les patterns de dépenses et d'épargne

**Exemple de réponse:**
```typescript
[
  "Your spending is 15% higher than last month. Consider reviewing your budget.",
  "You have 3 recurring subscriptions. Cancel unused ones to save money.",
  // ...
]
```

---

#### 5. `getAdminOverview(): Observable<AdminOverview>`

**Backend Endpoint:** `GET /api/v1/analytics/admin/overview`

**Description:** Récupère la vue d'ensemble système pour les administrateurs.

**Permissions:** Admin only (`@PreAuthorize("hasRole('ADMIN')")`)

**Mock Implementation:**
- Retourne des statistiques système mockées
- Inclut: utilisateurs actifs, transactions totales, revenus

**Exemple de réponse:**
```typescript
{
  activeUsers: 1247,
  totalTransactions: 15234,
  revenue: 125000.50
}
```

---

### Alert Endpoints

#### 6. `getActiveAlerts(userId: string): Observable<Alert[]>`

**Backend Endpoint:** `GET /api/v1/analytics/alerts/active?userId={userId}`

**Description:** Récupère les alertes actives pour un utilisateur.

**Mock Implementation:**
- Utilise `mockAlerts` initialisé dans le constructeur
- Filtre par `userId` et `status === ACTIVE`
- Retourne uniquement les alertes non résolues

**Exemple de réponse:**
```typescript
[
  {
    alertId: "alert-001",
    userId: "3",
    alertType: AlertType.SPENDING_THRESHOLD,
    severity: AlertSeverity.WARNING,
    title: "Monthly Spending Threshold Exceeded",
    message: "You have exceeded your monthly spending threshold...",
    thresholdValue: 3000,
    currentValue: 3450.75,
    status: AlertStatus.ACTIVE,
    triggeredAt: "2024-01-15T08:30:00.000Z",
    notified: true
  },
  // ...
]
```

---

#### 7. `resolveAlert(alertId: string): Observable<void>`

**Backend Endpoint:** `POST /api/v1/analytics/alerts/{alertId}/resolve`

**Description:** Résout une alerte (la marque comme résolue).

**Mock Implementation:**
- Met à jour le statut de l'alerte à `RESOLVED`
- Définit `resolvedAt` à la date actuelle
- Retourne `void`

---

## Mock Data Initialization

### Alertes Mockées

Le service initialise 5 alertes mockées dans `initializeMockAlerts()`:

1. **Spending Threshold** (User 3) - WARNING
2. **Low Balance** (User 3) - INFO
3. **Large Transaction** (User 3) - WARNING
4. **Budget Exceeded** (User 4) - CRITICAL
5. **Unusual Activity** (User 3) - CRITICAL

Ces alertes servent d'exemples pour tester l'affichage et la résolution.

---

## Migration vers HTTP Réel

### Étape 1: Configuration

1. **Désactiver le mode mock:**
   ```typescript
   private readonly useMock: boolean = false;
   ```
   Ou via variable d'environnement:
   ```typescript
   private readonly useMock: boolean = 
     environment.analytics?.useMock ?? true;
   ```

2. **Configurer l'URL de base:**
   - Via `localStorage.setItem('ANALYTICS_SERVICE_URL', 'http://localhost:8087/api/v1/analytics')`
   - Ou via variable d'environnement

### Étape 2: Authentification

Ajouter l'intercepteur HTTP pour l'authentification JWT:

```typescript
// Dans app.config.ts ou un intercepteur
{
  provide: HTTP_INTERCEPTORS,
  useClass: AuthInterceptor,
  multi: true
}
```

L'intercepteur doit ajouter le token JWT dans les headers:
```
Authorization: Bearer <token>
```

### Étape 3: Gestion des Erreurs

Le service utilise déjà `catchError` pour fallback vers mock. Pour la production:

1. **Retirer le fallback mock** (ou le garder comme backup)
2. **Gérer les erreurs HTTP** (401, 403, 500, etc.)
3. **Afficher des messages d'erreur appropriés**

### Étape 4: Tests

1. Tester chaque endpoint avec le backend réel
2. Vérifier que les types de réponse correspondent
3. Valider la gestion des erreurs
4. Tester l'authentification et les permissions

---

## Structure des Fichiers

```
frontend/ebanking-spa/src/app/core/services/
├── analytics-backend.service.ts    # Service principal (ce fichier)
└── ...

frontend/ebanking-spa/src/app/models/
└── analytics.model.ts               # Modèles TypeScript alignés
```

---

## Notes Importantes

1. **Latence simulée:** Les mocks utilisent `delay(200-300ms)` pour simuler la latence réseau
2. **Données réalistes:** Les mocks génèrent des données cohérentes et réalistes
3. **Fallback automatique:** En cas d'erreur HTTP, le service bascule automatiquement vers le mock
4. **Type Safety:** Tous les types sont alignés avec les modèles backend
5. **TODO Comments:** Tous les endroits nécessitant des modifications sont marqués avec `// TODO:`

---

## Prochaines Étapes

1. ✅ Phase 1: Modèles alignés - **TERMINÉ**
2. ✅ Phase 2: Service créé - **TERMINÉ**
3. ⏭️ Phase 3: Refactoriser les services existants pour utiliser `AnalyticsBackendService`
4. ⏭️ Phase 4: Mettre à jour les composants pour utiliser les nouveaux modèles
5. ⏭️ Phase 5: Tester et valider
6. ⏭️ Phase 6: Migration vers HTTP réel (quand le backend sera prêt)

---

**Dernière mise à jour:** Phase 2 - Service Analytics créé avec structure backend

