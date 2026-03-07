import {computed, inject, Injectable, signal} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {Router} from "@angular/router";
import {JwtHelperService} from "@auth0/angular-jwt";
import {LoginRequest} from "../model/request/login-request";
import {catchError, Observable, startWith, tap, throwError} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {TokenResponse} from "../model/response/token-response";

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    private readonly url = ENV.API_BASE_URL + "auth";
    private readonly http = inject(HttpClient);
    private readonly router = inject(Router);
    private readonly SESSION_KEY = 'hasSession';
    // private readonly jwtHelper = inject(JwtHelperService);
    
    private accessToken = signal<string | null>(null);
    private expiresAt = signal<number | null>(null);
    
    private storeAccessToken(data: TokenResponse): void {
        this.accessToken.set(data.accessToken);
        this.expiresAt.set(Date.now() + data.accessExpiresIn * 1000);
        localStorage.setItem(this.SESSION_KEY, 'true');
    }
    
    private clearMemory(): void {
        this.accessToken.set(null);
        this.expiresAt.set(null);
        localStorage.removeItem(this.SESSION_KEY);
    }
    
    getAccessToken() {
        return this.accessToken();
    }
    
    hasSession():boolean{
        return localStorage.getItem(this.SESSION_KEY) === 'true';
    }
    
    readonly isLoggedIn = computed(() =>
            !!this.accessToken() && !!this.expiresAt() && Date.now() < this.expiresAt()!
    );
    
    login(loginRequest: LoginRequest): Observable<SuccessResponse<TokenResponse>> {
        return this.http.post<SuccessResponse<TokenResponse>>(
                `${this.url}/login`,
                loginRequest,
                {withCredentials: true}
        ).pipe(
                tap(res => this.storeAccessToken(res.data))
        );
    }
    
    logout(): void {
        this.http.post(
                `${this.url}/logout`,
                {},
                {
                    withCredentials: true,
                    headers:
                            {
                                Authorization: `Bearer ${this.accessToken()}`
                            }
                }
        ).subscribe({
            complete: () => {
                this.clearMemory();
                this.router.navigate(['/dang-nhap']);
            },
            error: () => {
                this.clearMemory();
                this.router.navigate(['/dang-nhap']);
            }
        });
    }
    
    refreshToken(): Observable<SuccessResponse<TokenResponse>> {
        return this.http.post<SuccessResponse<TokenResponse>>(
                `${this.url}/refresh`,
                {},
                {withCredentials: true}
        ).pipe(
                tap(res => this.storeAccessToken(res.data)),
                catchError((err: HttpErrorResponse) => {
                    this.clearMemory();
                    this.router.navigate(['/dang-nhap']);
                    return throwError(() => err);
                })
        );
    }
}
