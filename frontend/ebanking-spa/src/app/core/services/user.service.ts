import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { User } from '../../models/user.model';
import { environment } from '../../../environments/environment';
import { map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly base = environment.userServiceUrl + '/admin/users';

  constructor(private http: HttpClient) {}

  createUser(user: Partial<User>): Observable<User> {
    return this.http.post<User>(`${this.base}`, user);
  }

  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${this.base}/${id}`);
  }

  updateUser(id: string, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.base}/${id}`, user);
  }

  searchUsers(query: string, role?: string, page: number = 0, size: number = 10): Observable<User[]> {
    let params = `q=${encodeURIComponent(query)}&page=${page}&size=${size}`;
    if (role) params += `&role=${role}`;
    return this.http.get<User[]>(`${this.base}/search?${params}`);
  }

  activateUser(userId: string): Observable<any> {
    return this.http.patch(`${this.base}/activate?userId=${userId}`, {});
  }

  deactivateUser(userId: string): Observable<any> {
    return this.http.patch(`${this.base}/deactivate?userId=${userId}`, {});
  }

  // AGENT - Client Management
  createClient(agentId: string, client: Partial<User>): Observable<User> {
    return this.http.post<User>(`http://34.22.142.65/agent/clients/${agentId}`, client);
  }

  updateClient(agentId: string, clientId: string, client: Partial<User>): Observable<User> {
    return this.http.put<User>(`http://34.22.142.65/agent/clients/${agentId}?clientId=${clientId}`, client);
  }

  fetchClient(agentId: string, clientId: string): Observable<User> {
    return this.http.get<User>(`http://34.22.142.65/agent/clients/${agentId}?clientId=${clientId}`);
  }

  searchAssignedClients(agentId: string, query: string, page: number = 0, size: number = 10): Observable<User[]> {
    return this.http.get<User[]>(`http://34.22.142.65/agent/clients/${agentId}/search?q=${encodeURIComponent(query)}&page=${page}&size=${size}`);
  }

  activateClient(agentId: string, clientId: string): Observable<any> {
    return this.http.patch(`http://34.22.142.65/agent/clients/${agentId}/activate?clientId=${clientId}`, {});
  }

  deactivateClient(agentId: string, clientId: string): Observable<any> {
    return this.http.patch(`http://34.22.142.65/agent/clients/${agentId}/deactivate?clientId=${clientId}`, {});
  }

  // USER - Profil personnel et préférences
  getMyProfile(id: string): Observable<User> {
    return this.http.get<User>(`http://34.22.142.65/me/${id}`);
  }

  updateMyProfile(id: string, user: Partial<User>): Observable<User> {
    return this.http.put<User>(`http://34.22.142.65/me/${id}`, user);
  }

  updateLastLogin(id: string): Observable<any> {
    return this.http.patch(`http://34.22.142.65/me/${id}/last-login`, {});
  }

  getMyPreferences(id: string): Observable<any> {
    return this.http.get(`http://34.22.142.65/me/${id}/preferences`);
  }

  updateMyPreferences(id: string, prefs: any): Observable<any> {
    return this.http.put(`http://34.22.142.65/me/${id}/preferences`, prefs);
  }

  login(login: string, password: string): Observable<any> {
    // Utilise le nouvel endpoint d'authentification
    return this.http.post(`${environment.authServiceUrl}/login`, { email: login, password });
  }

  /**
   * Change le mot de passe de l'utilisateur courant via le backend (Keycloak)
   */
  changePassword(userId: string, currentPassword: string, newPassword: string): Observable<boolean> {
    return this.http.post<{ success: boolean }>(`${environment.authServiceUrl}/change-password`, {
      userId,
      currentPassword,
      newPassword
    }).pipe(
      // Renvoie true si succès, false sinon
      // (adapter selon la réponse réelle de votre backend)
      // Si votre backend renvoie juste 200/204, adaptez le map
      map(res => !!res && res.success),
      catchError(() => of(false))
    );
  }

  // ADMIN - Assignments
  assignClientToAgent(agentId: string, clientId: string): Observable<any> {
    return this.http.post(`http://34.22.142.65/admin/users/assignments`, { agentId, clientId });
  }

  unassignClient(agentId: string, clientId: string): Observable<any> {
    return this.http.delete(`http://34.22.142.65/admin/users/assignments?clientId=${clientId}&agentId=${agentId}`);
  }

  getAgentsClients(agentId: string): Observable<User[]> {
    return this.http.get<User[]>(`http://34.22.142.65/admin/users/agents/${agentId}/clients`);
  }

  getClientsAgent(clientId: string): Observable<User> {
    return this.http.get<User>(`http://34.22.142.65/admin/users/clients/${clientId}/agent`);
  }

  // AGENT - Création et modification de client assigné
  createAssignedClient(agentId: string, client: Partial<User>): Observable<User> {
    // Utilise le même endpoint que createClient
    return this.createClient(agentId, client);
  }

  updateClientProfile(agentId: string, clientId: string, client: Partial<User>): Observable<User> {
    // Utilise le même endpoint que updateClient
    return this.updateClient(agentId, clientId, client);
  }

  /**
   * Retourne tous les utilisateurs (agents, clients, etc.)
   */
  getAllUsers(): Observable<User[]> {
    // Utilise searchUsers avec une requête vide pour tout récupérer (limite à 1000)
    return this.searchUsers('', undefined, 0, 1000);
  }
}
