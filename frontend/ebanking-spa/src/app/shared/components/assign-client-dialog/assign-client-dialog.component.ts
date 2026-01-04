import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { User } from '../../../models';

export interface AssignClientDialogData {
  agent: User;
  clients: User[];
  assignedClientIds?: string[];
}

@Component({
  selector: 'app-assign-client-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './assign-client-dialog.component.html',
  styleUrl: './assign-client-dialog.component.scss'
})
export class AssignClientDialogComponent {
  selectedClientId: string = '';

  constructor(
    private dialogRef: MatDialogRef<AssignClientDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AssignClientDialogData
  ) {}

  get availableClients(): User[] {
    if (!this.data.assignedClientIds || this.data.assignedClientIds.length === 0) {
      return this.data.clients;
    }
    return this.data.clients.filter(c => !this.data.assignedClientIds!.includes(c.id));
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onAssign(): void {
    if (this.selectedClientId) {
      this.dialogRef.close({ clientId: this.selectedClientId });
    }
  }
}

