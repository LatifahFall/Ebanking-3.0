import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuditEvent } from '../../models/audit.model';

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly baseUrl = environment.auditServiceUrl;

  constructor(private http: HttpClient) {}

  createAuditEvent(event: Partial<AuditEvent>): Observable<AuditEvent> {
    return this.http.post<AuditEvent>(`${this.baseUrl}/events`, event);
  }

  getAllEvents(page = 0, size = 20, sortBy = 'timestamp', sortDir = 'DESC'): Observable<any> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    return this.http.get<any>(`${this.baseUrl}/events`, { params });
  }

  getEventById(eventId: string): Observable<AuditEvent> {
    return this.http.get<AuditEvent>(`${this.baseUrl}/events/${eventId}`);
  }

  searchEvents(options: {
    userId?: number;
    eventType?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
  }): Observable<any> {
    let params = new HttpParams();
    if (options.userId) params = params.set('userId', options.userId);
    if (options.eventType) params = params.set('eventType', options.eventType);
    if (options.startDate) params = params.set('startDate', options.startDate);
    if (options.endDate) params = params.set('endDate', options.endDate);
    params = params.set('page', options.page?.toString() || '0');
    params = params.set('size', options.size?.toString() || '20');
    return this.http.get<any>(`${this.baseUrl}/events/search`, { params });
  }

  getUserTimeline(userId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.baseUrl}/users/${userId}/timeline`);
  }

  getUserEvents(userId: number, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<any>(`${this.baseUrl}/users/${userId}/events`, { params });
  }

  getEventsByType(eventType: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<any>(`${this.baseUrl}/events/type/${eventType}`, { params });
  }

  getEventsByService(serviceSource: string, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<any>(`${this.baseUrl}/events/service/${serviceSource}`, { params });
  }

  getHighRiskEvents(minRiskScore = 0.7, page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('minRiskScore', minRiskScore).set('page', page).set('size', size);
    return this.http.get<any>(`${this.baseUrl}/events/high-risk`, { params });
  }

  getFailedEvents(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<any>(`${this.baseUrl}/events/failures`, { params });
  }

  getUserStats(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats/user/${userId}`);
  }

  getTypeStats(eventType: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats/type/${eventType}`);
  }

  /**
   * Récupère le rapport de conformité (compliance report)
   */
  getComplianceReport(startDate: string, endDate: string, regulation: string = 'GDPR'): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('regulation', regulation);
    return this.http.get<any>(`${this.baseUrl}/compliance/report`, { params });
  }

  /**
   * Lance l'export d'audit (audit export)
   */
  exportAuditReport(startDate: string, endDate: string, format: string = 'PDF'): Observable<any> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('format', format);
    return this.http.post<any>(`${this.baseUrl}/export`, null, { params });
  }

  /**
   * Récupère le fichier exporté (audit export file)
   */
  getExportFile(jobId: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export/${jobId}`, { responseType: 'blob' });
  }

  anonymizeUserData(userId: number): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/users/${userId}/anonymize`, {});
  }

  verifyEventIntegrity(eventId: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/verify/${eventId}`, {});
  }

  getUserActivitySummary(userId: number, days = 30): Observable<any> {
    const params = new HttpParams().set('days', days);
    return this.http.get<any>(`${this.baseUrl}/users/${userId}/activity-summary`, { params });
  }

  getMyEvents(page = 0, size = 20): Observable<any> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<any>(`${this.baseUrl}/my/events`, { params });
  }

  // Monitoring endpoints
  getSuspiciousActivity(userId: number): Observable<AuditEvent[]> {
    return this.http.get<AuditEvent[]>(`${this.baseUrl}/users/${userId}/suspicious`);
  }

  getSecurityReport(userId: number, days = 30): Observable<any> {
    const params = new HttpParams().set('days', days);
    return this.http.get<any>(`${this.baseUrl}/users/${userId}/security-report`, { params });
  }

  checkMultipleIpLogins(userId: number, hours = 24): Observable<any> {
    const params = new HttpParams().set('hours', hours);
    return this.http.get<any>(`${this.baseUrl}/users/${userId}/multiple-ip-check`, { params });
  }

  getAbnormalActivity(hours = 24, threshold = 100): Observable<any> {
    const params = new HttpParams().set('hours', hours).set('threshold', threshold);
    return this.http.get<any>(`${this.baseUrl}/monitoring/abnormal-activity`, { params });
  }

  getComplianceAudit(flag: string, days = 30): Observable<any> {
    const params = new HttpParams().set('days', days);
    return this.http.get<any>(`${this.baseUrl}/compliance/${flag}/audit`, { params });
  }

  searchByAction(action: string): Observable<AuditEvent[]> {
    const params = new HttpParams().set('action', action);
    return this.http.get<AuditEvent[]>(`${this.baseUrl}/search/action`, { params });
  }

  getRealTimeRiskAnalysis(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/users/${userId}/risk-analysis`);
  }

  /**
   * Récupère l'historique d'audit de notifications pour un utilisateur
   */
  getUserNotificationAudit(userId: string | number): Observable<any> {
    return this.http.get<any>(`${environment.notificationServiceUrl}/audit/${userId}`);
  }

  /**
   * Récupère les statistiques globales de notifications
   */
  getNotificationStats(): Observable<any> {
    return this.http.get<any>(`${environment.notificationServiceUrl}/stats`);
  }

  /**
   * Vérifie la santé du service de notifications
   */
  getNotificationHealth(): Observable<any> {
    return this.http.get<any>(`${environment.notificationServiceUrl}/health`);
  }
}
