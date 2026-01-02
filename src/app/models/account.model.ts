/**
 * Account Model
 * Represents bank accounts (checking, savings, crypto wallet)
 */
export interface Account {
  id: string;
  accountNumber: string;
  accountType: AccountType;
  accountName: string;
  currency: string;
  balance: number;
  availableBalance: number;
  status: AccountStatus;
  iban?: string;
  bic?: string;
  createdAt: Date;
  lastActivity?: Date;
  userId: string;
  icon?: string;
  color?: string;
}

export enum AccountType {
  CHECKING = 'CHECKING',
  SAVINGS = 'SAVINGS',
  CRYPTO = 'CRYPTO',
  INVESTMENT = 'INVESTMENT',
  CREDIT = 'CREDIT'
}

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  FROZEN = 'FROZEN',
  CLOSED = 'CLOSED',
  PENDING = 'PENDING'
}

export interface CryptoAsset {
  id: string;
  symbol: string;
  name: string;
  balance: number;
  valueInUSD: number;
  valueInFiat: number;
  change24h: number;
  icon?: string;
  color?: string;
}

export interface AccountSummary {
  totalBalance: number;
  totalIncome: number;
  totalExpense: number;
  accounts: Account[];
  cryptoAssets?: CryptoAsset[];
}
