# Script de test complet pour tous les endpoints GraphQL
# Teste 38 Queries + 21 Mutations = 59 opérations totales

Write-Host "=== TEST COMPLET DU GRAPHQL GATEWAY ===" -ForegroundColor Cyan
Write-Host "Date: $(Get-Date)" -ForegroundColor Gray
Write-Host ""

$baseUrl = "http://localhost:8090/graphql"
$totalTests = 0
$passedTests = 0
$failedTests = 0

function Test-GraphQL {
    param(
        [string]$Name,
        [string]$Query,
        [string]$ExpectedField
    )
    
    $script:totalTests++
    Write-Host "Test $totalTests`: $Name" -NoNewline
    
    try {
        $body = @{
            query = $Query
        } | ConvertTo-Json -Compress
        
        $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        
        if ($response.errors) {
            Write-Host " - ÉCHOUÉ" -ForegroundColor Red
            Write-Host "  Erreur: $($response.errors[0].message)" -ForegroundColor Yellow
            $script:failedTests++
            return $false
        }
        elseif ($response.data -and $null -ne $response.data.$ExpectedField) {
            Write-Host " - RÉUSSI ✓" -ForegroundColor Green
            $script:passedTests++
            return $true
        }
        else {
            Write-Host " - RÉUSSI (sans données) ✓" -ForegroundColor Yellow
            $script:passedTests++
            return $true
        }
    }
    catch {
        Write-Host " - ERREUR" -ForegroundColor Red
        Write-Host "  Exception: $($_.Exception.Message)" -ForegroundColor Yellow
        $script:failedTests++
        return $false
    }
}

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "PARTIE 1: QUERIES (38 opérations)" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# ==================== SECTION 1: USER SERVICE QUERIES ====================
Write-Host "--- SECTION 1: User Service (5 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Get All Users" -Query "{ users { id login email fname lname role } }" -ExpectedField "users"
Test-GraphQL -Name "Get User By ID" -Query "{ userById(id: ""1"") { id login email fname lname } }" -ExpectedField "userById"
Test-GraphQL -Name "Get User Profile (Me)" -Query "{ me(id: ""1"") { id login email phone } }" -ExpectedField "me"
Test-GraphQL -Name "Get Clients By Agent" -Query "{ clientsByAgent(agentId: ""2"") { id login email } }" -ExpectedField "clientsByAgent"
Test-GraphQL -Name "Get Agent By Client" -Query "{ agentByClient(clientId: ""5"") { id login email } }" -ExpectedField "agentByClient"

# ==================== SECTION 2: ACCOUNT SERVICE QUERIES ====================
Write-Host ""
Write-Host "--- SECTION 2: Account Service (4 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Get Account By ID" -Query "{ accountById(id: ""1"") { id accountNumber balance currency status } }" -ExpectedField "accountById"
Test-GraphQL -Name "Get Accounts By User ID" -Query "{ accountsByUserId(userId: ""1"") { id accountNumber balance } }" -ExpectedField "accountsByUserId"
Test-GraphQL -Name "Get Account Balance" -Query "{ accountBalance(id: ""1"") { accountId balance currency timestamp } }" -ExpectedField "accountBalance"
Test-GraphQL -Name "Get Account Transactions" -Query "{ accountTransactions(id: ""1"") { id amount transactionType timestamp } }" -ExpectedField "accountTransactions"

# ==================== SECTION 3: AUTH SERVICE QUERIES ====================
Write-Host ""
Write-Host "--- SECTION 3: Auth Service (2 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Verify Token" -Query "{ verifyToken(token: ""test-token"") }" -ExpectedField "verifyToken"
Test-GraphQL -Name "Get Token Info" -Query "{ tokenInfo(token: ""test-token"") { sub username email roles } }" -ExpectedField "tokenInfo"

# ==================== SECTION 4: PAYMENT SERVICE QUERIES ====================
Write-Host ""
Write-Host "--- SECTION 4: Payment Service (3 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Get Payment By ID" -Query "{ paymentById(id: ""1"") { id amount currency status } }" -ExpectedField "paymentById"
Test-GraphQL -Name "Get Payments By User ID" -Query "{ paymentsByUserId(userId: ""1"") { id amount paymentType } }" -ExpectedField "paymentsByUserId"
Test-GraphQL -Name "Get Payments By Account ID" -Query "{ paymentsByAccountId(accountId: ""1"") { id amount status } }" -ExpectedField "paymentsByAccountId"

