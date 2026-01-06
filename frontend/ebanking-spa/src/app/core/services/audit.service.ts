import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, map, throwError } from 'rxjs';
import { AuditEvent, AuditPage, AuditReportJob } from '../../models';
import { environment } from '../../../environments/environment';
import { GraphQLService } from './graphql.service';

@Injectable({ providedIn: 'root' })
export class AuditService {
  // Build base using global apiBaseUrl to ensure calls go to `${apiBaseUrl}/api/audit`
  private base = (localStorage.getItem('API_BASE') || `${environment.apiBaseUrl}/api/audit`).replace(/\/+$|\s+/g, '');
  private readonly useGraphQL: boolean = environment.useGraphQL ?? false;

  constructor(
    private http: HttpClient,
    private graphqlService?: GraphQLService
  ) {}

  createEvent(event: AuditEvent): Observable<AuditEvent> {
    // Use GraphQL if enabled
    if (this.useGraphQL && this.graphqlService) {
      const mutation = `
        mutation CreateAuditEvent($input: AuditEventInput!) {
          createAuditEvent(input: $input) {
            id
            eventType
            userId
            username
            timestamp
            service
            riskScore
            details
          }
        }
      `;

      return this.graphqlService.mutate<{ createAuditEvent: AuditEvent }>(mutation, {
        input: event
      }).pipe(
        map(result => result.createAuditEvent),
        catchError(() => {
          // Fallback to REST
          return this.http.post<AuditEvent>(`${this.base}/events`, event);
        })
      );
    }

    // Use REST
    return this.http.post<AuditEvent>(`${this.base}/events`, event);
  }

