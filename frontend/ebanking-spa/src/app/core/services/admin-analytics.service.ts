import { Injectable } from '@angular/core';
import { Observable, combineLatest, map } from 'rxjs';
import { UserService } from './user.service';
import { TransactionService } from './transaction.service';
import { AccountService } from './account.service';
import { User, UserRole, UserStatus, KYCStatus } from '../../models';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';

export interface SystemStats {
  totalUsers: number;
  totalClients: number;
  totalAgents: number;
  totalAdmins: number;
  activeUsers: number;
  inactiveUsers: number;
  newUsersThisMonth: number;
  totalTransactions: number;
  totalRevenue: number;
  totalAccounts: number;
  pendingKyc: number;
  verifiedKyc: number;
  rejectedKyc: number;
}

export interface UserGrowthData {
  month: string;
  clients: number;
  agents: number;
  total: number;
}

export interface SystemAlert {
  id: string;
  type: 'error' | 'warning' | 'info' | 'fraud' | 'maintenance';
  title: string;
  message: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  timestamp: Date;
  resolved: boolean;
}

export interface ServiceHealth {
  name: string;
  status: 'UP' | 'DOWN' | 'DEGRADED';
  responseTime?: number;
  lastCheck: Date;
  details?: any;
}

export interface ApiPerformance {
  endpoint: string;
  method: string;
  avgResponseTime: number;
  requestCount: number;
  errorRate: number;
  last24h: number;
}

/**
 * Admin Analytics Service
 * Aggregates system-wide statistics for admin dashboard
 */
@Injectable({
  providedIn: 'root'
})
export class AdminAnalyticsService {

  constructor(
    private userService: UserService,
    private transactionService: TransactionService,
    private accountService: AccountService
  ) {}

  /**
   * Get system-wide statistics
   */
  getSystemStats(): Observable<SystemStats> {
    return combineLatest([
      this.userService.getAllUsers(),
      this.transactionService.getRecentTransactions(10000),
      this.accountService.getAccounts()
    ]).pipe(
      map(([users, transactions, accounts]) => {
        const now = new Date();
        const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

        const totalUsers = users.length;
        const totalClients = users.filter(u => u.role === UserRole.CLIENT).length;
        const totalAgents = users.filter(u => u.role === UserRole.AGENT).length;
        const totalAdmins = users.filter(u => u.role === UserRole.ADMIN || u.role === UserRole.SUPER_ADMIN).length;
        
        const activeUsers = users.filter(u => u.status === UserStatus.ACTIVE).length;
        const inactiveUsers = users.filter(u => u.status === UserStatus.INACTIVE || u.status === UserStatus.SUSPENDED).length;
        
        const newUsersThisMonth = users.filter(u => {
          const created = new Date(u.createdAt);
          return created >= startOfMonth;
        }).length;

        const completedTransactions = transactions.filter(t => t.status === 'COMPLETED');
        const totalTransactions = completedTransactions.length;
        const totalRevenue = completedTransactions
          .filter(t => t.amount > 0)
          .reduce((sum, t) => sum + t.amount, 0);

        const totalAccounts = accounts.length;

        const clients = users.filter(u => u.role === UserRole.CLIENT);
        const pendingKyc = clients.filter(c => c.kycStatus === KYCStatus.IN_PROGRESS || c.kycStatus === KYCStatus.NOT_STARTED).length;
        const verifiedKyc = clients.filter(c => c.kycStatus === KYCStatus.VERIFIED).length;
        const rejectedKyc = clients.filter(c => c.kycStatus === KYCStatus.REJECTED).length;

        return {
          totalUsers,
          totalClients,
          totalAgents,
          totalAdmins,
          activeUsers,
          inactiveUsers,
          newUsersThisMonth,
          totalTransactions,
          totalRevenue,
          totalAccounts,
          pendingKyc,
          verifiedKyc,
          rejectedKyc
        };
      })
    );
  }

