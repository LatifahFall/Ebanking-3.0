# Phase 5 : Alignement des données mock avec le backend

## Résumé

La Phase 5 a transformé les mocks statiques en générateurs de données réalistes basés sur les services existants (`AccountService` et `TransactionService`). Les données sont maintenant calculées selon la logique métier du backend, simulant fidèlement le comportement réel du service analytics.

## Changements principaux

### 1. Injection des services réels

**Avant :** Les mocks utilisaient des valeurs hardcodées
```typescript
const baseBalance = userId === '3' ? 174129.40 : 50000;
const monthlySpending = userId === '3' ? 3450.75 : 1200.50;
```

**Après :** Utilisation de `AccountService` et `TransactionService`
```typescript
constructor(
  private http: HttpClient,
  private accountService: AccountService,
  private transactionService: TransactionService
) {}
```

### 2. Calculs basés sur les données réelles

#### `getDashboardSummaryMock()`
- **Balance actuelle** : Somme des balances de tous les comptes de l'utilisateur
- **Monthly Spending** : Somme des transactions négatives (hors transferts) du mois en cours
- **Monthly Income** : Somme des transactions positives (salaires) du mois en cours
- **Transactions This Month** : Compte des transactions complétées du mois
- **Top Categories** : Calculées à partir des vraies catégories de transactions
- **Balance Trend** : Généré à partir de l'historique des comptes
- **Recent Transactions** : Dernières transactions réelles

#### `getSpendingBreakdownMock()`
- Filtre les transactions par période (MONTH/WEEK)
- Groupe par catégorie réelle (`TransactionCategory`)
- Calcule les montants et pourcentages
- **Normalise les pourcentages pour totaliser 100%**
- Trie par montant décroissant

#### `getBalanceTrendMock()`
- Génère des `DataPoint` sur N jours
- Base la progression sur le solde actuel des comptes
- Simule une évolution progressive avec variations réalistes (±2%)

### 3. Génération d'alertes basée sur des règles métier

Les alertes sont maintenant générées dynamiquement selon 5 règles métier :

#### Règle 1 : Low Balance Alert
- **Condition** : Balance < 1000 OU < 10% de la balance moyenne
- **Sévérité** : CRITICAL si < 500, sinon WARNING
- **Message** : Informe l'utilisateur de son solde faible

#### Règle 2 : Spending Threshold Alert
- **Condition** : Dépenses mensuelles > 3000
- **Sévérité** : CRITICAL si > 4500, sinon WARNING
- **Message** : Alerte sur le dépassement du seuil de dépenses

#### Règle 3 : Large Transaction Alert
- **Condition** : Transaction > 5000
- **Sévérité** : CRITICAL si > 10000, sinon WARNING
- **Message** : Alerte sur une transaction importante

#### Règle 4 : Frequent Transactions Alert
- **Condition** : > 20 transactions en 24h
- **Sévérité** : CRITICAL si > 30, sinon WARNING
- **Message** : Alerte sur une activité inhabituelle

#### Règle 5 : Budget Exceeded Alert
- **Condition** : Dépenses > 120% des revenus
- **Sévérité** : CRITICAL
- **Message** : Alerte sur le dépassement du budget

### 4. Recommandations intelligentes

Les recommandations sont générées selon l'analyse des données :

1. **Savings Account** : Si balance > 5000 et pas de compte épargne
2. **High Food Spending** : Si dépenses alimentaires > 30% du total
3. **Automatic Transfers** : Si balance > 2x revenus mensuels
4. **Budget Management** : Si dépenses > 80% des revenus
5. **Investment Opportunities** : Si balance > 10000 et revenus positifs
6. **Positive Feedback** : Si revenus > dépenses

### 5. Mapping des catégories

Mise en place d'un mapping entre `TransactionCategory` (frontend) et les noms de catégories du backend :

```typescript
private mapCategoryToBackendName(category: TransactionCategory): string {
  const mapping: Record<TransactionCategory, string> = {
    [TransactionCategory.FOOD]: 'Food & Dining',
    [TransactionCategory.TRANSPORT]: 'Transportation',
    [TransactionCategory.SHOPPING]: 'Shopping',
    // ...
  };
  return mapping[category] || 'Other';
}
```

## Avantages

### ✅ Cohérence avec le backend
- Les calculs simulent exactement la logique backend
- Les catégories correspondent aux valeurs attendues
- Les alertes suivent les règles métier réelles

### ✅ Données réalistes
- Basées sur les comptes et transactions réels
- Évoluent avec les données de l'utilisateur
- Reflètent fidèlement l'état du système

### ✅ Maintenabilité
- Pas de valeurs hardcodées
- Facile à adapter si les règles changent
- Code réutilisable et testable

### ✅ Performance
- Utilise `combineLatest` pour charger les données en parallèle
- Cache les alertes générées pour éviter les recalculs
- Délais simulés réalistes (200-300ms)

## Structure des données générées

### DashboardSummary
```typescript
{
  userId: string;
  currentBalance: number;        // Somme des comptes
  monthlySpending: number;        // Calculé depuis transactions
  monthlyIncome: number;          // Calculé depuis transactions
  transactionsThisMonth: number;  // Compté depuis transactions
  topCategories: CategoryBreakdown[]; // Calculé depuis transactions
  balanceTrend: BalanceTrend;     // Généré depuis comptes
  recentTransactions: RecentTransaction[]; // Depuis transactions
  generatedAt: string;
}
```

### CategoryBreakdown
```typescript
{
  category: string;      // Nom backend (ex: "Food & Dining")
  amount: number;        // Montant total
  count: number;        // Nombre de transactions
  percentage: number;    // Pourcentage (totalise 100%)
}
```

### Alert
```typescript
{
  alertId: string;
  userId: string;
  alertType: AlertType;
  severity: AlertSeverity;
  title: string;
  message: string;
  thresholdValue?: number;
  currentValue?: number;
  status: AlertStatus;
  triggeredAt: string;
  resolvedAt?: string;
  notified: boolean;
}
```

## Prochaines étapes

1. **Tests** : Vérifier que les données générées sont cohérentes
2. **Performance** : Optimiser si nécessaire pour de grandes quantités de transactions
3. **Règles métier** : Ajouter d'autres règles d'alertes si nécessaire
4. **Migration** : Quand le backend sera prêt, remplacer les mocks par les vrais appels HTTP

## Notes techniques

- Les mocks utilisent `combineLatest` pour charger les données en parallèle
- Les alertes sont mises en cache dans `mockAlerts` pour éviter les doublons
- Les pourcentages sont normalisés pour totaliser exactement 100%
- Les dates sont générées de manière réaliste (progression temporelle)
- Les variations dans les tendances sont limitées à ±2% pour rester réalistes

