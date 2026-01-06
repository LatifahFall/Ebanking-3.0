import { Injectable } from '@angular/core';
import { Observable, of, delay, catchError, map } from 'rxjs';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Account, AccountSummary, AccountType, CryptoAsset, Transaction } from '../../models';
import { environment } from '../../../environments/environment';

/**
 * Account Service
 * Manages account data and operations (MOCK - no real backend)
 */
@Injectable({
  providedIn: 'root'
})
export class AccountService {
  // Utiliser une URL relative pour permettre le proxy Vercel
  private baseUrl = '/api/accounts';
  private readonly useMock = environment.useMock;

  constructor(private http: HttpClient) {}

  /**
   * Get user accounts
   * @param userId Optional - if provided, returns accounts for that user
   */
  getAccounts(userId?: string): Observable<Account[]> {
    if (this.useMock) {
      return this.getAccountsMock(userId);
    }
    // Real HTTP call
    let url = this.baseUrl;
    if (userId) {
      url += `?userId=${userId}`;
    }
    return this.http.get<Account[]>(url).pipe(
      catchError(() => {
        // Fallback to mock on error
        return this.getAccountsMock(userId);
      })
    );
  }

  /**
   * Get user accounts (mock data)
   * @param userId Optional - if provided, returns accounts for that user
   */
  private getAccountsMock(userId?: string): Observable<Account[]> {
    // Generate mock accounts for different users
    const allMockAccounts: Account[] = [
      // Accounts for user '3' (Fatima Client)
      {
        id: 'acc-001',
        accountNumber: '****4521',
        accountType: AccountType.CHECKING,
        accountName: 'Main Checking',
        currency: 'USD',
        balance: 45678.90,
        availableBalance: 45678.90,
        status: 'ACTIVE' as any,
        iban: 'GB82WEST12345698765432',
        bic: 'WESTGB22',
        createdAt: new Date('2022-03-15'),
        lastActivity: new Date(),
        userId: '3',
        color: '#4F46E5',
        icon: 'account_balance_wallet'
      },
      {
        id: 'acc-002',
        accountNumber: '****7834',
        accountType: AccountType.SAVINGS,
        accountName: 'Savings Account',
        currency: 'USD',
        balance: 128450.50,
        availableBalance: 128450.50,
        status: 'ACTIVE' as any,
        iban: 'GB82WEST12345698765433',
        createdAt: new Date('2022-03-15'),
        userId: '3',
        color: '#10B981',
        icon: 'savings'
      },
      {
        id: 'acc-003',
        accountNumber: '****9102',
        accountType: AccountType.INVESTMENT,
        accountName: 'Investment Portfolio',
        currency: 'USD',
        balance: 89234.75,
        availableBalance: 89234.75,
        status: 'ACTIVE' as any,
        createdAt: new Date('2023-01-10'),
        userId: '3',
        color: '#F59E0B',
        icon: 'trending_up'
      },
      // Accounts for Ahmed Benali (ID: '4')
      {
        id: 'acc-004',
        accountNumber: '****3333',
        accountType: AccountType.CHECKING,
        accountName: 'Ahmed Checking',
        currency: 'MAD',
        balance: 25000.00,
        availableBalance: 25000.00,
        status: 'ACTIVE' as any,
        iban: 'MA64011519000001234567890123',
        bic: 'BCMAMAMC',
        createdAt: new Date('2023-06-10'),
        lastActivity: new Date(),
        userId: '4',
        color: '#4F46E5',
        icon: 'account_balance_wallet'
      },
      // Accounts for Sara Alami (ID: '5')
      {
        id: 'acc-005',
        accountNumber: '****4444',
        accountType: AccountType.CHECKING,
        accountName: 'Sara Checking',
        currency: 'EUR',
        balance: 8500.00,
        availableBalance: 8500.00,
        status: 'ACTIVE' as any,
        iban: 'FR7630006000011234567890390',
        bic: 'CRLYFR21',
        createdAt: new Date('2023-08-20'),
        lastActivity: new Date(),
        userId: '5',
        color: '#4F46E5',
        icon: 'account_balance_wallet'
      },
      {
        id: 'acc-006',
        accountNumber: '****5555',
        accountType: AccountType.SAVINGS,
        accountName: 'Sara Savings',
        currency: 'EUR',
        balance: 12000.00,
        availableBalance: 12000.00,
        status: 'ACTIVE' as any,
        iban: 'FR7630006000011234567890490',
        bic: 'CRLYFR21',
        createdAt: new Date('2023-09-01'),
        userId: '5',
        color: '#10B981',
        icon: 'savings'
      },
      // Accounts for other users (fallback)
      {
        id: 'acc-007',
        accountNumber: '****1234',
        accountType: AccountType.CHECKING,
        accountName: 'Main Checking',
        currency: 'USD',
        balance: 10000.00,
        availableBalance: 10000.00,
        status: 'ACTIVE' as any,
        iban: 'GB82WEST12345698765434',
        bic: 'WESTGB22',
        createdAt: new Date('2022-03-15'),
        lastActivity: new Date(),
        userId: 'usr-123456',
        color: '#4F46E5',
        icon: 'account_balance_wallet'
      }
    ];

    // Filter by userId if provided
    const filteredAccounts = userId
      ? allMockAccounts.filter(acc => acc.userId === userId)
      : allMockAccounts;

    return of(filteredAccounts).pipe(delay(300));
  }

