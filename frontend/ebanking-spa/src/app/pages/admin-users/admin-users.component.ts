import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../../core/services/user.service';
import { User, UserRole, UserStatus, KYCStatus } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ClientFormDialogComponent, ClientFormData } from '../../shared/components/client-form-dialog/client-form-dialog.component';
import { AssignAgentDialogComponent, AssignAgentData } from '../../shared/components/assign-agent-dialog/assign-agent-dialog.component';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    MatPaginatorModule,
    MatChipsModule,
    MatTooltipModule,
    MatMenuModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    PageHeaderComponent,
    CustomButtonComponent,
    LoaderComponent
  ],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  allUsers: User[] = []; // For filtering and export
  displayedColumns: string[] = ['select', 'name', 'email', 'role', 'status', 'kycStatus', 'createdAt', 'actions'];
  loading = false;
  errorMessage = '';
  searchQuery = '';
  selectedRole: UserRole | 'ALL' = 'ALL';
  selectedStatus: UserStatus | 'ALL' = 'ALL';
  selectedKycStatus: KYCStatus | 'ALL' = 'ALL';
  dateFrom: Date | null = null;
  dateTo: Date | null = null;

  // Selection for bulk actions
  selectedUsers = new Set<string>();
  selectAll = false;

  // Pagination
  page = 0;
  pageSize = 10;
  totalUsers = 0;

  // Roles for filter
  roles: { value: UserRole | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'All Roles' },
    { value: UserRole.CLIENT, label: 'Client' },
    { value: UserRole.AGENT, label: 'Agent' },
    { value: UserRole.ADMIN, label: 'Admin' }
  ];

  // Status options
  statuses: { value: UserStatus | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'All Statuses' },
    { value: UserStatus.ACTIVE, label: 'Active' },
    { value: UserStatus.INACTIVE, label: 'Inactive' },
    { value: UserStatus.SUSPENDED, label: 'Suspended' },
    { value: UserStatus.PENDING, label: 'Pending' }
  ];

  // KYC Status options
  kycStatuses: { value: KYCStatus | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'All KYC Statuses' },
    { value: KYCStatus.NOT_STARTED, label: 'Not Started' },
    { value: KYCStatus.IN_PROGRESS, label: 'In Progress' },
    { value: KYCStatus.VERIFIED, label: 'Verified' },
    { value: KYCStatus.REJECTED, label: 'Rejected' }
  ];

  // For assignment dialog
  agents: User[] = [];
  clients: User[] = [];

  // User modification history (mock)
  userHistory: Map<string, any[]> = new Map();

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadAgentsAndClients();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    const roleFilter = this.selectedRole === 'ALL' ? undefined : this.selectedRole;
    this.userService.searchUsers(
      this.searchQuery || '',
      roleFilter,
      0,
      10000
    ).subscribe({
      next: (result) => {
        this.allUsers = result;
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load users. Please try again.';
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.allUsers];

    // Role filter
    if (this.selectedRole !== 'ALL') {
      filtered = filtered.filter(u => u.role === this.selectedRole);
    }

    // Status filter
    if (this.selectedStatus !== 'ALL') {
      filtered = filtered.filter(u => u.status === this.selectedStatus);
    }

    // KYC Status filter
    if (this.selectedKycStatus !== 'ALL') {
      filtered = filtered.filter(u => u.kycStatus === this.selectedKycStatus);
    }

    // Date filters
    if (this.dateFrom) {
      filtered = filtered.filter(u => new Date(u.createdAt) >= this.dateFrom!);
    }
    if (this.dateTo) {
      const toDate = new Date(this.dateTo);
      toDate.setHours(23, 59, 59, 999);
      filtered = filtered.filter(u => new Date(u.createdAt) <= toDate);
    }

    // Search filter
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(u =>
        u.fullName.toLowerCase().includes(query) ||
        u.email.toLowerCase().includes(query) ||
        u.firstName.toLowerCase().includes(query) ||
        u.lastName.toLowerCase().includes(query)
      );
    }

    this.totalUsers = filtered.length;
    const start = this.page * this.pageSize;
    this.users = filtered.slice(start, start + this.pageSize);
  }

  loadAgentsAndClients(): void {
    this.userService.searchUsers('', UserRole.AGENT, 0, 100).subscribe(agents => {
      this.agents = agents;
    });
    this.userService.searchUsers('', UserRole.CLIENT, 0, 100).subscribe(clients => {
      this.clients = clients;
    });
  }

  onSearch(): void {
    this.page = 0;
    this.applyFilters();
  }

  onFilterChange(): void {
    this.page = 0;
    this.applyFilters();
  }

  onSelectAll(checked: boolean): void {
    this.selectAll = checked;
    if (checked) {
      this.users.forEach(u => this.selectedUsers.add(u.id));
    } else {
      this.selectedUsers.clear();
    }
  }

  onSelectUser(userId: string, checked: boolean): void {
    if (checked) {
      this.selectedUsers.add(userId);
    } else {
      this.selectedUsers.delete(userId);
      this.selectAll = false;
    }
  }

  isSelected(userId: string): boolean {
    return this.selectedUsers.has(userId);
  }

  onBulkActivate(): void {
    if (this.selectedUsers.size === 0) {
      this.snackBar.open('Please select users to activate', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;
    const userIds = Array.from(this.selectedUsers);
    let completed = 0;
    let failed = 0;

    userIds.forEach(userId => {
      this.userService.activateUser(userId).subscribe({
        next: () => {
          completed++;
          if (completed + failed === userIds.length) {
            this.loading = false;
            this.selectedUsers.clear();
            this.selectAll = false;
            this.loadUsers();
            this.snackBar.open(`Activated ${completed} user(s)`, 'Close', { duration: 3000 });
          }
        },
        error: () => {
          failed++;
          if (completed + failed === userIds.length) {
            this.loading = false;
            this.snackBar.open(`Activated ${completed}, failed ${failed}`, 'Close', { duration: 5000 });
          }
        }
      });
    });
  }

  onBulkDeactivate(): void {
    if (this.selectedUsers.size === 0) {
      this.snackBar.open('Please select users to deactivate', 'Close', { duration: 3000 });
      return;
    }

    this.loading = true;
    const userIds = Array.from(this.selectedUsers);
    let completed = 0;
    let failed = 0;

    userIds.forEach(userId => {
      this.userService.deactivateUser(userId).subscribe({
        next: () => {
          completed++;
          if (completed + failed === userIds.length) {
            this.loading = false;
            this.selectedUsers.clear();
            this.selectAll = false;
            this.loadUsers();
            this.snackBar.open(`Deactivated ${completed} user(s)`, 'Close', { duration: 3000 });
          }
        },
        error: () => {
          failed++;
          if (completed + failed === userIds.length) {
            this.loading = false;
            this.snackBar.open(`Deactivated ${completed}, failed ${failed}`, 'Close', { duration: 5000 });
          }
        }
      });
    });
  }

  onViewHistory(user: User): void {
    const history = this.userHistory.get(user.id) || [];
    if (history.length === 0) {
      this.snackBar.open('No modification history available for this user', 'Close', { duration: 3000 });
    } else {
      // In a real app, this would open a dialog with history
      this.snackBar.open(`User has ${history.length} modification(s) in history`, 'Close', { duration: 3000 });
    }
  }

  onExportCSV(): void {
    const headers = ['ID', 'Name', 'Email', 'Role', 'Status', 'KYC Status', 'Created At', 'Last Login'];
    const rows = this.allUsers.map(u => [
      u.id,
      u.fullName,
      u.email,
      u.role,
      u.status,
      u.kycStatus,
      new Date(u.createdAt).toLocaleDateString(),
      u.lastLogin ? new Date(u.lastLogin).toLocaleDateString() : 'Never'
    ]);

    const csvContent = [
      headers.join(','),
      ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `users_export_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);

    this.snackBar.open('Users exported to CSV', 'Close', { duration: 3000 });
  }

  onExportExcel(): void {
    // For Excel export, we'd typically use a library like xlsx
    // For now, we'll export as CSV with .xlsx extension (basic implementation)
    this.onExportCSV();
    this.snackBar.open('Excel export uses CSV format (install xlsx library for full Excel support)', 'Close', { duration: 5000 });
  }

  onCreateUser(): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '600px',
      data: {} as ClientFormData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        // Create user with CLIENT role by default (can be changed if needed)
        this.userService.createUser({
          ...result,
          role: UserRole.CLIENT
        }).subscribe({
          next: () => {
            this.loadUsers();
            this.loadAgentsAndClients();
          },
          error: () => {
            this.loading = false;
            this.errorMessage = 'Failed to create user. Please try again.';
          }
        });
      }
    });
  }

  onEditUser(user: User): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '600px',
      data: { client: user } as ClientFormData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loading = true;
        this.userService.updateUser(user.id, result).subscribe({
          next: () => {
            this.loadUsers();
            this.loadAgentsAndClients();
          },
          error: () => {
            this.loading = false;
            this.errorMessage = 'Failed to update user. Please try again.';
          }
        });
      }
    });
  }

  onToggleStatus(user: User): void {
    this.loading = true;
    const action = user.status === 'ACTIVE'
      ? this.userService.deactivateUser(user.id)
      : this.userService.activateUser(user.id);

    action.subscribe({
      next: () => {
        // Record in history
        if (!this.userHistory.has(user.id)) {
          this.userHistory.set(user.id, []);
        }
        this.userHistory.get(user.id)!.push({
          action: user.status === 'ACTIVE' ? 'DEACTIVATED' : 'ACTIVATED',
          timestamp: new Date(),
          performedBy: 'Admin'
        });
        this.loadUsers();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to update user status.';
      }
    });
  }

  onAssign(client: User): void {
    this.userService.getClientsAgent(client.id).subscribe(currentAgent => {
      const dialogRef = this.dialog.open(AssignAgentDialogComponent, {
        width: '500px',
        data: {
          client,
          agents: this.agents,
          currentAgentId: currentAgent?.id || null
        } as AssignAgentData
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result !== undefined) {
          if (result === null) {
            if (currentAgent) {
              this.loading = true;
              this.userService.unassignClient(currentAgent.id, client.id).subscribe({
                next: () => {
                  this.errorMessage = '';
                  this.loadUsers();
                },
                error: () => {
                  this.loading = false;
                  this.errorMessage = 'Failed to unassign client.';
                }
              });
            }
          } else if (result) {
            this.loading = true;
            this.userService.assignClientToAgent(result, client.id).subscribe({
              next: () => {
                this.errorMessage = '';
                this.loadUsers();
              },
              error: () => {
                this.loading = false;
                this.errorMessage = 'Failed to assign client to agent.';
              }
            });
          }
        }
      });
    });
  }

  onUnassign(client: User): void {
    this.userService.getClientsAgent(client.id).subscribe(agent => {
      if (agent) {
        this.loading = true;
        this.userService.unassignClient(agent.id, client.id).subscribe({
          next: () => {
            this.errorMessage = '';
            this.loadUsers();
          },
          error: () => {
            this.loading = false;
            this.errorMessage = 'Failed to unassign client.';
          }
        });
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;
    this.applyFilters();
  }

  getStatusColor(status: string): string {
    return status === 'ACTIVE' ? 'primary' : 'warn';
  }

  getKycStatusColor(kycStatus: string): string {
    switch (kycStatus) {
      case 'VERIFIED': return 'primary';
      case 'PENDING':
      case 'IN_PROGRESS': return 'accent';
      case 'REJECTED': return 'warn';
      default: return '';
    }
  }

  getRoleLabel(role: UserRole): string {
    return this.roles.find(r => r.value === role)?.label || role;
  }

  createUser(newUser: Partial<User>): void {
    this.loading = true;
    this.userService.createUser(newUser).subscribe({
      next: (user) => {
        this.snackBar.open('Utilisateur créé', 'Fermer', { duration: 3000 });
        this.loadUsers();
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors de la création', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  updateUser(user: User): void {
    this.loading = true;
    this.userService.updateUser(user.id, user).subscribe({
      next: () => {
        this.snackBar.open('Utilisateur modifié', 'Fermer', { duration: 3000 });
        this.loadUsers();
        this.loading = false;
      },
      error: () => {
        this.snackBar.open('Erreur lors de la modification', 'Fermer', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  activateUser(user: User): void {
    this.userService.activateUser(user.id).subscribe({
      next: () => {
        this.snackBar.open('Utilisateur activé', 'Fermer', { duration: 3000 });
        this.loadUsers();
      },
      error: () => {
        this.snackBar.open('Erreur lors de l’activation', 'Fermer', { duration: 3000 });
      }
    });
  }

  deactivateUser(user: User): void {
    this.userService.deactivateUser(user.id).subscribe({
      next: () => {
        this.snackBar.open('Utilisateur désactivé', 'Fermer', { duration: 3000 });
        this.loadUsers();
      },
      error: () => {
        this.snackBar.open('Erreur lors de la désactivation', 'Fermer', { duration: 3000 });
      }
    });
  }
}
