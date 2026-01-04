import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
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
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
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
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    PageHeaderComponent,
    CustomButtonComponent,
    LoaderComponent
  ],
  templateUrl: './agent-clients.component.html',
  styleUrl: './agent-clients.component.scss'
})
export class AgentClientsComponent implements OnInit {
  clients: User[] = [];
  filteredClients: User[] = [];
  displayedColumns: string[] = ['name', 'email', 'phone', 'status', 'kycStatus', 'createdAt', 'actions'];
  loading = false;
  errorMessage = '';
  searchQuery = '';
  
  // Filters
  statusFilter: string = 'ALL';
  kycFilter: string = 'ALL';
  dateFilter: string = 'ALL';
  
  // Pagination
  page = 0;
  pageSize = 10;
  totalClients = 0;
  
  currentAgentId: string | null = null;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private dialog: MatDialog,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.role === UserRole.AGENT) {
      this.currentAgentId = currentUser.id;
      
      // Check for search query parameter from navbar
      this.route.queryParams.subscribe(params => {
        if (params['search']) {
          this.searchQuery = params['search'];
        }
        this.loadClients();
      });
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
        this.applyFilters();
        this.totalClients = this.filteredClients.length;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load clients. Please try again.';
      }
    });
  }

  applyFilters(): void {
    this.filteredClients = [...this.clients];
    
    // Status filter
    if (this.statusFilter !== 'ALL') {
      this.filteredClients = this.filteredClients.filter(c => c.status === this.statusFilter);
    }
    
    // KYC filter
    if (this.kycFilter !== 'ALL') {
      this.filteredClients = this.filteredClients.filter(c => c.kycStatus === this.kycFilter);
    }
    
    // Date filter
    if (this.dateFilter !== 'ALL') {
      const now = new Date();
      const filterDate = new Date();
      
      switch (this.dateFilter) {
        case 'TODAY':
          filterDate.setHours(0, 0, 0, 0);
          this.filteredClients = this.filteredClients.filter(c => 
            new Date(c.createdAt) >= filterDate
          );
          break;
        case 'WEEK':
          filterDate.setDate(now.getDate() - 7);
          this.filteredClients = this.filteredClients.filter(c => 
            new Date(c.createdAt) >= filterDate
          );
          break;
        case 'MONTH':
          filterDate.setMonth(now.getMonth() - 1);
          this.filteredClients = this.filteredClients.filter(c => 
            new Date(c.createdAt) >= filterDate
          );
          break;
        case 'YEAR':
          filterDate.setFullYear(now.getFullYear() - 1);
          this.filteredClients = this.filteredClients.filter(c => 
            new Date(c.createdAt) >= filterDate
          );
          break;
      }
    }
    
    // Pagination
    const start = this.page * this.pageSize;
    const end = start + this.pageSize;
    this.filteredClients = this.filteredClients.slice(start, end);
  }

  onFilterChange(): void {
    this.page = 0;
    this.applyFilters();
    this.totalClients = this.clients.filter(c => {
      if (this.statusFilter !== 'ALL' && c.status !== this.statusFilter) return false;
      if (this.kycFilter !== 'ALL' && c.kycStatus !== this.kycFilter) return false;
      if (this.dateFilter !== 'ALL') {
        const now = new Date();
        const filterDate = new Date();
        switch (this.dateFilter) {
          case 'TODAY':
            filterDate.setHours(0, 0, 0, 0);
            return new Date(c.createdAt) >= filterDate;
          case 'WEEK':
            filterDate.setDate(now.getDate() - 7);
            return new Date(c.createdAt) >= filterDate;
          case 'MONTH':
            filterDate.setMonth(now.getMonth() - 1);
            return new Date(c.createdAt) >= filterDate;
          case 'YEAR':
            filterDate.setFullYear(now.getFullYear() - 1);
            return new Date(c.createdAt) >= filterDate;
        }
      }
      return true;
    }).length;
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
    this.applyFilters();
  }

  onViewClientDetails(client: User): void {
    this.router.navigate(['/agent/clients', client.id, 'details']);
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
