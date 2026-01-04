import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { AccountService } from '../../core/services/account.service';
import { Account } from '../../models';

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatButtonModule, PageHeaderComponent],
  templateUrl: './account-details.component.html',
  styleUrl: './account-details.component.scss'
})
export class AccountDetailsComponent implements OnInit {
  account: Account | null = null;

  constructor(
    private route: ActivatedRoute,
    private accountService: AccountService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.accountService.getAccountById(id).subscribe(a => (this.account = a));
    }
  }

  onBack(): void {
    this.router.navigate(['/accounts']);
  }

  formatCurrency(amount: number, currency: string): string {
    return `${currency} ${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  }

  onTransfer(): void {
    if (this.account) {
      this.router.navigate(['/payments'], {
        queryParams: { action: 'transfer', fromAccount: this.account.id }
      });
    }
  }
}