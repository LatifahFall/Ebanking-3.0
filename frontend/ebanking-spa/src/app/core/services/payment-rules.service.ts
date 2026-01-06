import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PaymentRule {
  uuid?: string;
  // Ajoutez ici les autres propriétés du modèle de règle de paiement
}

@Injectable({ providedIn: 'root' })
export class PaymentRulesService {
  private readonly baseUrl = 'http://34.22.142.65/api/admin/payment-rules';

  constructor(private http: HttpClient) {}

  getAllPaymentRules(): Observable<PaymentRule[]> {
    return this.http.get<PaymentRule[]>(this.baseUrl);
  }

  createPaymentRule(rule: PaymentRule): Observable<PaymentRule> {
    return this.http.post<PaymentRule>(this.baseUrl, rule);
  }

  updatePaymentRule(uuid: string, rule: PaymentRule): Observable<PaymentRule> {
    return this.http.put<PaymentRule>(`${this.baseUrl}/${uuid}`, rule);
  }

  deletePaymentRule(uuid: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${uuid}`);
  }
}

