import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, catchError, map } from 'rxjs';
import { AuditEvent, AuditPage, AuditReportJob } from '../../models';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private base = (localStorage.getItem('API_BASE') || '/api/v1').replace(/\/+$|\s+/g, '');
  private MOCK_EVENTS_KEY = 'mock_audit_events';
  private MOCK_EXPORTS_KEY = 'mock_audit_exports';

  constructor(private http: HttpClient) {}

  private loadMockEvents(): AuditEvent[] {
    try {
      const raw = localStorage.getItem(this.MOCK_EVENTS_KEY);
      if (!raw) return [];
      return JSON.parse(raw) as AuditEvent[];
    } catch { return []; }
  }

  private saveMockEvent(e: AuditEvent) {
    const arr = this.loadMockEvents();
    arr.unshift(e);
    try { localStorage.setItem(this.MOCK_EVENTS_KEY, JSON.stringify(arr)); } catch {}
  }

  createEvent(event: AuditEvent): Observable<AuditEvent> {
    // Try backend, otherwise persist locally for offline testing
    return this.http.post<AuditEvent>(`${this.base}/audit/events`, event).pipe(
      catchError(() => {
        const eWithId: AuditEvent = { ...event, id: `mock-${Date.now()}`, timestamp: event.timestamp || new Date().toISOString() };
        this.saveMockEvent(eWithId);
        return of(eWithId);
      })
    );
  }

  getEvents(page = 0, size = 20, sortBy = 'timestamp', sortDir = 'DESC'): Observable<AuditPage<AuditEvent>> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/events`, { params }).pipe(
      catchError(() => {
        const all = this.loadMockEvents();
        const start = page * size;
        const content = all.slice(start, start + size);
        const totalElements = all.length;
        const totalPages = Math.ceil(totalElements / size);
        return of({ content, totalElements, totalPages, page, size } as AuditPage<AuditEvent>);
      })
    );
  }

  getEventById(eventId: string): Observable<AuditEvent> {
    return this.http.get<AuditEvent>(`${this.base}/audit/events/${eventId}`).pipe(
      catchError(() => {
        const ev = this.loadMockEvents().find(e => e.id === eventId) || null;
        return of(ev as AuditEvent);
      })
    );
  }

  searchEvents(options: { userId?: number; eventType?: string; startDate?: string; endDate?: string; page?: number; size?: number } = {}): Observable<AuditPage<AuditEvent>> {
    let params = new HttpParams();
    if (options.userId != null) params = params.set('userId', String(options.userId));
    if (options.eventType) params = params.set('eventType', options.eventType);
    if (options.startDate) params = params.set('startDate', options.startDate);
    if (options.endDate) params = params.set('endDate', options.endDate);
    params = params.set('page', String(options.page ?? 0)).set('size', String(options.size ?? 20));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/events/search`, { params }).pipe(
      catchError(() => {
        // Simple client-side filter from mock events
        const all = this.loadMockEvents();
        let filtered = all;
        if (options.userId != null) filtered = filtered.filter(f => f.userId === options.userId);
        if (options.eventType) filtered = filtered.filter(f => f.eventType === options.eventType);
        // date filters skipped in mock for brevity
        const page = options.page ?? 0;
        const size = options.size ?? 20;
        const start = page * size;
        const content = filtered.slice(start, start + size);
        const totalElements = filtered.length;
        const totalPages = Math.ceil(totalElements / size);
        return of({ content, totalElements, totalPages, page, size } as AuditPage<AuditEvent>);
      })
    );
  }

  getUserTimeline(userId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.base}/audit/users/${userId}/timeline`).pipe(
      catchError(() => of(this.loadMockEvents().filter(e => e.userId === userId)))
    );
  }

  getUserEvents(userId: number, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/users/${userId}/events`, { params });
  }

  getEventsByType(eventType: string, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/events/type/${encodeURIComponent(eventType)}`, { params });
  }

  getEventsByService(serviceSource: string, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/events/service/${encodeURIComponent(serviceSource)}`, { params });
  }

  getHighRiskEvents(minRiskScore = 0.7, page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams()
      .set('minRiskScore', String(minRiskScore))
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/events/high-risk`, { params });
  }

  getFailedEvents(page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/events/failures`, { params });
  }

  getUserStats(userId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/audit/stats/user/${userId}`);
  }

  exportAuditReport(startDate: string, endDate: string, format = 'PDF'): Observable<AuditReportJob> {
    let params = new HttpParams().set('startDate', startDate).set('endDate', endDate).set('format', format);
    return this.http.post<AuditReportJob>(`${this.base}/audit/export`, null, { params }).pipe(
      catchError(() => {
        // create a mock completed job and store a mock blob
        const jobId = `mock-job-${Date.now()}`;
        const job: AuditReportJob = { jobId, status: 'COMPLETED', downloadUrl: '' };
        try {
          const exportsRaw = localStorage.getItem(this.MOCK_EXPORTS_KEY);
          const exports = exportsRaw ? JSON.parse(exportsRaw) : {};
          exports[jobId] = { created: Date.now(), content: 'Mock audit export' };
          localStorage.setItem(this.MOCK_EXPORTS_KEY, JSON.stringify(exports));
        } catch {}
        return of(job);
      })
    );
  }

  getExportFile(jobId: string): Observable<Blob> {
    return this.http.get(`${this.base}/audit/export/${jobId}`, { responseType: 'blob' }).pipe(
      catchError(() => {
        try {
          const exportsRaw = localStorage.getItem(this.MOCK_EXPORTS_KEY);
          const exports = exportsRaw ? JSON.parse(exportsRaw) : {};
          const item = exports[jobId];
          const text = item ? item.content : 'Mock export content';
          const blob = new Blob([text], { type: 'application/pdf' });
          return of(blob);
        } catch {
          const blob = new Blob(['Mock export content'], { type: 'application/pdf' });
          return of(blob);
        }
      })
    );
  }

  /**
   * Poll the export endpoint until the export file is available, then return the Blob.
   * Falls back to mock stored export if backend not available.
   */
  downloadExportWhenReady(jobId: string, pollInterval = 3000, timeout = 120000): Observable<Blob> {
    return new Observable<Blob>(observer => {
      // First, check local mock storage
      try {
        const exportsRaw = localStorage.getItem(this.MOCK_EXPORTS_KEY);
        const exports = exportsRaw ? JSON.parse(exportsRaw) : {};
        const item = exports[jobId];
        if (item) {
          const blob = new Blob([item.content], { type: 'application/pdf' });
          observer.next(blob);
          observer.complete();
          return;
        }
      } catch {}

      const start = Date.now();
      const attempt = () => {
        this.http.get(`${this.base}/audit/export/${jobId}`, { responseType: 'blob' }).subscribe({
          next: (blob: Blob) => {
            // if backend returns a small error page as blob, still treat it as valid; caller can inspect size/type
            observer.next(blob);
            observer.complete();
          },
          error: (err) => {
            // If not ready, server may return 202/501/404 as error — continue polling until timeout
            const elapsed = Date.now() - start;
            if (elapsed >= timeout) {
              observer.error(new Error('Export timeout'));
              return;
            }
            // schedule next attempt
            setTimeout(attempt, pollInterval);
          }
        });
      };

      // start polling
      attempt();

      // Teardown
      return () => {
        // nothing to cleanup since we use setTimeout chains; HTTP subscriptions complete/cancel automatically
      };
    });
  }

  anonymizeUserData(userId: number): Observable<any> {
    return this.http.post<any>(`${this.base}/audit/users/${userId}/anonymize`, {});
  }

  verifyEventIntegrity(eventId: string): Observable<any> {
    return this.http.post<any>(`${this.base}/audit/verify/${eventId}`, {});
  }

  getUserActivitySummary(userId: number, days = 30): Observable<any> {
    const params = new HttpParams().set('days', String(days));
    return this.http.get<any>(`${this.base}/audit/users/${userId}/activity-summary`, { params });
  }

  getMyEvents(page = 0, size = 20): Observable<AuditPage<AuditEvent>> {
    const params = new HttpParams().set('page', String(page)).set('size', String(size));
    return this.http.get<AuditPage<AuditEvent>>(`${this.base}/audit/my/events`, { params });
  }

  // Monitoring endpoints
  getSuspiciousActivity(userId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.base}/audit/users/${userId}/suspicious`);
  }

  getSecurityReport(userId: number, days = 30): Observable<any> {
    const params = new HttpParams().set('days', String(days));
    return this.http.get<any>(`${this.base}/audit/users/${userId}/security-report`, { params });
  }

  checkMultipleIpLogins(userId: number, hours = 24): Observable<any> {
    const params = new HttpParams().set('hours', String(hours));
    return this.http.get<any>(`${this.base}/audit/users/${userId}/multiple-ip-check`, { params });
  }

  getAbnormalActivity(hours = 24, threshold = 100): Observable<any> {
    const params = new HttpParams().set('hours', String(hours)).set('threshold', String(threshold));
    return this.http.get<any>(`${this.base}/audit/monitoring/abnormal-activity`, { params });
  }

  getComplianceAudit(flag: string, days = 30): Observable<any> {
    const params = new HttpParams().set('days', String(days));
    return this.http.get<any>(`${this.base}/audit/compliance/${encodeURIComponent(flag)}/audit`, { params });
  }

  searchByAction(action: string): Observable<AuditEvent[]> {
    const params = new HttpParams().set('action', action);
    return this.http.get<AuditEvent[]>(`${this.base}/audit/search/action`, { params });
  }

  getRealTimeRiskAnalysis(userId: number): Observable<any> {
    return this.http.get<any>(`${this.base}/audit/users/${userId}/risk-analysis`);
  }
}
