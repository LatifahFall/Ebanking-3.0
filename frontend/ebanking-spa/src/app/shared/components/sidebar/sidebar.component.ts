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
    
    // Agent Section
    { 
      label: 'My Clients', 
      icon: 'supervisor_account', 
      route: '/agent/clients',
      roles: [UserRole.AGENT]
    },
    
    // Admin Section
    { 
      label: 'Admin Dashboard', 
      icon: 'admin_panel_settings', 
      route: '/admin/dashboard',
      roles: [UserRole.ADMIN]
    },
    { 
      label: 'User Management', 
      icon: 'manage_accounts', 
      route: '/admin/users',
      roles: [UserRole.ADMIN]
    }
  ];

  constructor(private authService: AuthService) {}

  toggleSidebar(): void {
    this.collapsedChange.emit(!this.isCollapsed());
  }

  canShowItem(item: MenuItem): boolean {
    if (!item.roles || item.roles.length === 0) {
      return true;
    }
    
    const user = this.authService.getCurrentUser();
    return item.roles.includes(user?.role as UserRole);
  }
}
