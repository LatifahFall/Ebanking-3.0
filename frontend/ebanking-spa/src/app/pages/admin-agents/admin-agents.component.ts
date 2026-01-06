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
import {ClientFormData} from '../../shared/components/client-form-dialog/client-form-dialog.component'
import { UserService } from '../../core/services/user.service';
import { User, UserRole } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { ClientFormDialogComponent } from '../../shared/components/client-form-dialog/client-form-dialog.component';
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
  clients: User[] = [];
  agentPerformance: Map<string, AgentPerformance> = new Map();

  displayedColumns: string[] = ['name', 'email', 'clients', 'performance', 'actions'];
  loading = false;
  errorMessage = '';
  searchQuery = '';

  constructor(
    private userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAgents();
    this.loadClients();
  }

  /** Charge la liste des agents */
  loadAgents(): void {
    this.loading = true;
    // Utilise getAllUsers puis filtre sur le rôle AGENT
    this.userService.getAllUsers().subscribe({
      next: (users: User[]) => {
        this.agents = users.filter(u => u.role === UserRole.AGENT);
        this.loading = false;
        this.loadAgentPerformance();
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des agents', err);
        this.errorMessage = 'Impossible de charger les agents';
        this.loading = false;
        this.snackBar.open('Erreur de chargement des agents', 'Close', { duration: 5000 });
      }
    });
  }

  /** Charge tous les clients (pour l'assignation) */
  loadClients(): void {
    // Utilise getAllUsers puis filtre sur le rôle CLIENT
    this.userService.getAllUsers().subscribe({
      next: (users: User[]) => {
        this.clients = users.filter(u => u.role === UserRole.CLIENT);
      },
      error: (err: any) => {
        console.error('Erreur chargement clients', err);
        this.snackBar.open('Erreur lors du chargement des clients', 'Close', { duration: 5000 });
      }
    });
  }

  /** Calcule les performances pour chaque agent */
  loadAgentPerformance(): void {
    this.agentPerformance.clear();

    if (this.agents.length === 0) return;

    this.agents.forEach(agent => {
      this.userService.getAgentsClients(agent.id).subscribe({  // Note le "s" : getAgentsClients
        next: (clients: User[]) => {
          const now = new Date();
          const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);

          const totalClients = clients.length;
          const activeClients = clients.filter((c: User) => c.status === 'ACTIVE').length;

          const newClientsThisMonth = clients.filter((c: User) => {
            if (!c.createdAt) return false;
            const created = new Date(c.createdAt);
            return created >= startOfMonth;
          }).length;

          const pendingKyc = clients.filter((c: User) =>
            c.kycStatus === 'IN_PROGRESS' || c.kycStatus === 'NOT_STARTED'
          ).length;

          const verifiedKyc = clients.filter((c: User) => c.kycStatus === 'VERIFIED').length;

          this.agentPerformance.set(agent.id, {
            agentId: agent.id,
            totalClients,
            activeClients,
            newClientsThisMonth,
            pendingKyc,
            verifiedKyc
          });
        },
        error: (err) => {
          console.error(`Erreur performance agent ${agent.id}`, err);
          // On met des zéros en cas d'erreur
          this.agentPerformance.set(agent.id, {
            agentId: agent.id,
            totalClients: 0,
            activeClients: 0,
            newClientsThisMonth: 0,
            pendingKyc: 0,
            verifiedKyc: 0
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
      data: { title: 'Créer un nouvel agent', client: undefined } as ClientFormData
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
            this.snackBar.open('Agent créé avec succès !', 'Fermer', { duration: 3000 });
            this.loadAgents();
          },
          error: (error) => {
            this.snackBar.open(`Échec création agent : ${error.message || 'Erreur inconnue'}`, 'Fermer', { duration: 5000 });
          }
        });
      }
    });
  }

  onDeleteAgent(agent: User): void {
    if (confirm(`Voulez-vous vraiment désactiver l'agent ${agent.fullName} ?`)) {
      this.loading = true;
      this.userService.deactivateUser(agent.id).subscribe({
        next: () => {
          this.snackBar.open(`Agent ${agent.fullName} désactivé`, 'Fermer', { duration: 3000 });
          this.loadAgents();
        },
        error: () => {
          this.snackBar.open('Échec désactivation agent', 'Fermer', { duration: 5000 });
          this.loading = false;
        }
      });
    }
  }

  onAssignClients(agent: User): void {
    this.userService.getAgentsClients(agent.id).subscribe({
      next: (assignedClients: User[]) => {
        const assignedClientIds = assignedClients.map(c => c.id);

        const dialogRef = this.dialog.open(AssignClientDialogComponent, {
          width: '600px',
          data: {
            agent,
            clients: this.clients,
            assignedClientIds
          } as AssignClientDialogData
        });

        dialogRef.afterClosed().subscribe((result: { clientId: string } | undefined) => {
          if (result?.clientId) {
            this.userService.assignClientToAgent(agent.id, result.clientId).subscribe({
              next: () => {
                this.snackBar.open('Client assigné avec succès', 'Fermer', { duration: 3000 });
                this.loadAgentPerformance(); // Rafraîchir les stats
              },
              error: () => {
                this.snackBar.open('Échec assignation client', 'Fermer', { duration: 5000 });
              }
            });
          }
        });
      },
      error: () => {
        // En cas d'erreur, on propose tous les clients
        this.dialog.open(AssignClientDialogComponent, {
          width: '600px',
          data: {
            agent,
            clients: this.clients,
            assignedClientIds: []
          } as AssignClientDialogData
        });
      }
    });
  }

  onViewAgentDetails(agent: User): void {
    this.snackBar.open(`Détails de l'agent ${agent.fullName} (à implémenter)`, 'Fermer', { duration: 3000 });
  }

  onFilterAgents(): void {
    if (!this.searchQuery.trim()) {
      this.loadAgents();
      return;
    }

    const query = this.searchQuery.toLowerCase();
    this.agents = this.agents.filter(agent =>
      agent.fullName?.toLowerCase().includes(query) ||
      agent.email?.toLowerCase().includes(query)
    );
  }
}
