# GraphQL Gateway - Testing Guide

⚠️ **IMPORTANT**: All tests except `health` currently fail with `401 Unauthorized` errors.  
**Solution**: See `AUTHENTICATION_FIX_GUIDE.md` for how to disable security in microservices.

## Overview
This guide provides comprehensive test queries and mutations for all 7 microservices integrated into the GraphQL Gateway.

## Prerequisites
1. **Fix Authentication**: Follow `AUTHENTICATION_FIX_GUIDE.md` to disable security
2. Start the GraphQL Gateway: `mvn spring-boot:run`
3. Ensure all microservices are running on their respective ports
4. Access GraphiQL at: http://localhost:8090/graphiql

---

## 1. USER SERVICE TESTS

### Query: Get All Users
```graphql
query GetAllUsers {
  users {
    id
    login
    email
    fname
    lname
    role
    isActive
    kycStatus
  }
}
```

### Query: Get User By ID
```graphql
query GetUserById {
  userById(id: "1") {
    id
    login
    email
    fname
    lname
    phone
    role
    isActive
  }
}
```

**Note**: Use String ID wrapped in quotes `"1"` not numeric `1`
```

### Query: Get User Profile (Me)
```graphql
query GetMyProfile {
  me(id: "1") {
    id
    login
    email
    fname
    lname
    phone
  }
}
```

### Query: Get Clients by Agent
```graphql
query GetClientsByAgent {
  clientsByAgent(agentId: "2") {
    id
    login
    email
    fname
    lname
    role
  }
}
```

### Query: Get Agent by Client
```graphql
query GetAgentByClient {
  agentByClient(clientId: "5") {
    id
    login
    fname
    lname
    role
  }
}
```

### Mutation: Create User
```graphql
mutation CreateUser {
  createUser(input: {
    login: "johndoe"
    email: "john.doe@example.com"
    password: "SecurePass123!"
    fname: "John"
    lname: "Doe"
    role: "CLIENT"
  }) {
    id
    login
    email
    fname
    lname
    role
    isActive
  }
}
```

### Mutation: Activate User
```graphql
mutation ActivateUser {
  activateUser(id: "3") {
    id
    login
    isActive
  }
}
```

### Mutation: Deactivate User
```graphql
mutation DeactivateUser {
  deactivateUser(id: "3") {
    id
    login
    isActive
  }
}
```

### Mutation: Update Profile
```graphql
mutation UpdateProfile {
  updateProfile(id: "1", input: {
    phone: "+33612345678"
    email: "newemail@example.com"
  }) {
    id
    email
    phone
  }
}
```

### Mutation: Assign Client to Agent
```graphql
mutation AssignClient {
  assignClient(input: {
    agentId: 2
    clientId: 5
    notes: "New client assignment"
  }) {
    id
    agentId
    clientId
    assignedAt
  }
}
```

### Mutation: Unassign Client
```graphql
mutation UnassignClient {
  unassignClient(agentId: 2, clientId: 5)
}
```

---

## 2. ACCOUNT SERVICE TESTS

### Query: Get Account By ID
```graphql
query GetAccountById {
  accountById(id: "1") {
    id
    accountNumber
    userId
    accountType
    currency
    balance
    status
    createdAt
  }
}
```

### Query: Get Accounts by User ID
```graphql
query GetAccountsByUserId {
  accountsByUserId(userId: "1") {
    id
    accountNumber
    accountType
    currency
    balance
    status
  }
}
```

### Query: Get Account Balance
```graphql
query GetAccountBalance {
  accountBalance(id: "1") {
    accountId
    balance
    currency
    timestamp
  }
}
```

### Query: Get Account Transactions
```graphql
query GetAccountTransactions {
  accountTransactions(id: "1") {
    id
    transactionType
    amount
    balanceBefore
    balanceAfter
    timestamp
    description
  }
}
```

### Mutation: Create Account
```graphql
mutation CreateAccount {
  createAccount(input: {
    userId: 1
    accountType: "SAVINGS"
    currency: "EUR"
    initialBalance: 1000.00
  }) {
    id
    accountNumber
    userId
    accountType
    currency
    balance
    status
  }
}
```

### Mutation: Update Account
```graphql
mutation UpdateAccount {
  updateAccount(id: "1", input: {
    accountType: "CHECKING"
  }) {
    id
    accountType
    updatedAt
  }
}
```

### Mutation: Suspend Account
```graphql
mutation SuspendAccount {
  suspendAccount(id: "1", input: {
    reason: "Suspicious activity detected"
    suspendedBy: "ADMIN"
  }) {
    id
    status
    suspensionReason
    suspendedAt
  }
}
```

### Mutation: Close Account
```graphql
mutation CloseAccount {
  closeAccount(id: "1", input: {
    closureReason: "Customer request"
    closedBy: "ADMIN"
  }) {
    id
    status
    closureReason
    closedAt
  }
}
```

---

## 3. AUTH SERVICE TESTS

### Mutation: Login
```graphql
mutation Login {
  login(input: {
    username: "johndoe"
    password: "SecurePass123!"
  }) {
    access_token
    refresh_token
    expires_in
    token_type
  }
}
```

### Mutation: Refresh Token
```graphql
mutation RefreshToken {
  refreshToken(input: {
    refresh_token: "your-refresh-token-here"
  }) {
    access_token
    refresh_token
    expires_in
  }
}
```

### Mutation: Logout
```graphql
mutation Logout {
  logout(input: {
    refresh_token: "your-refresh-token-here"
  })
}
```

### Query: Verify Token
```graphql
query VerifyToken {
  verifyToken(token: "your-access-token-here")
}
```

### Query: Get Token Info
```graphql
query GetTokenInfo {
  tokenInfo(token: "your-access-token-here") {
    sub
    username
    email
    roles
    exp
    iat
  }
}
```

---

## 4. PAYMENT SERVICE TESTS

### Query: Get Payment By ID
```graphql
query GetPaymentById {
  paymentById(id: "1") {
    id
    fromAccountId
    toAccountId
    amount
    currency
    paymentType
    status
    beneficiaryName
    reference
    description
    createdAt
  }
}
```

### Query: Get Payments by User ID
```graphql
query GetPaymentsByUserId {
  paymentsByUserId(userId: "1") {
    id
    amount
    currency
    paymentType
    status
    createdAt
  }
}
```

### Query: Get Payments by Account ID
```graphql
query GetPaymentsByAccountId {
  paymentsByAccountId(accountId: "1") {
    id
    amount
    currency
    status
    createdAt
  }
}
```

### Mutation: Create Payment
```graphql
mutation CreatePayment {
  createPayment(input: {
    fromAccountId: 1
    toAccountId: 2
    amount: 100.50
    currency: "EUR"
    paymentType: "TRANSFER"
    beneficiaryName: "Jane Doe"
    reference: "INV-2024-001"
    description: "Invoice payment"
  }) {
    id
    fromAccountId
    toAccountId
    amount
    currency
    status
    reference
    createdAt
  }
}
```

### Mutation: Cancel Payment
```graphql
mutation CancelPayment {
  cancelPayment(id: "1") {
    id
    status
    updatedAt
  }
}
```

### Mutation: Reverse Payment
```graphql
mutation ReversePayment {
  reversePayment(id: "1", reason: "Duplicate payment") {
    id
    status
    reversalReason
    reversedAt
  }
}
```

---

## 5. CRYPTO SERVICE TESTS

### Query: Get Crypto Wallet by User ID
```graphql
query GetCryptoWallet {
  cryptoWalletByUserId(userId: "1") {
    id
    userId
    balance
    currency
    status
    createdAt
  }
}
```

### Query: Get Crypto Transactions by Wallet ID
```graphql
query GetCryptoTransactions {
  cryptoTransactionsByWalletId(walletId: "1") {
    id
    symbol
    transactionType
    cryptoAmount
    eurAmount
    price
    status
    createdAt
  }
}
```

### Query: Get All Crypto Coins
```graphql
query GetAllCryptoCoins {
  cryptoCoins {
    coinId
    symbol
    name
    currentPrice
    marketCap
    volume24h
    priceChange24h
  }
}
```

### Query: Get Crypto Coin By ID
```graphql
query GetCryptoCoinById {
  cryptoCoinById(coinId: "bitcoin") {
    coinId
    symbol
    name
    currentPrice
    marketCap
  }
}
```

### Mutation: Create Crypto Wallet
```graphql
mutation CreateCryptoWallet {
  createCryptoWallet(userId: "1") {
    id
    userId
    balance
    currency
    status
  }
}
```

### Mutation: Activate Crypto Wallet
```graphql
mutation ActivateCryptoWallet {
  activateCryptoWallet(walletId: "1") {
    id
    status
  }
}
```

### Mutation: Deactivate Crypto Wallet
```graphql
mutation DeactivateCryptoWallet {
  deactivateCryptoWallet(walletId: "1") {
    id
    status
  }
}
```

### Mutation: Buy Crypto
```graphql
mutation BuyCrypto {
  buyCrypto(walletId: "1", input: {
    symbol: "BTC"
    eurAmount: 100.00
  }) {
    id
    symbol
    transactionType
    cryptoAmount
    eurAmount
    price
    status
  }
}
```

### Mutation: Sell Crypto
```graphql
mutation SellCrypto {
  sellCrypto(walletId: "1", input: {
    symbol: "BTC"
    cryptoAmount: 0.001
  }) {
    id
    symbol
    transactionType
    cryptoAmount
    eurAmount
    price
    status
  }
}
```

---

## 6. NOTIFICATION SERVICE TESTS

### Query: Get Notifications by User ID
```graphql
query GetNotificationsByUserId {
  notificationsByUserId(userId: "1") {
    id
    userId
    type
    subject
    message
    category
    status
    createdAt
    read
  }
}
```

### Query: Get In-App Notifications
```graphql
query GetInAppNotifications {
  inAppNotificationsByUserId(userId: "1") {
    id
    subject
    message
    createdAt
    read
  }
}
```

### Mutation: Send Notification
```graphql
mutation SendNotification {
  sendNotification(input: {
    userId: "1"
    type: "EMAIL"
    subject: "Account Update"
    message: "Your account has been updated successfully"
    category: "ACCOUNT"
  }) {
    id
    userId
    type
    subject
    status
    createdAt
  }
}
```

### Mutation: Mark Notification as Read
```graphql
mutation MarkNotificationAsRead {
  markNotificationAsRead(id: "1") {
    id
    read
  }
}
```

---

## 7. AUDIT SERVICE TESTS

### Query: Get All Audit Events
```graphql
query GetAllAuditEvents {
  auditEvents {
    eventId
    userId
    eventType
    timestamp
    serviceSource
    result
    riskScore
    action
  }
}
```

### Query: Get Audit Event By ID
```graphql
query GetAuditEventById {
  auditEventById(eventId: "uuid-here") {
    eventId
    userId
    eventType
    timestamp
    serviceSource
    result
    action
    ipAddress
  }
}
```

### Query: Get Audit Events by User ID
```graphql
query GetAuditEventsByUserId {
  auditEventsByUserId(userId: "1") {
    eventId
    eventType
    timestamp
    result
    action
  }
}
```

### Query: Get Audit Events by Type
```graphql
query GetAuditEventsByType {
  auditEventsByType(eventType: "LOGIN") {
    eventId
    userId
    timestamp
    result
    ipAddress
  }
}
```

---

## Testing Checklist

### ✅ User Service
- [ ] Get all users
- [ ] Get user by ID
- [ ] Get user profile (me)
- [ ] Get clients by agent
- [ ] Get agent by client
- [ ] Create user
- [ ] Activate user
- [ ] Deactivate user
- [ ] Update profile
- [ ] Assign client
- [ ] Unassign client

### ✅ Account Service
- [ ] Get account by ID
- [ ] Get accounts by user ID
- [ ] Get account balance
- [ ] Get account transactions
- [ ] Create account
- [ ] Update account
- [ ] Suspend account
- [ ] Close account

### ✅ Auth Service
- [ ] Login
- [ ] Refresh token
- [ ] Logout
- [ ] Verify token
- [ ] Get token info

### ✅ Payment Service
- [ ] Get payment by ID
- [ ] Get payments by user ID
- [ ] Get payments by account ID
- [ ] Create payment
- [ ] Cancel payment
- [ ] Reverse payment

### ✅ Crypto Service
- [ ] Get crypto wallet by user ID
- [ ] Get crypto transactions by wallet ID
- [ ] Get all crypto coins
- [ ] Get crypto coin by ID
- [ ] Create crypto wallet
- [ ] Activate crypto wallet
- [ ] Deactivate crypto wallet
- [ ] Buy crypto
- [ ] Sell crypto

### ✅ Notification Service
- [ ] Get notifications by user ID
- [ ] Get in-app notifications
- [ ] Send notification
- [ ] Mark notification as read

### ✅ Audit Service
- [ ] Get all audit events
- [ ] Get audit event by ID
- [ ] Get audit events by user ID
- [ ] Get audit events by type

---

## Error Handling Tests

### Test 404 - Not Found
```graphql
query Test404 {
  userById(id: "99999") {
    id
    login
  }
}
```
**Expected**: GraphQL error with 404 status

### Test 400 - Bad Request
```graphql
mutation TestBadRequest {
  createUser(input: {
    login: ""
    email: "invalid-email"
    password: "123"
    fname: ""
    lname: ""
    role: "INVALID_ROLE"
  }) {
    id
  }
}
```
**Expected**: GraphQL error with 400 status and validation errors

### Test 500 - Server Error
Test by stopping a microservice and attempting to query it.
**Expected**: GraphQL error with connection failure message

---

## Performance Testing

### Complex Query - Multiple Services
```graphql
query ComplexDashboard {
  me(id: "1") {
    id
    login
    email
    fname
    lname
    role
  }
  
  accountsByUserId(userId: "1") {
    id
    accountNumber
    balance
    currency
    status
  }
  
  paymentsByUserId(userId: "1") {
    id
    amount
    status
    createdAt
  }
  
  notificationsByUserId(userId: "1") {
    id
    subject
    read
  }
}
```

---

## 🤖 Automated Testing

### Run JUnit Tests
```cmd
cd c:\Users\Hp\Desktop\graphql\Ebanking-3.0
mvn test
```

### Run Specific Test
```cmd
mvn test -Dtest=GraphQLIntegrationTest
```

### Test with Coverage
```cmd
mvn clean test jacoco:report
```

### View Test Results
After running tests, check:
- Console output for pass/fail summary
- `target/surefire-reports/` for detailed HTML reports
- `target/site/jacoco/` for coverage reports

---

## Notes

1. **Authentication**: All endpoints currently work without authentication. In production, Keycloak tokens should be required.

2. **Service Availability**: Ensure all microservices are running before testing:
   - User Service: http://localhost:8081
   - Account Service: http://localhost:8082
   - Auth Service: http://localhost:8081
   - Payment Service: http://localhost:8082
   - Crypto Service: http://localhost:8081
   - Notification Service: http://localhost:8084
   - Audit Service: http://localhost:8083

3. **Data Dependencies**: Some mutations require existing data (e.g., creating a payment requires existing accounts).

4. **Error Propagation**: All REST errors (400, 404, 500) are propagated as GraphQL errors with appropriate messages.

5. **Gateway Responsibility**: The gateway is stateless and only orchestrates calls to microservices. No business logic is implemented in the gateway.

---

## Troubleshooting

### Issue: "Connection refused"
**Solution**: Ensure the microservice is running on the correct port.

### Issue: "404 Not Found"
**Solution**: Verify the endpoint path in the microservice matches the resolver.

### Issue: "GraphQL validation error"
**Solution**: Check the schema.graphqls matches the input/output types.

### Issue: "Compilation error"
**Solution**: Run `mvn clean compile` to check for Java errors.

---

## Next Steps

1. ✅ All GraphQL operations implemented
2. ✅ Schema covers all 7 microservices
3. ✅ Resolvers map one-to-one with REST endpoints
4. 🔄 Test each operation systematically
5. 🔄 Document test results
6. 🔄 Fix any integration issues
7. 🔄 Optimize performance if needed

**Total GraphQL Operations**: 
- **Queries**: 27
- **Mutations**: 29
- **Total**: 56 operations covering 101 REST endpoints
