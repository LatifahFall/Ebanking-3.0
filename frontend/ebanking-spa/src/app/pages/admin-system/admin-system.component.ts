import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TransactionService } from '../../core/services/transaction.service';
import { PaymentRule } from '../../models/payment-rule.model';
import { PaymentRulesService } from '../../core/services/payment-rules.service';
import { Transaction } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
export interface SystemConfig {
  maxTransactionAmount: number;
  minTransactionAmount: number;
  dailyTransactionLimit: number;
  instantPaymentEnabled: boolean;
  biometricPaymentEnabled: boolean;
  qrCodePaymentEnabled: boolean;
}

export interface SecuritySettings {
  require2FA: boolean;
  sessionTimeout: number;
  maxLoginAttempts: number;
  passwordMinLength: number;
  requireStrongPassword: boolean;
  enableFraudDetection: boolean;
}

export interface SuspiciousTransaction {
  id: string;
  transactionId: string;
  userId: string;
  amount: number;
  reason: string;
  severity: 'low' | 'medium' | 'high' | 'critical';
  timestamp: Date;
  status: 'pending' | 'reviewed' | 'resolved' | 'false_positive';
}

@Component({
  selector: 'app-admin-system',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule,
    MatChipsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    PageHeaderComponent,
    LoaderComponent
  ],
  templateUrl: './admin-system.component.html',
  styleUrl: './admin-system.component.scss'
})
export class AdminSystemComponent implements OnInit {
  selectedTab = 0;
  loading = false;

  // System Configuration
  systemConfigForm: FormGroup;
  systemConfig: SystemConfig = {
    maxTransactionAmount: 100000,
    minTransactionAmount: 0.01,
    dailyTransactionLimit: 50000,
    instantPaymentEnabled: true,
    biometricPaymentEnabled: true,
    qrCodePaymentEnabled: true
  };

  // Security Settings
  securityForm: FormGroup;
  securitySettings: SecuritySettings = {
    require2FA: false,
    sessionTimeout: 30,
    maxLoginAttempts: 5,
    passwordMinLength: 8,
    requireStrongPassword: true,
    enableFraudDetection: true
  };

  // Payment Rules
  paymentRules: PaymentRule[] = [];
  paymentRuleForm: FormGroup;
  loadingPaymentRules = false;
  paymentRulesColumns: string[] = ['name', 'condition', 'action', 'enabled', 'actions'];

  // Suspicious Transactions
  suspiciousTransactions: SuspiciousTransaction[] = [];
  suspiciousColumns: string[] = ['transactionId', 'userId', 'amount', 'reason', 'severity', 'timestamp', 'status', 'actions'];

