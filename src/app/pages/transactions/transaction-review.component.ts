import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { Transaction } from '../../models';

@Component({
  selector: 'app-transaction-review',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  template: `
    <div class="page">
      <mat-card *ngIf="tx">
        <mat-card-title>Transaction {{ tx.id }}</mat-card-title>
        <mat-card-content>
          <p><strong>From:</strong> {{ tx.fromAccount }}</p>
          <p><strong>To:</strong> {{ tx.toAccount }}</p>
          <p><strong>Amount:</strong> {{ tx.amount }} {{ tx.currency }}</p>
          <p><strong>Date:</strong> {{ tx.date | date:'short' }}</p>
          <p><strong>Status:</strong> {{ tx.status }}</p>
          <p><strong>Description:</strong> {{ tx.description }}</p>
        </mat-card-content>
        <mat-card-actions>
          <button mat-stroked-button color="primary" (click)="goBack()">Back</button>
        </mat-card-actions>
      </mat-card>
      <div *ngIf="!tx">Transaction not found.</div>
    </div>
  `
})
export class TransactionReviewComponent implements OnInit {
  tx: Transaction | null = null;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    try {
      const raw = localStorage.getItem('mock_transactions');
      if (raw) {
        const list = JSON.parse(raw) as any[];
        const found = list.find(x => x.id === id) || null;
        if (found) {
          const tx: any = { ...found };
          if (!tx.date && tx.createdAt) tx.date = new Date(tx.createdAt);
          if (tx.date && typeof tx.date === 'string') tx.date = new Date(tx.date);
          this.tx = tx as Transaction;
        } else {
          this.tx = null;
        }
      }
    } catch {
      this.tx = null;
    }
  }

  goBack(): void {
    try {
      window.history.back();
    } catch {
      // noop
    }
  }
}
