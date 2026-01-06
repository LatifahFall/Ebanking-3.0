import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminAnalyticsService, SystemStats, SystemAlert, ServiceHealth, ApiPerformance } from '../../core/services/admin-analytics.service';
import { AnalyticsBackendService } from '../../core/services/analytics-backend.service';
import { UserService } from '../../core/services/user.service';
import { UserRole } from '../../models';
import { AdminOverview } from '../../models/analytics.model';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ClientFormDialogComponent, ClientFormData } from '../../shared/components/client-form-dialog/client-form-dialog.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    ChartWidgetComponent,
    PageHeaderComponent,
    LoaderComponent
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  systemStats: SystemStats | null = null;
  adminOverview: AdminOverview | null = null;
  userGrowthChart: ChartData | null = null;
  revenueChart: ChartData | null = null;
  userDistributionChart: ChartData | null = null;
  alerts: SystemAlert[] = [];
  serviceHealth: ServiceHealth[] = [];
  apiPerformance: ApiPerformance[] = [];
  
  loading = true;
  selectedTab = 0;

  get activeAlerts(): SystemAlert[] {
    return this.alerts.filter(a => !a.resolved);
  }

  get activeAlertsCount(): number {
    return this.activeAlerts.length;
  }

  // Table columns
  alertsColumns: string[] = ['type', 'title', 'severity', 'timestamp', 'actions'];
  healthColumns: string[] = ['name', 'status', 'responseTime', 'lastCheck'];
  performanceColumns: string[] = ['endpoint', 'method', 'avgResponseTime', 'requestCount', 'errorRate'];

  constructor(
    private adminAnalytics: AdminAnalyticsService,
    private analyticsBackend: AnalyticsBackendService,
    private userService: UserService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Load admin overview from Analytics Service backend
    this.analyticsBackend.getAdminOverview().subscribe({
      next: (overview) => {
        this.adminOverview = overview;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading admin overview:', error);
        this.checkLoadingComplete();
      }
    });

    // Load system stats (combines backend overview with local data)
    this.adminAnalytics.getSystemStats().subscribe({
      next: (stats) => {
        this.systemStats = stats;
        this.checkLoadingComplete();
      },
      error: () => {
        this.snackBar.open('Failed to load system statistics', 'Close', { duration: 3000 });
        this.checkLoadingComplete();
      }
    });

    // Load charts
    this.adminAnalytics.getUserGrowthChart().subscribe({
      next: (chart) => {
        this.userGrowthChart = chart;
        this.checkLoadingComplete();
      }
    });

    this.adminAnalytics.getRevenueChart().subscribe({
      next: (chart) => {
        this.revenueChart = chart;
        this.checkLoadingComplete();
      }
    });

    this.adminAnalytics.getUserDistributionChart().subscribe({
      next: (chart) => {
        this.userDistributionChart = chart;
        this.checkLoadingComplete();
      }
    });

    // Load alerts
    this.adminAnalytics.getSystemAlerts().subscribe({
      next: (alerts) => {
        this.alerts = alerts;
        this.checkLoadingComplete();
      }
    });

    // Load service health
    this.adminAnalytics.getServiceHealth().subscribe({
      next: (health) => {
        this.serviceHealth = health;
        this.checkLoadingComplete();
      }
    });

    // Load API performance
    this.adminAnalytics.getApiPerformance().subscribe({
      next: (performance) => {
        this.apiPerformance = performance;
        this.checkLoadingComplete();
      }
    });
  }

  private checkLoadingComplete(): void {
    // Simple check - in production, use a more sophisticated loading state management
    if (this.systemStats && this.userGrowthChart && this.revenueChart && this.userDistributionChart) {
      this.loading = false;
    }
  }

  onCreateUser(): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '500px',
      data: { title: 'Create New User', client: null }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result && result.email) {
        this.userService.createUser({
          email: result.email,
          firstName: result.firstName,
          lastName: result.lastName,
          phoneNumber: result.phoneNumber,
          role: UserRole.CLIENT // Default to CLIENT for new users
        }).subscribe({
          next: () => {
            this.snackBar.open('User created successfully!', 'Close', { duration: 3000 });
            this.loadDashboardData();
          },
          error: (error) => {
            this.snackBar.open(`Failed to create user: ${error.message || 'Unknown error'}`, 'Close', { duration: 5000 });
          }
        });
      }
    });
  }

  onResolveAlert(alertId: string): void {
    const alert = this.alerts.find(a => a.id === alertId);
    if (alert) {
      alert.resolved = true;
      this.snackBar.open('Alert resolved', 'Close', { duration: 2000 });
    }
  }

  getAlertTypeIcon(type: string): string {
    switch (type) {
      case 'error': return 'error';
      case 'warning': return 'warning';
      case 'info': return 'info';
      case 'fraud': return 'security';
      case 'maintenance': return 'build';
      default: return 'notifications';
    }
  }

  getAlertTypeColor(type: string): string {
    switch (type) {
      case 'error': return 'warn';
      case 'warning': return 'accent';
      case 'info': return 'primary';
      case 'fraud': return 'warn';
      case 'maintenance': return 'primary';
      default: return '';
    }
  }

  getSeverityColor(severity: string): string {
    switch (severity) {
      case 'critical': return 'warn';
      case 'high': return 'warn';
      case 'medium': return 'accent';
      case 'low': return 'primary';
      default: return '';
    }
  }

  getServiceStatusColor(status: string): string {
    switch (status) {
      case 'UP': return 'primary';
      case 'DEGRADED': return 'accent';
      case 'DOWN': return 'warn';
      default: return '';
    }
  }
}
