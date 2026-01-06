import { Injectable } from '@angular/core';
import { Observable, combineLatest, map } from 'rxjs';
import { UserService } from './user.service';
import { User, UserRole, KYCStatus } from '../../models';

export interface AgentStats {
  totalClients: number;
  activeClients: number;
  newClientsThisMonth: number;
  pendingKyc: number;
  verifiedKyc: number;
  rejectedKyc: number;
}

export interface RecentActivity {
  type: 'new_client' | 'kyc_update' | 'status_change';
  clientId: string;
  clientName: string;
  description: string;
  timestamp: Date;
  icon: string;
  color: string;
}

export interface AgentAlert {
  id: string;
  type: 'kyc_pending' | 'kyc_rejected' | 'inactive_client' | 'action_required';
  priority: 'high' | 'medium' | 'low';
  title: string;
  description: string;
  clientId?: string;
  clientName?: string;
  timestamp: Date;
  actionUrl?: string;
}

export interface PerformanceMetrics {
  clientGrowth: number; // Percentage
  activeClientRate: number; // Percentage
  kycCompletionRate: number; // Percentage
  monthlyTrend: {
    month: string;
    clients: number;
    active: number;
  }[];
}

/**
 * Agent Analytics Service
 * Aggregates data from UserService to generate agent-specific analytics
 *
 * Note: The backend Analytics Service does not currently provide agent-specific endpoints.
 * This service uses mock data based on UserService.getAgentClients().
 * When agent analytics endpoints are added to the backend, this service should be
 * refactored to use AnalyticsBackendService similar to AdminAnalyticsService.
 */
@Injectable({
  providedIn: 'root'
})
export class AgentAnalyticsService {

  constructor(private userService: UserService) {}

  /**
   * Get agent statistics
   */
  getAgentStats(agentId: string): Observable<AgentStats> {
    return this.userService.getAgentsClients(agentId).pipe(
      map((clients: User[]) => {
        const now = new Date();
        const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

        const totalClients = clients.length;
        const activeClients = clients.filter((c: User) => c.status === 'ACTIVE').length;
        const newClientsThisMonth = clients.filter((c: User) => {
          const created = new Date(c.createdAt);
          return created >= startOfMonth;
        }).length;
        const pendingKyc = clients.filter((c: User) => c.kycStatus === KYCStatus.IN_PROGRESS || c.kycStatus === KYCStatus.NOT_STARTED).length;
        const verifiedKyc = clients.filter((c: User) => c.kycStatus === KYCStatus.VERIFIED).length;
        const rejectedKyc = clients.filter((c: User) => c.kycStatus === KYCStatus.REJECTED).length;
        return {
          totalClients,
          activeClients,
          newClientsThisMonth,
          pendingKyc,
          verifiedKyc,
          rejectedKyc
        };
      })
    );
  }

  /**
   * Get recent activity
   */
  getRecentActivity(agentId: string, limit: number = 10): Observable<RecentActivity[]> {
    return this.userService.getAgentsClients(agentId).pipe(
      map((clients: User[]) => {
        const activities: RecentActivity[] = [];
        const now = new Date();
        const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
        clients.forEach((client: User) => {
          const created = new Date(client.createdAt);
          const updated = client.lastLogin ? new Date(client.lastLogin) : null;
          // New clients this month
          if (created >= thirtyDaysAgo) {
            activities.push({
              type: 'new_client',
              clientId: client.id,
              clientName: `${client.firstName} ${client.lastName}`,
              description: 'New client registered',
              timestamp: created,
              icon: 'person_add',
              color: '#10B981'
            });
          }
          // KYC status changes
          if (updated && updated >= thirtyDaysAgo) {
            if (client.kycStatus === KYCStatus.IN_PROGRESS || client.kycStatus === KYCStatus.NOT_STARTED) {
              activities.push({
                type: 'kyc_update',
                clientId: client.id,
                clientName: `${client.firstName} ${client.lastName}`,
                description: 'KYC verification pending',
                timestamp: updated,
                icon: 'pending_actions',
                color: '#F59E0B'
              });
            } else if (client.kycStatus === KYCStatus.VERIFIED) {
              activities.push({
                type: 'kyc_update',
                clientId: client.id,
                clientName: `${client.firstName} ${client.lastName}`,
                description: 'KYC verified',
                timestamp: updated,
                icon: 'verified',
                color: '#10B981'
              });
            }
          }
          // Status changes
          if (client.status === 'INACTIVE' && updated && updated >= thirtyDaysAgo) {
            activities.push({
              type: 'status_change',
              clientId: client.id,
              clientName: `${client.firstName} ${client.lastName}`,
              description: 'Client account deactivated',
              timestamp: updated,
              icon: 'block',
              color: '#EF4444'
            });
          }
        });
        // Sort by timestamp (most recent first) and limit
        return activities
          .sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
          .slice(0, limit);
      })
    );
  }

