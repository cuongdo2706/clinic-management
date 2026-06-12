import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../service/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const isAuthEndpoint = req.url.includes('/auth/');
  const token = authService.getAccessToken();
  const authReq =
    token && !isAuthEndpoint
      ? req.clone({
          setHeaders: { Authorization: `Bearer ${token}` },
          withCredentials: true,
        })
      : req.clone({ withCredentials: true });

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !isAuthEndpoint) {
        return authService.refreshToken().pipe(
          switchMap((res) =>
            next(
              req.clone({
                setHeaders: { Authorization: `Bearer ${res.data.accessToken}` },
                withCredentials: true,
              }),
            ),
          ),
          catchError((refreshErr: HttpErrorResponse) => {
            const returnUrl = router.url && router.url !== '/login' ? router.url : '/';
            router.navigate(['/'], { queryParams: { login: 'true', returnUrl } });
            return throwError(() => refreshErr);
          }),
        );
      }
      return throwError(() => err);
    }),
  );
};
