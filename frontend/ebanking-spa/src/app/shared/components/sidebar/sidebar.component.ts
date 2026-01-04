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

  // Client menu items
  clientMenuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'My Accounts', icon: 'account_balance_wallet', route: '/accounts' },
    { label: 'Transactions', icon: 'receipt_long', route: '/transactions' },
    { label: 'Payments', icon: 'payment', route: '/payments' },
    { label: 'Crypto Wallet', icon: 'currency_bitcoin', route: '/crypto' },
    { label: 'Analytics', icon: 'analytics', route: '/analytics' },
    { label: 'Profile', icon: 'person', route: '/profile' }
  ];

  // Agent menu items
  agentMenuItems: MenuItem[] = [
    { label: 'Agent Dashboard', icon: 'dashboard', route: '/agent/dashboard' },
    { label: 'My Clients', icon: 'supervisor_account', route: '/agent/clients' },
    { label: 'Client Operations', icon: 'payment', route: '/agent/payments' },
    { label: 'Client Support', icon: 'support_agent', route: '/agent/support' },
    { label: 'Reports', icon: 'assessment', route: '/agent/reports' },
    { label: 'Profile', icon: 'person', route: '/profile' }
  ];

  // Admin menu items
  adminMenuItems: MenuItem[] = [
    { label: 'Admin Dashboard', icon: 'admin_panel_settings', route: '/admin/dashboard' },
    { label: 'User Management', icon: 'manage_accounts', route: '/admin/users' },
    { label: 'Agent Management', icon: 'supervisor_account', route: '/admin/agents' },
    { label: 'System Management', icon: 'settings', route: '/admin/system' },
    { label: 'Profile', icon: 'person', route: '/profile' }
  ];

  get menuItems(): MenuItem[] {
    const user = this.authService.getCurrentUser();
    if (!user) return this.clientMenuItems;

    switch (user.role) {
      case UserRole.AGENT:
        return this.agentMenuItems;
      case UserRole.ADMIN:
        return this.adminMenuItems;
      default:
        return this.clientMenuItems;
    }
  }

  constructor(private authService: AuthService) {}

  toggleSidebar(): void {
    this.collapsedChange.emit(!this.isCollapsed());
  }
}
