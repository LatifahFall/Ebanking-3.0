import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatCheckboxModule,
    CustomButtonComponent
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  // Form fields
  username = '';
  email = '';
  password = '';
  confirmPassword = '';
  firstName = '';
  lastName = '';
  phoneNumber = '';
  
  // Checkboxes
  acceptTerms = false;
  acceptPrivacy = false;
  
  // UI state
  hidePassword = true;
  hideConfirmPassword = true;
  loading = false;
  errorMessage = '';
  
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Validation
    if (!this.validateForm()) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const registerData = {
      username: this.username,
      email: this.email,
      password: this.password,
      firstName: this.firstName,
      lastName: this.lastName,
      phoneNumber: this.phoneNumber || undefined
    };

    this.authService.register(registerData).subscribe({
      next: () => {
        console.log('✅ Registration successful');
        // Auto-login after registration
        this.authService.login(this.username, this.password).subscribe({
          next: () => {
            this.router.navigate(['/dashboard']);
          },
          error: () => {
            // If auto-login fails, redirect to login page
            this.router.navigate(['/auth/login']);
          }
        });
      },
      error: (error) => {
        console.error('❌ Registration failed:', error);
        this.errorMessage = error.message || 'Registration failed. Please try again.';
        this.loading = false;
      }
    });
  }

  validateForm(): boolean {
    // Required fields
    if (!this.username || !this.email || !this.password || !this.firstName || !this.lastName) {
      this.errorMessage = 'Please fill in all required fields';
      return false;
    }

    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.errorMessage = 'Please enter a valid email address';
      return false;
    }

    // Username validation (min 3 characters)
    if (this.username.length < 3) {
      this.errorMessage = 'Username must be at least 3 characters';
      return false;
    }

    // Password validation (min 8 characters)
    if (this.password.length < 8) {
      this.errorMessage = 'Password must be at least 8 characters';
      return false;
    }

    // Password confirmation
    if (this.password !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match';
      return false;
    }

    // Terms acceptance
    if (!this.acceptTerms || !this.acceptPrivacy) {
      this.errorMessage = 'You must accept the terms and privacy policy';
      return false;
    }

    return true;
  }
}
