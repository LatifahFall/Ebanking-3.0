# 🔗 Backend API Mapping - E-Banking 3.0

## 📋 Vue d'ensemble

Ce document mappe **tous les endpoints backend** analysés vers les **services frontend mock**.  
Les mocks respectent **exactement** la structure des DTOs, la logique métier et les endpoints réels.

---

## 🔐 AUTH SERVICE (Port 8081)

### Base URL
```
http://localhost:8081/auth
```

### Endpoints

#### 1. Login
```http
POST /auth/login
```
**Request (LoginRequest):**
```typescript
{
  username: string;  // @NotBlank
  password: string;  // @NotBlank
}
```
**Response (TokenResponse):**
```typescript
{
  access_token: string;
  refresh_token: string;
  expires_in: number;       // seconds
  refresh_expires_in: number;
  token_type: string;       // "Bearer"
  scope: string;
}
```

#### 2. Register
```http
POST /auth/register
```
**Request (RegisterRequest):**
```typescript
{
  username: string;    // @Size(min=3, max=50)
  email: string;       // @Email
  password: string;    // @Size(min=8)
  firstName: string;   // @NotBlank
  lastName: string;    // @NotBlank
  phoneNumber?: string;
}
```
**Response (RegisterResponse):**
```typescript
{
  success: boolean;
  message: string;
  userId: string;
}
```

#### 3. Refresh Token
```http
POST /auth/refresh
```
**Request (RefreshRequest):**
```typescript
{
  refresh_token: string;
}
```
**Response (TokenResponse):** Même structure que login

#### 4. Logout
```http
POST /auth/logout
```
**Request (RefreshRequest):**
```typescript
{
  refresh_token: string;
}
```
**Response:**
```typescript
{
  message: string;  // "Logged out successfully"
}
```

#### 5. Verify Token
```http
POST /auth/verify-token
```
**Request (TokenRequest):**
```typescript
{
  token: string;
}
```
**Response:**
```typescript
{
  valid: boolean;
}
```

#### 6. Token Info
```http
POST /auth/token-info
```
**Request (TokenRequest):**
```typescript
{
  token: string;
}
```
**Response (TokenInfo):**
```typescript
{
  sub: string;           // userId
  email: string;
  preferred_username: string;
  given_name: string;
  family_name: string;
  roles: string[];
  exp: number;           // timestamp
  iat: number;           // timestamp
}
```

---

## 👤 USER SERVICE (Port 8084)

### Endpoints fournis dans la demande

#### 1. Admin - Create User
```http
POST /admin/users
```

#### 2. Admin - Get User By Id
```http
GET /admin/users/{userId}
```

#### 3. Admin - Update User
```http
PUT /admin/users/{userId}
```

#### 4. Admin - Assign Client to Agent
```http
POST /admin/users/assignments
```

#### 5. Admin - Unassign Client
```http
DELETE /admin/users/assignments?clientId={id}&agentId={id}
```

#### 6. Admin - Search Users
```http
GET /admin/users/search?q={query}&role={role}&page={page}&size={size}
```

#### 7. Admin - Get Client's Agent
```http
GET /admin/users/clients/{clientId}/agent
```

#### 8. Admin - Get Agent's Clients
```http
GET /admin/users/agents/{agentId}/clients
```

#### 9. Admin - Activate User
```http
PATCH /admin/users/activate?userId={userId}
```

#### 10. Admin - Deactivate User
```http
PATCH /admin/users/deactivate?userId={userId}
```

#### 11. Me - Get My Profile
```http
GET /me/{userId}
```

#### 12. Me - Update My Profile
```http
PUT /me/{userId}
```

#### 13. Me - Set Last Login
```http
PATCH /me/{userId}/last-login
```

#### 14. Me - Get Preferences
```http
GET /me/{userId}/preferences
```

#### 15. Me - Update Preferences
```http
PUT /me/{userId}/preferences
```

#### 16. Me - Authenticate
```http
POST /me/login?login={login}&password={password}
```

#### 17. Agent - Create Assigned Client
```http
POST /agent/clients/{agentId}
```

#### 18. Agent - Update Client Profile
```http
PUT /agent/clients/{agentId}?clientId={clientId}
```

#### 19. Agent - Get Client Profile
```http
GET /agent/clients/{agentId}?clientId={clientId}
```

#### 20. Agent - Search Clients
```http
GET /agent/clients/{agentId}/search?q={query}&page={page}&size={size}
```

#### 21. Agent - Activate Client
```http
PATCH /agent/clients/{agentId}/activate?clientId={clientId}
```

#### 22. Agent - Deactivate Client
```http
PATCH /agent/clients/{agentId}/deactivate?clientId={clientId}
```

---

