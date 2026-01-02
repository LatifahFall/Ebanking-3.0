import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { InfoCardComponent } from '../../shared/components/info-card/info-card.component';
import { TransactionItemComponent } from '../../shared/components/transaction-item/transaction-item.component';
import { ChartWidgetComponent } from '../../shared/components/chart-widget/chart-widget.component';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';
import { Account, Transaction, AccountSummary } from '../../models';

/**
 * Dashboard Page
 * Main landing page after authentication with overview cards and widgets
 */
@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    MatIconModule,
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
  accountSummary: AccountSummary | null = null;
  accounts: Account[] = [];
  recentTransactions: Transaction[] = [];

  constructor(
    private accountService: AccountService,
    private transactionService: TransactionService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.loading = true;

    // Load account summary
    this.accountService.getAccountSummary().subscribe(summary => {
      this.accountSummary = summary;
    });

    // Load accounts
    this.accountService.getAccounts().subscribe(accounts => {
      this.accounts = accounts;
    });

    // Load recent transactions
    this.transactionService.getRecentTransactions(5).subscribe(transactions => {
      this.recentTransactions = transactions;
      this.loading = false;
    });
  }

  formatCurrency(amount: number): string {
    return `$${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  getCryptoTotal(): number {
    return (this.accountSummary?.cryptoAssets || []).reduce((sum, asset) => sum + asset.valueInUSD, 0);
  }

  onViewAllTransactions(): void {
    console.log('Navigate to transactions page');
  }

  onViewAllAccounts(): void {
    console.log('Navigate to accounts page');
  }

  onQuickTransfer(): void {
    console.log('Open quick transfer dialog');
  }

  onPayBills(): void {
    console.log('Navigate to pay bills');
  }
}

