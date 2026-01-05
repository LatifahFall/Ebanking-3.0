# 🧪 TESTS GRAPHQL COMPLETS - E-BANKING 3.0

**Date**: 5 Janvier 2026  
**Endpoint GraphQL**: `http://localhost:8090/graphql`  
**Méthode**: POST  
**Content-Type**: application/json

---

## 📋 Table des Matières

1. [User Service - 5 Queries + 6 Mutations](#user-service)
2. [Account Service - 4 Queries + 4 Mutations](#account-service)
3. [Auth Service - 2 Queries + 3 Mutations](#auth-service)
4. [Payment Service - 3 Queries + 3 Mutations](#payment-service)
5. [Crypto Service - 4 Queries + 5 Mutations](#crypto-service)
6. [Notification Service - 2 Queries + 2 Mutations](#notification-service)
7. [Audit Service - 4 Queries](#audit-service)
8. [Analytics Service - 6 Queries + 1 Mutation](#analytics-service)

---

## 🔐 Headers Requis

```http
Content-Type: application/json
Authorization: Bearer <votre_token_jwt>
```

---

# 1. USER SERVICE

## 📖 QUERIES (5)

### 1.1. Liste de tous les utilisateurs

```graphql
query GetAllUsers {
  users {
    id
    username
    email
    firstName
    lastName
    role
    status
    createdAt
    lastLogin
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"query":"query { users { id username email firstName lastName role status } }"}'
```

---

### 1.2. Utilisateur par ID

```graphql
query GetUserById($id: ID!) {
  userById(id: $id) {
    id
    username
    email
    firstName
    lastName
    phoneNumber
    address
    role
    status
    createdAt
    updatedAt
    lastLogin
  }
}
```

**Variables**:
```json
{
  "id": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($id: ID!) { userById(id: $id) { id username email firstName lastName role } }",
    "variables": {"id": 1}
  }'
```

---

### 1.3. Profil utilisateur connecté

```graphql
query GetMyProfile {
  me {
    id
    username
    email
    firstName
    lastName
    phoneNumber
    address
    role
    status
    createdAt
    lastLogin
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"query":"query { me { id username email firstName lastName role status } }"}'
```

---

### 1.4. Clients assignés à un agent

```graphql
query GetClientsByAgent($agentId: ID!) {
  clientsByAgent(agentId: $agentId) {
    id
    username
    email
    firstName
    lastName
    role
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "agentId": 2
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($agentId: ID!) { clientsByAgent(agentId: $agentId) { id username email firstName lastName } }",
    "variables": {"agentId": 2}
  }'
```

---

### 1.5. Agent assigné à un client

```graphql
query GetAgentByClient($clientId: ID!) {
  agentByClient(clientId: $clientId) {
    id
    username
    email
    firstName
    lastName
    phoneNumber
    role
    status
  }
}
```

**Variables**:
```json
{
  "clientId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($clientId: ID!) { agentByClient(clientId: $clientId) { id username email firstName lastName role } }",
    "variables": {"clientId": 3}
  }'
```

---

## ✏️ MUTATIONS (6)

### 1.6. Créer un utilisateur

```graphql
mutation CreateUser($input: CreateUserInput!) {
  createUser(input: $input) {
    id
    username
    email
    firstName
    lastName
    role
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "input": {
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+33612345678",
    "address": "123 Main St, Paris",
    "role": "CLIENT"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($input: CreateUserInput!) { createUser(input: $input) { id username email role status } }",
    "variables": {
      "input": {
        "username": "john.doe",
        "email": "john.doe@example.com",
        "password": "SecurePass123!",
        "firstName": "John",
        "lastName": "Doe",
        "role": "CLIENT"
      }
    }
  }'
```

---

### 1.7. Activer un utilisateur

```graphql
mutation ActivateUser($id: ID!) {
  activateUser(id: $id) {
    id
    username
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($id: ID!) { activateUser(id: $id) { id username status } }",
    "variables": {"id": 5}
  }'
```

---

### 1.8. Désactiver un utilisateur

```graphql
mutation DeactivateUser($id: ID!) {
  deactivateUser(id: $id) {
    id
    username
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($id: ID!) { deactivateUser(id: $id) { id username status } }",
    "variables": {"id": 5}
  }'
```

---

### 1.9. Mettre à jour le profil

```graphql
mutation UpdateProfile($id: ID!, $input: UpdateProfileInput!) {
  updateProfile(id: $id, input: $input) {
    id
    username
    email
    firstName
    lastName
    phoneNumber
    address
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 3,
  "input": {
    "firstName": "Jean",
    "lastName": "Dupont",
    "phoneNumber": "+33687654321",
    "address": "456 Avenue des Champs, Lyon"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($id: ID!, $input: UpdateProfileInput!) { updateProfile(id: $id, input: $input) { id firstName lastName phoneNumber } }",
    "variables": {
      "id": 3,
      "input": {
        "firstName": "Jean",
        "lastName": "Dupont",
        "phoneNumber": "+33687654321"
      }
    }
  }'
```

---

### 1.10. Assigner un client à un agent

```graphql
mutation AssignClient($input: AssignClientInput!) {
  assignClient(input: $input) {
    id
    agentId
    clientId
    assignedAt
  }
}
```

**Variables**:
```json
{
  "input": {
    "agentId": 2,
    "clientId": 10
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($input: AssignClientInput!) { assignClient(input: $input) { id agentId clientId assignedAt } }",
    "variables": {
      "input": {
        "agentId": 2,
        "clientId": 10
      }
    }
  }'
```

---

### 1.11. Désassigner un client d'un agent

```graphql
mutation UnassignClient($agentId: ID!, $clientId: ID!) {
  unassignClient(agentId: $agentId, clientId: $clientId)
}
```

**Variables**:
```json
{
  "agentId": 2,
  "clientId": 10
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($agentId: ID!, $clientId: ID!) { unassignClient(agentId: $agentId, clientId: $clientId) }",
    "variables": {"agentId": 2, "clientId": 10}
  }'
```

---

# 2. ACCOUNT SERVICE

## 📖 QUERIES (4)

### 2.1. Compte par ID

```graphql
query GetAccountById($id: ID!) {
  accountById(id: $id) {
    id
    accountNumber
    accountType
    balance
    currency
    status
    userId
    createdAt
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($id: ID!) { accountById(id: $id) { id accountNumber accountType balance currency status } }",
    "variables": {"id": 1}
  }'
```

---

### 2.2. Comptes par utilisateur

```graphql
query GetAccountsByUser($userId: ID!) {
  accountsByUserId(userId: $userId) {
    id
    accountNumber
    accountType
    balance
    currency
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "userId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: ID!) { accountsByUserId(userId: $userId) { id accountNumber accountType balance status } }",
    "variables": {"userId": 3}
  }'
```

---

### 2.3. Solde d'un compte

```graphql
query GetAccountBalance($accountId: ID!) {
  accountBalance(accountId: $accountId) {
    accountId
    balance
    currency
    availableBalance
    lastUpdated
  }
}
```

**Variables**:
```json
{
  "accountId": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($accountId: ID!) { accountBalance(accountId: $accountId) { accountId balance currency availableBalance } }",
    "variables": {"accountId": 1}
  }'
```

---

### 2.4. Transactions d'un compte

```graphql
query GetAccountTransactions($accountId: ID!, $startDate: String, $endDate: String) {
  accountTransactions(accountId: $accountId, startDate: $startDate, endDate: $endDate) {
    id
    accountId
    type
    amount
    currency
    description
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "accountId": 1,
  "startDate": "2026-01-01",
  "endDate": "2026-01-31"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($accountId: ID!, $startDate: String, $endDate: String) { accountTransactions(accountId: $accountId, startDate: $startDate, endDate: $endDate) { id type amount currency description status createdAt } }",
    "variables": {"accountId": 1, "startDate": "2026-01-01", "endDate": "2026-01-31"}
  }'
```

---

## ✏️ MUTATIONS (4)

### 2.5. Créer un compte

```graphql
mutation CreateAccount($input: CreateAccountInput!) {
  createAccount(input: $input) {
    id
    accountNumber
    accountType
    balance
    currency
    status
    userId
    createdAt
  }
}
```

**Variables**:
```json
{
  "input": {
    "userId": 3,
    "accountType": "SAVINGS",
    "currency": "EUR",
    "initialDeposit": 1000.00
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($input: CreateAccountInput!) { createAccount(input: $input) { id accountNumber accountType balance currency status } }",
    "variables": {
      "input": {
        "userId": 3,
        "accountType": "SAVINGS",
        "currency": "EUR",
        "initialDeposit": 1000.00
      }
    }
  }'
```

---

### 2.6. Mettre à jour un compte

```graphql
mutation UpdateAccount($id: ID!, $input: UpdateAccountInput!) {
  updateAccount(id: $id, input: $input) {
    id
    accountNumber
    accountType
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5,
  "input": {
    "accountType": "CHECKING",
    "status": "ACTIVE"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($id: ID!, $input: UpdateAccountInput!) { updateAccount(id: $id, input: $input) { id accountNumber status updatedAt } }",
    "variables": {
      "id": 5,
      "input": {"status": "ACTIVE"}
    }
  }'
```

---

### 2.7. Suspendre un compte

```graphql
mutation SuspendAccount($id: ID!, $input: SuspendAccountInput!) {
  suspendAccount(id: $id, input: $input) {
    id
    accountNumber
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5,
  "input": {
    "reason": "Suspicious activity detected",
    "notes": "Account under investigation"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($id: ID!, $input: SuspendAccountInput!) { suspendAccount(id: $id, input: $input) { id accountNumber status } }",
    "variables": {
      "id": 5,
      "input": {"reason": "Suspicious activity"}
    }
  }'
```

---

### 2.8. Fermer un compte

```graphql
mutation CloseAccount($id: ID!, $input: CloseAccountInput!) {
  closeAccount(id: $id, input: $input) {
    id
    accountNumber
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5,
  "input": {
    "reason": "Customer request",
    "transferAccountId": 1
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($id: ID!, $input: CloseAccountInput!) { closeAccount(id: $id, input: $input) { id accountNumber status } }",
    "variables": {
      "id": 5,
      "input": {"reason": "Customer request", "transferAccountId": 1}
    }
  }'
```

---

# 3. AUTH SERVICE

## 📖 QUERIES (2)

### 3.1. Vérifier un token

```graphql
query VerifyToken($token: String!) {
  verifyToken(token: $token) {
    valid
    userId
    username
    role
    expiresAt
  }
}
```

**Variables**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query($token: String!) { verifyToken(token: $token) { valid userId username role expiresAt } }",
    "variables": {"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
  }'
```

---

### 3.2. Informations du token

```graphql
query TokenInfo($token: String!) {
  tokenInfo(token: $token) {
    userId
    username
    email
    role
    issuedAt
    expiresAt
    tokenType
  }
}
```

**Variables**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query($token: String!) { tokenInfo(token: $token) { userId username email role issuedAt expiresAt } }",
    "variables": {"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
  }'
```

---

## ✏️ MUTATIONS (3)

### 3.3. Login

```graphql
mutation Login($input: LoginInput!) {
  login(input: $input) {
    accessToken
    refreshToken
    tokenType
    expiresIn
    user {
      id
      username
      email
      role
    }
  }
}
```

**Variables**:
```json
{
  "input": {
    "username": "john.doe",
    "password": "SecurePass123!"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation($input: LoginInput!) { login(input: $input) { accessToken refreshToken tokenType expiresIn user { id username role } } }",
    "variables": {
      "input": {
        "username": "john.doe",
        "password": "SecurePass123!"
      }
    }
  }'
```

---

### 3.4. Refresh Token

```graphql
mutation RefreshToken($input: RefreshTokenInput!) {
  refreshToken(input: $input) {
    accessToken
    refreshToken
    tokenType
    expiresIn
  }
}
```

**Variables**:
```json
{
  "input": {
    "refreshToken": "refresh_token_here..."
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -d '{
    "query": "mutation($input: RefreshTokenInput!) { refreshToken(input: $input) { accessToken refreshToken expiresIn } }",
    "variables": {
      "input": {"refreshToken": "refresh_token_here..."}
    }
  }'
```

---

### 3.5. Logout

```graphql
mutation Logout($input: RefreshTokenInput!) {
  logout(input: $input)
}
```

**Variables**:
```json
{
  "input": {
    "refreshToken": "refresh_token_here..."
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($input: RefreshTokenInput!) { logout(input: $input) }",
    "variables": {
      "input": {"refreshToken": "refresh_token_here..."}
    }
  }'
```

---

# 4. PAYMENT SERVICE

## 📖 QUERIES (3)

### 4.1. Paiement par ID

```graphql
query GetPaymentById($id: ID!) {
  paymentById(id: $id) {
    id
    sourceAccountId
    destinationAccountId
    amount
    currency
    description
    status
    type
    createdAt
    processedAt
  }
}
```

**Variables**:
```json
{
  "id": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($id: ID!) { paymentById(id: $id) { id amount currency status type createdAt } }",
    "variables": {"id": 1}
  }'
```

---

### 4.2. Paiements par utilisateur

```graphql
query GetPaymentsByUser($userId: ID!) {
  paymentsByUserId(userId: $userId) {
    id
    sourceAccountId
    destinationAccountId
    amount
    currency
    description
    status
    type
    createdAt
  }
}
```

**Variables**:
```json
{
  "userId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: ID!) { paymentsByUserId(userId: $userId) { id amount currency status type createdAt } }",
    "variables": {"userId": 3}
  }'
```

---

### 4.3. Paiements par compte

```graphql
query GetPaymentsByAccount($accountId: ID!) {
  paymentsByAccountId(accountId: $accountId) {
    id
    sourceAccountId
    destinationAccountId
    amount
    currency
    description
    status
    type
    createdAt
  }
}
```

**Variables**:
```json
{
  "accountId": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($accountId: ID!) { paymentsByAccountId(accountId: $accountId) { id amount currency status type createdAt } }",
    "variables": {"accountId": 1}
  }'
```

---

## ✏️ MUTATIONS (3)

### 4.4. Créer un paiement

```graphql
mutation CreatePayment($input: CreatePaymentInput!) {
  createPayment(input: $input) {
    id
    sourceAccountId
    destinationAccountId
    amount
    currency
    description
    status
    type
    createdAt
  }
}
```

**Variables**:
```json
{
  "input": {
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 250.00,
    "currency": "EUR",
    "description": "Payment for invoice #12345",
    "type": "TRANSFER"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($input: CreatePaymentInput!) { createPayment(input: $input) { id amount currency status type } }",
    "variables": {
      "input": {
        "sourceAccountId": 1,
        "destinationAccountId": 2,
        "amount": 250.00,
        "currency": "EUR",
        "description": "Payment for invoice",
        "type": "TRANSFER"
      }
    }
  }'
```

---

### 4.5. Annuler un paiement

```graphql
mutation CancelPayment($id: ID!) {
  cancelPayment(id: $id) {
    id
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($id: ID!) { cancelPayment(id: $id) { id status updatedAt } }",
    "variables": {"id": 5}
  }'
```

---

### 4.6. Inverser un paiement

```graphql
mutation ReversePayment($id: ID!, $reason: String!) {
  reversePayment(id: $id, reason: $reason) {
    id
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5,
  "reason": "Incorrect amount - refund requested by customer"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($id: ID!, $reason: String!) { reversePayment(id: $id, reason: $reason) { id status updatedAt } }",
    "variables": {"id": 5, "reason": "Refund requested"}
  }'
```

---

# 5. CRYPTO SERVICE

## 📖 QUERIES (4)

### 5.1. Portefeuille crypto par utilisateur

```graphql
query GetCryptoWallet($userId: ID!) {
  cryptoWalletByUserId(userId: $userId) {
    id
    userId
    walletAddress
    balance
    status
    createdAt
    updatedAt
  }
}
```

**Variables**:
```json
{
  "userId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: ID!) { cryptoWalletByUserId(userId: $userId) { id userId walletAddress balance status } }",
    "variables": {"userId": 3}
  }'
```

---

### 5.2. Transactions crypto par portefeuille

```graphql
query GetCryptoTransactions($walletId: ID!) {
  cryptoTransactionsByWalletId(walletId: $walletId) {
    id
    walletId
    type
    coinId
    coinSymbol
    amount
    price
    totalValue
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "walletId": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($walletId: ID!) { cryptoTransactionsByWalletId(walletId: $walletId) { id type coinSymbol amount price totalValue status createdAt } }",
    "variables": {"walletId": 1}
  }'
```

---

### 5.3. Liste des crypto-monnaies disponibles

```graphql
query GetCryptoCoins {
  cryptoCoins {
    id
    name
    symbol
    currentPrice
    marketCap
    volume24h
    change24h
    lastUpdated
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"query":"query { cryptoCoins { id name symbol currentPrice change24h } }"}'
```

---

### 5.4. Crypto-monnaie par ID

```graphql
query GetCryptoCoinById($id: ID!) {
  cryptoCoinById(id: $id) {
    id
    name
    symbol
    currentPrice
    marketCap
    volume24h
    change24h
    change7d
    high24h
    low24h
    lastUpdated
  }
}
```

**Variables**:
```json
{
  "id": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($id: ID!) { cryptoCoinById(id: $id) { id name symbol currentPrice change24h high24h low24h } }",
    "variables": {"id": 1}
  }'
```

---

## ✏️ MUTATIONS (5)

### 5.5. Créer un portefeuille crypto

```graphql
mutation CreateCryptoWallet($userId: ID!) {
  createCryptoWallet(userId: $userId) {
    id
    userId
    walletAddress
    balance
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "userId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($userId: ID!) { createCryptoWallet(userId: $userId) { id userId walletAddress status } }",
    "variables": {"userId": 3}
  }'
```

---

### 5.6. Activer un portefeuille crypto

```graphql
mutation ActivateCryptoWallet($walletId: ID!) {
  activateCryptoWallet(walletId: $walletId) {
    id
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "walletId": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($walletId: ID!) { activateCryptoWallet(walletId: $walletId) { id status updatedAt } }",
    "variables": {"walletId": 1}
  }'
```

---

### 5.7. Désactiver un portefeuille crypto

```graphql
mutation DeactivateCryptoWallet($walletId: ID!) {
  deactivateCryptoWallet(walletId: $walletId) {
    id
    status
    updatedAt
  }
}
```

**Variables**:
```json
{
  "walletId": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($walletId: ID!) { deactivateCryptoWallet(walletId: $walletId) { id status updatedAt } }",
    "variables": {"walletId": 1}
  }'
```

---

### 5.8. Acheter des cryptos

```graphql
mutation BuyCrypto($walletId: ID!, $input: BuyCryptoInput!) {
  buyCrypto(walletId: $walletId, input: $input) {
    id
    walletId
    type
    coinId
    coinSymbol
    amount
    price
    totalValue
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "walletId": 1,
  "input": {
    "coinId": 1,
    "amount": 0.5,
    "price": 45000.00
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($walletId: ID!, $input: BuyCryptoInput!) { buyCrypto(walletId: $walletId, input: $input) { id type coinSymbol amount price totalValue status } }",
    "variables": {
      "walletId": 1,
      "input": {"coinId": 1, "amount": 0.5, "price": 45000.00}
    }
  }'
```

---

### 5.9. Vendre des cryptos

```graphql
mutation SellCrypto($walletId: ID!, $input: SellCryptoInput!) {
  sellCrypto(walletId: $walletId, input: $input) {
    id
    walletId
    type
    coinId
    coinSymbol
    amount
    price
    totalValue
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "walletId": 1,
  "input": {
    "coinId": 1,
    "amount": 0.25,
    "price": 46000.00
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($walletId: ID!, $input: SellCryptoInput!) { sellCrypto(walletId: $walletId, input: $input) { id type coinSymbol amount price totalValue status } }",
    "variables": {
      "walletId": 1,
      "input": {"coinId": 1, "amount": 0.25, "price": 46000.00}
    }
  }'
```

---

# 6. NOTIFICATION SERVICE

## 📖 QUERIES (2)

### 6.1. Notifications par utilisateur

```graphql
query GetNotificationsByUser($userId: ID!) {
  notificationsByUserId(userId: $userId) {
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

**Variables**:
```json
{
  "userId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: ID!) { notificationsByUserId(userId: $userId) { id type subject message status read createdAt } }",
    "variables": {"userId": 3}
  }'
```

---

### 6.2. Notifications in-app par utilisateur

```graphql
query GetInAppNotifications($userId: ID!) {
  inAppNotificationsByUserId(userId: $userId) {
    id
    userId
    title
    message
    type
    priority
    status
    read
    createdAt
    readAt
  }
}
```

**Variables**:
```json
{
  "userId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: ID!) { inAppNotificationsByUserId(userId: $userId) { id title message type priority read createdAt } }",
    "variables": {"userId": 3}
  }'
```

---

## ✏️ MUTATIONS (2)

### 6.3. Envoyer une notification

```graphql
mutation SendNotification($input: SendNotificationInput!) {
  sendNotification(input: $input) {
    id
    userId
    type
    subject
    message
    category
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "input": {
    "userId": 3,
    "type": "EMAIL",
    "subject": "Account Update",
    "message": "Your account information has been updated successfully.",
    "category": "ACCOUNT"
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "mutation($input: SendNotificationInput!) { sendNotification(input: $input) { id userId type subject status } }",
    "variables": {
      "input": {
        "userId": 3,
        "type": "EMAIL",
        "subject": "Account Update",
        "message": "Your account has been updated.",
        "category": "ACCOUNT"
      }
    }
  }'
```

---

### 6.4. Marquer une notification comme lue

```graphql
mutation MarkNotificationAsRead($id: ID!) {
  markNotificationAsRead(id: $id) {
    id
    read
    updatedAt
  }
}
```

**Variables**:
```json
{
  "id": 5
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($id: ID!) { markNotificationAsRead(id: $id) { id read updatedAt } }",
    "variables": {"id": 5}
  }'
```

---

# 7. AUDIT SERVICE

## 📖 QUERIES (4)

### 7.1. Tous les événements d'audit

```graphql
query GetAllAuditEvents {
  auditEvents {
    id
    userId
    eventType
    action
    resource
    description
    ipAddress
    userAgent
    status
    createdAt
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"query":"query { auditEvents { id userId eventType action resource status createdAt } }"}'
```

---

### 7.2. Événement d'audit par ID

```graphql
query GetAuditEventById($id: ID!) {
  auditEventById(id: $id) {
    id
    userId
    eventType
    action
    resource
    resourceId
    description
    ipAddress
    userAgent
    status
    metadata
    createdAt
  }
}
```

**Variables**:
```json
{
  "id": 1
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "query($id: ID!) { auditEventById(id: $id) { id userId eventType action resource description status createdAt } }",
    "variables": {"id": 1}
  }'
```

---

### 7.3. Événements d'audit par utilisateur

```graphql
query GetAuditEventsByUser($userId: ID!) {
  auditEventsByUserId(userId: $userId) {
    id
    userId
    eventType
    action
    resource
    description
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "userId": 3
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "query($userId: ID!) { auditEventsByUserId(userId: $userId) { id eventType action resource status createdAt } }",
    "variables": {"userId": 3}
  }'
```

---

### 7.4. Événements d'audit par type

```graphql
query GetAuditEventsByType($eventType: String!) {
  auditEventsByType(eventType: $eventType) {
    id
    userId
    eventType
    action
    resource
    description
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "eventType": "LOGIN"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{
    "query": "query($eventType: String!) { auditEventsByType(eventType: $eventType) { id userId action resource status createdAt } }",
    "variables": {"eventType": "LOGIN"}
  }'
```

---

# 8. ANALYTICS SERVICE

## 📖 QUERIES (6)

### 8.1. Alertes actives

```graphql
query GetActiveAlerts($userId: String!) {
  activeAlerts(userId: $userId) {
    id
    userId
    type
    severity
    message
    threshold
    currentValue
    status
    createdAt
  }
}
```

**Variables**:
```json
{
  "userId": "3"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: String!) { activeAlerts(userId: $userId) { id type severity message status createdAt } }",
    "variables": {"userId": "3"}
  }'
```

---

### 8.2. Résumé du tableau de bord

```graphql
query GetDashboardSummary($userId: String!) {
  dashboardSummary(userId: $userId) {
    userId
    totalBalance
    totalIncome
    totalExpenses
    savingsRate
    accountCount
    transactionCount
    lastUpdated
  }
}
```

**Variables**:
```json
{
  "userId": "3"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: String!) { dashboardSummary(userId: $userId) { totalBalance totalIncome totalExpenses savingsRate accountCount } }",
    "variables": {"userId": "3"}
  }'
```

---

### 8.3. Répartition des dépenses

```graphql
query GetSpendingBreakdown($userId: String!, $period: String) {
  spendingBreakdown(userId: $userId, period: $period) {
    category
    amount
    percentage
    transactionCount
    trend
  }
}
```

**Variables**:
```json
{
  "userId": "3",
  "period": "MONTH"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: String!, $period: String) { spendingBreakdown(userId: $userId, period: $period) { category amount percentage transactionCount trend } }",
    "variables": {"userId": "3", "period": "MONTH"}
  }'
```

---

### 8.4. Tendance du solde

```graphql
query GetBalanceTrend($userId: String!, $days: Int) {
  balanceTrend(userId: $userId, days: $days) {
    userId
    period
    dataPoints {
      date
      balance
    }
    averageBalance
    trend
  }
}
```

**Variables**:
```json
{
  "userId": "3",
  "days": 30
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: String!, $days: Int) { balanceTrend(userId: $userId, days: $days) { userId period averageBalance trend dataPoints { date balance } } }",
    "variables": {"userId": "3", "days": 30}
  }'
```

---

### 8.5. Recommandations

```graphql
query GetRecommendations($userId: String!) {
  recommendations(userId: $userId)
}
```

**Variables**:
```json
{
  "userId": "3"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "query($userId: String!) { recommendations(userId: $userId) }",
    "variables": {"userId": "3"}
  }'
```

---

### 8.6. Vue d'ensemble admin

```graphql
query GetAdminOverview {
  adminOverview {
    totalUsers
    activeUsers
    totalAccounts
    totalTransactions
    totalVolume
    avgTransactionValue
    topCategories
    systemHealth
  }
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin_token>" \
  -d '{"query":"query { adminOverview { totalUsers activeUsers totalAccounts totalTransactions totalVolume systemHealth } }"}'
```

---

## ✏️ MUTATIONS (1)

### 8.7. Résoudre une alerte

```graphql
mutation ResolveAlert($alertId: String!) {
  resolveAlert(alertId: $alertId)
}
```

**Variables**:
```json
{
  "alertId": "alert-123"
}
```

**cURL**:
```bash
curl -X POST http://localhost:8090/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "query": "mutation($alertId: String!) { resolveAlert(alertId: $alertId) }",
    "variables": {"alertId": "alert-123"}
  }'
```

---

## 📊 RÉCAPITULATIF

### Statistiques Totales
| Service | Queries | Mutations | Total |
|---------|---------|-----------|-------|
| User Service | 5 | 6 | 11 |
| Account Service | 4 | 4 | 8 |
| Auth Service | 2 | 3 | 5 |
| Payment Service | 3 | 3 | 6 |
| Crypto Service | 4 | 5 | 9 |
| Notification Service | 2 | 2 | 4 |
| Audit Service | 4 | 0 | 4 |
| Analytics Service | 6 | 1 | 7 |
| **TOTAL** | **30** | **24** | **54** |

---

## 🔧 Script PowerShell de Test Automatique

Créez un fichier `test-all-graphql.ps1`:

```powershell
$endpoint = "http://localhost:8090/graphql"
$token = "votre_token_jwt_ici"

$headers = @{
    "Content-Type" = "application/json"
    "Authorization" = "Bearer $token"
}

# Test 1: User Service
Write-Host "Testing User Service..." -ForegroundColor Cyan
$body = '{"query":"query { users { id username email role } }"}'
Invoke-RestMethod -Uri $endpoint -Method Post -Headers $headers -Body $body

# Test 2: Account Service
Write-Host "Testing Account Service..." -ForegroundColor Cyan
$body = '{"query":"query { accountById(id: 1) { id accountNumber balance } }"}'
Invoke-RestMethod -Uri $endpoint -Method Post -Headers $headers -Body $body

# Test 3: Payment Service
Write-Host "Testing Payment Service..." -ForegroundColor Cyan
$body = '{"query":"query { paymentsByUserId(userId: 3) { id amount status } }"}'
Invoke-RestMethod -Uri $endpoint -Method Post -Headers $headers -Body $body

# Test 4: Crypto Service
Write-Host "Testing Crypto Service..." -ForegroundColor Cyan
$body = '{"query":"query { cryptoCoins { id name symbol currentPrice } }"}'
Invoke-RestMethod -Uri $endpoint -Method Post -Headers $headers -Body $body

# Test 5: Notification Service
Write-Host "Testing Notification Service..." -ForegroundColor Cyan
$body = '{"query":"query { notificationsByUserId(userId: 3) { id subject read } }"}'
Invoke-RestMethod -Uri $endpoint -Method Post -Headers $headers -Body $body

Write-Host "All tests completed!" -ForegroundColor Green
```

**Exécution**:
```bash
powershell -ExecutionPolicy Bypass -File test-all-graphql.ps1
```

---

## 📝 Notes Importantes

1. **Token d'authentification**: Remplacez `<token>` par un JWT valide obtenu via la mutation `login`
2. **IDs de test**: Adaptez les IDs (userId, accountId, etc.) selon vos données
3. **Permissions**: Certaines opérations nécessitent un rôle ADMIN
4. **Microservices**: Assurez-vous que tous les microservices sont démarrés
5. **Gateway**: Le Gateway doit être actif sur le port 8090

---

**Date de création**: 5 Janvier 2026  
**Version**: 1.0.0  
**Auteur**: GitHub Copilot  
**Statut**: ✅ Prêt pour les tests
