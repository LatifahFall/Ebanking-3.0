import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../../models';

/**
 * Dashboard Redirect Guard
 * Redirects users to their role-specific dashboard
 */
export const dashboardRedirectGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/auth/login']);
    return false;
  }

  const user = authService.getCurrentUser();
  if (!user) {
    router.navigate(['/auth/login']);
    return false;
  }

  // Redirect based on role
  if (user.role === UserRole.AGENT) {
    router.navigate(['/agent/dashboard']);
    return false;
  }

  if (user.role === UserRole.ADMIN) {
    router.navigate(['/admin/dashboard']);
    return false;
  }

  // Client stays on regular dashboard
  return true;
};