  /**
   * Get alerts and tasks
   */
  getAlerts(agentId: string): Observable<AgentAlert[]> {
    return this.userService.getAgentsClients(agentId).pipe(
      map((clients: User[]) => {
        const alerts: AgentAlert[] = [];
        clients.forEach((client: User) => {
          // KYC pending alerts
          if (client.kycStatus === KYCStatus.IN_PROGRESS || client.kycStatus === KYCStatus.NOT_STARTED) {
            alerts.push({
              id: `kyc-pending-${client.id}`,
              type: 'kyc_pending',
              priority: client.kycStatus === KYCStatus.NOT_STARTED ? 'high' : 'medium',
              title: 'KYC Verification Pending',
              description: `${client.firstName} ${client.lastName} needs KYC verification`,
              clientId: client.id,
              clientName: `${client.firstName} ${client.lastName}`,
              timestamp: new Date(client.createdAt),
              actionUrl: `/agent/clients?clientId=${client.id}`
            });
          }
          // KYC rejected alerts
          if (client.kycStatus === KYCStatus.REJECTED) {
            alerts.push({
              id: `kyc-rejected-${client.id}`,
              type: 'kyc_rejected',
              priority: 'high',
              title: 'KYC Verification Rejected',
              description: `${client.firstName} ${client.lastName}'s KYC was rejected`,
              clientId: client.id,
              clientName: `${client.firstName} ${client.lastName}`,
              timestamp: new Date(client.lastLogin || client.createdAt),
              actionUrl: `/agent/clients?clientId=${client.id}`
            });
          }
          // Inactive client alerts
          if (client.status === 'INACTIVE') {
            alerts.push({
              id: `inactive-${client.id}`,
              type: 'inactive_client',
              priority: 'medium',
              title: 'Inactive Client',
              description: `${client.firstName} ${client.lastName}'s account is inactive`,
              clientId: client.id,
              clientName: `${client.firstName} ${client.lastName}`,
              timestamp: new Date(client.lastLogin || client.createdAt),
              actionUrl: `/agent/clients?clientId=${client.id}`
            });
          }
        });
        // Sort by priority (high first) and timestamp
        return alerts.sort((a, b) => {
          const priorityOrder = { high: 0, medium: 1, low: 2 };
          const priorityDiff = priorityOrder[a.priority] - priorityOrder[b.priority];
          if (priorityDiff !== 0) return priorityDiff;
          return b.timestamp.getTime() - a.timestamp.getTime();
        });
      })
    );
  }

  /**
   * Get performance metrics
   */
  getPerformanceMetrics(agentId: string): Observable<PerformanceMetrics> {
    return this.userService.getAgentsClients(agentId).pipe(
      map((clients: User[]) => {
        const now = new Date();
        const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
        const thisMonth = new Date(now.getFullYear(), now.getMonth(), 1);
        const clientsLastMonth = clients.filter((c: User) => {
          const created = new Date(c.createdAt);
          return created < thisMonth;
        }).length;
        const clientsThisMonth = clients.filter((c: User) => {
          const created = new Date(c.createdAt);
          return created >= thisMonth;
        }).length;
        const clientGrowth = clientsLastMonth > 0
          ? ((clientsThisMonth - clientsLastMonth) / clientsLastMonth) * 100
          : clientsThisMonth > 0 ? 100 : 0;
        const activeClientRate = clients.length > 0
          ? (clients.filter((c: User) => c.status === 'ACTIVE').length / clients.length) * 100
          : 0;
        const kycCompletionRate = clients.length > 0
          ? (clients.filter((c: User) => c.kycStatus === KYCStatus.VERIFIED).length / clients.length) * 100
          : 0;
        // Generate monthly trend (last 6 months)
        const monthlyTrend = [];
        for (let i = 5; i >= 0; i--) {
          const monthDate = new Date(now.getFullYear(), now.getMonth() - i, 1);
          const monthName = monthDate.toLocaleDateString('en-US', { month: 'short' });
          const monthClients = clients.filter((c: User) => {
            const created = new Date(c.createdAt);
            return created.getMonth() === monthDate.getMonth() &&
                   created.getFullYear() === monthDate.getFullYear();
          });
          const monthActive = monthClients.filter((c: User) => c.status === 'ACTIVE').length;
          monthlyTrend.push({
            month: monthName,
            clients: monthClients.length,
            active: monthActive
          });
        }
        return {
          clientGrowth,
          activeClientRate,
          kycCompletionRate,
          monthlyTrend
        };
      })
    );
  }
}
