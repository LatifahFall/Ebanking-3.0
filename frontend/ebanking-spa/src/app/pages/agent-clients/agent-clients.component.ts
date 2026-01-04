import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { User, UserRole } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { CustomButtonComponent } from '../../shared/components/custom-button/custom-button.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ClientFormDialogComponent, ClientFormData } from '../../shared/components/client-form-dialog/client-form-dialog.component';

@Component({
  selector: 'app-agent-clients',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDialogModule,
    MatPaginatorModule,
    MatChipsModule,
    MatTooltipModule,
    PageHeaderComponent,
    CustomButtonComponent,
    LoaderComponent
  ],
  templateUrl: './agent-clients.component.html',
  styleUrl: './agent-clients.component.scss'
})
export class AgentClientsComponent implements OnInit {
  clients: User[] = [];
  displayedColumns: string[] = ['name', 'email', 'phone', 'status', 'kycStatus', 'actions'];
  loading = false;
  errorMessage = '';
  searchQuery = '';
  
  // Pagination
  page = 0;
  pageSize = 10;
  totalClients = 0;
  
  currentAgentId: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.role === UserRole.AGENT) {
      this.currentAgentId = currentUser.id;
      this.loadClients();
    } else {
      this.errorMessage = 'Access denied. Agent role required.';
    }
  }

  loadClients(): void {
    if (!this.currentAgentId) return;
    
    this.loading = true;
    this.errorMessage = '';
    
    this.userService.searchAssignedClients(
      this.currentAgentId,
      this.searchQuery || undefined,
      this.page,
      this.pageSize
    ).subscribe({
      next: (result) => {
        this.clients = result.clients;
        this.totalClients = result.total;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load clients. Please try again.';
      }
    });
  }

  onSearch(): void {
    this.page = 0;
    this.loadClients();
  }

  onCreateClient(): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '600px',
      data: {} as ClientFormData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.currentAgentId) {
        this.loading = true;
        this.userService.createAssignedClient(this.currentAgentId, result).subscribe({
          next: () => {
            this.loadClients();
          },
          error: () => {
            this.loading = false;
            this.errorMessage = 'Failed to create client. Please try again.';
          }
        });
      }
    });
  }

  onEditClient(client: User): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '600px',
      data: { client } as ClientFormData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && this.currentAgentId) {
        this.loading = true;
        this.userService.updateClientProfile(this.currentAgentId, client.id, result).subscribe({
          next: () => {
            this.loadClients();
          },
          error: () => {
            this.loading = false;
            this.errorMessage = 'Failed to update client. Please try again.';
          }
        });
      }
    });
  }

  onToggleStatus(client: User): void {
    if (!this.currentAgentId) return;
    
    this.loading = true;
    const action = client.status === 'ACTIVE' 
      ? this.userService.deactivateClient(this.currentAgentId, client.id)
      : this.userService.activateClient(this.currentAgentId, client.id);
    
    action.subscribe({
      next: () => {
        this.loadClients();
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to update client status.';
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadClients();
  }

  getStatusColor(status: string): string {
    return status === 'ACTIVE' ? 'primary' : 'warn';
  }

  getKycStatusColor(kycStatus: string): string {
    switch (kycStatus) {
      case 'VERIFIED': return 'primary';
      case 'PENDING': return 'accent';
      case 'REJECTED': return 'warn';
      default: return '';
    }
  }
}
