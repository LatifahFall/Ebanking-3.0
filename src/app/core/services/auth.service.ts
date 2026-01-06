import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { User, UserRole, LoginRequest, RegisterRequest, TokenResponse, TokenInfo, RegisterResponse } from '../../models';
import { BehaviorSubject, Observable, of, delay, throwError, tap, map, catchError } from 'rxjs';
import { AuditService } from './audit.service';

/**
 * Authentication Service
 * MOCK implementation matching backend auth-service structure
 * Endpoints: POST /auth/login, /auth/register, /auth/refresh, /auth/logout, /auth/verify-token, /auth/token-info
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'ebanking-user';

  // Reactive state
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  public isAuthenticated = signal<boolean>(!!this.getStoredUser());

  private base = (localStorage.getItem('API_BASE') || '/api/v1').replace(/\/+$/, '');

  // ========================================
  // MOCK USERS (Backend-like structure)
  // ========================================
  private readonly MOCK_USERS = [
    {
      id: '1',
      username: 'admin',
      password: 'admin123',
      email: 'admin@ebanking.com',
      role: UserRole.ADMIN,
      firstName: 'Admin',
      lastName: 'System',
      phoneNumber: '+212 600 000 000',
      cin: 'AA123456',
      address: 'Rabat, Morocco',
      mfaEnabled: true
    },
    {
      id: '2',
      username: 'agent',
      password: 'agent123',
      email: 'agent@ebanking.com',
      role: UserRole.AGENT,
      firstName: 'Mohamed',
      lastName: 'Alaoui',
      phoneNumber: '+212 611 111 111',
      cin: 'BB234567',
      address: 'Casablanca, Morocco',
      mfaEnabled: true
    },
    {
      id: '3',
      username: 'client',
      password: 'client123',
      email: 'client@ebanking.com',
      role: UserRole.CLIENT,
      firstName: 'Fatima',
      lastName: 'Zahra',
      phoneNumber: '+212 622 222 222',
      cin: 'CC345678',
      address: 'Marrakech, Morocco',
      mfaEnabled: false
    }
  ];

  // Pending MFA user id when login requires 2FA (used by MfaComponent)
  private pendingMfaUserId: string | null = null;

  constructor(private router: Router, private http: HttpClient, private auditService: AuditService) {
    // Check token validity on init
    this.validateStoredToken();
  }

  // ========================================
  // 1. LOGIN (POST /auth/login)
  // ========================================
  login(username: string, password: string): Observable<{ success: boolean; requiresMFA?: boolean; userId?: string }> {
    // Backend-first attempt
    const payload = { username, password };
    return this.http.post<any>(`/api/auth/login`, payload).pipe(
      map(res => {
        // If backend responds with MFA required flag
        if (res && res.requiresMFA) {
          // store pending MFA user id so MfaComponent can retrieve it
          if (res.userId) {
            this.pendingMfaUserId = res.userId;
            // generate a dev MFA code if backend is not providing one (safe no-op)
            try { this.generateAndStoreMfaCode(res.userId); } catch {}
          }
          return { success: true, requiresMFA: true, userId: res.userId };
        }
        // If backend returns tokens, store them
        if (res && res.access_token) {
          this.storeTokens(res as TokenResponse);
          const info = this.decodeToken((res as TokenResponse).access_token);
          const user = info ? this.MOCK_USERS.find(u => u.id === info.sub) : null;
          if (user) this.setCurrentUser(user);
          return { success: true, requiresMFA: false };
        }
        return { success: false };
      }),
      catchError(() => {
        // Fallback to mock
        return of(null).pipe(
          delay(600),
          map(() => {
            const user = this.MOCK_USERS.find(
              u => (u.username === username || u.email === username) && u.password === password
            );

            if (!user) {
              throw new Error('Invalid credentials');
            }

            // Return MFA requirement for users with MFA enabled
            if (user.mfaEnabled) {
              // store pending MFA user id and generate a mock code
              this.pendingMfaUserId = user.id;
              this.generateAndStoreMfaCode(user.id);
              return { success: true, requiresMFA: true, userId: user.id };
            }

            // Direct login without MFA
            const tokenResponse = this.generateTokenResponse(user);
            this.storeTokens(tokenResponse);
            this.setCurrentUser(user);

            // Emit audit event (fire-and-forget)
            try {
              this.auditService.createEvent({
                eventType: 'USER_LOGIN', // <-- utiliser eventType à la place d'action
                userId: Number(user.id),
                username: user.username,
                timestamp: new Date().toISOString(),
                details: { method: 'mock' }
              }).subscribe(() => {}, () => {});
            } catch (e) {}

            return { success: true, requiresMFA: false };
          })
        );
      })
    );
  }

  /**
   * Login with backend DTOs
   */
  loginWithDTO(request: LoginRequest): Observable<TokenResponse> {
    // Try real backend first, fallback to mock
    return this.http.post<TokenResponse>(`/api/auth/login`, request).pipe(
      tap(response => {
        this.storeTokens(response);
        // try to resolve user from token or fallback to mock users
        const info = this.decodeToken(response.access_token);
        const user = info ? this.MOCK_USERS.find(u => u.id === info.sub) : null;
        if (user) {
          this.setCurrentUser(user);
          try {
            this.auditService.createEvent({ eventType: 'USER_LOGIN', userId: Number(user.id), username: user.username, timestamp: new Date().toISOString(), details: { method: 'backend' } }).subscribe(() => {}, () => {});
          } catch (e) {}
        }
      }),
      catchError(() => {
        // fallback to mock implementation
        return of(null).pipe(
          delay(800),
          map(() => {
            const user = this.MOCK_USERS.find(
              u => u.username === request.username && u.password === request.password
            );

            if (!user) {
              throw new Error('Invalid credentials');
            }

            const response = this.generateTokenResponse(user);
            this.storeTokens(response);
            this.setCurrentUser(user);

            try {
              this.auditService.createEvent({ eventType: 'USER_LOGIN', userId: Number(user.id), username: user.username, timestamp: new Date().toISOString(), details: { method: 'mock' } }).subscribe(() => {}, () => {});
            } catch (e) {}

            return response;
          })
        );
      })
    );
  }

  // ========================================
  // 2. REGISTER (POST /auth/register)
  // ========================================
  register(request: RegisterRequest): Observable<RegisterResponse> {
    // Try backend register endpoint first, fallback to mock implementation on error
    return this.http.post<RegisterResponse>(`/api/auth/register`, request).pipe(
      catchError(() => {
        return of(null).pipe(
          delay(1200),
          map(() => {
            // Validate username not taken
            const existingUser = this.MOCK_USERS.find(u => u.username === request.username);
            if (existingUser) {
              throw new Error('Username already taken');
            }

            // Validate email not taken
            const existingEmail = this.MOCK_USERS.find(u => u.email === request.email);
            if (existingEmail) {
              throw new Error('Email already registered');
            }

            const newUserId = `usr-${Date.now()}`;

            const resp = {
              success: true,
              message: 'Registration successful. Please check your email to verify your account.',
              userId: newUserId
            } as RegisterResponse;

            // Emit audit event for registration
            try {
              this.auditService.createEvent({
                eventType: 'USER_REGISTER', // <-- eventType
                userId: null,
                username: request.username,
                timestamp: new Date().toISOString(),
                details: { email: request.email }
              }).subscribe(() => {}, () => {});
            } catch (e) {}

            return resp;
          })
        );
      })
    );
  }

  // ========================================
  // 3. REFRESH TOKEN (POST /auth/refresh)
  // ========================================
  refreshToken(refreshToken?: string): Observable<TokenResponse> {
    const token = refreshToken || localStorage.getItem(this.REFRESH_TOKEN_KEY);
    if (!token) {
      return throwError(() => new Error('No refresh token available'));
    }

    // Try backend refresh endpoint
    return this.http.post<TokenResponse>(`/api/auth/refresh`, { refresh_token: token }).pipe(
      tap(response => this.storeTokens(response)),
      catchError(() => {
        // Fallback to mock
        return of(null).pipe(
          delay(300),
          map(() => {
            const userId = this.extractUserIdFromRefreshToken(token);
            const user = this.MOCK_USERS.find(u => u.id === userId);

            if (!user) {
              throw new Error('Invalid refresh token');
            }

            const response = this.generateTokenResponse(user);
            this.storeTokens(response);

            return response;
          })
        );
      })
    );
  }

  // ========================================
  // 4. LOGOUT (POST /auth/logout)
  // ========================================
  logout(): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`/api/auth/logout`, {}).pipe(
      tap(() => this.logoutSync()),
      catchError(() => of({ message: 'Logged out (mock)' }))
    );
  }

  /**
   * Synchronous logout (for guards)
   */
  logoutSync(): void {
    this.clearTokens();
    this.currentUserSubject.next(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/auth/login']);
  }

  // ========================================
  // 5. VERIFY TOKEN (POST /auth/verify-token)
  // ========================================
  verifyToken(token: string): Observable<{ valid: boolean }> {
    return this.http.post<{ valid: boolean }>(`/api/auth/verify-token`, { token }).pipe(
      catchError(() => of({ valid: !!this.decodeToken(token) && !this.isTokenExpired(this.decodeToken(token) as any) }))
    );
  }

  // ========================================
  // 6. TOKEN INFO (POST /auth/token-info)
  // ========================================
  getTokenInfo(token?: string): Observable<TokenInfo | null> {
    const accessToken = token || localStorage.getItem(this.TOKEN_KEY);
    if (!accessToken) {
      return of(null);
    }
    return this.http.post<TokenInfo>(`/api/auth/token-info`, { token: accessToken }).pipe(
      catchError(() => of(this.decodeToken(accessToken)))
    );
  }

  // ========================================
  // MFA VERIFICATION
  // ========================================
  verifyMFA(code: string, userId?: string): Observable<{ success: boolean; user?: User }> {
    // Try backend verify endpoint first
    return this.http.post<{ success: boolean; access_token?: string }>(`/api/auth/mfa/verify`, { code, userId }).pipe(
      tap(res => {
        if (res && res.access_token) {
          const token: TokenResponse = { access_token: res.access_token, refresh_token: '', expires_in: 3600, refresh_expires_in: 86400, token_type: 'Bearer', scope: '' };
          this.storeTokens(token);
        }
      }),
      map(res => ({ success: !!res.success })),
      catchError(() => {
        // Fallback mock
        return of(null).pipe(
          delay(800),
          map(() => {
            const effectiveUserId = userId || this.pendingMfaUserId;
            if (!effectiveUserId) throw new Error('No MFA pending user');

            const storedRaw = localStorage.getItem(`mock_mfa_${effectiveUserId}`);
            if (!storedRaw) throw new Error('No MFA code available');
            let stored: { code: string; expiresAt: number } | null = null;
            try { stored = JSON.parse(storedRaw); } catch {}
            if (!stored) throw new Error('No MFA code available');

            // Check expiry
            if (stored.expiresAt < Date.now()) {
              throw new Error('MFA code expired');
            }

            if (stored.code !== code) {
              throw new Error('Invalid MFA code');
            }

            const user = this.MOCK_USERS.find(u => u.id === effectiveUserId);
            if (!user) throw new Error('Invalid MFA user');

            const tokenResponse = this.generateTokenResponse(user);
            this.storeTokens(tokenResponse);
            const userObj = this.setCurrentUser(user);

            // Clear pending MFA and stored code
            this.pendingMfaUserId = null;
            try { localStorage.removeItem(`mock_mfa_${effectiveUserId}`); } catch {}

            return { success: true, user: userObj };
          })
        );
      })
    );
  }

  // Generate a 6-digit MFA code for a user and store it (dev/mocked)
  private generateAndStoreMfaCode(userId: string): string {
    const code = Math.floor(100000 + Math.random() * 900000).toString();
    const expiresAt = Date.now() + 5 * 60 * 1000; // 5 minutes
    try {
      localStorage.setItem(`mock_mfa_${userId}`, JSON.stringify({ code, expiresAt }));
    } catch {}
    return code;
  }

  // Expose pending MFA user id for MfaComponent
  getPendingMfaUserId(): string | null {
    return this.pendingMfaUserId;
  }

  // Return the dev MFA code for a user (if available) — useful for dev/testing only
  getMfaCodeForDev(userId?: string): string | null {
    const uid = userId || this.pendingMfaUserId;
    if (!uid) return null;
    try {
      const raw = localStorage.getItem(`mock_mfa_${uid}`);
      if (!raw) return null;
      const parsed = JSON.parse(raw) as { code: string; expiresAt: number };
      if (parsed.expiresAt < Date.now()) return null;
      return parsed.code;
    } catch {
      return null;
    }
  }

  // ========================================
  // HELPER METHODS
  // ========================================

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  hasRole(role: UserRole): boolean {
    const user = this.getCurrentUser();
    return user?.role === role;
  }

  isAdmin(): boolean {
    return this.hasRole(UserRole.ADMIN) || this.hasRole(UserRole.SUPER_ADMIN);
  }

  isAgent(): boolean {
    return this.hasRole(UserRole.AGENT);
  }

  isClient(): boolean {
    return this.hasRole(UserRole.CLIENT);
  }

  private validateStoredToken(): void {
    const token = localStorage.getItem(this.TOKEN_KEY);
    if (token) {
      const tokenInfo = this.decodeToken(token);
      if (!tokenInfo || this.isTokenExpired(tokenInfo)) {
        this.clearTokens();
        this.currentUserSubject.next(null);
        this.isAuthenticated.set(false);
      }
    }
  }

  private generateTokenResponse(user: any): TokenResponse {
    const now = Math.floor(Date.now() / 1000);
    return {
      access_token: this.generateMockToken(user, now),
      refresh_token: this.generateMockRefreshToken(user.id),
      expires_in: 3600,         // 1 hour
      refresh_expires_in: 86400, // 24 hours
      token_type: 'Bearer',
      scope: user.role.toLowerCase()
    };
  }

  private generateMockToken(user: any, iat: number): string {
    const tokenInfo: TokenInfo = {
      sub: user.id,
      email: user.email,
      preferred_username: user.username,
      given_name: user.firstName,
      family_name: user.lastName,
      roles: [user.role],
      exp: iat + 3600,
      iat: iat
    };
    const payload = btoa(JSON.stringify(tokenInfo));
    return `mock.${payload}.signature`;
  }

  private generateMockRefreshToken(userId: string): string {
    return `refresh-${userId}-${Date.now()}`;
  }

  private decodeToken(token: string): TokenInfo | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = JSON.parse(atob(parts[1]));
      return payload as TokenInfo;
    } catch {
      return null;
    }
  }

  private extractUserIdFromRefreshToken(token: string): string | null {
    const match = token.match(/refresh-(.+?)-\d+/);
    return match ? match[1] : null;
  }

  private isTokenExpired(tokenInfo: TokenInfo): boolean {
    const now = Math.floor(Date.now() / 1000);
    return tokenInfo.exp <= now;
  }

  private setCurrentUser(user: any): User {
    const userObj: User = {
      id: user.id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      fullName: `${user.firstName} ${user.lastName}`,
      phoneNumber: user.phoneNumber,
      role: user.role,
      status: 'ACTIVE' as any,
      createdAt: new Date(),
      lastLogin: new Date(),
      kycStatus: 'VERIFIED' as any,
      preferences: {
        theme: 'dark',
        language: 'fr',
        currency: 'MAD',
        notifications: {
          email: true,
          sms: true,
          push: true,
          transactionAlerts: true,
          securityAlerts: true
        }
      }
    };

    localStorage.setItem(this.USER_KEY, JSON.stringify(userObj));
    this.currentUserSubject.next(userObj);
    this.isAuthenticated.set(true);

    return userObj;
  }

  // Public helper to update stored current user (used by profile page updates)
  public updateStoredUser(user: User): void {
    try {
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.currentUserSubject.next(user);
      this.isAuthenticated.set(true);
    } catch {}
  }

  private storeTokens(response: TokenResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.access_token);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refresh_token);
    localStorage.setItem('token_expires_at', (Math.floor(Date.now() / 1000) + response.expires_in).toString());
  }

  private clearTokens(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem('token_expires_at');
    localStorage.removeItem(this.USER_KEY);
  }

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
}
