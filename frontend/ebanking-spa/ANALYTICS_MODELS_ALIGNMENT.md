# Analytics Models Alignment Documentation

Ce document décrit l'alignement entre les DTOs du backend Analytics Service (Java) et les modèles TypeScript du frontend.

## Vue d'ensemble

Tous les modèles dans `src/app/models/analytics.model.ts` sont alignés exactement avec les DTOs Java du backend Analytics Service.

**Backend Location:** `services/analytics-service/src/main/java/com/banking/analytics/`

**Frontend Location:** `frontend/ebanking-spa/src/app/models/analytics.model.ts`

---

## Mapping des Types Java → TypeScript

| Type Java | Type TypeScript | Notes |
|-----------|----------------|-------|
| `String` | `string` | Direct mapping |
| `Integer` | `number` | Direct mapping |
| `Long` | `number` | Direct mapping |
| `Double` | `number` | Direct mapping |
| `BigDecimal` | `number` | Direct mapping (precision handled by backend) |
| `LocalDateTime` | `string` | ISO 8601 format string (e.g., "2024-01-15T10:30:00") |
| `boolean` | `boolean` | Direct mapping |
| `List<T>` | `T[]` | Array type |
| `Enum` | `enum` | TypeScript enum with same values |

---

## DTOs Mapping

### 1. DataPoint

**Backend:** `com.banking.analytics.dto.DataPoint`

```java
public class DataPoint {
    private LocalDateTime timestamp;
    private BigDecimal value;
}
```

**Frontend:** `DataPoint`

```typescript
export interface DataPoint {
  timestamp: string; // LocalDateTime -> ISO string
  value: number; // BigDecimal -> number
}
```

**Usage:** Used in `BalanceTrend` to represent time series data points.

---

### 2. RecentTransaction

**Backend:** `com.banking.analytics.dto.RecentTransaction`

```java
public class RecentTransaction {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String merchant;
    private LocalDateTime date;
}
```

**Frontend:** `RecentTransaction`

```typescript
export interface RecentTransaction {
  transactionId: string;
  type: string;
  amount: number; // BigDecimal -> number
  merchant: string;
  date: string; // LocalDateTime -> ISO string
}
```

**Usage:** Used in `DashboardSummary.recentTransactions` to show recent transaction history.

---

### 3. CategoryBreakdown

**Backend:** `com.banking.analytics.dto.CategoryBreakdown`

```java
public class CategoryBreakdown {
    private String category;
    private BigDecimal amount;
    private Integer count;
    private Double percentage;
}
```

**Frontend:** `CategoryBreakdown`

```typescript
export interface CategoryBreakdown {
  category: string;
  amount: number; // BigDecimal -> number
  count: number; // Integer -> number
  percentage: number; // Double -> number
}
```

**Usage:** Used in `DashboardSummary.topCategories` and returned by `/analytics/spending/breakdown` endpoint.

---

### 4. BalanceTrend

**Backend:** `com.banking.analytics.dto.BalancedTrend.BalanceTrend` (inner class)

```java
public static class BalanceTrend {
    private String period;
    private List<DataPoint> dataPoints;
}
```

**Frontend:** `BalanceTrend`

```typescript
export interface BalanceTrend {
  period: string; // e.g., "30 days"
  dataPoints: DataPoint[];
}
```

**Usage:** Used in `DashboardSummary.balanceTrend` and returned by `/analytics/trends/balance` endpoint.

---

### 5. DashboardSummary

**Backend:** `com.banking.analytics.dto.DashboardSummary`

```java
public class DashboardSummary {
    private String userId;
    private BigDecimal currentBalance;
    private BigDecimal monthlySpending;
    private BigDecimal monthlyIncome;
    private Integer transactionsThisMonth;
    private List<CategoryBreakdown> topCategories;
    private BalancedTrend.BalanceTrend balanceTrend;
    private List<RecentTransaction> recentTransactions;
    private LocalDateTime generatedAt;
}
```

**Frontend:** `DashboardSummary`

```typescript
export interface DashboardSummary {
  userId: string;
  currentBalance: number; // BigDecimal -> number
  monthlySpending: number; // BigDecimal -> number
  monthlyIncome: number; // BigDecimal -> number
  transactionsThisMonth: number; // Integer -> number
  topCategories?: CategoryBreakdown[]; // Optional
  balanceTrend?: BalanceTrend; // Optional
  recentTransactions?: RecentTransaction[]; // Optional
  generatedAt: string; // LocalDateTime -> ISO string
}
```

**Endpoint:** `GET /api/v1/analytics/dashboard/summary?userId={userId}`

**Note:** `topCategories`, `balanceTrend`, and `recentTransactions` are optional in the frontend model to handle cases where they might not be included in the response.

---

### 6. AdminOverview

**Backend:** `com.banking.analytics.dto.AdminOverview`

```java
public class AdminOverview {
    private Long activeUsers;
    private Long totalTransactions;
    private BigDecimal revenue;
}
```

**Frontend:** `AdminOverview`

```typescript
export interface AdminOverview {
  activeUsers: number; // Long -> number
  totalTransactions: number; // Long -> number
  revenue: number; // BigDecimal -> number
}
```

**Endpoint:** `GET /api/v1/analytics/admin/overview` (Admin only)

---

### 7. Alert

**Backend:** `com.banking.analytics.model.Alert` (Entity)

