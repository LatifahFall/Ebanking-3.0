package com.ebanking.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

/**
 * Service de gestion des templates de notifications
 *
 * RÔLE : Génère des messages personnalisés selon le type d'événement
 *
 * AVANTAGES :
 * - Messages cohérents et professionnels
 * - Facile à maintenir et modifier
 * - Support multilingue (future extension)
 */
@Service
@Slf4j
public class NotificationTemplateService {

    /**
     * Génère le template EMAIL pour une transaction complétée
     */
    public String generateTransactionEmailTemplate(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "Client");
        Double amount = (Double) data.getOrDefault("amount", 0.0);
        String transactionId = (String) data.getOrDefault("transactionId", "N/A");
        String date = (String) data.getOrDefault("date", "N/A");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                        .container { background-color: #ffffff; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 20px; }
                        .amount { font-size: 24px; font-weight: bold; color: #4CAF50; text-align: center; margin: 20px 0; }
                        .details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; color: #888; margin-top: 30px; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>✅ Transaction Confirmée</h2>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Votre transaction a été traitée avec succès.</p>
                            
                            <div class="amount">%.2f MAD</div>
                            
                            <div class="details">
                                <p><strong>ID Transaction :</strong> %s</p>
                                <p><strong>Date :</strong> %s</p>
                                <p><strong>Statut :</strong> <span style="color: #4CAF50;">✓ Complétée</span></p>
                            </div>
                            
                            <p>Si vous n'êtes pas à l'origine de cette transaction, contactez immédiatement notre service client.</p>
                        </div>
                        <div class="footer">
                            <p>E-Banking 3.0 - Service de Notification</p>
                            <p>Cet email est généré automatiquement, merci de ne pas y répondre.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, userName, amount, transactionId, date);
    }

    /**
     * Génère le template SMS pour une transaction
     */
    public String generateTransactionSmsTemplate(Map<String, Object> data) {
        Double amount = (Double) data.getOrDefault("amount", 0.0);
        String transactionId = (String) data.getOrDefault("transactionId", "N/A");

        return String.format(
                "E-Banking: Transaction de %.2f MAD effectuée avec succès. ID: %s. Si ce n'est pas vous, contactez-nous immédiatement.",
                amount, transactionId
        );
    }