  constructor(
    private fb: FormBuilder,
    private transactionService: TransactionService,
    private paymentRulesService: PaymentRulesService,
    private snackBar: MatSnackBar
  ) {
    this.systemConfigForm = this.fb.group({
      maxTransactionAmount: [this.systemConfig.maxTransactionAmount, [Validators.required, Validators.min(0.01)]],
      minTransactionAmount: [this.systemConfig.minTransactionAmount, [Validators.required, Validators.min(0.01)]],
      dailyTransactionLimit: [this.systemConfig.dailyTransactionLimit, [Validators.required, Validators.min(0)]],
      instantPaymentEnabled: [this.systemConfig.instantPaymentEnabled],
      biometricPaymentEnabled: [this.systemConfig.biometricPaymentEnabled],
      qrCodePaymentEnabled: [this.systemConfig.qrCodePaymentEnabled]
    });

    this.securityForm = this.fb.group({
      require2FA: [this.securitySettings.require2FA],
      sessionTimeout: [this.securitySettings.sessionTimeout, [Validators.required, Validators.min(5)]],
      maxLoginAttempts: [this.securitySettings.maxLoginAttempts, [Validators.required, Validators.min(3)]],
      passwordMinLength: [this.securitySettings.passwordMinLength, [Validators.required, Validators.min(6)]],
      requireStrongPassword: [this.securitySettings.requireStrongPassword],
      enableFraudDetection: [this.securitySettings.enableFraudDetection]
    });

    this.paymentRuleForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      isActive: [true]
    });
  }

  ngOnInit(): void {
    this.loadPaymentRules();
    this.loadSuspiciousTransactions();
  }

  onSaveSystemConfig(): void {
    if (this.systemConfigForm.valid) {
      this.loading = true;
      setTimeout(() => {
        this.systemConfig = this.systemConfigForm.value;
        this.loading = false;
        this.snackBar.open('System configuration saved successfully', 'Close', { duration: 3000 });
      }, 500);
    }
  }

  onSaveSecuritySettings(): void {
    if (this.securityForm.valid) {
      this.loading = true;
      // In a real app, this would call a backend endpoint
      setTimeout(() => {
        this.securitySettings = this.securityForm.value;
        this.loading = false;
        this.snackBar.open('Security settings saved successfully', 'Close', { duration: 3000 });
      }, 500);
    }
  }

  onTogglePaymentRule(rule: PaymentRule): void {
    rule.enabled = !rule.enabled;
    this.snackBar.open(`Payment rule "${rule.name}" ${rule.enabled ? 'enabled' : 'disabled'}`, 'Close', { duration: 2000 });
  }

  onDeletePaymentRule(rule: PaymentRule): void {
    if (confirm(`Are you sure you want to delete rule "${rule.name}"?`)) {
      this.paymentRules = this.paymentRules.filter(r => r.id !== rule.id);
      this.snackBar.open('Payment rule deleted', 'Close', { duration: 2000 });
    }
  }

  loadPaymentRules(): void {
    this.loadingPaymentRules = true;
    this.paymentRulesService.getAllPaymentRules().subscribe({
      next: (rules) => {
        this.paymentRules = rules;
        this.loadingPaymentRules = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement des règles de paiement', 'Fermer', { duration: 3000 });
        this.loadingPaymentRules = false;
      }
    });
  }

  createPaymentRule(): void {
    if (this.paymentRuleForm.invalid) return;
    this.paymentRulesService.createPaymentRule(this.paymentRuleForm.value).subscribe({
      next: (rule) => {
        this.snackBar.open('Règle de paiement créée', 'Fermer', { duration: 3000 });
        this.loadPaymentRules();
        this.paymentRuleForm.reset({ isActive: true });
      },
      error: () => {
        this.snackBar.open('Erreur lors de la création', 'Fermer', { duration: 3000 });
      }
    });
  }

  updatePaymentRule(rule: PaymentRule): void {
    if (!rule.uuid) return;
    this.paymentRulesService.updatePaymentRule(rule.uuid, rule).subscribe({
      next: () => {
        this.snackBar.open('Règle mise à jour', 'Fermer', { duration: 3000 });
        this.loadPaymentRules();
      },
      error: () => {
        this.snackBar.open('Erreur lors de la mise à jour', 'Fermer', { duration: 3000 });
      }
    });
  }

  deletePaymentRule(rule: PaymentRule): void {
    if (!rule.uuid) return;
    this.paymentRulesService.deletePaymentRule(rule.uuid).subscribe({
      next: () => {
        this.snackBar.open('Règle supprimée', 'Fermer', { duration: 3000 });
        this.loadPaymentRules();
      },
      error: () => {
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
      }
    });
  }

  loadSuspiciousTransactions(): void {
    this.loading = true;
    this.transactionService.getSuspiciousTransactions().subscribe({
      next: (transactions) => {
        this.suspiciousTransactions = transactions;
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement des transactions suspectes', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onReviewTransaction(suspicious: SuspiciousTransaction, action: 'approve' | 'reject' | 'flag'): void {
    if (action === 'approve') {
      suspicious.status = 'resolved';
      this.snackBar.open('Transaction approved', 'Close', { duration: 2000 });
    } else if (action === 'reject') {
      suspicious.status = 'false_positive';
      this.snackBar.open('Transaction marked as false positive', 'Close', { duration: 2000 });
    } else {
      suspicious.status = 'reviewed';
      this.snackBar.open('Transaction flagged for further review', 'Close', { duration: 2000 });
    }
  }

  getSeverityColor(severity: string): string {
    switch (severity) {
      case 'critical': return 'warn';
      case 'high': return 'warn';
      case 'medium': return 'accent';
      case 'low': return 'primary';
      default: return '';
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'pending': return 'accent';
      case 'reviewed': return 'primary';
      case 'resolved': return 'primary';
      case 'false_positive': return '';
      default: return '';
    }
  }
}
