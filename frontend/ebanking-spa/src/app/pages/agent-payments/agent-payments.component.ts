import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormControl } from '@angular/forms';
import { map, startWith } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { UserService } from '../../core/services/user.service';
import { AccountService } from '../../core/services/account.service';
import { PaymentService } from '../../core/services/payment.service';
import { AuthService } from '../../core/services/auth.service';
import { User, UserRole, Account, PaymentRequest, PaymentType } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';

enum OperationType {
  DEPOSIT = 'DEPOSIT',
  WITHDRAWAL = 'WITHDRAWAL',
  TRANSFER = 'TRANSFER'
}

@Component({
  selector: 'app-agent-payments',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatAutocompleteModule,
    FormsModule,
    ReactiveFormsModule,
    PageHeaderComponent,
    LoaderComponent
  ],
  templateUrl: './agent-payments.component.html',
  styleUrl: './agent-payments.component.scss'
})
export class AgentPaymentsComponent implements OnInit {
  clients: User[] = [];
  filteredClients$!: Observable<User[]>;
  selectedClientId: string | null = null;
  selectedClient: User | null = null;
  clientAccounts: Account[] = [];
  loading = false;
  processing = false;
  currentAgentId: string | null = null;
  operationForm: FormGroup;
  operationType: OperationType = OperationType.DEPOSIT;
  clientSearchControl = new FormControl('');

  OperationType = OperationType;

