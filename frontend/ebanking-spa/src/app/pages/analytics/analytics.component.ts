import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatInputModule } from '@angular/material/input';
import { FormsModule, ReactiveFormsModule, FormControl } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { map, startWith } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { ChartWidgetComponent, ChartData } from '../../shared/components/chart-widget/chart-widget.component';
import { AnalyticsBackendService } from '../../core/services/analytics-backend.service';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { User, UserRole } from '../../models';
import { 
  DashboardSummary, 
  CategoryBreakdown, 
  BalanceTrend, 
  Alert,
  AlertType,
  AlertSeverity,
  formatAlertType,
  getAlertSeverityColor
} from '../../models/analytics.model';

/**
 * Analytics Component
 * Displays financial analytics from Analytics Service backend
 */
@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatGridListModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatChipsModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    DatePipe,
    PageHeaderComponent,
    ChartWidgetComponent
  ],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.scss'
})
export class AnalyticsComponent implements OnInit {
  loading = true;
  dashboardSummary: DashboardSummary | null = null;
  balanceTrendChart: ChartData | null = null;
  categoryBreakdown: CategoryBreakdown[] = [];
  categoryChart: ChartData | null = null;
  recommendations: string[] = [];
  activeAlerts: Alert[] = [];
  
  userId: string | null = null;
  currentUser: User | null = null;
  isAgentOrAdmin = false;
  
  // Client selection for agents/admins
  clients: User[] = [];
  filteredClients$!: Observable<User[]>;
  clientSearchControl = new FormControl('');
  selectedClient: User | null = null;

  constructor(
    private analyticsBackend: AnalyticsBackendService,
    private authService: AuthService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    
    if (!this.currentUser) {
      this.userId = '3'; // Fallback to default user
      this.loadAnalytics();
      return;
    }

    // Check if user is agent or admin
    this.isAgentOrAdmin = this.currentUser.role === UserRole.AGENT || this.currentUser.role === UserRole.ADMIN;

    if (this.isAgentOrAdmin) {
      // Load clients for selection
      this.loadClients();
      
      // Setup filtered clients observable
      this.filteredClients$ = this.clientSearchControl.valueChanges.pipe(
        startWith(''),
        map(value => {
          const searchTerm = typeof value === 'string' ? value : '';
          return this.filterClients(searchTerm);
        })
      );
    } else {
      // Client: use their own ID
      this.userId = this.currentUser.id || '3';
      this.loadAnalytics();
    }
  }

  loadClients(): void {
    if (!this.currentUser) return;

    if (this.currentUser.role === UserRole.AGENT) {
      // Load clients assigned to this agent
      this.userService.getAgentClients(this.currentUser.id).subscribe({
        next: (clients) => {
          this.clients = clients;
          if (clients.length > 0) {
            // Auto-select first client
            this.selectedClient = clients[0];
            this.userId = clients[0].id;
            this.clientSearchControl.setValue(`${clients[0].firstName} ${clients[0].lastName}`);
            this.loadAnalytics();
          }
        },
        error: (error) => {
          console.error('Error loading clients:', error);
        }
      });
    } else if (this.currentUser.role === UserRole.ADMIN) {
      // Load all clients
      this.userService.getAllUsers().subscribe({
        next: (users) => {
          this.clients = users.filter(u => u.role === UserRole.CLIENT);
          if (this.clients.length > 0) {
            // Auto-select first client
            this.selectedClient = this.clients[0];
            this.userId = this.clients[0].id;
            this.clientSearchControl.setValue(`${this.clients[0].firstName} ${this.clients[0].lastName}`);
            this.loadAnalytics();
          }
        },
        error: (error) => {
          console.error('Error loading clients:', error);
        }
      });
    }
  }

  filterClients(searchTerm: string): User[] {
    if (!searchTerm) {
      return this.clients;
    }
    const term = searchTerm.toLowerCase();
    return this.clients.filter(client =>
      `${client.firstName} ${client.lastName}`.toLowerCase().includes(term) ||
      client.email.toLowerCase().includes(term)
    );
  }

