import { Component, OnInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../../core/services/user.service';
import { AccountService } from '../../core/services/account.service';
import { TransactionService } from '../../core/services/transaction.service';
import { AuthService } from '../../core/services/auth.service';
import { User, UserRole, Account, Transaction, AccountType } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';

@Component({
  selector: 'app-agent-client-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    PageHeaderComponent,
    LoaderComponent
  ],
  templateUrl: './agent-client-details.component.html',
  styleUrl: './agent-client-details.component.scss'
})
export class AgentClientDetailsComponent implements OnInit {
  client: User | null = null;
  accounts: Account[] = [];
  transactions: Transaction[] = [];
  loading = true;
  errorMessage = '';
  currentAgentId: string | null = null;
  clientId: string | null = null;
  selectedTab = 0;

  displayedColumns: string[] = ['date', 'description', 'amount', 'status', 'category'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private userService: UserService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.role === UserRole.AGENT) {
      this.currentAgentId = currentUser.id;
      this.route.params.subscribe(params => {
        this.clientId = params['id'];
        if (this.clientId) {
          this.loadClientDetails();
        }
      });
    } else {
      this.errorMessage = 'Access denied. Agent role required.';
      this.loading = false;
    }
  }

  loadClientDetails(): void {
    if (!this.currentAgentId || !this.clientId) return;

    this.loading = true;
    this.userService.getClientProfile(this.currentAgentId, this.clientId).subscribe({
      next: (client) => {
        this.client = client;
        this.loadAccounts();
        this.loadTransactions();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load client details.';
        this.showError('Failed to load client details.');
      }
    });
  }

  loadAccounts(): void {
    if (!this.clientId) return;
    // TODO: Get accounts for specific client when backend supports it
    this.accountService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts.filter(a => a.userId === this.clientId);
      }
    });
  }

  loadTransactions(): void {
    if (!this.clientId) return;
    // TODO: Get transactions for specific client when backend supports it
    this.transactionService.getRecentTransactions(20).subscribe({
      next: (transactions) => {
        this.transactions = transactions.filter(t => t.userId === this.clientId);
      }
    });
  }

  onActivateClient(): void {
    if (!this.currentAgentId || !this.clientId || !this.client) return;
    this.userService.activateClient(this.currentAgentId, this.clientId).subscribe({
      next: () => {
        this.loadClientDetails();
        this.showSuccess('Client activated successfully.');
      },
      error: () => {
        this.showError('Failed to activate client.');
      }
    });
  }

  onDeactivateClient(): void {
    if (!this.currentAgentId || !this.clientId || !this.client) return;
    this.userService.deactivateClient(this.currentAgentId, this.clientId).subscribe({
      next: () => {
        this.loadClientDetails();
        this.showSuccess('Client deactivated successfully.');
      },
      error: () => {
        this.showError('Failed to deactivate client.');
      }
    });
  }

  onBack(): void {
    this.router.navigate(['/agent/clients']);
  }

  getStatusColor(status: string): string {
    return status === 'ACTIVE' ? 'primary' : 'warn';
  }

  getKycStatusColor(kycStatus: string): string {
    switch (kycStatus) {
      case 'VERIFIED': return 'primary';
      case 'IN_PROGRESS': return 'accent';
      case 'REJECTED': return 'warn';
      default: return '';
    }
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000
    });
  }

  onCreateAccount(): void {
    if (!this.clientId) return;

    const dialogRef = this.dialog.open(CreateAccountDialogComponent, {
      width: '500px',
      data: { clientId: this.clientId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadAccounts();
        this.showSuccess('Account created successfully.');
      }
    });
  }
}

// Dialog Component for Creating Account
@Component({
  selector: 'app-create-account-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    FormsModule,
    ReactiveFormsModule
  ],
  template: `
    <h2 mat-dialog-title>Create New Account</h2>
    <mat-dialog-content>
      <form [formGroup]="accountForm" class="account-form">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Account Type</mat-label>
          <mat-select formControlName="accountType">
            <mat-option [value]="AccountType.CHECKING">Checking</mat-option>
            <mat-option [value]="AccountType.SAVINGS">Savings</mat-option>
            <mat-option [value]="AccountType.INVESTMENT">Investment</mat-option>
            <mat-option [value]="AccountType.CRYPTO">Crypto</mat-option>
          </mat-select>
          @if (accountForm.get('accountType')?.hasError('required') && accountForm.get('accountType')?.touched) {
            <mat-error>Account type is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Currency</mat-label>
          <mat-select formControlName="currency">
            <mat-option value="USD">USD</mat-option>
            <mat-option value="EUR">EUR</mat-option>
            <mat-option value="GBP">GBP</mat-option>
            <mat-option value="MAD">MAD</mat-option>
          </mat-select>
          @if (accountForm.get('currency')?.hasError('required') && accountForm.get('currency')?.touched) {
            <mat-error>Currency is required</mat-error>
          }
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Initial Balance (Optional)</mat-label>
          <input matInput type="number" formControlName="initialBalance" step="0.01" min="0">
          <mat-hint>Leave empty for zero balance</mat-hint>
        </mat-form-field>
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onSubmit()" [disabled]="accountForm.invalid || creating">
        @if (creating) {
          Creating...
        } @else {
          Create Account
        }
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    .account-form {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      padding: 1rem 0;
    }
    .full-width {
      width: 100%;
    }
  `]
})
export class CreateAccountDialogComponent {
  accountForm: FormGroup;
  creating = false;
  AccountType = AccountType;

  constructor(
    private dialogRef: MatDialogRef<CreateAccountDialogComponent>,
    private accountService: AccountService,
    private snackBar: MatSnackBar,
    private fb: FormBuilder,
    @Inject(MAT_DIALOG_DATA) public data: { clientId: string }
  ) {
    this.accountForm = this.fb.group({
      accountType: ['', Validators.required],
      currency: ['USD', Validators.required],
      initialBalance: [0, [Validators.min(0)]]
    });
  }

  onSubmit(): void {
    if (this.accountForm.invalid) return;

    this.creating = true;
    const formValue = this.accountForm.value;

    this.accountService.createAccount(
      this.data.clientId,
      formValue.accountType,
      formValue.currency,
      formValue.initialBalance || 0
    ).subscribe({
      next: () => {
        this.creating = false;
        this.dialogRef.close(true);
      },
      error: () => {
        this.creating = false;
        this.snackBar.open('Failed to create account', 'Close', { duration: 3000 });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}

