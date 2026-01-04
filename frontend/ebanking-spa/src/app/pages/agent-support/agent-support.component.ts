import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { LoaderComponent } from '../../shared/components/loader/loader.component';
import { SupportTicket, TicketStatus, TicketPriority, TicketCategory, TicketNote } from '../../models/support.model';

@Component({
  selector: 'app-agent-support',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTabsModule,
    MatSnackBarModule,
    PageHeaderComponent,
    LoaderComponent
  ],
  templateUrl: './agent-support.component.html',
  styleUrl: './agent-support.component.scss'
})
export class AgentSupportComponent implements OnInit {
  tickets: SupportTicket[] = [];
  filteredTickets: SupportTicket[] = [];
  selectedTicket: SupportTicket | null = null;
  loading = false;
  currentAgentId: string | null = null;
  
  // Filters
  statusFilter: string = 'ALL';
  priorityFilter: string = 'ALL';
  
  displayedColumns: string[] = ['client', 'subject', 'category', 'priority', 'status', 'createdAt', 'actions'];
  newNote = '';

  constructor(
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const currentUser = this.authService.getCurrentUser();
    if (currentUser && currentUser.role === UserRole.AGENT) {
      this.currentAgentId = currentUser.id;
      this.loadTickets();
    }
  }

  loadTickets(): void {
    this.loading = true;
    // TODO: Replace with real API call when backend supports it
    // Mock data for now
    setTimeout(() => {
      this.tickets = this.getMockTickets();
      this.applyFilters();
      this.loading = false;
    }, 500);
  }

  applyFilters(): void {
    this.filteredTickets = [...this.tickets];
    
    if (this.statusFilter !== 'ALL') {
      this.filteredTickets = this.filteredTickets.filter(t => t.status === this.statusFilter);
    }
    
    if (this.priorityFilter !== 'ALL') {
      this.filteredTickets = this.filteredTickets.filter(t => t.priority === this.priorityFilter);
    }
  }

  onFilterChange(): void {
    this.applyFilters();
  }

  onViewTicket(ticket: SupportTicket): void {
    this.selectedTicket = ticket;
  }

  onUpdateStatus(ticket: SupportTicket, status: TicketStatus): void {
    ticket.status = status;
    ticket.updatedAt = new Date();
    this.showSuccess('Ticket status updated.');
    // TODO: Call API to update status
  }

  onAddNote(): void {
    if (!this.selectedTicket || !this.newNote.trim()) return;
    
    const note: TicketNote = {
      id: `note-${Date.now()}`,
      ticketId: this.selectedTicket.id,
      authorId: this.currentAgentId || '',
      authorName: 'Agent',
      content: this.newNote,
      createdAt: new Date(),
      isInternal: false
    };
    
    this.selectedTicket.notes.push(note);
    this.newNote = '';
    this.showSuccess('Note added successfully.');
    // TODO: Call API to save note
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'URGENT': return 'warn';
      case 'HIGH': return 'accent';
      case 'MEDIUM': return 'primary';
      default: return '';
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'OPEN': return 'primary';
      case 'IN_PROGRESS': return 'accent';
      case 'RESOLVED': return 'primary';
      case 'CLOSED': return '';
      default: return '';
    }
  }

  private getMockTickets(): SupportTicket[] {
    return [
      {
        id: 'ticket-001',
        clientId: 'usr-123456',
        clientName: 'John Doe',
        subject: 'Account access issue',
        description: 'Cannot access my account after password reset',
        status: TicketStatus.OPEN,
        priority: TicketPriority.HIGH,
        category: TicketCategory.ACCOUNT_ISSUE,
        createdAt: new Date('2026-01-02'),
        updatedAt: new Date('2026-01-02'),
        notes: []
      },
      {
        id: 'ticket-002',
        clientId: 'usr-123457',
        clientName: 'Jane Smith',
        subject: 'Transaction inquiry',
        description: 'Need clarification on a recent transaction',
        status: TicketStatus.IN_PROGRESS,
        priority: TicketPriority.MEDIUM,
        category: TicketCategory.TRANSACTION,
        createdAt: new Date('2026-01-01'),
        updatedAt: new Date('2026-01-03'),
        notes: [
          {
            id: 'note-001',
            ticketId: 'ticket-002',
            authorId: this.currentAgentId || '',
            authorName: 'Agent',
            content: 'Contacted client, waiting for response',
            createdAt: new Date('2026-01-02'),
            isInternal: false
          }
        ]
      }
    ];
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', { duration: 3000 });
  }

  // Export enum for template
  TicketStatus = TicketStatus;
}

