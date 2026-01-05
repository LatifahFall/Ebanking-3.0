import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../models';

/**
 * Sidebar Component
 * Collapsible navigation sidebar with role-based menu items
 */

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  roles?: UserRole[];
  badge?: number;
}

@Component({
  selector: 'app-sidebar',
  imports: [CommonModule, RouterModule, MatIconModule, MatTooltipModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss'
})
export class SidebarComponent {
  isCollapsed = input<boolean>(false);
  collapsedChange = output<boolean>();

  menuItems: MenuItem[] = [
    // Client Section
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'My Accounts', icon: 'account_balance_wallet', route: '/accounts' },
    { label: 'Transactions', icon: 'receipt_long', route: '/transactions' },
    { label: 'Payments', icon: 'payment', route: '/payments' },
    { label: 'Crypto Wallet', icon: 'currency_bitcoin', route: '/crypto' },
    { label: 'Analytics', icon: 'analytics', route: '/analytics' },
    { label: 'Profile', icon: 'person', route: '/profile' },

    // Employee Section
    {
      label: 'Customer Management',
      icon: 'supervisor_account',
      route: '/employee/customers',
      roles: [UserRole.ADMIN, UserRole.SUPER_ADMIN]
    },
    {
      label: 'Transactions Review',
      icon: 'fact_check',
      route: '/employee/transactions',
      roles: [UserRole.ADMIN, UserRole.SUPER_ADMIN]
    },

    // Admin Section
    {
      label: 'Admin Dashboard',
      icon: 'admin_panel_settings',
      route: '/admin',
      roles: [UserRole.ADMIN, UserRole.SUPER_ADMIN]
    },
    {
      label: 'User Management',
      icon: 'manage_accounts',
      route: '/admin/users',
      roles: [UserRole.SUPER_ADMIN]
    },
    {
      label: 'System Settings',
      icon: 'settings',
      route: '/admin/settings',
      roles: [UserRole.SUPER_ADMIN]
    },
    {
      label: 'Audit Logs',
      icon: 'history',
      route: '/admin/audit',
      roles: [UserRole.ADMIN, UserRole.SUPER_ADMIN]
    }
  ];

  constructor(private authService: AuthService) {}

  toggleSidebar(): void {
    this.collapsedChange.emit(!this.isCollapsed());
  }

  // Helper to decide display of Audit Logs (used by template)
  canShowAudit(): boolean {
    const user = this.authService.getCurrentUser();
    return !!user && ([UserRole.ADMIN, UserRole.SUPER_ADMIN].includes(user.role as UserRole));
  }

  canShowItem(item: MenuItem): boolean {
    if (!item.roles || item.roles.length === 0) {
      return true;
    }

    const user = this.authService.getCurrentUser();
    return item.roles.includes(user?.role as UserRole);
  }
}
