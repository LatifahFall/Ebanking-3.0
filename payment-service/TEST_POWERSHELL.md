# Guide de Test PowerShell - Paiement Biométrique

## Commandes PowerShell pour tester

### 1. Enregistrer votre visage (première fois)

```powershell
# Étape 1 : Convertir votre image en Base64
$imagePath = "C:\Users\latif\Pictures\ma_photo.jpg"  # Remplacez par le chemin de votre image
$imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
$base64Image = [System.Convert]::ToBase64String($imageBytes)

# Étape 2 : Créer le body de la requête
$body = @{
    faceImageBase64 = $base64Image
    deviceId = "device-123"
} | ConvertTo-Json

# Étape 3 : Envoyer la requête avec gestion d'erreur
try {
    $response = Invoke-RestMethod -Uri http://localhost:8082/api/payments/biometric/enroll `
        -Method Post `
        -ContentType "application/json" `
        -Body $body
    Write-Host "Succès !" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Erreur:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Détails:" -ForegroundColor Yellow
        Write-Host $_.ErrorDetails.Message -ForegroundColor Yellow
    }
    # Afficher le code de statut HTTP
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
        Write-Host "Code HTTP: $statusCode" -ForegroundColor Yellow
    }
}
```

### 2. Faire un paiement biométrique

```powershell
# Étape 1 : Convertir votre nouvelle image en Base64
$imagePath = "C:\Users\latif\Pictures\ma_photo_actuelle.jpg"
$imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
$base64Image = [System.Convert]::ToBase64String($imageBytes)

# Étape 2 : Créer le body de la requête
$body = @{
    fromAccountId = "123e4567-e89b-12d3-a456-426614174000"
    toAccountId = "123e4567-e89b-12d3-a456-426614174001"
    amount = 100.50
    currency = "EUR"
    biometricData = @{
        type = "FACE"
        template = $base64Image
        deviceId = "device-123"
        sessionId = "session-456"
    }
    reference = "Test payment"
    description = "Biometric payment test"
} | ConvertTo-Json -Depth 10

# Étape 3 : Envoyer la requête
Invoke-RestMethod -Uri http://localhost:8082/api/payments/biometric `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### 3. Vérifier le health

```powershell
Invoke-RestMethod -Uri http://localhost:8082/actuator/health -Method Get
```

### 4. Script complet (copier-coller)

```powershell
# Configuration
$serviceUrl = "http://localhost:8082"
$imagePath = "C:\Users\latif\Pictures\ma_photo.jpg"  # CHANGEZ-MOI !

# Convertir l'image en Base64
Write-Host "Conversion de l'image en Base64..." -ForegroundColor Yellow
$imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
$base64Image = [System.Convert]::ToBase64String($imageBytes)
Write-Host "Image convertie !" -ForegroundColor Green

# Enregistrer le visage
Write-Host "Enregistrement du visage..." -ForegroundColor Yellow
$enrollBody = @{
    faceImageBase64 = $base64Image
    deviceId = "device-123"
} | ConvertTo-Json

try {
    $enrollResponse = Invoke-RestMethod -Uri "$serviceUrl/api/payments/biometric/enroll" `
        -Method Post `
        -ContentType "application/json" `
        -Body $enrollBody
    Write-Host "Enregistrement réussi !" -ForegroundColor Green
    $enrollResponse | ConvertTo-Json
} catch {
    Write-Host "Erreur lors de l'enregistrement :" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
}

# Faire un paiement
Write-Host "`nTest de paiement biométrique..." -ForegroundColor Yellow
$paymentBody = @{
    fromAccountId = "123e4567-e89b-12d3-a456-426614174000"
    toAccountId = "123e4567-e89b-12d3-a456-426614174001"
    amount = 100.50
    currency = "EUR"
    biometricData = @{
        type = "FACE"
        template = $base64Image
        deviceId = "device-123"
        sessionId = "session-456"
    }
    reference = "Test payment"
    description = "Biometric payment test"
} | ConvertTo-Json -Depth 10

try {
    $paymentResponse = Invoke-RestMethod -Uri "$serviceUrl/api/payments/biometric" `
        -Method Post `
        -ContentType "application/json" `
        -Body $paymentBody
    Write-Host "Paiement réussi !" -ForegroundColor Green
    $paymentResponse | ConvertTo-Json
} catch {
    Write-Host "Erreur lors du paiement :" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host $responseBody -ForegroundColor Red
    }
}
```

