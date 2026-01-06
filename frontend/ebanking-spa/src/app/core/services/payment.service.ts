import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PaymentRequest, PaymentResponse, PaymentListResponse, PaymentStatus, PaymentType, QRCodePaymentRequest, QRCodeResponse } from '../../models';
import { environment } from '../../../environments/environment';

/**
 * Payment Service
 * Utilise uniquement l'API REST réelle, aucune logique mock
 */
@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private baseUrl = (localStorage.getItem('PAYMENTS_BASE') || environment.paymentServiceUrl || `${environment.apiBaseUrl}/api/payments`).replace(/\/+$/, '');
  private adminRulesUrl = (localStorage.getItem('ADMIN_BASE') || `${environment.apiBaseUrl}/api/admin/payment-rules`).replace(/\/+$/, '');

  constructor(private http: HttpClient) {}

  // Payments
  initiatePayment(request: PaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}`, request);
  }

  getPaymentById(id: string): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.baseUrl}/${id}`);
  }

  listPayments(accountId: string, status?: string, page?: number, size?: number): Observable<PaymentListResponse> {
    let params = new HttpParams().set('accountId', accountId);
    if (status) params = params.set('status', status);
    if (page !== undefined) params = params.set('page', page.toString());
    if (size !== undefined) params = params.set('size', size.toString());
    return this.http.get<PaymentListResponse>(`${this.baseUrl}`, { params });
  }

  cancelPayment(id: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}/${id}/cancel`, {});
  }

  reversePayment(id: string, reason: string): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}/${id}/reverse?reason=${encodeURIComponent(reason)}`, {});
  }

  // QR Code Payments
  generateQRCode(request: QRCodePaymentRequest): Observable<QRCodeResponse> {
    return this.http.post<QRCodeResponse>(`${this.baseUrl}/qrcode/generate`, request);
  }

  initiateQRCodePayment(request: QRCodePaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}/qrcode`, request);
  }

  // Biometric Payments
  generateBiometricQR(request: any): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/biometric/generate-qr`, request);
  }

  initiateBiometricPayment(request: any): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(`${this.baseUrl}/biometric`, request);
  }
}
