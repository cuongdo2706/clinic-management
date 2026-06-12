import {computed, inject, Injectable, signal} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {Router} from "@angular/router";
import {LoginRequest} from "../model/request/login-request";
import {catchError, Observable, tap, throwError} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {TokenResponse} from "../model/response/token-response";

@Injectable({
    providedIn: 'root',
})
export class AuthService {
    private readonly url = `${ENV.API_BASE_URL}/auth`;
    private readonly http = inject(HttpClient);
    private readonly router = inject(Router);
    private readonly SESSION_KEY = 'hasSession';
    
    private accessToken = signal<string | null>(null);
    private expiresAt = signal<number | null>(null);
    private roles = signal<string[]>([]);
    private permissions = signal<string[]>([]);
    private username = signal<string | null>(null);
    
    private storeAccessToken(data: TokenResponse): void {
        this.accessToken.set(data.accessToken);
        this.expiresAt.set(Date.now() + data.accessExpiresIn * 1000);
        const payload = this.decodePayload(data.accessToken);
        this.roles.set(this.extractRoles(payload));
        this.permissions.set(this.extractPermissions(payload));
        this.username.set(typeof payload?.['sub'] === 'string' ? payload['sub'] : null);
        localStorage.setItem(this.SESSION_KEY, 'true');
    }
    
    private clearMemory(): void {
        this.accessToken.set(null);
        this.expiresAt.set(null);
        this.roles.set([]);
        this.permissions.set([]);
        this.username.set(null);
        localStorage.removeItem(this.SESSION_KEY);
    }
    
    getAccessToken() {
        return this.accessToken();
    }

    getUsername(): string | null {
        return this.username();
    }

    getRoles(): string[] {
        return this.roles();
    }

    hasRole(role: string): boolean {
        return this.roles().includes(role);
    }

    hasAnyRole(roles: string[]): boolean {
        return roles.some(role => this.hasRole(role));
    }

    getPermissions(): string[] {
        return this.permissions();
    }

    hasPermission(permission: string): boolean {
        return this.permissions().includes(permission);
    }

    hasAllPermissions(permissions: string[]): boolean {
        return permissions.every(permission => this.hasPermission(permission));
    }
    
    hasSession():boolean{
        return localStorage.getItem(this.SESSION_KEY) === 'true';
    }
    
    readonly isLoggedIn = computed(() =>
            !!this.accessToken() && !!this.expiresAt() && Date.now() < this.expiresAt()!
    );
    
    login(loginRequest: LoginRequest): Observable<SuccessResponse<TokenResponse>> {
        return this.http.post<SuccessResponse<TokenResponse>>(
                `${this.url}/clinic/login`,
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
                this.router.navigate(['/login']);
            },
            error: () => {
                this.clearMemory();
                this.router.navigate(['/login']);
            }
        });
    }
    
    refreshToken(): Observable<SuccessResponse<TokenResponse>> {
        return this.http.post<SuccessResponse<TokenResponse>>(
                `${this.url}/clinic/refresh`,
                {},
                {withCredentials: true}
        ).pipe(
                tap(res => this.storeAccessToken(res.data)),
                catchError((err: HttpErrorResponse) => {
                    this.clearMemory();
                    this.router.navigate(['/login']);
                    return throwError(() => err);
                })
        );
    }

    private decodePayload(token: string): Record<string, unknown> | null {
        const parts = token.split('.');
        if (parts.length < 2) {
            return null;
        }

        try {
            const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
            const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
            const decoded = atob(padded);
            return JSON.parse(decoded) as Record<string, unknown>;
        } catch {
            return null;
        }
    }

    private extractRoles(payload: Record<string, unknown> | null): string[] {
        const roleClaim = payload?.['role'];
        if (Array.isArray(roleClaim)) {
            return roleClaim.filter((item): item is string => typeof item === 'string');
        }
        if (typeof roleClaim === 'string') {
            return [roleClaim];
        }
        return [];
    }

    private extractPermissions(payload: Record<string, unknown> | null): string[] {
        const permissionClaim = payload?.['permission'];
        if (Array.isArray(permissionClaim)) {
            return permissionClaim.filter((item): item is string => typeof item === 'string');
        }
        if (typeof permissionClaim === 'string') {
            return [permissionClaim];
        }
        return [];
    }
}
