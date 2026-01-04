import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { User } from '../../../models';

export interface AssignAgentData {
  client: User;
  agents: User[];
  currentAgentId?: string | null;
}

@Component({
  selector: 'app-assign-agent-dialog',
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
  templateUrl: './assign-agent-dialog.component.html',
  styleUrl: './assign-agent-dialog.component.scss'
})
export class AssignAgentDialogComponent implements OnInit {
  selectedAgentId: string = '';

  constructor(
    private dialogRef: MatDialogRef<AssignAgentDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AssignAgentData
  ) {
    if (data.currentAgentId) {
      this.selectedAgentId = data.currentAgentId;
    } else if (data.agents.length > 0) {
      this.selectedAgentId = data.agents[0].id;
    }
  }

  ngOnInit(): void {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onAssign(): void {
    if (this.selectedAgentId) {
      this.dialogRef.close(this.selectedAgentId);
    }
  }

  onUnassign(): void {
    this.dialogRef.close(null);
  }
}
