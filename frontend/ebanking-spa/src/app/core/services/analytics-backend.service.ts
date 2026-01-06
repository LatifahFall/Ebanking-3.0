import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, of, delay, catchError, combineLatest, map, retry, throwError, timer } from 'rxjs';
import {
  DashboardSummary,
  CategoryBreakdown,
  BalanceTrend,
  AdminOverview,
  Alert,
  AlertType,
  AlertSeverity,
  AlertStatus,
  DataPoint,
  RecentTransaction,
  SpendingPeriod
} from '../../models/analytics.model';
import { AccountService } from './account.service';
import { TransactionService } from './transaction.service';
import { TransactionCategory, TransactionStatus } from '../../models';
import { environment } from '../../../environments/environment';

/**
 * Analytics Backend Service
 *
 * This service mirrors the backend Analytics Service endpoints:
 * - Base URL: /api/v1/analytics
 * - Port: 8087 (configured via environment)
 *
 * Currently uses MOCK data, but structured to easily switch to real HTTP calls.
 *
 * Backend Endpoints:
 * - GET /api/v1/analytics/dashboard/summary?userId={userId}
 * - GET /api/v1/analytics/spending/breakdown?userId={userId}&period={MONTH|WEEK}
 * - GET /api/v1/analytics/trends/balance?userId={userId}&days={30}
 * - GET /api/v1/analytics/insights/recommendations?userId={userId}
 * - GET /api/v1/analytics/admin/overview
 * - GET /api/v1/analytics/alerts/active?userId={userId}
 * - POST /api/v1/analytics/alerts/{alertId}/resolve
 */
@Injectable({
  providedIn: 'root'
})
export class AnalyticsBackendService {
  /**
   * Flag to control whether to use mock data or real HTTP calls
   * Set from environment configuration
   */
  private readonly useMock: boolean = environment.useMock;

  /**
   * Mock alerts storage (simulating database)
   */
  private mockAlerts: Alert[] = [];

  constructor(
    private http: HttpClient,
    private accountService: AccountService,
    private transactionService: TransactionService
  ) {
    // Initialize mock alerts
    this.initializeMockAlerts();
  }

  /**
   * Get base URL for analytics service
   * Uses environment configuration, with localStorage override for development
   */
  private getBaseUrl(): string {
    // Check for custom base URL in localStorage (for development/testing)
    const customBase = localStorage.getItem('ANALYTICS_SERVICE_URL');
    if (customBase) {
      return customBase.replace(/\/+$/, '');
    }
    // Build base using global apiBaseUrl to ensure calls go to `${apiBaseUrl}/api/analytics`
    return `${environment.apiBaseUrl}/api/analytics`.replace(/\/+$/, '');
  }

  // ============================================================================
  // HTTP Helper Methods
  // ============================================================================

  /**
   * Generic HTTP call method with retry logic and fallback to mock
   *
   * @param endpoint - API endpoint (relative to base URL)
   * @param method - HTTP method (GET, POST, PUT, DELETE)
   * @param params - Query parameters
   * @param body - Request body (for POST/PUT)
   * @param mockFallback - Observable to return if HTTP call fails
   * @param retryCount - Number of retry attempts (default: 2)
   * @returns Observable<T>
   *
   * TODO: Replace mock fallback with proper error handling when backend is ready
   */
  private httpCall<T>(
    endpoint: string,
    method: 'GET' | 'POST' | 'PUT' | 'DELETE' = 'GET',
    params?: HttpParams,
    body?: any,
    mockFallback?: Observable<T>,
    retryCount: number = 2
  ): Observable<T> {
    const url = `${this.getBaseUrl()}/${endpoint.replace(/^\//, '')}`;
    const getOptions = params ? { params } : {};
    const postOptions = params ? { params } : {};

    let request: Observable<T>;

    switch (method) {
      case 'GET':
        request = this.http.get<T>(url, getOptions);
        break;
      case 'POST':
        request = this.http.post<T>(url, body || {}, postOptions);
        break;
      case 'PUT':
        request = this.http.put<T>(url, body || {}, postOptions);
        break;
      case 'DELETE':
        request = this.http.delete<T>(url, getOptions);
        break;
      default:
        throw new Error(`Unsupported HTTP method: ${method}`);
    }

    return request.pipe(
      // Retry logic: retry on network errors or 5xx server errors
      retry({
        count: retryCount,
        delay: (error: HttpErrorResponse, retryCount: number) => {
          // Only retry on network errors or server errors (5xx)
          if (error.status >= 500 || error.status === 0) {
            // Exponential backoff: 1s, 2s, 4s...
            return timer(Math.pow(2, retryCount) * 1000);
          }
          // Don't retry on client errors (4xx)
          return throwError(() => error);
        }
      }),
      catchError((error: HttpErrorResponse) => {
        console.error(`HTTP Error [${method} ${url}]:`, error);

        // If mock fallback is provided, use it
        if (mockFallback) {
          console.warn('Falling back to mock data due to HTTP error');
          return mockFallback;
        }

        // Otherwise, throw the error
        return throwError(() => error);
      })
    );
  }

