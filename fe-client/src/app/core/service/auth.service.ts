import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { ENV } from '../../environment';
import { LoginRequest } from '../model/request/login-request';
import { RegisterRequest } from '../model/request/register-request';
import { PatientProfileResponse } from '../model/response/patient-profile-response';
import { SuccessResponse } from '../model/response/success-response';
import { TokenResponse } from '../model/response/token-response';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly url = `${ENV.API_BASE_URL}/auth`;
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly sessionKey = 'clientHasSession';

  private readonly accessToken = signal<string | null>(null);
  private readonly expiresAt = signal<number | null>(null);

  readonly isLoggedIn = computed(
    () => !!this.accessToken() && !!this.expiresAt() && Date.now() < this.expiresAt()!,
  );

  getAccessToken(): string | null {
    return this.accessToken();
  }

  hasSession(): boolean {
    return localStorage.getItem(this.sessionKey) === 'true';
  }

  login(request: LoginRequest): Observable<SuccessResponse<TokenResponse>> {
    return this.http
      .post<SuccessResponse<TokenResponse>>(`${this.url}/client/login`, request, { withCredentials: true })
      .pipe(tap((res) => this.storeAccessToken(res.data)));
  }

  register(request: RegisterRequest): Observable<SuccessResponse<PatientProfileResponse>> {
    return this.http.post<SuccessResponse<PatientProfileResponse>>(`${this.url}/register`, request);
  }

  logout(): void {
    this.http
      .post(
        `${this.url}/logout`,
        {},
        {
          withCredentials: true,
          headers: this.accessToken() ? { Authorization: `Bearer ${this.accessToken()}` } : {},
        },
      )
      .subscribe({
        complete: () => {
          this.clearSession();
          this.router.navigate(['/']);
        },
        error: () => {
          this.clearSession();
          this.router.navigate(['/']);
        },
      });
  }

  refreshToken(): Observable<SuccessResponse<TokenResponse>> {
    return this.http
      .post<SuccessResponse<TokenResponse>>(`${this.url}/client/refresh`, {}, { withCredentials: true })
      .pipe(
        tap((res) => this.storeAccessToken(res.data)),
        catchError((err: HttpErrorResponse) => {
          this.clearSession();
          return throwError(() => err);
        }),
      );
  }

  clearSession(): void {
    this.accessToken.set(null);
    this.expiresAt.set(null);
    localStorage.removeItem(this.sessionKey);
  }

  private storeAccessToken(data: TokenResponse): void {
    this.accessToken.set(data.accessToken);
    this.expiresAt.set(Date.now() + data.accessExpiresIn * 1000);
    localStorage.setItem(this.sessionKey, 'true');
  }
}
