import { Component, OnInit, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';
import { NotificationBellComponent } from '../notification-bell/notification-bell.component';
import { User } from '../../../models';

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
    NotificationBellComponent
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements OnInit {
  isSidebarCollapsed = input<boolean>(false);
  currentUser: User | null = null;

  constructor(
    public authService: AuthService,
    public themeService: ThemeService
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
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
}

