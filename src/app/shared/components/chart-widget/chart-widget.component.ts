import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

/**
 * Chart Widget Component (Placeholder)
 * Ready for integration with Chart.js, ApexCharts, or ngx-charts
 */
@Component({
  selector: 'app-chart-widget',
  imports: [CommonModule],
  templateUrl: './chart-widget.component.html',
  styleUrl: './chart-widget.component.scss'
})
export class ChartWidgetComponent {
  title = input<string>('Chart');
  type = input<'line' | 'bar' | 'pie' | 'donut'>('line');
  height = input<number>(300);
}

