import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface AdminOverview {
  totalUsers: number;
  totalClients: number;
  totalAgents: number;
  totalAdmins: number;
  activeUsers: number;
  inactiveUsers: number;
  newUsersThisMonth: number;
  totalTransactions: number;
  revenue: number;
  totalAccounts: number;
  pendingKyc: number;
  verifiedKyc: number;
  rejectedKyc: number;
}

export interface UserGrowthData {
  labels: string[];
  clients: number[];
  agents: number[];
}

export interface RoleDistribution {
  role: string;
  count: number;
  percentage: number;
}

export interface SystemHealth {
  uptime: string;
  responseTime: string;
  errorRate: string;
  activeConnections: number;
  cpuUsage: number;
  memoryUsage: number;
  diskUsage: number;
}

export interface SystemActivity {
  id: string;
  type: string;
  description: string;
  timestamp: string;
  severity: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminAnalyticsService {
  private readonly analyticsUrl = environment.analyticsServiceUrl;

  constructor(private http: HttpClient) {} // ← Seulement HttpClient est nécessaire

  /**
   * ✅ CORRIGÉ : Plus de userService → Plus d'erreur TS2339
   * Appel direct vers le backend /admin/overview
   */
  getAdminOverview(): Observable<AdminOverview> {
    return this.http.get<AdminOverview>(`${this.analyticsUrl}/admin/overview`).pipe(
      catchError(error => {
        console.error('Erreur lors de la récupération du dashboard admin :', error);
        // Retourner un objet vide pour éviter le crash de l'UI
        return of({
          totalUsers: 0,
          totalClients: 0,
          totalAgents: 0,
          totalAdmins: 0,
          activeUsers: 0,
          inactiveUsers: 0,
          newUsersThisMonth: 0,
          totalTransactions: 0,
          revenue: 0,
          totalAccounts: 0,
          pendingKyc: 0,
          verifiedKyc: 0,
          rejectedKyc: 0
        });
      })
    );
  }

  getUserGrowthData(): Observable<UserGrowthData> {
    return this.http.get<UserGrowthData>(`${this.analyticsUrl}/admin/user-growth`).pipe(
      catchError(() => of({ labels: [], clients: [], agents: [] }))
    );
  }

  getRoleDistribution(): Observable<RoleDistribution[]> {
    return this.http.get<RoleDistribution[]>(`${this.analyticsUrl}/admin/role-distribution`).pipe(
      catchError(() => of([]))
    );
  }

  getSystemHealth(): Observable<SystemHealth | null> {
    return this.http.get<SystemHealth>(`${this.analyticsUrl}/admin/system-health`).pipe(
      catchError(() => of(null))
    );
  }

  getRecentActivities(): Observable<SystemActivity[]> {
    return this.http.get<SystemActivity[]>(`${this.analyticsUrl}/admin/activities`).pipe(
      catchError(() => of([]))
    );
  }
}
