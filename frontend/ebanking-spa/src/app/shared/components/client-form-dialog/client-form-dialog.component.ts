import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { User } from '../../../models';

export interface ClientFormData {
  client?: User; // If provided, we're editing; otherwise, creating
}

@Component({
  selector: 'app-client-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './client-form-dialog.component.html',
  styleUrl: './client-form-dialog.component.scss'
})
export class ClientFormDialogComponent implements OnInit {
  isEditMode = false;
  formData = {
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: ''
  };

  constructor(
    private dialogRef: MatDialogRef<ClientFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ClientFormData
  ) {
    this.isEditMode = !!data.client;
    if (data.client) {
      this.formData = {
        firstName: data.client.firstName || '',
        lastName: data.client.lastName || '',
        email: data.client.email || '',
        phoneNumber: data.client.phoneNumber || ''
      };
    }
  }

  ngOnInit(): void {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.isFormValid()) {
      this.dialogRef.close(this.formData);
    }
  }

  isFormValid(): boolean {
    return !!(
      this.formData.firstName?.trim() &&
      this.formData.lastName?.trim() &&
      this.formData.email?.trim() &&
      this.isValidEmail(this.formData.email)
    );
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}
