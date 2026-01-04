export interface SupportTicket {
  id: string;
  clientId: string;
  clientName: string;
  subject: string;
  description: string;
  status: TicketStatus;
  priority: TicketPriority;
  category: TicketCategory;
  createdAt: Date;
  updatedAt: Date;
  assignedTo?: string;
  notes: TicketNote[];
}

export interface TicketNote {
  id: string;
  ticketId: string;
  authorId: string;
  authorName: string;
  content: string;
  createdAt: Date;
  isInternal: boolean;
}

export enum TicketStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export enum TicketPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  URGENT = 'URGENT'
}

export enum TicketCategory {
  ACCOUNT_ISSUE = 'ACCOUNT_ISSUE',
  TRANSACTION = 'TRANSACTION',
  TECHNICAL = 'TECHNICAL',
  GENERAL = 'GENERAL',
  KYC = 'KYC'
}

