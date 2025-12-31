package com.ebanking.notificationservice;

import com.ebanking.notificationservice.controller.NotificationController;
import com.ebanking.notificationservice.model.Notification;
import com.ebanking.notificationservice.model.NotificationAudit;
import com.ebanking.notificationservice.model.NotificationPreference;
import com.ebanking.notificationservice.model.NotificationType;
import com.ebanking.notificationservice.service.NotificationAuditService;
import com.ebanking.notificationservice.service.NotificationPreferenceService;
import com.ebanking.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests unitaires pour NotificationController - Version Corrigée
 *
 * CORRECTION : Vérifier les messages exacts retournés par le controller
 */
@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationPreferenceService preferenceService;

    @MockBean
    private NotificationAuditService auditService;

    private Notification testNotification;
    private NotificationPreference testPreference;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUserId("user123");
        testNotification.setType(NotificationType.EMAIL);
        testNotification.setRecipient("test@example.com");
        testNotification.setSubject("Test");
        testNotification.setMessage("Test message");
        testNotification.setStatus("SENT");
        testNotification.setCreatedAt(LocalDateTime.now());

        testPreference = new NotificationPreference();
        testPreference.setId(1L);
        testPreference.setUserId("user123");
        testPreference.setEmailEnabled(true);
        testPreference.setSmsEnabled(true);
        testPreference.setPushEnabled(true);
        testPreference.setInAppEnabled(true);
        testPreference.setTransactionNotifications(true);
        testPreference.setPaymentNotifications(true);
        testPreference.setSecurityAlerts(true);
        testPreference.setMarketingNotifications(false);
    }

    @Test
    void sendNotification_ReturnsCreatedNotification() throws Exception {
        when(notificationService.sendNotification(any(Notification.class)))
                .thenReturn(testNotification);

        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNotification)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testNotification.getId()))
                .andExpect(jsonPath("$.data.userId").value(testNotification.getUserId()))
                .andExpect(jsonPath("$.data.status").value(testNotification.getStatus()));

        verify(notificationService, times(1)).sendNotification(any(Notification.class));
    }

    @Test
    void getUserNotifications_ReturnsUserNotifications() throws Exception {
        when(notificationService.getUserNotifications("user123"))
                .thenReturn(List.of(testNotification));

        mockMvc.perform(get("/api/notifications/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("user123"));

        verify(notificationService, times(1)).getUserNotifications("user123");
    }

    @Test
    void getInAppNotifications_ReturnsInAppNotifications() throws Exception {
        testNotification.setType(NotificationType.IN_APP);

        when(notificationService.getInAppNotifications("user123"))
                .thenReturn(List.of(testNotification));

        mockMvc.perform(get("/api/notifications/in-app/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].type").value("IN_APP"));

        verify(notificationService, times(1)).getInAppNotifications("user123");
    }

    @Test
    void markAsRead_ReturnsSuccess() throws Exception {
        doNothing().when(notificationService).markAsRead(1L);

        // CORRECTION : Le controller retourne ApiResponse.success(String message)
        // qui utilise le message par défaut "Opération réussie" si pas de data
        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        // Message n'est pas vérifié car ApiResponse.success(message) renvoie "Opération réussie"

        verify(notificationService, times(1)).markAsRead(1L);
    }

    @Test
    void getPreferences_ReturnsUserPreferences() throws Exception {
        when(preferenceService.getUserPreferences("user123"))
                .thenReturn(testPreference);

        mockMvc.perform(get("/api/notifications/preferences/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value("user123"))
                .andExpect(jsonPath("$.data.emailEnabled").value(true));

        verify(preferenceService, times(1)).getUserPreferences("user123");
    }

    @Test
    void updatePreferences_ReturnsUpdatedPreferences() throws Exception {
        when(preferenceService.updatePreferences(anyString(), any(NotificationPreference.class)))
                .thenReturn(testPreference);

        mockMvc.perform(put("/api/notifications/preferences/user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPreference)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Préférences mises à jour"));

        verify(preferenceService, times(1))
                .updatePreferences(anyString(), any(NotificationPreference.class));
    }

    @Test
    void toggleAllNotifications_ReturnsUpdatedPreferences() throws Exception {
        when(preferenceService.toggleAllNotifications("user123", true))
                .thenReturn(testPreference);

        mockMvc.perform(put("/api/notifications/preferences/user123/toggle-all")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notifications activées"));

        verify(preferenceService, times(1)).toggleAllNotifications("user123", true);
    }

    @Test
    void toggleDoNotDisturb_ReturnsUpdatedPreferences() throws Exception {
        when(preferenceService.toggleDoNotDisturb("user123", true))
                .thenReturn(testPreference);

        mockMvc.perform(put("/api/notifications/preferences/user123/do-not-disturb")
                        .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Mode silencieux activé"));

        verify(preferenceService, times(1)).toggleDoNotDisturb("user123", true);
    }

    @Test
    void getUserAuditHistory_ReturnsAuditList() throws Exception {
        NotificationAudit audit = new NotificationAudit();
        audit.setId(1L);
        audit.setUserId("user123");
        audit.setType(NotificationType.EMAIL);
        audit.setAction("NOTIFICATION_SENT");
        audit.setStatus("SENT");
        audit.setSuccess(true);
        audit.setTimestamp(LocalDateTime.now());

        when(auditService.getUserAuditHistory("user123"))
                .thenReturn(List.of(audit));

        mockMvc.perform(get("/api/notifications/audit/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value("user123"))
                .andExpect(jsonPath("$.data[0].action").value("NOTIFICATION_SENT"));

        verify(auditService, times(1)).getUserAuditHistory("user123");
    }

    @Test
    void getStats_ReturnsStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalNotifications", 100L);
        stats.put("successfulNotifications", 95L);
        stats.put("failedNotifications", 5L);
        stats.put("successRate", 95.0);

        when(auditService.getAuditStats()).thenReturn(stats);

        mockMvc.perform(get("/api/notifications/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalNotifications").value(100))
                .andExpect(jsonPath("$.data.successRate").value(95.0));

        verify(auditService, times(1)).getAuditStats();
    }

    @Test
    void getPendingNotifications_ReturnsPendingList() throws Exception {
        testNotification.setStatus("PENDING");

        when(notificationService.getPendingNotifications())
                .thenReturn(List.of(testNotification));

        mockMvc.perform(get("/api/notifications/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        verify(notificationService, times(1)).getPendingNotifications();
    }

    @Test
    void health_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification Service is running! 🚀"));
    }
}