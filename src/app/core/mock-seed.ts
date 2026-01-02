/**
 * Client-side mock seeder for development.
 * Writes sample accounts, transactions and payments to localStorage
 * if they are not already present. Safe to call multiple times.
 */
export function seedMockData(): void {
  try {
    if (localStorage.getItem('mock-seeded')) return;

    const accounts = [
      { id: 'acc-1', ownerId: '1', accountNumber: 'MAD-100-0001', type: 'CURRENT', currency: 'MAD', balance: 12500.5 },
      { id: 'acc-2', ownerId: '1', accountNumber: 'USD-200-0002', type: 'SAVINGS', currency: 'USD', balance: 3200.0 },
      { id: 'acc-3', ownerId: '3', accountNumber: 'MAD-300-0003', type: 'CURRENT', currency: 'MAD', balance: 540.75 }
    ];

    const transactions = [
      { id: 'tx-1', fromAccount: 'acc-1', toAccount: 'acc-3', amount: 250.0, currency: 'MAD', status: 'COMPLETED', createdAt: '2025-12-15T10:23:00Z', description: 'Paiement facture' },
      { id: 'tx-2', fromAccount: 'acc-2', toAccount: 'acc-1', amount: 150.0, currency: 'USD', status: 'PENDING', createdAt: '2025-12-20T14:11:00Z', description: 'Transfert intercompte' }
    ];

    const payments = [
      { id: 'pay-1', userId: '1', fromAccountId: 'acc-1', amount: 120.0, currency: 'MAD', status: 'COMPLETED', createdAt: '2025-12-05T09:00:00Z', description: 'Paiement mobile' },
      { id: 'pay-2', userId: '3', fromAccountId: 'acc-3', amount: 45.5, currency: 'MAD', status: 'FAILED', createdAt: '2025-12-18T16:20:00Z', description: 'Paiement magasin' }
    ];

    localStorage.setItem('mock_accounts', JSON.stringify(accounts));
    localStorage.setItem('mock_transactions', JSON.stringify(transactions));
    localStorage.setItem('mock_payments', JSON.stringify(payments));

    // Mark as seeded to avoid overwriting user changes
    localStorage.setItem('mock-seeded', new Date().toISOString());
    console.log('Mock data seeded: accounts, transactions, payments');
  } catch (e) {
    // If localStorage not available, ignore silently
    // eslint-disable-next-line no-console
    console.warn('Could not seed mock data', e);
  }
}