  // ============================================================================
  // Dashboard Endpoints
  // ============================================================================

  /**
   * GET /api/v1/analytics/dashboard/summary?userId={userId}
   * Get dashboard summary for a user
   *
   * TODO: Replace mock with HTTP call when backend is ready
   * Backend endpoint: GET /api/v1/analytics/dashboard/summary?userId={userId}
   * Expected response: DashboardSummary DTO
   */
  getDashboardSummary(userId: string): Observable<DashboardSummary> {
    if (this.useMock) {
      return this.getDashboardSummaryMock(userId);
    }

    // TODO: Replace mock with HTTP call
    const params = new HttpParams().set('userId', userId);
    return this.httpCall<DashboardSummary>(
      'dashboard/summary',
      'GET',
      params,
      undefined,
      this.getDashboardSummaryMock(userId) // Fallback to mock on error
    );
  }

  /**
   * GET /api/v1/analytics/spending/breakdown?userId={userId}&period={MONTH|WEEK}
   * Get spending breakdown by category
   *
   * TODO: Replace mock with HTTP call when backend is ready
   * Backend endpoint: GET /api/v1/analytics/spending/breakdown?userId={userId}&period={MONTH|WEEK}
   * Expected response: CategoryBreakdown[] DTO
   */
  getSpendingBreakdown(userId: string, period: SpendingPeriod = 'MONTH'): Observable<CategoryBreakdown[]> {
    if (this.useMock) {
      return this.getSpendingBreakdownMock(userId, period);
    }

    // TODO: Replace mock with HTTP call
    const params = new HttpParams()
      .set('userId', userId)
      .set('period', period);
    return this.httpCall<CategoryBreakdown[]>(
      'spending/breakdown',
      'GET',
      params,
      undefined,
      this.getSpendingBreakdownMock(userId, period) // Fallback to mock on error
    );
  }

  /**
   * GET /api/v1/analytics/trends/balance?userId={userId}&days={30}
   * Get balance trend over time
   *
   * TODO: Replace mock with HTTP call when backend is ready
   * Backend endpoint: GET /api/v1/analytics/trends/balance?userId={userId}&days={30}
   * Expected response: BalanceTrend DTO
   */
  getBalanceTrend(userId: string, days: number = 30): Observable<BalanceTrend> {
    if (this.useMock) {
      return this.getBalanceTrendMock(userId, days);
    }

    // TODO: Replace mock with HTTP call
    const params = new HttpParams()
      .set('userId', userId)
      .set('days', days.toString());
    return this.httpCall<BalanceTrend>(
      'trends/balance',
      'GET',
      params,
      undefined,
      this.getBalanceTrendMock(userId, days) // Fallback to mock on error
    );
  }

  /**
   * GET /api/v1/analytics/insights/recommendations?userId={userId}
   * Get personalized recommendations
   *
   * TODO: Replace mock with HTTP call when backend is ready
   * Backend endpoint: GET /api/v1/analytics/insights/recommendations?userId={userId}
   * Expected response: string[] (array of recommendation messages)
   */
  getRecommendations(userId: string): Observable<string[]> {
    if (this.useMock) {
      return this.getRecommendationsMock(userId);
    }

    // TODO: Replace mock with HTTP call
    const params = new HttpParams().set('userId', userId);
    return this.httpCall<string[]>(
      'insights/recommendations',
      'GET',
      params,
      undefined,
      this.getRecommendationsMock(userId) // Fallback to mock on error
    );
  }