```java
public class Alert {
    private String alertId; // UUID
    private String userId;
    private AlertType alertType;
    private Severity severity;
    private String title;
    private String message;
    private BigDecimal thresholdValue; // Optional
    private BigDecimal currentValue; // Optional
    private AlertStatus status;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt; // Optional
    private boolean notified;
}
```

**Frontend:** `Alert`

```typescript
export interface Alert {
  alertId: string; // UUID
  userId: string;
  alertType: AlertType;
  severity: AlertSeverity;
  title: string;
  message: string;
  thresholdValue?: number; // BigDecimal -> number, optional
  currentValue?: number; // BigDecimal -> number, optional
  status: AlertStatus;
  triggeredAt: string; // LocalDateTime -> ISO string
  resolvedAt?: string; // LocalDateTime -> ISO string, optional
  notified: boolean;
}
```

**Endpoints:**
- `GET /api/v1/analytics/alerts/active?userId={userId}` - Get active alerts
- `POST /api/v1/analytics/alerts/{alertId}/resolve` - Resolve an alert

---

## Enums Mapping

### AlertType

**Backend:** `com.banking.analytics.model.Alert.AlertType`

```java
public enum AlertType {
    SPENDING_THRESHOLD,
    LOW_BALANCE,
    UNUSUAL_ACTIVITY,
    BUDGET_EXCEEDED,
    LARGE_TRANSACTION,
    FREQUENT_TRANSACTIONS,
    SUSPICIOUS_LOGIN
}
```

**Frontend:** `AlertType`

```typescript
export enum AlertType {
  SPENDING_THRESHOLD = 'SPENDING_THRESHOLD',
  LOW_BALANCE = 'LOW_BALANCE',
  UNUSUAL_ACTIVITY = 'UNUSUAL_ACTIVITY',
  BUDGET_EXCEEDED = 'BUDGET_EXCEEDED',
  LARGE_TRANSACTION = 'LARGE_TRANSACTION',
  FREQUENT_TRANSACTIONS = 'FREQUENT_TRANSACTIONS',
  SUSPICIOUS_LOGIN = 'SUSPICIOUS_LOGIN'
}
```

---

### Severity

**Backend:** `com.banking.analytics.model.Alert.Severity`

```java
public enum Severity {
    INFO,
    WARNING,
    CRITICAL
}
```

**Frontend:** `AlertSeverity`

```typescript
export enum AlertSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL'
}
```

**Note:** Named `AlertSeverity` in frontend to avoid conflicts with other severity types.

---

### AlertStatus

**Backend:** `com.banking.analytics.model.Alert.AlertStatus`

```java
public enum AlertStatus {
    ACTIVE,
    RESOLVED,
    IGNORED
}
```

**Frontend:** `AlertStatus`

```typescript
export enum AlertStatus {
  ACTIVE = 'ACTIVE',
  RESOLVED = 'RESOLVED',
  IGNORED = 'IGNORED'
}
```

---

## Helper Functions

Le fichier `analytics.model.ts` inclut des fonctions utilitaires pour faciliter l'utilisation des modèles :

- `formatAlertType(type: AlertType): string` - Format alert type for display
- `getAlertSeverityColor(severity: AlertSeverity): string` - Get color for alert severity
- `isAlertActive(alert: Alert): boolean` - Check if alert is active

---

## Endpoints Backend Correspondants

| Endpoint | Method | Request | Response | Model |
|----------|--------|---------|----------|-------|
| `/api/v1/analytics/dashboard/summary` | GET | `?userId={userId}` | `DashboardSummary` | `DashboardSummary` |
| `/api/v1/analytics/spending/breakdown` | GET | `?userId={userId}&period={MONTH\|WEEK}` | `List<CategoryBreakdown>` | `CategoryBreakdown[]` |
| `/api/v1/analytics/trends/balance` | GET | `?userId={userId}&days={30}` | `BalanceTrend` | `BalanceTrend` |
| `/api/v1/analytics/insights/recommendations` | GET | `?userId={userId}` | `List<String>` | `string[]` |
| `/api/v1/analytics/admin/overview` | GET | - | `AdminOverview` | `AdminOverview` |
| `/api/v1/analytics/alerts/active` | GET | `?userId={userId}` | `List<Alert>` | `Alert[]` |
| `/api/v1/analytics/alerts/{alertId}/resolve` | POST | - | `void` | - |

---

## Notes Importantes

1. **Dates:** Toutes les dates `LocalDateTime` sont converties en chaînes ISO 8601 (`string`) dans le frontend. Utiliser `new Date(dateString)` pour les convertir en objets Date si nécessaire.

2. **BigDecimal:** Les valeurs `BigDecimal` sont converties en `number` dans le frontend. La précision est gérée par le backend.

3. **Optional Fields:** Certains champs sont marqués comme optionnels (`?`) dans le frontend pour gérer les cas où le backend ne les inclut pas toujours.

4. **Enum Values:** Les valeurs des enums TypeScript correspondent exactement aux valeurs Java (même casse).

5. **Naming:** Les noms de propriétés correspondent exactement aux noms Java (camelCase).

---

## Migration Future

Lors de la connexion au backend réel :

1. ✅ Les modèles sont déjà alignés - pas de changement nécessaire
2. ⚠️ Vérifier que les dates sont bien au format ISO 8601
3. ⚠️ Vérifier la gestion des valeurs null/undefined pour les champs optionnels
4. ⚠️ S'assurer que les enums correspondent exactement (comparer les valeurs)

---

**Dernière mise à jour:** Phase 1 - Analyse et alignement des modèles

