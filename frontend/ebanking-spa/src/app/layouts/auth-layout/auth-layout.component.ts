import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

/**
 * Auth Layout Component
 * Minimalist layout for authentication pages (Login, MFA, etc.)
 */
@Component({
  selector: 'app-auth-layout',
  imports: [CommonModule, RouterModule],
  templateUrl: './auth-layout.component.html',
  styleUrl: './auth-layout.component.scss'
})
export class AuthLayoutComponent {
  currentYear = new Date().getFullYear();
}

