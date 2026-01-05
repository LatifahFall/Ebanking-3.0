import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuditService } from './audit.service';
import { AuditEvent } from '../../models';

describe('AuditService', () => {
  let service: AuditService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuditService]
    });

    service = TestBed.inject(AuditService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.removeItem('mock_audit_events');
    localStorage.removeItem('mock_audit_exports');
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.removeItem('mock_audit_events');
    localStorage.removeItem('mock_audit_exports');
  });

  it('should fallback to mock when createEvent backend fails', (done) => {
    const evt: AuditEvent = {
      eventType: 'USER_LOGIN',
      userId: 1,
      username: 'test',
      timestamp: new Date().toISOString()
    };

    service.createEvent(evt).subscribe(res => {
      // Jasmine expect
      expect(res).toBeTruthy();
      expect(res.id).toMatch(/mock-/);

      const stored = JSON.parse(localStorage.getItem('mock_audit_events') || '[]');
      expect(stored.length as number).toBeGreaterThan(0);
      done();
    });

    const req = httpMock.expectOne('/api/v1/audit/events');
    req.error(new ErrorEvent('Server Error'), { status: 500, statusText: 'Server Error' });
  });

  it('should return job and store export when backend export fails', (done) => {
    const start = new Date().toISOString();
    const end = new Date().toISOString();

    service.exportAuditReport(start, end, 'PDF').subscribe(job => {
      expect(job).toBeTruthy();
      expect(job.jobId).toContain('mock-job-');

      const exportsRaw = localStorage.getItem('mock_audit_exports');
      expect(exportsRaw).toBeTruthy();

      service.getExportFile(job.jobId).subscribe(blob => {
        expect(blob).toBeTruthy();
        expect((blob.size as number)).toBeGreaterThan(0);
        done();
      });

      const getReq = httpMock.expectOne(`/api/v1/audit/export/${job.jobId}`);
      getReq.error(new ErrorEvent('Server Error'), { status: 500, statusText: 'Server Error' });
    });

    const req = httpMock.expectOne(req => req.method === 'POST' && req.url.includes('/api/v1/audit/export'));
    req.flush({}, { status: 500, statusText: 'Server Error' });
  });
});
