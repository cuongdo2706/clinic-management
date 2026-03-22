import {Routes} from '@angular/router';
import {Login} from "./core/component/login/login";
import {loginGuard} from "./core/guard/login-guard";
import {MainLayout} from "./core/component/main-layout/main-layout";
import {authGuard} from "./core/guard/auth-guard";
import {Dashboard} from "./features/dashboard/dashboard";
import {PageNotFound} from "./features/page-not-found/page-not-found";
import {Dentist} from "./features/dentist/dentist";
import {Appointment} from "./features/appointment/appointment";
import {Patient} from "./features/patient/patient";
import {ClinicService} from "./features/service/clinic-service";

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
                canActivate: []
            },
            {
                path: "nha-si",
                title: "Nha sĩ",
                component: Dentist,
            },
            {
                path: "lich-hen",
                title: "Lịch hẹn",
                component: Appointment,
            },
            {
                path: "benh-nhan",
                title: "Bệnh nhân",
                component: Patient,
            },
            {
                path: "dich-vu",
                title: "Dịch vụ",
                component: ClinicService,
            },
            {
                path: "kham-benh",
                title: "Luồng khám bệnh",
                loadComponent: () => import("./features/examination/examination").then(m => m.Examination),
            },
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