  /**
   * GET /api/v1/analytics/admin/overview
   * Get admin overview (admin only)
   *
   * TODO: Replace mock with HTTP call when backend is ready
   * Backend endpoint: GET /api/v1/analytics/admin/overview
   * Expected response: AdminOverview DTO
   * Note: Requires admin authentication token in headers
   */
  getAdminOverview(): Observable<AdminOverview> {
    if (this.useMock) {
      return this.getAdminOverviewMock();
    }

    // TODO: Replace mock with HTTP call
    return this.httpCall<AdminOverview>(
      'admin/overview',
      'GET',
      undefined,
      undefined,
      this.getAdminOverviewMock() // Fallback to mock on error
    );
  }

  // ============================================================================
  // Alert Endpoints
  // ============================================================================

  /**
   * GET /api/v1/analytics/alerts/active?userId={userId}
   * Get active alerts for a user
   *
   * TODO: Replace mock with HTTP call when backend is ready
   * Backend endpoint: GET /api/v1/analytics/alerts/active?userId={userId}
   * Expected response: Alert[] DTO
   */
  getActiveAlerts(userId: string): Observable<Alert[]> {
    if (this.useMock) {
      return this.getActiveAlertsMock(userId);
    }

    // TODO: Replace mock with HTTP call
    const params = new HttpParams().set('userId', userId);
    return this.httpCall<Alert[]>(
      'alerts/active',
      'GET',
      params,
      undefined,
      this.getActiveAlertsMock(userId) // Fallback to mock on error
    );
  }

  /**
   * POST /api/v1/analytics/alerts/{alertId}/resolve
   * Resolve an alert
   *
   * TODO: Replace mock with HTTP call when backend is ready
   * Backend endpoint: POST /api/v1/analytics/alerts/{alertId}/resolve
   * Expected response: void (204 No Content)
   * Request body: {} (empty object)
   */
  resolveAlert(alertId: string): Observable<void> {
    if (this.useMock) {
      return this.resolveAlertMock(alertId);
    }

    // TODO: Replace mock with HTTP call
    return this.httpCall<void>(
      `alerts/${alertId}/resolve`,
      'POST',
      undefined,
      {}, // Empty request body
      this.resolveAlertMock(alertId) // Fallback to mock on error
    );
  }

  // ============================================================================
  // Mock Implementations
  // ============================================================================

  /**
   * Mock implementation for getDashboardSummary
   * Uses real data from AccountService and TransactionService to simulate backend logic
   */
  private getDashboardSummaryMock(userId: string): Observable<DashboardSummary> {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

    return combineLatest([
      this.accountService.getAccounts(userId),
      this.transactionService.getRecentTransactions(1000)
    ]).pipe(
      map(([accounts, allTransactions]) => {
        // Filter transactions for this user and current month
        const userTransactions = allTransactions.filter(t =>
          t.userId === userId || accounts.some(acc => acc.id === t.fromAccount || acc.id === t.toAccount)
        );

        const monthTransactions = userTransactions.filter(t => {
          const txDate = new Date(t.date);
          return txDate >= startOfMonth && txDate <= now;
        });

        // Calculate current balance from accounts
        const currentBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

        // Calculate monthly spending (negative amounts, excluding transfers)
        const monthlySpending = monthTransactions
          .filter(t => t.amount < 0 && t.category !== TransactionCategory.TRANSFER && t.status === TransactionStatus.COMPLETED)
          .reduce((sum, t) => sum + Math.abs(t.amount), 0);

        // Calculate monthly income (positive amounts, excluding transfers)
        const monthlyIncome = monthTransactions
          .filter(t => t.amount > 0 && t.category !== TransactionCategory.TRANSFER && t.status === TransactionStatus.COMPLETED)
          .reduce((sum, t) => sum + t.amount, 0);

        const transactionsThisMonth = monthTransactions.filter(t => t.status === TransactionStatus.COMPLETED).length;

        // Generate balance trend based on account history (simulated)
        const dataPoints = this.generateBalanceTrendDataPoints(accounts, currentBalance, 30);

        // Generate top categories from real transactions
        const topCategories = this.generateCategoryBreakdownFromTransactions(monthTransactions);

        // Generate recent transactions from real data
        const recentTransactions = this.generateRecentTransactionsFromData(
          monthTransactions.slice(0, 5).reverse()
        );

        const summary: DashboardSummary = {
          userId,
          currentBalance,
          monthlySpending,
          monthlyIncome,
          transactionsThisMonth,
          topCategories,
          balanceTrend: {
            period: '30 days',
            dataPoints
          },
          recentTransactions,
          generatedAt: now.toISOString()
        };

        return summary;
      }),
      delay(300)
    );
  }

