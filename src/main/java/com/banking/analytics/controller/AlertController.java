// ========== AlertController.java ==========
package com.banking.analytics.controller;

import com.banking.analytics.model.Alert;
import com.banking.analytics.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/analytics/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @GetMapping("/active")
    @PreAuthorize("#userId == authentication.principal.claims['sub'] or hasRole('ADMIN')")
    public ResponseEntity<List<Alert>> getActiveAlerts(@RequestParam String userId) {
        List<Alert> alerts = alertService.getActiveAlerts(userId);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/{alertId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> resolveAlert(@PathVariable String alertId) {
        alertService.resolveAlert(alertId);
        return ResponseEntity.ok().build();
    }
}

