/**
 * Audit Models
 * Models for audit events and logging
 */

// Event Types
export enum EventType {
  USER_LOGIN = 'USER_LOGIN',
  USER_LOGOUT = 'USER_LOGOUT',
  USER_REGISTER = 'USER_REGISTER',
  USER_UPDATE = 'USER_UPDATE',
  USER_DELETE = 'USER_DELETE',
  ACCOUNT_CREATE = 'ACCOUNT_CREATE',
  ACCOUNT_UPDATE = 'ACCOUNT_UPDATE',
  ACCOUNT_DELETE = 'ACCOUNT_DELETE',
  TRANSACTION_CREATE = 'TRANSACTION_CREATE',
  TRANSACTION_UPDATE = 'TRANSACTION_UPDATE',
  PAYMENT_CREATE = 'PAYMENT_CREATE',
  PAYMENT_APPROVED = 'PAYMENT_APPROVED',
  PAYMENT_REJECTED = 'PAYMENT_REJECTED',
  PASSWORD_CHANGE = 'PASSWORD_CHANGE',
  PASSWORD_RESET = 'PASSWORD_RESET',
  SECURITY_ALERT = 'SECURITY_ALERT',
  ACCESS_DENIED = 'ACCESS_DENIED',
  SUSPICIOUS_ACTIVITY = 'SUSPICIOUS_ACTIVITY'
}

// Audit Event
export interface AuditEvent {
  id?: string;
  eventType: string;
  userId?: number | null;
  username?: string;
  timestamp?: string;
  service?: string;
  riskScore?: number;
  details?: any;
  ipAddress?: string;
  userAgent?: string;
  success?: boolean;
}

// Audit Page (for pagination)
export interface AuditPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

// Audit Report Job
export interface AuditReportJob {
  jobId: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  downloadUrl?: string;
  createdAt?: string;
  completedAt?: string;
}
