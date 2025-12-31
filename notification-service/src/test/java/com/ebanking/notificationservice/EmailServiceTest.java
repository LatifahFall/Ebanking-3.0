package com.ebanking.notificationservice;

import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour EmailService
 * Note : Ces tests utilisent des mocks, donc pas besoin de serveur SMTP réel
 */
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        // Injecter la valeur fromEmail via réflexion
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@ebanking.com");

        testNotification = new Notification();
        testNotification.setType(NotificationType.EMAIL);
        testNotification.setRecipient("user@example.com");
        testNotification.setSubject("Test Email");
        testNotification.setMessage("Test message content");
    }

    @Test
    void sendEmail_Success() {
        // Arrange
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertDoesNotThrow(() -> emailService.sendEmail(testNotification));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_ThrowsException_WhenMailerFails() {
        // Arrange
        doThrow(new RuntimeException("SMTP connection failed"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> emailService.sendEmail(testNotification)
        );

        assertTrue(exception.getMessage().contains("Email sending failed"));
    }
}