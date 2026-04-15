import {CanActivateFn, Router} from '@angular/router';
import {inject} from "@angular/core";
import {AuthService} from "../service/auth.service";

export const loginGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);
    if (!authService.isLoggedIn()) {
        return true;
    }
    return router.createUrlTree(["/dashboard"]);
};
