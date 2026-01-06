import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { TransactionService } from '../../core/services/transaction.service';
import { AccountService } from '../../core/services/account.service';
import { Transaction } from '../../models';
import { Account } from '../../models/account.model';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, RouterModule, MatListModule, MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <div class="page">
      <mat-card *ngIf="selectedAccount">
        <mat-card-title>Transactions</mat-card-title>
        <mat-card-subtitle>
          <span>Compte : {{ selectedAccount.accountName }} ({{ selectedAccount.accountNumber }})</span><br>
          <span>Solde : <b>{{ balance?.balance | number:'1.2-2' }} {{ selectedAccount.currency }}</b></span>
        </mat-card-subtitle>
        <mat-card-content>
          <mat-list *ngIf="transactions.length>0">
            <mat-list-item *ngFor="let t of transactions">
              <div class="tx-line">
                <div>
                  <strong>{{ t.id }}</strong>
                  <div class="muted">{{ t.date | date:'short' }}</div>
                </div>
                <div class="spacer"></div>
                <div class="tx-right">
                  <div>{{ t.amount }} {{ t.currency }}</div>
                  <div class="muted">{{ t.status }}</div>
                  <button mat-stroked-button color="primary" (click)="review(t.id)">
                    <mat-icon>visibility</mat-icon>
                    Review
                  </button>
                </div>
              </div>
            </mat-list-item>
          </mat-list>
          <div *ngIf="transactions.length===0">Aucune transaction</div>
        </mat-card-content>
      </mat-card>
      <div *ngIf="!selectedAccount">Aucun compte trouvé.</div>
    </div>
  `,
  styles: [`.tx-line{display:flex;align-items:center;width:100%}.tx-right{display:flex;gap:12px;align-items:center}.muted{color:rgba(0,0,0,0.54)} .spacer{flex:1}`]
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  selectedAccount: Account | null = null;
  balance: { balance: number; availableBalance?: number } | null = null;
  searchQuery = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private transactionService: TransactionService,
    private accountService: AccountService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['search']) {
        this.searchQuery = params['search'];
      }
      this.loadAccountsAndTransactions();
    });
  }

  loadAccountsAndTransactions(): void {
    this.accountService.getAccounts().subscribe(accounts => {
      if (accounts && accounts.length > 0) {
        this.selectedAccount = accounts[0];
        this.loadBalance();
        this.loadTransactions();
      } else {
        this.selectedAccount = null;
        this.transactions = [];
        this.balance = null;
      }
    });
  }

  loadBalance(): void {
    if (!this.selectedAccount) return;
    this.accountService.getBalance(this.selectedAccount.id).subscribe(bal => {
      this.balance = bal;
    });
  }

  loadTransactions(): void {
    if (!this.selectedAccount) return;
    this.accountService.getTransactions(this.selectedAccount.id, 50).subscribe(transactions => {
      this.transactions = transactions;
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase();
        this.transactions = this.transactions.filter(t =>
          t.description?.toLowerCase().includes(query) ||
          t.category?.toLowerCase().includes(query) ||
          t.id.toLowerCase().includes(query)
        );
      }
    });
  }

  review(id: string): void {
    // Navigation ou affichage d'un détail de transaction
    // this.router.navigate(['/transaction', id]);
  }
}
