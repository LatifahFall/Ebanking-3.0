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
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../core/services/user.service';
import { User, UserRole } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ClientFormDialogComponent, ClientFormData } from '../../shared/components/client-form-dialog/client-form-dialog.component';
import { AssignClientDialogComponent, AssignClientDialogData } from '../../shared/components/assign-client-dialog/assign-client-dialog.component';

export interface AgentPerformance {
  agentId: string;
  totalClients: number;
  activeClients: number;
  newClientsThisMonth: number;
  pendingKyc: number;
  verifiedKyc: number;
}

@Component({
  selector: 'app-admin-agents',
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
    MatSelectModule,
    MatDialogModule,
    MatChipsModule,
    MatTooltipModule,
    MatMenuModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    PageHeaderComponent,
    LoaderComponent
  ],
  templateUrl: './admin-agents.component.html',
  styleUrl: './admin-agents.component.scss'
})
export class AdminAgentsComponent implements OnInit {
  agents: User[] = [];
  agentPerformance: Map<string, AgentPerformance> = new Map();
  displayedColumns: string[] = ['name', 'email', 'clients', 'performance', 'actions'];
  loading = false;
  errorMessage = '';
  searchQuery = '';
  clients: User[] = [];

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAgents();
    this.loadClients();
  }

  loadAgents(): void {
    this.loading = true;
    this.userService.searchUsers('', UserRole.AGENT, 0, 1000).subscribe({
      next: (result) => {
        this.agents = result.users;
        this.loadAgentPerformance();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Failed to load agents.';
      }
    });
  }

  loadClients(): void {
    this.userService.searchUsers('', UserRole.CLIENT, 0, 1000).subscribe({
      next: (result) => {
        this.clients = result.users;
      }
    });
  }

  loadAgentPerformance(): void {
    this.agents.forEach(agent => {
      this.userService.getAgentClients(agent.id).subscribe({
        next: (clients) => {
          const now = new Date();
          const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

          const totalClients = clients.length;
          const activeClients = clients.filter(c => c.status === 'ACTIVE').length;
          const newClientsThisMonth = clients.filter(c => {
            const created = new Date(c.createdAt);
            return created >= startOfMonth;
          }).length;
          const pendingKyc = clients.filter(c => c.kycStatus === 'IN_PROGRESS' || c.kycStatus === 'NOT_STARTED').length;
          const verifiedKyc = clients.filter(c => c.kycStatus === 'VERIFIED').length;

          this.agentPerformance.set(agent.id, {
            agentId: agent.id,
            totalClients,
            activeClients,
            newClientsThisMonth,
            pendingKyc,
            verifiedKyc
          });
        }
      });
    });
  }

  getPerformance(agentId: string): AgentPerformance | null {
    return this.agentPerformance.get(agentId) || null;
  }

  onCreateAgent(): void {
    const dialogRef = this.dialog.open(ClientFormDialogComponent, {
      width: '500px',
      data: { title: 'Create New Agent', client: null }
    });

    dialogRef.afterClosed().subscribe((result: any) => {
      if (result && result.email) {
        this.userService.createUser({
          email: result.email,
          firstName: result.firstName,
          lastName: result.lastName,
          phoneNumber: result.phoneNumber,
          role: UserRole.AGENT
        }).subscribe({
          next: () => {
            this.snackBar.open('Agent created successfully!', 'Close', { duration: 3000 });
            this.loadAgents();
          },
          error: (error) => {
            this.snackBar.open(`Failed to create agent: ${error.message || 'Unknown error'}`, 'Close', { duration: 5000 });
          }
        });
      }
    });
  }

  onDeleteAgent(agent: User): void {
    if (confirm(`Are you sure you want to deactivate agent ${agent.fullName}?`)) {
      this.loading = true;
      this.userService.deactivateUser(agent.id).subscribe({
        next: () => {
          this.loading = false;
          this.snackBar.open(`Agent ${agent.fullName} has been deactivated`, 'Close', { duration: 3000 });
          this.loadAgents();
        },
        error: () => {
          this.loading = false;
          this.snackBar.open('Failed to deactivate agent', 'Close', { duration: 3000 });
        }
      });
    }
  }

  onAssignClients(agent: User): void {
    // Get currently assigned clients to filter them out
    this.userService.getAgentClients(agent.id).subscribe({
      next: (assignedClients) => {
        const assignedClientIds = assignedClients.map(c => c.id);

        if (assignedClientIds.length >= this.clients.length) {
          this.snackBar.open('All clients are already assigned to this agent', 'Close', { duration: 3000 });
          return;
        }

        // Open dialog to select a client
        const dialogRef = this.dialog.open(AssignClientDialogComponent, {
          width: '500px',
          data: {
            agent: agent,
            clients: this.clients,
            assignedClientIds: assignedClientIds
          } as AssignClientDialogData
        });

        dialogRef.afterClosed().subscribe((result: { clientId: string } | undefined) => {
          if (result && result.clientId) {
            this.loading = true;
            this.userService.assignClientToAgent(agent.id, result.clientId).subscribe({
              next: () => {
                this.loading = false;
                this.snackBar.open('Client assigned successfully', 'Close', { duration: 3000 });
                this.loadAgentPerformance();
              },
              error: () => {
                this.loading = false;
                this.snackBar.open('Failed to assign client', 'Close', { duration: 3000 });
              }
            });
          }
        });
      },
      error: () => {
        // If we can't get assigned clients, proceed with all clients
        const dialogRef = this.dialog.open(AssignClientDialogComponent, {
          width: '500px',
          data: {
            agent: agent,
            clients: this.clients,
            assignedClientIds: []
          } as AssignClientDialogData
        });

        dialogRef.afterClosed().subscribe((result: { clientId: string } | undefined) => {
          if (result && result.clientId) {
            this.loading = true;
            this.userService.assignClientToAgent(agent.id, result.clientId).subscribe({
              next: () => {
                this.loading = false;
                this.snackBar.open('Client assigned successfully', 'Close', { duration: 3000 });
                this.loadAgentPerformance();
              },
              error: () => {
                this.loading = false;
                this.snackBar.open('Failed to assign client', 'Close', { duration: 3000 });
              }
            });
          }
        });
      }
    });
  }

  onViewAgentDetails(agent: User): void {
    // Navigate to agent details page (could be created separately)
    this.snackBar.open(`Viewing details for ${agent.fullName}`, 'Close', { duration: 2000 });
  }

  onFilterAgents(): void {
    if (!this.searchQuery.trim()) {
      this.loadAgents();
      return;
    }

    const query = this.searchQuery.toLowerCase();
    this.agents = this.agents.filter(agent =>
      agent.fullName.toLowerCase().includes(query) ||
      agent.email.toLowerCase().includes(query)
    );
  }
}

