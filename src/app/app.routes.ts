import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { LoginComponent } from './pages/login/login.component';
import { MfaComponent } from './pages/mfa/mfa.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { AccountsComponent } from './pages/accounts/accounts.component';

export const routes: Routes = [
  // Redirect root to dashboard or login
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  // Auth routes (Login, MFA) - use AuthLayout
  {
    path: 'auth',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    children: [
      {
        path: 'login',
        component: LoginComponent,
        title: 'Login - E-Banking 3.0'
      },
      {
        path: 'mfa',
        component: MfaComponent,
        title: 'Two-Factor Authentication - E-Banking 3.0'
      },
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full'
      }
    ]
  },

  // Main app routes - use MainLayout with auth guard
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        component: DashboardComponent,
        title: 'Dashboard - E-Banking 3.0'
      },
      {
        path: 'accounts',
        component: AccountsComponent,
        title: 'Accounts - E-Banking 3.0'
      },
      // Add more routes as needed
      // {
      //   path: 'transactions',
      //   loadComponent: () => import('./pages/transactions/transactions.component').then(m => m.TransactionsComponent),
      //   title: 'Transactions - E-Banking 3.0'
      // },
      // {
      //   path: 'payments',
      //   loadComponent: () => import('./pages/payments/payments.component').then(m => m.PaymentsComponent),
      //   title: 'Payments - E-Banking 3.0'
      // },
      // {
      //   path: 'crypto',
      //   loadComponent: () => import('./pages/crypto/crypto.component').then(m => m.CryptoComponent),
      //   title: 'Crypto Wallet - E-Banking 3.0'
      // },
      // {
      //   path: 'analytics',
      //   loadComponent: () => import('./pages/analytics/analytics.component').then(m => m.AnalyticsComponent),
      //   title: 'Analytics - E-Banking 3.0'
      // },
      // {
      //   path: 'notifications',
      //   loadComponent: () => import('./pages/notifications/notifications.component').then(m => m.NotificationsComponent),
      //   title: 'Notifications - E-Banking 3.0'
      // },
      // {
      //   path: 'profile',
      //   loadComponent: () => import('./pages/profile/profile.component').then(m => m.ProfileComponent),
      //   title: 'Profile - E-Banking 3.0'
      // },
      // {
      //   path: 'admin',
      //   loadComponent: () => import('./pages/admin/admin.component').then(m => m.AdminComponent),
      //   canActivate: [adminGuard],
      //   title: 'Admin Panel - E-Banking 3.0'
      // }
    ]
  },

  // Wildcard route - 404
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];