## 🏦 ACCOUNT SERVICE (Port 8082)

### Base URL
```
http://localhost:8082/api/accounts
```

### Endpoints

#### 1. Create Account
```http
POST /api/accounts
```
**Request (CreateAccountRequest):**
```typescript
{
  userId: number;
  accountType: 'CHECKING' | 'SAVINGS' | 'INVESTMENT' | 'CRYPTO';
  currency: string;
  initialBalance?: BigDecimal;
}
```

#### 2. Get Account By ID
```http
GET /api/accounts/{id}
```

#### 3. Get Accounts By User
```http
GET /api/accounts?userId={userId}
```

#### 4. Update Account
```http
PUT /api/accounts/{id}
```

#### 5. Suspend Account
```http
POST /api/accounts/{id}/suspend
```
**Request (SuspendAccountRequest):**
```typescript
{
  reason: string;
  suspendedBy: number;
}
```

#### 6. Close Account
```http
POST /api/accounts/{id}/close
```
**Request (CloseAccountRequest):**
```typescript
{
  closureReason: string;
  closedBy: number;
}
```

#### 7. Get Balance
```http
GET /api/accounts/{id}/balance
```
**Response (BalanceResponse):**
```typescript
{
  accountId: number;
  balance: BigDecimal;
  currency: string;
  timestamp: LocalDateTime;
}
```

#### 8. Get Transaction History
```http
GET /api/accounts/{id}/transactions?limit={limit}
```
**Response:** List<TransactionResponse>

#### 9. Get Account Statement
```http
GET /api/accounts/{id}/statement?startDate={date}&endDate={date}
```

---

## 💸 PAYMENT SERVICE (Port 8083)

### Base URL
```
http://localhost:8083/api/payments
```

### Endpoints

#### 1. Initiate Payment
```http
POST /api/payments
```
**Request (PaymentRequest):**
```typescript
{
  fromAccountId: UUID;
  toAccountId?: UUID;
  amount: BigDecimal;        // @DecimalMin("0.01")
  currency: string;
  paymentType: 'TRANSFER' | 'BILL_PAYMENT' | 'MOBILE_TOP_UP' | ...;
  beneficiaryName?: string;
  reference?: string;
  description?: string;
}
```

#### 2. Get Payment
```http
GET /api/payments/{id}
```

#### 3. Get Payments (Paginated)
```http
GET /api/payments?accountId={id}&status={status}&page={page}&size={size}&sortBy={field}&sortDir={ASC|DESC}
```

#### 4. Cancel Payment
```http
POST /api/payments/{id}/cancel
```

#### 5. Reverse Payment
```http
POST /api/payments/{id}/reverse?reason={reason}
```

---

## 🔔 NOTIFICATION SERVICE (Port 8085)

### Kafka Events Consommés

#### Depuis user-service:
- `user.created`
- `user.updated`
- `user.activated`
- `user.deactivated`
- `client.assigned`
- `client.unassigned`

#### Frontend Polling:
```http
GET /api/notifications/unread
GET /api/notifications
POST /api/notifications/{id}/mark-read
POST /api/notifications/mark-all-read
```

---

## 📊 Structure des DTOs Frontend

### User
```typescript
interface User {
  id: string | number;
  login: string;
  email: string;
  fname: string;
  lname: string;
  phone: string;
  cin: string;
  address: string;
  role: 'ADMIN' | 'AGENT' | 'CLIENT';
  isActive: boolean;
  kycStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  gdprConsent: boolean;
  createdAt: string;
  updatedAt: string;
  lastLogin: string;
}
```

### Account
```typescript
interface Account {
  id: number;
  accountNumber: string;
  userId: number;
  accountType: 'CHECKING' | 'SAVINGS' | 'INVESTMENT' | 'CRYPTO';
  currency: string;
  balance: number;
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
  suspendedAt?: string;
  closedAt?: string;
}
```

### Payment
```typescript
interface Payment {
  id: string;
  fromAccountId: string;
  toAccountId?: string;
  amount: number;
  currency: string;
  paymentType: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  beneficiaryName?: string;
  reference?: string;
  description?: string;
  createdAt: string;
  completedAt?: string;
}
```

---

## 🎯 Frontend Service Mock Structure

```
src/app/core/services/
├── auth.service.ts          → auth-service endpoints
├── user.service.ts          → user-service endpoints (/admin, /me, /agent)
├── account.service.ts       → account-service endpoints
├── payment.service.ts       → payment-service endpoints
└── notification.service.ts  → notification polling + Kafka events simulation
```

---

**Date**: January 2, 2026  
**Status**: ✅ API complètement mappée depuis le backend  
**Objectif**: Mock respectant 100% la structure backend en attendant Keycloak + GraphQL