  /**
   * Mock implementation for getSpendingBreakdown
   * Uses real transaction data to calculate category breakdown
   */
  private getSpendingBreakdownMock(userId: string, period: SpendingPeriod): Observable<CategoryBreakdown[]> {
    const now = new Date();
    const isMonth = period === 'MONTH';
    const periodStart = isMonth
      ? new Date(now.getFullYear(), now.getMonth(), 1)
      : new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);

    return combineLatest([
      this.accountService.getAccounts(userId),
      this.transactionService.getRecentTransactions(1000)
    ]).pipe(
      map(([accounts, allTransactions]) => {
        // Filter transactions for this user and period
        const userTransactions = allTransactions.filter(t =>
          t.userId === userId || accounts.some(acc => acc.id === t.fromAccount || acc.id === t.toAccount)
        );

        const periodTransactions = userTransactions.filter(t => {
          const txDate = new Date(t.date);
          return txDate >= periodStart && txDate <= now &&
                 t.amount < 0 &&
                 t.category !== TransactionCategory.TRANSFER &&
                 t.status === TransactionStatus.COMPLETED;
        });

        // Calculate breakdown by category
        const categoryMap = new Map<string, { amount: number; count: number }>();

        periodTransactions.forEach(t => {
          const categoryName = this.mapCategoryToBackendName(t.category);
          const current = categoryMap.get(categoryName) || { amount: 0, count: 0 };
          categoryMap.set(categoryName, {
            amount: current.amount + Math.abs(t.amount),
            count: current.count + 1
          });
        });

        const totalSpending = Array.from(categoryMap.values()).reduce((sum, val) => sum + val.amount, 0);

        // Convert to CategoryBreakdown array
        const breakdown: CategoryBreakdown[] = Array.from(categoryMap.entries())
          .map(([category, data]) => ({
            category,
            amount: Math.round(data.amount * 100) / 100,
            count: data.count,
            percentage: totalSpending > 0 ? Math.round((data.amount / totalSpending) * 100 * 10) / 10 : 0
          }))
          .sort((a, b) => b.amount - a.amount);

        // Normalize percentages to total 100%
        const totalPercentage = breakdown.reduce((sum, item) => sum + item.percentage, 0);
        if (totalPercentage !== 100 && breakdown.length > 0) {
          const diff = 100 - totalPercentage;
          breakdown[0].percentage = Math.round((breakdown[0].percentage + diff) * 10) / 10;
        }

        return breakdown;
      }),
      delay(250)
    );
  }

  /**
   * Mock implementation for getBalanceTrend
   * Generates balance trend based on account balances and transaction history
   */
  private getBalanceTrendMock(userId: string, days: number): Observable<BalanceTrend> {
    return combineLatest([
      this.accountService.getAccounts(userId),
      this.transactionService.getRecentTransactions(1000)
    ]).pipe(
      map(([accounts]) => {
        const currentBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

        // Generate data points based on transaction history
        const dataPoints = this.generateBalanceTrendDataPoints(accounts, currentBalance, days);

        const trend: BalanceTrend = {
          period: `${days} days`,
          dataPoints
        };

        return trend;
      }),
      delay(300)
    );
  }

  /**
   * Mock implementation for getRecommendations
   * Generates recommendations based on real account and transaction data
   */
  private getRecommendationsMock(userId: string): Observable<string[]> {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

    return combineLatest([
      this.accountService.getAccounts(userId),
      this.transactionService.getRecentTransactions(1000)
    ]).pipe(
      map(([accounts, allTransactions]) => {
        const recommendations: string[] = [];

        // Filter user transactions
        const userTransactions = allTransactions.filter(t =>
          t.userId === userId || accounts.some(acc => acc.id === t.fromAccount || acc.id === t.toAccount)
        );

        const monthTransactions = userTransactions.filter(t => {
          const txDate = new Date(t.date);
          return txDate >= startOfMonth && txDate <= now && t.status === TransactionStatus.COMPLETED;
        });

        const currentBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);
        const monthlySpending = monthTransactions
          .filter(t => t.amount < 0 && t.category !== TransactionCategory.TRANSFER)
          .reduce((sum, t) => sum + Math.abs(t.amount), 0);
        const monthlyIncome = monthTransactions
          .filter(t => t.amount > 0 && t.category === TransactionCategory.SALARY)
          .reduce((sum, t) => sum + t.amount, 0);

        // Recommendation 1: Savings account
        const hasSavings = accounts.some(acc => acc.accountType === 'SAVINGS');
        if (!hasSavings && currentBalance > 5000) {
          recommendations.push('Consider opening a savings account to earn interest on your funds.');
        }

        // Recommendation 2: High spending on food
        const foodSpending = monthTransactions
          .filter(t => t.category === TransactionCategory.FOOD && t.amount < 0)
          .reduce((sum, t) => sum + Math.abs(t.amount), 0);
        if (foodSpending > monthlySpending * 0.3) {
          recommendations.push('You\'re spending a significant portion on dining out. Try meal planning to reduce costs.');
        }

        // Recommendation 3: Automatic transfers
        if (monthlyIncome > 0 && currentBalance > monthlyIncome * 2) {
          recommendations.push('Consider setting up automatic transfers to your savings account.');
        }

        // Recommendation 4: Budget management
        if (monthlyIncome > 0 && monthlySpending > monthlyIncome * 0.8) {
          recommendations.push('Your spending is high relative to your income. Consider creating a budget to track expenses.');
        }

        // Recommendation 5: Investment opportunities
        if (currentBalance > 10000 && monthlyIncome > 0) {
          recommendations.push('You have a healthy balance. Consider exploring investment opportunities to grow your wealth.');
        }

        // Recommendation 6: Positive feedback
        if (monthlyIncome > monthlySpending && monthlyIncome > 0) {
          recommendations.push('Great job managing your finances! You\'re saving more than you\'re spending this month.');
        }

        // Default recommendations if none generated
        if (recommendations.length === 0) {
          recommendations.push('Keep tracking your expenses to better understand your spending habits.');
          recommendations.push('Review your transactions regularly to identify areas for savings.');
        }

        return recommendations.slice(0, 5); // Return top 5 recommendations
      }),
      delay(200)
    );
  }

  /**
   * Mock implementation for getAdminOverview
   */
  private getAdminOverviewMock(): Observable<AdminOverview> {
    const overview: AdminOverview = {
      activeUsers: 1247,
      totalTransactions: 15234,
      revenue: 125000.50
    };

    return of(overview).pipe(delay(250));
  }

  /**
   * Mock implementation for getActiveAlerts
   * Generates alerts based on real account and transaction data using business rules
   */
  private getActiveAlertsMock(userId: string): Observable<Alert[]> {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

    return combineLatest([
      this.accountService.getAccounts(userId),
      this.transactionService.getRecentTransactions(1000)
    ]).pipe(
      map(([accounts, allTransactions]) => {
        const alerts: Alert[] = [];

        // Filter user transactions
        const userTransactions = allTransactions.filter(t =>
          t.userId === userId || accounts.some(acc => acc.id === t.fromAccount || acc.id === t.toAccount)
        );

        const monthTransactions = userTransactions.filter(t => {
          const txDate = new Date(t.date);
          return txDate >= startOfMonth && txDate <= now && t.status === TransactionStatus.COMPLETED;
        });

        // Calculate current balance
        const currentBalance = accounts.reduce((sum, acc) => sum + acc.balance, 0);

        // Calculate monthly spending
        const monthlySpending = monthTransactions
          .filter(t => t.amount < 0 && t.category !== TransactionCategory.TRANSFER)
          .reduce((sum, t) => sum + Math.abs(t.amount), 0);

        // Rule 1: Low Balance Alert (if balance < 1000 or < 10% of average balance)
        const avgBalance = accounts.length > 0 ? currentBalance / accounts.length : 0;
        if (currentBalance < 1000 || (avgBalance > 0 && currentBalance < avgBalance * 0.1)) {
          alerts.push({
            alertId: `alert-low-balance-${userId}`,
            userId,
            alertType: AlertType.LOW_BALANCE,
            severity: currentBalance < 500 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
            title: 'Low Balance Alert',
            message: `Your account balance is low: ${currentBalance.toFixed(2)}. Consider transferring funds.`,
            thresholdValue: 1000,
            currentValue: currentBalance,
            status: AlertStatus.ACTIVE,
            triggeredAt: new Date(now.getTime() - 2 * 60 * 60 * 1000).toISOString(),
            notified: false
          });
        }

        // Rule 2: Spending Threshold Alert (if monthly spending > 3000)
        const spendingThreshold = 3000;
        if (monthlySpending > spendingThreshold) {
          alerts.push({
            alertId: `alert-spending-threshold-${userId}`,
            userId,
            alertType: AlertType.SPENDING_THRESHOLD,
            severity: monthlySpending > spendingThreshold * 1.5 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
            title: 'Monthly Spending Threshold Exceeded',
            message: `You have exceeded your monthly spending threshold of ${spendingThreshold}. Current spending: ${monthlySpending.toFixed(2)}`,
            thresholdValue: spendingThreshold,
            currentValue: monthlySpending,
            status: AlertStatus.ACTIVE,
            triggeredAt: new Date(now.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString(),
            notified: true
          });
        }

        // Rule 3: Large Transaction Alert (if any transaction > 5000)
        const largeTransactions = monthTransactions.filter(t => Math.abs(t.amount) > 5000);
        if (largeTransactions.length > 0) {
          const largest = largeTransactions.reduce((max, t) => Math.abs(t.amount) > Math.abs(max.amount) ? t : max, largeTransactions[0]);
          alerts.push({
            alertId: `alert-large-transaction-${userId}-${largest.id}`,
            userId,
            alertType: AlertType.LARGE_TRANSACTION,
            severity: Math.abs(largest.amount) > 10000 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
            title: 'Large Transaction Detected',
            message: `A large transaction of ${Math.abs(largest.amount).toFixed(2)} was detected. Please verify if this was authorized.`,
            currentValue: Math.abs(largest.amount),
            status: AlertStatus.ACTIVE,
            triggeredAt: new Date(largest.date).toISOString(),
            notified: true
          });
        }

        // Rule 4: Frequent Transactions Alert (if > 20 transactions in last 24 hours)
        const last24Hours = new Date(now.getTime() - 24 * 60 * 60 * 1000);
        const recentTransactions = monthTransactions.filter(t => new Date(t.date) >= last24Hours);
        if (recentTransactions.length > 20) {
          alerts.push({
            alertId: `alert-frequent-transactions-${userId}`,
            userId,
            alertType: AlertType.FREQUENT_TRANSACTIONS,
            severity: recentTransactions.length > 30 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
            title: 'Frequent Transactions Detected',
            message: `You have made ${recentTransactions.length} transactions in the last 24 hours. This is unusual activity.`,
            currentValue: recentTransactions.length,
            status: AlertStatus.ACTIVE,
            triggeredAt: now.toISOString(),
            notified: false
          });
        }

        // Rule 5: Budget Exceeded (if spending > 120% of income)
        const monthlyIncome = monthTransactions
          .filter(t => t.amount > 0 && t.category === TransactionCategory.SALARY)
          .reduce((sum, t) => sum + t.amount, 0);

        if (monthlyIncome > 0 && monthlySpending > monthlyIncome * 1.2) {
          alerts.push({
            alertId: `alert-budget-exceeded-${userId}`,
            userId,
            alertType: AlertType.BUDGET_EXCEEDED,
            severity: AlertSeverity.CRITICAL,
            title: 'Budget Exceeded',
            message: `You have exceeded your monthly budget. Spending: ${monthlySpending.toFixed(2)}, Income: ${monthlyIncome.toFixed(2)}`,
            thresholdValue: monthlyIncome,
            currentValue: monthlySpending,
            status: AlertStatus.ACTIVE,
            triggeredAt: new Date(now.getTime() - 3 * 60 * 60 * 1000).toISOString(),
            notified: true
          });
        }

        // Merge with existing alerts (avoid duplicates)
        const existingAlertIds = new Set(this.mockAlerts.map(a => a.alertId));
        const newAlerts = alerts.filter(a => !existingAlertIds.has(a.alertId));
        this.mockAlerts = [...this.mockAlerts, ...newAlerts];

        return this.mockAlerts.filter(a => a.userId === userId && a.status === AlertStatus.ACTIVE);
      }),
      delay(200)
    );
  }

  /**
   * Mock implementation for resolveAlert
   */
  private resolveAlertMock(alertId: string): Observable<void> {
    const alert = this.mockAlerts.find(a => a.alertId === alertId);
    if (alert) {
      alert.status = AlertStatus.RESOLVED;
      alert.resolvedAt = new Date().toISOString();
    }
    return of(void 0).pipe(delay(150));
  }

  /**
   * Initialize mock alerts - will be regenerated based on real data
   */
  private initializeMockAlerts(): void {
    // Alerts will be generated dynamically based on real account and transaction data
    this.mockAlerts = [];
  }

  /**
   * Generate balance trend data points based on account history
   */
  private generateBalanceTrendDataPoints(accounts: any[], currentBalance: number, days: number): DataPoint[] {
    const now = new Date();
    const dataPoints: DataPoint[] = [];

    // Start with a base balance (assume 10% lower 30 days ago)
    let baseBalance = currentBalance * 0.9;

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);

      // Simulate gradual progression towards current balance
      const progress = (days - i) / days;
      const variation = (Math.random() - 0.5) * (currentBalance * 0.02); // ±2% variation
      const balance = baseBalance + (currentBalance - baseBalance) * progress + variation;

      dataPoints.push({
        timestamp: date.toISOString(),
        value: Math.round(Math.max(0, balance) * 100) / 100
      });
    }

    return dataPoints;
  }

  /**
   * Generate category breakdown from real transactions
   */
  private generateCategoryBreakdownFromTransactions(transactions: any[]): CategoryBreakdown[] {
    const categoryMap = new Map<string, { amount: number; count: number }>();

    // Map TransactionCategory to backend category names
    transactions
      .filter(t => t.amount < 0 && t.category !== TransactionCategory.TRANSFER && t.status === TransactionStatus.COMPLETED)
      .forEach(t => {
        const categoryName = this.mapCategoryToBackendName(t.category);
        const current = categoryMap.get(categoryName) || { amount: 0, count: 0 };
        categoryMap.set(categoryName, {
          amount: current.amount + Math.abs(t.amount),
          count: current.count + 1
        });
      });

    const totalSpending = Array.from(categoryMap.values()).reduce((sum, val) => sum + val.amount, 0);

    const breakdown: CategoryBreakdown[] = Array.from(categoryMap.entries())
      .map(([category, data]) => ({
        category,
        amount: Math.round(data.amount * 100) / 100,
        count: data.count,
        percentage: totalSpending > 0 ? Math.round((data.amount / totalSpending) * 100 * 10) / 10 : 0
      }))
      .sort((a, b) => b.amount - a.amount)
      .slice(0, 5); // Top 5 categories

    // Normalize percentages to total 100%
    const totalPercentage = breakdown.reduce((sum, item) => sum + item.percentage, 0);
    if (totalPercentage !== 100 && breakdown.length > 0) {
      const diff = 100 - totalPercentage;
      breakdown[0].percentage = Math.round((breakdown[0].percentage + diff) * 10) / 10;
    }

    return breakdown;
  }

  /**
   * Map TransactionCategory enum to backend category names
   */
  private mapCategoryToBackendName(category: TransactionCategory): string {
    const mapping: Record<TransactionCategory, string> = {
      [TransactionCategory.FOOD]: 'Food & Dining',
      [TransactionCategory.TRANSPORT]: 'Transportation',
      [TransactionCategory.SHOPPING]: 'Shopping',
      [TransactionCategory.UTILITIES]: 'Utilities',
      [TransactionCategory.ENTERTAINMENT]: 'Entertainment',
      [TransactionCategory.HEALTHCARE]: 'Healthcare',
      [TransactionCategory.EDUCATION]: 'Education',
      [TransactionCategory.INVESTMENT]: 'Investment',
      [TransactionCategory.SALARY]: 'Salary',
      [TransactionCategory.TRANSFER]: 'Transfer',
      [TransactionCategory.OTHER]: 'Other'
    };
    return mapping[category] || 'Other';
  }

  /**
   * Generate recent transactions from transaction data
   */
  private generateRecentTransactionsFromData(transactions: any[]): RecentTransaction[] {
    return transactions.map(t => ({
      transactionId: t.id,
      type: t.type === TransactionCategory.SALARY ? 'CREDIT' : (t.amount < 0 ? 'DEBIT' : 'CREDIT'),
      amount: Math.abs(t.amount),
      merchant: t.merchant || t.description || 'Unknown',
      date: new Date(t.date).toISOString()
    }));
  }
}

