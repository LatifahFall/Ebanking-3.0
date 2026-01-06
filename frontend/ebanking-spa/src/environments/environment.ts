/**
 * Environment configuration for development
 * Backend: GKE europe-west1-b via Cloudflare Tunnel
 * All services are now operational ✅
 */
export const environment = {
  production: false,
  useMock: false,

  // Base URL - Cloudflare Tunnel (HTTPS)
  apiBaseUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com',

  // Individual service URLs - All operational
  authServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/auth',
  userServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com',
  accountServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/accounts',
  paymentServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/payments',
  cryptoServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/coins',
  walletServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/wallets',
  transactionServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/transactions',
  notificationServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/notifications',
  auditServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/audit',
  analyticsServiceUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/analytics',

  // GraphQL Gateway (optional)
  graphqlGatewayUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/gateway/graphql',

  // Keycloak Identity & Access Management
  keycloakUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/auth',
  keycloakAdminUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/auth/admin',

  // Feature flags - All services operational
  useGraphQL: false,
  enableAnalytics: true,
  enableCrypto: true,        // ✅ Now working
  enableNotifications: true,
  enableAudit: true,         // ✅ Now working

  // API Configuration
  apiTimeout: 30000,
  maxRetries: 3,
  retryDelay: 1000,

  // Logging
  enableDebugLogs: true,
  enableErrorTracking: true,

  // WebSocket (for real-time notifications)
  wsUrl: 'wss://inspection-reaction-stolen-development.trycloudflare.com/ws'
};
