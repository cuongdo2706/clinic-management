import {Routes} from '@angular/router';
import {Login} from "./core/component/login/login";
import {loginGuard} from "./core/guard/login-guard";
import {MainLayout} from "./core/component/main-layout/main-layout";
import {authGuard} from "./core/guard/auth-guard";
import {Dashboard} from "./features/dashboard/dashboard";
import {PageNotFound} from "./features/page-not-found/page-not-found";

export const routes: Routes = [
    {
        path: "dang-nhap",
        component: Login,
        canActivate: [loginGuard]
    },
    {
        path: "",
        component: MainLayout,
        canActivate: [authGuard],
        children: [
            {
                path: "",
                redirectTo: "thong-ke",
                pathMatch: "full",
            },
            {
                path: "thong-ke",
                title: "Thống kê",
                component: Dashboard,
                canActivate:[]
            }
        ]
    },
    {
        path: "404",
        component: PageNotFound
    },
    {
        path: "**",
        redirectTo: "/404"
    },
];
