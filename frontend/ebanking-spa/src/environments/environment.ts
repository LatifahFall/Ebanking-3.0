/**
 * Environment configuration for development
 * Uses localhost or mock data
 */
export const environment = {
  production: false,
  useMock: true, // Use mock data in development
  
  // GraphQL Gateway (point d'entrée unique) - Development
  graphqlGatewayUrl: 'http://localhost:8090/graphql',
  
  // Base URL for all services (development - localhost)
  apiBaseUrl: 'http://localhost',
  
  // Individual service URLs (development) - Fallback si GraphQL non disponible
  authServiceUrl: 'http://localhost:8081/api/auth',
  userServiceUrl: 'http://localhost:8082/api/users',
  accountServiceUrl: 'http://localhost:8082/api/accounts',
  paymentServiceUrl: 'http://localhost:8084/api/payments',
  transactionServiceUrl: 'http://localhost:8085/api/transactions',
  cryptoServiceUrl: 'http://localhost:8083/api/crypto',
  notificationServiceUrl: 'http://localhost:8086/api/notifications',
  analyticsServiceUrl: 'http://localhost:8087/api/v1/analytics',
  auditServiceUrl: 'http://localhost:8088/api/audit',
  
  // Flag pour utiliser GraphQL Gateway
  useGraphQL: false, // En développement, utiliser REST direct par défaut
  
  // Keycloak (if used)
  keycloakUrl: 'http://localhost:8080/auth',
  
  // Feature flags
  enableAnalytics: true,
  enableCrypto: true,
  enableNotifications: true
};

