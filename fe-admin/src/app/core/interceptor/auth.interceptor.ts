import {HttpErrorResponse, HttpInterceptorFn} from "@angular/common/http";
import {inject} from "@angular/core";
import {AuthService} from "../service/auth.service";
import {catchError, switchMap, throwError} from "rxjs";

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const isAuthEndpoint = req.url.includes('/auth/');
    const authReq = authService.getAccessToken() && !isAuthEndpoint
            ? req.clone({
                setHeaders: { Authorization: `Bearer ${authService.getAccessToken()}` },
                withCredentials: true
            })
            : req.clone({ withCredentials: true });
    return next(authReq).pipe(
            catchError((err: HttpErrorResponse) => {
                // accessToken hết hạn → tự refresh rồi retry
                if (err.status === 401 && !isAuthEndpoint) {
                    return authService.refreshToken().pipe(
                            switchMap(res =>
                                    next(req.clone({
                                        setHeaders: { Authorization: `Bearer ${res.data.accessToken}` },
                                        withCredentials: true
                                    }))
                            )
                    );
                }
                return throwError(() => err);
            })
    );
};