import { Injectable } from '@angular/core';
import { Observable, combineLatest, map, switchMap } from 'rxjs';
import { AccountService } from './account.service';
import { TransactionService } from './transaction.service';
import { AnalyticsBackendService } from './analytics-backend.service';
import { AuthService } from './auth.service';
import { Account, Transaction, TransactionType, TransactionCategory } from '../../models';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';
import { DashboardSummary, CategoryBreakdown, BalanceTrend } from '../../models/analytics.model';

export interface AnalyticsSummary {
  totalBalance: number;
  totalIncome: number;
  totalExpenses: number;
  netIncome: number;
  transactionCount: number;
  accountsCount: number;
}

export interface CategorySpending {
  category: TransactionCategory;
  amount: number;
  percentage: number;
  color: string;
}

export interface MonthlyData {
  month: string;
  income: number;
  expenses: number;
  balance: number;
}

/**
 * Analytics Service
 * 
 * Refactored to use AnalyticsBackendService while maintaining backward compatibility
 * with existing components. Acts as an adapter layer between components and backend service.
 */
@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {

  constructor(
    private accountService: AccountService,
    private transactionService: TransactionService,
    private analyticsBackend: AnalyticsBackendService,
    private authService: AuthService
  ) {}

  /**
   * Get analytics summary
   * Uses AnalyticsBackendService.getDashboardSummary() and adapts to AnalyticsSummary interface
   */
  getSummary(): Observable<AnalyticsSummary> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || '3'; // Fallback to default user

    return combineLatest([
      this.analyticsBackend.getDashboardSummary(userId),
      this.accountService.getAccounts()
    ]).pipe(
      map(([dashboardSummary, accounts]) => {
        // Adapt DashboardSummary to AnalyticsSummary
        return {
          totalBalance: dashboardSummary.currentBalance,
          totalIncome: dashboardSummary.monthlyIncome,
          totalExpenses: dashboardSummary.monthlySpending,
          netIncome: dashboardSummary.monthlyIncome - dashboardSummary.monthlySpending,
          transactionCount: dashboardSummary.transactionsThisMonth,
          accountsCount: accounts.length
        };
      })
    );
  }

  /**
   * Get income vs expenses chart data (last 6 months)
   * Uses transaction service for historical data (backend doesn't provide monthly breakdown)
   */
  getIncomeExpensesChart(): Observable<ChartData> {
    // Keep existing implementation as backend doesn't provide monthly income/expenses breakdown
    // This could be enhanced later to use balance trend data if available
    return this.transactionService.getRecentTransactions(1000).pipe(
      map((transactions) => {
        const completedTransactions = transactions.filter(t => t.status === 'COMPLETED');
        const now = new Date();
        const months: MonthlyData[] = [];

        // Generate last 6 months
        for (let i = 5; i >= 0; i--) {
          const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
          const monthName = date.toLocaleDateString('en-US', { month: 'short' });
          
          const monthTransactions = completedTransactions.filter(t => {
            const tDate = new Date(t.date);
            return tDate.getMonth() === date.getMonth() && 
                   tDate.getFullYear() === date.getFullYear();
          });

          const income = monthTransactions
            .filter(t => t.amount > 0)
            .reduce((sum, t) => sum + t.amount, 0);
          
          const expenses = Math.abs(monthTransactions
            .filter(t => t.amount < 0)
            .reduce((sum, t) => sum + t.amount, 0));

          months.push({
            month: monthName,
            income,
            expenses,
            balance: income - expenses
          });
        }

        return {
          labels: months.map(m => m.month),
          datasets: [
            {
              label: 'Income',
              data: months.map(m => m.income),
              color: '#10B981'
            },
            {
              label: 'Expenses',
              data: months.map(m => m.expenses),
              color: '#EF4444'
            }
          ]
        };
      })
    );
  }

  /**
   * Get balance evolution chart data
   * Uses AnalyticsBackendService.getBalanceTrend() and adapts to ChartData format
   */
  getBalanceEvolutionChart(): Observable<ChartData> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || '3'; // Fallback to default user

    return this.analyticsBackend.getBalanceTrend(userId, 180).pipe( // 6 months = ~180 days
      map((balanceTrend) => {
        // Convert DataPoint[] to monthly aggregation for chart
        const dataPoints = balanceTrend.dataPoints;
        if (dataPoints.length === 0) {
          return {
            labels: [],
            datasets: [{ label: 'Balance', data: [], color: '#1E6AE1' }]
          };
        }

        // Group by month
        const monthlyMap = new Map<string, number[]>();
        dataPoints.forEach(point => {
          const date = new Date(point.timestamp);
          const monthKey = date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
          if (!monthlyMap.has(monthKey)) {
            monthlyMap.set(monthKey, []);
          }
          monthlyMap.get(monthKey)!.push(point.value);
        });

        // Calculate average balance per month
        const months: string[] = [];
        const balances: number[] = [];
        monthlyMap.forEach((values, month) => {
          months.push(month);
          const avgBalance = values.reduce((sum, val) => sum + val, 0) / values.length;
          balances.push(Math.round(avgBalance * 100) / 100);
        });

        return {
          labels: months,
          datasets: [
            {
              label: 'Balance',
              data: balances,
              color: '#1E6AE1'
            }
          ]
        };
      })
    );
  }

  /**
   * Get spending by category
   * Uses AnalyticsBackendService.getSpendingBreakdown() and adapts to CategorySpending format
   */
  getCategorySpending(): Observable<CategorySpending[]> {
    const currentUser = this.authService.getCurrentUser();
    const userId = currentUser?.id || '3'; // Fallback to default user

    return this.analyticsBackend.getSpendingBreakdown(userId, 'MONTH').pipe(
      map((breakdown: CategoryBreakdown[]) => {
        // Map backend category names to TransactionCategory enum
        const categoryMapping: Record<string, TransactionCategory> = {
          'Food & Dining': TransactionCategory.FOOD,
          'Transportation': TransactionCategory.TRANSPORT,
          'Shopping': TransactionCategory.SHOPPING,
          'Utilities': TransactionCategory.UTILITIES,
          'Entertainment': TransactionCategory.ENTERTAINMENT,
          'Healthcare': TransactionCategory.HEALTHCARE,
          'Education': TransactionCategory.EDUCATION,
          'Investment': TransactionCategory.INVESTMENT
        };

        const categoryColors: Record<TransactionCategory, string> = {
          [TransactionCategory.SHOPPING]: '#EF4444',
          [TransactionCategory.FOOD]: '#F59E0B',
          [TransactionCategory.TRANSPORT]: '#3B82F6',
          [TransactionCategory.UTILITIES]: '#8B5CF6',
          [TransactionCategory.ENTERTAINMENT]: '#EC4899',
          [TransactionCategory.HEALTHCARE]: '#10B981',
          [TransactionCategory.EDUCATION]: '#06B6D4',
          [TransactionCategory.INVESTMENT]: '#6366F1',
          [TransactionCategory.SALARY]: '#10B981',
          [TransactionCategory.TRANSFER]: '#94A3B8',
          [TransactionCategory.OTHER]: '#64748B'
        };

        return breakdown.map(item => ({
          category: categoryMapping[item.category] || TransactionCategory.OTHER,
          amount: item.amount,
          percentage: item.percentage,
          color: categoryColors[categoryMapping[item.category] || TransactionCategory.OTHER] || '#64748B'
        })).slice(0, 6); // Top 6 categories
      })
    );
  }

  /**
   * Get category spending chart data
   */
  getCategorySpendingChart(): Observable<ChartData> {
    return this.getCategorySpending().pipe(
      map((categories) => {
        return {
          labels: categories.map(c => this.formatCategoryName(c.category)),
          datasets: [
            {
              label: 'Spending',
              data: categories.map(c => c.amount),
              color: '#EF4444'
            }
          ]
        };
      })
    );
  }

  /**
   * Format category name for display
   */
  private formatCategoryName(category: TransactionCategory): string {
    return category.charAt(0) + category.slice(1).toLowerCase().replace('_', ' ');
  }
}

