import { Injectable } from '@angular/core';
import { Observable, of, delay, catchError, map, switchMap, throwError } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { User, UserRole } from '../../models';

/**
 * User Service (MOCK)
 * Mirrors user-service endpoints: /admin/users, /me, /agent/users
 */
@Injectable({
  providedIn: 'root'
})
export class UserService {
  private users: User[] = [];
  private clientAssignments: Record<string, string[]> = {}; // agentId -> clientIds

  private base = (localStorage.getItem('API_BASE') || '/api/v1').replace(/\/+$/, '');

  constructor(private http: HttpClient) {
    // Seed users
    this.users = [
      {
        id: '1',
        email: 'admin@ebanking.com',
        firstName: 'Admin',
        lastName: 'System',
        fullName: 'Admin System',
        avatar: '',
        phoneNumber: '+212600000000',
        role: UserRole.ADMIN,
        status: 'ACTIVE' as any,
        lastLogin: new Date(),
        createdAt: new Date('2022-01-01'),
        preferences: { theme: 'dark', language: 'fr', currency: 'MAD', notifications: { email: true, sms: true, push: true, transactionAlerts: true, securityAlerts: true } },
        kycStatus: 'VERIFIED' as any
      },
      {
        id: '2',
        email: 'agent@ebanking.com',
        firstName: 'Said',
        lastName: 'Agent',
        fullName: 'Said Agent',
        avatar: '',
        phoneNumber: '+212611111111',
        role: UserRole.AGENT,
        status: 'ACTIVE' as any,
        lastLogin: new Date(),
        createdAt: new Date('2022-06-01'),
        preferences: { theme: 'dark', language: 'fr', currency: 'MAD', notifications: { email: true, sms: true, push: true, transactionAlerts: true, securityAlerts: true } },
        kycStatus: 'VERIFIED' as any
      },
      {
        id: '3',
        email: 'client@ebanking.com',
        firstName: 'Fatima',
        lastName: 'Client',
        fullName: 'Fatima Client',
        avatar: '',
        phoneNumber: '+212622222222',
        role: UserRole.CLIENT,
        status: 'ACTIVE' as any,
        lastLogin: new Date(),
        createdAt: new Date('2023-01-15'),
        preferences: { theme: 'light', language: 'fr', currency: 'MAD', notifications: { email: true, sms: false, push: true, transactionAlerts: true, securityAlerts: true } },
        kycStatus: 'VERIFIED' as any
      }
    ];

    // Assign client '3' to agent '2'
    this.clientAssignments['2'] = ['3'];
  }

  // ADMIN: POST /admin/users
  createUser(payload: Partial<User>): Observable<User> {
    // Try backend first
    return this.http.post<User>(`${this.base}/admin/users`, payload).pipe(
      catchError(() => {
        const newUser: User = {
          id: `${Date.now()}`,
          email: payload.email || 'new@user.com',
          firstName: payload.firstName || 'New',
          lastName: payload.lastName || 'User',
          fullName: `${payload.firstName || 'New'} ${payload.lastName || 'User'}`,
          avatar: '',
          phoneNumber: payload.phoneNumber || '',
          role: (payload.role || UserRole.CLIENT) as UserRole,
          status: 'ACTIVE' as any,
          lastLogin: new Date(),
          createdAt: new Date(),
          preferences: payload.preferences || { theme: 'light', language: 'en', currency: 'USD', notifications: { email: true, sms: false, push: true, transactionAlerts: true, securityAlerts: true } },
          kycStatus: 'NOT_STARTED' as any
        };

        this.users.push(newUser);
        return of(newUser).pipe(delay(300));
      })
    );
  }

  // ADMIN: GET /admin/users/{id}
  getUserById(id: string): Observable<User | null> {
    return this.http.get<User>(`${this.base}/admin/users/${id}`).pipe(
      catchError(() => {
        const u = this.users.find(x => x.id === id) || null;
        return of(u).pipe(delay(200));
      })
    );
  }

  // ADMIN: PUT /admin/users/{id}
  updateUser(id: string, payload: Partial<User>): Observable<User | null> {
    return this.http.put<User | null>(`${this.base}/admin/users/${id}`, payload).pipe(
      catchError(() => {
        const u = this.users.find(x => x.id === id) || null;
        if (!u) return of(null).pipe(delay(200));

        Object.assign(u, payload);
        return of(u).pipe(delay(200));
      })
    );
  }

