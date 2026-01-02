import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { Transaction } from '../../../models';

/**
 * Transaction Item Component
 * Display individual transaction with styling
 */
@Component({
  selector: 'app-transaction-item',
  imports: [CommonModule, MatIconModule],
  templateUrl: './transaction-item.component.html',
  styleUrl: './transaction-item.component.scss'
})
export class TransactionItemComponent {
  transaction = input.required<Transaction>();
  clickable = input<boolean>(true);

  getTransactionIcon(transaction: Transaction): string {
    return transaction.icon || 'receipt';
  }

  formatAmount(amount: number, currency: string): string {
    const sign = amount > 0 ? '+' : '';
    return `${sign}${currency} ${Math.abs(amount).toFixed(2)}`;
  }

  formatDate(date: Date): string {
    const d = new Date(date);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (d.toDateString() === today.toDateString()) {
      return 'Today';
    } else if (d.toDateString() === yesterday.toDateString()) {
      return 'Yesterday';
    }
    
    return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }
}

