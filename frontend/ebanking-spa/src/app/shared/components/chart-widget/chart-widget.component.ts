import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    color?: string;
  }[];
}

/**
 * Chart Widget Component
 * Displays simple charts using SVG (no external dependencies)
 * Ready for future integration with Chart.js, ApexCharts, or ngx-charts
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
  data = input<ChartData | null>(null);

  // Default mock data for demonstration
  defaultData: ChartData = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
    datasets: [
      {
        label: 'Income',
        data: [5000, 5200, 4800, 5500, 6000, 5800],
        color: '#10B981'
      },
      {
        label: 'Expenses',
        data: [3200, 3500, 2800, 4000, 3800, 4200],
        color: '#EF4444'
      }
    ]
  };

  chartData = computed(() => this.data() || this.defaultData);

  // Calculate chart dimensions and scaling
  chartWidth = 600;
  chartHeight = computed(() => this.height() - 60); // Account for padding
  padding = { top: 20, right: 20, bottom: 40, left: 60 };

  // Calculate max value for scaling
  maxValue = computed(() => {
    const allData = this.chartData().datasets.flatMap(d => d.data);
    return Math.max(...allData, 1000) * 1.1; // Add 10% padding
  });

  // Calculate bar width for bar charts
  barWidth = computed(() => {
    const labels = this.chartData().labels;
    const availableWidth = this.chartWidth - this.padding.left - this.padding.right;
    const gap = 10;
    return (availableWidth / labels.length) - gap;
  });

  // Get Y-axis scale
  getYScale(value: number): number {
    const availableHeight = this.chartHeight() - this.padding.top - this.padding.bottom;
    return availableHeight - (value / this.maxValue()) * availableHeight;
  }

  // Get X position for a data point
  getXPosition(index: number): number {
    const availableWidth = this.chartWidth - this.padding.left - this.padding.right;
    const step = availableWidth / this.chartData().labels.length;
    return this.padding.left + (index * step) + (step / 2);
  }

  // Format currency
  formatCurrency(value: number): string {
    return `$${value.toLocaleString('en-US', { maximumFractionDigits: 0 })}`;
  }

  // Generate line chart points
  getLinePoints(data: number[]): string {
    return data.map((value, index) => {
      const x = this.getXPosition(index);
      const y = this.padding.top + this.getYScale(value);
      return `${x},${y}`;
    }).join(' ');
  }
}

