import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  CryptoWallet,
  CryptoHolding,
  CryptoCoin,
  CryptoTransaction,
  BuyCryptoRequest,
  SellCryptoRequest
} from '../../models/crypto.model';

@Injectable({
  providedIn: 'root'
})
export class CryptoService {
  private readonly API_BASE_URL = '/api';

  constructor(private http: HttpClient) {}

  // Coins
  getAllCoinsDetails(): Observable<CryptoCoin[]> {
    return this.http.get<CryptoCoin[]>(`${this.API_BASE_URL}/coins/details`);
  }

  getCoinDetails(coinId: string): Observable<CryptoCoin> {
    return this.http.get<CryptoCoin>(`${this.API_BASE_URL}/coins/${coinId}`);
  }

  // Wallets
  createWallet(userId: string): Observable<CryptoWallet> {
    return this.http.post<CryptoWallet>(`${this.API_BASE_URL}/wallets?userId=${userId}`, {});
  }

  getWalletByUserId(userId: string): Observable<CryptoWallet> {
    return this.http.get<CryptoWallet>(`${this.API_BASE_URL}/wallets/user/${userId}`);
  }

  activateWallet(userId: string): Observable<CryptoWallet> {
    return this.http.patch<CryptoWallet>(`${this.API_BASE_URL}/wallets/activate?userId=${userId}`, {});
  }

  deactivateWallet(userId: string): Observable<CryptoWallet> {
    return this.http.patch<CryptoWallet>(`${this.API_BASE_URL}/wallets/deactivate?userId=${userId}`, {});
  }

  // Holdings
  getHoldings(walletId: string): Observable<CryptoHolding[]> {
    return this.http.get<CryptoHolding[]>(`${this.API_BASE_URL}/holdings/wallet/${walletId}`);
  }

  // Transactions
  getWalletTransactions(walletId: string): Observable<CryptoTransaction[]> {
    return this.http.get<CryptoTransaction[]>(`${this.API_BASE_URL}/transactions/wallet/${walletId}`);
  }

  buyCrypto(walletId: string, request: BuyCryptoRequest): Observable<CryptoTransaction> {
    return this.http.post<CryptoTransaction>(`${this.API_BASE_URL}/transactions/buy?walletId=${walletId}`, request);
  }

  sellCrypto(walletId: string, request: SellCryptoRequest): Observable<CryptoTransaction> {
    return this.http.post<CryptoTransaction>(`${this.API_BASE_URL}/transactions/sell?walletId=${walletId}`, request);
  }
}
