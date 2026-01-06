import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

/**
 * GraphQL Service
 * Service de base pour les appels GraphQL via Apollo Client
 * Gère automatiquement l'authentification via le header Authorization
 */
@Injectable({
  providedIn: 'root'
})
export class GraphQLService {
  constructor(
    private apollo: Apollo
  ) {}

  /**
   * Exécute une query GraphQL avec authentification automatique
   */
  query<T = any>(query: string, variables?: any): Observable<T> {
    // Read token directly from storage to avoid DI circular dependency with AuthService
    const token = localStorage.getItem('access_token');
    const context: any = {};

    if (token) {
      context.headers = {
        'Authorization': `Bearer ${token}`
      };
    }

    return this.apollo.query<T>({
      query: gql(query),
      variables,
      context,
      fetchPolicy: 'network-only',
    }).pipe(
      map(result => result.data as T),
      catchError(error => {
        console.error('GraphQL Query Error:', error);
        throw error;
      })
    );
  }

  /**
   * Exécute une mutation GraphQL avec authentification automatique
   */
  mutate<T = any>(mutation: string, variables?: any): Observable<T> {
    // Read token directly from storage to avoid DI circular dependency with AuthService
    const token = localStorage.getItem('access_token');
    const context: any = {};

    if (token) {
      context.headers = {
        'Authorization': `Bearer ${token}`
      };
    }

    return this.apollo.mutate<T>({
      mutation: gql(mutation),
      variables,
      context,
    }).pipe(
      map(result => result.data as T),
      catchError(error => {
        console.error('GraphQL Mutation Error:', error);
        throw error;
      })
    );
  }
}
