import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SidebarComponent } from '../../shared/components/sidebar/sidebar.component';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';

/**
 * Main Layout Component
 * Contains sidebar, navbar, and main content area
 * Used for authenticated pages
 */
@Component({
  selector: 'app-main-layout',
  imports: [CommonModule, RouterModule, SidebarComponent, NavbarComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  isSidebarCollapsed = signal<boolean>(false);

  onSidebarToggle(collapsed: boolean): void {
    this.isSidebarCollapsed.set(collapsed);
  }
}

