package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Service d'envoi de SMS via Twilio
 *
 * Configuration via application.yml:
 * - twilio.account.sid: ${TWILIO_ACCOUNT_SID}
 * - twilio.auth.token: ${TWILIO_AUTH_TOKEN}
 * - twilio.messaging.service.sid: ${TWILIO_MESSAGING_SERVICE_SID}
 * - twilio.phone.number: ${TWILIO_PHONE_NUMBER}
 *
 * ⚠️ COMPTE TRIAL : Vous devez VÉRIFIER les numéros destinataires sur :
 *    https://console.twilio.com/us1/develop/phone-numbers/manage/verified
 */
@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.messaging.service.sid}")
    private String messagingServiceSid;

    @Value("${twilio.phone.number:+18166769869}")
    private String fromPhoneNumber;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envoie un SMS via l'API Twilio avec Messaging Service
     */
    public void sendSms(Notification notification) {
        try {
            log.info("📱 Attempting to send SMS...");
            log.info("   ├─ To: {}", notification.getRecipient());
            log.info("   ├─ Message: {}", notification.getMessage());

            // URL de l'API Twilio
            String url = String.format(
                    "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json",
                    accountSid
            );

            // Corps de la requête
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("To", notification.getRecipient());
            body.add("MessagingServiceSid", messagingServiceSid);
            body.add("Body", notification.getMessage());

            // Headers avec authentification Basic
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = accountSid + ":" + authToken;
            String encodedAuth = Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);

            // Requête HTTP
            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            // Envoi
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ SMS sent successfully!");
                log.info("   └─ Twilio Response: {}", response.getBody());
            } else {
                log.error("❌ Failed to send SMS. Status: {}", response.getStatusCode());
                throw new RuntimeException("SMS sending failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error sending SMS: {}", e.getMessage());

            // Message d'aide pour compte trial
            if (e.getMessage().contains("21608") || e.getMessage().contains("not verified")) {
                log.error("⚠️  COMPTE TRIAL TWILIO DÉTECTÉ");
                log.error("   Le numéro {} n'est pas vérifié.", notification.getRecipient());
                log.error("   👉 Vérifiez-le sur : https://console.twilio.com/us1/develop/phone-numbers/manage/verified");
            }

            throw new RuntimeException("SMS sending failed: " + e.getMessage(), e);
        }
    }

    /**
     * Alternative : Envoi avec numéro Twilio au lieu du Messaging Service
     * Utilisez cette méthode si le Messaging Service ne fonctionne pas
     */
    public void sendSmsWithPhoneNumber(Notification notification) {
        try {
            log.info("📱 Sending SMS with phone number method...");

            String url = String.format(
                    "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json",
                    accountSid
            );

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("To", notification.getRecipient());
            body.add("From", fromPhoneNumber);  // +18166769869
            body.add("Body", notification.getMessage());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String auth = accountSid + ":" + authToken;
            String encodedAuth = Base64.getEncoder()
                    .encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ SMS sent successfully via phone number!");
            } else {
                throw new RuntimeException("SMS failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error sending SMS: {}", e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }
}