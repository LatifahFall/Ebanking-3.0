/**
 * Environment configuration for production
 * Uses deployed backend services on Google Cloud Platform (GKE)
 * IP: 34.22.142.65
 */
export const environment = {
  production: true,
  useMock: false, // Use real HTTP calls in production
  
  // GraphQL Gateway (point d'entrée unique)
  graphqlGatewayUrl: 'http://34.22.142.65/api/gateway/graphql', // À confirmer avec le déploiement
  
  // Base URL for all services (production - GCP GKE) - Fallback si GraphQL non disponible
  apiBaseUrl: 'http://34.22.142.65/api',
  
  // Individual service URLs (production) - Fallback si GraphQL non disponible
  authServiceUrl: 'http://34.22.142.65/api/auth',
  userServiceUrl: 'http://34.22.142.65/api/users',
  accountServiceUrl: 'http://34.22.142.65/api/accounts',
  paymentServiceUrl: 'http://34.22.142.65/api/payments',
  transactionServiceUrl: 'http://34.22.142.65/api/transactions', // Note: may need to verify endpoint
  cryptoServiceUrl: 'http://34.22.142.65/api/crypto',
  notificationServiceUrl: 'http://34.22.142.65/api/notifications',
  analyticsServiceUrl: 'http://34.22.142.65/api/v1/analytics', // Note: verify if analytics service is deployed
  auditServiceUrl: 'http://34.22.142.65/api/audit',
  
  // Flag pour utiliser GraphQL Gateway
  useGraphQL: true, // Utiliser GraphQL Gateway si true, sinon REST direct
  
  // Keycloak (if used)
  keycloakUrl: 'http://34.22.142.65/auth',
  
  // Feature flags
  enableAnalytics: true,
  enableCrypto: true,
  enableNotifications: true
};

