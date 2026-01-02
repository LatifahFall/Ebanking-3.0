import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';

@Component({
  selector: 'app-crypto',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatListModule],
  template: `
    <div class="page">
      <mat-card>
        <mat-card-title>Crypto Wallet</mat-card-title>
        <mat-card-subtitle>Mock balances</mat-card-subtitle>
        <mat-card-content>
          <mat-list>
            <mat-list-item>BTC: <span class="spacer"></span>0.0123</mat-list-item>
            <mat-list-item>ETH: <span class="spacer"></span>0.45</mat-list-item>
            <mat-list-item>USDT: <span class="spacer"></span>150.00</mat-list-item>
          </mat-list>
        </mat-card-content>
      </mat-card>
    </div>
  `
})
export class CryptoComponent {}
