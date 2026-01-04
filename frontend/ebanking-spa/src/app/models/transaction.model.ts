/**
 * Transaction Model
 * Represents financial transactions in the system
 */
export interface Transaction {
  id: string;
  type: TransactionType;
  category: TransactionCategory;
  amount: number;
  currency: string;
  description: string;
  status: TransactionStatus;
  date: Date;
  fromAccount?: string;
  toAccount?: string;
  reference?: string;
  merchant?: string;
  location?: string;
  icon?: string;
  color?: string;
  userId: string;
}

export enum TransactionType {
  DEBIT = 'DEBIT',
  CREDIT = 'CREDIT',
  TRANSFER = 'TRANSFER',
  PAYMENT = 'PAYMENT',
  WITHDRAWAL = 'WITHDRAWAL',
  DEPOSIT = 'DEPOSIT',
  CRYPTO_BUY = 'CRYPTO_BUY',
  CRYPTO_SELL = 'CRYPTO_SELL'
}

export enum TransactionCategory {
  SALARY = 'SALARY',
  SHOPPING = 'SHOPPING',
  FOOD = 'FOOD',
  TRANSPORT = 'TRANSPORT',
  UTILITIES = 'UTILITIES',
  ENTERTAINMENT = 'ENTERTAINMENT',
  HEALTHCARE = 'HEALTHCARE',
  EDUCATION = 'EDUCATION',
  INVESTMENT = 'INVESTMENT',
  TRANSFER = 'TRANSFER',
  OTHER = 'OTHER'
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  PROCESSING = 'PROCESSING'
}

export interface TransactionFilter {
  startDate?: Date;
  endDate?: Date;
  type?: TransactionType;
  category?: TransactionCategory;
  status?: TransactionStatus;
  minAmount?: number;
  maxAmount?: number;
  accountId?: string;
}
