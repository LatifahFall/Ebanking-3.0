import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-mfa',
  imports: [CommonModule, FormsModule, MatIconModule, CustomButtonComponent],
  templateUrl: './mfa.component.html',
  styleUrl: './mfa.component.scss'
})
export class MfaComponent {
  code = ['', '', '', '', '', ''];
  loading = false;
  errorMessage = '';
  resendTimer = 60;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.startResendTimer();
  }

  onCodeInput(index: number, event: any): void {
    const value = event.target.value;
    
    if (value && index < 5) {
      const nextInput = event.target.nextElementSibling;
      if (nextInput) {
        nextInput.focus();
      }
    }
    
    if (this.isCodeComplete()) {
      this.verifyCode();
    }
  }

  onKeyDown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace' && !this.code[index] && index > 0) {
      const prevInput = (event.target as HTMLElement).previousElementSibling as HTMLInputElement;
      if (prevInput) {
        prevInput.focus();
      }
    }
  }

  isCodeComplete(): boolean {
    return this.code.every(digit => digit !== '');
  }

  verifyCode(): void {
    const fullCode = this.code.join('');
    this.loading = true;
    this.errorMessage = '';

    this.authService.verifyMFA(fullCode).subscribe({
      next: (response) => {
        if (response.success) {
          this.router.navigate(['/dashboard']);
        }
        this.loading = false;
      },
      error: (error) => {
        this.errorMessage = 'Invalid verification code';
        this.code = ['', '', '', '', '', ''];
        this.loading = false;
      }
    });
  }

  resendCode(): void {
    console.log('Resend code');
    this.resendTimer = 60;
    this.startResendTimer();
  }

  startResendTimer(): void {
    const interval = setInterval(() => {
      this.resendTimer--;
      if (this.resendTimer <= 0) {
        clearInterval(interval);
      }
    }, 1000);
  }
}

