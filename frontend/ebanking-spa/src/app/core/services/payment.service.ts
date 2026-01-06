import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of, delay, catchError, map } from 'rxjs';
import { PaymentRequest, PaymentResponse, PaymentListResponse, PaymentStatus, PaymentType, QRCodePaymentRequest, QRCodeResponse } from '../../models';
import { environment } from '../../../environments/environment';

/**
 * Payment Service (MOCK)
 * Mirrors backend PaymentController endpoints
 */
@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private payments: PaymentResponse[] = [];
  // Build base using environment.paymentServiceUrl with optional local override
  private baseUrl = (localStorage.getItem('PAYMENTS_BASE') || environment.paymentServiceUrl || `${environment.apiBaseUrl}/api/payments`).replace(/\/+$/, '');
  private adminRulesUrl = (localStorage.getItem('ADMIN_BASE') || `${environment.apiBaseUrl}/api/admin/payment-rules`).replace(/\/+$/, '');
  private readonly useMock = environment.useMock;
  // In-memory admin payment rules mock
  private adminPaymentRules: any[] = [
    { uuid: 'rule-1', name: 'Max transfer 10k', enabled: true, conditions: { maxAmount: 10000 } }
  ];

  constructor(private http: HttpClient) {
    // Seed with some mock payments
    this.payments = [
      {
        id: 'pay-001',
        fromAccountId: 'acc-001',
        toAccountId: 'acc-002',
        amount: 150.5,
        currency: 'USD',
        paymentType: PaymentType.STANDARD, // Backend enum: STANDARD, INSTANT, RECURRING, BIOMETRIC, QR_CODE
        status: PaymentStatus.COMPLETED,
        beneficiaryName: 'Alice',
        reference: 'INV-1001',
        description: 'Invoice payment',
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        completedAt: new Date(Date.now() - 86000000).toISOString()
      },
      {
        id: 'pay-002',
        fromAccountId: 'acc-002',
        toAccountId: 'acc-003',
        amount: 25.0,
        currency: 'USD',
        paymentType: PaymentType.STANDARD, // Backend enum: STANDARD, INSTANT, RECURRING, BIOMETRIC, QR_CODE
        status: PaymentStatus.PENDING,
        beneficiaryName: 'Mobile Topup',
        reference: 'TOPUP-01',
        description: 'Top up mobile',
        createdAt: new Date().toISOString()
      }
    ];
  }

  // POST /api/payments
  initiatePayment(request: PaymentRequest): Observable<PaymentResponse> {
    if (this.useMock) {
      // Use mock data
      const newPayment: PaymentResponse = {
        id: `pay-${Math.random().toString(36).substr(2, 9)}`,
        fromAccountId: request.fromAccountId,
        toAccountId: request.toAccountId,
        amount: request.amount,
        currency: request.currency,
        paymentType: request.paymentType,
        status: PaymentStatus.PENDING,
        beneficiaryName: request.beneficiaryName,
        reference: request.reference,
        description: request.description,
        createdAt: new Date().toISOString()
      };
      this.payments.unshift(newPayment);
      return of(newPayment).pipe(delay(500));
    }
    // Try backend first
    return this.http.post<PaymentResponse>(`${this.baseUrl}`, request).pipe(
      catchError(() => {
        // Fallback to mock
        const newPayment: PaymentResponse = {
          id: `pay-${Math.random().toString(36).substr(2, 9)}`,
          fromAccountId: request.fromAccountId,
          toAccountId: request.toAccountId,
          amount: request.amount,
          currency: request.currency,
          paymentType: request.paymentType,
          status: PaymentStatus.PENDING,
          beneficiaryName: request.beneficiaryName,
          reference: request.reference,
          description: request.description,
          createdAt: new Date().toISOString()
        };
        this.payments.unshift(newPayment);
        return of(newPayment).pipe(delay(500));
      })
    );
  }

  // GET /api/payments/{id}
  getPayment(id: string): Observable<PaymentResponse | null> {
    if (this.useMock) {
      const payment = this.payments.find(p => p.id === id) || null;
      return of(payment).pipe(delay(200));
    }
    return this.http.get<PaymentResponse>(`${this.baseUrl}/${id}`).pipe(
      catchError(() => {
        const payment = this.payments.find(p => p.id === id) || null;
        return of(payment).pipe(delay(200));
      })
    );
  }

  // GET /api/payments
  getPayments(accountId?: string, status?: PaymentStatus, page = 0, size = 10, sortBy = 'createdAt', sortDir: 'ASC' | 'DESC' = 'DESC'): Observable<PaymentListResponse> {
    // Attempt to call backend paginated endpoint
    let params = new HttpParams()
      .set('page', `${page}`)
      .set('size', `${size}`)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);
    if (accountId) params = params.set('accountId', accountId);
    if (status) params = params.set('status', status as any);

    if (this.useMock) {
      // Fallback to mock pagination
      let filtered = [...this.payments];
      if (accountId) filtered = filtered.filter(p => p.fromAccountId === accountId || p.toAccountId === accountId);
      if (status) filtered = filtered.filter(p => p.status === status);

      const totalElements = filtered.length;
      const totalPages = Math.ceil(totalElements / size);

      // Apply sorting in-memory as fallback
      const sorted = filtered.sort((a, b) => {
        const field = sortBy as keyof PaymentResponse;
        const av = (a as any)[field];
        const bv = (b as any)[field];
        if (av == null) return 1;
        if (bv == null) return -1;
        if (sortDir === 'ASC') return av > bv ? 1 : av < bv ? -1 : 0;
        return av < bv ? 1 : av > bv ? -1 : 0;
      });

      const paged = sorted.slice(page * size, page * size + size);
      return of({ payments: paged, page, size, totalElements, totalPages }).pipe(delay(300));
    }
    return this.http.get<PaymentListResponse>(this.baseUrl, { params }).pipe(
      catchError(() => {
        // Fallback to mock pagination
        let filtered = [...this.payments];
        if (accountId) filtered = filtered.filter(p => p.fromAccountId === accountId || p.toAccountId === accountId);
        if (status) filtered = filtered.filter(p => p.status === status);

        const totalElements = filtered.length;
        const totalPages = Math.ceil(totalElements / size);

        // Apply sorting in-memory as fallback
        const sorted = filtered.sort((a, b) => {
          const field = sortBy as keyof PaymentResponse;
          const av = (a as any)[field];
          const bv = (b as any)[field];
          if (av == null) return 1;
          if (bv == null) return -1;
          if (sortDir === 'ASC') return av > bv ? 1 : av < bv ? -1 : 0;
          return av < bv ? 1 : av > bv ? -1 : 0;
        });

        const paged = sorted.slice(page * size, page * size + size);

        return of({ payments: paged, page, size, totalElements, totalPages }).pipe(delay(300));
      })
    );
  }

  // POST /api/payments/{id}/cancel
  cancelPayment(id: string): Observable<{ success: boolean; message: string }> {
    if (this.useMock) {
      const p = this.payments.find(x => x.id === id);
      if (!p) return of({ success: false, message: 'Payment not found' }).pipe(delay(200));
      if (p.status === PaymentStatus.COMPLETED) {
        return of({ success: false, message: 'Cannot cancel completed payment' }).pipe(delay(200));
      }
      p.status = PaymentStatus.CANCELLED;
      return of({ success: true, message: 'Payment cancelled' }).pipe(delay(200));
    }
    return this.http.post(`${this.baseUrl}/${id}/cancel`, {}).pipe(
      map(() => ({ success: true, message: 'Payment cancelled' })),
      catchError(() => {
        const p = this.payments.find(x => x.id === id);
        if (!p) return of({ success: false, message: 'Payment not found' }).pipe(delay(200));
        if (p.status === PaymentStatus.COMPLETED) {
          return of({ success: false, message: 'Cannot cancel completed payment' }).pipe(delay(200));
        }
        p.status = PaymentStatus.CANCELLED;
        return of({ success: true, message: 'Payment cancelled' }).pipe(delay(200));
      })
    );
  }

  // POST /api/payments/{id}/reverse
  reversePayment(id: string, reason?: string): Observable<{ success: boolean; message: string }> {
    if (this.useMock) {
      const p = this.payments.find(x => x.id === id);
      if (!p) return of({ success: false, message: 'Payment not found' }).pipe(delay(200));
      if (p.status !== PaymentStatus.COMPLETED) {
        return of({ success: false, message: 'Only completed payments can be reversed' }).pipe(delay(200));
      }

      const reversal: PaymentResponse = {
        id: `pay-${Math.random().toString(36).substr(2, 9)}`,
        fromAccountId: p.toAccountId || '',
        toAccountId: p.fromAccountId,
        amount: p.amount,
        currency: p.currency,
        paymentType: p.paymentType,
        status: PaymentStatus.COMPLETED,
        beneficiaryName: `REVERSAL of ${p.id}`,
        reference: `REV-${p.id}`,
        description: `Reversal: ${reason || 'unspecified'}`,
        createdAt: new Date().toISOString(),
        completedAt: new Date().toISOString()
      };
      this.payments.unshift(reversal);
      p.status = PaymentStatus.REVERSED as any;
      return of({ success: true, message: 'Payment reversed' }).pipe(delay(300));
    }
    const params = reason ? new HttpParams().set('reason', reason) : undefined;
    return this.http.post(`${this.baseUrl}/${id}/reverse`, null, { params }).pipe(
      map(() => ({ success: true, message: 'Payment reversed' })),
      catchError(() => {
        const p = this.payments.find(x => x.id === id);
        if (!p) return of({ success: false, message: 'Payment not found' }).pipe(delay(200));
        if (p.status !== PaymentStatus.COMPLETED) {
          return of({ success: false, message: 'Only completed payments can be reversed' }).pipe(delay(200));
        }

        const reversal: PaymentResponse = {
          id: `pay-${Math.random().toString(36).substr(2, 9)}`,
          fromAccountId: p.toAccountId || '',
          toAccountId: p.fromAccountId,
          amount: p.amount,
          currency: p.currency,
          paymentType: p.paymentType,
          status: PaymentStatus.COMPLETED,
          beneficiaryName: `REVERSAL of ${p.id}`,
          reference: `REV-${p.id}`,
          description: `Reversal: ${reason || 'unspecified'}`,
          createdAt: new Date().toISOString(),
          completedAt: new Date().toISOString()
        };

        this.payments.unshift(reversal);
        p.status = PaymentStatus.REVERSED as any;

        return of({ success: true, message: 'Payment reversed' }).pipe(delay(300));
      })
    );
  }

  // POST /api/payments/qrcode/generate
  generateQRCode(request: PaymentRequest): Observable<QRCodeResponse> {
    if (this.useMock) {
      return of({
        qrCode: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
        message: 'QR code generated (mock)',
        format: 'PNG (base64)'
      }).pipe(delay(300));
    }
    return this.http.post<QRCodeResponse>(`${this.baseUrl}/qrcode/generate`, request).pipe(
      catchError(() => {
        // Fallback: generate a mock QR code (in real app, this would be handled by backend)
        return of({
          qrCode: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
          message: 'QR code generated (mock)',
          format: 'PNG (base64)'
        }).pipe(delay(300));
      })
    );
  }

  // POST /api/payments/qrcode
  initiateQRCodePayment(request: QRCodePaymentRequest): Observable<PaymentResponse> {
    if (this.useMock) {
      const newPayment: PaymentResponse = {
        id: `pay-${Math.random().toString(36).substr(2, 9)}`,
        fromAccountId: request.fromAccountId,
        toAccountId: request.toAccountId,
        amount: request.amount,
        currency: request.currency,
        paymentType: PaymentType.QR_CODE,
        status: PaymentStatus.COMPLETED,
        beneficiaryName: request.beneficiaryName,
        reference: request.reference,
        description: request.description,
        createdAt: new Date().toISOString(),
        completedAt: new Date().toISOString()
      };
      this.payments.unshift(newPayment);
      return of(newPayment).pipe(delay(500));
    }
    return this.http.post<PaymentResponse>(`${this.baseUrl}/qrcode`, request).pipe(
      catchError(() => {
        // Fallback to mock
        const newPayment: PaymentResponse = {
          id: `pay-${Math.random().toString(36).substr(2, 9)}`,
          fromAccountId: request.fromAccountId,
          toAccountId: request.toAccountId,
          amount: request.amount,
          currency: request.currency,
          paymentType: PaymentType.QR_CODE,
          status: PaymentStatus.COMPLETED,
          beneficiaryName: request.beneficiaryName,
          reference: request.reference,
          description: request.description,
          createdAt: new Date().toISOString(),
          completedAt: new Date().toISOString()
        };

        this.payments.unshift(newPayment);
        return of(newPayment).pipe(delay(500));
      })
    );
  }

  // ----------------------------------------
  // Biometric payments
  // POST /api/payments/biometric/generate-qr
  generateBiometricQRCode(request: PaymentRequest): Observable<QRCodeResponse> {
    if (this.useMock) {
      return of({ qrCode: 'data:image/png;base64,MOCK-BIO-QR', message: 'Biometric QR (mock)', format: 'PNG (base64)' }).pipe(delay(300));
    }
    return this.http.post<QRCodeResponse>(`${this.baseUrl}/biometric/generate-qr`, request).pipe(
      catchError(() => of({ qrCode: 'data:image/png;base64,MOCK-BIO-QR', message: 'Biometric QR (fallback)', format: 'PNG (base64)' }))
    );
  }

  // POST /api/payments/biometric
  initiateBiometricPayment(request: QRCodePaymentRequest | PaymentRequest): Observable<PaymentResponse> {
    if (this.useMock) {
      const newPayment: PaymentResponse = {
        id: `pay-${Math.random().toString(36).substr(2, 9)}`,
        fromAccountId: (request as any).fromAccountId,
        toAccountId: (request as any).toAccountId,
        amount: (request as any).amount,
        currency: (request as any).currency || 'USD',
        paymentType: PaymentType.BIOMETRIC,
        status: PaymentStatus.COMPLETED,
        beneficiaryName: (request as any).beneficiaryName || 'Biometric Pay',
        reference: (request as any).reference || '',
        description: (request as any).description || '',
        createdAt: new Date().toISOString(),
        completedAt: new Date().toISOString()
      };
      this.payments.unshift(newPayment);
      return of(newPayment).pipe(delay(500));
    }
    return this.http.post<PaymentResponse>(`${this.baseUrl}/biometric`, request as any).pipe(
      catchError(() => {
        const newPayment: PaymentResponse = {
          id: `pay-${Math.random().toString(36).substr(2, 9)}`,
          fromAccountId: (request as any).fromAccountId,
          toAccountId: (request as any).toAccountId,
          amount: (request as any).amount,
          currency: (request as any).currency || 'USD',
          paymentType: PaymentType.BIOMETRIC,
          status: PaymentStatus.COMPLETED,
          beneficiaryName: (request as any).beneficiaryName || 'Biometric Pay',
          reference: (request as any).reference || '',
          description: (request as any).description || '',
          createdAt: new Date().toISOString(),
          completedAt: new Date().toISOString()
        };
        this.payments.unshift(newPayment);
        return of(newPayment).pipe(delay(500));
      })
    );
  }

  // ----------------------------------------
  // Admin - Payment Rules
  // GET /api/admin/payment-rules
  getPaymentRules(): Observable<any[]> {
    if (this.useMock) {
      return of(this.adminPaymentRules).pipe(delay(200));
    }
    return this.http.get<any[]>(this.adminRulesUrl).pipe(
      catchError(() => of(this.adminPaymentRules))
    );
  }

  // POST /api/admin/payment-rules
  createPaymentRule(rule: any): Observable<any> {
    if (this.useMock) {
      const newRule = { ...rule, uuid: `rule-${Date.now()}` };
      this.adminPaymentRules.push(newRule);
      return of(newRule).pipe(delay(200));
    }
    return this.http.post<any>(this.adminRulesUrl, rule).pipe(
      catchError(() => {
        const newRule = { ...rule, uuid: `rule-${Date.now()}` };
        this.adminPaymentRules.push(newRule);
        return of(newRule).pipe(delay(200));
      })
    );
  }

  // PUT /api/admin/payment-rules/{uuid}
  updatePaymentRule(uuid: string, rule: any): Observable<any> {
    if (this.useMock) {
      const idx = this.adminPaymentRules.findIndex(r => r.uuid === uuid);
      if (idx === -1) return of(null).pipe(delay(200));
      this.adminPaymentRules[idx] = { ...this.adminPaymentRules[idx], ...rule };
      return of(this.adminPaymentRules[idx]).pipe(delay(200));
    }
    return this.http.put<any>(`${this.adminRulesUrl}/${uuid}`, rule).pipe(
      catchError(() => {
        const idx = this.adminPaymentRules.findIndex(r => r.uuid === uuid);
        if (idx === -1) return of(null).pipe(delay(200));
        this.adminPaymentRules[idx] = { ...this.adminPaymentRules[idx], ...rule };
        return of(this.adminPaymentRules[idx]).pipe(delay(200));
      })
    );
  }

  // DELETE /api/admin/payment-rules/{uuid}
  deletePaymentRule(uuid: string): Observable<{ success: boolean }> {
    if (this.useMock) {
      const idx = this.adminPaymentRules.findIndex(r => r.uuid === uuid);
      if (idx === -1) return of({ success: false }).pipe(delay(200));
      this.adminPaymentRules.splice(idx, 1);
      return of({ success: true }).pipe(delay(200));
    }
    return this.http.delete<{ success: boolean }>(`${this.adminRulesUrl}/${uuid}`).pipe(
      catchError(() => {
        const idx = this.adminPaymentRules.findIndex(r => r.uuid === uuid);
        if (idx === -1) return of({ success: false }).pipe(delay(200));
        this.adminPaymentRules.splice(idx, 1);
        return of({ success: true }).pipe(delay(200));
      })
    );
  }
}
