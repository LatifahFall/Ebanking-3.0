import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { User, UserRole } from '../../models';
import { BehaviorSubject, Observable, of, delay } from 'rxjs';

/**
 * Authentication Service
 * Handles user authentication, session management (MOCK - no real backend)
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'ebanking-token';
  private readonly USER_KEY = 'ebanking-user';

  // Reactive state
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();
  
  public isAuthenticated = signal<boolean>(!!this.getStoredUser());

  constructor(private router: Router) {}

  /**
   * Mock login - no real authentication
   */
  login(email: string, password: string): Observable<{ success: boolean; requiresMFA?: boolean }> {
    // Mock authentication delay
    return of({ success: true, requiresMFA: true }).pipe(delay(800));
  }

  /**
   * Mock MFA verification
   */
  verifyMFA(code: string): Observable<{ success: boolean; user?: User }> {
    // Mock user data
    const mockUser: User = {
      id: 'usr-123456',
      email: 'john.doe@example.com',
      firstName: 'John',
      lastName: 'Doe',
      fullName: 'John Doe',
      avatar: 'https://i.pravatar.cc/150?img=12',
      phoneNumber: '+1 234 567 8900',
      role: UserRole.CLIENT,
      status: 'ACTIVE' as any,
      createdAt: new Date('2023-01-15'),
      lastLogin: new Date(),
      kycStatus: 'VERIFIED' as any,
      preferences: {
        theme: 'dark',
        language: 'en',
        currency: 'USD',
        notifications: {
          email: true,
          sms: true,
          push: true,
          transactionAlerts: true,
          securityAlerts: true
        }
      }
    };

    // Store mock token and user
    localStorage.setItem(this.TOKEN_KEY, 'mock-jwt-token-' + Date.now());
    localStorage.setItem(this.USER_KEY, JSON.stringify(mockUser));
    
    this.currentUserSubject.next(mockUser);
    this.isAuthenticated.set(true);

    return of({ success: true, user: mockUser }).pipe(delay(500));
  }

  /**
   * Logout user
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/auth/login']);
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get stored user from localStorage
   */
  private getStoredUser(): User | null {
    const stored = localStorage.getItem(this.USER_KEY);
    if (stored) {
      try {
        return JSON.parse(stored);
      } catch {
        return null;
      }
    }
    return null;
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: UserRole): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }

  /**
   * Check if user is admin
   */
  isAdmin(): boolean {
    return this.hasRole(UserRole.ADMIN) || this.hasRole(UserRole.SUPER_ADMIN);
  }
}
