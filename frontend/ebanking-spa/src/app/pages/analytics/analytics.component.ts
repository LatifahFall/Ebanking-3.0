import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { AnalyticsService, AnalyticsSummary, CategorySpending } from '../../core/services/analytics.service';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';

/**
 * Analytics Component
 * Displays financial analytics based on accounts and transactions
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
    PageHeaderComponent,
    ChartWidgetComponent
  ],
  templateUrl: './analytics.component.html',
  styleUrl: './analytics.component.scss'
})
export class AnalyticsComponent implements OnInit {
  loading = true;
  summary: AnalyticsSummary | null = null;
  incomeExpensesChart: ChartData | null = null;
  balanceEvolutionChart: ChartData | null = null;
  categorySpending: CategorySpending[] = [];
  categoryChart: ChartData | null = null;

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadAnalytics();
  }

  loadAnalytics(): void {
    this.loading = true;

    // Load summary
    this.analyticsService.getSummary().subscribe({
      next: (summary) => {
        this.summary = summary;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading summary:', error);
        this.loading = false;
      }
    });

    // Load income vs expenses chart
    this.analyticsService.getIncomeExpensesChart().subscribe({
      next: (data) => {
        this.incomeExpensesChart = data;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading income/expenses chart:', error);
        this.checkLoadingComplete();
      }
    });

    // Load balance evolution chart
    this.analyticsService.getBalanceEvolutionChart().subscribe({
      next: (data) => {
        this.balanceEvolutionChart = data;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading balance evolution chart:', error);
        this.checkLoadingComplete();
      }
    });

    // Load category spending
    this.analyticsService.getCategorySpending().subscribe({
      next: (categories) => {
        this.categorySpending = categories;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading category spending:', error);
        this.checkLoadingComplete();
      }
    });

    // Load category chart
    this.analyticsService.getCategorySpendingChart().subscribe({
      next: (data) => {
        this.categoryChart = data;
        this.checkLoadingComplete();
      },
      error: (error) => {
        console.error('Error loading category chart:', error);
        this.checkLoadingComplete();
      }
    });
  }

  private checkLoadingComplete(): void {
    // Simple check - in production, use a counter or RxJS combineLatest
    if (this.summary && this.incomeExpensesChart && this.balanceEvolutionChart) {
      this.loading = false;
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: 2
    }).format(amount);
  }

  formatCategoryName(category: string): string {
    return category.charAt(0) + category.slice(1).toLowerCase().replace('_', ' ');
  }
}