# ==================== SECTION 5: CRYPTO SERVICE QUERIES ====================
Write-Host ""
Write-Host "--- SECTION 5: Crypto Service (4 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Get Crypto Wallet By User ID" -Query "{ cryptoWalletByUserId(userId: ""1"") { id balance currency status } }" -ExpectedField "cryptoWalletByUserId"
Test-GraphQL -Name "Get Crypto Transactions" -Query "{ cryptoTransactionsByWalletId(walletId: ""1"") { id symbol transactionType cryptoAmount } }" -ExpectedField "cryptoTransactionsByWalletId"
Test-GraphQL -Name "Get All Crypto Coins" -Query "{ cryptoCoins { coinId symbol name currentPrice } }" -ExpectedField "cryptoCoins"
Test-GraphQL -Name "Get Crypto Coin By ID" -Query "{ cryptoCoinById(coinId: ""bitcoin"") { coinId symbol name currentPrice } }" -ExpectedField "cryptoCoinById"

# ==================== SECTION 6: NOTIFICATION SERVICE QUERIES ====================
Write-Host ""
Write-Host "--- SECTION 6: Notification Service (2 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Get Notifications By User ID" -Query "{ notificationsByUserId(userId: ""1"") { id subject message status } }" -ExpectedField "notificationsByUserId"
Test-GraphQL -Name "Get In-App Notifications" -Query "{ inAppNotificationsByUserId(userId: ""1"") { id subject message read } }" -ExpectedField "inAppNotificationsByUserId"

# ==================== SECTION 7: AUDIT SERVICE QUERIES ====================
Write-Host ""
Write-Host "--- SECTION 7: Audit Service (4 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Get All Audit Events" -Query "{ auditEvents { eventId eventType timestamp result } }" -ExpectedField "auditEvents"
Test-GraphQL -Name "Get Audit Event By ID" -Query "{ auditEventById(eventId: ""test-id"") { eventId eventType action } }" -ExpectedField "auditEventById"
Test-GraphQL -Name "Get Audit Events By User ID" -Query "{ auditEventsByUserId(userId: ""1"") { eventId eventType userId } }" -ExpectedField "auditEventsByUserId"
Test-GraphQL -Name "Get Audit Events By Type" -Query "{ auditEventsByType(eventType: ""LOGIN"") { eventId eventType timestamp } }" -ExpectedField "auditEventsByType"

# ==================== SECTION 8: ANALYTICS SERVICE QUERIES ====================
Write-Host ""
Write-Host "--- SECTION 8: Analytics Service (6 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Get Active Alerts" -Query "{ activeAlerts(userId: ""1"") { alertId alertType severity title } }" -ExpectedField "activeAlerts"
Test-GraphQL -Name "Get Dashboard Summary" -Query "{ dashboardSummary(userId: ""1"") { userId currentBalance monthlySpending transactionsThisMonth } }" -ExpectedField "dashboardSummary"
Test-GraphQL -Name "Get Spending Breakdown" -Query "{ spendingBreakdown(userId: ""1"", period: ""MONTH"") { category amount count } }" -ExpectedField "spendingBreakdown"
Test-GraphQL -Name "Get Balance Trend" -Query "{ balanceTrend(userId: ""1"", days: 30) { period dataPoints { timestamp value } } }" -ExpectedField "balanceTrend"
Test-GraphQL -Name "Get Recommendations" -Query "{ recommendations(userId: ""1"") }" -ExpectedField "recommendations"
Test-GraphQL -Name "Get Admin Overview" -Query "{ adminOverview { activeUsers totalTransactions revenue } }" -ExpectedField "adminOverview"

