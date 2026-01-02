describe('Payments smoke test', () => {
  const MOCK_USER = {
    id: '3',
    email: 'client@ebanking.com',
    firstName: 'Fatima',
    lastName: 'Zahra',
    fullName: 'Fatima Zahra',
    phoneNumber: '+212 622 222 222',
    role: 'CLIENT',
    status: 'ACTIVE'
  };

  beforeEach(() => {
    // Ensure the app treats us as authenticated (bypass guard)
    cy.visit('/', {
      onBeforeLoad(win) {
        win.localStorage.setItem('ebanking-user', JSON.stringify(MOCK_USER));
        win.localStorage.setItem('access_token', 'mock.token.signature');
      }
    });
  });

  it('creates a payment and verifies it appears in the list and pagination works', () => {
    // Navigate to payments route
    cy.visit('/payments');

    cy.contains('h2', 'Payments').should('be.visible');

    // Initiate a new payment
    cy.get('input[placeholder="From Account ID"]').type('test-from-acc');
    cy.get('input[placeholder="To Account ID"]').type('test-to-acc');
    cy.get('input[placeholder="Amount"]').type('42');
    // Currency defaults to USD but set it explicitly
    cy.get('input[placeholder="Currency"]').clear().type('USD');
    cy.contains('button', 'Send').click();

    // The app uses an in-memory fallback which unshifts the new payment; wait for it
    cy.contains('li', '42 USD').should('be.visible');

    // Check that the newest payment appears on page 1
    cy.get('ul > li').first().should('contain.text', '42 USD');

    // Now change page size to 1 to force pagination and verify navigation
    cy.get('select').first().select('1');

    // Wait for list to reload and confirm first page contains our payment
    cy.get('ul > li').first().then($li => {
      const firstText = $li.text();
      expect(firstText).to.contain('42 USD');
    });

    // Click next page and ensure content changes
    cy.contains('button', 'Next').click();

    cy.get('ul > li').first().should($li => {
      const t = $li.text();
      expect(t).to.not.contain('42 USD');
    });
  });
});