  /**
   * Get user growth chart data (last 6 months)
   */
  getUserGrowthChart(): Observable<ChartData> {
    return this.userService.getAllUsers().pipe(
      map((users) => {
        const now = new Date();
        const months: string[] = [];
        const clientsData: number[] = [];
        const agentsData: number[] = [];
        const totalData: number[] = [];

        for (let i = 5; i >= 0; i--) {
          const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
          const monthName = date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
          months.push(monthName);

          const monthStart = new Date(date.getFullYear(), date.getMonth(), 1);
          const monthEnd = new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59);

          const clientsInMonth = users.filter(u => {
            const created = new Date(u.createdAt);
            return u.role === UserRole.CLIENT && created >= monthStart && created <= monthEnd;
          }).length;

          const agentsInMonth = users.filter(u => {
            const created = new Date(u.createdAt);
            return u.role === UserRole.AGENT && created >= monthStart && created <= monthEnd;
          }).length;

          clientsData.push(clientsInMonth);
          agentsData.push(agentsInMonth);
          totalData.push(clientsInMonth + agentsInMonth);
        }

        return {
          labels: months,
          datasets: [
            {
              label: 'Clients',
              data: clientsData,
              color: '#4F46E5'
            },
            {
              label: 'Agents',
              data: agentsData,
              color: '#10B981'
            },
            {
              label: 'Total',
              data: totalData,
              color: '#F59E0B'
            }
          ]
        };
      })
    );
  }

  /**
   * Get revenue chart data (last 6 months)
   */
  getRevenueChart(): Observable<ChartData> {
    return combineLatest([
      this.transactionService.getRecentTransactions(10000),
      this.accountService.getAccounts()
    ]).pipe(
      map(([transactions, accounts]) => {
        const now = new Date();
        const months: string[] = [];
        const revenueData: number[] = [];

        for (let i = 5; i >= 0; i--) {
          const date = new Date(now.getFullYear(), now.getMonth() - i, 1);
          const monthName = date.toLocaleDateString('en-US', { month: 'short', year: 'numeric' });
          months.push(monthName);

          const monthStart = new Date(date.getFullYear(), date.getMonth(), 1);
          const monthEnd = new Date(date.getFullYear(), date.getMonth() + 1, 0, 23, 59, 59);

          const monthTransactions = transactions.filter(t => {
            const txDate = new Date(t.date);
            return t.status === 'COMPLETED' && t.amount > 0 && txDate >= monthStart && txDate <= monthEnd;
          });

          const monthRevenue = monthTransactions.reduce((sum, t) => sum + t.amount, 0);
          revenueData.push(monthRevenue);
        }

        return {
          labels: months,
          datasets: [
            {
              label: 'Revenue',
              data: revenueData,
              color: '#10B981'
            }
          ]
        };
      })
    );
  }

  /**
   * Get user distribution by role
   */
  getUserDistributionChart(): Observable<ChartData> {
    return this.userService.getAllUsers().pipe(
      map((users) => {
        const clients = users.filter(u => u.role === UserRole.CLIENT).length;
        const agents = users.filter(u => u.role === UserRole.AGENT).length;
        const admins = users.filter(u => u.role === UserRole.ADMIN || u.role === UserRole.SUPER_ADMIN).length;

        return {
          labels: ['Clients', 'Agents', 'Admins'],
          datasets: [
            {
              label: 'Users',
              data: [clients, agents, admins],
              color: '#4F46E5'
            }
          ]
        };
      })
    );
  }

  /**
   * Get system alerts (mock data - in production, this would come from a monitoring service)
   */
  getSystemAlerts(): Observable<SystemAlert[]> {
    // Mock alerts - in production, this would call a monitoring/alerting service
    const alerts: SystemAlert[] = [
      {
        id: 'alert-1',
        type: 'warning',
        title: 'High Transaction Volume',
        message: 'Transaction volume is 20% above average',
        severity: 'medium',
        timestamp: new Date(Date.now() - 3600000),
        resolved: false
      },
      {
        id: 'alert-2',
        type: 'info',
        title: 'Scheduled Maintenance',
        message: 'System maintenance scheduled for tonight at 2 AM',
        severity: 'low',
        timestamp: new Date(Date.now() - 7200000),
        resolved: false
      },
      {
        id: 'alert-3',
        type: 'fraud',
        title: 'Suspicious Activity Detected',
        message: 'Multiple failed login attempts detected',
        severity: 'high',
        timestamp: new Date(Date.now() - 1800000),
        resolved: false
      }
    ];

    return new Observable(observer => {
      observer.next(alerts);
      observer.complete();
    });
  }

  /**
   * Get service health status (mock data - in production, this would call actuator endpoints)
   */
  getServiceHealth(): Observable<ServiceHealth[]> {
    // Mock health data - in production, this would call /actuator/health endpoints
    const services: ServiceHealth[] = [
      {
        name: 'User Service',
        status: 'UP',
        responseTime: 45,
        lastCheck: new Date(),
        details: { database: 'UP', kafka: 'UP' }
      },
      {
        name: 'Payment Service',
        status: 'UP',
        responseTime: 62,
        lastCheck: new Date(),
        details: { database: 'UP', kafka: 'UP' }
      },
      {
        name: 'Account Service',
        status: 'UP',
        responseTime: 38,
        lastCheck: new Date(),
        details: { database: 'UP' }
      },
      {
        name: 'Transaction Service',
        status: 'DEGRADED',
        responseTime: 250,
        lastCheck: new Date(),
        details: { database: 'UP', kafka: 'SLOW' }
      }
    ];

    return new Observable(observer => {
      observer.next(services);
      observer.complete();
    });
  }

  /**
   * Get API performance metrics (mock data - in production, this would come from metrics service)
   */
  getApiPerformance(): Observable<ApiPerformance[]> {
    // Mock performance data - in production, this would come from Prometheus/metrics
    const performance: ApiPerformance[] = [
      {
        endpoint: '/api/v1/admin/users',
        method: 'GET',
        avgResponseTime: 120,
        requestCount: 1543,
        errorRate: 0.2,
        last24h: 234
      },
      {
        endpoint: '/api/v1/payments',
        method: 'POST',
        avgResponseTime: 85,
        requestCount: 8921,
        errorRate: 0.1,
        last24h: 1234
      },
      {
        endpoint: '/api/v1/accounts',
        method: 'GET',
        avgResponseTime: 45,
        requestCount: 4567,
        errorRate: 0.05,
        last24h: 567
      },
      {
        endpoint: '/api/v1/transactions',
        method: 'GET',
        avgResponseTime: 78,
        requestCount: 6789,
        errorRate: 0.15,
        last24h: 890
      }
    ];

    return new Observable(observer => {
      observer.next(performance);
      observer.complete();
    });
  }
}

