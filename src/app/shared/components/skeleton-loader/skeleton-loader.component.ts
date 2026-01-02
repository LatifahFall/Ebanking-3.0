import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-skeleton-loader',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './skeleton-loader.component.html',
  styleUrls: ['./skeleton-loader.component.scss']
})
export class SkeletonLoaderComponent {
  variant = input<'card' | 'text' | 'text-large' | 'text-small' | 'circle'>('text');
  count = input<number>(1);
  width = input<string>('100%');
  height = input<string>();
}
