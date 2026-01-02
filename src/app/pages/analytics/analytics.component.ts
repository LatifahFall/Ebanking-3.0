import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatGridListModule],
  template: `
    <div class="page">
      <mat-grid-list cols="3" rowHeight="120px" gutterSize="16px">
        <mat-grid-tile>
          <mat-card>
            <mat-card-title>Active users</mat-card-title>
            <mat-card-content>1,234</mat-card-content>
          </mat-card>
        </mat-grid-tile>
        <mat-grid-tile>
          <mat-card>
            <mat-card-title>Monthly volume</mat-card-title>
            <mat-card-content>MAD 1,234,567</mat-card-content>
          </mat-card>
        </mat-grid-tile>
        <mat-grid-tile>
          <mat-card>
            <mat-card-title>Success rate</mat-card-title>
            <mat-card-content>98.6%</mat-card-content>
          </mat-card>
        </mat-grid-tile>
      </mat-grid-list>
    </div>
  `
})
export class AnalyticsComponent {}
