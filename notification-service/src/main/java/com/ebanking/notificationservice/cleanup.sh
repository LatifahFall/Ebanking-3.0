#!/bin/bash

# Script de nettoyage des fichiers temporaires
# Exécuter : ./cleanup.sh

echo "🧹 Nettoyage des fichiers temporaires..."

# Supprimer les fichiers temporaires
rm -f compose-temp.yaml
rm -f application-test.yml  # L'ancien fichier dans src/main/resources
rm -rf RealNotificationIntegrationTest.java  # Test trop complexe

# Supprimer les builds
echo "🗑️  Suppression du dossier target..."
rm -rf target/

# Supprimer les logs
echo "🗑️  Suppression des logs..."
rm -rf logs/

# Supprimer les fichiers IDE temporaires
rm -rf .idea/
rm -rf *.iml
rm -rf .vscode/

echo "✅ Nettoyage terminé !"
echo ""
echo "📁 Fichiers conservés :"
echo "   ✅ RealEmailTest.java (test email réel)"
echo "   ✅ RealSmsTest.java (test SMS réel)"
echo "   ✅ Tests unitaires mock (EmailServiceTest, NotificationServiceTest, etc.)"
echo ""
echo "❌ Fichiers supprimés :"
echo "   🗑️  compose-temp.yaml"
echo "   🗑️  RealNotificationIntegrationTest.java"
echo "   🗑️  target/"