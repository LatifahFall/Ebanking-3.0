# Script de test pour l'endpoint /qrcode/generate
$body = @{
    fromAccountId = 1
    toAccountId = 2
    amount = 100.5
    currency = "EUR"
    paymentType = "QR_CODE"
    reference = "Payment ref 123"
    description = "QR code payment"
} | ConvertTo-Json

Write-Host "Testing QR code generation endpoint..." -ForegroundColor Cyan
Write-Host "URL: http://localhost:8082/api/payments/qrcode/generate" -ForegroundColor Cyan
Write-Host "Body: $body" -ForegroundColor Gray
Write-Host ""

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/payments/qrcode/generate" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body
    
    Write-Host "✅ QR Code généré avec succès!" -ForegroundColor Green
    Write-Host "`nRéponse:" -ForegroundColor Cyan
    $response | ConvertTo-Json -Depth 10
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    Write-Host "❌ Erreur HTTP: $statusCode" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    
    # Essayer d'obtenir plus de détails
    if ($_.ErrorDetails.Message) {
        Write-Host "`nDétails de l'erreur:" -ForegroundColor Yellow
        try {
            $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
            $errorDetails | ConvertTo-Json -Depth 10
        } catch {
            Write-Host $_.ErrorDetails.Message
        }
    }
    
    # Afficher la réponse brute si disponible
    if ($_.Exception.Response) {
        try {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "`nRéponse brute:" -ForegroundColor Yellow
            Write-Host $responseBody
        } catch {
            Write-Host "Impossible de lire la réponse brute"
        }
    }
}

