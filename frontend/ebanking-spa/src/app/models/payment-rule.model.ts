import {Component} from '@angular/core';

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

export interface PaymentRule {
  id?: string | number;
  uuid?: string;
  name?: string;
  description?: string;
  enabled?: boolean;
  isActive?: boolean;
}

