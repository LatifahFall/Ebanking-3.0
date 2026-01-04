import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';
import { seedMockData } from './app/core/mock-seed';

// Seed mock data once on app start (safe no-op if already seeded)
try {
  seedMockData();
} catch (e) {
  // ignore
}

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