  constructor(
    private userService: UserService,
    private accountService: AccountService,
    private paymentService: PaymentService,
    private authService: AuthService,
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.operationForm = this.fb.group({
      accountId: ['', Validators.required],
      toAccountId: [''],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      currency: ['USD', Validators.required],
      reference: [''],
      description: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.role === UserRole.AGENT) {
      this.currentAgentId = currentUser.id;
      this.loadClients();
    }

    // Setup filtered clients observable
    this.filteredClients$ = this.clientSearchControl.valueChanges.pipe(
      startWith(''),
      map(value => {
        const searchTerm = typeof value === 'string' ? value : '';
        return this.filterClients(searchTerm);
      })
    );

    // Watch operation type changes to update form validation
    this.operationForm.get('accountId')?.valueChanges.subscribe(() => {
      this.updateFormValidation();
    });
  }

  private filterClients(searchTerm: string): User[] {
    if (!searchTerm) {
      return this.clients;
    }
    const term = searchTerm.toLowerCase();
    return this.clients.filter(client =>
      client.fullName.toLowerCase().includes(term) ||
      client.email.toLowerCase().includes(term) ||
      (client.firstName && client.firstName.toLowerCase().includes(term)) ||
      (client.lastName && client.lastName.toLowerCase().includes(term))
    );
  }

  displayClient(client: User | null): string {
    if (!client) return '';
    return `${client.fullName} (${client.email})`;
  }

  onClientSelected(client: User): void {
    this.selectedClient = client;
    this.selectedClientId = client.id;
    this.clientSearchControl.setValue(this.displayClient(client), { emitEvent: false });
    this.onClientChange();
  }

  loadClients(): void {
    if (!this.currentAgentId) return;

    this.loading = true;
    this.userService.getAgentsClients(this.currentAgentId).subscribe({
      next: (clients) => {
        this.clients = clients;
        this.loading = false;
        // Reset search control to trigger filtering
        this.clientSearchControl.setValue('');
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load clients', 'Close', { duration: 3000 });
      }
    });
  }

  onClientChange(): void {
    this.loadClientAccounts();
    this.operationForm.reset({
      currency: 'USD',
      operationType: this.operationType
    });
  }

  loadClientAccounts(): void {
    if (!this.selectedClientId) {
      this.clientAccounts = [];
      return;
    }

    this.loading = true;
    // TODO: When backend supports it, use GET /api/accounts?userId={clientId}
    // Pass userId to getAccounts to filter on the service side
    this.accountService.getAccounts(this.selectedClientId).subscribe({
      next: (accounts) => {
        this.clientAccounts = accounts;
        this.loading = false;
        if (this.clientAccounts.length > 0) {
          this.operationForm.patchValue({ accountId: this.clientAccounts[0].id });
        }
      },
      error: () => {
        this.loading = false;
        this.snackBar.open('Failed to load client accounts', 'Close', { duration: 3000 });
      }
    });
  }

  onOperationTypeChange(): void {
    this.updateFormValidation();
    this.operationForm.patchValue({ toAccountId: '' });
  }

  updateFormValidation(): void {
    const toAccountControl = this.operationForm.get('toAccountId');
    if (this.operationType === OperationType.TRANSFER) {
      toAccountControl?.setValidators([Validators.required]);
    } else {
      toAccountControl?.clearValidators();
    }
    toAccountControl?.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.operationForm.invalid || !this.selectedClientId) {
      this.snackBar.open('Please fill all required fields', 'Close', { duration: 3000 });
      return;
    }

    this.processing = true;
    const formValue = this.operationForm.value;

    // Build payment request based on operation type
    // Backend PaymentType enum: STANDARD, INSTANT, RECURRING, BIOMETRIC, QR_CODE
    // Note: Backend doesn't have DEPOSIT/WITHDRAWAL types, so we use STANDARD
    // For WITHDRAWAL: fromAccountId = client account, toAccountId = null (as per backend comment)
    // For DEPOSIT: This is not directly supported by payment service - would need account service endpoint
    // For TRANSFER: fromAccountId = source, toAccountId = destination

    let paymentRequest: PaymentRequest;

    if (this.operationType === OperationType.WITHDRAWAL) {
      // Withdrawal: fromAccountId = client account, toAccountId = null
      paymentRequest = {
        fromAccountId: formValue.accountId,
        toAccountId: undefined, // null for withdrawals (as per backend migration comment)
        amount: formValue.amount,
        currency: formValue.currency,
        paymentType: PaymentType.STANDARD, // Backend doesn't have WITHDRAWAL type
        reference: formValue.reference || `AGENT-WITHDRAWAL-${Date.now()}`,
        description: formValue.description || `Withdrawal operation by agent`
      };
    } else if (this.operationType === OperationType.TRANSFER) {
      // Transfer: fromAccountId = source, toAccountId = destination
      paymentRequest = {
        fromAccountId: formValue.accountId,
        toAccountId: formValue.toAccountId,
        amount: formValue.amount,
        currency: formValue.currency,
        paymentType: PaymentType.STANDARD, // Backend doesn't have TRANSFER type
        reference: formValue.reference || `AGENT-TRANSFER-${Date.now()}`,
        description: formValue.description || `Transfer operation by agent`
      };
    } else {
      // DEPOSIT: Not directly supported by payment service
      // Would need account service endpoint or system account
      // For now, we'll show an error
      this.snackBar.open(
        'Deposit operations are not yet supported via payment service. Please use account service endpoints.',
        'Close',
        { duration: 5000 }
      );
      this.processing = false;
      return;
    }

    this.paymentService.initiatePayment(paymentRequest).subscribe({
      next: (response) => {
        this.processing = false;
        this.snackBar.open(
          `Operation ${this.operationType.toLowerCase()} completed successfully`,
          'Close',
          { duration: 5000 }
        );
        this.operationForm.reset({
          currency: 'USD',
          accountId: this.clientAccounts.length > 0 ? this.clientAccounts[0].id : ''
        });
        // Reload accounts to show updated balance
        this.loadClientAccounts();
      },
      error: (error) => {
        this.processing = false;
        this.snackBar.open(
          `Operation failed: ${error.error?.message || 'Unknown error'}`,
          'Close',
          { duration: 5000 }
        );
      }
    });
  }

  getSelectedAccount(): Account | null {
    const accountId = this.operationForm.get('accountId')?.value;
    return this.clientAccounts.find(acc => acc.id === accountId) || null;
  }
}