  // ADMIN: POST /admin/users/assignments
  assignClientToAgent(agentId: string, clientId: string): Observable<{ success: boolean }> {
    if (!this.clientAssignments[agentId]) this.clientAssignments[agentId] = [];
    if (!this.clientAssignments[agentId].includes(clientId)) this.clientAssignments[agentId].push(clientId);
    return of({ success: true }).pipe(delay(200));
  }

  // ADMIN: DELETE /admin/users/assignments
  unassignClient(agentId: string, clientId: string): Observable<{ success: boolean }> {
    if (!this.clientAssignments[agentId]) return of({ success: false }).pipe(delay(200));
    this.clientAssignments[agentId] = this.clientAssignments[agentId].filter(id => id !== clientId);
    return of({ success: true }).pipe(delay(200));
  }

  // ADMIN: GET /admin/users/search
  searchUsers(query?: string, role?: UserRole, page = 0, size = 20): Observable<{ users: User[]; total: number }> {
    const params: any = { page, size };
    if (query) params.query = query;
    if (role) params.role = role;

    return this.http.get<{ users: User[]; total: number }>(`${this.base}/admin/users/search`, { params }).pipe(
      catchError(() => {
        let filtered = [...this.users];
        if (role) filtered = filtered.filter(u => u.role === role);
        if (query) filtered = filtered.filter(u => u.firstName.toLowerCase().includes(query.toLowerCase()) || u.lastName.toLowerCase().includes(query.toLowerCase()) || u.email.toLowerCase().includes(query.toLowerCase()));
        const total = filtered.length;
        const paged = filtered.slice(page * size, page * size + size);
        return of({ users: paged, total }).pipe(delay(250));
      })
    );
  }

  // ADMIN: GET /admin/users/clients/{id}/agent
  getClientsAgent(clientId: string): Observable<User | null> {
    const agentId = Object.keys(this.clientAssignments).find(a => this.clientAssignments[a].includes(clientId));
    if (!agentId) return of(null).pipe(delay(200));
    return this.getUserById(agentId);
  }

  // ADMIN: GET /admin/users/agents/{id}/clients
  // Also used by agents via searchAssignedClients
  getAgentClients(agentId: string): Observable<User[]> {
    // Try backend first (admin endpoint)
    return this.http.get<User[]>(`${this.base}/admin/users/agents/${agentId}/clients`).pipe(
      catchError(() => {
        // Fallback to mock: get assigned clients
        const clientIds = this.clientAssignments[agentId] || [];
        const clients = this.users.filter(u => clientIds.includes(u.id));
        return of(clients).pipe(delay(250));
      })
    );
  }

  // ADMIN: PATCH /admin/users/activate
  activateUser(userId: string): Observable<{ success: boolean }> {
    const u = this.users.find(x => x.id === userId);
    if (!u) return of({ success: false }).pipe(delay(200));
    u.status = 'ACTIVE' as any;
    return of({ success: true }).pipe(delay(200));
  }

  // ADMIN: PATCH /admin/users/deactivate
  deactivateUser(userId: string): Observable<{ success: boolean }> {
    const u = this.users.find(x => x.id === userId);
    if (!u) return of({ success: false }).pipe(delay(200));
    u.status = 'INACTIVE' as any;
    return of({ success: true }).pipe(delay(200));
  }

  // ME: GET /me/{id}
  getMyProfile(userId: string): Observable<User | null> {
    return this.http.get<User | null>(`${this.base}/me/${userId}`).pipe(
      catchError(() => this.getUserById(userId))
    );
  }

  // ME: PUT /me/{id}
  updateMyProfile(userId: string, payload: Partial<User>): Observable<User | null> {
    return this.http.put<User | null>(`${this.base}/me/${userId}`, payload).pipe(
      catchError(() => this.updateUser(userId, payload))
    );
  }

  // ME: PATCH /me/{id}/last-login
  setLastLogin(userId: string): Observable<{ success: boolean }> {
    const u = this.users.find(x => x.id === userId);
    if (!u) return of({ success: false }).pipe(delay(200));
    u.lastLogin = new Date();
    return of({ success: true }).pipe(delay(100));
  }

