import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

/**
 * Info Card Component
 * Reusable card for displaying stats, balances, crypto, etc.
 */
@Component({
  selector: 'app-info-card',
  imports: [CommonModule, MatIconModule],
  templateUrl: './info-card.component.html',
  styleUrl: './info-card.component.scss'
})
export class InfoCardComponent {
  title = input<string>('');
  value = input<string | number>('');
  subtitle = input<string>('');
  icon = input<string>('');
  trend = input<number | undefined>();
  trendLabel = input<string>('');
  color = input<string>('#4F46E5');
  clickable = input<boolean>(false);
}

