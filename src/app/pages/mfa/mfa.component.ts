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
  pendingUserId: string | null = null;
  devCode: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.startResendTimer();
    // get pending MFA user id and dev code if available
    try {
      this.pendingUserId = this.authService.getPendingMfaUserId();
      this.devCode = this.pendingUserId ? this.authService.getMfaCodeForDev(this.pendingUserId) : null;
    } catch {}
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
    this.errorMessage = '';import { Component, OnInit, OnDestroy } from '@angular/core';
    import { CommonModule } from '@angular/common';
    import { Router } from '@angular/router';
    import { FormsModule } from '@angular/forms';
    import { MatIconModule } from '@angular/material/icon';
    import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
    import { AuthService } from '../../core/services/auth.service';

    @Component({
      selector: 'app-mfa',
      standalone: true,
      imports: [CommonModule, FormsModule, MatIconModule, CustomButtonComponent],
      templateUrl: './mfa.component.html',
      styleUrl: './mfa.component.scss'
    })
    export class MfaComponent implements OnInit, OnDestroy {
      code = ['', '', '', '', '', ''];
      loading = false;
      errorMessage = '';
      resendTimer = 60;
      pendingUserId: string | null = null;
      devCode: string | null = null;
      private timerInterval: any;

      constructor(
        private authService: AuthService,
        private router: Router
      ) {}

      ngOnInit(): void {
        // Get pending MFA user id and dev code if available
        try {
          this.pendingUserId = this.authService.getPendingMfaUserId();
          this.devCode = this.pendingUserId ? this.authService.getMfaCodeForDev(this.pendingUserId) : null;

          // If no pending user ID, redirect to login
          if (!this.pendingUserId) {
            this.router.navigate(['/login']);
            return;
          }
        } catch (error) {
          console.error('Error getting MFA info:', error);
        }

        this.startResendTimer();
      }

      ngOnDestroy(): void {
        // Clear timer on component destroy
        if (this.timerInterval) {
          clearInterval(this.timerInterval);
        }
      }

      onCodeInput(index: number, event: any): void {
        const value = event.target.value;

        // Only allow numbers
        if (value && !/^\d$/.test(value)) {
          this.code[index] = '';
          event.target.value = '';
          return;
        }

        this.code[index] = value;

        // Auto-focus next input
        if (value && index < 5) {
          const nextInput = event.target.nextElementSibling;
          if (nextInput) {
            nextInput.focus();
          }
        }

        // Auto-verify when all digits are entered
        if (this.isCodeComplete()) {
          this.verifyCode();
        }
      }

      onKeyDown(index: number, event: KeyboardEvent): void {
        // Handle backspace
        if (event.key === 'Backspace') {
          if (!this.code[index] && index > 0) {
            const prevInput = (event.target as HTMLElement).previousElementSibling as HTMLInputElement;
            if (prevInput) {
              prevInput.focus();
            }
          } else {
            this.code[index] = '';
          }
        }

        // Handle paste
        if (event.key === 'v' && (event.ctrlKey || event.metaKey)) {
          event.preventDefault();
          navigator.clipboard.readText().then(text => {
            this.handlePaste(text, index);
          });
        }
      }

      onPaste(index: number, event: ClipboardEvent): void {
        event.preventDefault();
        const pastedData = event.clipboardData?.getData('text') || '';
        this.handlePaste(pastedData, index);
      }

      handlePaste(text: string, startIndex: number): void {
        // Extract only digits from pasted text
        const digits = text.replace(/\D/g, '').split('').slice(0, 6);

        if (digits.length === 0) return;

        // Fill the code array from the start index
        digits.forEach((digit, i) => {
          const index = startIndex + i;
          if (index < 6) {
            this.code[index] = digit;
          }
        });

        // Focus the next empty input or the last one
        const nextEmptyIndex = this.code.findIndex(d => d === '');
        const focusIndex = nextEmptyIndex === -1 ? 5 : nextEmptyIndex;

        const inputs = document.querySelectorAll('.code-input');
        if (inputs[focusIndex]) {
          (inputs[focusIndex] as HTMLInputElement).focus();
        }

        // Auto-verify if code is complete
        if (this.isCodeComplete()) {
          this.verifyCode();
        }
      }

      isCodeComplete(): boolean {
        return this.code.every(digit => digit !== '');
      }

      verifyCode(): void {
        if (!this.isCodeComplete()) {
          this.errorMessage = 'Please enter the complete 6-digit code';
          return;
        }

        const fullCode = this.code.join('');
        this.loading = true;
        this.errorMessage = '';

        this.authService.verifyMFA(fullCode, this.pendingUserId || undefined).subscribe({
          next: (response) => {
            this.loading = false;
            if (response.success) {
              this.router.navigate(['/dashboard']);
            } else {
              this.errorMessage = response.message || 'Verification failed';
              this.clearCode();
            }
          },
          error: (error) => {
            this.loading = false;
            this.errorMessage = error.error?.message || 'Invalid verification code. Please try again.';
            this.clearCode();
          }
        });
      }

      clearCode(): void {
        this.code = ['', '', '', '', '', ''];
        const firstInput = document.querySelector('.code-input') as HTMLInputElement;
        if (firstInput) {
          firstInput.focus();
        }
      }

      resendCode(): void {
        if (this.resendTimer > 0) return;

        this.loading = true;
        this.errorMessage = '';

        this.authService.resendMFACode(this.pendingUserId || undefined).subscribe({
          next: (response) => {
            this.loading = false;
            if (response.success) {
              this.resendTimer = 60;
              this.startResendTimer();
              this.clearCode();
              // Update dev code if available
              if (this.pendingUserId) {
                this.devCode = this.authService.getMfaCodeForDev(this.pendingUserId);
              }
            }
          },
          error: (error) => {
            this.loading = false;
            this.errorMessage = error.error?.message || 'Failed to resend code. Please try again.';
          }
        });
      }

      startResendTimer(): void {
        if (this.timerInterval) {
          clearInterval(this.timerInterval);
        }

        this.timerInterval = setInterval(() => {
          this.resendTimer--;
          if (this.resendTimer <= 0) {
            clearInterval(this.timerInterval);
          }
        }, 1000);
      }

      formatTimer(): string {
        const minutes = Math.floor(this.resendTimer / 60);
        const seconds = this.resendTimer % 60;
        return `${minutes}:${seconds.toString().padStart(2, '0')}`;
      }

      goBack(): void {
        this.authService.clearPendingMfaUserId();
        this.router.navigate(['/login']);
      }
    }
    this.authService.verifyMFA(fullCode, this.pendingUserId || undefined).subscribe({
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

