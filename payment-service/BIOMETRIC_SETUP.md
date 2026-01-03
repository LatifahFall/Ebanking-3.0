# Configuration du Paiement Biométrique

## Vue d'ensemble

Le service de paiement biométrique supporte plusieurs services de vérification faciale :
1. **Face++ API (Recommandé)** - COMPLÈTEMENT GRATUIT, pas besoin de carte de crédit
2. **Azure Face API** - Gratuit mais nécessite une carte de crédit
3. **Mode Fallback** - Vérification basique si aucun service externe n'est configuré

## Configuration Face++ API (Recommandé - Gratuit sans carte de crédit)

### Étape 1 : Créer un compte Face++ gratuit

1. Allez sur [https://www.faceplusplus.com/](https://www.faceplusplus.com/)
2. Cliquez sur "Sign Up" ou "Inscription"
3. Créez un compte gratuit (pas besoin de carte de crédit)
4. Confirmez votre email

### Étape 2 : Créer une application et obtenir les clés API

1. Connectez-vous à votre compte Face++
2. Allez dans "My Applications" ou "Mes Applications"
3. Cliquez sur "Create Application" ou "Créer une application"
4. Remplissez le formulaire :
   - **Application Name** : `ebanking-payment` (ou un nom de votre choix)
   - **Description** : Description de votre application
5. Une fois créée, vous obtiendrez :
   - **API Key** : Votre clé API
   - **API Secret** : Votre secret API

### Étape 3 : Configurer l'application

Ajoutez les variables d'environnement ou modifiez `application.yml` :

```yaml
biometric:
  faceplusplus:
    api:
      enabled: true
      key: YOUR_API_KEY_HERE
      secret: YOUR_API_SECRET_HERE
```

Ou via variables d'environnement :

```bash
export BIOMETRIC_FACEPLUSPLUS_API_ENABLED=true
export BIOMETRIC_FACEPLUSPLUS_API_KEY=YOUR_API_KEY_HERE
export BIOMETRIC_FACEPLUSPLUS_API_SECRET=YOUR_API_SECRET_HERE
```

## Limites du niveau gratuit Face++

- **30 000 appels par mois** (gratuit à vie)
- **Pas de limite de temps**
- **Pas besoin de carte de crédit**
- **Pas d'engagement**

## Configuration Azure Face API (Alternative - Nécessite carte de crédit)

**Note** : Azure nécessite une carte de crédit même pour le niveau gratuit. Si vous n'êtes pas éligible, utilisez **Face++** ci-dessus.

### Étape 1 : Créer un compte Azure gratuit

1. Allez sur [https://azure.microsoft.com/free/](https://azure.microsoft.com/free/)
2. Créez un compte Microsoft (si vous n'en avez pas)
3. Suivez le processus d'inscription (carte de crédit requise mais pas de frais pour le niveau gratuit)

### Étape 2 : Créer une ressource Face API

1. Connectez-vous au [Azure Portal](https://portal.azure.com/)
2. Cliquez sur "Créer une ressource"
3. Recherchez "Face" dans la marketplace
4. Sélectionnez "Face" et cliquez sur "Créer"
5. Remplissez le formulaire :
   - **Nom** : `ebanking-face-api` (ou un nom de votre choix)
   - **Abonnement** : Votre abonnement gratuit
   - **Région** : Choisissez la région la plus proche (ex: `westeurope`, `eastus`)
   - **Niveau tarifaire** : Sélectionnez "Free F0" (gratuit)
6. Cliquez sur "Créer"

### Étape 3 : Récupérer les clés API

1. Une fois la ressource créée, allez dans "Clés et point de terminaison"
2. Copiez :
   - **Clé 1** (ou Clé 2) : C'est votre `BIOMETRIC_AZURE_FACE_API_KEY`
   - **Point de terminaison** : C'est votre `BIOMETRIC_AZURE_FACE_API_ENDPOINT`
   - Format du endpoint : `https://YOUR_REGION.api.cognitive.microsoft.com`

### Étape 4 : Configurer l'application

Ajoutez les variables d'environnement ou modifiez `application.yml` :

```yaml
biometric:
  azure:
    face:
      api:
        enabled: true
        endpoint: https://YOUR_REGION.api.cognitive.microsoft.com
        key: YOUR_API_KEY_HERE
```

Ou via variables d'environnement :

```bash
export BIOMETRIC_AZURE_FACE_API_ENABLED=true
export BIOMETRIC_AZURE_FACE_API_ENDPOINT=https://YOUR_REGION.api.cognitive.microsoft.com
export BIOMETRIC_AZURE_FACE_API_KEY=YOUR_API_KEY_HERE
```

## Limites du niveau gratuit

- **30 000 transactions par mois**
- **20 transactions par minute**
- Pas de limite de temps (gratuit à vie tant que vous restez dans les limites)

## Mode Fallback

Si Azure Face API n'est pas configuré ou est indisponible, le système utilise un mode fallback qui effectue une vérification basique (vérifie que le template n'est pas vide).

Pour désactiver le fallback (recommandé en production) :

```yaml
biometric:
  verification:
    fallback:
      enabled: false
```

## Types de biométrie supportés

### Actuellement implémenté
- **FACE** : Utilise Azure Face API

### À venir (peut être ajouté facilement)
- **FINGERPRINT** : Nécessite un service externe ou bibliothèque
- **VOICE** : Azure Speech Services (gratuit jusqu'à 5 heures/mois)
- **IRIS** : Nécessite un service spécialisé

## Test de l'intégration

### 1. Vérifier la configuration

```bash
curl http://localhost:8082/actuator/health
```

### 2. Tester un paiement biométrique

```bash
curl -X POST http://localhost:8082/api/payments/biometric \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "fromAccountId": "123e4567-e89b-12d3-a456-426614174000",
    "toAccountId": "123e4567-e89b-12d3-a456-426614174001",
    "amount": 100.50,
    "currency": "EUR",
    "biometricData": {
      "type": "FACE",
      "template": "BASE64_ENCODED_IMAGE",
      "deviceId": "device-123",
      "sessionId": "session-456"
    },
    "reference": "Test payment",
    "description": "Biometric payment test"
  }'
```

## Format du template

Pour le type `FACE`, le template doit être une image encodée en **Base64** :
- Format supporté : JPEG, PNG, GIF, BMP
- Taille recommandée : 36x36 à 4096x4096 pixels
- Taille maximale : 6 MB

Exemple de conversion en Base64 (Java) :

```java
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

byte[] imageBytes = Files.readAllBytes(Paths.get("path/to/image.jpg"));
String base64Image = Base64.getEncoder().encodeToString(imageBytes);
```

## Dépannage

### Erreur 401/403
- Vérifiez que votre clé API est correcte
- Vérifiez que le endpoint est correct

### Erreur 429 (Rate Limit)
- Vous avez dépassé 20 transactions/minute
- Attendez quelques secondes et réessayez

### Erreur "No face detected"
- L'image ne contient pas de visage clairement visible
- Vérifiez la qualité de l'image
- Assurez-vous que le visage est bien visible et non masqué

## Comparaison des services

| Service | Gratuit | Carte de crédit | Quota gratuit | Recommandation |
|---------|---------|-----------------|---------------|----------------|
| **Face++** | ✅ Oui | ❌ Non | 30 000/mois | ⭐⭐⭐⭐⭐ Recommandé |
| **Azure Face API** | ✅ Oui | ✅ Oui | 30 000/mois | ⭐⭐⭐⭐ Si éligible |
| **AWS Rekognition** | ✅ Oui | ✅ Oui | 5 000/mois | ⭐⭐⭐ |
| **Google Cloud Vision** | ✅ Oui | ✅ Oui | 1 000/mois | ⭐⭐ |

## Alternatives gratuites

Si Face++ et Azure ne conviennent pas, voici d'autres options :

1. **AWS Rekognition** : 5 000 images/mois gratuites (nécessite carte de crédit)
2. **Google Cloud Vision API** : 1 000 requêtes/mois gratuites (nécessite carte de crédit)
3. **OpenCV** : Bibliothèque open-source (nécessite un serveur de traitement)

## Support

Pour plus d'informations :
- [Documentation Azure Face API](https://learn.microsoft.com/en-us/azure/ai-services/computer-vision/)
- [Pricing Azure Face API](https://azure.microsoft.com/pricing/details/cognitive-services/face-api/)


