package com.ebanking.notificationservice;

import com.ebanking.notificationservice.config.TestConfig;
import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Test EMAIL RÉEL - Envoie un vrai email via Gmail SMTP
 *
 * IMPORTANT : Changez l'email destinataire avant d'exécuter
 * Commande : mvn test -Dtest=RealEmailTest
 */
@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=PLAINTEXT://localhost:9092",
        "spring.kafka.consumer.auto-offset-reset=latest",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class RealEmailTest {

    @Autowired
    private EmailService emailService;

    @Test
    void testRealEmailSending() {
        System.out.println("\n🚀 DÉBUT DU TEST EMAIL RÉEL...\n");

        // ⚠️ CHANGEZ L'EMAIL ICI
        String destinataire = "ben10aabir@gmail.com";

        Notification notification = new Notification();
        notification.setType(NotificationType.EMAIL);
        notification.setRecipient(destinataire);
        notification.setSubject("✅ Test E-Banking Notification Service");
        notification.setMessage(
                "Bonjour,\n\n" +
                        "Ceci est un email de test envoyé depuis le notification-service.\n\n" +
                        "Si vous recevez cet email, cela signifie que :\n" +
                        "✅ La configuration Gmail SMTP fonctionne\n" +
                        "✅ Le service d'email est opérationnel\n" +
                        "✅ L'intégration Spring Mail est correcte\n\n" +
                        "Cordialement,\n" +
                        "E-Banking Notification Service"
        );

        try {
            emailService.sendEmail(notification);
            System.out.println("✅ Email envoyé avec succès !");
            System.out.println("📧 Destinataire : " + destinataire);
            System.out.println("📬 Vérifiez votre boîte mail (et le dossier spam)");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi : " + e.getMessage());
            throw e;
        }
    }
}