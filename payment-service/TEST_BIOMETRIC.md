# Guide de Test - Paiement Biométrique

## Prérequis

1. **Service démarré** : Le payment-service doit être en cours d'exécution
2. **Base de données** : PostgreSQL doit être accessible
3. **Face++ API** : Les clés API doivent être configurées dans `application.yml`

## 1. Démarrer le service

```bash
cd payment-service
mvn spring-boot:run
```

Ou si vous utilisez un IDE, lancez la classe `PaymentServiceApplication`.

Le service devrait démarrer sur le port **8082** (par défaut).

## 2. Vérifier que le service est démarré

```bash
curl http://localhost:8082/actuator/health
```

Vous devriez recevoir :
```json
{
  "status": "UP"
}
```

## 3. Tester l'endpoint de paiement biométrique

### Endpoint
```
POST http://localhost:8082/api/payments/biometric
```

### Headers requis
```
Content-Type: application/json
```

**Note importante :**
- **En développement** : Aucun token d'authentification requis. Le service fonctionne sans le service auth.
- **En production** : Quand le service auth sera disponible, Keycloak sera activé et un token JWT sera requis dans le header :
  ```
  Authorization: Bearer <token_jwt>
  ```

### Corps de la requête

```json
{
  "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
  "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
  "amount": 100.50,
  "currency": "EUR",
  "biometricData": {
    "type": "FACE",
    "template": "BASE64_ENCODED_IMAGE_HERE",
    "deviceId": "device-123",
    "sessionId": "session-456"
  },
  "reference": "Test payment",
  "description": "Biometric payment test"
}
```

### Exemple avec cURL (Linux/Mac)

```bash
curl -X POST http://localhost:8082/api/payments/biometric \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
    "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
    "amount": 100.50,
    "currency": "EUR",
    "biometricData": {
      "type": "FACE",
      "template": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
      "deviceId": "device-123",
      "sessionId": "session-456"
    },
    "reference": "Test payment",
    "description": "Biometric payment test"
  }'
```

### Exemple avec PowerShell (Windows)

```powershell
# Créer le body de la requête
$body = @{
    fromAccountId = "123e4567-e89b-12d3-a456-426614174000"
    toAccountId = "123e4567-e89b-12d3-a456-426614174001"
    amount = 100.50
    currency = "EUR"
    biometricData = @{
        type = "FACE"
        template = "VOTRE_IMAGE_BASE64_ICI"
        deviceId = "device-123"
        sessionId = "session-456"
    }
    reference = "Test payment"
    description = "Biometric payment test"
} | ConvertTo-Json -Depth 10

# Envoyer la requête
Invoke-RestMethod -Uri http://localhost:8082/api/payments/biometric `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

## 4. Convertir une image en Base64

### Sur Windows (PowerShell)

```powershell
# Lire une image et la convertir en Base64
$imageBytes = [System.IO.File]::ReadAllBytes("C:\path\to\your\image.jpg")
$base64Image = [System.Convert]::ToBase64String($imageBytes)
Write-Host $base64Image
```

### Sur Linux/Mac

```bash
# Convertir une image en Base64
base64 -i /path/to/your/image.jpg
```

### En ligne de commande (toutes plateformes)

```bash
# Avec Python
python -c "import base64; print(base64.b64encode(open('image.jpg', 'rb').read()).decode())"
```

## 6. Exemple complet avec une vraie image

### Étape 1 : Convertir votre image

```powershell
# PowerShell
$imagePath = "C:\Users\latif\Pictures\face.jpg"
$imageBytes = [System.IO.File]::ReadAllBytes($imagePath)
$base64Image = [System.Convert]::ToBase64String($imageBytes)
```

### Étape 2 : Créer le fichier JSON

Créez un fichier `test-biometric.json` :

```json
{
  "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
  "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
  "amount": 50.00,
  "currency": "EUR",
  "biometricData": {
    "type": "FACE",
    "template": "VOTRE_BASE64_ICI",
    "deviceId": "mobile-app-001",
    "sessionId": "session-2024-01-02-001"
  },
  "reference": "PAY-BIO-001",
  "description": "Test paiement biométrique"
}
```

