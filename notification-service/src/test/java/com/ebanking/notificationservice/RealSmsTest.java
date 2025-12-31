package com.ebanking.notificationservice;

import com.ebanking.notificationservice.config.TestConfig;
import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

/**
 * Test SMS RÉEL via Twilio
 *
 * PRÉREQUIS : Vérifiez votre numéro sur Twilio Console
 * URL : https://console.twilio.com/us1/develop/phone-numbers/manage/verified
 * Commande : mvn test -Dtest=RealSmsTest
 */
@SpringBootTest
@Import(TestConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=PLAINTEXT://localhost:9092",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class RealSmsTest {

    @Autowired
    private SmsService smsService;

    @Test
    void testRealSmsSending() {
        System.out.println("\n📱 DÉBUT DU TEST SMS RÉEL (Twilio)...\n");

        // ⚠️ IMPORTANT : Numéro doit être VÉRIFIÉ sur Twilio Console
        String numeroDestinaire = "+212768580831";

        Notification notification = new Notification();
        notification.setType(NotificationType.SMS);
        notification.setRecipient(numeroDestinaire);
        notification.setSubject("Test SMS");
        notification.setMessage("Test E-Banking: Votre service de notification SMS fonctionne correctement!");

        try {
            System.out.println("📤 Envoi en cours vers : " + numeroDestinaire);
            smsService.sendSms(notification);
            System.out.println("✅ SMS envoyé avec succès !");
            System.out.println("📱 Vérifiez votre téléphone dans quelques secondes");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi : " + e.getMessage());

            if (e.getMessage().contains("21608") || e.getMessage().contains("not verified")) {
                System.err.println("\n⚠️  ERREUR COMPTE TRIAL TWILIO");
                System.err.println("   Le numéro " + numeroDestinaire + " n'est PAS vérifié.");
                System.err.println("   👉 Vérifiez-le sur : https://console.twilio.com/us1/develop/phone-numbers/manage/verified");
            }

            throw e;
        }
    }
}
