import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, map, of, catchError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Notification } from '../../models';
import { environment } from '../../../environments/environment';

/**
 * Notification Service
 * Manages notifications and alerts (MOCK - no real backend)
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private unreadCountSubject = new BehaviorSubject<number>(0);
  public unreadCount$ = this.unreadCountSubject.asObservable();
  // Utiliser une URL relative pour permettre le proxy Vercel
  private baseUrl = '/api/notifications';

  constructor(private http: HttpClient) {
    this.updateUnreadCount();
  }

  /**
   * Get all notifications
   */
  getNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(this.baseUrl);
  }

  /**
   * Send a single notification
   * POST /api/notifications
   */
  sendNotification(payload: Partial<Notification>): Observable<any> {
    return this.http.post<any>(this.baseUrl, payload);
  }

  /**
   * Send bulk notifications (broadcast)
   * POST /api/notifications/bulk
   */
  sendBulkNotifications(payload: Partial<Notification>[]): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/bulk`, payload);
  }

  /**
   * Get user notification history
   * GET /api/notifications/user/{userId}
   */
  getUserNotifications(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/user/${userId}`);
  }

  /**
   * Get unread IN_APP notifications
   * GET /api/notifications/in-app/{userId}
   */
  getInAppNotifications(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/in-app/${userId}`).pipe(
      map(notifications => notifications.filter(n => !n.read))
    );
  }

  /**
   * Get pending notifications (monitoring)
   * GET /api/notifications/pending
   */
  getPendingNotifications(): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.baseUrl}/pending`);
  }

  /**
   * Get unread notification count
   */
  getUnreadCount(): Observable<number> {
    return new Observable<number>(observer => {
      this.getNotifications().subscribe(notifications => {
        const count = (notifications || []).filter(n => !n.read).length;
        observer.next(count);
        observer.complete();
      }, () => { observer.next(0); observer.complete(); });
    });
  }

  /**
   * Mark notification as read
   */
  markAsRead(notificationId: string): Observable<boolean> {
    return this.http.put<boolean>(`${this.baseUrl}/${notificationId}/read`, {}).pipe(
      map(() => { this.updateUnreadCount(); return true; }),
      catchError(() => { this.updateUnreadCount(); return of(true); })
    );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<boolean> {
    this.unreadCountSubject.next(0);
    return of(true).pipe(catchError(() => of(false)));
  }

  /**
   * Update unread count
   */
  private updateUnreadCount(): void {
    this.getUnreadCount().subscribe(count => {
      this.unreadCountSubject.next(count);
    });
  }

  /**
   * Get user notification preferences
   * GET /api/notifications/preferences/{userId}
   */
  getUserPreferences(userId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/preferences/${userId}`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Update user notification preferences
   * PUT /api/notifications/preferences/{userId}
   */
  updateUserPreferences(userId: string, preferences: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/preferences/${userId}`, preferences).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Toggle all notifications (enable/disable)
   * PUT /api/notifications/preferences/{userId}/toggle-all?enabled={true|false}
   */
  toggleAllNotifications(userId: string, enabled: boolean): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/preferences/${userId}/toggle-all`, null, {
      params: { enabled: String(enabled) }
    }).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Toggle Do Not Disturb mode
   * PUT /api/notifications/preferences/{userId}/do-not-disturb?enabled={true|false}
   */
  toggleDoNotDisturb(userId: string, enabled: boolean): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/preferences/${userId}/do-not-disturb`, null, {
      params: { enabled: String(enabled) }
    }).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Get user audit history (notifications)
   * GET /api/notifications/audit/{userId}
   */
  getUserNotificationAudit(userId: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/audit/${userId}`).pipe(
      catchError(() => of([]))
    );
  }

  /**
   * Get global notification statistics
   * GET /api/notifications/stats
   */
  getNotificationStats(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Health check notifications
   * GET /api/notifications/health
   */
  getNotificationHealth(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/health`).pipe(
      catchError(() => of(null))
    );
  }
}
