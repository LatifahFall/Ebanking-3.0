import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { AgentAnalyticsService, AgentStats, RecentActivity, AgentAlert, PerformanceMetrics } from '../../core/services/agent-analytics.service';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../models';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';

/**
 * Agent Dashboard Component
 * Dedicated dashboard for agents with client statistics, activity, alerts, and performance
 */
@Component({
  selector: 'app-agent-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    PageHeaderComponent,
    ChartWidgetComponent,
    LoaderComponent
  ],
  templateUrl: './agent-dashboard.component.html',
  styleUrl: './agent-dashboard.component.scss'
})
export class AgentDashboardComponent implements OnInit {
  loading = true;
  agentId: string | null = null;
  
  stats: AgentStats | null = null;
  recentActivity: RecentActivity[] = [];
  alerts: AgentAlert[] = [];
  performance: PerformanceMetrics | null = null;
  performanceChart: ChartData | null = null;

  constructor(
    private agentAnalytics: AgentAnalyticsService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.role === UserRole.AGENT) {
      this.agentId = currentUser.id;
      this.loadDashboard();
    } else {
      this.showError('Access denied. Agent role required.');
      this.loading = false;
    }
  }

  loadDashboard(): void {
    if (!this.agentId) return;

    this.loading = true;

    // Load stats
    this.agentAnalytics.getAgentStats(this.agentId).subscribe({
      next: (stats) => {
        this.stats = stats;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading stats:', error);
        this.checkLoadingComplete();
      }
    });

    // Load recent activity
    this.agentAnalytics.getRecentActivity(this.agentId, 5).subscribe({
      next: (activity) => {
        this.recentActivity = activity;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading activity:', error);
        this.checkLoadingComplete();
      }
    });

    // Load alerts
    this.agentAnalytics.getAlerts(this.agentId).subscribe({
      next: (alerts) => {
        this.alerts = alerts.slice(0, 5); // Top 5 alerts
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading alerts:', error);
        this.checkLoadingComplete();
      }
    });

    // Load performance
    this.agentAnalytics.getPerformanceMetrics(this.agentId).subscribe({
      next: (performance) => {
        this.performance = performance;
        
        // Generate chart data
        this.performanceChart = {
          labels: performance.monthlyTrend.map(m => m.month),
          datasets: [
            {
              label: 'Total Clients',
              data: performance.monthlyTrend.map(m => m.clients),
              color: '#1E6AE1'
            },
            {
              label: 'Active Clients',
              data: performance.monthlyTrend.map(m => m.active),
              color: '#10B981'
            }
          ]
        };
        
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading performance:', error);
        this.checkLoadingComplete();
      }
    });
  }

  private checkLoadingComplete(): void {
    if (this.stats && this.performance) {
      this.loading = false;
    }
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'high': return '#EF4444';
      case 'medium': return '#F59E0B';
      case 'low': return '#64748B';
      default: return '#64748B';
    }
  }

  getAlertIcon(type: string): string {
    switch (type) {
      case 'kyc_pending': return 'pending_actions';
      case 'kyc_rejected': return 'cancel';
      case 'inactive_client': return 'block';
      case 'action_required': return 'warning';
      default: return 'info';
    }
  }

  formatTimeAgo(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - new Date(date).getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return `${days}d ago`;
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}

