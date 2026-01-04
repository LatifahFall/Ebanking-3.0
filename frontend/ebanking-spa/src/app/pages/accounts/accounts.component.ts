import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { AccountService } from '../../core/services/account.service';
import { Account } from '../../models';

@Component({
  selector: 'app-accounts',
  imports: [CommonModule, RouterModule, MatIconModule, PageHeaderComponent],
  templateUrl: './accounts.component.html',
  styleUrl: './accounts.component.scss'
})
export class AccountsComponent implements OnInit {
  accounts: Account[] = [];
  loading = true;

  constructor(
    private accountService: AccountService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.accountService.getAccounts().subscribe(accounts => {
      this.accounts = accounts;
      this.loading = false;
    });
  }

  formatCurrency(amount: number, currency: string): string {
    return `${currency} ${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  onViewDetails(accountId: string): void {
    this.router.navigate(['/accounts', accountId]);
  }

  onTransfer(accountId: string): void {
    this.router.navigate(['/payments'], {
      queryParams: { action: 'transfer', fromAccount: accountId }
    });
  }
}

