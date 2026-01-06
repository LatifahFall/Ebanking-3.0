import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, delay, catchError, map, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CryptoWallet,
  CryptoTransaction,
  CryptoHolding,
  CryptoCoin,
  CryptoHoldingWithPrice,
  CryptoPortfolio,
  BuyCryptoRequest,
  SellCryptoRequest,
  ConvertCryptoRequest,
  WalletStatus,
  TransactionType,
  TransactionStatus
} from '../../models/crypto.model';

/**
 * Crypto Service
 * Manages crypto wallet, transactions, holdings, and market data
 * Supports both mock and real API calls
 */
@Injectable({
  providedIn: 'root'
})
export class CryptoService {
  private readonly API_BASE_URL = environment.cryptoServiceUrl;
  private useMock = environment.useMock;

  // Mock data
  private mockWallet: CryptoWallet = {
    id: 1,
    userId: 1,
    status: WalletStatus.ACTIVE,
    balance: 10000.00,
    createdAt: new Date('2024-01-01').toISOString(),
    updatedAt: new Date().toISOString()
  };

  private mockHoldings: CryptoHolding[] = [
    { walletId: 1, cryptoSymbol: 'BTC', amount: 0.0123, createdAt: new Date('2024-01-15').toISOString(), updatedAt: new Date().toISOString() },
    { walletId: 1, cryptoSymbol: 'ETH', amount: 0.45, createdAt: new Date('2024-02-01').toISOString(), updatedAt: new Date().toISOString() },
    { walletId: 1, cryptoSymbol: 'USDT', amount: 150.00, createdAt: new Date('2024-02-10').toISOString(), updatedAt: new Date().toISOString() }
  ];

  private mockTransactions: CryptoTransaction[] = [
    {
      id: 1,
      walletId: 1,
      type: TransactionType.BUY,
      cryptoSymbol: 'BTC',
      cryptoAmount: 0.0123,
      eurAmount: 500.00,
      eurPricePerUnit: 40650.41,
      fee: 5.00,
      status: TransactionStatus.COMPLETED,
      createdAt: new Date('2024-01-15').toISOString()
    },
    {
      id: 2,
      walletId: 1,
      type: TransactionType.BUY,
      cryptoSymbol: 'ETH',
      cryptoAmount: 0.45,
      eurAmount: 1000.00,
      eurPricePerUnit: 2222.22,
      fee: 10.00,
      status: TransactionStatus.COMPLETED,
      createdAt: new Date('2024-02-01').toISOString()
    },
    {
      id: 3,
      walletId: 1,
      type: TransactionType.BUY,
      cryptoSymbol: 'USDT',
      cryptoAmount: 150.00,
      eurAmount: 150.00,
      eurPricePerUnit: 1.00,
      fee: 1.50,
      status: TransactionStatus.COMPLETED,
      createdAt: new Date('2024-02-10').toISOString()
    }
  ];

  private mockCoins: CryptoCoin[] = [
    {
      id: 'bitcoin',
      symbol: 'btc',
      name: 'Bitcoin',
      image: 'https://assets.coingecko.com/coins/images/1/large/bitcoin.png',
      current_price: 40650.41,
      market_cap: 798234567890,
      market_cap_rank: 1,
      total_volume: 12345678901,
      price_change_percentage_24h: 2.5,
      market_cap_change_percentage_24h: 2.3
    },
    {
      id: 'ethereum',
      symbol: 'eth',
      name: 'Ethereum',
      image: 'https://assets.coingecko.com/coins/images/279/large/ethereum.png',
      current_price: 2222.22,
      market_cap: 267123456789,
      market_cap_rank: 2,
      total_volume: 5678901234,
      price_change_percentage_24h: -1.2,
      market_cap_change_percentage_24h: -1.1
    },
    {
      id: 'tether',
      symbol: 'usdt',
      name: 'Tether',
      image: 'https://assets.coingecko.com/coins/images/325/large/Tether.png',
      current_price: 1.00,
      market_cap: 95123456789,
      market_cap_rank: 3,
      total_volume: 23456789012,
      price_change_percentage_24h: 0.01,
      market_cap_change_percentage_24h: 0.01
    }
  ];

  constructor(private http: HttpClient) {}

  // ========== Wallet Operations ==========

