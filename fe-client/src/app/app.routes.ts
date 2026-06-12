import { Routes } from '@angular/router';
import { MainLayout } from './core/component/main-layout/main-layout';
import { authGuard } from './core/guard/auth-guard';

export const routes: Routes = [
  {
    path: 'login',
    redirectTo: '',
    pathMatch: 'full',
  },
  {
    path: '',
    component: MainLayout,
    children: [
      {
        path: '',
        title: 'MC Smiles',
        loadComponent: () => import('./features/home/home').then((m) => m.Home),
      },
      {
        path: 'booking',
        title: 'Đặt lịch khám',
        loadComponent: () => import('./features/booking/booking').then((m) => m.Booking),
        canActivate: [authGuard],
      },
      {
        path: 'appointments',
        title: 'Quản lý lịch khám',
        loadComponent: () => import('./features/appointment/appointment').then((m) => m.Appointment),
        canActivate: [authGuard],
      },
      {
        path: 'health-records',
        title: 'Hồ sơ sức khỏe',
        loadComponent: () =>
          import('./features/health-record/health-record').then((m) => m.HealthRecord),
        canActivate: [authGuard],
      },
      {
        path: 'profile',
        title: 'Thông tin cá nhân',
        loadComponent: () => import('./features/profile/profile').then((m) => m.Profile),
        canActivate: [authGuard],
      },
    ],
  },
  {
    path: '404',
    loadComponent: () =>
      import('./features/page-not-found/page-not-found').then((m) => m.PageNotFound),
  },
  {
    path: '**',
    redirectTo: '/404',
  },
];
