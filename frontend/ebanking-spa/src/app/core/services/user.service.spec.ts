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

  it('should return error when getUserById backend fails', (done) => {
    service.getUserById('3').subscribe({
      next: () => {
        fail('Should not return user on backend error');
      },
      error: (err) => {
        expect(err.status).toBe(500);
        expect(err.statusText).toBe('Server Error');
        done();
      }
    });

    const httpReq = httpMock.expectOne('http://34.22.142.65/admin/users/3');
    httpReq.flush({}, { status: 500, statusText: 'Server Error' });
  });
});
