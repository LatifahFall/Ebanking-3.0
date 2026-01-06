import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { InfoCardComponent } from '../../shared/components/info-card/info-card.component';
import { TransactionItemComponent } from '../../shared/components/transaction-item/transaction-item.component';
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';
import { CryptoService } from '../../core/services/crypto.service';
import { Account, Transaction, AccountSummary } from '../../models';
import { ChartData } from '../../shared/components/chart-widget/chart-widget.component';
import { CryptoPortfolio } from '../../models/crypto.model';
import { TransactionType } from '../../models/transaction.model';

/**
 * Dashboard Page
 * Main landing page after authentication with overview cards and widgets
 */
@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatSnackBarModule,
    MatButtonModule,
    PageHeaderComponent,
    InfoCardComponent,
    TransactionItemComponent,
    ChartWidgetComponent,
    CustomButtonComponent,
    LoaderComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  loading = true;
  errorMessage: string | null = null;
  accountSummary: AccountSummary | null = null;
  accounts: Account[] = [];
  recentTransactions: Transaction[] = [];
  spendingData: ChartData | null = null;
  cryptoPortfolio: CryptoPortfolio | null = null;

  constructor(
    private accountService: AccountService,
    private transactionService: TransactionService,
    private cryptoService: CryptoService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;
    this.errorMessage = null;

    // Load account summary
    this.accountService.getAccountSummary().subscribe({
      next: (summary) => {
        this.accountSummary = summary;
        this.generateSpendingData();
      },
      error: (error) => {
        console.error('Error loading account summary:', error);
        this.errorMessage = 'Failed to load account summary. Please try again.';
        this.showError('Failed to load account summary');
      }
    });

    // Load accounts
    this.accountService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
      },
      error: (error) => {
        console.error('Error loading accounts:', error);
        this.showError('Failed to load accounts');
      }
    });

    // Load recent transactions
    this.transactionService.getRecentTransactions(5).subscribe({
      next: (transactions) => {
        this.recentTransactions = transactions;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading transactions:', error);
        this.errorMessage = 'Failed to load recent transactions. Please try again.';
        this.loading = false;
        this.showError('Failed to load recent transactions');
      }
    });
  }

  /**
   * Génère les données de dépenses pour les graphiques à partir des transactions réelles
   */
  private generateSpendingData(): void {
    if (!this.recentTransactions || this.recentTransactions.length === 0) {
      this.spendingData = null;
      return;
    }
    const now = new Date();
    const months: string[] = [];
    const income: number[] = [];
    const expenses: number[] = [];
    for (let i = 5; i >= 0; i--) {
      const monthDate = new Date(now.getFullYear(), now.getMonth() - i, 1);
      const monthLabel = monthDate.toLocaleDateString('en-US', { month: 'short' });
      months.push(monthLabel);
      const monthTx = this.recentTransactions.filter(tx => {
        const txDate = new Date(tx.date);
        return txDate.getMonth() === monthDate.getMonth() && txDate.getFullYear() === monthDate.getFullYear();
      });
      // Utilise les bons types pour revenus et dépenses
      const monthIncome = monthTx.filter(tx => tx.type === TransactionType.CREDIT || tx.type === TransactionType.DEPOSIT).reduce((sum, tx) => sum + tx.amount, 0);
      const monthExpenses = monthTx.filter(tx => tx.type === TransactionType.DEBIT || tx.type === TransactionType.PAYMENT || tx.type === TransactionType.WITHDRAWAL).reduce((sum, tx) => sum + tx.amount, 0);
      income.push(monthIncome);
      expenses.push(monthExpenses);
    }
    this.spendingData = {
      labels: months,
      datasets: [
        {
          label: 'Income',
          data: income,
          color: '#10B981'
        },
        {
          label: 'Expenses',
          data: expenses,
          color: '#EF4444'
        }
      ]
    };
  }

  formatCurrency(amount: number): string {
    return `$${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  formatCrypto(amount: number, decimals: number = 8): string {
    return amount.toFixed(decimals);
  }

  getCryptoTotal(): number {
    return (this.accountSummary?.cryptoAssets || []).reduce((sum, asset) => sum + asset.valueInUSD, 0);
  }

  onViewAllTransactions(): void {
    this.router.navigate(['/transactions']);
  }

  onViewAllAccounts(): void {
    this.router.navigate(['/accounts']);
  }

  onViewCrypto(): void {
    this.router.navigate(['/crypto']);
  }

  onQuickTransfer(): void {
    // Navigate to payments page with transfer pre-selected
    this.router.navigate(['/payments'], {
      queryParams: { action: 'transfer' }
    });
  }

  onPayBills(): void {
    // Navigate to payments page
    this.router.navigate(['/payments']);
  }

  /**
   * Show error snackbar notification
   */
  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar']
    });
  }
}
