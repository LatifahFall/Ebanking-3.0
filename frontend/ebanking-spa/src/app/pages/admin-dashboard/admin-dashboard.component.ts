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
import { AdminAnalyticsService } from '../../core/services/admin-analytics.service';
import { AnalyticsBackendService } from '../../core/services/analytics-backend.service';
import { UserService } from '../../core/services/user.service';
import { UserRole } from '../../models';
import { AdminOverview } from '../../models/analytics.model'; // Import raccourci possible
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ClientFormDialogComponent } from '../../shared/components/client-form-dialog/client-form-dialog.component';
import { SystemHealth, UserGrowthData, RoleDistribution } from '../../core/services/admin-analytics.service';

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
  systemHealth: SystemHealth | null = null;
  adminOverview: AdminOverview | null = null;
  userGrowthData: UserGrowthData | null = null;
  roleDistribution: RoleDistribution[] = [];

  // Propriétés attendues par le template
  systemStats: any = null;
  activeAlertsCount: number = 0;
  userGrowthChart: any = null;
  revenueChart: any = null;
  userDistributionChart: any = null;
  activeAlerts: any[] = [];
  alertsColumns: string[] = ['type', 'message', 'status', 'actions'];
  serviceHealth: any[] = [];
  apiPerformance: any[] = [];

  loading = true;
  selectedTab = 0;

  // Table columns
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
    // Admin overview
    this.analyticsBackend.getAdminOverview().subscribe({
      next: (overview: AdminOverview) => {
        this.adminOverview = overview;
        this.systemStats = overview; // mapping simple
        this.activeAlertsCount = overview?.activeAlerts?.length || 0;
        this.activeAlerts = overview?.activeAlerts || [];
        this.serviceHealth = overview?.serviceHealth || [];
        this.apiPerformance = overview?.apiPerformance || [];
        this.checkLoadingComplete();
      },
      error: (error: any) => {
        console.error('Error loading admin overview:', error);
        this.checkLoadingComplete();
      }
    });
    // User growth
    this.adminAnalytics.getUserGrowthData().subscribe({
      next: (data: UserGrowthData) => {
        this.userGrowthData = data;
        this.userGrowthChart = data;
        this.checkLoadingComplete();
      }
    });
    // Role distribution
    this.adminAnalytics.getRoleDistribution().subscribe({
      next: (data: RoleDistribution[]) => {
        this.roleDistribution = data;
        this.userDistributionChart = data;
        this.checkLoadingComplete();
      }
    });
    // System health (remplace alerts)
    this.adminAnalytics.getSystemHealth().subscribe({
      next: (health: SystemHealth | null) => {
        this.systemHealth = health;
        this.checkLoadingComplete();
      }
    });
    // Revenue chart (exemple, à adapter selon vos données)
    // this.revenueChart = ...
  }

  private checkLoadingComplete(): void {
    // Simple check - in production, use a more sophisticated loading state management
    this.loading = false;
  }

  onCreateUser(): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '500px',
      data: { title: 'Create New User', client: undefined }
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
    // À adapter selon la structure réelle de vos alertes système
    this.snackBar.open('Alert resolved', 'Close', { duration: 2000 });
    // alertId n'est pas utilisé, mais conservé pour compatibilité
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
