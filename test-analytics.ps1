# ========================================
# GraphQL Gateway - Analytics Service Tests
# ========================================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  ANALYTICS SERVICE GraphQL Tests" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$graphqlUrl = "http://localhost:8090/graphql"
$testsPassed = 0
$testsFailed = 0
$testsBlocked = 0

function Test-Query {
    param($Name, $Query)
    
    Write-Host "Testing: $Name" -NoNewline
    try {
        $body = @{ query = $Query } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri $graphqlUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        
        if ($response.errors) {
            if ($response.errors[0].message -match "401|Unauthorized|UNAUTHORIZED") {
                Write-Host " [BLOCKED - Auth Required]" -ForegroundColor Yellow
                $script:testsBlocked++
            } else {
                Write-Host " [FAILED]" -ForegroundColor Red
                Write-Host "   Error: $($response.errors[0].message)" -ForegroundColor Red
                $script:testsFailed++
            }
        } else {
            Write-Host " [OK]" -ForegroundColor Green
            $script:testsPassed++
        }
    } catch {
        Write-Host " [ERROR]" -ForegroundColor Red
        Write-Host "   Exception: $($_.Exception.Message)" -ForegroundColor Red
        $script:testsFailed++
    }
}

# ==================== ANALYTICS QUERIES ====================

Write-Host "`n[1] ANALYTICS SERVICE QUERIES`n" -ForegroundColor Yellow

Test-Query "Active Alerts" @"
{ activeAlerts(userId: "test-user-123") { alertId userId alertType severity title message status triggeredAt } }
"@

Test-Query "Dashboard Summary" @"
{ dashboardSummary(userId: "test-user-123") { userId currentBalance monthlySpending monthlyIncome transactionsThisMonth generatedAt } }
"@

Test-Query "Spending Breakdown (MONTH)" @"
{ spendingBreakdown(userId: "test-user-123", period: "MONTH") { category amount count percentage } }
"@

Test-Query "Spending Breakdown (WEEK)" @"
{ spendingBreakdown(userId: "test-user-123", period: "WEEK") { category amount count percentage } }
"@

Test-Query "Balance Trend (30 days)" @"
{ balanceTrend(userId: "test-user-123", days: 30) { period dataPoints { timestamp value } } }
"@

Test-Query "Balance Trend (7 days)" @"
{ balanceTrend(userId: "test-user-123", days: 7) { period dataPoints { timestamp value } } }
"@

Test-Query "Recommendations" @"
{ recommendations(userId: "test-user-123") }
"@

Test-Query "Admin Overview" @"
{ adminOverview { activeUsers totalTransactions revenue } }
"@

# ==================== ANALYTICS MUTATIONS ====================

Write-Host "`n[2] ANALYTICS SERVICE MUTATIONS`n" -ForegroundColor Yellow

Test-Query "Resolve Alert" @"
mutation { resolveAlert(alertId: "alert-123") }
"@

# ==================== SUMMARY ====================

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "           TEST SUMMARY" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Total Tests:     " -NoNewline
Write-Host ($testsPassed + $testsFailed + $testsBlocked) -ForegroundColor White
Write-Host "Passed:          " -NoNewline
Write-Host $testsPassed -ForegroundColor Green
Write-Host "Failed:          " -NoNewline
Write-Host $testsFailed -ForegroundColor Red
Write-Host "Blocked (Auth):  " -NoNewline
Write-Host $testsBlocked -ForegroundColor Yellow

$successRate = if (($testsPassed + $testsFailed + $testsBlocked) -gt 0) {
    [math]::Round(($testsPassed / ($testsPassed + $testsFailed + $testsBlocked)) * 100, 1)
} else { 0 }

Write-Host "`nSuccess Rate:    " -NoNewline
if ($successRate -ge 90) {
    Write-Host "$successRate%" -ForegroundColor Green
} elseif ($successRate -ge 70) {
    Write-Host "$successRate%" -ForegroundColor Yellow
} else {
    Write-Host "$successRate%" -ForegroundColor Red
}

Write-Host "========================================`n" -ForegroundColor Cyan

if ($testsFailed -eq 0) {
    Write-Host "✓ All tests passed or blocked by auth!" -ForegroundColor Green
} else {
    Write-Host "✗ Some tests failed. Check errors above." -ForegroundColor Red
}
