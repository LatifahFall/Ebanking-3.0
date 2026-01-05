# 📊 Analytics Service Integration Guide

## ✅ Integration Complete

The GraphQL Gateway has been successfully extended with **Analytics Service** integration.

---

## 📋 What Was Added

### 1️⃣ **7 New DTOs**
- `AlertDTO.java` - Alert entities with severity, type, status
- `DashboardSummaryDTO.java` - Comprehensive dashboard metrics
- `CategoryBreakdownDTO.java` - Spending category analysis
- `BalanceTrendDTO.java` - Balance trend over time
- `DataPointDTO.java` - Time-series data points
- `RecentTransactionDTO.java` - Recent transaction details
- `AdminOverviewDTO.java` - Admin-level metrics

### 2️⃣ **6 New Queries**
- `activeAlerts(userId)` - Get active alerts for a user
- `dashboardSummary(userId)` - Get comprehensive dashboard summary
- `spendingBreakdown(userId, period)` - Get spending by category
- `balanceTrend(userId, days)` - Get balance trend over time
- `recommendations(userId)` - Get personalized recommendations
- `adminOverview()` - Get system-wide metrics (ADMIN only)

### 3️⃣ **1 New Mutation**
- `resolveAlert(alertId)` - Mark an alert as resolved

---

## 🔌 Analytics Service Details

**Port:** 8087  
**Base URL:** `http://localhost:8087/api/v1`  
**Context Path:** `/api/v1`

**REST Endpoints Mapped:**
```
GET  /analytics/alerts/active
POST /analytics/alerts/{alertId}/resolve
GET  /analytics/dashboard/summary
GET  /analytics/spending/breakdown
GET  /analytics/trends/balance
GET  /analytics/insights/recommendations
GET  /analytics/admin/overview
```

---

## 🧪 Testing

### Run Analytics Tests
```powershell
powershell -ExecutionPolicy Bypass -File test-analytics.ps1
```

### Sample GraphQL Queries

#### Get Active Alerts
```graphql
{
  activeAlerts(userId: "test-user-123") {
    alertId
    userId
    alertType
    severity
    title
    message
    status
    triggeredAt
  }
}
```

#### Get Dashboard Summary
```graphql
{
  dashboardSummary(userId: "test-user-123") {
    userId
    currentBalance
    monthlySpending
    monthlyIncome
    transactionsThisMonth
    generatedAt
  }
}
```

#### Get Spending Breakdown
```graphql
{
  spendingBreakdown(userId: "test-user-123", period: "MONTH") {
    category
    amount
    count
    percentage
  }
}
```

#### Get Balance Trend
```graphql
{
  balanceTrend(userId: "test-user-123", days: 30) {
    period
    dataPoints {
      timestamp
      value
    }
  }
}
```

#### Get Recommendations
```graphql
{
  recommendations(userId: "test-user-123")
}
```

#### Get Admin Overview (ADMIN only)
```graphql
{
  adminOverview {
    activeUsers
    totalTransactions
    revenue
  }
}
```

### Sample Mutation

#### Resolve Alert
```graphql
mutation {
  resolveAlert(alertId: "alert-123")
}
```

---

## 🔐 Security & Authentication

The analytics service requires authentication for all endpoints:

- **User Endpoints:** Require valid JWT token with matching userId or ADMIN role
- **Admin Endpoints:** Require ROLE_ADMIN

The GraphQL Gateway automatically forwards the `Authorization` header to the analytics service.

**How to Test with Authentication:**

1. Login via auth-service to get token:
```graphql
mutation {
  login(input: {username: "testuser", password: "password"}) {
    access_token
  }
}
```

2. Use token in subsequent requests:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{"query":"{ activeAlerts(userId: \"test-user-123\") { alertId title } }"}'
```

---

## 📊 Complete Coverage

The GraphQL Gateway now covers **8 microservices**:

1. ✅ User Service (8081)
2. ✅ Account Service (8082)
3. ✅ Auth Service (8081)
4. ✅ Payment Service (8082)
5. ✅ Crypto Service (8081)
6. ✅ Notification Service (8084)
7. ✅ Audit Service (8083)
8. ✅ **Analytics Service (8087)** ← NEW!

**Total Operations:** 57 (33 queries + 24 mutations)

---

## 🚀 Next Steps

1. **Start Analytics Service:**
   ```bash
   cd c:\Users\Hp\Desktop\graphql\analytics-service
   mvn spring-boot:run
   ```

2. **Start GraphQL Gateway:**
   ```bash
   cd c:\Users\Hp\Desktop\graphql\Ebanking-3.0
   java -jar target\graphql-gateway-0.0.1-SNAPSHOT.jar
   ```

3. **Run Tests:**
   ```powershell
   powershell -ExecutionPolicy Bypass -File test-analytics.ps1
   ```

4. **Access GraphiQL:**
   ```
   http://localhost:8090/graphiql
   ```

---

## 📁 Files Modified/Created

### Created:
- `src/main/java/com/bank/graphql_gateway/model/AlertDTO.java`
- `src/main/java/com/bank/graphql_gateway/model/DashboardSummaryDTO.java`
- `src/main/java/com/bank/graphql_gateway/model/CategoryBreakdownDTO.java`
- `src/main/java/com/bank/graphql_gateway/model/BalanceTrendDTO.java`
- `src/main/java/com/bank/graphql_gateway/model/DataPointDTO.java`
- `src/main/java/com/bank/graphql_gateway/model/RecentTransactionDTO.java`
- `src/main/java/com/bank/graphql_gateway/model/AdminOverviewDTO.java`
- `test-analytics.ps1`
- `ANALYTICS_INTEGRATION.md` (this file)

### Modified:
- `src/main/java/com/bank/graphql_gateway/resolver/QueryResolver.java` (+6 queries)
- `src/main/java/com/bank/graphql_gateway/resolver/MutationResolver.java` (+1 mutation)
- `src/main/resources/graphql/schema.graphqls` (+7 types, +6 queries, +1 mutation)
- `src/main/resources/application.properties` (+1 service URL)

---

## ✅ Build Status

```
[INFO] BUILD SUCCESS
[INFO] Total time: 18.856 s
[INFO] Compiling 40 source files (7 new analytics DTOs)
```

**Status:** 🟢 **READY TO USE**

---

*Last Updated: January 5, 2026*
