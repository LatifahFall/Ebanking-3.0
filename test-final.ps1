# Comprehensive GraphQL Gateway Tests
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "   GraphQL Gateway - Full Endpoint Test" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$baseUrl = "http://localhost:8090/graphql"
$success = 0
$failed = 0

function Test-Query {
    param([string]$Name, [string]$Query)
    
    Write-Host "Testing: $Name ... " -NoNewline
    
    $body = @{ query = $Query } | ConvertTo-Json -Compress
    
    try {
        $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        
        if ($response.data) {
            Write-Host "[OK]" -ForegroundColor Green
            $script:success++
            return $true
        } elseif ($response.errors) {
            $err = $response.errors[0].message
            if ($err -match "401|Unauthorized") {
                Write-Host "[BLOCKED - 401]" -ForegroundColor Yellow
            } elseif ($err -match "INTERNAL_ERROR") {
                Write-Host "[ERROR - Service Down]" -ForegroundColor Magenta
            } else {
                Write-Host "[FAIL] $($err.Substring(0, [Math]::Min(60, $err.Length)))" -ForegroundColor Red
                $script:failed++
            }
            return $false
        }
    } catch {
        Write-Host "[EXCEPTION]" -ForegroundColor Red
        $script:failed++
        return $false
    }
}

# HEALTH & SCHEMA
Write-Host "`n--- SYSTEM CHECKS ---" -ForegroundColor Cyan
Test-Query "Health" "{ health }"
Test-Query "Query Type" "{ __schema { queryType { name } } }"
Test-Query "Mutation Type" "{ __schema { mutationType { name } } }"

# USER SERVICE
Write-Host "`n--- USER SERVICE ---" -ForegroundColor Cyan
Test-Query "All Users" "{ users { id login email } }"
Test-Query "User By ID" "query { userById(id: 1) { id login } }"
Test-Query "Me" "query { me(id: 1) { id email } }"

# ACCOUNT SERVICE  
Write-Host "`n--- ACCOUNT SERVICE ---" -ForegroundColor Cyan
Test-Query "Account By ID" "query { accountById(id: 1) { accountNumber balance } }"
Test-Query "Accounts By User" "query { accountsByUserId(userId: 1) { accountNumber } }"

# AUTH SERVICE
Write-Host "`n--- AUTH SERVICE ---" -ForegroundColor Cyan
Test-Query "Verify Token" "query { verifyToken(token: ""abc"") }"

# PAYMENT SERVICE
Write-Host "`n--- PAYMENT SERVICE ---" -ForegroundColor Cyan
Test-Query "Payment By ID" "query { paymentById(id: 1) { amount status } }"

# CRYPTO SERVICE
Write-Host "`n--- CRYPTO SERVICE ---" -ForegroundColor Cyan
Test-Query "Crypto Wallet" "query { cryptoWalletByUserId(userId: 1) { balance } }"
Test-Query "All Coins" "{ cryptoCoins { symbol name currentPrice } }"

# NOTIFICATION SERVICE
Write-Host "`n--- NOTIFICATION SERVICE ---" -ForegroundColor Cyan
Test-Query "User Notifications" "query { notificationsByUserId(userId: ""1"") { message status } }"

# AUDIT SERVICE
Write-Host "`n--- AUDIT SERVICE ---" -ForegroundColor Cyan
Test-Query "All Events" "{ auditEvents { eventType timestamp } }"
Test-Query "Events By User" "query { auditEventsByUserId(userId: 1) { eventType } }"

# MUTATION TEST
Write-Host "`n--- MUTATIONS ---" -ForegroundColor Cyan
Test-Query "Login" "mutation { login(input: { username: ""test"", password: ""test"" }) { access_token refresh_token } }"

# SUMMARY
$total = $success + $failed
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Total: $total | Success: $success | Failed: $failed" -ForegroundColor White
if ($total -gt 0) {
    $rate = [math]::Round(($success/$total)*100, 1)
    Write-Host "Success Rate: $rate%" -ForegroundColor $(if ($rate -gt 75) {"Green"} else {"Yellow"})
}
Write-Host "========================================`n" -ForegroundColor Cyan
