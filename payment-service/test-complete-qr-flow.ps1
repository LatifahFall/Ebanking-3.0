# Script de test complet du flux QR code
# Ce script teste la génération puis le traitement d'un paiement QR code

Write-Host "=== Test complet du flux QR code ===" -ForegroundColor Cyan
Write-Host ""

# Étape 1: Générer un QR code via /biometric/generate-qr (qui retourne le paymentId)
Write-Host "Étape 1: Génération du QR code biométrique..." -ForegroundColor Yellow

$generateBody = @{
    fromAccountId = 1
    toAccountId = 2
    amount = 300.0
    currency = "EUR"
    paymentType = "BIOMETRIC"
    reference = "Complete QR Test"
    description = "Test complet du flux QR code"
} | ConvertTo-Json

try {
    $generateResponse = Invoke-RestMethod -Uri "http://localhost:8082/api/payments/biometric/generate-qr" `
        -Method Post `
        -ContentType "application/json" `
        -Body $generateBody
    
    Write-Host "✅ QR Code généré avec succès!" -ForegroundColor Green
    Write-Host "Payment ID: $($generateResponse.paymentId)" -ForegroundColor Cyan
    Write-Host ""
    
    $paymentId = $generateResponse.paymentId
    
    # Étape 2: Récupérer les détails du paiement
    Write-Host "Étape 2: Récupération des détails du paiement..." -ForegroundColor Yellow
    $paymentDetails = Invoke-RestMethod -Uri "http://localhost:8082/api/payments/$paymentId" -Method Get
    Write-Host "Status: $($paymentDetails.status)" -ForegroundColor Cyan
    Write-Host "Amount: $($paymentDetails.amount) $($paymentDetails.currency)" -ForegroundColor Cyan
    Write-Host ""
    
    # Étape 3: Pour traiter le paiement, nous avons besoin du qrToken
    # Le qrToken est dans le qrCodeData stocké dans la table qr_code_payment
    Write-Host "Étape 3: Récupération du qrToken depuis la base de données..." -ForegroundColor Yellow
    Write-Host "Note: Le qrToken est stocké dans qr_code_payment.qr_token pour payment_id = $paymentId" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Pour récupérer le token, exécutez cette requête SQL:" -ForegroundColor Yellow
    Write-Host "SELECT qr_token, qr_code_data FROM qr_code_payment WHERE payment_id = $paymentId;" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "OU utilisez l'endpoint /api/payments/biometric avec le qrToken" -ForegroundColor Yellow
    Write-Host ""
    
    # Étape 4: Test avec l'endpoint /api/payments/biometric
    Write-Host "Étape 4: Test de l'endpoint /api/payments/biometric..." -ForegroundColor Yellow
    Write-Host "Note: Vous devez remplacer 'YOUR_QR_TOKEN' par le vrai token depuis la base" -ForegroundColor Gray
    Write-Host ""
    
    $biometricBody = @{
        fromAccountId = 1
        toAccountId = 2
        amount = 300.0
        currency = "EUR"
        biometricData = @{
            type = "QR_CODE"
            qrToken = "YOUR_QR_TOKEN"  # À remplacer par le vrai token
            deviceId = "test-device"
            sessionId = "test-session"
        }
    } | ConvertTo-Json -Depth 5
    
    Write-Host "Structure de la requête:" -ForegroundColor Cyan
    Write-Host $biometricBody
    Write-Host ""
    Write-Host "⚠️  Pour un test complet:" -ForegroundColor Yellow
    Write-Host "   1. Exécutez la requête SQL ci-dessus pour obtenir le qrToken" -ForegroundColor Yellow
    Write-Host "   2. Remplacez 'YOUR_QR_TOKEN' dans le script" -ForegroundColor Yellow
    Write-Host "   3. Relancez ce script" -ForegroundColor Yellow
    Write-Host ""
    
} catch {
    Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "Détails:" -ForegroundColor Yellow
        $errorDetails | ConvertTo-Json -Depth 5
    }
}

