// ========================================
// AuditControllerTest.java
// ========================================
package com.banking.audit.controller;

import com.banking.audit.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.banking.audit.service.AuditService;
import com.banking.audit.service.AuditExportService;
import com.banking.audit.model.AuditEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.TestingAuthenticationToken;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuditControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuditService auditService;

    @MockBean
    private AuditExportService exportService;

    @BeforeEach
    void setUp() {
        // Setup test data
    }

    private AuditEvent sampleEvent() {
        return AuditEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(AuditEvent.EventType.USER_LOGIN)
                .userId(123L)
                .action("LOGIN_SUCCESS")
                .serviceSource("test-service")
                .timestamp(LocalDateTime.now())
                .result(AuditEvent.AuditResult.SUCCESS)
                .build();
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/audit/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get all events with ADMIN role")
    void shouldGetAllEventsWithAdminRole() throws Exception {
        AuditEvent ev = sampleEvent();
        given(auditService.findAllEvents(any())).willReturn(new PageImpl<>(List.of(ev), PageRequest.of(0,10), 1));

        mockMvc.perform(get("/audit/events")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should search events with filters")
    void shouldSearchEventsWithFilters() throws Exception {
        AuditEvent ev = sampleEvent();
        given(auditService.searchEvents(eq(123L), any(), any(), any(), any())).willReturn(new PageImpl<>(List.of(ev), PageRequest.of(0,10),1));

        mockMvc.perform(get("/audit/events/search")
                        .param("userId", "123")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get user timeline")
    void shouldGetUserTimeline() throws Exception {
        AuditEvent ev = sampleEvent();
        given(auditService.findUserTimeline(123L)).willReturn(List.of(ev));

        mockMvc.perform(get("/audit/users/123/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get compliance report")
    void shouldGetComplianceReport() throws Exception {
        // Préparer mock pour findEventsByDateRange utilisé par le contrôleur
        AuditEvent ev = sampleEvent();
        given(auditService.findEventsByDateRange(any(), any(), any())).willReturn(new PageImpl<>(List.of(ev), PageRequest.of(0,10),1));

        mockMvc.perform(get("/audit/compliance/report")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59")
                        .param("regulation", "RGPD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regulation").value("RGPD"))
                .andExpect(jsonPath("$.totalEvents").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create audit event")
    void shouldCreateAuditEvent() throws Exception {
        AuditEvent in = AuditEvent.builder().userId(123L).eventType(AuditEvent.EventType.USER_LOGIN).action("LOGIN").build();
        AuditEvent out = sampleEvent();
        given(auditService.saveAuditEvent(any())).willReturn(out);

        mockMvc.perform(post("/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(in)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get event by id")
    void shouldGetEventById() throws Exception {
        AuditEvent ev = sampleEvent();
        UUID id = ev.getEventId();
        given(auditService.findEventById(id)).willReturn(Optional.of(ev));

        mockMvc.perform(get("/audit/events/" + id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value(id.toString()));
    }

    @Test
    @DisplayName("Should export audit report")
    @WithMockUser(roles = "ADMIN")
    void shouldExportAuditReport() throws Exception {
        given(exportService.initiateExport(any(), any(), any())).willReturn("job-123");

        mockMvc.perform(post("/audit/export")
                        .param("startDate", "2026-01-01T00:00:00")
                        .param("endDate", "2026-12-31T23:59:59")
                        .param("format", "PDF"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should anonymize user data")
    void shouldAnonymizeUserData() throws Exception {
        mockMvc.perform(post("/audit/users/123/anonymize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should get my events when principal is Long")
    void shouldGetMyEventsWithPrincipalLong() throws Exception {
        Long currentUserId = 999L;

        // Création manuelle d'un Authentication avec principal = Long
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(currentUserId);
        when(authentication.isAuthenticated()).thenReturn(true);

        // Mock du service
        Page<AuditEvent> mockPage = new PageImpl<>(
                List.of(sampleEvent()),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "timestamp")),
                1
        );
        given(auditService.findEventsByUserId(eq(currentUserId), any(Pageable.class)))
                .willReturn(mockPage);

        // Exécution de la requête avec l'authentication mockée
        mockMvc.perform(get("/api/v1/audit/events")
                        .param("page", "0")
                        .param("size", "20")
                        .with(authentication(authentication)))  // ← Clé : injecte le mock
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should verify event integrity")
    void shouldVerifyEventIntegrity() throws Exception {
        AuditEvent ev = sampleEvent();
        UUID id = ev.getEventId();
        given(auditService.findEventById(id)).willReturn(Optional.of(ev));
        given(auditService.verifyIntegrity(ev)).willReturn(true);

        mockMvc.perform(post("/audit/verify/" + id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should get user activity summary")
    void shouldGetUserActivitySummary() throws Exception {
        given(auditService.getUserActivitySummary(eq(123L), any())).willReturn(Map.of("total", 5));

        mockMvc.perform(get("/audit/users/123/activity-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5));
    }

}
