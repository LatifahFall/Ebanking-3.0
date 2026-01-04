/**
 * Crypto Models
 * Represents crypto wallet, transactions, holdings, and market data
 */

export interface CryptoWallet {
  id: number;
  userId: number;
  status: WalletStatus;
  balance: number; // EUR balance
  createdAt: string;
  updatedAt: string;
}

export enum WalletStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE'
}

export interface CryptoTransaction {
  id: number;
  walletId: number;
  type: TransactionType;
  cryptoSymbol: string;
  cryptoAmount: number;
  eurAmount: number;
  eurPricePerUnit: number;
  fee: number;
  status: TransactionStatus;
  createdAt: string;
}

export enum TransactionType {
  BUY = 'BUY',
  SELL = 'SELL'
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
}

export interface CryptoHolding {
  walletId: number;
  cryptoSymbol: string;
  amount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CryptoCoin {
  id: string;
  symbol: string;
  name: string;
  image?: string;
  current_price: number;
  market_cap: number;
  market_cap_rank: number;
  fully_diluted_valuation?: number;
  total_volume: number;
  high_24h?: number;
  low_24h?: number;
  price_change_24h?: number;
  price_change_percentage_24h?: number;
  market_cap_change_24h?: number;
  market_cap_change_percentage_24h?: number;
  circulating_supply?: number;
  total_supply?: number;
  max_supply?: number;
  ath?: number;
  ath_change_percentage?: number;
  ath_date?: string;
  atl?: number;
  atl_change_percentage?: number;
  atl_date?: string;
  last_updated?: string;
}

export interface CryptoHoldingWithPrice extends CryptoHolding {
  currentPrice: number;
  valueInEUR: number;
  change24h: number;
  change24hPercent: number;
  coinName?: string;
  coinImage?: string;
}

export interface CryptoPortfolio {
  wallet: CryptoWallet;
  holdings: CryptoHoldingWithPrice[];
  totalValueEUR: number;
  totalGainLoss: number;
  totalGainLossPercent: number;
  transactions: CryptoTransaction[];
}

export interface BuyCryptoRequest {
  symbol: string;
  eurAmount: number;
}

export interface SellCryptoRequest {
  symbol: string;
  cryptoAmount: number;
}

export interface ConvertCryptoRequest {
  fromSymbol: string;
  toSymbol: string;
  amount: number; // Amount in fromSymbol
}

