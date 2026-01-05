export enum EventType {
  USER_LOGIN = 'USER_LOGIN',
  USER_LOGOUT = 'USER_LOGOUT',
  USER_REGISTER = 'USER_REGISTER',
  PAYMENT_INITIATED = 'PAYMENT_INITIATED',
  PAYMENT_FAILED = 'PAYMENT_FAILED',
  TRANSACTION_CREATED = 'TRANSACTION_CREATED',
  TRANSACTION_SUSPICIOUS = 'TRANSACTION_SUSPICIOUS',
  PROFILE_UPDATED = 'PROFILE_UPDATED',
  DATA_ANONYMIZED = 'DATA_ANONYMIZED',
  SYSTEM_ALERT = 'SYSTEM_ALERT'
}

export enum AuditResult {
  SUCCESS = 'SUCCESS',
  FAILURE = 'FAILURE',
  PENDING = 'PENDING'
}

export interface AuditEvent {
  id?: string; // UUID
  userId?: number | null;
  username?: string | null;
  eventType: EventType | string;
  result?: AuditResult | string;
  details?: any;
  timestamp?: string;
  ip?: string;
  service?: string;
  riskScore?: number;
  metadata?: Record<string, any>;
}

export interface AuditPage<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface AuditSearchParams {
  userId?: number;
  eventType?: EventType | string;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export interface AuditReportJob {
  jobId: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  downloadUrl?: string;
}
