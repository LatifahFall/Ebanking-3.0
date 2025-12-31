package com.ebanking.notificationservice.service;

import com.ebanking.notificationservice.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(Notification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());

            mailSender.send(message);

            log.info("✅ Email sent successfully");
            log.info("   ├─ To: {}", notification.getRecipient());
            log.info("   ├─ Subject: {}", notification.getSubject());
            log.info("   └─ Status: DELIVERED");

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}",
                    notification.getRecipient(), e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }
}