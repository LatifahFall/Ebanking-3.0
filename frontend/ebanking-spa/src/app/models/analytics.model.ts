/**
 * Analytics Models
 * 
 * These models are aligned with the backend Analytics Service DTOs.
 * They correspond exactly to the Java DTOs in:
 * - com.banking.analytics.dto.*
 * - com.banking.analytics.model.Alert
 * 
 * TODO: When connecting to real backend, ensure these match the Java DTOs exactly.
 */

// ============================================================================
// Data Transfer Objects (DTOs)
// ============================================================================

/**
 * DataPoint - Represents a single data point in a time series
 * Corresponds to: com.banking.analytics.dto.DataPoint
 */
export interface DataPoint {
  timestamp: string; // LocalDateTime -> ISO string format
  value: number; // BigDecimal -> number
}

/**
 * RecentTransaction - Represents a recent transaction for display
 * Corresponds to: com.banking.analytics.dto.RecentTransaction
 */
export interface RecentTransaction {
  transactionId: string;
  type: string;
  amount: number; // BigDecimal -> number
  merchant: string;
  date: string; // LocalDateTime -> ISO string format
}

/**
 * CategoryBreakdown - Represents spending breakdown by category
 * Corresponds to: com.banking.analytics.dto.CategoryBreakdown
 */
export interface CategoryBreakdown {
  category: string;
  amount: number; // BigDecimal -> number
  count: number; // Integer -> number
  percentage: number; // Double -> number
}

/**
 * BalanceTrend - Represents balance evolution over time
 * Corresponds to: com.banking.analytics.dto.BalancedTrend.BalanceTrend
 */
export interface BalanceTrend {
  period: string; // e.g., "30 days"
  dataPoints: DataPoint[];
}

/**
 * DashboardSummary - Main dashboard summary for a user
 * Corresponds to: com.banking.analytics.dto.DashboardSummary
 */
export interface DashboardSummary {
  userId: string;
  currentBalance: number; // BigDecimal -> number
  monthlySpending: number; // BigDecimal -> number
  monthlyIncome: number; // BigDecimal -> number
  transactionsThisMonth: number; // Integer -> number
  topCategories?: CategoryBreakdown[]; // Optional list
  balanceTrend?: BalanceTrend; // Optional
  recentTransactions?: RecentTransaction[]; // Optional list
  generatedAt: string; // LocalDateTime -> ISO string format
}

/**
 * AdminOverview - System-wide overview for administrators
 * Corresponds to: com.banking.analytics.dto.AdminOverview
 */
export interface AdminOverview {
  activeUsers: number; // Long -> number
  totalTransactions: number; // Long -> number
  revenue: number; // BigDecimal -> number
}

// ============================================================================
// Alert Model and Enums
// ============================================================================

/**
 * AlertType - Types of alerts that can be triggered
 * Corresponds to: com.banking.analytics.model.Alert.AlertType
 */
export enum AlertType {
  SPENDING_THRESHOLD = 'SPENDING_THRESHOLD',
  LOW_BALANCE = 'LOW_BALANCE',
  UNUSUAL_ACTIVITY = 'UNUSUAL_ACTIVITY',
  BUDGET_EXCEEDED = 'BUDGET_EXCEEDED',
  LARGE_TRANSACTION = 'LARGE_TRANSACTION',
  FREQUENT_TRANSACTIONS = 'FREQUENT_TRANSACTIONS',
  SUSPICIOUS_LOGIN = 'SUSPICIOUS_LOGIN'
}

/**
 * Severity - Alert severity levels
 * Corresponds to: com.banking.analytics.model.Alert.Severity
 */
export enum AlertSeverity {
  INFO = 'INFO',
  WARNING = 'WARNING',
  CRITICAL = 'CRITICAL'
}

/**
 * AlertStatus - Current status of an alert
 * Corresponds to: com.banking.analytics.model.Alert.AlertStatus
 */
export enum AlertStatus {
  ACTIVE = 'ACTIVE',
  RESOLVED = 'RESOLVED',
  IGNORED = 'IGNORED'
}

/**
 * Alert - Represents a system or user alert
 * Corresponds to: com.banking.analytics.model.Alert
 */
export interface Alert {
  alertId: string; // UUID
  userId: string;
  alertType: AlertType;
  severity: AlertSeverity;
  title: string;
  message: string;
  thresholdValue?: number; // BigDecimal -> number, optional
  currentValue?: number; // BigDecimal -> number, optional
  status: AlertStatus;
  triggeredAt: string; // LocalDateTime -> ISO string format
  resolvedAt?: string; // LocalDateTime -> ISO string format, optional (null when active)
  notified: boolean;
}

// ============================================================================
// Helper Types and Utilities
// ============================================================================

/**
 * Period type for spending breakdown queries
 */
export type SpendingPeriod = 'MONTH' | 'WEEK';

/**
 * Helper function to format alert type for display
 */
export function formatAlertType(type: AlertType): string {
  const typeMap: Record<AlertType, string> = {
    [AlertType.SPENDING_THRESHOLD]: 'Spending Threshold',
    [AlertType.LOW_BALANCE]: 'Low Balance',
    [AlertType.UNUSUAL_ACTIVITY]: 'Unusual Activity',
    [AlertType.BUDGET_EXCEEDED]: 'Budget Exceeded',
    [AlertType.LARGE_TRANSACTION]: 'Large Transaction',
    [AlertType.FREQUENT_TRANSACTIONS]: 'Frequent Transactions',
    [AlertType.SUSPICIOUS_LOGIN]: 'Suspicious Login'
  };
  return typeMap[type] || type;
}

/**
 * Helper function to get alert severity color
 */
export function getAlertSeverityColor(severity: AlertSeverity): string {
  const colorMap: Record<AlertSeverity, string> = {
    [AlertSeverity.INFO]: '#3B82F6', // Blue
    [AlertSeverity.WARNING]: '#F59E0B', // Orange
    [AlertSeverity.CRITICAL]: '#EF4444' // Red
  };
  return colorMap[severity] || '#64748B';
}

/**
 * Helper function to check if alert is active
 */
export function isAlertActive(alert: Alert): boolean {
  return alert.status === AlertStatus.ACTIVE;
}

