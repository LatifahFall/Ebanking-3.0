import { defineConfig } from 'cypress'

export default defineConfig({
  e2e: {
    // Base URL used during tests (default dev server)
    baseUrl: 'http://localhost:4200',
    specPattern: 'cypress/e2e/**/*.cy.{js,ts}',
    supportFile: false,
    video: false,
    defaultCommandTimeout: 8000
  }
})
