import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { User, UserRole, LoginRequest, RegisterRequest, TokenResponse, TokenInfo, RegisterResponse } from '../../models';
import { BehaviorSubject, Observable, of, delay, throwError, tap, map, catchError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { GraphQLService } from './graphql.service';

// (Auth DTOs are exported from src/app/models/auth.model.ts)

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

  /**
   * Get current access token
   * Used by GraphQL service to add Authorization header
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  // Reactive state
  private currentUserSubject = new BehaviorSubject<User | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  public isAuthenticated = signal<boolean>(!!this.getStoredUser());

  // API base (generic) and explicit auth base (can be overridden via environment.authServiceUrl)
  private apiBase = (localStorage.getItem('API_BASE') || environment.apiBaseUrl).replace(/\/+$/, '');
  private authBase = (localStorage.getItem('AUTH_BASE') || environment.authServiceUrl || `${this.apiBase}/api/auth`).replace(/\/+$/, '');
  // Helper to build auth endpoint URLs consistently
  private authUrl(path: string): string {
    const p = path.replace(/^\/+/, '');
    return `${this.authBase}/${p}`;
  }
  // No mock mode: always call backend auth service
  private readonly useMock: boolean = false;
  private readonly useGraphQL: boolean = environment.useGraphQL ?? false;


  // Pending MFA user id when login requires 2FA (used by MfaComponent)
  private pendingMfaUserId: string | null = null;

  constructor(
    private router: Router,
    private http: HttpClient,
    private graphqlService?: GraphQLService
  ) {
    // Check token validity on init
    this.validateStoredToken();
  }

  // ========================================
  // 1. LOGIN (POST /auth/login)
  // ========================================
  login(username: string, password: string): Observable<{ success: boolean; requiresMFA?: boolean; userId?: string }> {
    const payload = { username, password };
    return this.http.post<any>(this.authUrl('login'), payload).pipe(
      map(res => {
        if (res && res.requiresMFA) {
          if (res.userId) this.pendingMfaUserId = res.userId;
          return { success: true, requiresMFA: true, userId: res.userId };
        }
        if (res && res.access_token) {
          this.storeTokens(res as TokenResponse);
          return { success: true, requiresMFA: false };
        }
        return { success: false };
      }),
      catchError(() => of({ success: false }))
    );
  }

  /**
   * Login with backend DTOs
   */
  loginWithDTO(request: LoginRequest): Observable<TokenResponse> {
    // Always use backend REST/GraphQL for DTO-based login
    if (this.useGraphQL && this.graphqlService) {
      const loginMutation = `
        mutation Login($input: LoginInput!) {
          login(input: $input) {
            access_token
            refresh_token
            expires_in
            refresh_expires_in
            token_type
            scope
          }
        }
      `;

      return this.graphqlService.mutate<{ login: TokenResponse }>(loginMutation, {
        input: {
          username: request.username,
          password: request.password
        }
      }).pipe(
        map(result => result.login),
        tap(response => {
          this.storeTokens(response);
        }),
        catchError(() => throwError(() => new Error('Login failed')))
      );
    }

    return this.http.post<TokenResponse>(this.authUrl('login'), request).pipe(
      tap(response => this.storeTokens(response)),
      catchError(() => throwError(() => new Error('Login failed')))
    );
  }

  // ========================================
  // 2. REGISTER (POST /auth/register)
  // ========================================
  register(request: RegisterRequest): Observable<RegisterResponse> {
    // Always call backend register
    return this.http.post<RegisterResponse>(this.authUrl('register'), request).pipe(
      catchError(() => throwError(() => new Error('Registration failed')))
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

    // Always call backend refresh
    if (this.useGraphQL && this.graphqlService) {
      const refreshMutation = `
        mutation RefreshToken($input: RefreshTokenInput!) {
          refreshToken(input: $input) {
            access_token
            refresh_token
            expires_in
            refresh_expires_in
            token_type
            scope
          }
        }
      `;

      return this.graphqlService.mutate<{ refreshToken: TokenResponse }>(refreshMutation, {
        input: {
          refresh_token: token
        }
      }).pipe(
        map(result => result.refreshToken),
        tap(response => this.storeTokens(response)),
        catchError(() => {
          // Fallback to REST
          return this.http.post<TokenResponse>(this.authUrl('refresh'), { refresh_token: token }).pipe(
            tap(response => this.storeTokens(response)),
            catchError(() => throwError(() => new Error('Refresh failed')))
          );
        })
      );
    }

    return this.http.post<TokenResponse>(this.authUrl('refresh'), { refresh_token: token }).pipe(
      tap(response => this.storeTokens(response)),
      catchError(() => throwError(() => new Error('Refresh failed')))
    );
  }

  // ========================================
  // 4. LOGOUT (POST /auth/logout)
  // ========================================
  logout(): Observable<{ message: string }> {
    const refreshToken = localStorage.getItem(this.REFRESH_TOKEN_KEY);

    // Use GraphQL Gateway if enabled
    if (this.useGraphQL && this.graphqlService && refreshToken) {
      const logoutMutation = `
        mutation Logout($input: RefreshTokenInput!) {
          logout(input: $input)
        }
      `;

      return this.graphqlService.mutate<{ logout: boolean }>(logoutMutation, {
        input: {
          refresh_token: refreshToken
        }
      }).pipe(
        map(() => ({ message: 'Logged out successfully' })),
        tap(() => this.logoutSync()),
        catchError(() => {
          // Fallback to REST
          return this.http.post<{ message: string }>(this.authUrl('logout'), {}).pipe(
            tap(() => this.logoutSync()),
            catchError(() => {
              this.logoutSync();
              return of({ message: 'Logged out (fallback)' });
            })
          );
        })
      );
    }

    return this.http.post<{ message: string }>(this.authUrl('logout'), {}).pipe(
      tap(() => this.logoutSync()),
      catchError(() => {
        this.logoutSync();
        return of({ message: 'Logged out (fallback)' });
      })
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
    return this.http.post<{ valid: boolean }>(this.authUrl('verify-token'), { token }).pipe(
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
    return this.http.post<TokenInfo>(this.authUrl('token-info'), { token: accessToken }).pipe(
      catchError(() => of(this.decodeToken(accessToken)))
    );
  }

  // ========================================
  // MFA VERIFICATION
  // ========================================
  verifyMFA(code: string, userId?: string): Observable<{ success: boolean; user?: User }> {
    return this.http.post<{ success: boolean; access_token?: string }>(this.authUrl('mfa/verify'), { code, userId }).pipe(
      tap(res => { if (res && res.access_token) this.storeTokens({ access_token: res.access_token, refresh_token: '', expires_in: 3600, refresh_expires_in: 86400, token_type: 'Bearer', scope: '' }); }),
      map(res => ({ success: !!res.success })),
      catchError(() => of({ success: false }))
    );
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
  // Add these methods to your AuthService class

  getPendingMfaUserId(): string | null {
    return localStorage.getItem('pendingMfaUserId');
  }

  getMfaCodeForDev(userId: string): string | null {
    // This should only be used in development
    // In production, you would never expose MFA codes
    if (environment.production) {
      return null;
    }
    // Retrieve the dev MFA code from localStorage or your dev storage
    return localStorage.getItem(`mfa_code_dev_${userId}`);
  }

// Also add a method to set the pending user ID when initiating MFA
  setPendingMfaUserId(userId: string): void {
    localStorage.setItem('pendingMfaUserId', userId);
  }

// And a method to clear it after successful verification
  clearPendingMfaUserId(): void {
    localStorage.removeItem('pendingMfaUserId');
  }
}