### Étape 3 : Envoyer la requête

```bash
curl -X POST http://localhost:8082/api/payments/biometric \
  -H "Content-Type: application/json" \
  -d @test-biometric.json
```

## 7. Réponses possibles

### Succès (200 OK)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
  "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
  "amount": 100.50,
  "currency": "EUR",
  "paymentType": "BIOMETRIC",
  "status": "COMPLETED",
  "reference": "Test payment",
  "description": "Biometric payment test",
  "createdAt": "2024-01-02T10:30:00Z",
  "completedAt": "2024-01-02T10:30:01Z"
}
```

### Erreur - Vérification biométrique échouée (401)

```json
{
  "timestamp": "2024-01-02T10:30:00Z",
  "status": 401,
  "error": "Biometric Verification Failed",
  "message": "Biometric verification failed",
  "path": "/api/payments/biometric"
}
```

### Erreur - Aucun visage détecté (400)

```json
{
  "timestamp": "2024-01-02T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "No face detected in image",
  "path": "/api/payments/biometric"
}
```

## 8. Tester avec Swagger UI

1. Ouvrez votre navigateur
2. Allez sur : `http://localhost:8082/swagger-ui.html`
3. Trouvez l'endpoint `POST /api/payments/biometric`
4. Cliquez sur "Try it out"
5. Remplissez le formulaire avec vos données
6. Cliquez sur "Execute"

## 9. Vérifier les logs

Les logs du service afficheront :
- La détection du visage avec Face++ API
- Le résultat de la vérification
- Les erreurs éventuelles

Exemple de logs :
```
INFO  - Verifying biometric data for user: 123e4567-e89b-12d3-a456-426614174000, type: FACE
DEBUG - Using Face++ API for face verification
DEBUG - Detecting face in image using Face++ API
DEBUG - Face detected successfully for user: 123e4567-e89b-12d3-a456-426614174000 with Face++
INFO  - Biometric verification successful for user: 123e4567-e89b-12d3-a456-426614174000
```

## 10. Configuration Keycloak

**Par défaut, Keycloak est désactivé** pour permettre les tests sans le service auth.

- **En développement** : Keycloak est désactivé (`keycloak.enabled=false` par défaut)
- **En production** : Keycloak sera activé quand le service auth sera disponible

Pour activer Keycloak manuellement (si vous avez accès au service auth) :
```bash
export KEYCLOAK_ENABLED=true
```

Ou dans `application.yml` :
```yaml
keycloak:
  enabled: true
```

## 11. Tester le mode fallback

Si Face++ n'est pas disponible, le service utilisera le mode fallback :

```yaml
biometric:
  faceplusplus:
    api:
      enabled: false
  verification:
    fallback:
      enabled: true
```

Le mode fallback vérifie simplement que le template n'est pas vide (pour le développement uniquement).

## Notes importantes

1. **Format d'image** : Face++ supporte JPEG, PNG, GIF, BMP
2. **Taille** : Recommandé entre 36x36 et 4096x4096 pixels
3. **Taille maximale** : 6 MB
4. **Qualité** : L'image doit contenir un visage clairement visible
5. **Base64** : Le template doit être l'image encodée en Base64 (sans le préfixe `data:image/...`)

## Dépannage

### Erreur "Face++ API is not enabled or configured"
- Vérifiez que `biometric.faceplusplus.api.enabled=true` dans `application.yml`
- Vérifiez que les clés API sont correctes

### Erreur "No face detected in image"
- Vérifiez que l'image contient un visage
- Vérifiez que l'image est correctement encodée en Base64
- Essayez avec une autre image

### Erreur "Authentication failed"
- Vérifiez vos clés API Face++
- Vérifiez que votre compte Face++ est actif

### Service ne démarre pas
- Vérifiez que PostgreSQL est accessible
- Vérifiez les logs pour les erreurs de configuration

