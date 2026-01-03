#!/bin/bash

# Configuration
BASE_URL="http://localhost:8082/api/payments"
USER_ID="123e4567-e89b-12d3-a456-426614174000"  # Remplacez par un vrai UUID
FROM_ACCOUNT="123e4567-e89b-12d3-a456-426614174001"
TO_ACCOUNT="123e4567-e89b-12d3-a456-426614174002"

echo "=== Étape 1: Générer le QR code ==="
RESPONSE=$(curl -s -X POST "$BASE_URL/biometric/generate-qr" \
  -H "Content-Type: application/json" \
  -d "{
    \"fromAccountId\": \"$FROM_ACCOUNT\",
    \"toAccountId\": \"$TO_ACCOUNT\",
    \"amount\": 100.50,
    \"currency\": \"EUR\",
    \"paymentType\": \"BIOMETRIC\",
    \"reference\": \"TEST-QR-001\",
    \"description\": \"Test QR code payment\"
  }")

echo "$RESPONSE" | jq '.'

# Extraire paymentId et qrToken
PAYMENT_ID=$(echo "$RESPONSE" | jq -r '.paymentId')
QR_CODE_BASE64=$(echo "$RESPONSE" | jq -r '.qrCode')

echo ""
echo "Payment ID: $PAYMENT_ID"
echo "QR Code (base64): ${QR_CODE_BASE64:0:50}..."

# Décoder le QR code pour obtenir le token
# Le QR code contient un JSON avec le token
QR_JSON=$(echo "$QR_CODE_BASE64" | base64 -d 2>/dev/null || echo "")
if [ -z "$QR_JSON" ]; then
  # Si base64 -d échoue, essayer avec python
  QR_JSON=$(python3 -c "import base64, sys; print(base64.b64decode('$QR_CODE_BASE64').decode('utf-8'))" 2>/dev/null || echo "")
fi

echo ""
echo "QR Code JSON: $QR_JSON"

# Extraire le token du JSON
QR_TOKEN=$(echo "$QR_JSON" | jq -r '.token' 2>/dev/null || echo "")

if [ -z "$QR_TOKEN" ]; then
  echo "⚠️  Impossible d'extraire le token. Utilisez le token manuellement."
  echo "Le QR code JSON devrait contenir un champ 'token'"
  exit 1
fi

echo ""
echo "QR Token: $QR_TOKEN"
echo ""
echo "=== Étape 2: Valider et traiter le paiement avec le QR code ==="

curl -s -X POST "$BASE_URL/biometric" \
  -H "Content-Type: application/json" \
  -d "{
    \"fromAccountId\": \"$FROM_ACCOUNT\",
    \"toAccountId\": \"$TO_ACCOUNT\",
    \"amount\": 100.50,
    \"currency\": \"EUR\",
    \"biometricData\": {
      \"type\": \"QR_CODE\",
      \"qrToken\": \"$QR_TOKEN\",
      \"deviceId\": \"test-device-001\",
      \"sessionId\": \"test-session-001\"
    },
    \"reference\": \"TEST-QR-001\",
    \"description\": \"Test QR code payment\"
  }" | jq '.'