  /**
   * Get account summary with totals
   */
  getAccountSummary(): Observable<AccountSummary> {
    const mockCryptoAssets: CryptoAsset[] = [
      {
        id: 'crypto-001',
        symbol: 'BTC',
        name: 'Bitcoin',
        balance: 0.5234,
        valueInUSD: 23456.78,
        valueInFiat: 23456.78,
        change24h: 2.34,
        color: '#F7931A',
        icon: '₿'
      },
      {
        id: 'crypto-002',
        symbol: 'ETH',
        name: 'Ethereum',
        balance: 8.7234,
        valueInUSD: 18234.56,
        valueInFiat: 18234.56,
        change24h: -1.23,
        color: '#627EEA',
        icon: 'Ξ'
      },
      {
        id: 'crypto-003',
        symbol: 'USDT',
        name: 'Tether',
        balance: 5000,
        valueInUSD: 5000.00,
        valueInFiat: 5000.00,
        change24h: 0.01,
        color: '#26A17B',
        icon: '₮'
      }
    ];

    // This would come from getAccounts in real scenario
    const totalBalance = 45678.90 + 128450.50 + 89234.75;
    const totalCrypto = mockCryptoAssets.reduce((sum, asset) => sum + asset.valueInUSD, 0);

    const summary: AccountSummary = {
      totalBalance: totalBalance + totalCrypto,
      totalIncome: 12450.00,
      totalExpense: 8234.56,
      accounts: [],
      cryptoAssets: mockCryptoAssets
    };

    return of(summary).pipe(delay(400));
  }

  /**
   * Get single account by ID
   */
  getAccountById(accountId: string): Observable<Account | null> {
    if (this.useMock) {
      return new Observable<Account | null>(observer => {
        this.getAccountsMock().subscribe(accounts => {
          const account = accounts.find(acc => acc.id === accountId);
          observer.next(account || null);
          observer.complete();
        });
      });
    }
    return this.http.get<Account>(`${this.baseUrl}/${accountId}`).pipe(
      catchError(() => {
        // Fallback to mock
        return this.getAccountsMock().pipe(
          map(accounts => {
            const account = accounts.find(acc => acc.id === accountId);
            return account || null;
          })
        );
      })
    );
  }

  /**
   * Create account for a user
   * POST /api/accounts
   */
  createAccount(userId: string, accountType: AccountType, currency: string, initialBalance?: number): Observable<Account> {
    const newAccount: Account = {
      id: `acc-${Date.now()}`,
      accountNumber: `****${Math.floor(Math.random() * 10000).toString().padStart(4, '0')}`,
      accountType: accountType,
      accountName: `${accountType} Account`,
      currency: currency,
      balance: initialBalance || 0,
      availableBalance: initialBalance || 0,
      status: 'ACTIVE' as any,
      createdAt: new Date(),
      lastActivity: new Date(),
      userId: userId,
      color: this.getAccountColor(accountType),
      icon: this.getAccountIcon(accountType)
    };

    if (this.useMock) {
      return of(newAccount).pipe(delay(300));
    }
    // Real HTTP call
    return this.http.post<Account>(this.baseUrl, {
      userId,
      accountType,
      currency,
      initialBalance
    }).pipe(
      catchError(() => {
        // Fallback to mock
        return of(newAccount).pipe(delay(300));
      })
    );
  }