    /**
     * Génère le template EMAIL pour un paiement
     */
    public String generatePaymentEmailTemplate(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "Client");
        Double amount = (Double) data.getOrDefault("amount", 0.0);
        String beneficiary = (String) data.getOrDefault("beneficiary", "N/A");
        String reference = (String) data.getOrDefault("reference", "N/A");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                        .container { background-color: #ffffff; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }
                        .header { background-color: #2196F3; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 20px; }
                        .amount { font-size: 24px; font-weight: bold; color: #2196F3; text-align: center; margin: 20px 0; }
                        .details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>💳 Paiement Effectué</h2>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Votre paiement a été traité avec succès.</p>
                            
                            <div class="amount">%.2f MAD</div>
                            
                            <div class="details">
                                <p><strong>Bénéficiaire :</strong> %s</p>
                                <p><strong>Référence :</strong> %s</p>
                                <p><strong>Statut :</strong> <span style="color: #4CAF50;">✓ Confirmé</span></p>
                            </div>
                            
                            <p>Merci d'utiliser E-Banking 3.0 pour vos transactions.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, userName, amount, beneficiary, reference);
    }

    /**
     * Génère le template SMS pour un paiement
     */
    public String generatePaymentSmsTemplate(Map<String, Object> data) {
        Double amount = (Double) data.getOrDefault("amount", 0.0);
        String beneficiary = (String) data.getOrDefault("beneficiary", "N/A");

        return String.format(
                "E-Banking: Paiement de %.2f MAD vers %s effectué avec succès.",
                amount, beneficiary
        );
    }

    /**
     * Génère le template pour alerte de sécurité
     */
    public String generateSecurityAlertTemplate(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "Client");
        String eventType = (String) data.getOrDefault("eventType", "Événement de sécurité");
        String ipAddress = (String) data.getOrDefault("ipAddress", "N/A");
        String location = (String) data.getOrDefault("location", "N/A");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                        .container { background-color: #ffffff; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }
                        .header { background-color: #f44336; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .alert { background-color: #ffebee; border-left: 5px solid #f44336; padding: 15px; margin: 20px 0; }
                        .details { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>🔒 Alerte de Sécurité</h2>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            
                            <div class="alert">
                                <p><strong>⚠️ Événement détecté :</strong> %s</p>
                            </div>
                            
                            <div class="details">
                                <p><strong>Adresse IP :</strong> %s</p>
                                <p><strong>Localisation :</strong> %s</p>
                            </div>
                            
                            <p>Si vous n'êtes pas à l'origine de cette action, contactez <strong>immédiatement</strong> notre service client.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, userName, eventType, ipAddress, location);
    }

    /**
     * Génère le template pour notification de connexion
     */
    public String generateLoginNotificationTemplate(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "Client");
        String device = (String) data.getOrDefault("device", "Appareil inconnu");
        String location = (String) data.getOrDefault("location", "Localisation inconnue");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #fff; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #2196F3;">🔐 Nouvelle Connexion Détectée</h2>
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Une nouvelle connexion à votre compte a été détectée :</p>
                        <div style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;">
                            <p><strong>Appareil :</strong> %s</p>
                            <p><strong>Localisation :</strong> %s</p>
                        </div>
                        <p>Si ce n'est pas vous, veuillez changer votre mot de passe immédiatement.</p>
                    </div>
                </body>
                </html>
                """, userName, device, location);
    }

    /**
     * Génère un template générique pour autres cas
     */
    public String generateGenericTemplate(String title, String message) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #fff; padding: 30px; border-radius: 10px;">
                        <h2 style="color: #2196F3;">%s</h2>
                        <p>%s</p>
                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; text-align: center; color: #888; font-size: 12px;">
                            <p>E-Banking 3.0 - Service de Notification</p>
                        </div>
                    </div>
                </body>
                </html>
                """, title, message);
    }

    /**
     * Génère le template EMAIL de bienvenue pour un nouveau compte
     */
    public String generateWelcomeEmailTemplate(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "Client");
        String accountType = (String) data.getOrDefault("accountType", "STANDARD");

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                        .container { background-color: #ffffff; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }
                        .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 20px; }
                        .cta-button { background-color: #4CAF50; color: white; padding: 15px 30px; text-align: center; border-radius: 5px; text-decoration: none; display: inline-block; margin: 20px 0; }
                        .features { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; color: #888; margin-top: 30px; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 Bienvenue chez E-Banking 3.0!</h1>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Nous sommes ravis de vous accueillir parmi nos clients!</p>
                            
                            <div class="features">
                                <p><strong>Votre compte %s</strong> a été créé avec succès.</p>
                                <p>Vous pouvez maintenant profiter de nos services :</p>
                                <ul>
                                    <li>✓ Virements instantanés</li>
                                    <li>✓ Paiements sécurisés</li>
                                    <li>✓ Trading crypto</li>
                                    <li>✓ Support 24/7</li>
                                </ul>
                            </div>
                            
                            <p style="text-align: center;">
                                <a href="#" class="cta-button">Découvrir Mon Espace</a>
                            </p>
                            
                            <p>Notre équipe reste à votre disposition pour toute question.</p>
                        </div>
                        <div class="footer">
                            <p>E-Banking 3.0 - Votre banque nouvelle génération</p>
                            <p>Cet email est généré automatiquement, merci de ne pas y répondre.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, userName, accountType);
    }

    /**
     * Génère le template EMAIL pour changement de statut KYC
     */
    public String generateKycStatusEmailTemplate(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "Client");
        String status = (String) data.getOrDefault("status", "UNKNOWN");
        String reason = (String) data.getOrDefault("reason", "");

        String statusColor = switch (status) {
            case "APPROVED" -> "#4CAF50";
            case "REJECTED" -> "#f44336";
            case "PENDING" -> "#FF9800";
            default -> "#2196F3";
        };

        String statusEmoji = switch (status) {
            case "APPROVED" -> "✅";
            case "REJECTED" -> "❌";
            case "PENDING" -> "⏳";
            default -> "📋";
        };

        String statusMessage = switch (status) {
            case "APPROVED" -> "Félicitations! Votre vérification d'identité a été approuvée. Vous avez maintenant accès à tous nos services.";
            case "REJECTED" -> "Malheureusement, votre vérification d'identité n'a pas pu être validée. Veuillez soumettre de nouveaux documents.";
            case "PENDING" -> "Votre demande de vérification est en cours de traitement. Nous vous tiendrons informé sous 24-48h.";
            default -> "Le statut de votre vérification d'identité a été mis à jour.";
        };

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                        .container { background-color: #ffffff; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }
                        .header { background-color: %s; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 20px; }
                        .status-box { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid %s; }
                        .footer { text-align: center; color: #888; margin-top: 30px; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>%s Mise à jour Statut KYC</h2>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>%s</p>
                            
                            <div class="status-box">
                                <p><strong>Nouveau Statut :</strong> %s</p>
                                %s
                            </div>
                            
                            <p>Pour toute question, notre équipe support est disponible 24/7.</p>
                        </div>
                        <div class="footer">
                            <p>E-Banking 3.0 - Service de Vérification</p>
                            <p>Cet email est généré automatiquement, merci de ne pas y répondre.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, statusColor, statusColor, statusEmoji, userName, statusMessage, status,
                !reason.isEmpty() ? String.format("<p><strong>Raison :</strong> %s</p>", reason) : "");
    }

    /**
     * Génère le template EMAIL pour transaction crypto
     */
    public String generateCryptoTransactionEmailTemplate(Map<String, Object> data) {
        String userName = (String) data.getOrDefault("userName", "Client");
        String transactionType = (String) data.getOrDefault("transactionType", "BUY");
        String cryptocurrency = (String) data.getOrDefault("cryptocurrency", "BTC");
        Double cryptoAmount = (Double) data.getOrDefault("cryptoAmount", 0.0);
        Double fiatAmount = (Double) data.getOrDefault("fiatAmount", 0.0);
        String fiatCurrency = (String) data.getOrDefault("fiatCurrency", "EUR");

        String action = "BUY".equals(transactionType) ? "Achat" : "Vente";
        String actionColor = "BUY".equals(transactionType) ? "#4CAF50" : "#2196F3";

        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                        .container { background-color: #ffffff; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }
                        .header { background-color: %s; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { padding: 20px; }
                        .crypto-box { background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0; text-align: center; }
                        .amount { font-size: 28px; font-weight: bold; color: %s; margin: 10px 0; }
                        .details { background-color: #fff; padding: 15px; border-radius: 5px; margin: 20px 0; border: 1px solid #eee; }
                        .footer { text-align: center; color: #888; margin-top: 30px; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h2>💰 %s de Cryptomonnaie Confirmé</h2>
                        </div>
                        <div class="content">
                            <p>Bonjour <strong>%s</strong>,</p>
                            <p>Votre transaction crypto a été traitée avec succès.</p>
                            
                            <div class="crypto-box">
                                <p style="font-size: 14px; color: #888;">%s DE</p>
                                <div class="amount">%.8f %s</div>
                                <p style="font-size: 14px; color: #888; margin-top: 15px;">POUR</p>
                                <div style="font-size: 24px; font-weight: bold; color: #333; margin-top: 5px;">
                                    %.2f %s
                                </div>
                            </div>
                            
                            <div class="details">
                                <p><strong>Type :</strong> %s</p>
                                <p><strong>Cryptomonnaie :</strong> %s</p>
                                <p><strong>Statut :</strong> <span style="color: %s;">✓ Confirmée</span></p>
                            </div>
                            
                            <p style="color: #888; font-size: 14px;">
                                ⚠️ Les cryptomonnaies sont volatiles. Investissez prudemment.
                            </p>
                        </div>
                        <div class="footer">
                            <p>E-Banking 3.0 - Trading Crypto</p>
                            <p>Cet email est généré automatiquement, merci de ne pas y répondre.</p>
                        </div>
                    </div>
                </body>
                </html>
                """, actionColor, actionColor, action, userName, action.toUpperCase(),
                cryptoAmount, cryptocurrency, fiatAmount, fiatCurrency,
                action, cryptocurrency, actionColor);
    }
}