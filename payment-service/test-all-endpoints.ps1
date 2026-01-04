Write-Host "=== Test de tous les endpoints Payment Service ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8082/api/payments"
$paymentId = $null

# Test 1: POST /api/payments - Créer un paiement standard
Write-Host "1. POST /api/payments (Standard Payment)" -ForegroundColor Yellow
try {
    $body = @{
        fromAccountId = 1
        toAccountId = 2
        amount = 100.50
        currency = "EUR"
        paymentType = "STANDARD"
        reference = "Test Standard Payment"
        description = "Test standard payment"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl" -Method Post -ContentType "application/json" -Body $body
    $paymentId = $response.id
    Write-Host "  ✅ SUCCESS - Payment ID: $paymentId, Status: $($response.status)" -ForegroundColor Green
} catch {
    Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 2: POST /api/payments - Créer un paiement instantané
Write-Host "2. POST /api/payments (Instant Payment)" -ForegroundColor Yellow
try {
    $body = @{
        fromAccountId = 1
        toAccountId = 2
        amount = 200.75
        currency = "EUR"
        paymentType = "INSTANT"
        reference = "Test Instant Payment"
        description = "Test instant payment"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl" -Method Post -ContentType "application/json" -Body $body
    Write-Host "  ✅ SUCCESS - Payment ID: $($response.id), Status: $($response.status)" -ForegroundColor Green
} catch {
    Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 3: POST /api/payments/biometric/generate-qr
Write-Host "3. POST /api/payments/biometric/generate-qr" -ForegroundColor Yellow
try {
    $body = @{
        fromAccountId = 1
        toAccountId = 2
        amount = 300.0
        currency = "EUR"
        reference = "Test Biometric QR"
        description = "Test biometric QR generation"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/biometric/generate-qr" -Method Post -ContentType "application/json" -Body $body
    Write-Host "  ✅ SUCCESS - QR Code generated" -ForegroundColor Green
    Write-Host "    PaymentId: $($response.paymentId)" -ForegroundColor Gray
    Write-Host "    QR Code present: $(if($response.qrCode){'Yes'}else{'No'})" -ForegroundColor Gray
    $biometricQrToken = $response.qrToken
} catch {
    Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($errorDetails.message)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 4: GET /api/payments/{id}
Write-Host "4. GET /api/payments/{id}" -ForegroundColor Yellow
if ($paymentId) {
    try {
        $response = Invoke-RestMethod -Uri "$baseUrl/$paymentId" -Method Get
        Write-Host "  ✅ SUCCESS - Payment ID: $($response.id), Status: $($response.status)" -ForegroundColor Green
        Write-Host "    Amount: $($response.amount) $($response.currency)" -ForegroundColor Gray
        Write-Host "    Type: $($response.paymentType)" -ForegroundColor Gray
    } catch {
        Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "  ⚠ SKIPPED - No payment ID available" -ForegroundColor Yellow
}
Write-Host ""

# Test 5: GET /api/payments?accountId=X
Write-Host "5. GET /api/payments?accountId=1" -ForegroundColor Yellow
try {
    $uri = [System.UriBuilder]::new("http://localhost:8082/api/payments")
    $uri.Query = "accountId=1&page=0&size=10"
    $response = Invoke-RestMethod -Uri $uri.Uri -Method Get
    Write-Host "  ✅ SUCCESS - Found $($response.totalElements) payments" -ForegroundColor Green
    Write-Host "    Page: $($response.currentPage), Size: $($response.pageSize)" -ForegroundColor Gray
    if ($response.payments -and $response.payments.Count -gt 0) {
        Write-Host "    First payment ID: $($response.payments[0].id), Status: $($response.payments[0].status)" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($errorDetails.message)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 6: GET /api/payments?accountId=X&status=COMPLETED
Write-Host "6. GET /api/payments?accountId=1&status=COMPLETED" -ForegroundColor Yellow
try {
    $uri = [System.UriBuilder]::new("http://localhost:8082/api/payments")
    $uri.Query = "accountId=1&status=COMPLETED&page=0&size=10"
    $response = Invoke-RestMethod -Uri $uri.Uri -Method Get
    Write-Host "  ✅ SUCCESS - Found $($response.totalElements) completed payments" -ForegroundColor Green
} catch {
    Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "    Error: $($errorDetails.message)" -ForegroundColor Red
    }
}
Write-Host ""

# Test 7: POST /api/payments/{id}/cancel - Créer un nouveau paiement PENDING pour le test
Write-Host "7. POST /api/payments/{id}/cancel" -ForegroundColor Yellow
try {
    # Créer un paiement qui sera en PENDING
    $cancelBody = @{
        fromAccountId = 1
        toAccountId = 2
        amount = 50.0
        currency = "EUR"
        paymentType = "STANDARD"
        reference = "Test Cancel Payment"
        description = "Test cancel payment"
    } | ConvertTo-Json
    
    $cancelPayment = Invoke-RestMethod -Uri "$baseUrl" -Method Post -ContentType "application/json" -Body $cancelBody
    $cancelPaymentId = $cancelPayment.id
    
    # Essayer de l'annuler (peut ne pas fonctionner si le statut n'est pas PENDING)
    try {
        $cancelResponse = Invoke-RestMethod -Uri "$baseUrl/$cancelPaymentId/cancel" -Method Post
        Write-Host "  ✅ SUCCESS - Payment $cancelPaymentId cancelled, Status: $($cancelResponse.status)" -ForegroundColor Green
    } catch {
        Write-Host "  ⚠ CANNOT CANCEL - Status is $($cancelPayment.status), cannot cancel" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# Test 8: POST /api/payments/{id}/reverse - Utiliser un paiement COMPLETED existant
Write-Host "8. POST /api/payments/{id}/reverse" -ForegroundColor Yellow
if ($paymentId) {
    try {
        $reverseResponse = Invoke-RestMethod -Uri "$baseUrl/$paymentId/reverse?reason=CUSTOMER_REQUEST" -Method Post
        Write-Host "  ✅ SUCCESS - Payment $paymentId reversed, Status: $($reverseResponse.status)" -ForegroundColor Green
    } catch {
        $status = $_.Exception.Response.StatusCode.value__
        if ($status -eq 409) {
            Write-Host "  ⚠ CANNOT REVERSE - Payment status does not allow reversal" -ForegroundColor Yellow
        } else {
            Write-Host "  ❌ FAILED - Status: $status" -ForegroundColor Red
            if ($_.ErrorDetails.Message) {
                $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
                Write-Host "    Error: $($errorDetails.message)" -ForegroundColor Red
            }
        }
    }
} else {
    Write-Host "  ⚠ SKIPPED - No payment ID available" -ForegroundColor Yellow
}
Write-Host ""

# Test 9: POST /api/payments/qrcode/generate (déjà testé mais on le refait pour être complet)
Write-Host "9. POST /api/payments/qrcode/generate" -ForegroundColor Yellow
try {
    $body = @{
        fromAccountId = 1
        toAccountId = 2
        amount = 400.0
        currency = "EUR"
        paymentType = "QR_CODE"
        reference = "Test QR Generate All"
        description = "Test QR code generation"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$baseUrl/qrcode/generate" -Method Post -ContentType "application/json" -Body $body
    Write-Host "  ✅ SUCCESS - QR Code generated" -ForegroundColor Green
    Write-Host "    PaymentId: $($response.paymentId)" -ForegroundColor Gray
    Write-Host "    qrCodeData present: $(if($response.qrCodeData){'Yes'}else{'No'})" -ForegroundColor Gray
} catch {
    Write-Host "  ❌ FAILED - $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "=== Tests terminés ===" -ForegroundColor Cyan