  /**
   * Get account balance
   * GET /api/accounts/{id}/balance
   */
  getBalance(accountId: string): Observable<{ balance: number; availableBalance?: number } | null> {
    if (this.useMock) {
      // find account in mock and return balances
      return this.getAccountsMock().pipe(
        map(accounts => {
          const acc = accounts.find(a => a.id === accountId);
          if (!acc) return null;
          return { balance: acc.balance || 0, availableBalance: acc.availableBalance ?? acc.balance };
        }),
        catchError(() => of(null))
      );
    }
    return this.http.get<{ balance: number; availableBalance?: number }>(`${this.baseUrl}/${accountId}/balance`).pipe(
      catchError(() => of(null))
    );
  }

  /**
   * Get transaction history for an account
   * GET /api/accounts/{id}/transactions?limit={limit}
   */
  getTransactions(accountId: string, limit = 50): Observable<Transaction[]> {
    if (this.useMock) {
      // generate mock transactions based on account
      return this.getAccountsMock().pipe(
        map(accounts => {
          const acc = accounts.find(a => a.id === accountId);
          if (!acc) return [] as Transaction[];
          const now = Date.now();
          const list: Transaction[] = Array.from({ length: Math.min(limit, 10) }).map((_, i) => ({
            id: `tx-${accountId}-${i}-${now}`,
            type: i % 2 === 0 ? ("DEBIT" as any) : ("CREDIT" as any),
            category: ("OTHER" as any),
            amount: +(Math.random() * 500).toFixed(2),
            currency: acc.currency || 'USD',
            description: i % 2 === 0 ? 'Payment' : 'Deposit',
            status: (i % 3 === 0 ? ("COMPLETED" as any) : ("PENDING" as any)),
            date: new Date(now - i * 86400000),
            fromAccount: i % 2 === 0 ? acc.id : undefined,
            toAccount: i % 2 === 0 ? undefined : acc.id,
            userId: acc.userId
          } as Transaction));
          return list;
        })
      );
    }

    const params = new HttpParams().set('limit', `${limit}`);
    return this.http.get<Transaction[]>(`${this.baseUrl}/${accountId}/transactions`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  /**
   * Get account statement for date range
   * GET /api/accounts/{id}/statement?startDate={ISO}&endDate={ISO}
   */
  getStatement(accountId: string, startDate: Date, endDate: Date): Observable<Transaction[]> {
    const start = startDate.toISOString();
    const end = endDate.toISOString();
    if (this.useMock) {
      // reuse getTransactions and filter by date range
      return this.getTransactions(accountId, 100).pipe(
        map(list => list.filter(t => t.date >= startDate && t.date <= endDate))
      );
    }
    const params = new HttpParams().set('startDate', start).set('endDate', end);
    return this.http.get<Transaction[]>(`${this.baseUrl}/${accountId}/statement`, { params }).pipe(
      catchError(() => of([]))
    );
  }

  private getAccountColor(accountType: AccountType): string {
    switch (accountType) {
      case AccountType.CHECKING: return '#4F46E5';
      case AccountType.SAVINGS: return '#10B981';
      case AccountType.INVESTMENT: return '#F59E0B';
      case AccountType.CRYPTO: return '#F7931A';
      default: return '#64748B';
    }
  }

  private getAccountIcon(accountType: AccountType): string {
    switch (accountType) {
      case AccountType.CHECKING: return 'account_balance_wallet';
      case AccountType.SAVINGS: return 'savings';
      case AccountType.INVESTMENT: return 'trending_up';
      case AccountType.CRYPTO: return 'currency_bitcoin';
      default: return 'account_balance';
    }
  }
}
