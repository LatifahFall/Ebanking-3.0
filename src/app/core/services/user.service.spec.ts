import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService]
    });

    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('falls back to mock when getUserById backend fails', (done) => {
    service.getUserById('3').subscribe(u => {
      expect(u).toBeTruthy();
      expect(u?.email).toContain('@');
      done();
    });

    const httpReq = httpMock.expectOne('/api/v1/admin/users/3');
    httpReq.flush({}, { status: 500, statusText: 'Server Error' });
  });
});
