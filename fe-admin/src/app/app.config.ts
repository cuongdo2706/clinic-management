import {APP_INITIALIZER, ApplicationConfig, importProvidersFrom, inject, provideAppInitializer, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection} from '@angular/core';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import {routes} from './app.routes';
import {providePrimeNG} from "primeng/config";
import Aura from '@primeuix/themes/aura';
import {JwtModule} from "@auth0/angular-jwt";
import {provideHttpClient, withInterceptors} from "@angular/common/http";
import {authInterceptor} from "./core/interceptor/auth.interceptor";
import {AuthService} from "./core/service/auth.service";
import {catchError, of} from "rxjs";

export const appConfig: ApplicationConfig = {
    providers: [
        provideBrowserGlobalErrorListeners(),
        provideRouter(routes, withComponentInputBinding()),
        providePrimeNG({
            theme: {
                preset: Aura,
                options: {
                    darkModeSelector: false,
                    cssLayer: {
                        name: 'primeng',
                        order: 'theme, base, primeng'
                    }
                }
            }
        }),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideAppInitializer(() => {
            const authService = inject(AuthService);
            if (!authService.hasSession()) {
                return of(null);
            }
            return authService.refreshToken().pipe(
                    catchError(() => of(null)) // chưa login → bỏ qua
            );
        }),
    ]
};