  /**
   * Get wallet by user ID
   * GET /api/wallets/user/{userId}
   */
  getWalletByUserId(userId: number): Observable<CryptoWallet> {
    if (this.useMock) {
      return of(this.mockWallet).pipe(delay(300));
    }
    return this.http.get<CryptoWallet>(`${this.API_BASE_URL}/wallets/user/${userId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Create wallet for user
   * POST /api/wallets?userId={userId}
   */
  createWallet(userId: number): Observable<CryptoWallet> {
    if (this.useMock) {
      const newWallet = { ...this.mockWallet, id: Date.now(), userId };
      return of(newWallet).pipe(delay(500));
    }
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.post<CryptoWallet>(`${this.API_BASE_URL}/wallets`, null, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Activate wallet
   * PATCH /api/wallets/activate?userId={userId}
   */
  activateWallet(userId: number): Observable<CryptoWallet> {
    if (this.useMock) {
      const activated = { ...this.mockWallet, status: WalletStatus.ACTIVE };
      return of(activated).pipe(delay(300));
    }
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.patch<CryptoWallet>(`${this.API_BASE_URL}/wallets/activate`, null, { params })
      .pipe(catchError(this.handleError));
  }

  /**
   * Deactivate wallet
   * PATCH /api/wallets/deactivate?userId={userId}
   */
  deactivateWallet(userId: number): Observable<CryptoWallet> {
    if (this.useMock) {
      const deactivated = { ...this.mockWallet, status: WalletStatus.INACTIVE };
      return of(deactivated).pipe(delay(300));
    }
    const params = new HttpParams().set('userId', userId.toString());
    return this.http.patch<CryptoWallet>(`${this.API_BASE_URL}/wallets/deactivate`, null, { params })
      .pipe(catchError(this.handleError));
  }

  // ========== Holdings Operations ==========

  /**
   * Get holdings by wallet ID
   * GET /api/holdings/wallet/{walletId}
   */
  getHoldingsByWallet(walletId: number): Observable<CryptoHolding[]> {
    if (this.useMock) {
      return of(this.mockHoldings.filter(h => h.walletId === walletId)).pipe(delay(300));
    }
    return this.http.get<CryptoHolding[]>(`${this.API_BASE_URL}/holdings/wallet/${walletId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get holdings with current prices
   */
  getHoldingsWithPrices(walletId: number): Observable<CryptoHoldingWithPrice[]> {
    return this.getHoldingsByWallet(walletId).pipe(
      map(holdings => {
        return holdings.map(holding => {
          const coin = this.mockCoins.find(c => c.symbol.toUpperCase() === holding.cryptoSymbol.toUpperCase());
          const currentPrice = coin?.current_price || 0;
          const valueInEUR = holding.amount * currentPrice;
          const change24h = coin?.price_change_percentage_24h || 0;

          return {
            ...holding,
            currentPrice,
            valueInEUR,
            change24h,
            change24hPercent: change24h,
            coinName: coin?.name,
            coinImage: coin?.image
          };
        });
      })
    );
  }

  // ========== Transaction Operations ==========

  /**
   * Get transactions by wallet ID
   * GET /api/transactions/wallet/{walletId}
   */
  getTransactionsByWallet(walletId: number): Observable<CryptoTransaction[]> {
    if (this.useMock) {
      return of(this.mockTransactions.filter(t => t.walletId === walletId)).pipe(delay(300));
    }
    return this.http.get<CryptoTransaction[]>(`${this.API_BASE_URL}/transactions/wallet/${walletId}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Buy crypto
   * NOTE: Backend endpoint does not exist yet - using mock only
   * TODO: Implement POST /api/transactions/buy in backend
   */
  buyCrypto(walletId: number, request: BuyCryptoRequest): Observable<CryptoTransaction> {
    // Backend endpoint does not exist - using mock only
    const coin = this.mockCoins.find(c => c.symbol.toUpperCase() === request.symbol.toUpperCase());
    const pricePerUnit = coin?.current_price || 0;
    const cryptoAmount = request.eurAmount / pricePerUnit;
    const fee = request.eurAmount * 0.001; // 0.1% fee

    const transaction: CryptoTransaction = {
      id: Date.now(),
      walletId,
      type: TransactionType.BUY,
      cryptoSymbol: request.symbol.toUpperCase(),
      cryptoAmount,
      eurAmount: request.eurAmount,
      eurPricePerUnit: pricePerUnit,
      fee,
      status: TransactionStatus.COMPLETED,
      createdAt: new Date().toISOString()
    };

    // Update mock holdings
    const existingHolding = this.mockHoldings.find(h => h.walletId === walletId && h.cryptoSymbol === request.symbol.toUpperCase());
    if (existingHolding) {
      existingHolding.amount += cryptoAmount;
      existingHolding.updatedAt = new Date().toISOString();
    } else {
      this.mockHoldings.push({
        walletId,
        cryptoSymbol: request.symbol.toUpperCase(),
        amount: cryptoAmount,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      });
    }

    this.mockTransactions.unshift(transaction);
    return of(transaction).pipe(delay(500));
  }

  /**
   * Sell crypto
   * NOTE: Backend endpoint does not exist yet - using mock only
   * TODO: Implement POST /api/transactions/sell in backend
   */
  sellCrypto(walletId: number, request: SellCryptoRequest): Observable<CryptoTransaction> {
    // Backend endpoint does not exist - using mock only
    const coin = this.mockCoins.find(c => c.symbol.toUpperCase() === request.symbol.toUpperCase());
    const pricePerUnit = coin?.current_price || 0;
    const eurAmount = request.cryptoAmount * pricePerUnit;
    const fee = eurAmount * 0.001; // 0.1% fee

    const transaction: CryptoTransaction = {
      id: Date.now(),
      walletId,
      type: TransactionType.SELL,
      cryptoSymbol: request.symbol.toUpperCase(),
      cryptoAmount: request.cryptoAmount,
      eurAmount,
      eurPricePerUnit: pricePerUnit,
      fee,
      status: TransactionStatus.COMPLETED,
      createdAt: new Date().toISOString()
    };

    // Update mock holdings
    const existingHolding = this.mockHoldings.find(h => h.walletId === walletId && h.cryptoSymbol === request.symbol.toUpperCase());
    if (existingHolding) {
      existingHolding.amount -= request.cryptoAmount;
      existingHolding.updatedAt = new Date().toISOString();
    }

    this.mockTransactions.unshift(transaction);
    return of(transaction).pipe(delay(500));
  }

  // ========== Market Data Operations ==========

  /**
   * Get all supported coins details
   * GET /api/coins/details
   */
  getCoinsDetails(): Observable<CryptoCoin[]> {
    if (this.useMock) {
      return of(this.mockCoins).pipe(delay(300));
    }
    return this.http.get<CryptoCoin[]>(`${this.API_BASE_URL}/coins/details`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get coin prices
   * GET /api/coins/prices
   */
  getCoinsPrices(): Observable<Record<string, { eur: number }>> {
    if (this.useMock) {
      const prices: Record<string, { eur: number }> = {};
      this.mockCoins.forEach(coin => {
        prices[coin.id] = { eur: coin.current_price };
      });
      return of(prices).pipe(delay(300));
    }
    return this.http.get<Record<string, { eur: number }>>(`${this.API_BASE_URL}/coins/prices`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get coin by ID
   * NOTE: Backend endpoint does not exist yet - using mock only
   * TODO: Implement GET /api/coins/{coinId} in backend or filter from getCoinsDetails()
   */
  getCoinById(coinId: string): Observable<CryptoCoin> {
    // Backend endpoint does not exist - using mock only
    const coin = this.mockCoins.find(c => c.id === coinId);
    if (!coin) {
      return throwError(() => new Error(`Coin ${coinId} not found`));
    }
    return of(coin).pipe(delay(300));
  }

  // ========== Portfolio Operations ==========

  /**
   * Get complete portfolio (wallet + holdings + transactions)
   */
  getPortfolio(userId: number): Observable<CryptoPortfolio> {
    return this.getWalletByUserId(userId).pipe(
      map(wallet => {
        const holdings: CryptoHoldingWithPrice[] = [];
        let totalValueEUR = 0;

        this.mockHoldings
          .filter(h => h.walletId === wallet.id)
          .forEach(holding => {
            const coin = this.mockCoins.find(c => c.symbol.toUpperCase() === holding.cryptoSymbol.toUpperCase());
            const currentPrice = coin?.current_price || 0;
            const valueInEUR = holding.amount * currentPrice;
            totalValueEUR += valueInEUR;

            holdings.push({
              ...holding,
              currentPrice,
              valueInEUR,
              change24h: coin?.price_change_24h || 0,
              change24hPercent: coin?.price_change_percentage_24h || 0,
              coinName: coin?.name,
              coinImage: coin?.image
            });
          });

        const transactions = this.mockTransactions.filter(t => t.walletId === wallet.id);

        // Calculate total gain/loss (simplified - based on current value vs initial investment)
        const totalInvested = transactions
          .filter(t => t.type === TransactionType.BUY)
          .reduce((sum, t) => sum + t.eurAmount, 0);
        const totalGainLoss = totalValueEUR - totalInvested;
        const totalGainLossPercent = totalInvested > 0 ? (totalGainLoss / totalInvested) * 100 : 0;

        return {
          wallet,
          holdings,
          totalValueEUR,
          totalGainLoss,
          totalGainLossPercent,
          transactions
        };
      })
    );
  }

  // ========== Error Handling ==========

  private handleError = (error: any): Observable<never> => {
    console.error('CryptoService error:', error);
    return throwError(() => error);
  };
}

