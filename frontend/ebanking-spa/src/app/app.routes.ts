import { Routes } from '@angular/router';
import { authGuard, guestGuard, agentGuard, adminGuard } from './core/guards/auth.guard';
import { dashboardRedirectGuard } from './core/guards/dashboard-redirect.guard';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { LoginComponent } from './pages/login/login.component';
import { MfaComponent } from './pages/mfa/mfa.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { AccountsComponent } from './pages/accounts/accounts.component';
import { RegisterComponent } from './pages/register/register.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { PaymentsComponent } from './pages/payments/payments.component';
import { AdminUsersComponent } from './pages/admin-users/admin-users.component';
import { AgentClientsComponent } from './pages/agent-clients/agent-clients.component';
import { CryptoComponent } from './pages/crypto/crypto.component';
import { AnalyticsComponent } from './pages/analytics/analytics.component';
import { TransactionsComponent } from './pages/transactions/transactions.component';
import { TransactionReviewComponent } from './pages/transactions/transaction-review.component';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { AccountDetailsComponent } from './pages/account-details/account-details.component';
import { PreferencesComponent } from './pages/preferences/preferences.component';
import { SecuritySettingsComponent } from './pages/security-settings/security-settings.component';
import { AgentDashboardComponent } from './pages/agent-dashboard/agent-dashboard.component';

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
        path: 'register',
        component: RegisterComponent,
        title: 'Register - E-Banking 3.0'
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
        canActivate: [dashboardRedirectGuard],
        title: 'Dashboard - E-Banking 3.0'
      },
      {
        path: 'agent/dashboard',
        component: AgentDashboardComponent,
        canActivate: [agentGuard],
        title: 'Agent Dashboard - E-Banking 3.0'
      },
      {
        path: 'accounts',
        component: AccountsComponent,
        title: 'Accounts - E-Banking 3.0'
      },
        {
          path: 'accounts/:id',
          component: AccountDetailsComponent,
          title: 'Account Details - E-Banking 3.0'
        },
        {
          path: 'profile',
          component: ProfileComponent,
          title: 'Profile - E-Banking 3.0'
        },
        {
          path: 'profile/preferences',
          component: PreferencesComponent,
          title: 'Preferences - E-Banking 3.0'
        },
        {
          path: 'profile/security',
          component: SecuritySettingsComponent,
          title: 'Security Settings - E-Banking 3.0'
        },
        {
          path: 'payments',
          component: PaymentsComponent,
          title: 'Payments - E-Banking 3.0'
        },
        {
          path: 'transactions',
          component: TransactionsComponent,
          title: 'Transactions - E-Banking 3.0'
        },
        {
          path: 'transactions/:id',
          component: TransactionReviewComponent,
          title: 'Transaction Review - E-Banking 3.0'
        },
        {
          path: 'crypto',
          component: CryptoComponent,
          title: 'Crypto Wallet - E-Banking 3.0'
        },
        {
          path: 'analytics',
          component: AnalyticsComponent,
          title: 'Analytics - E-Banking 3.0'
        },
        {
          path: 'admin/dashboard',
          component: AdminDashboardComponent,
          title: 'Admin Dashboard - E-Banking 3.0'
        },
        {
          path: 'admin/users',
          component: AdminUsersComponent,
          canActivate: [adminGuard],
          title: 'User Management - E-Banking 3.0'
        },
        {
          path: 'agent/clients',
          component: AgentClientsComponent,
          canActivate: [agentGuard],
          title: 'My Clients - E-Banking 3.0'
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

