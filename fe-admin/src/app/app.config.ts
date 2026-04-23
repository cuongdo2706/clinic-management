import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners, LOCALE_ID} from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeVi from '@angular/common/locales/vi';
import {provideRouter, withComponentInputBinding} from '@angular/router';
import {routes} from './app.routes';
import {providePrimeNG} from "primeng/config";
import Aura from '@primeuix/themes/aura';
import {definePreset} from '@primeuix/themes';
import {provideHttpClient, withInterceptors} from "@angular/common/http";
import {authInterceptor} from "./core/interceptor/auth.interceptor";
import {AuthService} from "./core/service/auth.service";
import {catchError, of} from "rxjs";

registerLocaleData(localeVi);

const GreenPreset = definePreset(Aura, {
    semantic: {
        primary: {
            50: '{green.50}',
            100: '{green.100}',
            200: '{green.200}',
            300: '{green.300}',
            400: '{green.400}',
            500: '{green.500}',
            600: '{green.600}',
            700: '{green.700}',
            800: '{green.800}',
            900: '{green.900}',
            950: '{green.950}',
        },
    }
});

export const appConfig: ApplicationConfig = {
    providers: [
        { provide: LOCALE_ID, useValue: 'vi' },
        provideBrowserGlobalErrorListeners(),
        provideRouter(routes, withComponentInputBinding()),
        providePrimeNG({
            theme: {
                preset: GreenPreset,
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
