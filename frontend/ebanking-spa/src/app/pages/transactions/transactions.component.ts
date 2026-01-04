import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
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

  constructor(private router: Router) {}

  ngOnInit(): void {
    try {
      const raw = localStorage.getItem('mock_transactions');
      if (raw) {
        const list = JSON.parse(raw) as any[];
        // Normalize each item to Transaction shape (accept createdAt fallback)
        this.transactions = list.map(it => {
          const t: any = { ...it };
          if (!t.date && t.createdAt) t.date = new Date(t.createdAt);
          if (t.date && typeof t.date === 'string') t.date = new Date(t.date);
          return t as Transaction;
        });
      }
    } catch {}
  }

  review(id: string) {
    this.router.navigate(['/transactions', id]);
  }
}
