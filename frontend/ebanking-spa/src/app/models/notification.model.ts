/**
 * Notification Model
 * Represents system notifications and alerts
 */
export interface Notification {
  id: string;
  type: NotificationType;
  priority: NotificationPriority;
  title: string;
  message: string;
  read: boolean;
  actionUrl?: string;
  actionLabel?: string;
  timestamp: Date;
  userId: string;
  icon?: string;
  color?: string;
}

export enum NotificationType {
  TRANSACTION = 'TRANSACTION',
  SECURITY = 'SECURITY',
  ACCOUNT = 'ACCOUNT',
  SYSTEM = 'SYSTEM',
  PROMOTION = 'PROMOTION',
  ALERT = 'ALERT'
}

export enum NotificationPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export interface NotificationSettings {
  enabled: boolean;
  types: NotificationType[];
  channels: NotificationChannel[];
}

export enum NotificationChannel {
  EMAIL = 'EMAIL',
  SMS = 'SMS',
  PUSH = 'PUSH',
  IN_APP = 'IN_APP'
}
