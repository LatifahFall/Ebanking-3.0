/**
 * Payment Models (matching backend DTOs)
 */

export enum PaymentType {
  STANDARD = 'STANDARD',
  INSTANT = 'INSTANT',
  RECURRING = 'RECURRING',
  BIOMETRIC = 'BIOMETRIC',
  QR_CODE = 'QR_CODE'
  // Note: Backend PaymentType enum only has these values
  // TRANSFER, BILL_PAYMENT, MOBILE_TOP_UP, CARD_PAYMENT are not in backend enum
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
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

export interface QRCodePaymentRequest {
  fromAccountId: string;
  toAccountId?: string;
  amount: number;
  currency: string;
  beneficiaryName?: string;
  reference?: string;
  description?: string;
  qrCodeData: string; // Scanned QR code content
}

export interface QRCodeResponse {
  qrCode: string; // Base64 encoded PNG image
  message: string;
  format: string;
}