  onClientSelected(client: User): void {
    this.selectedClient = client;
    this.userId = client.id;
    this.loadAnalytics();
  }

  displayClient(client: User | null): string {
    return client ? `${client.firstName} ${client.lastName}` : '';
  }

  loadAnalytics(): void {
    this.loading = true;
    if (!this.userId) return;

    // Load dashboard summary
    this.analyticsBackend.getDashboardSummary(this.userId).subscribe({
      next: (summary) => {
        this.dashboardSummary = summary;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading dashboard summary:', error);
        this.loading = false;
      }
    });

    // Load balance trend
    this.analyticsBackend.getBalanceTrend(this.userId, 30).subscribe({
      next: (trend) => {
        this.balanceTrendChart = this.convertBalanceTrendToChartData(trend);
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading balance trend:', error);
        this.checkLoadingComplete();
      }
    });

    // Load spending breakdown
    this.analyticsBackend.getSpendingBreakdown(this.userId, 'MONTH').subscribe({
      next: (breakdown) => {
        this.categoryBreakdown = breakdown;
        this.categoryChart = this.convertCategoryBreakdownToChartData(breakdown);
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading spending breakdown:', error);
        this.checkLoadingComplete();
      }
    });

    // Load recommendations
    this.analyticsBackend.getRecommendations(this.userId).subscribe({
      next: (recs) => {
        this.recommendations = recs;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading recommendations:', error);
        this.checkLoadingComplete();
      }
    });

    // Load active alerts
    this.analyticsBackend.getActiveAlerts(this.userId).subscribe({
      next: (alerts) => {
        this.activeAlerts = alerts;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading alerts:', error);
        this.checkLoadingComplete();
      }
    });
  }

  private checkLoadingComplete(): void {
    if (this.dashboardSummary && this.balanceTrendChart && this.categoryChart) {
      this.loading = false;
    }
  }

  private convertBalanceTrendToChartData(trend: BalanceTrend): ChartData {
    const labels = trend.dataPoints.map(point => {
      const date = new Date(point.timestamp);
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    });
    
    const data = trend.dataPoints.map(point => point.value);

    return {
      labels,
      datasets: [{
        label: 'Balance',
        data,
        color: '#1E6AE1'
      }]
    };
  }

  private convertCategoryBreakdownToChartData(breakdown: CategoryBreakdown[]): ChartData {
    return {
      labels: breakdown.map(c => c.category),
      datasets: [{
        label: 'Spending',
        data: breakdown.map(c => c.amount),
        color: '#EF4444'
      }]
    };
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  }

  formatAlertType(type: AlertType): string {
    return formatAlertType(type);
  }

  getAlertSeverityColor(severity: AlertSeverity): string {
    return getAlertSeverityColor(severity);
  }

  getCategoryColor(category: string): string {
    const colorMap: Record<string, string> = {
      'Food & Dining': '#F59E0B',
      'Transportation': '#3B82F6',
      'Shopping': '#EF4444',
      'Utilities': '#8B5CF6',
      'Entertainment': '#EC4899',
      'Healthcare': '#10B981',
      'Education': '#06B6D4',
      'Investment': '#6366F1'
    };
    return colorMap[category] || '#64748B';
  }

  onResolveAlert(alertId: string): void {
    this.analyticsBackend.resolveAlert(alertId).subscribe({
      next: () => {
        // Remove alert from list
        this.activeAlerts = this.activeAlerts.filter(a => a.alertId !== alertId);
      },
      error: (error) => {
        console.error('Error resolving alert:', error);
      }
    });
  }

  getGeneratedAtFormatted(): string {
    if (this.dashboardSummary?.generatedAt) {
      return new Date(this.dashboardSummary.generatedAt).toLocaleString('en-US', { 
        dateStyle: 'short', 
        timeStyle: 'short' 
      });
    }
    return 'now';
  }
}
