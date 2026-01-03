# Test QR Code Payment - PowerShell Script

$baseUrl = "http://localhost:8082/api/payments"
$userId = "123e4567-e89b-12d3-a456-426614174000"  # Remplacez par un vrai UUID
$fromAccount = "123e4567-e89b-12d3-a456-426614174001"
$toAccount = "123e4567-e89b-12d3-a456-426614174002"

Write-Host "=== Étape 1: Générer le QR code ===" -ForegroundColor Green

$generateBody = @{
    fromAccountId = $fromAccount
    toAccountId = $toAccount
    amount = 100.50
    currency = "EUR"
    paymentType = "BIOMETRIC"
    reference = "TEST-QR-001"
    description = "Test QR code payment"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "$baseUrl/biometric/generate-qr" `
    -Method Post `
    -ContentType "application/json" `
    -Body $generateBody

Write-Host "Réponse:" -ForegroundColor Yellow
$response | ConvertTo-Json -Depth 10

$paymentId = $response.paymentId
$qrCodeBase64 = $response.qrCode

Write-Host "`nPayment ID: $paymentId" -ForegroundColor Cyan
Write-Host "QR Code (base64): $($qrCodeBase64.Substring(0, [Math]::Min(50, $qrCodeBase64.Length)))..." -ForegroundColor Cyan

# Décoder le QR code pour obtenir le token
try {
    $qrBytes = [Convert]::FromBase64String($qrCodeBase64)
    $qrJson = [System.Text.Encoding]::UTF8.GetString($qrBytes)
    Write-Host "`nQR Code JSON: $qrJson" -ForegroundColor Yellow
    
    $qrData = $qrJson | ConvertFrom-Json
    $qrToken = $qrData.token
    
    Write-Host "QR Token: $qrToken" -ForegroundColor Cyan
} catch {
    Write-Host "⚠️  Erreur lors du décodage du QR code: $_" -ForegroundColor Red
    Write-Host "Le QR code devrait contenir un JSON avec un champ 'token'" -ForegroundColor Yellow
    exit 1
}

Write-Host "`n=== Étape 2: Valider et traiter le paiement avec le QR code ===" -ForegroundColor Green

$paymentBody = @{
    fromAccountId = $fromAccount
    toAccountId = $toAccount
    amount = 100.50
    currency = "EUR"
    biometricData = @{
        type = "QR_CODE"
        qrToken = $qrToken
        deviceId = "test-device-001"
        sessionId = "test-session-001"
    }
    reference = "TEST-QR-001"
    description = "Test QR code payment"
} | ConvertTo-Json -Depth 10

$paymentResponse = Invoke-RestMethod -Uri "$baseUrl/biometric" `
    -Method Post `
    -ContentType "application/json" `
    -Body $paymentBody

Write-Host "Réponse du paiement:" -ForegroundColor Yellow
$paymentResponse | ConvertTo-Json -Depth 10

