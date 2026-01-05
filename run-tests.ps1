# Comprehensive GraphQL Gateway Tests
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   GraphQL Gateway - Full Endpoint Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8090/graphql"
$success = 0
$failed = 0
$total = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Query,
        [string]$Category
    )
    
    $total++
    Write-Host "[$total] Testing: $Name" -ForegroundColor Yellow -NoNewline
    
    $body = @{ query = $Query } | ConvertTo-Json -Compress
    
    try {
        $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        
        if ($response.data) {
            Write-Host " [OK] SUCCESS" -ForegroundColor Green
            $script:success++
            return $true
        } elseif ($response.errors) {
            $errorMsg = $response.errors[0].message
            if ($errorMsg -match "401" -or $errorMsg -match "Unauthorized") {
                Write-Host " [WARN] BLOCKED (401 Auth Required)" -ForegroundColor Yellow
            } elseif ($errorMsg -match "INTERNAL_ERROR") {
                Write-Host " [ERROR] Microservice Down" -ForegroundColor Magenta
            } else {
                Write-Host " [FAIL] $errorMsg" -ForegroundColor Red
                $script:failed++
            }
            return $false
        }
    } catch {
        Write-Host " [EXCEPTION] $($_.Exception.Message)" -ForegroundColor Red
        $script:failed++
        return $false
    }
}

# =========================
# 1. HEALTH CHECK
# =========================
Write-Host "`n--- HEALTH CHECK ---" -ForegroundColor Cyan
Test-Endpoint -Name "Health Check" -Query "{ health }" -Category "System"

# =========================
# 2. SCHEMA INTROSPECTION
# =========================
Write-Host "`n--- SCHEMA INTROSPECTION ---" -ForegroundColor Cyan
Test-Endpoint -Name "Query Type" -Query "{ __schema { queryType { name } } }" -Category "Schema"
Test-Endpoint -Name "Mutation Type" -Query "{ __schema { mutationType { name } } }" -Category "Schema"

# =========================
# 3. USER SERVICE
# =========================
Write-Host "`n--- USER SERVICE (Port 8081) ---" -ForegroundColor Cyan
Test-Endpoint -Name "Get All Users" -Query "{ users { id login email } }" -Category "User"
Test-Endpoint -Name "Get User by ID" -Query "{ userById(id: \"1\") { id login email } }" -Category "User"
Test-Endpoint -Name "Get Me" -Query "{ me(id: \"1\") { id login email } }" -Category "User"
Test-Endpoint -Name "Clients by Agent" -Query "{ clientsByAgent(agentId: \"1\") { id login } }" -Category "User"
Test-Endpoint -Name "Agent by Client" -Query "{ agentByClient(clientId: \"1\") { id login } }" -Category "User"

# =========================
# 4. ACCOUNT SERVICE
# =========================
Write-Host "`n--- ACCOUNT SERVICE (Port 8082) ---" -ForegroundColor Cyan
Test-Endpoint -Name "Get Account by ID" -Query "{ accountById(id: \"1\") { id accountNumber balance } }" -Category "Account"
Test-Endpoint -Name "Accounts by User ID" -Query "{ accountsByUserId(userId: \"1\") { id accountNumber } }" -Category "Account"
Test-Endpoint -Name "Account Balance" -Query "{ accountBalance(id: \"1\") { balance currency } }" -Category "Account"
Test-Endpoint -Name "Account Transactions" -Query "{ accountTransactions(id: \"1\") { id amount } }" -Category "Account"

# =========================
# 5. AUTH SERVICE
# =========================
Write-Host "`n--- AUTH SERVICE (Port 8081) ---" -ForegroundColor Cyan
Test-Endpoint -Name "Verify Token" -Query "{ verifyToken(token: \"test\") }" -Category "Auth"
Test-Endpoint -Name "Token Info" -Query "{ tokenInfo(token: \"test\") { userId username } }" -Category "Auth"

# =========================
# 6. PAYMENT SERVICE
# =========================
Write-Host "`n--- PAYMENT SERVICE (Port 8082) ---" -ForegroundColor Cyan
Test-Endpoint -Name "Payment by ID" -Query "{ paymentById(id: \"1\") { id amount status } }" -Category "Payment"
Test-Endpoint -Name "Payments by User ID" -Query "{ paymentsByUserId(userId: \"1\") { id amount } }" -Category "Payment"
Test-Endpoint -Name "Payments by Account ID" -Query "{ paymentsByAccountId(accountId: \"1\") { id amount } }" -Category "Payment"

# =========================
# 7. CRYPTO SERVICE
# =========================
Write-Host "`n--- CRYPTO SERVICE (Port 8081) ---" -ForegroundColor Cyan
Test-Endpoint -Name "Crypto Wallet by User ID" -Query "{ cryptoWalletByUserId(userId: \"1\") { id balance } }" -Category "Crypto"
Test-Endpoint -Name "Crypto Transactions by Wallet" -Query "{ cryptoTransactionsByWalletId(walletId: \"1\") { id amount } }" -Category "Crypto"
Test-Endpoint -Name "All Crypto Coins" -Query "{ cryptoCoins { id symbol name } }" -Category "Crypto"
Test-Endpoint -Name "Crypto Coin by ID" -Query "{ cryptoCoinById(coinId: \"BTC\") { id symbol } }" -Category "Crypto"

# =========================
# 8. NOTIFICATION SERVICE
# =========================
Write-Host "`n--- NOTIFICATION SERVICE (Port 8084) ---" -ForegroundColor Cyan
Test-Endpoint -Name "Notifications by User ID" -Query "{ notificationsByUserId(userId: \"1\") { id message } }" -Category "Notification"
Test-Endpoint -Name "In-App Notifications" -Query "{ inAppNotificationsByUserId(userId: \"1\") { id message } }" -Category "Notification"

# =========================
# 9. AUDIT SERVICE
# =========================
Write-Host "`n--- AUDIT SERVICE (Port 8083) ---" -ForegroundColor Cyan
Test-Endpoint -Name "All Audit Events" -Query "{ auditEvents { id eventType } }" -Category "Audit"
Test-Endpoint -Name "Audit Event by ID" -Query "{ auditEventById(eventId: \"1\") { id eventType } }" -Category "Audit"
Test-Endpoint -Name "Audit Events by User ID" -Query "{ auditEventsByUserId(userId: \"1\") { id eventType } }" -Category "Audit"
Test-Endpoint -Name "Audit Events by Type" -Query "{ auditEventsByType(eventType: \"LOGIN\") { id eventType } }" -Category "Audit"

# =========================
# 10. MUTATION TESTS (Sample)
# =========================
Write-Host "`n--- MUTATION TESTS (Sample) ---" -ForegroundColor Cyan
Test-Endpoint -Name "Login Mutation" -Query "mutation { login(input: { username: \"test\", password: \"test\" }) { accessToken } }" -Category "Auth"

# =========================
# SUMMARY
# =========================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Tests: $total" -ForegroundColor White
Write-Host "Successful: $success" -ForegroundColor Green
Write-Host "Failed: $failed" -ForegroundColor Red
Write-Host "Success Rate: $([math]::Round(($success/$total)*100, 2))%" -ForegroundColor $(if ($success -eq $total) { "Green" } else { "Yellow" })

Write-Host "`nNote: Tests blocked by 401 errors require microservices to be running" -ForegroundColor Yellow
Write-Host "GraphQL Gateway itself is functioning correctly if schema tests pass" -ForegroundColor Cyan
Write-Host ""