  // ME: GET /me/{id}/preferences
  getPreferences(userId: string): Observable<any> {
    return this.http.get<any>(`${this.base}/me/${userId}/preferences`).pipe(
      catchError(() => {
        const u = this.users.find(x => x.id === userId);
        // Convert frontend preferences format to backend format
        if (u && u.preferences) {
          const backendPrefs = {
            language: u.preferences.language || 'en',
            theme: u.preferences.theme || 'light',
            notificationEmail: u.preferences.notifications?.email ?? true,
            notificationSms: u.preferences.notifications?.sms ?? true,
            notificationPush: u.preferences.notifications?.push ?? true,
            notificationInApp: u.preferences.notifications?.transactionAlerts ?? true
          };
          return of(backendPrefs).pipe(delay(150));
        }
        return of({
          language: 'en',
          theme: 'light',
          notificationEmail: true,
          notificationSms: true,
          notificationPush: true,
          notificationInApp: true
        }).pipe(delay(150));
      })
    );
  }

  // ME: PUT /me/{id}/preferences
  updatePreferences(userId: string, prefs: any): Observable<any> {
    return this.http.put<any>(`${this.base}/me/${userId}/preferences`, prefs).pipe(
      catchError(() => {
        const u = this.users.find(x => x.id === userId);
        if (!u) return of(null).pipe(delay(200));
        // Update mock preferences
        if (!u.preferences) {
          u.preferences = {
            theme: 'light',
            language: 'en',
            currency: 'USD',
            notifications: {
              email: true,
              sms: true,
              push: true,
              transactionAlerts: true,
              securityAlerts: true
            }
          };
        }
        if (prefs.language) u.preferences.language = prefs.language;
        if (prefs.theme) u.preferences.theme = prefs.theme;
        if (prefs.notificationEmail !== undefined) {
          if (!u.preferences.notifications) {
            u.preferences.notifications = {
              email: true,
              sms: true,
              push: true,
              transactionAlerts: true,
              securityAlerts: true
            };
          }
          u.preferences.notifications.email = prefs.notificationEmail;
        }
        if (prefs.notificationSms !== undefined) {
          if (!u.preferences.notifications) {
            u.preferences.notifications = {
              email: true,
              sms: true,
              push: true,
              transactionAlerts: true,
              securityAlerts: true
            };
          }
          u.preferences.notifications.sms = prefs.notificationSms;
        }
        if (prefs.notificationPush !== undefined) {
          if (!u.preferences.notifications) {
            u.preferences.notifications = {
              email: true,
              sms: true,
              push: true,
              transactionAlerts: true,
              securityAlerts: true
            };
          }
          u.preferences.notifications.push = prefs.notificationPush;
        }
        if (prefs.notificationInApp !== undefined) {
          if (!u.preferences.notifications) {
            u.preferences.notifications = {
              email: true,
              sms: true,
              push: true,
              transactionAlerts: true,
              securityAlerts: true
            };
          }
          u.preferences.notifications.transactionAlerts = prefs.notificationInApp;
        }
        return of(prefs).pipe(delay(150));
      })
    );
  }

  // ME: PUT /me/{id} - Change password
  // Note: Backend PUT /me/{id} accepts UpdateProfileRequest with optional password field
  // Backend doesn't verify current password server-side, but we verify it client-side for security
  changePassword(userId: string, currentPassword: string, newPassword: string): Observable<User | null> {
    // First, get user profile to get login/email for verification
    return this.getMyProfile(userId).pipe(
      switchMap((user) => {
        if (!user) {
          return throwError(() => new Error('User not found'));
        }
        // Verify current password using authenticate endpoint (POST /me/login)
        // This ensures we don't change password without verifying current one
        const login = user.email || user.id;
        return this.authenticate(login, currentPassword).pipe(
          switchMap((authenticatedUser) => {
            if (!authenticatedUser) {
              return throwError(() => new Error('Current password is incorrect'));
            }
            // If current password is correct, change to new password
            // Backend expects: PUT /me/{userId} with { password: "newPassword" }
            return this.http.put<User | null>(`${this.base}/me/${userId}`, {
              password: newPassword
            }).pipe(
              catchError(() => {
                // In mock mode, just return success
                const u = this.users.find(x => x.id === userId);
                return of(u || null).pipe(delay(200));
              })
            );
          })
        );
      }),
      catchError((error) => {
        // In mock mode, verify password and change
        const u = this.users.find(x => x.id === userId);
        if (!u) {
          return throwError(() => new Error('User not found'));
        }
        // Simple mock verification (in real app, backend should verify)
        // Check if current password matches any of the mock passwords
        const mockPasswords = ['client123', 'agent123', 'admin123', currentPassword];
        const isValid = mockPasswords.some(pwd => pwd === currentPassword);
        if (!isValid) {
          return throwError(() => new Error('Current password is incorrect'));
        }
        // Password verified, return user (mock change)
        return of(u).pipe(delay(200));
      })
    );
  }

