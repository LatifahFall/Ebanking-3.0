import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaymentService } from '../../core/services/payment.service';
import { PaymentResponse, PaymentRequest, PaymentStatus } from '../../models';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatListModule, MatIconModule, CustomButtonComponent],
  template: `
    <div class="page">
      <mat-card>
        <mat-card-title>Payments</mat-card-title>
        <mat-card-subtitle>Initiate a payment or review history</mat-card-subtitle>

        <mat-card-content>
          <form (submit)="onInitiate($event)" class="initiate-form">
            <mat-form-field appearance="outline">
              <mat-label>From Account ID</mat-label>
              <input matInput name="from" required />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>To Account ID</mat-label>
              <input matInput name="to" />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Amount</mat-label>
              <input matInput name="amount" type="number" required />
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Currency</mat-label>
              <input matInput name="currency" value="USD" />
            </mat-form-field>

            <app-custom-button label="Send" class="send-btn" (clicked)="onInitiate($event)"></app-custom-button>
          </form>

          <div class="history-controls">
            <label>Page size:
              <select [(ngModel)]="size" (change)="applyPaginationSettings()">
                <option *ngFor="let s of pageSizeOptions" [value]="s">{{ s }}</option>
              </select>
            </label>

            <label>Sort by:
              <select [(ngModel)]="sortBy" (change)="applyPaginationSettings()">
                <option value="createdAt">Created</option>
                <option value="amount">Amount</option>
                <option value="reference">Reference</option>
              </select>
            </label>

            <label>Direction:
              <select [(ngModel)]="sortDir" (change)="applyPaginationSettings()">
                <option value="DESC">DESC</option>
                <option value="ASC">ASC</option>
              </select>
            </label>
          </div>

          <div *ngIf="loading">Loading payments...</div>
          <div *ngIf="!loading && errorMessage" class="error">{{ errorMessage }}</div>

          <mat-list *ngIf="!loading && payments.length > 0">
            <mat-list-item *ngFor="let p of payments">
              <div class="payment-line">
                <div class="left">
                  <div class="title">{{ p.reference || p.id }}</div>
                  <div class="muted">{{ p.createdAt | date:'short' }}</div>
                </div>
                <div class="spacer"></div>
                <div class="right">
                  <div class="amount">{{ p.amount }} {{ p.currency }}</div>
                  <div class="status">{{ p.status }}</div>
                  <button mat-stroked-button color="warn" *ngIf="p.status!== 'COMPLETED'" (click)="cancel(p.id)">Cancel</button>
                  <button mat-stroked-button color="primary" *ngIf="p.status === 'COMPLETED'" (click)="reverse(p.id)">Reverse</button>
                </div>
              </div>
            </mat-list-item>
          </mat-list>

          <div *ngIf="!loading && payments.length === 0" class="empty">No payments found.</div>

          <div class="pagination" *ngIf="totalElements > 0">
            <button mat-button (click)="prevPage()" [disabled]="page === 0">Previous</button>
            <span>Page {{ page + 1 }} of {{ totalPages }} — {{ totalElements }} items</span>
            <button mat-button (click)="nextPage()" [disabled]="page + 1 >= totalPages">Next</button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `
})
export class PaymentsComponent implements OnInit {
  payments: PaymentResponse[] = [];
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  loading = false;
  errorMessage = '';
  pageSizeOptions = [5, 10, 20, 50];
  sortBy: string = 'createdAt';
  sortDir: 'ASC' | 'DESC' = 'DESC';

  constructor(private paymentService: PaymentService) {}

  ngOnInit(): void {
    this.loadPayments();
  }

  loadPayments(page = this.page): void {
    this.loading = true;
    this.errorMessage = '';
    this.paymentService.getPayments(undefined, undefined, page, this.size, this.sortBy, this.sortDir).subscribe(resp => {
      this.payments = resp.payments;
      this.page = resp.page;
      this.size = resp.size;
      this.totalElements = resp.totalElements;
      this.totalPages = resp.totalPages;
      this.loading = false;
    }, () => {
      this.loading = false;
      this.errorMessage = 'Failed to load payments. Please try again.';
    });
  }

  onInitiate(e?: Event | any): void {
    // support both native form submit event and custom-button click (which may emit void)
    let form: HTMLFormElement | null = null;
    if (e && typeof e.preventDefault === 'function') {
      e.preventDefault();
      form = e.target as HTMLFormElement;
    } else {
      // custom button click - find the form by known class
      form = document.querySelector('.initiate-form') as HTMLFormElement;
    }
    if (!form) return;
    const from = (form.elements.namedItem('from') as HTMLInputElement).value;
    const to = (form.elements.namedItem('to') as HTMLInputElement).value || undefined;
    const amount = Number((form.elements.namedItem('amount') as HTMLInputElement).value);
    const currency = (form.elements.namedItem('currency') as HTMLInputElement).value || 'USD';

    const req: PaymentRequest = {
      fromAccountId: from,
      toAccountId: to,
      amount,
      currency,
      paymentType: 'TRANSFER'
    } as any;

    this.paymentService.initiatePayment(req).subscribe(() => this.loadPayments(0));
    form.reset();
  }

  prevPage(): void {
    if (this.page > 0) this.loadPayments(this.page - 1);
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) this.loadPayments(this.page + 1);
  }

  applyPaginationSettings(): void {
    // reset to first page when changing size or sort
    this.loadPayments(0);
  }

  cancel(id: string): void {
    if (!id) return;
    this.loading = true;
    this.paymentService.cancelPayment(id).subscribe(() => {
      this.loadPayments(0);
    }, () => {
      this.loading = false;
      this.errorMessage = 'Failed to cancel payment.';
    });
  }

  reverse(id: string): void {
    if (!id) return;
    this.loading = true;
    this.paymentService.reversePayment(id).subscribe(() => {
      this.loadPayments(0);
    }, () => {
      this.loading = false;
      this.errorMessage = 'Failed to reverse payment.';
    });
  }
}