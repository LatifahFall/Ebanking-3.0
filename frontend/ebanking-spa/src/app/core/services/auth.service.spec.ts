import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [AuthService]
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

    const httpReq = httpMock.expectOne('http://34.22.142.65/auth/login');
    httpReq.flush({}, { status: 500, statusText: 'Server Error' });
  });

  it('login() falls back to mock when backend login fails', (done) => {
    service.login('client', 'client123').subscribe(res => {
      expect(res).toBeTruthy();
      expect(res.success).toBeTrue();
      done();
    });

    const httpReq = httpMock.expectOne('http://34.22.142.65/auth/login');
    httpReq.flush({}, { status: 500, statusText: 'Server Error' });
  });
});
