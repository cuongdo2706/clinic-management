import {inject} from "@angular/core";
import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from "../service/auth.service";
import {MessageService} from "primeng/api";

export const roleGuard: CanActivateFn = (route) => {
    const authService = inject(AuthService);
    const router = inject(Router);
    const messageService = inject(MessageService);
    const roles = (route.data?.['roles'] as string[] | undefined) ?? [];
    const permissions = (route.data?.['permissions'] as string[] | undefined) ?? [];

    const allowedByRole = roles.length === 0 || authService.hasAnyRole(roles);
    const allowedByPermission = permissions.length === 0 || authService.hasAllPermissions(permissions);

    if (allowedByRole && allowedByPermission) {
        return true;
    }

    messageService.add({
        severity: 'error',
        summary: 'Không có quyền',
        detail: 'Tài khoản của bạn không có quyền truy cập vào chức năng này',
    });
    return router.createUrlTree(['/dashboard']);
};
