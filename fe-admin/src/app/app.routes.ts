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
import {Procedure} from "./features/procedure/procedure";
import {roleGuard} from "./core/guard/role-guard";
import {Examination} from "./features/examination/examination";
import {Accounts} from "./features/accounts/accounts";

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
                canActivate: [roleGuard],
                data: {permissions: ['DASHBOARD:VIEW']},
            },
            {
                path: "staffs",
                title: "Nhân Viên",
                component: Staff,
                canActivate: [roleGuard],
                data: {permissions: ['STAFF:VIEW']},
            },
            {
                path: "appointments",
                title: "Lịch hẹn",
                component: Appointment,
                canActivate: [roleGuard],
                data: {permissions: ['APPOINTMENT:VIEW']},
            },
            {
                path: "examinations",
                title: "Khám bệnh",
                component: Examination,
                canActivate: [roleGuard],
                data: {roles: ['DENTIST'], permissions: ['EXAMINATION:VIEW']},
            },
            {
                path: "patients",
                title: "Bệnh nhân",
                component: Patient,
                canActivate: [roleGuard],
                data: {permissions: ['PATIENT:VIEW']},
            },
            {
                path: "procedures",
                title: "Dịch vụ",
                component: Procedure,
                canActivate: [roleGuard],
                data: {permissions: ['PROCEDURE:VIEW']},
            },
            {
                path: "medicines",
                title: "Thuốc",
                component: Medicine,
                canActivate: [roleGuard],
                data: {permissions: ['MEDICINE:VIEW']},
            },
            {
                path: "permissions",
                title: "Phân quyền",
                component: Permission,
                canActivate: [roleGuard],
                data: {roles: ['ADMIN']},
            },
            {
                path: "accounts",
                title: "Tài khoản",
                component: Accounts,
                canActivate: [roleGuard],
                data: {roles: ['ADMIN']},
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
