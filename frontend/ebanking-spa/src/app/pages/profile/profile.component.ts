import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatInputModule, MatCardModule, CustomButtonComponent],
  template: `
    <div class="page">
      <mat-card *ngIf="user">
        <mat-card-title>Profile</mat-card-title>
        <mat-card-content>
          <form (ngSubmit)="save()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>First name</mat-label>
              <input matInput [(ngModel)]="user.firstName" name="firstName" required />
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Last name</mat-label>
              <input matInput [(ngModel)]="user.lastName" name="lastName" required />
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Email</mat-label>
              <input matInput [(ngModel)]="user.email" name="email" type="email" required />
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Phone</mat-label>
              <input matInput [(ngModel)]="user.phoneNumber" name="phoneNumber" />
            </mat-form-field>

            <div class="actions">
              <app-custom-button label="Save" (clicked)="save()"></app-custom-button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <div *ngIf="!user">Loading...</div>

      <div *ngIf="message" class="message">{{ message }}</div>
    </div>
  `,
  styles: [`.full-width{width:100%}.actions{margin-top:12px}.message{margin-top:12px;color:green}`]
})
export class ProfileComponent implements OnInit {
  user: any = null;
  message = '';

  constructor(private auth: AuthService, private userService: UserService) {}

  ngOnInit(): void {
    const u = this.auth.getCurrentUser();
    if (u) {
      this.userService.getMyProfile(u.id).subscribe(profile => (this.user = profile));
    }
  }

  save(): void {
    if (!this.user) return;
    this.userService.updateMyProfile(this.user.id, {
      firstName: this.user.firstName,
      lastName: this.user.lastName,
      email: this.user.email,
      phoneNumber: this.user.phoneNumber
    }).subscribe(updated => {
      if (updated) {
        this.message = 'Profile updated';
        try { this.auth.updateStoredUser(updated); } catch {}
      } else {
        this.message = 'Failed to update profile';
      }
      setTimeout(() => (this.message = ''), 3000);
    }, () => {
      this.message = 'Failed to update profile';
      setTimeout(() => (this.message = ''), 3000);
    });
  }
}