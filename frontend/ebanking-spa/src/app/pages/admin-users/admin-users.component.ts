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
import { UserService } from '../../core/services/user.service';
import { User, UserRole, UserStatus } from '../../models';
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
    PageHeaderComponent,
    CustomButtonComponent,
    LoaderComponent
  ],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];
  displayedColumns: string[] = ['name', 'email', 'role', 'status', 'kycStatus', 'actions'];
  loading = false;
  errorMessage = '';
  searchQuery = '';
  selectedRole: UserRole | 'ALL' = 'ALL';
  
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

  // For assignment dialog
  agents: User[] = [];
  clients: User[] = [];

  constructor(
    private userService: UserService,
    private dialog: MatDialog
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
      this.searchQuery || undefined,
      roleFilter,
      this.page,
      this.pageSize
    ).subscribe({
      next: (result) => {
        this.users = result.users;
        this.totalUsers = result.total;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load users. Please try again.';
      }
    });
  }

  loadAgentsAndClients(): void {
    // Load agents and clients for assignment dialog
    this.userService.searchUsers('', UserRole.AGENT, 0, 100).subscribe(result => {
      this.agents = result.users;
    });
    this.userService.searchUsers('', UserRole.CLIENT, 0, 100).subscribe(result => {
      this.clients = result.users;
    });
  }

  onSearch(): void {
    this.page = 0;
    this.loadUsers();
  }

  onRoleFilterChange(): void {
    this.page = 0;
    this.loadUsers();
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
            // Unassign
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
            // Assign to selected agent
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
    this.loadUsers();
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
}
