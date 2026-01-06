# 🔗 Intégration GraphQL Gateway - Frontend

## 📋 Vue d'ensemble

Ce document décrit l'intégration du GraphQL Gateway dans le frontend Angular. Le frontend peut maintenant utiliser le GraphQL Gateway comme point d'entrée unique au lieu d'appeler directement les services REST.

## ✅ Ce qui a été fait

### 1. Installation des dépendances
- ✅ `@apollo/client` - Client GraphQL
- ✅ `apollo-angular` - Intégration Angular pour Apollo Client
- ✅ `graphql` - Bibliothèque GraphQL

### 2. Configuration Apollo Client
- ✅ Configuration dans `app.config.ts`
- ✅ URL du GraphQL Gateway configurée via `environment.graphqlGatewayUrl`
- ✅ Cache InMemory activé

### 3. Service GraphQL de base
- ✅ `GraphQLService` créé dans `src/app/core/services/graphql.service.ts`
- ✅ Méthodes `query()` et `mutate()` pour exécuter des requêtes GraphQL
- ✅ Propagation automatique du token JWT via header `Authorization`

### 4. Configuration des environnements
- ✅ `environment.ts` (dev) : `graphqlGatewayUrl: 'http://localhost:8090/graphql'`
- ✅ `environment.prod.ts` (prod) : `graphqlGatewayUrl: 'http://34.22.142.65/api/gateway/graphql'`
- ✅ Flag `useGraphQL` ajouté pour activer/désactiver GraphQL

### 5. Adaptation AuthService
- ✅ `loginWithDTO()` utilise GraphQL si `useGraphQL: true`
- ✅ `refreshToken()` utilise GraphQL si `useGraphQL: true`
- ✅ `logout()` utilise GraphQL si `useGraphQL: true`
- ✅ Fallback automatique vers REST si GraphQL échoue

## 🔄 Stratégie de migration

### Mode hybride (actuel)
Le frontend supporte **deux modes** :

1. **Mode GraphQL** (`useGraphQL: true`)
   - Utilise le GraphQL Gateway
   - Fallback vers REST en cas d'erreur
   - Fallback vers mock si REST échoue aussi

2. **Mode REST** (`useGraphQL: false`)
   - Utilise directement les services REST
   - Fallback vers mock en cas d'erreur

### Ordre de priorité (quand `useGraphQL: true`)
```
GraphQL Gateway → REST direct → Mock
```

## 📝 Services à adapter (TODO)

Les services suivants doivent être adaptés pour utiliser GraphQL :

### Priorité 1 (Services critiques)
- [ ] `user.service.ts` - Gestion des utilisateurs
- [ ] `account.service.ts` - Gestion des comptes
- [ ] `payment.service.ts` - Gestion des paiements

### Priorité 2 (Services secondaires)
- [ ] `crypto.service.ts` - Portefeuille crypto
- [ ] `notification.service.ts` - Notifications
- [ ] `analytics-backend.service.ts` - Analytics

## 🔧 Exemple d'adaptation d'un service

### Avant (REST)
```typescript
getUserById(id: string): Observable<User> {
  return this.http.get<User>(`${this.baseUrl}/admin/users/${id}`);
}
```

### Après (GraphQL)
```typescript
getUserById(id: string): Observable<User> {
  if (this.useGraphQL && this.graphqlService) {
    const query = `
      query GetUser($id: ID!) {
        userById(id: $id) {
          id
          login
          email
          fname
          lname
          role
          isActive
        }
      }
    `;
    return this.graphqlService.query<{ userById: User }>(query, { id })
      .pipe(map(result => result.userById));
  }
  // Fallback REST
  return this.http.get<User>(`${this.baseUrl}/admin/users/${id}`);
}
```

## 📊 Schéma GraphQL disponible

Le GraphQL Gateway expose **50 opérations** :

### Queries (27)
- `users`, `userById`, `me`, `clientsByAgent`, `agentByClient`
- `accountById`, `accountsByUserId`, `accountBalance`, `accountTransactions`
- `verifyToken`, `tokenInfo`
- `paymentById`, `paymentsByUserId`, `paymentsByAccountId`
- `cryptoWalletByUserId`, `cryptoTransactionsByWalletId`, `cryptoCoins`, `cryptoCoinById`
- `notificationsByUserId`, `inAppNotificationsByUserId`
- `auditEvents`, `auditEventById`, `auditEventsByUserId`, `auditEventsByType`
- `activeAlerts`, `dashboardSummary`, `spendingBreakdown`, `balanceTrend`, `recommendations`, `adminOverview`

### Mutations (23)
- `createUser`, `activateUser`, `deactivateUser`, `updateProfile`, `assignClient`, `unassignClient`
- `createAccount`, `updateAccount`, `suspendAccount`, `closeAccount`
- `login`, `refreshToken`, `logout`
- `createPayment`, `cancelPayment`, `reversePayment`
- `createCryptoWallet`, `activateCryptoWallet`, `deactivateCryptoWallet`, `buyCrypto`, `sellCrypto`
- `sendNotification`, `markNotificationAsRead`
- `resolveAlert`

## 🔐 Authentification

Le token JWT est automatiquement propagé :
1. `AuthService.getToken()` récupère le token depuis `localStorage`
2. `GraphQLService` ajoute le header `Authorization: Bearer <token>`
3. Le GraphQL Gateway transmet le token aux microservices

## ⚙️ Configuration

### Développement (`environment.ts`)
```typescript
useGraphQL: false, // REST direct par défaut
graphqlGatewayUrl: 'http://localhost:8090/graphql',
```

### Production (`environment.prod.ts`)
```typescript
useGraphQL: true, // GraphQL Gateway activé
graphqlGatewayUrl: 'http://34.22.142.65/api/gateway/graphql',
```

## 🚀 Prochaines étapes

1. **Adapter les services restants** pour utiliser GraphQL
2. **Tester avec le gateway déployé** une fois disponible
3. **Ajuster l'URL du gateway** selon le déploiement final
4. **Désactiver le fallback REST** une fois GraphQL validé en production

## 📚 Documentation GraphQL Gateway

Voir `services/graphql-gateway/README_GRAPHQL.md` pour :
- Liste complète des opérations
- Exemples de queries/mutations
- Structure des types GraphQL

