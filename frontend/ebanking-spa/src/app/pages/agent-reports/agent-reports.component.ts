import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AgentAnalyticsService } from '../../core/services/agent-analytics.service';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';

export interface MonthlyReport {
  month: string;
  newClients: number;
  activeClients: number;
  kycCompleted: number;
  kycPending: number;
  totalClients: number;
}

@Component({
  selector: 'app-agent-reports',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatSnackBarModule,
    PageHeaderComponent,
    ChartWidgetComponent,
    LoaderComponent
  ],
  templateUrl: './agent-reports.component.html',
  styleUrl: './agent-reports.component.scss'
})
export class AgentReportsComponent implements OnInit {
  loading = false;
  currentAgentId: string | null = null;
  selectedMonth: Date = new Date();
  monthlyReport: MonthlyReport | null = null;
  conversionStats: any = null;
  reportChart: ChartData | null = null;
  
  displayedColumns: string[] = ['month', 'newClients', 'activeClients', 'kycCompleted', 'kycPending', 'totalClients'];

  constructor(
    private agentAnalytics: AgentAnalyticsService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.role === UserRole.AGENT) {
      this.currentAgentId = currentUser.id;
      this.loadReport();
    }
  }

  loadReport(): void {
    if (!this.currentAgentId) return;
    
    this.loading = true;
    
    // Load performance metrics
    this.agentAnalytics.getPerformanceMetrics(this.currentAgentId).subscribe({
      next: (metrics) => {
        this.generateMonthlyReport(metrics);
        this.calculateConversionStats(metrics);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.showError('Failed to load report data.');
      }
    });
  }

  generateMonthlyReport(metrics: any): void {
    const month = this.selectedMonth.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    const monthData = metrics.monthlyTrend[metrics.monthlyTrend.length - 1] || {
      month: month,
      clients: 0,
      active: 0
    };

    this.monthlyReport = {
      month: month,
      newClients: monthData.clients,
      activeClients: monthData.active,
      kycCompleted: Math.round(monthData.clients * (metrics.kycCompletionRate / 100)),
      kycPending: Math.round(monthData.clients * ((100 - metrics.kycCompletionRate) / 100)),
      totalClients: monthData.clients
    };

    // Generate chart
    this.reportChart = {
      labels: metrics.monthlyTrend.map((m: any) => m.month),
      datasets: [
        {
          label: 'New Clients',
          data: metrics.monthlyTrend.map((m: any) => m.clients),
          color: '#1E6AE1'
        },
        {
          label: 'Active Clients',
          data: metrics.monthlyTrend.map((m: any) => m.active),
          color: '#10B981'
        }
      ]
    };
  }

  calculateConversionStats(metrics: any): void {
    this.conversionStats = {
      clientGrowth: metrics.clientGrowth,
      activeClientRate: metrics.activeClientRate,
      kycCompletionRate: metrics.kycCompletionRate,
      totalClients: metrics.monthlyTrend.reduce((sum: number, m: any) => sum + m.clients, 0)
    };
  }

  onExportReport(): void {
    if (!this.monthlyReport) return;
    
    const data = {
      month: this.monthlyReport.month,
      report: this.monthlyReport,
      stats: this.conversionStats,
      generatedAt: new Date().toISOString()
    };
    
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `agent-report-${this.selectedMonth.toISOString().split('T')[0]}.json`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    this.showSuccess('Report exported successfully.');
  }

  onExportCSV(): void {
    if (!this.monthlyReport) return;
    
    const csv = [
      ['Month', 'New Clients', 'Active Clients', 'KYC Completed', 'KYC Pending', 'Total Clients'],
      [
        this.monthlyReport.month,
        this.monthlyReport.newClients.toString(),
        this.monthlyReport.activeClients.toString(),
        this.monthlyReport.kycCompleted.toString(),
        this.monthlyReport.kycPending.toString(),
        this.monthlyReport.totalClients.toString()
      ]
    ].map(row => row.join(',')).join('\n');
    
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `agent-report-${this.selectedMonth.toISOString().split('T')[0]}.csv`;
    link.click();
    window.URL.revokeObjectURL(url);
    
    this.showSuccess('Report exported as CSV.');
  }

  onMonthChange(): void {
    this.loadReport();
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', { duration: 3000 });
  }
}

