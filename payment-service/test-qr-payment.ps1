# Script de test complet pour le processus de paiement QR code
Write-Host "=== Test complet du processus de paiement QR code ===" -ForegroundColor Cyan
Write-Host ""

# Étape 1: Générer un QR code
Write-Host "Étape 1: Génération du QR code..." -ForegroundColor Yellow
$generateBody = @{
    fromAccountId = 1
    toAccountId = 2
    amount = 100.5
    currency = "EUR"
    paymentType = "QR_CODE"
    reference = "Payment ref 123"
    description = "QR code payment test"
} | ConvertTo-Json

try {
    $generateResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/payments/qrcode/generate" `
        -Method Post `
        -ContentType "application/json" `
        -Body $generateBody
    
    Write-Host "✅ QR Code généré avec succès!" -ForegroundColor Green
    Write-Host "QR Code (base64): $($generateResponse.qrCode.Substring(0, [Math]::Min(100, $generateResponse.qrCode.Length)))..." -ForegroundColor Gray
    Write-Host ""
    
    # Le QR code est une image PNG, mais nous devons récupérer les données JSON depuis la base
    # Pour cela, nous devons trouver le paymentId. Mais la réponse ne le contient pas.
    # Nous devons utiliser l'endpoint /biometric/generate-qr qui retourne le paymentId
    # OU récupérer le dernier paiement créé
    
    Write-Host "Étape 2: Récupération des données du QR code..." -ForegroundColor Yellow
    
    # Utilisons l'endpoint /biometric/generate-qr qui retourne plus d'informations
    $biometricBody = @{
        fromAccountId = 1
        toAccountId = 2
        amount = 100.5
        currency = "EUR"
        paymentType = "BIOMETRIC"
        reference = "Payment ref 456"
        description = "Biometric QR code payment test"
    } | ConvertTo-Json
    
    $biometricResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/payments/biometric/generate-qr" `
        -Method Post `
        -ContentType "application/json" `
        -Body $biometricBody
    
    Write-Host "✅ QR Code biométrique généré!" -ForegroundColor Green
    Write-Host "Payment ID: $($biometricResponse.paymentId)" -ForegroundColor Cyan
    Write-Host ""
    
    # Maintenant, récupérons le paiement pour obtenir les détails
    $paymentId = $biometricResponse.paymentId
    $paymentDetails = Invoke-RestMethod -Uri "http://localhost:8082/api/payments/$paymentId" -Method Get
    
    Write-Host "Détails du paiement:" -ForegroundColor Cyan
    $paymentDetails | ConvertTo-Json -Depth 5
    Write-Host ""
    
    # Pour traiter le paiement, nous devons utiliser l'endpoint /qrcode avec le qrCodeData
    # Le qrCodeData doit être le JSON qui contient le token
    # Nous devons récupérer ce token depuis la base de données via le paymentId
    
    Write-Host "Étape 3: Traitement du paiement avec QR code..." -ForegroundColor Yellow
    Write-Host "Note: Pour tester /api/payments/qrcode, nous avons besoin du qrCodeData (JSON avec token)" -ForegroundColor Gray
    Write-Host "Ce token est stocké dans la table qr_code_payment et peut être récupéré via le paymentId" -ForegroundColor Gray
    Write-Host ""
    
    # Pour l'instant, créons un QR code data fictif basé sur la structure attendue
    # En production, ce JSON serait scanné depuis le QR code
    $qrCodeDataJson = @{
        token = "test-token-$(Get-Date -Format 'yyyyMMddHHmmss')"
        paymentId = $paymentId.ToString()
        userId = "1"
        amount = "100.5"
        currency = "EUR"
        timestamp = [DateTimeOffset]::Now.ToUnixTimeMilliseconds()
    } | ConvertTo-Json -Compress
    
    Write-Host "QR Code Data (exemple): $qrCodeDataJson" -ForegroundColor Gray
    Write-Host ""
    Write-Host "⚠️  Pour un test complet, vous devez:" -ForegroundColor Yellow
    Write-Host "   1. Récupérer le vrai token depuis la base de données (table qr_code_payment)" -ForegroundColor Yellow
    Write-Host "   2. Ou scanner le QR code pour obtenir le JSON" -ForegroundColor Yellow
    Write-Host ""
    
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Détails: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
}

