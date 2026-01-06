import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { FormsModule } from '@angular/forms';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { AnalyticsBackendService } from '../../core/services/analytics-backend.service';
import { AuthService } from '../../core/services/auth.service';
import { 
  Alert, 
  AlertType, 
  AlertSeverity, 
  AlertStatus,
  formatAlertType,
  getAlertSeverityColor
} from '../../models/analytics.model';

@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    FormsModule,
    PageHeaderComponent
  ],
  templateUrl: './alerts.component.html',
  styleUrl: './alerts.component.scss'
})
export class AlertsComponent implements OnInit {
  loading = true;
  allAlerts: Alert[] = [];
  filteredAlerts: Alert[] = [];
  
  selectedType: AlertType | 'ALL' = 'ALL';
  selectedSeverity: AlertSeverity | 'ALL' = 'ALL';
  
  alertTypes = Object.values(AlertType);
  severities = Object.values(AlertSeverity);
  
  userId: string | null = null;

  constructor(
    private analyticsBackend: AnalyticsBackendService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    this.userId = currentUser?.id || '3';
    this.loadAlerts();
  }

  loadAlerts(): void {
    if (!this.userId) return;
    
    this.loading = true;
    this.analyticsBackend.getActiveAlerts(this.userId).subscribe({
      next: (alerts) => {
        this.allAlerts = alerts;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading alerts:', error);
        this.loading = false;
      }
    });
  }

  applyFilters(): void {
    this.filteredAlerts = this.allAlerts.filter(alert => {
      const typeMatch = this.selectedType === 'ALL' || alert.alertType === this.selectedType;
      const severityMatch = this.selectedSeverity === 'ALL' || alert.severity === this.selectedSeverity;
      return typeMatch && severityMatch;
    });
  }

  onTypeFilterChange(): void {
    this.applyFilters();
  }

  onSeverityFilterChange(): void {
    this.applyFilters();
  }

  onResolveAlert(alertId: string): void {
    this.analyticsBackend.resolveAlert(alertId).subscribe({
      next: () => {
        this.allAlerts = this.allAlerts.filter(a => a.alertId !== alertId);
        this.applyFilters();
      },
      error: (error) => {
        console.error('Error resolving alert:', error);
      }
    });
  }

  formatAlertType(type: AlertType): string {
    return formatAlertType(type);
  }

  getAlertSeverityColor(severity: AlertSeverity): string {
    return getAlertSeverityColor(severity);
  }

  getAlertIcon(type: AlertType): string {
    const iconMap: Record<AlertType, string> = {
      [AlertType.SPENDING_THRESHOLD]: 'account_balance_wallet',
      [AlertType.LOW_BALANCE]: 'warning',
      [AlertType.UNUSUAL_ACTIVITY]: 'security',
      [AlertType.BUDGET_EXCEEDED]: 'money_off',
      [AlertType.LARGE_TRANSACTION]: 'payments',
      [AlertType.FREQUENT_TRANSACTIONS]: 'repeat',
      [AlertType.SUSPICIOUS_LOGIN]: 'lock'
    };
    return iconMap[type] || 'notifications';
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  }
}

