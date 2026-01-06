import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private readonly TOKEN_KEY = 'access_token';

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = localStorage.getItem(this.TOKEN_KEY);
    let url = req.url;
    // Préfixer les URL relatives par apiBaseUrl (si l'URL n'est pas absolue)
    if (!/^https?:\/\//i.test(url)) {
      const base = (environment.apiBaseUrl || '').replace(/\/$/, '');
      url = `${base}/${url.replace(/^\/+/, '')}`;
    }
    const headers = token ? { Authorization: `Bearer ${token}` } : undefined;
    const cloned = req.clone({ url, setHeaders: headers });
    return next.handle(cloned);
  }
}
