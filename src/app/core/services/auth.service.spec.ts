import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { AuthService } from './auth.service';
import { AuditService } from './audit.service';

// Avoid TypeScript conflict between Chai and Jasmine expect in this workspace
declare const expect: any;

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [
        AuthService,
        { provide: AuditService, useValue: { createEvent: () => of(null) } }
      ]
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('falls back to mock when backend login fails', (done) => {
    const req = service.loginWithDTO({ username: 'client', password: 'client123' }).subscribe(res => {
      expect(res).toBeTruthy();
      expect(res.access_token).toBeTruthy();
      expect(localStorage.getItem('access_token')).toBeTruthy();
      done();
    });

    const httpReq = httpMock.expectOne('/api/v1/auth/login');
    httpReq.flush({}, { status: 500, statusText: 'Server Error' });
  });

  it('login() falls back to mock when backend login fails', (done) => {
    service.login('client', 'client123').subscribe(res => {
      expect(res).toBeTruthy();
      expect(res.success).toBeTrue();
      done();
    });

    const httpReq = httpMock.expectOne('/api/v1/auth/login');
    httpReq.flush({}, { status: 500, statusText: 'Server Error' });
  });
});
