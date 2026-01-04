import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { Account, AccountSummary, AccountType, CryptoAsset } from '../../models';

/**
 * Account Service
 * Manages account data and operations (MOCK - no real backend)
 */
@Injectable({
  providedIn: 'root'
})
export class AccountService {

  /**
   * Get user accounts (mock data)
   */
  getAccounts(): Observable<Account[]> {
    const mockAccounts: Account[] = [
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
        userId: 'usr-123456',
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
        userId: 'usr-123456',
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
        userId: 'usr-123456',
        color: '#F59E0B',
        icon: 'trending_up'
      }
    ];

    return of(mockAccounts).pipe(delay(300));
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
    return new Observable(observer => {
      this.getAccounts().subscribe(accounts => {
        const account = accounts.find(acc => acc.id === accountId);
        observer.next(account || null);
        observer.complete();
      });
    });
  }
}