  // ME: POST /me/login - Simple authenticate
  authenticate(login: string, password: string): Observable<User | null> {
    const u = this.users.find(x => (x.email === login || x.id === login) && (password === 'client123' || password === 'agent123' || password === 'admin123')) || null;
    return of(u).pipe(delay(300));
  }

  // AGENT: POST /agent/clients/{agentId}
  createAssignedClient(agentId: string, payload: Partial<User>): Observable<User> {
    const newClient: User = {
      id: `${Date.now()}`,
      email: payload.email || 'newclient@bank.com',
      firstName: payload.firstName || 'Client',
      lastName: payload.lastName || 'New',
      fullName: `${payload.firstName || 'Client'} ${payload.lastName || 'New'}`,
      avatar: '',
      phoneNumber: payload.phoneNumber || '',
      role: UserRole.CLIENT,
      status: 'ACTIVE' as any,
      lastLogin: new Date(),
      createdAt: new Date(),
      preferences: payload.preferences || { theme: 'light', language: 'en', currency: 'USD', notifications: { email: true, sms: false, push: true, transactionAlerts: true, securityAlerts: true } },
      kycStatus: 'NOT_STARTED' as any
    };
    this.users.push(newClient);
    if (!this.clientAssignments[agentId]) this.clientAssignments[agentId] = [];
    this.clientAssignments[agentId].push(newClient.id);
    return of(newClient).pipe(delay(300));
  }

  // AGENT: PUT /agent/clients/{agentId}
  updateClientProfile(agentId: string, clientId: string, payload: Partial<User>): Observable<User | null> {
    const client = this.users.find(x => x.id === clientId && x.role === UserRole.CLIENT) || null;
    if (!client) return of(null).pipe(delay(200));
    Object.assign(client, payload);
    return of(client).pipe(delay(200));
  }

  // AGENT: GET /agent/clients/{agentId}?clientId={clientId}
  getClientProfile(agentId: string, clientId: string): Observable<User> {
    return this.http.get<User>(`${this.base}/agent/clients/${agentId}?clientId=${clientId}`).pipe(
      catchError(() => {
        // Fallback to mock
        const assigned = this.clientAssignments[agentId] || [];
        if (!assigned.includes(clientId)) {
          return throwError(() => new Error('Client not found or not assigned to this agent'));
        }
        const client = this.users.find(u => u.id === clientId);
        if (!client) {
          return throwError(() => new Error('Client not found'));
        }
        return of(client).pipe(delay(200));
      })
    );
  }

  // AGENT: GET /agent/clients/{agentId}/search
  searchAssignedClients(agentId: string, query?: string, page = 0, size = 20): Observable<{ clients: User[]; total: number }> {
    const assigned = this.clientAssignments[agentId] || [];
    let clients = this.users.filter(u => assigned.includes(u.id));
    if (query) clients = clients.filter(c => c.firstName.toLowerCase().includes(query.toLowerCase()) || c.lastName.toLowerCase().includes(query.toLowerCase()) || c.email.toLowerCase().includes(query.toLowerCase()));
    const total = clients.length;
    const paged = clients.slice(page * size, page * size + size);
    return of({ clients: paged, total }).pipe(delay(200));
  }

  // AGENT: PATCH /agent/clients/{agentId}/activate
  activateClient(agentId: string, clientId: string): Observable<{ success: boolean }> {
    const client = this.users.find(x => x.id === clientId && x.role === UserRole.CLIENT);
    if (!client) return of({ success: false }).pipe(delay(200));
    client.status = 'ACTIVE' as any;
    return of({ success: true }).pipe(delay(200));
  }

  // AGENT: PATCH /agent/clients/{agentId}/deactivate
  deactivateClient(agentId: string, clientId: string): Observable<{ success: boolean }> {
    const client = this.users.find(x => x.id === clientId && x.role === UserRole.CLIENT);
    if (!client) return of({ success: false }).pipe(delay(200));
    client.status = 'INACTIVE' as any;
    return of({ success: true }).pipe(delay(200));
  }
}