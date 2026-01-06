export const environment = {
  production: true,
  useMock: false,

  // Base URL - Production API
  apiBaseUrl: 'https://api.flagsbank.org',

  // Service URLs - All operational
  authServiceUrl: 'https://api.flagsbank.org/auth',
  userServiceUrl: 'https://api.flagsbank.org',
  accountServiceUrl: 'https://api.flagsbank.org/api/accounts',
  paymentServiceUrl: 'https://api.flagsbank.org/api/payments',
  cryptoServiceUrl: 'https://api.flagsbank.org/api/coins',
  walletServiceUrl: 'https://api.flagsbank.org/api/wallets',
  transactionServiceUrl: 'https://api.flagsbank.org/api/transactions',
  notificationServiceUrl: 'https://api.flagsbank.org/api/notifications',
  auditServiceUrl: 'https://api.flagsbank.org/api/audit',
  analyticsServiceUrl: 'https://api.flagsbank.org/api/analytics',

  graphqlGatewayUrl: 'https://api.flagsbank.org/api/gateway/graphql',

  keycloakUrl: 'https://api.flagsbank.org/auth',
  keycloakAdminUrl: 'https://api.flagsbank.org/auth/admin',

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
  wsUrl: 'wss://api.flagsbank.org/ws'
};