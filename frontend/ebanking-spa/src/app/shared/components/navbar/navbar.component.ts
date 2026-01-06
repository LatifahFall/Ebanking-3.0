import { Component, OnInit, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { FormsModule } from '@angular/forms';
import { map, startWith } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';
import { User, UserRole } from '../../../models';

interface SearchFeature {
  label: string;
  route: string;
  icon: string;
  description: string;
}

/**
 * Top Navbar Component
 * Profile dropdown, notifications, theme toggle, breadcrumbs
 */
@Component({
  selector: 'app-navbar',
  imports: [
    CommonModule, 
    RouterModule, 
    MatIconModule, 
    MatMenuModule, 
    MatButtonModule,
    MatBadgeModule,
    MatTooltipModule,
    MatDividerModule,
    MatAutocompleteModule,
    FormsModule,
    NotificationBellComponent
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  isSidebarCollapsed = input<boolean>(false);
  currentUser: User | null = null;
  searchQuery = '';
  filteredFeatures$!: Observable<SearchFeature[]>;
  
  // Features available based on role
  clientFeatures: SearchFeature[] = [
    { label: 'Dashboard', route: '/dashboard', icon: 'dashboard', description: 'View your account overview' },
    { label: 'My Accounts', route: '/accounts', icon: 'account_balance_wallet', description: 'Manage your accounts' },
    { label: 'Transactions', route: '/transactions', icon: 'receipt_long', description: 'View transaction history' },
    { label: 'Payments', route: '/payments', icon: 'payment', description: 'Make payments and transfers' },
    { label: 'Crypto Wallet', route: '/crypto', icon: 'currency_bitcoin', description: 'Manage cryptocurrency' },
    { label: 'Analytics', route: '/analytics', icon: 'analytics', description: 'View financial analytics' },
    { label: 'Profile', route: '/profile', icon: 'person', description: 'Manage your profile' },
    { label: 'Preferences', route: '/profile/preferences', icon: 'settings', description: 'Account preferences' },
    { label: 'Security Settings', route: '/profile/security', icon: 'security', description: 'Security and password' }
  ];

  agentFeatures: SearchFeature[] = [
    { label: 'Agent Dashboard', route: '/agent/dashboard', icon: 'dashboard', description: 'Agent overview' },
    { label: 'My Clients', route: '/agent/clients', icon: 'supervisor_account', description: 'Manage assigned clients' },
    { label: 'Client Operations', route: '/agent/payments', icon: 'payment', description: 'Perform client operations' },
    { label: 'Client Support', route: '/agent/support', icon: 'support_agent', description: 'Handle support tickets' },
    { label: 'Reports', route: '/agent/reports', icon: 'assessment', description: 'View agent reports' },
    { label: 'Analytics', route: '/analytics', icon: 'analytics', description: 'View financial analytics' },
    { label: 'Profile', route: '/profile', icon: 'person', description: 'Manage your profile' },
    { label: 'Preferences', route: '/profile/preferences', icon: 'settings', description: 'Account preferences' },
    { label: 'Security Settings', route: '/profile/security', icon: 'security', description: 'Security and password' }
  ];

  adminFeatures: SearchFeature[] = [
    { label: 'Admin Dashboard', route: '/admin/dashboard', icon: 'admin_panel_settings', description: 'Admin overview' },
    { label: 'User Management', route: '/admin/users', icon: 'manage_accounts', description: 'Manage all users' },
    { label: 'Agent Management', route: '/admin/agents', icon: 'supervisor_account', description: 'Manage agents' },
    { label: 'System Management', route: '/admin/system', icon: 'settings', description: 'System configuration' },
    { label: 'Analytics', route: '/analytics', icon: 'analytics', description: 'View financial analytics' },
    { label: 'Profile', route: '/profile', icon: 'person', description: 'Manage your profile' },
    { label: 'Preferences', route: '/profile/preferences', icon: 'settings', description: 'Account preferences' },
    { label: 'Security Settings', route: '/profile/security', icon: 'security', description: 'Security and password' }
  ];

  constructor(
    public authService: AuthService,
    public themeService: ThemeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    // Setup filtered features observable
    this.filteredFeatures$ = this.authService.currentUser$.pipe(
      map(user => {
        const features = this.getFeaturesForRole(user?.role);
        return features;
      }),
      startWith(this.getFeaturesForRole(this.currentUser?.role))
    );
  }

  getFeaturesForRole(role?: UserRole): SearchFeature[] {
    switch (role) {
      case UserRole.AGENT:
        return this.agentFeatures;
      case UserRole.ADMIN:
        return this.adminFeatures;
      default:
        return this.clientFeatures;
    }
  }

  filterFeatures(query: string): SearchFeature[] {
    if (!query) return [];
    const features = this.getFeaturesForRole(this.currentUser?.role);
    const term = query.toLowerCase();
    return features.filter(f => 
      f.label.toLowerCase().includes(term) ||
      f.description.toLowerCase().includes(term)
    );
  }

  displayFeature(feature: SearchFeature | null): string {
    return feature ? feature.label : '';
  }

  onFeatureSelected(feature: SearchFeature): void {
    this.searchQuery = '';
    this.router.navigate([feature.route]);
  }

  onLogout(): void {
    this.authService.logout().subscribe();
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }

  getUserInitials(): string {
    if (!this.currentUser) return '';
    return `${this.currentUser.firstName[0]}${this.currentUser.lastName[0]}`.toUpperCase();
  }

  onSearch(): void {
    // Search is handled by autocomplete selection
    // If user presses enter without selecting, navigate to first result
    if (!this.searchQuery.trim()) return;
    
    const features = this.filterFeatures(this.searchQuery);
    if (features.length > 0) {
      this.router.navigate([features[0].route]);
      this.searchQuery = '';
    }
  }
}

