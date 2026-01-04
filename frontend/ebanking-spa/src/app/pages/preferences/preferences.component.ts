import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { UserPreferences, UserPreferencesRequest } from '../../models/preferences.model';

/**
 * Preferences Component
 * Manage user preferences (language, theme, notifications)
 */
@Component({
  selector: 'app-preferences',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSnackBarModule,
    MatButtonModule,
    MatIconModule,
    PageHeaderComponent,
    CustomButtonComponent,
    LoaderComponent
  ],
  templateUrl: './preferences.component.html',
  styleUrl: './preferences.component.scss'
})
export class PreferencesComponent implements OnInit {
  loading = true;
  saving = false;
  preferences: UserPreferences | null = null;
  userId: string | null = null;

  // Form model
  form = {
    language: 'en',
    theme: 'light',
    notificationEmail: true,
    notificationSms: true,
    notificationPush: true,
    notificationInApp: true
  };

  languages = [
    { value: 'en', label: 'English' },
    { value: 'fr', label: 'Français' },
    { value: 'ar', label: 'العربية' },
    { value: 'es', label: 'Español' }
  ];

  themes = [
    { value: 'light', label: 'Light' },
    { value: 'dark', label: 'Dark' }
  ];

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser) {
      this.userId = currentUser.id;
      this.loadPreferences();
    } else {
      this.loading = false;
      this.showError('User not authenticated');
    }
  }

  loadPreferences(): void {
    if (!this.userId) return;

    this.loading = true;
    this.userService.getPreferences(this.userId).subscribe({
      next: (prefs) => {
        if (prefs) {
          this.preferences = prefs;
          this.form = {
            language: prefs.language || 'en',
            theme: prefs.theme || 'light',
            notificationEmail: prefs.notificationEmail ?? true,
            notificationSms: prefs.notificationSms ?? true,
            notificationPush: prefs.notificationPush ?? true,
            notificationInApp: prefs.notificationInApp ?? true
          };
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading preferences:', error);
        this.showError('Failed to load preferences');
        this.loading = false;
      }
    });
  }

  onSave(): void {
    if (!this.userId) return;

    this.saving = true;
    const request: UserPreferencesRequest = {
      language: this.form.language,
      theme: this.form.theme,
      notificationEmail: this.form.notificationEmail,
      notificationSms: this.form.notificationSms,
      notificationPush: this.form.notificationPush,
      notificationInApp: this.form.notificationInApp
    };

    this.userService.updatePreferences(this.userId, request).subscribe({
      next: (updated) => {
        if (updated) {
          this.preferences = updated;
          this.showSuccess('Preferences updated successfully');
          
          // Apply theme immediately if changed
          if (updated.theme) {
            document.body.setAttribute('data-theme', updated.theme);
          }
        }
        this.saving = false;
      },
      error: (error) => {
        console.error('Error updating preferences:', error);
        this.showError('Failed to update preferences');
        this.saving = false;
      }
    });
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }
}

