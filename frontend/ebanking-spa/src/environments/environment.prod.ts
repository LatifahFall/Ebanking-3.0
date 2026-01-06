export const environment = {
  production: true,
  useMock: false,

  // Base URL - Cloudflare Tunnel
  apiBaseUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com',

  // Service URLs - All operational
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

  graphqlGatewayUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/api/gateway/graphql',

  keycloakUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/auth',
  keycloakAdminUrl: 'https://inspection-reaction-stolen-development.trycloudflare.com/auth/admin',

  // Feature flags - All services operational
  useGraphQL: false,
  enableAnalytics: true,
  enableCrypto: true,
  enableNotifications: true,
  enableAudit: true,

  // API Configuration
  apiTimeout: 30000,
  maxRetries: 3,
  retryDelay: 1000,

  // Logging (disabled in production)
  enableDebugLogs: false,
  enableErrorTracking: true,

  // WebSocket
  wsUrl: 'wss://inspection-reaction-stolen-development.trycloudflare.com/ws'
};
