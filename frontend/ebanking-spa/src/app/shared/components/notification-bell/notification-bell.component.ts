import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { NotificationService } from '../../../core/services/notification.service';
import { Notification } from '../../../models';

@Component({
  selector: 'app-notification-bell',
  imports: [
    CommonModule, 
    RouterModule, 
    MatIconModule, 
    MatBadgeModule, 
    MatMenuModule,
    MatButtonModule
  ],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.scss'
})
export class NotificationBellComponent implements OnInit {
  unreadCount = 0;
  notifications: Notification[] = [];

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.unreadCount$.subscribe(count => {
      this.unreadCount = count;
    });

    this.loadNotifications();
  }

  loadNotifications(): void {
    this.notificationService.getNotifications().subscribe(notifications => {
      this.notifications = notifications.slice(0, 5); // Show only 5 most recent
    });
  }

  markAsRead(notificationId: string): void {
    this.notificationService.markAsRead(notificationId).subscribe(() => {
      this.loadNotifications();
    });
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead().subscribe();
  }

  getNotificationIcon(notification: Notification): string {
    return notification.icon || 'notifications';
  }

  getTimeAgo(date: Date): string {
    const now = new Date();
    const diff = now.getTime() - new Date(date).getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    return `${days}d ago`;
  }
}

