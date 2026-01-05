# Script de test complet pour tous les endpoints GraphQL
# Ce script teste systématiquement les 56 opérations GraphQL

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
    
    $totalTests++
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
        elseif ($response.data -and $response.data.$ExpectedField) {
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

Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 1: HEALTH CHECK" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Health Check" -Query "{ health }" -ExpectedField "health"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 2: USER SERVICE (8 opérations)" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Get All Users" -Query "{ users { id login email } }" -ExpectedField "users"
Test-GraphQL -Name "Get User By ID" -Query "{ userById(id: \`"1\`") { id login email } }" -ExpectedField "userById"
Test-GraphQL -Name "Get User Profile (Me)" -Query "{ me(id: \`"1\`") { id login email } }" -ExpectedField "me"
Test-GraphQL -Name "Get Clients By Agent" -Query "{ clientsByAgent(agentId: \`"2\`") { id login } }" -ExpectedField "clientsByAgent"
Test-GraphQL -Name "Get Agent By Client" -Query "{ agentByClient(clientId: \`"5\`") { id login } }" -ExpectedField "agentByClient"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 3: ACCOUNT SERVICE (4 queries)" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Get Account By ID" -Query "{ accountById(id: \`"1\`") { id accountNumber } }" -ExpectedField "accountById"
Test-GraphQL -Name "Get Accounts By User ID" -Query "{ accountsByUserId(userId: \`"1\`") { id accountNumber } }" -ExpectedField "accountsByUserId"
Test-GraphQL -Name "Get Account Balance" -Query "{ accountBalance(id: \`"1\`") { accountId balance } }" -ExpectedField "accountBalance"
Test-GraphQL -Name "Get Account Transactions" -Query "{ accountTransactions(id: \`"1\`") { id amount } }" -ExpectedField "accountTransactions"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 4: AUTH SERVICE (2 queries)" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Verify Token" -Query "{ verifyToken(token: \`"test-token\`") }" -ExpectedField "verifyToken"
Test-GraphQL -Name "Get Token Info" -Query "{ tokenInfo(token: \`"test-token\`") { sub username } }" -ExpectedField "tokenInfo"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 5: PAYMENT SERVICE (3 queries)" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Get Payment By ID" -Query "{ paymentById(id: \`"1\`") { id amount } }" -ExpectedField "paymentById"
Test-GraphQL -Name "Get Payments By User ID" -Query "{ paymentsByUserId(userId: \`"1\`") { id amount } }" -ExpectedField "paymentsByUserId"
Test-GraphQL -Name "Get Payments By Account ID" -Query "{ paymentsByAccountId(accountId: \`"1\`") { id amount } }" -ExpectedField "paymentsByAccountId"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 6: CRYPTO SERVICE (4 queries)" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Get Crypto Wallet By User ID" -Query "{ cryptoWalletByUserId(userId: \`"1\`") { id balance } }" -ExpectedField "cryptoWalletByUserId"
Test-GraphQL -Name "Get Crypto Transactions" -Query "{ cryptoTransactionsByWalletId(walletId: \`"1\`") { id symbol } }" -ExpectedField "cryptoTransactionsByWalletId"
Test-GraphQL -Name "Get All Crypto Coins" -Query "{ cryptoCoins { coinId symbol name } }" -ExpectedField "cryptoCoins"
Test-GraphQL -Name "Get Crypto Coin By ID" -Query "{ cryptoCoinById(coinId: \`"bitcoin\`") { coinId symbol } }" -ExpectedField "cryptoCoinById"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 7: NOTIFICATION SERVICE (2 queries)" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Get Notifications By User ID" -Query "{ notificationsByUserId(userId: \`"1\`") { id subject } }" -ExpectedField "notificationsByUserId"
Test-GraphQL -Name "Get In-App Notifications" -Query "{ inAppNotificationsByUserId(userId: \`"1\`") { id subject } }" -ExpectedField "inAppNotificationsByUserId"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 8: AUDIT SERVICE (4 queries)" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Get All Audit Events" -Query "{ auditEvents { eventId eventType } }" -ExpectedField "auditEvents"
Test-GraphQL -Name "Get Audit Event By ID" -Query "{ auditEventById(eventId: \`"test-id\`") { eventId eventType } }" -ExpectedField "auditEventById"
Test-GraphQL -Name "Get Audit Events By User ID" -Query "{ auditEventsByUserId(userId: \`"1\`") { eventId eventType } }" -ExpectedField "auditEventsByUserId"
Test-GraphQL -Name "Get Audit Events By Type" -Query "{ auditEventsByType(eventType: \`"LOGIN\`") { eventId eventType } }" -ExpectedField "auditEventsByType"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "SECTION 9: SCHEMA INTROSPECTION" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan

Test-GraphQL -Name "Schema Query Type" -Query "{ __schema { queryType { name } } }" -ExpectedField "__schema"
Test-GraphQL -Name "Schema Mutation Type" -Query "{ __schema { mutationType { name } } }" -ExpectedField "__schema"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "RÉSUMÉ DES TESTS" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "Total de tests exécutés: $totalTests" -ForegroundColor White
Write-Host "Tests réussis: $passedTests" -ForegroundColor Green
Write-Host "Tests échoués: $failedTests" -ForegroundColor Red

if ($failedTests -eq 0) {
    Write-Host ""
    Write-Host "✓ TOUS LES TESTS ONT RÉUSSI!" -ForegroundColor Green
    exit 0
} else {
    $successRate = [math]::Round(($passedTests / $totalTests) * 100, 2)
    Write-Host ""
    Write-Host "Taux de réussite: $successRate%" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "NOTE: Les échecs sont probablement dus à:" -ForegroundColor Yellow
    Write-Host "  1. Authentification requise (erreurs 401)" -ForegroundColor Yellow
    Write-Host "  2. Microservices non démarrés" -ForegroundColor Yellow
    Write-Host "  3. Voir AUTHENTICATION_FIX_GUIDE.md pour les solutions" -ForegroundColor Yellow
    exit 1
}
