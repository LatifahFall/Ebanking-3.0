import { Injectable } from '@angular/core';
import { Observable, of, delay, BehaviorSubject, catchError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { Notification, NotificationType, NotificationPriority } from '../../models';
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
  private readonly baseUrl = environment.notificationServiceUrl;
  private readonly useMock = environment.useMock;

  constructor(private http: HttpClient) {
    this.updateUnreadCount();
  }

  /**
   * Get all notifications
   */
  getNotifications(): Observable<Notification[]> {
    if (this.useMock) {
      return this.getNotificationsMock();
    }
    return this.http.get<Notification[]>(this.baseUrl).pipe(
      catchError(() => {
        // Fallback to mock
        return this.getNotificationsMock();
      })
    );
  }

  /**
   * Get all notifications (mock data)
   */
  private getNotificationsMock(): Observable<Notification[]> {
    const mockNotifications: Notification[] = [
      {
        id: 'notif-001',
        type: NotificationType.TRANSACTION,
        priority: NotificationPriority.MEDIUM,
        title: 'Payment Received',
        message: 'You received $5,500.00 from Tech Corp Inc.',
        read: false,
        timestamp: new Date('2025-12-31T09:00:00'),
        userId: 'usr-123456',
        icon: 'account_balance',
        color: '#10B981'
      },
      {
        id: 'notif-002',
        type: NotificationType.SECURITY,
        priority: NotificationPriority.HIGH,
        title: 'New Device Login',
        message: 'Login detected from new device in New York, NY',
        read: false,
        actionUrl: '/profile/security',
        actionLabel: 'Review Activity',
        timestamp: new Date('2025-12-30T22:15:00'),
        userId: 'usr-123456',
        icon: 'security',
        color: '#F59E0B'
      },
      {
        id: 'notif-003',
        type: NotificationType.ACCOUNT,
        priority: NotificationPriority.LOW,
        title: 'Monthly Statement Ready',
        message: 'Your December statement is now available',
        read: true,
        actionUrl: '/accounts',
        actionLabel: 'View Statement',
        timestamp: new Date('2025-12-30T08:00:00'),
        userId: 'usr-123456',
        icon: 'description',
        color: '#6B7280'
      },
      {
        id: 'notif-004',
        type: NotificationType.ALERT,
        priority: NotificationPriority.URGENT,
        title: 'Large Transaction Alert',
        message: 'A transaction of $1,000.00 was processed',
        read: false,
        timestamp: new Date('2025-12-30T14:20:00'),
        userId: 'usr-123456',
        icon: 'warning',
        color: '#EF4444'
      },
      {
        id: 'notif-005',
        type: NotificationType.PROMOTION,
        priority: NotificationPriority.LOW,
        title: 'Special Offer',
        message: 'Get 2% cashback on all purchases this month',
        read: true,
        timestamp: new Date('2025-12-28T10:00:00'),
        userId: 'usr-123456',
        icon: 'local_offer',
        color: '#8B5CF6'
      }
    ];

    return of(mockNotifications).pipe(delay(300));
  }

  /**
   * Get unread notification count
   */
  getUnreadCount(): Observable<number> {
    return new Observable(observer => {
      this.getNotifications().subscribe(notifications => {
        const count = notifications.filter(n => !n.read).length;
        observer.next(count);
        observer.complete();
      });
    });
  }

  /**
   * Mark notification as read
   */
  markAsRead(notificationId: string): Observable<boolean> {
    if (this.useMock) {
      this.updateUnreadCount();
      return of(true).pipe(delay(200));
    }
    return this.http.put<boolean>(`${this.baseUrl}/${notificationId}/read`, {}).pipe(
      catchError(() => {
        this.updateUnreadCount();
        return of(true).pipe(delay(200));
      })
    );
  }

  /**
   * Mark all notifications as read
   */
  markAllAsRead(): Observable<boolean> {
    this.unreadCountSubject.next(0);
    return of(true).pipe(delay(200));
  }

  /**
   * Update unread count
   */
  private updateUnreadCount(): void {
    this.getUnreadCount().subscribe(count => {
      this.unreadCountSubject.next(count);
    });
  }
}
