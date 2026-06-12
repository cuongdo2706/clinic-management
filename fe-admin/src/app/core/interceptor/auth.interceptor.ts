import {HttpErrorResponse, HttpInterceptorFn} from "@angular/common/http";
import {inject} from "@angular/core";
import {AuthService} from "../service/auth.service";
import {catchError, switchMap, throwError} from "rxjs";
import {MessageService} from "primeng/api";

const ACCESS_DENIED_MESSAGE = 'Tài khoản của bạn không có quyền truy cập vào chức năng này';
let lastAccessDeniedToastAt = 0;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const messageService = inject(MessageService);
    const isAuthEndpoint = req.url.includes('/auth/');
    const authReq = authService.getAccessToken() && !isAuthEndpoint
            ? req.clone({
                setHeaders: { Authorization: `Bearer ${authService.getAccessToken()}` },
                withCredentials: true
            })
            : req.clone({ withCredentials: true });
    return next(authReq).pipe(
            catchError((err: HttpErrorResponse) => {
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
                if (err.status === 403 && !isAuthEndpoint) {
                    const now = Date.now();
                    if (now - lastAccessDeniedToastAt > 2000) {
                        lastAccessDeniedToastAt = now;
                        messageService.add({
                            severity: 'error',
                            summary: 'Không có quyền',
                            detail: ACCESS_DENIED_MESSAGE,
                        });
                    }
                }
                return throwError(() => err);
            })
    );
};
