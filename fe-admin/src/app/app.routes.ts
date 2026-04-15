import {Routes} from '@angular/router';
import {Login} from "./core/component/login/login";
import {loginGuard} from "./core/guard/login-guard";
import {MainLayout} from "./core/component/main-layout/main-layout";
import {authGuard} from "./core/guard/auth-guard";
import {Dashboard} from "./features/dashboard/dashboard";
import {PageNotFound} from "./features/page-not-found/page-not-found";
import {Appointment} from "./features/appointment/appointment";
import {Patient} from "./features/patient/patient";
import {Medicine} from "./features/medicine/medicine";
import {Staff} from "./features/staff/staff";
import {Permission} from "./features/permission/permission";
import {Treatment} from "./features/treatment/treatment";

export const routes: Routes = [
    {
        path: "login",
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
                redirectTo: "dashboard",
                pathMatch: "full",
            },
            {
                path: "dashboard",
                title: "Thống kê",
                component: Dashboard,
                canActivate: []
            },
            {
                path: "staffs",
                title: "Nhân Viên",
                component: Staff,
            },
            {
                path: "appointments",
                title: "Lịch hẹn",
                component: Appointment,
            },
            {
                path: "patients",
                title: "Bệnh nhân",
                component: Patient,
            },
            {
                path: "treatments",
                title: "Dịch vụ",
                component: Treatment,
            },
            {
                path: "medicines",
                title: "Thuốc",
                component: Medicine,
            },
            {
                path: "permissions",
                title: "Phân quyền",
                component: Permission,
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
