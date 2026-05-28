import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
@Injectable({ providedIn: 'root' })
export class AuthService {
  private base = `${environment.apiBaseUrl}/api/auth`;
  constructor(private http: HttpClient) {}
  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.base}/login`, { email, password }).pipe(
      tap(user => {
        localStorage.setItem('userId', user.id);
        localStorage.setItem('userEmail', user.email);
        localStorage.setItem('userName', user.name);
        localStorage.setItem('userRole', user.role);
        if (user.phone) localStorage.setItem('userPhone', user.phone);
        if (user.createdAt) localStorage.setItem('userCreatedAt', user.createdAt);
      })
    );
  }
  register(name: string, email: string, password: string, phone: string): Observable<any> {
    return this.http.post(`${this.base}/register`, { name, email, password, phone }, { responseType: 'text' }) as Observable<any>;
  }
  registerPharmacy(dto: {
    pharmacyName: string;
    pharmacyAddress: string;
    contactPhoneNumber: string;
    locationLatitude: number | null;
    locationLongitude: number | null;
    isOperated247: boolean;
    sellerEmailAddress: string;
  }): Observable<any> {
    return this.http.post<any>(`${environment.apiBaseUrl}/api/seller-onboarding/register-pharmacy`, dto);
  }
  logout(): void {
    this.http.post(`${this.base}/logout`, {}, { responseType: 'text' }).subscribe({
      next: () => localStorage.clear(),
      error: () => localStorage.clear()
    });
  }
  getCurrentUser() {
    return {
      id: localStorage.getItem('userId'),
      email: localStorage.getItem('userEmail'),
      name: localStorage.getItem('userName'),
      role: localStorage.getItem('userRole'),
      phone: localStorage.getItem('userPhone'),
      createdAt: localStorage.getItem('userCreatedAt')
    };
  }
  isLoggedIn(): boolean {
    return !!localStorage.getItem('userId');
  }
  getRole(): string | null {
    return localStorage.getItem('userRole');
  }
  forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(`${this.base}/forgot-password`, { email });
  }
  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post<any>(`${this.base}/reset-password`, { token, newPassword });
  }
}
