import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import { Transaction, TransactionType, TransactionCategory, TransactionStatus } from '../../models';

/**
 * Transaction Service
 * Manages transaction data and operations (MOCK - no real backend)
 */
@Injectable({
  providedIn: 'root'
})
export class TransactionService {

  /**
   * Get recent transactions (mock data)
   */
  getRecentTransactions(limit: number = 10): Observable<Transaction[]> {
    const mockTransactions: Transaction[] = [
      {
        id: 'txn-001',
        type: TransactionType.DEBIT,
        category: TransactionCategory.SHOPPING,
        amount: -145.99,
        currency: 'USD',
        description: 'Amazon Purchase',
        status: TransactionStatus.COMPLETED,
        date: new Date('2026-01-01T10:30:00'),
        fromAccount: 'acc-001',
        merchant: 'Amazon.com',
        userId: 'usr-123456',
        icon: 'shopping_cart',
        color: '#EF4444'
      },
      {
        id: 'txn-002',
        type: TransactionType.CREDIT,
        category: TransactionCategory.SALARY,
        amount: 5500.00,
        currency: 'USD',
        description: 'Monthly Salary',
        status: TransactionStatus.COMPLETED,
        date: new Date('2025-12-31T09:00:00'),
        toAccount: 'acc-001',
        merchant: 'Tech Corp Inc.',
        userId: 'usr-123456',
        icon: 'account_balance',
        color: '#10B981'
      },
      {
        id: 'txn-003',
        type: TransactionType.DEBIT,
        category: TransactionCategory.FOOD,
        amount: -67.50,
        currency: 'USD',
        description: 'Restaurant Payment',
        status: TransactionStatus.COMPLETED,
        date: new Date('2025-12-30T19:45:00'),
        fromAccount: 'acc-001',
        merchant: 'Le Bistro',
        location: 'New York, NY',
        userId: 'usr-123456',
        icon: 'restaurant',
        color: '#EF4444'
      },
      {
        id: 'txn-004',
        type: TransactionType.TRANSFER,
        category: TransactionCategory.TRANSFER,
        amount: -1000.00,
        currency: 'USD',
        description: 'Transfer to Savings',
        status: TransactionStatus.COMPLETED,
        date: new Date('2025-12-30T14:20:00'),
        fromAccount: 'acc-001',
        toAccount: 'acc-002',
        userId: 'usr-123456',
        icon: 'swap_horiz',
        color: '#F59E0B'
      },
      {
        id: 'txn-005',
        type: TransactionType.DEBIT,
        category: TransactionCategory.UTILITIES,
        amount: -234.56,
        currency: 'USD',
        description: 'Electricity Bill',
        status: TransactionStatus.COMPLETED,
        date: new Date('2025-12-29T08:00:00'),
        fromAccount: 'acc-001',
        merchant: 'Electric Co.',
        userId: 'usr-123456',
        icon: 'electric_bolt',
        color: '#EF4444'
      },
      {
        id: 'txn-006',
        type: TransactionType.CRYPTO_BUY,
        category: TransactionCategory.INVESTMENT,
        amount: -500.00,
        currency: 'USD',
        description: 'Bitcoin Purchase',
        status: TransactionStatus.COMPLETED,
        date: new Date('2025-12-28T15:30:00'),
        fromAccount: 'acc-001',
        userId: 'usr-123456',
        icon: 'currency_bitcoin',
        color: '#F59E0B'
      },
      {
        id: 'txn-007',
        type: TransactionType.DEBIT,
        category: TransactionCategory.TRANSPORT,
        amount: -89.00,
        currency: 'USD',
        description: 'Uber Rides',
        status: TransactionStatus.COMPLETED,
        date: new Date('2025-12-27T18:45:00'),
        fromAccount: 'acc-001',
        merchant: 'Uber',
        userId: 'usr-123456',
        icon: 'local_taxi',
        color: '#EF4444'
      },
      {
        id: 'txn-008',
        type: TransactionType.DEBIT,
        category: TransactionCategory.ENTERTAINMENT,
        amount: -15.99,
        currency: 'USD',
        description: 'Netflix Subscription',
        status: TransactionStatus.COMPLETED,
        date: new Date('2025-12-26T12:00:00'),
        fromAccount: 'acc-001',
        merchant: 'Netflix',
        userId: 'usr-123456',
        icon: 'movie',
        color: '#EF4444'
      }
    ];

    return of(mockTransactions.slice(0, limit)).pipe(delay(350));
  }

  /**
   * Get all transactions with optional filtering
   */
  getTransactions(page: number = 1, pageSize: number = 20): Observable<{ transactions: Transaction[], total: number }> {
    return new Observable(observer => {
      this.getRecentTransactions(50).subscribe(transactions => {
        const start = (page - 1) * pageSize;
        const paginatedTransactions = transactions.slice(start, start + pageSize);

        observer.next({
          transactions: paginatedTransactions,
          total: transactions.length
        });
        observer.complete();
      });
    });
  }

  /**
   * Get transaction statistics
   */
  getTransactionStats(): Observable<{ income: number; expenses: number; balance: number }> {
    const stats = {
      income: 12450.00,
      expenses: 8234.56,
      balance: 4215.44
    };

    return of(stats).pipe(delay(300));
  }




getSuspiciousTransactions(): Observable<Transaction[]> {
  return new Observable(observer => {
    this.getRecentTransactions(50).subscribe(transactions => {
      const suspicious = transactions.filter(t =>
        Math.abs(t.amount || 0) > 1000 ||
        t.type === TransactionType.CRYPTO_BUY ||
        t.status === TransactionStatus.PENDING
      );
      observer.next(suspicious);
      observer.complete();
    });
  });
}



}