  getEvents(page = 0, size = 20, sortBy = 'timestamp', sortDir = 'DESC'): Observable<AuditPage<AuditEvent>> {
    // Use GraphQL if enabled
    if (this.useGraphQL && this.graphqlService) {
      const query = `
        query GetAuditEvents($page: Int!, $size: Int!, $sortBy: String!, $sortDir: String!) {
          auditEvents(page: $page, size: $size, sortBy: $sortBy, sortDir: $sortDir) {
            content {
              id
              eventType
              userId
              username
              timestamp
              service
              riskScore
              details
            }
            totalElements
            totalPages
            page
            size
          }
        }
      `;

      return this.graphqlService.query<{ auditEvents: AuditPage<AuditEvent> }>(query, {
        page,
        size,
        sortBy,
        sortDir
      }).pipe(
        map(result => result.auditEvents),
        catchError(() => {
          // Fallback to REST
          let params = new HttpParams()
            .set('page', String(page))
            .set('size', String(size))
            .set('sortBy', sortBy)
            .set('sortDir', sortDir);
          return this.http.get<AuditPage<AuditEvent>>(`${this.base}/events`, { params });
        })
      );
    }

    // Use REST
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/events`, { params });
  }

  getEventById(eventId: string): Observable<AuditEvent> {
    // Use GraphQL if enabled
    if (this.useGraphQL && this.graphqlService) {
      const query = `
        query GetAuditEvent($eventId: ID!) {
          auditEvent(id: $eventId) {
            id
            eventType
            userId
            username
            timestamp
            service
            riskScore
            details
          }
        }
      `;

      return this.graphqlService.query<{ auditEvent: AuditEvent }>(query, {
        eventId
      }).pipe(
        map(result => result.auditEvent),
        catchError(() => {
          return this.http.get<AuditEvent>(`${this.base}/events/${eventId}`);
        })
      );
    }

    return this.http.get<AuditEvent>(`${this.base}/events/${eventId}`);
  }

  searchEvents(options: { userId?: number; eventType?: string; startDate?: string; endDate?: string; page?: number; size?: number } = {}): Observable<AuditPage<AuditEvent>> {
    // Use GraphQL if enabled
    if (this.useGraphQL && this.graphqlService) {
      const query = `
        query SearchAuditEvents($userId: Int, $eventType: String, $startDate: String, $endDate: String, $page: Int!, $size: Int!) {
          searchAuditEvents(userId: $userId, eventType: $eventType, startDate: $startDate, endDate: $endDate, page: $page, size: $size) {
            content {
              id
              eventType
              userId
              username
              timestamp
              service
              riskScore
              details
            }
            totalElements
            totalPages
            page
            size
          }
        }
      `;

      return this.graphqlService.query<{ searchAuditEvents: AuditPage<AuditEvent> }>(query, {
        ...options,
        page: options.page ?? 0,
        size: options.size ?? 20
      }).pipe(
        map(result => result.searchAuditEvents),
        catchError(() => {
          // Fallback to REST with corrected endpoint
          let params = new HttpParams();
          if (options.userId != null) params = params.set('userId', String(options.userId));
          if (options.eventType) params = params.set('eventType', options.eventType);
          if (options.startDate) params = params.set('startDate', options.startDate);
          if (options.endDate) params = params.set('endDate', options.endDate);
          params = params.set('page', String(options.page ?? 0)).set('size', String(options.size ?? 20));

          // ✅ Corrected: /search instead of /events/search
          return this.http.get<AuditPage<AuditEvent>>(`${this.base}/search`, { params });
        })
      );
    }

    // Use REST with corrected endpoint
    let params = new HttpParams();
    if (options.userId != null) params = params.set('userId', String(options.userId));
    if (options.eventType) params = params.set('eventType', options.eventType);
    if (options.startDate) params = params.set('startDate', options.startDate);
    if (options.endDate) params = params.set('endDate', options.endDate);
    params = params.set('page', String(options.page ?? 0)).set('size', String(options.size ?? 20));

    // ✅ Corrected: /search instead of /events/search
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/search`, { params });
  }

  getUserTimeline(userId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.base}/users/${userId}/timeline`);
  }

  getUserEvents(userId: number, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/users/${userId}/events`, { params });
  }

  getEventsByType(eventType: string, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/events/type/${encodeURIComponent(eventType)}`, { params });
  }

  getEventsByService(serviceSource: string, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/events/service/${encodeURIComponent(serviceSource)}`, { params });
  }

  getHighRiskEvents(minRiskScore = 0.7, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams()
      .set('minRiskScore', String(minRiskScore))
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/events/high-risk`, { params });
  }

  getFailedEvents(page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/events/failures`, { params });
  }

  getUserStats(userId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/stats/user/${userId}`);
  }

  exportAuditReport(startDate: string, endDate: string, format = 'PDF'): Observable<AuditReportJob> {
    let params = new HttpParams().set('startDate', startDate).set('endDate', endDate).set('format', format);
    return this.http.post<AuditReportJob>(`${this.base}/export`, null, { params });
  }

  getExportFile(jobId: string): Observable<Blob> {
    return this.http.get(`${this.base}/export/${jobId}`, { responseType: 'blob' });
  }

  downloadExportWhenReady(jobId: string, pollInterval = 3000, timeout = 120000): Observable<Blob> {
    return new Observable<Blob>(observer => {
      const start = Date.now();
      const attempt = () => {
        this.http.get(`${this.base}/export/${jobId}`, { responseType: 'blob' }).subscribe({
          next: (blob: Blob) => {
            observer.next(blob);
            observer.complete();
          },
          error: () => {
            const elapsed = Date.now() - start;
            if (elapsed >= timeout) {
              observer.error(new Error('Export timeout'));
              return;
            }
            setTimeout(attempt, pollInterval);
          }
        });
      };
      attempt();
    });
  }

  anonymizeUserData(userId: number): Observable<any> {
    return this.http.post<any>(`${this.base}/users/${userId}/anonymize`, {});
  }

  verifyEventIntegrity(eventId: string): Observable<any> {
    return this.http.post<any>(`${this.base}/verify/${eventId}`, {});
  }

  getUserActivitySummary(userId: number, days = 30): Observable<any> {
    const params = new HttpParams().set('days', String(days));
    return this.http.get<any>(`${this.base}/users/${userId}/activity-summary`, { params });
  }

  getMyEvents(page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/my/events`, { params });
  }

  getSuspiciousActivity(userId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.base}/users/${userId}/suspicious`);
  }

  getSecurityReport(userId: number, days = 30): Observable<any> {
    const params = new HttpParams().set('days', String(days));
    return this.http.get<any>(`${this.base}/users/${userId}/security-report`, { params });
  }

  checkMultipleIpLogins(userId: number, hours = 24): Observable<any> {
    const params = new HttpParams().set('hours', String(hours));
    return this.http.get<any>(`${this.base}/users/${userId}/multiple-ip-check`, { params });
  }

  getAbnormalActivity(hours = 24, threshold = 100): Observable<any> {
    const params = new HttpParams().set('hours', String(hours)).set('threshold', String(threshold));
    return this.http.get<any>(`${this.base}/monitoring/abnormal-activity`, { params });
  }

  getComplianceAudit(flag: string, days = 30): Observable<any> {
    const params = new HttpParams().set('days', String(days));
    return this.http.get<any>(`${this.base}/compliance/${encodeURIComponent(flag)}/audit`, { params });
  }

  searchByAction(action: string): Observable<AuditEvent[]> {
    const params = new HttpParams().set('action', action);
    return this.http.get<AuditEvent[]>(`${this.base}/search/action`, { params });
  }

  getRealTimeRiskAnalysis(userId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/users/${userId}/risk-analysis`);
  }

  /**
   * Get user audit history
   * GET /api/audit/history/{userId}
   */
  getUserAuditHistory(userId: string): Observable<any> {
    return this.http.get<any>(`/api/audit/history/${userId}`).pipe(
      catchError(() => throwError(() => 'Erreur lors de la récupération de l\'historique d\'audit'))
    );
  }

  /**
   * Get global audit statistics
   * GET /api/audit/stats
   */
  getAuditStats(): Observable<any> {
    return this.http.get<any>(`/api/audit/stats`).pipe(
      catchError(() => throwError(() => 'Erreur lors de la récupération des statistiques d\'audit'))
    );
  }

  /**
   * Health check
   * GET /api/audit/health
   */
  getAuditHealth(): Observable<any> {
    return this.http.get<any>(`/api/audit/health`).pipe(
      catchError(() => throwError(() => 'Erreur lors du health check audit'))
    );
  }
}