# ==================== SECTION 9: SCHEMA INTROSPECTION ====================
Write-Host ""
Write-Host "--- SECTION 9: Schema Introspection (8 queries) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Schema Query Type" -Query "{ __schema { queryType { name fields { name } } } }" -ExpectedField "__schema"
Test-GraphQL -Name "Schema Mutation Type" -Query "{ __schema { mutationType { name fields { name } } } }" -ExpectedField "__schema"
Test-GraphQL -Name "All Types" -Query "{ __schema { types { name kind } } }" -ExpectedField "__schema"
Test-GraphQL -Name "User Type" -Query "{ __type(name: ""User"") { name fields { name type { name } } } }" -ExpectedField "__type"
Test-GraphQL -Name "Account Type" -Query "{ __type(name: ""Account"") { name fields { name type { name } } } }" -ExpectedField "__type"
Test-GraphQL -Name "Payment Type" -Query "{ __type(name: ""Payment"") { name fields { name type { name } } } }" -ExpectedField "__type"
Test-GraphQL -Name "CryptoWallet Type" -Query "{ __type(name: ""CryptoWallet"") { name fields { name type { name } } } }" -ExpectedField "__type"
Test-GraphQL -Name "Alert Type" -Query "{ __type(name: ""Alert"") { name fields { name type { name } } } }" -ExpectedField "__type"

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "PARTIE 2: MUTATIONS (21 opérations)" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""

# ==================== USER SERVICE MUTATIONS ====================
Write-Host "--- SECTION 10: User Service Mutations (6 mutations) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Create User" -Query "mutation { createUser(input: { login: ""testuser"", email: ""test@example.com"", password: ""Test123!"", fname: ""Test"", lname: ""User"", role: ""CLIENT"" }) { id login } }" -ExpectedField "createUser"
Test-GraphQL -Name "Activate User" -Query "mutation { activateUser(id: ""1"") { id isActive } }" -ExpectedField "activateUser"
Test-GraphQL -Name "Deactivate User" -Query "mutation { deactivateUser(id: ""2"") { id isActive } }" -ExpectedField "deactivateUser"
Test-GraphQL -Name "Update Profile" -Query "mutation { updateProfile(id: ""1"", input: { phone: ""0612345678"" }) { id phone } }" -ExpectedField "updateProfile"
Test-GraphQL -Name "Assign Client" -Query "mutation { assignClient(input: { agentId: ""2"", clientId: ""5"", notes: ""Test assignment"" }) { id agentId clientId } }" -ExpectedField "assignClient"
Test-GraphQL -Name "Unassign Client" -Query "mutation { unassignClient(agentId: ""2"", clientId: ""5"") }" -ExpectedField "unassignClient"

# ==================== ACCOUNT SERVICE MUTATIONS ====================
Write-Host ""
Write-Host "--- SECTION 11: Account Service Mutations (4 mutations) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Create Account" -Query "mutation { createAccount(input: { userId: ""1"", accountType: ""SAVINGS"", currency: ""EUR"", initialBalance: 1000.0 }) { id accountNumber } }" -ExpectedField "createAccount"
Test-GraphQL -Name "Update Account" -Query "mutation { updateAccount(id: ""1"", input: { accountType: ""CURRENT"" }) { id accountType } }" -ExpectedField "updateAccount"
Test-GraphQL -Name "Suspend Account" -Query "mutation { suspendAccount(id: ""1"", input: { reason: ""Suspicious activity"", suspendedBy: ""admin"" }) { id status } }" -ExpectedField "suspendAccount"
Test-GraphQL -Name "Close Account" -Query "mutation { closeAccount(id: ""1"", input: { closureReason: ""Customer request"", closedBy: ""admin"" }) { id status } }" -ExpectedField "closeAccount"

# ==================== AUTH SERVICE MUTATIONS ====================
Write-Host ""
Write-Host "--- SECTION 12: Auth Service Mutations (3 mutations) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Login" -Query "mutation { login(input: { username: ""admin"", password: ""admin123"" }) { access_token token_type } }" -ExpectedField "login"
Test-GraphQL -Name "Refresh Token" -Query "mutation { refreshToken(input: { refresh_token: ""test-refresh-token"" }) { access_token } }" -ExpectedField "refreshToken"
Test-GraphQL -Name "Logout" -Query "mutation { logout(input: { refresh_token: ""test-refresh-token"" }) }" -ExpectedField "logout"

