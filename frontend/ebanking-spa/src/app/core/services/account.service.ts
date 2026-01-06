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
  private baseUrl = 'http://34.22.142.65/api/accounts';
  private readonly useMock = environment.useMock;

  constructor(private http: HttpClient) {}

  /**
   * Créer un compte
   */
  createAccount(userId: string, accountType: string, currency: string, initialBalance: number): Observable<Account> {
    return this.http.post<Account>(this.baseUrl, {
      userId,
      accountType,
      currency,
      initialBalance
    });
  }

  /**
   * Récupérer un compte par ID
   */
  getAccountById(id: string): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/${id}`);
  }

  /**
   * Récupérer tous les comptes d'un utilisateur
   */
  getAccounts(userId?: string): Observable<Account[]> {
    let url = this.baseUrl;
    if (userId) url += `?userId=${userId}`;
    return this.http.get<Account[]>(url);
  }

  /**
   * Mettre à jour un compte
   */
  updateAccount(id: string, data: Partial<Account>): Observable<Account> {
    return this.http.put<Account>(`${this.baseUrl}/${id}`, data);
  }

  /**
   * Suspendre un compte
   */
  suspendAccount(id: string, reason: string, suspendedBy: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/suspend`, { reason, suspendedBy });
  }

  /**
   * Clôturer un compte
   */
  closeAccount(id: string, closureReason: string, closedBy: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/${id}/close`, { closureReason, closedBy });
  }

  /**
   * Récupérer le solde d'un compte
   */
  getBalance(id: string): Observable<number> {
    return this.http.get<number>(`${this.baseUrl}/${id}/balance`);
  }

  /**
   * Récupérer l'historique des transactions
   */
  getTransactions(id: string, limit: number = 20): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.baseUrl}/${id}/transactions?limit=${limit}`);
  }

  /**
   * Récupérer le relevé de compte sur une période
   */
  getStatement(id: string, startDate: string, endDate: string): Observable<Transaction[]> {
    return this.http.get<Transaction[]>(`${this.baseUrl}/${id}/statement?startDate=${startDate}&endDate=${endDate}`);
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
