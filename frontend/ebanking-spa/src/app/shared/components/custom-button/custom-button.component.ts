import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-custom-button',
  imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './custom-button.component.html',
  styleUrl: './custom-button.component.scss'
})
export class CustomButtonComponent {
  label = input<string>('');
  variant = input<'primary' | 'secondary' | 'danger' | 'ghost'>('primary');
  icon = input<string>('');
  iconPosition = input<'left' | 'right'>('left');
  disabled = input<boolean>(false);
  loading = input<boolean>(false);
  fullWidth = input<boolean>(false);
  size = input<'small' | 'medium' | 'large'>('medium');
  
  clicked = output<void>();

  onClick(): void {
    if (!this.disabled() && !this.loading()) {
      this.clicked.emit();
    }
  }
}