# ==================== PAYMENT SERVICE MUTATIONS ====================
Write-Host ""
Write-Host "--- SECTION 13: Payment Service Mutations (3 mutations) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Create Payment" -Query "mutation { createPayment(input: { fromAccountId: ""1"", toAccountId: ""2"", amount: 100.0, currency: ""EUR"", paymentType: ""TRANSFER"", reference: ""Test payment"" }) { id amount status } }" -ExpectedField "createPayment"
Test-GraphQL -Name "Cancel Payment" -Query "mutation { cancelPayment(id: ""1"") { id status } }" -ExpectedField "cancelPayment"
Test-GraphQL -Name "Reverse Payment" -Query "mutation { reversePayment(id: ""1"", reason: ""Error correction"") { id status reversalReason } }" -ExpectedField "reversePayment"

# ==================== CRYPTO SERVICE MUTATIONS ====================
Write-Host ""
Write-Host "--- SECTION 14: Crypto Service Mutations (5 mutations) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Create Crypto Wallet" -Query "mutation { createCryptoWallet(userId: ""1"") { id userId status } }" -ExpectedField "createCryptoWallet"
Test-GraphQL -Name "Activate Crypto Wallet" -Query "mutation { activateCryptoWallet(walletId: ""1"") { id status } }" -ExpectedField "activateCryptoWallet"
Test-GraphQL -Name "Deactivate Crypto Wallet" -Query "mutation { deactivateCryptoWallet(walletId: ""1"") { id status } }" -ExpectedField "deactivateCryptoWallet"
Test-GraphQL -Name "Buy Crypto" -Query "mutation { buyCrypto(walletId: ""1"", input: { symbol: ""BTC"", eurAmount: 100.0 }) { id symbol cryptoAmount } }" -ExpectedField "buyCrypto"
Test-GraphQL -Name "Sell Crypto" -Query "mutation { sellCrypto(walletId: ""1"", input: { symbol: ""BTC"", cryptoAmount: 0.001 }) { id symbol eurAmount } }" -ExpectedField "sellCrypto"

# ==================== NOTIFICATION SERVICE MUTATIONS ====================
Write-Host ""
Write-Host "--- SECTION 15: Notification Service Mutations (2 mutations) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Send Notification" -Query "mutation { sendNotification(input: { userId: ""1"", type: ""EMAIL"", subject: ""Test"", message: ""Test message"", category: ""GENERAL"" }) { id subject } }" -ExpectedField "sendNotification"
Test-GraphQL -Name "Mark Notification As Read" -Query "mutation { markNotificationAsRead(id: ""1"") { id read } }" -ExpectedField "markNotificationAsRead"

# ==================== ANALYTICS SERVICE MUTATIONS ====================
Write-Host ""
Write-Host "--- SECTION 16: Analytics Service Mutations (1 mutation) ---" -ForegroundColor Yellow
Test-GraphQL -Name "Resolve Alert" -Query "mutation { resolveAlert(alertId: ""test-alert-id"") }" -ExpectedField "resolveAlert"

Write-Host ""
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Total de tests exécutés: $totalTests" -ForegroundColor White
Write-Host "Tests réussis: $passedTests" -ForegroundColor Green
Write-Host "Tests échoués: $failedTests" -ForegroundColor Red

if ($failedTests -eq 0) {
    Write-Host ""
    Write-Host "TOUS LES TESTS ONT REUSSI!" -ForegroundColor Green
    exit 0
} else {
    $successRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
    Write-Host ""
    Write-Host "Taux de réussite: $successRate%" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "NOTE: Les echecs peuvent etre dus a:" -ForegroundColor Yellow
    Write-Host "  1. Authentification requise (erreurs 401)" -ForegroundColor Yellow
    Write-Host "  2. Microservices non demarres" -ForegroundColor Yellow
    Write-Host "  3. Donnees de test inexistantes" -ForegroundColor Yellow
    Write-Host "  4. Voir AUTHENTICATION_FIX_GUIDE.md pour les solutions" -ForegroundColor Yellow
    exit 1
}
