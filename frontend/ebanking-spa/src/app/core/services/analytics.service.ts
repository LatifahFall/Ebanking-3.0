import { Injectable } from '@angular/core';
import { Observable, combineLatest, map } from 'rxjs';
import { AccountService } from './account.service';
import { TransactionService } from './transaction.service';
import { Account, Transaction, TransactionType, TransactionCategory } from '../../models';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';

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
 * Aggregates data from AccountService and TransactionService to generate analytics
 */
@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {

  constructor(
    private accountService: AccountService,
    private transactionService: TransactionService
  ) {}

  /**
   * Get analytics summary
   */
  getSummary(): Observable<AnalyticsSummary> {
    return combineLatest([
      this.accountService.getAccounts(),
      this.transactionService.getRecentTransactions(1000) // Get more transactions for analytics
    ]).pipe(
      map(([accounts, transactions]) => {
        const totalBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);
        
        const completedTransactions = transactions.filter(t => t.status === 'COMPLETED');
        const totalIncome = completedTransactions
          .filter(t => t.amount > 0)
          .reduce((sum, t) => sum + t.amount, 0);
        
        const totalExpenses = Math.abs(completedTransactions
          .filter(t => t.amount < 0)
          .reduce((sum, t) => sum + t.amount, 0));
        
        const netIncome = totalIncome - totalExpenses;

        return {
          totalBalance,
          totalIncome,
          totalExpenses,
          netIncome,
          transactionCount: completedTransactions.length,
          accountsCount: accounts.length
        };
      })
    );
  }

  /**
   * Get income vs expenses chart data (last 6 months)
   */
  getIncomeExpensesChart(): Observable<ChartData> {
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
   */
  getBalanceEvolutionChart(): Observable<ChartData> {
    return combineLatest([
      this.accountService.getAccounts(),
      this.transactionService.getRecentTransactions(1000)
    ]).pipe(
      map(([accounts, transactions]) => {
        const now = new Date();
        const months: MonthlyData[] = [];
        const completedTransactions = transactions.filter(t => t.status === 'COMPLETED');

        // Calculate initial balance (6 months ago)
        const initialBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);
        let runningBalance = initialBalance;

        // Generate last 6 months
        for (let i = 5; i >= 0; i--) {
          const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
          const monthName = date.toLocaleDateString('en-US', { month: 'short' });
          
          const monthTransactions = completedTransactions.filter(t => {
            const tDate = new Date(t.date);
            return tDate.getMonth() === date.getMonth() && 
                   tDate.getFullYear() === date.getFullYear();
          });

          const monthNet = monthTransactions.reduce((sum, t) => sum + t.amount, 0);
          runningBalance += monthNet;

          months.push({
            month: monthName,
            income: 0,
            expenses: 0,
            balance: runningBalance
          });
        }

        return {
          labels: months.map(m => m.month),
          datasets: [
            {
              label: 'Balance',
              data: months.map(m => m.balance),
              color: '#1E6AE1'
            }
          ]
        };
      })
    );
  }

  /**
   * Get spending by category
   */
  getCategorySpending(): Observable<CategorySpending[]> {
    return this.transactionService.getRecentTransactions(1000).pipe(
      map((transactions) => {
        const completedExpenses = transactions
          .filter(t => t.status === 'COMPLETED' && t.amount < 0)
          .filter(t => t.category !== TransactionCategory.TRANSFER); // Exclude transfers

        const categoryMap = new Map<TransactionCategory, number>();
        
        completedExpenses.forEach(t => {
          const current = categoryMap.get(t.category) || 0;
          categoryMap.set(t.category, current + Math.abs(t.amount));
        });

        const total = Array.from(categoryMap.values()).reduce((sum, val) => sum + val, 0);

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

        const result: CategorySpending[] = Array.from(categoryMap.entries())
          .map(([category, amount]) => ({
            category,
            amount,
            percentage: total > 0 ? (amount / total) * 100 : 0,
            color: categoryColors[category] || '#64748B'
          }))
          .sort((a, b) => b.amount - a.amount)
          .slice(0, 6); // Top 6 categories

        return result;
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

