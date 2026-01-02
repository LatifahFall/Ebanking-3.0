/**
 * Payment Models (matching backend DTOs)
 */

export enum PaymentType {
  TRANSFER = 'TRANSFER',
  BILL_PAYMENT = 'BILL_PAYMENT',
  MOBILE_TOP_UP = 'MOBILE_TOP_UP',
  CARD_PAYMENT = 'CARD_PAYMENT'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REVERSED = 'REVERSED'
}

export interface PaymentRequest {
  fromAccountId: string; // UUID
  toAccountId?: string;  // UUID
  amount: number;        // BigDecimal on backend
  currency: string;
  paymentType: PaymentType;
  beneficiaryName?: string;
  reference?: string;
  description?: string;
}

export interface PaymentResponse {
  id: string; // UUID
  fromAccountId: string;
  toAccountId?: string;
  amount: number;
  currency: string;
  paymentType: PaymentType;
  status: PaymentStatus;
  beneficiaryName?: string;
  reference?: string;
  description?: string;
  createdAt: string;
  completedAt?: string;
}

export interface PaymentListResponse {
  payments: PaymentResponse[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}