import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatGridListModule],
  template: `
    <div class="page">
      <mat-grid-list cols="3" rowHeight="100px" gutterSize="16px">
        <mat-grid-tile>
          <mat-card>
            <mat-card-title>Total users</mat-card-title>
            <mat-card-content>3</mat-card-content>
          </mat-card>
        </mat-grid-tile>
        <mat-grid-tile>
          <mat-card>
            <mat-card-title>Pending verifications</mat-card-title>
            <mat-card-content>0</mat-card-content>
          </mat-card>
        </mat-grid-tile>
        <mat-grid-tile>
          <mat-card>
            <mat-card-title>Recent signups</mat-card-title>
            <mat-card-content>1</mat-card-content>
          </mat-card>
        </mat-grid-tile>
      </mat-grid-list>
    </div>
  `
})
export class AdminDashboardComponent {}
