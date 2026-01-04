import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { TransactionService } from '../../core/services/transaction.service';
import { Transaction } from '../../models';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, RouterModule, MatListModule, MatButtonModule, MatCardModule, MatIconModule],
  template: `
    <div class="page">
      <mat-card>
        <mat-card-title>Transactions</mat-card-title>
        <mat-card-subtitle>Recent activity (mock)</mat-card-subtitle>
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
          <div *ngIf="transactions.length===0">No transactions</div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`.tx-line{display:flex;align-items:center;width:100%}.tx-right{display:flex;gap:12px;align-items:center}.muted{color:rgba(0,0,0,0.54)} .spacer{flex:1}`]
})
export class TransactionsComponent implements OnInit {
  transactions: Transaction[] = [];
  searchQuery = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private transactionService: TransactionService
  ) {}

  ngOnInit(): void {
    // Check for search query parameter from navbar
    this.route.queryParams.subscribe(params => {
      if (params['search']) {
        this.searchQuery = params['search'];
      }
      this.loadTransactions();
    });
  }

  loadTransactions(): void {
    this.transactionService.getRecentTransactions(50).subscribe({
      next: (transactions) => {
        this.transactions = transactions;
        
        // Filter by search query if provided
        if (this.searchQuery.trim()) {
          const query = this.searchQuery.toLowerCase();
          this.transactions = this.transactions.filter(t => 
            t.description?.toLowerCase().includes(query) ||
            t.category?.toLowerCase().includes(query) ||
            t.id.toLowerCase().includes(query)
          );
        }
      },
      error: () => {
        // Fallback to localStorage if service fails
        try {
          const raw = localStorage.getItem('mock_transactions');
          if (raw) {
            const list = JSON.parse(raw) as any[];
            this.transactions = list.map(it => {
              const t: any = { ...it };
              if (!t.date && t.createdAt) t.date = new Date(t.createdAt);
              if (t.date && typeof t.date === 'string') t.date = new Date(t.date);
              return t as Transaction;
            });
            
            // Filter by search query if provided
            if (this.searchQuery.trim()) {
              const query = this.searchQuery.toLowerCase();
              this.transactions = this.transactions.filter(t => 
                t.description?.toLowerCase().includes(query) ||
                t.category?.toLowerCase().includes(query) ||
                t.id.toLowerCase().includes(query)
              );
            }
          }
        } catch {}
      }
    });
  }

  review(id: string) {
    this.router.navigate(['/transactions', id]);
  }
}
